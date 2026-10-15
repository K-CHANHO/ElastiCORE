package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coverage of the v3 template directory: every target the v3 mode emits must
 * have its own skeleton under elasticore-template/v3, and rendering it must
 * leave no unresolved slot behind.
 *
 * The same rule sets serve both modes - the v3 skeletons deliberately use the
 * variable names the existing rules already provide - so adding a skeleton is
 * all that a target needs to join the v3 mode.
 */
public class V3TemplateSetTest {

    /** Targets the v3 generation mode emits today. */
    private static final String[] V3_TARGETS = {
            "entity", "service", "repository", "service_extend", "control", "proto"
    };

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec order() {
        String base = "io.elasticore.blueprint.domain.shop";
        return new EntitySpec("Order")
                .domain("shop", "Shop")
                .apiPath("/api/v1")
                .tablePrefix("shop_")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                .pkg("repository", base + ".repository")
                .pkg("service", base + ".service")
                .pkg("control", base + ".controller")
                .pkg("enumeration", base + ".enums")
                .addField(loader.parse("orderId", "long @id @sequence"))
                .addField(loader.parse("orderNo", "string(30)"));
    }

    private String renderV3(String target, String... ruleSets) {
        return RuleCatalog.standard()
                .engineFor(ruleSets)
                .templateSet(SourceCode.TemplateSet.V3)
                .run(order(), target)
                .render();
    }

    // ------------------------------------------------------------------

    @Test
    public void everyV3Target_hasItsOwnSkeleton() {
        for (String target : V3_TARGETS) {
            String path = String.format("/elasticore-template/v3/%s.tmpl", target);
            InputStream in = getClass().getResourceAsStream(path);
            assertNotNull(in, "missing v3 skeleton for target '" + target + "': " + path);
            try {
                in.close();
            } catch (Exception ignore) {
                // nothing to do
            }
        }
    }

    @Test
    public void v3Repository_rendersWithoutUnresolvedSlots() {
        String out = renderV3("repository", "jpa-repository");

        assertTrue(out.contains("public interface OrderRepository extends JpaRepository<Order, Long>"), out);
        assertTrue(out.contains("import io.elasticore.blueprint.domain.shop.entity.Order;"), out);
        assertNoUnresolvedSlot(out);
    }

    @Test
    public void v3ServiceExtend_rendersWithoutUnresolvedSlots() {
        String out = renderV3("service_extend", "spring-service-extend");

        assertTrue(out.contains("public class OrderService extends OrderCoreService {"), out);
        assertTrue(out.contains("public OrderService(ShopRepositoryHelper helper) {"), out);
        // stays developer-owned: the generate-once marker survives
        assertTrue(out.contains("no longer managed"), out);
        assertNoUnresolvedSlot(out);
    }

    @Test
    public void v3Control_rendersWithoutUnresolvedSlots() {
        String out = renderV3("control", "spring-controller");

        assertTrue(out.contains("public class OrderController {"), out);
        assertTrue(out.contains("@RequestMapping(\"/api/v1/shop/order\")"), out);
        assertTrue(out.contains("@GetMapping(\"/{orderId}\")"), out);
        assertTrue(out.contains("import io.elasticore.blueprint.domain.shop.dto.OrderDTO;"), out);
        // no wildcard DTO import in the clean mode
        assertFalse(out.contains(".dto.*;"), out);
        assertNoUnresolvedSlot(out);
    }

    /** Method selection keeps working on the v3 controller skeleton. */
    @Test
    public void v3Control_honoursReadonly() {
        EntitySpec e = order().meta("readonly", null);
        String out = RuleCatalog.standard()
                .engineFor("spring-controller")
                .templateSet(SourceCode.TemplateSet.V3)
                .run(e, "control")
                .render();

        assertFalse(out.contains("@DeleteMapping"), out);
        assertFalse(out.contains("@PutMapping"), out);
        assertTrue(out.contains("@PostMapping(\"/search\")"), out);
        assertTrue(out.trim().endsWith("}"), out);
    }

    private void assertNoUnresolvedSlot(String out) {
        assertFalse(out.contains("${"), "unresolved template slot:\n" + out);
    }
}
