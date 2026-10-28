package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The extension service of a domain with supportAI describes its tools by
 * the entity's name with the label beside it - "Accident (car accident)" -
 * as 2.x writes it. The rule engine wrote the label alone, so a model lost
 * the name a request is put in. An entity without a label is described by
 * its name, as before.
 */
public class AiToolDescriptionTest {

    private static final String MODEL = String.join("\n",
            "entity:",
            "  Accident:",
            "    meta: entity @expose -- car accident",
            "    fields:",
            "      accidentId: long @id @sequence",
            "  Garage:",
            "    meta: entity @expose",
            "    fields:",
            "      garageId: long @id @sequence",
            "");

    private static final List<File> projects = new ArrayList<File>();

    private static File compat;
    private static File clean;

    @BeforeAll
    static void generate() throws Exception {
        compat = run("elcore-ai-tool-compat", false);
        clean = run("elcore-ai-tool-v3", true);
    }

    @AfterAll
    static void cleanUp() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        for (File project : projects) delete(project);
    }

    @Test
    public void aLabelledEntityIsNamedWithItsLabelBeside() throws Exception {
        for (File project : new File[]{compat, clean}) {
            String service = found(project, "AccidentService.java");
            assertTrue(service.contains("\"Retrieves all Accident (car accident) records without sorting\""), service);
        }
    }

    @Test
    public void anEntityWithoutALabelIsNamedAlone() throws Exception {
        for (File project : new File[]{compat, clean}) {
            String service = found(project, "GarageService.java");
            assertTrue(service.contains("\"Retrieves all Garage records without sorting\""), service);
        }
    }

    private static File run(String name, boolean v3) throws Exception {
        File project = Files.createTempDirectory(name).toFile();
        projects.add(project);
        File dir = new File(project, "src/main/resources/blueprint/claim");
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(new File(dir, "env.yml").toPath(), String.join("\n",
                "elasticore:", "  enable: true", "  config:", "    domainName: claim", "    framework: springboot",
                "    mode: jpa", "    j2ee: jakarta", "    supportAI: true", "  namespace:",
                "    entity: io.acme.claim.entity", "    dto: io.acme.claim.dto",
                "    repository: io.acme.claim.repository", "    service: io.acme.claim.service",
                "    control: io.acme.claim.controller", "")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), MODEL.getBytes(StandardCharsets.UTF_8));

        String[] keys = {"elasticore.base.path", "elasticore.architecture", "elasticore.language",
                "elasticore.rules.force", "elasticore.rules.v3", "elasticore.rules.v2", "elasticore.rules.ts"};
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        try {
            for (String k : keys) System.clearProperty(k);
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", "layered");
            System.setProperty("elasticore.language", "java");
            System.setProperty("elasticore.rules.force", "true");
            System.setProperty(v3 ? "elasticore.rules.v3" : "elasticore.rules.v2", "true");
            assertEquals(0, RulesGenRunner.run(new String[0]), "the run failed: " + project);
        } finally {
            for (int i = 0; i < keys.length; i++) {
                if (saved[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], saved[i]);
            }
        }
        return project;
    }

    private static String found(File project, String fileName) throws IOException {
        List<File> hits = new ArrayList<File>();
        collect(project, fileName, hits);
        assertEquals(1, hits.size(), fileName + " in " + project + ": " + hits);
        return new String(Files.readAllBytes(hits.get(0).toPath()), StandardCharsets.UTF_8);
    }

    private static void collect(File dir, String fileName, List<File> hits) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) collect(f, fileName, hits);
            else if (f.getName().equals(fileName)) hits.add(f);
        }
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
