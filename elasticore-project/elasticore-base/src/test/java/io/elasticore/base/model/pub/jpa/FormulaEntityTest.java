package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.extract.ModelExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A @formula field is mapped by @Formula alone. Hibernate reads @Column first
 * and then ignores @Formula, so a @Column from the field's length mapped a
 * real column (created by ddl-auto, read and written as data) instead of the
 * expression; @Comment on a formula fails the boot ("value involves formulas").
 *
 * Generates a small project with the 2.x publishers and reads the entity
 * and schema.yml.
 */
public class FormulaEntityTest {

    private static final String EXPR =
            "coalesce((select m.name from fx_member m where m.member_id = created_by), created_by)";

    private static final String ENV =
            "elasticore:\n"
            + "  enable: true\n"
            + "  config:\n"
            + "    domainName: formulafx\n"
            + "    framework: springboot\n"
            + "    mode: jpa\n"
            + "    j2ee: jakarta\n"
            + "  namespace:\n"
            + "    entity: io.fx.entity\n"
            + "    dto: io.fx.dto\n"
            + "    enumeration: io.fx.enums\n"
            + "    repository: io.fx.repository\n"
            + "    service: io.fx.service\n"
            + "    control: io.fx.controller\n";

    private static final String MODEL =
            "entity:\n"
            + "  FxMember:\n"
            + "    meta: entity @table(fx_member)\n"
            + "    fields:\n"
            + "      memberId: string(15) @id\n"
            + "      createdBy: string(20) -- creator\n"
            + "      createdByNm: string(100) @readonly @updatable(false) @formula(\"" + EXPR + "\") -- creator name\n"
            + "      modifiedByNm: string! @unique @formula(\"" + EXPR + "\")\n"
            + "      name: string(100) -- member name\n";

    @TempDir
    Path dir;

    @Test
    public void aFormulaIsMappedByTheExpressionOnly() throws Exception {
        Path domain = dir.resolve("src/main/resources/blueprint/formulafx");
        Files.createDirectories(domain.resolve("model"));
        Files.write(domain.resolve("env.yml"), ENV.getBytes(StandardCharsets.UTF_8));
        Files.write(domain.resolve("model/entity.yml"), MODEL.getBytes(StandardCharsets.UTF_8));

        // Generate in a JVM of its own, as './gradlew elcore' does: the extractor
        // keeps the loaded model in static state that other tests here expect empty.
        Path log = dir.resolve("gen.log");
        Process p = new ProcessBuilder(
                Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xmx256m",
                "-Dfile.encoding=UTF-8",
                "-Delasticore.base.path=" + dir,
                "-cp", System.getProperty("java.class.path"),
                ModelExtractor.class.getName())
                .directory(dir.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
        try {
            assertTrue(p.waitFor(120, TimeUnit.SECONDS), "generation did not finish");
        } finally {
            p.destroyForcibly();
        }
        String genLog = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        assertEquals(0, p.exitValue(), genLog);

        Path entity = dir.resolve("src/main/java/io/fx/entity/FxMember.java");
        assertTrue(Files.exists(entity), "not generated: " + entity + "\n" + genLog);
        String src = new String(Files.readAllBytes(entity), StandardCharsets.UTF_8);

        String formula = annotationsOf(src, "private String createdByNm;");
        assertTrue(formula.contains("@Formula(\"" + EXPR + "\")"), formula);
        assertFalse(formula.contains("@Column"), "a @Column makes Hibernate ignore @Formula:\n" + formula);
        assertFalse(formula.contains("@Comment"), "Hibernate fails on @Comment on a formula:\n" + formula);

        // Not only a declared length: a length guessed from the name, '!' and @unique made a @Column too.
        String guessed = annotationsOf(src, "private String modifiedByNm;");
        assertTrue(guessed.contains("@Formula(\"" + EXPR + "\")"), guessed);
        assertFalse(guessed.contains("@Column"), guessed);

        String name = annotationsOf(src, "private String name;");
        assertTrue(name.contains("@Column(") && name.contains("length = 100)"), "an ordinary field keeps its column:\n" + name);
        assertTrue(name.contains("@Comment(\"member name\")"), name);

        // schema.yml describes the table: the formula is listed, not as a column.
        String schema = new String(Files.readAllBytes(
                dir.resolve("src/main/resources/blueprint/schema.yml")), StandardCharsets.UTF_8);
        assertTrue(schema.contains("createdByNm: string(100) @Formula"), schema);
        assertFalse(schema.contains("created_by_nm"), schema);
    }

    /** The lines between the previous declaration and this one. */
    private static String annotationsOf(String src, String declaration) {
        int end = src.indexOf(declaration);
        assertTrue(end >= 0, "missing " + declaration + " in\n" + src);
        int start = src.lastIndexOf(';', end - 1) + 1;
        return src.substring(start, end);
    }
}
