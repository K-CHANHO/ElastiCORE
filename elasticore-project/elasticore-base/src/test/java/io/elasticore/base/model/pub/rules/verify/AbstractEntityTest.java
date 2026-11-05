package io.elasticore.base.model.pub.rules.verify;

import io.elasticore.base.model.pub.rules.schema.DdlConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
 * {@code @abstract(entity)}: an abstract {@code @Entity}, the root of a hierarchy
 * that is never stored as itself, whose subclasses one repository and one
 * service read together. It has no create: nothing in the generated code may
 * write {@code new} of it. Plain {@code @abstract} stays a mapped superclass.
 */
public class AbstractEntityTest {

    private static final String MODEL = String.join(
            "\n",
            "entity:",
            "  AeCode:",
            "    meta: entity @abstract(entity) @expose(20) @jpa:Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)",
            "    fields:",
            "      code: string(10) @id",
            "      name: string(50)! @search(like)",
            "  AeStatusCode:",
            "    meta: entity @extend(AeCode) @expose(20)",
            "    fields:",
            "      terminal: boolean",
            "  AeRegionCode:",
            "    meta: entity @extend(AeCode) @expose(20)",
            "    fields:",
            "      country: string(2)",
            // A relation to the root: its rows are in several tables.
            "  AeTicket:",
            "    meta: entity @expose(20)",
            "    fields:",
            "      ticketId: long @id @sequence",
            "      status: AeCode @ManyToOne",
            // Plain @abstract with a key, which stays what it was.
            "  AeStamped:",
            "    meta: entity @abstract",
            "    fields:",
            "      stampId: long @id @sequence",
            "      stampedBy: string(20)",
            "  AeStampedNote:",
            "    meta: entity @extend(AeStamped)",
            "    fields:",
            "      note: string(100)",
            "");

    private static final List<File> projects = new ArrayList<File>();

    private static File layered;
    private static File hexagonal;
    private static File kotlin;
    private static File typescript;

    @BeforeAll
    static void generate() throws Exception {
        layered = run("elcore-abstract-layered", "layered", "java", "jpa", MODEL, 0);
        hexagonal = run("elcore-abstract-hex", "hexagonal", "java", "jpa", MODEL, 0);
        kotlin = run("elcore-abstract-kt", "layered", "kotlin", "jpa", MODEL, 0);
        typescript = run("elcore-abstract-ts", "layered", "typescript", "jpa", MODEL, 0);
    }

    @AfterAll
    static void cleanUp() throws Exception {
        java.lang.reflect.Field tmp =
                io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        if (System.getenv("KEEP_ABSTRACT") != null) return;
        for (File project : projects) delete(project);
    }

    @Test
    public void itIsAnAbstractEntity() throws Exception {
        for (File project : new File[] {layered, hexagonal}) {
            String entity = found(project, "AeCode.java");
            assertTrue(entity.contains("@Entity\n"), entity);
            assertFalse(entity.contains("@MappedSuperclass"), entity);
            assertTrue(entity.contains("@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)"), entity);
            assertTrue(entity.contains("public abstract class AeCode"), entity);
        }
        String entity = found(kotlin, "AeCode.kt");
        assertTrue(entity.contains("@Entity\n"), entity);
        assertTrue(entity.contains("abstract class AeCode"), entity);
    }

    @Test
    public void plainAbstractStaysAMappedSuperclass() throws Exception {
        for (File project : new File[] {layered, hexagonal}) {
            String entity = found(project, "AeStamped.java");
            assertTrue(entity.contains("@MappedSuperclass"), entity);
            assertFalse(entity.contains("@Entity\n"), entity);
            assertFalse(found(project, "Q.java").contains("$AeStamped<"), "plain @abstract has no Q class");
            // Its subclass has its own table, and it takes the frame's columns.
            String ddl = read(new File(project, "src/main/resources/db/schema-postgresql.sql"));
            assertFalse(ddl.contains("CREATE TABLE ae_stamped "), ddl);
            assertTrue(ddl.contains("CREATE TABLE ae_stamped_note "), ddl);
        }
    }

    @Test
    public void itHasARepositoryAndAQClass() throws Exception {
        for (File project : new File[] {layered, hexagonal}) {
            String repository = found(project, "AeCodeRepository.java");
            assertTrue(repository.contains("AeCode"), repository);
            assertTrue(found(project, "Q.java").contains("$AeCode<"), "the Q class of AeCode");
        }
        assertTrue(found(kotlin, "AeCodeRepository.kt").contains("AeCode"));
    }

    @Test
    public void theServiceReadsSearchesUpdatesAndDeletesButDoesNotCreate() throws Exception {
        for (File project : new File[] {layered, hexagonal}) {
            String service = found(project, "AeCodeCoreService.java");
            assertFalse(service.contains(" save("), service);
            assertFalse(service.contains("toEntity"), service);
            for (String kept : new String[] {" findById(", " findBySearch(", " update(", " deleteById("}) {
                assertTrue(service.contains(kept), kept + "\n" + service);
            }
        }
        String service = found(kotlin, "AeCodeCoreService.kt");
        assertFalse(service.contains("fun save("), service);
        assertFalse(service.contains("toEntity"), service);
        assertTrue(service.contains("fun update("), service);
    }

    @Test
    public void theControllerHasNoPost() throws Exception {
        for (File project : new File[] {layered, hexagonal}) {
            String controller = found(project, "AeCodeController.java");
            assertFalse(
                    controller.contains("@PostMapping\n") || controller.contains("@PostMapping(" + "\"\")"),
                    controller);
            assertFalse(controller.contains(" create("), controller);
            assertTrue(controller.contains("@GetMapping(\"/{code}\")"), controller);
            assertTrue(controller.contains("@PostMapping(\"/search\")"), controller);
            assertTrue(controller.contains("@DeleteMapping(\"/{code}\")"), controller);
        }
    }

    @Test
    public void theMapperDoesNotMakeOne() throws Exception {
        for (File project : new File[] {layered, hexagonal}) {
            String mapper = found(project, "RefdataMapper.java");
            assertFalse(mapper.contains("new AeCode()"), mapper);
            assertFalse(mapper.contains("AeCode toEntity("), mapper);
            // The update path, and the way back to a DTO, are there.
            assertTrue(mapper.contains("mapping(AeCodeDTO from, AeCode to"), mapper);
            assertTrue(mapper.contains("mapping(AeCode from, AeCodeDTO to"), mapper);
            // A subclass is concrete, and made as ever.
            assertTrue(mapper.contains("new AeStatusCode()"), mapper);
            // A relation to it holds a proxy: there is nothing to make a stub of.
            assertTrue(mapper.contains("em.getReference(AeCode.class, from.getStatusCode())"), mapper);
        }
        String mapper = found(kotlin, "RefdataMapper.kt");
        assertFalse(mapper.contains("= AeCode()"), mapper);
        assertFalse(mapper.contains("fun toEntity(from: AeCodeDTO"), mapper);
        assertTrue(mapper.contains("AeStatusCode()"), mapper);
    }

    @Test
    public void aHexagonalProjectHasNoCreateUseCase() throws Exception {
        String useCase = found(hexagonal, "AeCodeUseCase.java");
        assertFalse(useCase.contains(" save("), useCase);
        assertFalse(useCase.contains(" create("), useCase);
        assertTrue(useCase.contains(" update("), useCase);
        assertTrue(useCase.contains(" findById("), useCase);
    }

    @Test
    public void theGeneratedServiceTestDoesNotMakeOne() throws Exception {
        String test = found(kotlin, "AeCodeServiceTest.kt");
        assertFalse(test.contains("= AeCode()"), test);
        assertFalse(test.contains("save_"), test);
        assertTrue(test.contains("Mockito.mock(AeCode::class.java"), test);
        // Any other entity's test is as it was.
        String other = found(kotlin, "AeTicketServiceTest.kt");
        assertTrue(other.contains("val entity = AeTicket()"), other);
        assertTrue(other.contains("save_"), other);
    }

    @Test
    public void theTypeScriptClientHasNoCreate() throws Exception {
        String api = found(typescript, "AeCodeApi.ts");
        assertFalse(api.contains("create"), api);
        assertTrue(api.contains("search"), api);
        assertTrue(api.contains("update"), api);
    }

    /** Under TABLE_PER_CLASS an abstract class has no table, and so no foreign key can point at one. */
    @Test
    public void theSchemaHasNoTableForItAndNoForeignKeyToIt() throws Exception {
        for (File project : new File[] {layered, hexagonal, kotlin}) {
            String ddl = read(new File(project, "src/main/resources/db/schema-postgresql.sql"));
            assertFalse(ddl.contains("CREATE TABLE ae_code "), ddl);
            assertTrue(ddl.contains("CREATE TABLE ae_status_code "), ddl);
            assertTrue(ddl.contains("CREATE TABLE ae_region_code "), ddl);
            // Each table has the root's columns.
            int status = ddl.indexOf("CREATE TABLE ae_status_code ");
            String table = ddl.substring(status, ddl.indexOf(");", status));
            assertTrue(table.contains("code VARCHAR(10) NOT NULL"), table);
            assertTrue(table.contains("name VARCHAR(50) NOT NULL"), table);
            assertTrue(table.contains("PRIMARY KEY (code)"), table);
            assertTrue(ddl.contains("status_code VARCHAR(10)"), ddl);
            assertFalse(ddl.contains("REFERENCES ae_code"), ddl);
        }
    }

    /** With a strategy that gives the root a table, the table and the foreign key are there. */
    @Test
    public void aJoinedRootHasItsTable() throws Exception {
        File joined =
                run("elcore-abstract-joined", "layered", "java", "jpa", MODEL.replace("TABLE_PER_CLASS", "JOINED"), 0);
        String ddl = read(new File(joined, "src/main/resources/db/schema-postgresql.sql"));
        assertTrue(ddl.contains("CREATE TABLE ae_code "), ddl);
        assertTrue(ddl.contains("REFERENCES ae_code"), ddl);
    }

    @Test
    public void myBatisRefusesIt() throws Exception {
        File project = run("elcore-abstract-mybatis", "layered", "java", "mybatis", MODEL, 1);
        String log = read(new File(project, "elcore.log"));
        assertTrue(log.contains("mode: mybatis does not write an abstract entity"), log);
    }

    @Test
    public void aRootWithoutAKeyIsRefused() throws Exception {
        File project = run(
                "elcore-abstract-nokey",
                "layered",
                "java",
                "jpa",
                String.join(
                        "\n",
                        "entity:",
                        "  AeNoKey:",
                        "    meta: entity @abstract(entity)",
                        "    fields:",
                        "      name: string(50)",
                        ""),
                1);
        String log = read(new File(project, "elcore.log"));
        assertTrue(log.contains("AeNoKey: @abstract(entity) is the root of a hierarchy, and a root has the key"), log);
    }

    /** The 2.x-compatible output is not a place for it: there it reads as @abstract, as 2.x does. */
    @Test
    public void theCompatibleOutputReadsItAsPlainAbstract() throws Exception {
        File compat = run("elcore-abstract-compat", "layered", "java", "jpa", MODEL, 0, "elasticore.rules.v2");
        String mapper = found(compat, "RefdataMapper.java");
        assertTrue(mapper.contains("new AeCode()"), mapper);
        assertTrue(found(compat, "AeCodeCoreService.java").contains(" save("));
    }

    private static File run(String name, String architecture, String language, String mode, String model, int expected)
            throws Exception {
        return run(name, architecture, language, mode, model, expected, "elasticore.rules.v3");
    }

    private static File run(
            String name, String architecture, String language, String mode, String model, int expected, String rules)
            throws Exception {
        File project = Files.createTempDirectory(name).toFile();
        projects.add(project);
        File dir = new File(project, "src/main/resources/blueprint/refdata");
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(
                new File(dir, "env.yml").toPath(),
                String.join(
                                "\n",
                                "elasticore:",
                                "  enable: true",
                                "  config:",
                                "    domainName: refdata",
                                "    framework: springboot",
                                "    mode: " + mode,
                                "    j2ee: jakarta",
                                "    testCode: \"true\"",
                                "  namespace:",
                                "    base: io.acme.refdata",
                                "")
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), model.getBytes(StandardCharsets.UTF_8));

        String[] keys = {
            "elasticore.base.path",
            "elasticore.architecture",
            "elasticore.language",
            "elasticore.rules.force",
            "elasticore.rules.v3",
            "elasticore.rules.v2",
            "elasticore.rules.ts",
            DdlConfig.DATABASES
        };
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        PrintStream out = System.out;
        try {
            for (String k : keys) System.clearProperty(k);
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", architecture);
            System.setProperty("elasticore.language", "typescript".equals(language) ? "java" : language);
            System.setProperty("elasticore.rules.force", "true");
            System.setProperty(rules, "true");
            if ("typescript".equals(language)) System.setProperty("elasticore.rules.ts", "true");
            System.setProperty(DdlConfig.DATABASES, "postgresql");
            System.setOut(new PrintStream(log, true, "UTF-8"));
            int code = RulesGenRunner.run(new String[0]);
            System.setOut(out);
            assertEquals(expected, code, new String(log.toByteArray(), StandardCharsets.UTF_8));
        } finally {
            System.setOut(out);
            Files.write(new File(project, "elcore.log").toPath(), log.toByteArray());
            for (int i = 0; i < keys.length; i++) {
                if (saved[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], saved[i]);
            }
            io.elasticore.base.model.pub.rules.ModelCheck.reset();
        }
        return project;
    }

    private static String read(File file) throws IOException {
        assertTrue(file.isFile(), file + " was not written");
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String found(File project, String fileName) throws IOException {
        List<File> hits = new ArrayList<File>();
        collect(project, fileName, hits);
        assertEquals(1, hits.size(), fileName + " in " + project + ": " + hits);
        return read(hits.get(0));
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
