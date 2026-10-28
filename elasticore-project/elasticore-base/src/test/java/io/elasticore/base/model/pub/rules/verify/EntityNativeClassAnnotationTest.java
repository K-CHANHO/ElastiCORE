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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An annotation the DSL hands to the entity class as written - a soft-delete
 * filter, @jpa:Where(...) - belongs on the class in every mode. The
 * compatibility output always wrote it; the clean and Kotlin outputs dropped
 * it without a word, and every query read the deleted rows again. The line is
 * written against the two packages 2.x's entity template imports on demand,
 * so a file carrying one imports them too; a file without one is unchanged.
 */
public class EntityNativeClassAnnotationTest {

    private static final String MODEL = String.join("\n",
            "entity:",
            "  Accident:",
            "    meta: entity @jpa:Where(clause = \"delete_yn = 'N'\")",
            "    fields:",
            "      accidentId: long @id @sequence",
            "      deleteYn: string(1)",
            "  Vehicle:",
            "    meta: entity @jpa:Inheritance(strategy = InheritanceType.JOINED)",
            "    fields:",
            "      vehicleId: long @id @sequence",
            "  Garage:",
            "    meta: entity",
            "    fields:",
            "      garageId: long @id @sequence",
            "");

    private static final String WHERE = "@Where(clause = \"delete_yn = 'N'\")";

    private static final List<File> projects = new ArrayList<File>();

    private static File compat;
    private static File java;
    private static File kotlin;

    @BeforeAll
    static void generate() throws Exception {
        compat = run("elcore-native-compat", "java", false);
        java = run("elcore-native-java", "java", true);
        kotlin = run("elcore-native-kt", "kotlin", true);
    }

    @AfterAll
    static void clean() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        for (File project : projects) delete(project);
    }

    @Test
    public void theCompatibleOutputKeepsTheLine() throws Exception {
        assertTrue(classAnnotations(found(compat, "Accident.java"), "class Accident").contains(WHERE));
    }

    @Test
    public void theCleanOutputWritesTheLineOnTheClass() throws Exception {
        String entity = found(java, "Accident.java");
        assertTrue(classAnnotations(entity, "class Accident").contains(WHERE), entity);
        assertTrue(entity.contains("import org.hibernate.annotations.*;"), entity);
        assertTrue(entity.contains("import jakarta.persistence.*;"), entity);
    }

    @Test
    public void aTypeTheLineNamesResolvesThroughTheSameImports() throws Exception {
        String entity = found(java, "Vehicle.java");
        assertTrue(classAnnotations(entity, "class Vehicle")
                .contains("@Inheritance(strategy = InheritanceType.JOINED)"), entity);
        assertTrue(entity.contains("import jakarta.persistence.*;"), entity);
    }

    @Test
    public void kotlinWritesTheLineOnTheClass() throws Exception {
        String entity = found(kotlin, "Accident.kt");
        assertTrue(classAnnotations(entity, "class Accident").contains(WHERE), entity);
        assertTrue(entity.contains("import org.hibernate.annotations.*\n"), entity);
        assertTrue(entity.contains("import jakarta.persistence.*\n"), entity);
    }

    @Test
    public void anEntityWithoutALineImportsWhatItUsesOnly() throws Exception {
        assertFalse(found(java, "Garage.java").contains(".*;"));
        assertFalse(found(kotlin, "Garage.kt").contains(".*\n"));
    }

    /** The text above the class declaration: where a class annotation is. */
    private static String classAnnotations(String source, String declaration) {
        int at = source.indexOf(declaration);
        assertTrue(at > 0, source);
        return source.substring(0, at);
    }

    private static File run(String name, String language, boolean v3) throws Exception {
        File project = Files.createTempDirectory(name).toFile();
        projects.add(project);
        File dir = new File(project, "src/main/resources/blueprint/claim");
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(new File(dir, "env.yml").toPath(), String.join("\n",
                "elasticore:", "  enable: true", "  config:", "    domainName: claim", "    framework: springboot",
                "    mode: jpa", "    j2ee: jakarta", "  namespace:", "    entity: io.acme.claim.entity",
                "    dto: io.acme.claim.dto", "    repository: io.acme.claim.repository",
                "    service: io.acme.claim.service", "    control: io.acme.claim.controller", "")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), MODEL.getBytes(StandardCharsets.UTF_8));

        String[] keys = {"elasticore.base.path", "elasticore.architecture", "elasticore.language",
                "elasticore.rules.force", "elasticore.rules.v3", "elasticore.rules.ts"};
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        try {
            for (String k : keys) System.clearProperty(k);
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", "layered");
            System.setProperty("elasticore.language", language);
            System.setProperty("elasticore.rules.force", "true");
            if (v3) System.setProperty("elasticore.rules.v3", "true");
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
        return new String(Files.readAllBytes(hits.get(0).toPath()), StandardCharsets.UTF_8).replace("\r\n", "\n");
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
