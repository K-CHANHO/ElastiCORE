package io.elasticore.base.model.pub.rules.verify;

import io.elasticore.base.extract.ModelExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An entity named like an annotation the generated code uses - a board's
 * Comment and Hibernate's @Comment, which every field with a "-- label" gets.
 *
 * The rule engine imported org.hibernate.annotations.Comment into the entity
 * files, and that import shadowed the entity of the same package: PostBase's
 * List of Comment meant the annotation, and nothing compiled. The 2.x
 * templates import org.hibernate.annotations.*, which loses to the entity, so
 * there @Comment("...") named the entity and did not compile either.
 *
 * Each generator writes the annotation in full where the domain has a type of
 * its name, and by its simple name as before where it does not. An annotation
 * a rule writes as a fixed line - the DTOs' @Size and @Schema - keeps its
 * simple name and so has to keep its import, whatever the domain is called.
 */
public class ModelNamedLikeAnAnnotationTest {

    private static final String BOARD =
            "entity:\n"
            + "  Post:\n"
            + "    meta: entity @root -- post\n"
            + "    fields:\n"
            + "      postId:    long @id @sequence\n"
            + "      title:     string(100)! -- title\n"
            + "  Comment:\n"
            + "    meta: entity @root -- comment\n"
            + "    fields:\n"
            + "      commentId: long @id @sequence\n"
            + "      content:   string(500)! -- comment text\n"
            + "      post:      Post! -- parent post\n";

    /** The same board with the comment called something no annotation is called. */
    private static final String CONTROL = BOARD.replace("Comment:", "Reply:").replace("commentId", "replyId");

    /** @Comment by its simple name; Kotlin may put a use-site target first. */
    private static final Pattern SIMPLE_COMMENT = Pattern.compile("(?m)^\\s*@(?:field:|get:|param:)?Comment\\(");
    /** The import, in Java or Kotlin. */
    private static final Pattern COMMENT_IMPORT_LINE = Pattern.compile("(?m)^import org\\.hibernate\\.annotations\\.Comment;?\\s*$");
    private static final String COMMENT_IMPORT = "import org.hibernate.annotations.Comment;";

    @TempDir
    Path dir;

    @Test
    public void the2xGeneratorWritesHibernatesCommentInFull() throws Exception {
        Path project = project("v2", BOARD, env2x());
        generate(project, ModelExtractor.class.getName());
        String comment = read(project.resolve("src/main/java/io/board/entity/Comment.java"));
        assertTrue(comment.contains("@org.hibernate.annotations.Comment(\"comment text\")"), comment);
        assertNoSimpleComment(project);
    }

    @Test
    public void theRuleEngineWritesItInFullOnLayeredJava() throws Exception {
        Path project = project("layered", BOARD, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=layered",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"comment text\")"), tree(project));
        assertNoSimpleComment(project);
    }

    /** Layered Java is the v3 code since 3.0.261028; the 2.x-compatible code is asked for, and checked too. */
    @Test
    public void theRuleEngineWritesItInFullOnLayeredJavaKeeping2x() throws Exception {
        Path project = project("layered-v2", BOARD, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=layered",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8", "-Delasticore.rules.v2=true");
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"comment text\")"), tree(project));
        assertNoSimpleComment(project);
    }

    @Test
    public void theRuleEngineWritesItInFullOnHexagonal() throws Exception {
        Path project = project("hexagonal", BOARD, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"comment text\")"), tree(project));
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"title\")"),
                "Post refers to the Comment entity too, so its own labels are written in full\n" + tree(project));
        assertNoSimpleComment(project);
    }

    @Test
    public void theRuleEngineWritesItInFullInKotlin() throws Exception {
        Path project = project("kotlin", BOARD, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=layered",
                "-Delasticore.language=kotlin", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(project, "org.hibernate.annotations.Comment(\"comment text\")"), tree(project));
        assertNoSimpleComment(project);
    }

    /**
     * An enum named Size and an entity named Schema, next to a product whose DTOs carry @Size and
     * @Schema as fixed lines: those lines keep their simple names, so their imports have to stay.
     * (A field of the type Size in the same DTO is another matter: two types then claim the name,
     * neither is imported, and the fixed @Size line does not compile. That case is not handled yet.)
     */
    @Test
    public void annotationsWrittenAsFixedLinesKeepTheirImports() throws Exception {
        String model = "entity:\n"
                + "  Product:\n"
                + "    meta: entity @root @expose(20) -- product\n"
                + "    fields:\n"
                + "      productId: long @id @sequence\n"
                + "      name:      string(50)! -- product name\n"
                + "  Schema:\n"
                + "    meta: entity @root @expose(20) -- schema\n"
                + "    fields:\n"
                + "      schemaId:  long @id @sequence\n"
                + "      title:     string(100)! -- schema title\n"
                + "enumeration:\n"
                + "  Size:\n"
                + "    meta: enum -- size\n"
                + "    fields:\n"
                + "      code: string(1)\n"
                + "    enum:\n"
                + "      SMALL: S\n"
                + "      LARGE: L\n";
        Path project = project("fixed-lines", model, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        int checked = assertImportedWhereUsed(project, "Size", "jakarta.validation.constraints.Size")
                + assertImportedWhereUsed(project, "Schema", "io.swagger.v3.oas.annotations.media.Schema");
        assertTrue(checked > 0, "no generated file writes @Size or @Schema by its simple name\n" + tree(project));
    }

    /**
     * Entities named Setter and JoinColumn, where the same file writes lombok's @Setter and
     * @JoinColumn both as a fixed line and as a built annotation: the import serves both, so it stays.
     */
    @Test
    public void anAnnotationAlsoWrittenAsAFixedLineKeepsItsImport() throws Exception {
        String model = "entity:\n"
                + "  Pet:\n"
                + "    meta: entity @root @expose(20) -- pet\n"
                + "    fields:\n"
                + "      petId:     long @id @sequence\n"
                + "      name:      string(50)! -- pet name\n"
                + "      vet:       Vet! @db(vet_id) -- vet\n"
                + "      owner:     Owner @db(owner_id) -- owner\n"
                + "  Vet:\n"
                + "    meta: entity @root -- vet\n"
                + "    fields:\n"
                + "      vetId:     long @id @sequence\n"
                + "      name:      string(50)! -- vet name\n"
                + "  Owner:\n"
                + "    meta: entity @root -- owner\n"
                + "    fields:\n"
                + "      ownerId:   long @id @sequence\n"
                + "      name:      string(50)! -- owner name\n"
                + "  Setter:\n"
                + "    meta: entity @root -- setter\n"
                + "    fields:\n"
                + "      setterId:  long @id @sequence\n"
                + "      label:     string(10) -- label\n"
                + "  JoinColumn:\n"
                + "    meta: entity @root -- join column\n"
                + "    fields:\n"
                + "      joinColumnId: long @id @sequence\n"
                + "      label:        string(10) -- label\n";
        Path project = project("fixed-and-built", model, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        int checked = assertImportedWhereUsed(project, "Setter", "lombok.Setter")
                + assertImportedWhereUsed(project, "JoinColumn", "jakarta.persistence.JoinColumn");
        assertTrue(checked > 0, "no generated file writes @Setter or @JoinColumn by its simple name\n" + tree(project));
    }

    @Test
    public void withoutSuchAnEntityTheAnnotationStaysShort() throws Exception {
        Path v2 = project("control-v2", CONTROL, env2x());
        generate(v2, ModelExtractor.class.getName());
        assertTrue(read(v2.resolve("src/main/java/io/board/entity/Reply.java")).contains("@Comment(\"comment text\")"));

        Path v3 = project("control-v3", CONTROL, env3());
        generate(v3, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(v3, COMMENT_IMPORT), tree(v3));
        assertFalse(anyFileContains(v3, "@org.hibernate.annotations.Comment("), tree(v3));
    }

    // ------------------------------------------------------------------------------------------------

    private static String env2x() {
        return "elasticore:\n"
                + "  enable: true\n"
                + "  config:\n"
                + "    domainName: board\n"
                + "    framework: springboot\n"
                + "    mode: jpa\n"
                + "    j2ee: jakarta\n"
                + "  namespace:\n"
                + "    entity: io.board.entity\n"
                + "    dto: io.board.dto\n"
                + "    enumeration: io.board.enums\n"
                + "    repository: io.board.repository\n"
                + "    service: io.board.service\n"
                + "    control: io.board.controller\n";
    }

    private static String env3() {
        return "elasticore:\n"
                + "  enable: true\n"
                + "  config:\n"
                + "    domainName: board\n"
                + "    framework: springboot\n"
                + "    mode: jpa\n"
                + "    j2ee: jakarta\n"
                + "    apiPath: \"/api\"\n"
                + "  namespace:\n"
                + "    base: io.board\n";
    }

    private Path project(String name, String model, String env) throws IOException {
        Path project = dir.resolve(name);
        Path domain = project.resolve("src/main/resources/blueprint/board");
        Files.createDirectories(domain.resolve("model"));
        Files.write(domain.resolve("env.yml"), env.getBytes(StandardCharsets.UTF_8));
        Files.write(domain.resolve("model/entity.yml"), model.getBytes(StandardCharsets.UTF_8));
        return project;
    }

    /**
     * Generates in a JVM of its own, as the Gradle tasks do: both generators keep the loaded model in
     * static state that other tests here expect empty.
     */
    private static void generate(Path project, String mainClass, String... properties) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Xmx384m");
        command.add("-Dfile.encoding=UTF-8");
        command.add("-Delasticore.base.path=" + project);
        for (String p : properties) command.add(p);
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(mainClass);
        Path log = project.resolve("gen.log");
        Process p = new ProcessBuilder(command).directory(project.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(p.waitFor(180, TimeUnit.SECONDS), "generation did not finish");
        } finally {
            p.destroyForcibly();
        }
        assertEquals(0, p.exitValue(), read(log));
    }

    private static void assertNoSimpleComment(Path project) throws IOException {
        for (Path file : javaFiles(project)) {
            String src = read(file);
            assertFalse(COMMENT_IMPORT_LINE.matcher(src).find(), file + " imports Hibernate's Comment over the entity:\n" + src);
            assertFalse(SIMPLE_COMMENT.matcher(src).find(), file + " writes @Comment, which names the entity:\n" + src);
        }
    }

    /** Every file that writes @name by its simple name imports fqcn; returns how many such files there were. */
    private static int assertImportedWhereUsed(Path project, String name, String fqcn) throws IOException {
        Pattern use = Pattern.compile("(?m)^\\s*@" + name + "\\b");
        int files = 0;
        for (Path file : javaFiles(project)) {
            String src = read(file);
            if (!use.matcher(src).find()) continue;
            files++;
            assertTrue(src.contains("import " + fqcn + ";"),
                    file + " writes @" + name + " but does not import " + fqcn + ":\n" + src);
        }
        return files;
    }

    private static boolean anyFileContains(Path project, String text) throws IOException {
        for (Path file : javaFiles(project)) {
            if (read(file).contains(text)) return true;
        }
        return false;
    }

    /** The generated Java and Kotlin sources. */
    private static List<Path> javaFiles(Path project) throws IOException {
        List<Path> out = new ArrayList<Path>();
        for (String root : new String[]{"src/main/java", "src/main/kotlin"}) {
            Path dir = project.resolve(root);
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> files = Files.walk(dir)) {
                out.addAll(files.filter(f -> f.toString().endsWith(".java") || f.toString().endsWith(".kt"))
                        .sorted().collect(Collectors.toList()));
            }
        }
        return out;
    }

    private static String tree(Path project) throws IOException {
        StringBuilder sb = new StringBuilder("generated:\n");
        for (Path f : javaFiles(project)) sb.append("  ").append(project.relativize(f)).append('\n');
        return sb.append(read(project.resolve("gen.log"))).toString();
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
