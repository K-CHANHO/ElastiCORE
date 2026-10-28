package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A relation marked @fetchjoin (or every relation, under table.autoJoin) is
 * fetched with the root by 2.x's CoreService (output = 'v2'). The v3 one -
 * the default, and every Kotlin build - has no JOIN FETCH at all, so the
 * relation loads a query per row - and said nothing. The run now warns,
 * naming the relations; the 2.x-compatible run, which does fetch them, does
 * not.
 */
public class FetchJoinWarningTest {

    private static final String MODEL = String.join("\n",
            "entity:",
            "  Customer:",
            "    meta: entity @expose",
            "    fields:",
            "      customerId: long @id @sequence",
            "  Purchase:",
            "    meta: entity @expose",
            "    fields:",
            "      purchaseId: long @id @sequence",
            "      customer: Customer @ManyToOne @fetchjoin",
            "  Coupon:",
            "    meta: entity @expose",
            "    fields:",
            "      couponId: long @id @sequence",
            "      customer: Customer @ManyToOne",
            "");

    private static final String WARNING = "[Shop] Purchase: customer (@fetchjoin) is not fetched with Purchase";

    private static final List<File> projects = new ArrayList<File>();

    @AfterAll
    static void cleanUp() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        for (File project : projects) delete(project);
    }

    @Test
    public void theV3RunWarnsOfTheRelationItDoesNotFetch() throws Exception {
        String out = run("elcore-fetchjoin-v3", "java", true);
        assertTrue(out.contains(WARNING), out);
        assertFalse(out.contains("Coupon: customer"), out);
    }

    @Test
    public void theKotlinRunWarnsToo() throws Exception {
        assertTrue(run("elcore-fetchjoin-kt", "kotlin", true).contains(WARNING));
    }

    @Test
    public void theHexagonalRunWarnsToo() throws Exception {
        // Hexagonal and DDD are v3 bundles: the same CoreService, the same missing fetch.
        assertTrue(run("elcore-fetchjoin-hex", "java", true, "hexagonal").contains(WARNING));
    }

    @Test
    public void theCompatibleRunFetchesItAndSaysNothing() throws Exception {
        String out = run("elcore-fetchjoin-compat", "java", false);
        assertFalse(out.contains("is not fetched with"), out);
    }

    /** Generates the model and returns what the run printed. */
    private static String run(String name, String language, boolean v3) throws Exception {
        return run(name, language, v3, "layered");
    }

    private static String run(String name, String language, boolean v3, String architecture) throws Exception {
        File project = Files.createTempDirectory(name).toFile();
        projects.add(project);
        File dir = new File(project, "src/main/resources/blueprint/shop");
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(new File(dir, "env.yml").toPath(), String.join("\n",
                "elasticore:", "  enable: true", "  config:", "    domainName: shop", "    framework: springboot",
                "    mode: jpa", "    j2ee: jakarta", "  namespace:",
                "    entity: io.acme.shop.entity", "    dto: io.acme.shop.dto",
                "    repository: io.acme.shop.repository", "    service: io.acme.shop.service",
                "    control: io.acme.shop.controller", "")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), MODEL.getBytes(StandardCharsets.UTF_8));

        String[] keys = {"elasticore.base.path", "elasticore.architecture", "elasticore.language",
                "elasticore.rules.force", "elasticore.rules.v3", "elasticore.rules.v2", "elasticore.rules.ts"};
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        PrintStream original = System.out;
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        try {
            for (String k : keys) System.clearProperty(k);
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", architecture);
            System.setProperty("elasticore.language", language);
            System.setProperty("elasticore.rules.force", "true");
            System.setProperty(v3 ? "elasticore.rules.v3" : "elasticore.rules.v2", "true");
            System.setOut(new PrintStream(printed, true, "UTF-8"));
            assertEquals(0, RulesGenRunner.run(new String[0]), "the run failed: " + project);
        } finally {
            System.setOut(original);
            for (int i = 0; i < keys.length; i++) {
                if (saved[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], saved[i]);
            }
        }
        return new String(printed.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void delete(File root) throws IOException {
        if (!root.exists()) return;
        Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
