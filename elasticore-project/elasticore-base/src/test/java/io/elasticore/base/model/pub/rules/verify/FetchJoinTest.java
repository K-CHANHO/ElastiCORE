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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A relation marked @fetchjoin (or every relation, under table.autoJoin) is
 * fetched with the root in the paged search - by 2.x's CoreService
 * (output = 'v2') and, since 3.0.261029-rc4, by the v3 one: the default, every
 * hexagonal, DDD and Kotlin build.
 *
 * Until then the v3 service had no JOIN FETCH, so the relation loaded a query
 * per row, and an extension service calling withJoinFetches() did not
 * compile; a build before this release warned of it. The method is written only for an
 * entity that asks for a fetch. That the fetch takes the per-row selects
 * away is shown on a running application: elasticore-template-ddd's
 * JoinFetchTest.
 */
public class FetchJoinTest {

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

    private static final List<File> projects = new ArrayList<File>();

    @AfterAll
    static void cleanUp() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        for (File project : projects) delete(project);
    }

    @Test
    public void theV3ServiceFetchesTheMarkedRelationInThePagedSearch() throws Exception {
        File project = run("elcore-fetchjoin-v3", "java", true, "layered");
        String purchase = generated(project, "PurchaseCoreService.java");
        assertTrue(purchase.contains("protected Specification<Purchase> withJoinFetches() {"), purchase);
        assertTrue(purchase.contains("root.fetch(\"customer\", jakarta.persistence.criteria.JoinType.LEFT);"), purchase);
        assertTrue(purchase.contains("if (query != null && !Long.class.equals(query.getResultType())) {"), purchase);
        assertTrue(purchase.contains("toSpec(searchDTO).and(withJoinFetches());"), purchase);
        // Only the paged search fetches, as in 2.x.
        assertEquals(1, count(purchase, ".and(withJoinFetches())"), purchase);

        // A relation not marked is not fetched, and its service is as it was.
        String coupon = generated(project, "CouponCoreService.java");
        assertFalse(coupon.contains("withJoinFetches"), coupon);
    }

    @Test
    public void theKotlinServiceFetchesItToo() throws Exception {
        File project = run("elcore-fetchjoin-kt", "kotlin", true, "layered");
        String purchase = generated(project, "PurchaseCoreService.kt");
        assertTrue(purchase.contains("protected fun withJoinFetches(): Specification<Purchase> = Specification { root, query, _ ->"),
                purchase);
        assertTrue(purchase.contains("root.fetch<Purchase, Any>(\"customer\", jakarta.persistence.criteria.JoinType.LEFT)"),
                purchase);
        assertTrue(purchase.contains("toSpec(searchDTO).and(withJoinFetches())"), purchase);
        assertFalse(generated(project, "CouponCoreService.kt").contains("withJoinFetches"));
    }

    @Test
    public void theHexagonalServiceFetchesItToo() throws Exception {
        // Hexagonal and DDD are v3 bundles: the same CoreService, searching through its port.
        File project = run("elcore-fetchjoin-hex", "java", true, "hexagonal");
        String purchase = generated(project, "PurchaseCoreService.java");
        assertTrue(purchase.contains("root.fetch(\"customer\", jakarta.persistence.criteria.JoinType.LEFT);"), purchase);
        assertTrue(purchase.contains(".and(withJoinFetches());"), purchase);
    }

    @Test
    public void theRunNoLongerWarns() throws Exception {
        // A build before this release warned that the v3 service did not fetch; it does now.
        File project = run("elcore-fetchjoin-quiet", "java", true, "layered");
        assertFalse(lastOutput.contains("is not fetched with"), lastOutput);
        assertTrue(project.isDirectory());
    }

    @Test
    public void theCompatibleServiceStillFetchesIt() throws Exception {
        File project = run("elcore-fetchjoin-compat", "java", false, "layered");
        String purchase = generated(project, "PurchaseCoreService.java");
        assertTrue(purchase.contains("root.fetch(\"customer\", JoinType.LEFT);"), purchase);
        assertFalse(lastOutput.contains("is not fetched with"), lastOutput);
    }

    private static String lastOutput = "";

    /** Generates the model; what the run printed is kept in lastOutput. */
    private static File run(String name, String language, boolean v3, String architecture) throws Exception {
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
        lastOutput = new String(printed.toByteArray(), StandardCharsets.UTF_8);
        return project;
    }

    private static String generated(File project, String fileName) throws IOException {
        try (Stream<Path> all = Files.walk(project.toPath())) {
            Path found = all.filter(p -> p.getFileName().toString().equals(fileName)).findFirst().orElse(null);
            assertTrue(found != null, "not generated: " + fileName + " in " + project);
            return new String(Files.readAllBytes(found), StandardCharsets.UTF_8);
        }
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + 1)) n++;
        return n;
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
