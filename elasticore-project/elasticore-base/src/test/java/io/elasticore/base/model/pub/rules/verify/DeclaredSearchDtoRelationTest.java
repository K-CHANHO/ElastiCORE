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
 * A search DTO the model declares (@searchable(entity=...)) carries every
 * @search(eq) relation of its entity as an equality property, required or
 * not, of this domain or another - as 2.x writes it. The rule engine dropped
 * the required ones, so a search by such a relation lost its condition
 * without a word. A search DTO derived from the entity's @search carries
 * the flattened id alone, in 2.x and here alike.
 */
public class DeclaredSearchDtoRelationTest {

    private static final String ORGANIZATION = String.join("\n",
            "entity:",
            "  Company:",
            "    meta: entity @expose(20)",
            "    fields:",
            "      companyId: long @id @sequence",
            "      name: string(100)",
            "");

    private static final String SALES = String.join("\n",
            "entity:",
            "  Branch:",
            "    meta: entity @expose(20)",
            "    fields:",
            "      branchId: long @id @sequence",
            "  Sale:",
            "    meta: entity @expose(20)",
            "    fields:",
            "      saleId: long @id @sequence",
            "      insurer: Company! @search(eq)",
            "      vendor: Company @search(eq)",
            "      branch: Branch! @search(eq)",
            "  Delivery:",
            "    meta: entity @expose(20)",
            "    fields:",
            "      deliveryId: long @id @sequence",
            "      branch: Branch! @search(eq)",
            "dto:",
            "  SaleSrchDTO:",
            "    meta: dto @searchable(entity=Sale, pageable=true)",
            "    fields:",
            "      saleNo: string",
            "");

    private static final List<File> projects = new ArrayList<File>();

    private static File compat;
    private static File clean;

    @BeforeAll
    static void generate() throws Exception {
        compat = run("elcore-srch-relation-compat", false);
        clean = run("elcore-srch-relation-v3", true);
    }

    @AfterAll
    static void cleanUp() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        for (File project : projects) delete(project);
    }

    @Test
    public void theDeclaredSearchDtoKeepsARequiredRelation() throws Exception {
        for (File project : new File[]{compat, clean}) {
            String dto = found(project, "SaleSrchDTO.java");
            assertTrue(dto.contains("private Company insurer;"), dto);
            assertTrue(dto.contains("private Branch branch;"), dto);
            assertTrue(dto.contains("private Company vendor;"), dto);
        }
    }

    @Test
    public void theMapperSearchesByIt() throws Exception {
        for (File project : new File[]{compat, clean}) {
            String mapper = found(project, "SalesMapper.java");
            assertTrue(mapper.contains("r.get(\"insurer\")"), mapper);
            assertTrue(mapper.contains("r.get(\"branch\")"), mapper);
        }
    }

    @Test
    public void aDerivedSearchDtoCarriesTheFlattenedIdAlone() throws Exception {
        for (File project : new File[]{compat, clean}) {
            String dto = found(project, "DeliverySrchDTO.java");
            assertFalse(dto.contains("private Branch branch;"), dto);
            assertTrue(dto.contains("branchBranchId"), dto);
        }
    }

    private static File run(String name, boolean v3) throws Exception {
        File project = Files.createTempDirectory(name).toFile();
        projects.add(project);
        domain(project, "organization", ORGANIZATION);
        domain(project, "sales", SALES);

        String[] keys = {"elasticore.base.path", "elasticore.architecture", "elasticore.language",
                "elasticore.rules.force", "elasticore.rules.v3", "elasticore.rules.ts"};
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        try {
            for (String k : keys) System.clearProperty(k);
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", "layered");
            System.setProperty("elasticore.language", "java");
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

    private static void domain(File project, String name, String model) throws IOException {
        File dir = new File(project, "src/main/resources/blueprint/" + name);
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(new File(dir, "env.yml").toPath(), String.join("\n",
                "elasticore:", "  enable: true", "  config:", "    domainName: " + name, "    framework: springboot",
                "    mode: jpa", "    j2ee: jakarta", "  namespace:",
                "    entity: io.acme." + name + ".entity", "    dto: io.acme." + name + ".dto",
                "    repository: io.acme." + name + ".repository", "    service: io.acme." + name + ".service",
                "    control: io.acme." + name + ".controller", "")
                .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), model.getBytes(StandardCharsets.UTF_8));
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
