package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A process manager reacting to other contexts' events, generated the way a
 * project generates it: what it can have is a handler, what it cannot is a
 * DSL warning naming why - never an import of a class nobody writes.
 */
public class CrossContextGenerationTest {

    private static File project;
    private static String log = "";

    private static String env(String name, String rules) {
        return String.join("\n",
                "elasticore:",
                "  enable: true",
                "  config:",
                "    domainName: " + name,
                "    framework: springboot",
                "    mode: jpa",
                "    j2ee: jakarta",
                rules,
                "  namespace:",
                "    base: io.acme." + name,
                "");
    }

    /** shop raises OrderPlaced from its root; its member's @event raises nothing. */
    private static final String SHOP = String.join("\n",
            "entity:",
            "  Order:",
            "    meta: entity @root @expose(20) @event(Placed(total))",
            "    fields:",
            "      orderId: long @id @sequence",
            "      total:   long",
            "      lines:   List<OrderLine> @OneToMany(mappedBy=\"order\")",
            "  OrderLine:",
            "    meta: entity @event(Added)",
            "    fields:",
            "      lineId: long @id @sequence",
            "      order:  Order @ManyToOne",
            "");

    /** catalog declares an event, but runs the layered v3 bundle, which writes no event classes. */
    private static final String CATALOG = String.join("\n",
            "entity:",
            "  Book:",
            "    meta: entity @expose(20) @event(Published)",
            "    fields:",
            "      bookId: long @id @sequence",
            "");

    private static final String BILLING = String.join("\n",
            "entity:",
            "  Receipt:",
            "    meta: entity @root @expose(20)",
            "    fields:",
            "      receiptId: long @id @sequence",
            "      orderId:   long",
            "processManager:",
            "  Receipting:",
            "    on: [shop:OrderPlaced, shop:OrderPlacedEvent, shop:OrderLineAdded, catalog:BookPublished, press:ArticlePublished, nowhere:Placed, OrderPlaced]",
            "    uses: [Receipt]",
            "");

    /** press runs the hexagonal bundle but suppresses ddd-event: its events are never written. */
    private static final String PRESS = String.join("\n",
            "entity:",
            "  Article:",
            "    meta: entity @root @expose(20) @event(Published)",
            "    fields:",
            "      articleId: long @id @sequence",
            "");

    /** A context that only reacts: nothing to hang its process manager on. */
    private static final String NOTIFY = String.join("\n",
            "processManager:",
            "  Welcome:",
            "    on: [shop:OrderPlaced]",
            "");

    private static void domain(String name, String env, String model) throws Exception {
        File dir = new File(project, "src/main/resources/blueprint/" + name);
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(new File(dir, "env.yml").toPath(), env.getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), model.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeAll
    static void generate() throws Exception {
        project = Files.createTempDirectory("elcore-xctx").toFile();
        domain("shop", env("shop", ""), SHOP);
        domain("catalog", env("catalog", "    publisher:\n      rules: [jpa-springboot-v3]"), CATALOG);
        domain("billing", env("billing", ""), BILLING);
        domain("notify", env("notify", ""), NOTIFY);
        domain("press", env("press", "    publisher:\n      suppress: [ddd-event]"), PRESS);

        String[] keys = {"elasticore.base.path", "elasticore.architecture", "elasticore.language", "elasticore.rules.force"};
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        java.io.PrintStream out = System.out;
        java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(captured, true, "UTF-8"));
        try {
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", "hexagonal");
            System.setProperty("elasticore.language", "java");
            System.setProperty("elasticore.rules.force", "true");
            RulesGenRunner.main(new String[0]);
        } finally {
            System.setOut(out);
            for (int i = 0; i < keys.length; i++) {
                if (saved[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], saved[i]);
            }
        }
        log = new String(captured.toByteArray(), StandardCharsets.UTF_8);
        out.print(log);
    }

    @AfterAll
    static void clean() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        SpecFactory.valueObjectsInDto(false);
        SpecFactory.rootsSplitByDefault(false);
        SpecFactory.commandOnlyRoots(false);
        if (project == null || !project.exists()) return;
        Files.walkFileTree(project.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws java.io.IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, java.io.IOException e) throws java.io.IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** Both halves: the regenerated base and the developer's class. */
    private static String processManager() throws Exception {
        return file("ReceiptingProcessManagerBase") + file("ReceiptingProcessManager");
    }

    private static String file(String className) throws Exception {
        File f = new File(project, "src/main/java/io/acme/billing/application/" + className + ".java");
        assertTrue(f.exists(), "not generated: " + f);
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) n++;
        return n;
    }

    @Test
    public void theEventShopRaisesGetsOneHandler() throws Exception {
        String pm = processManager();
        assertTrue(pm.contains("import io.acme.shop.domain.event.OrderPlacedEvent;"), pm);
        assertEquals(1, count(pm, "public void on("), pm);
        assertTrue(pm.contains("it reacts to shop:OrderPlaced. Each event"), "only what got a handler: " + pm);
        assertEquals(1, count(pm, "public void on(OrderPlacedEvent event) {"), pm);
        assertEquals(1, count(pm, "protected abstract void handle(OrderPlacedEvent event);"), pm);
        assertEquals(1, count(pm, "protected void handle(OrderPlacedEvent event) {"), pm);
        assertTrue(new File(project, "src/main/java/io/acme/shop/domain/event/OrderPlacedEvent.java").exists(),
                "the class the handler imports is generated");
        assertTrue(log.contains("[billing] processManager Receipting: shop:OrderPlacedEvent is named twice in 'on';"
                + " it gets one handler."), log);
    }

    @Test
    public void whatCannotBeHadIsSaidAndLeftOut() throws Exception {
        String pm = processManager();
        assertFalse(pm.contains("OrderLineAdded"), pm);
        assertFalse(pm.contains("BookPublished"), pm);
        assertTrue(log.contains("[billing] processManager Receipting: shop:OrderLineAdded - OrderLine is a member of Order;"
                + " only a root raises events."), "a member's @event raises nothing: " + log);
        assertFalse(pm.contains("ArticlePublished"), pm);
        assertTrue(log.contains("[billing] processManager Receipting: press:ArticlePublished - press declares it, but its"
                + " rules generate no event classes"), "a suppressed ddd-event writes no class: " + log);
        assertTrue(log.contains("[billing] processManager Receipting: catalog:BookPublished - catalog declares it, but"
                + " its rules generate no event classes"), log);
        assertFalse(new File(project, "src/main/java/io/acme/catalog/domain/event/BookPublishedEvent.java").exists());
        assertFalse(new File(project, "src/main/java/io/acme/catalog/domain/model/BookPublishedEvent.java").exists());
        assertTrue(log.contains("[billing] processManager Receipting: nowhere:Placed - there is no context nowhere"), log);
        assertTrue(log.contains("[billing] processManager Receipting: OrderPlaced is an event of shop - write shop:OrderPlaced"), log);
    }

    @Test
    public void aContextWithNoEntityIsTold() {
        assertTrue(log.contains("[notify] declares domain services or process managers but no entity"), log);
    }
}
