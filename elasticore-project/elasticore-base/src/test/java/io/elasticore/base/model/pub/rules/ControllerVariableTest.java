package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.ControllerBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Controller variables that ControlSrcPublisher computes and the rules were
 * not: the service class it injects, the label and description of the API
 * tag, the search return type, and the primary-key path for a composite key.
 */
public class ControllerVariableTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec base(String name) {
        String pkg = "kr.co.acme.domain";
        return new EntitySpec(name)
                .domain("shop", "Shop")
                .apiPath("/api/v1")
                .pkg("dto", pkg + ".dto")
                .pkg("service", pkg + ".service")
                .pkg("control", pkg + ".controller");
    }

    private String render(EntitySpec e) {
        return new RuleEngine().register(new ControllerBaseRules())
                .run(e, "control").render();
    }

    // ---- injected service ---------------------------------------------

    /**
     * An @expose entity is served by the developer-owned extension class,
     * which is what the committed controllers inject.
     */
    @Test
    public void exposedEntity_injectsTheExtensionService() {
        EntitySpec e = base("Order").meta("expose", "30").pageable(true)
                .addField(loader.parse("orderId", "long @id"));

        String out = render(e);
        assertTrue(out.contains("private final OrderService orderService;"), out);
        assertFalse(out.contains("OrderCoreService"), out);
    }

    /** Without @expose the core service is injected directly. */
    @Test
    public void plainEntity_injectsTheCoreService() {
        EntitySpec e = base("Order")
                .addField(loader.parse("orderId", "long @id"));

        assertTrue(render(e).contains("private final OrderCoreService orderService;"), render(e));
    }

    // ---- api tag -------------------------------------------------------

    /** @label overrides the entity name in the API tag. */
    @Test
    public void label_overridesTheTagName() {
        EntitySpec e = base("Order").meta("label", "purchase order")
                .addField(loader.parse("orderId", "long @id"));

        assertTrue(render(e).contains("purchase order"), render(e));
    }

    // ---- search return type --------------------------------------------

    @Test
    public void pageableEntity_returnsPage() {
        EntitySpec e = base("Order").meta("expose", "30").pageable(true)
                .addField(loader.parse("orderId", "long @id"));

        assertTrue(render(e).contains("public Page<OrderDTO> search("), render(e));
    }

    /** A search DTO without a page size yields a plain list. */
    @Test
    public void nonPageableEntity_returnsList() {
        EntitySpec e = base("Order")
                .addField(loader.parse("orderId", "long @id"));

        assertTrue(render(e).contains("public List<OrderDTO> search("), render(e));
    }

    /** A dedicated search-result DTO replaces the plain one. */
    @Test
    public void searchResultDto_overridesThePlainDto() {
        EntitySpec e = base("Order").meta("expose", "30").pageable(true)
                .searchResultDtoName("OrderSummaryDTO")
                .addField(loader.parse("orderId", "long @id"));

        String out = render(e);
        assertTrue(out.contains("public Page<OrderSummaryDTO> search("), out);
        // the create/update endpoints still use the plain DTO
        assertTrue(out.contains("public OrderDTO create("), out);
    }

    // ---- primary key ----------------------------------------------------

    @Test
    public void singleKey_producesOnePathVariable() {
        EntitySpec e = base("Order").meta("expose", "30").pageable(true)
                .addField(loader.parse("orderId", "long @id"));

        String out = render(e);
        assertTrue(out.contains("@GetMapping(\"/{orderId}\")"), out);
        assertTrue(out.contains("findById(@PathVariable(\"orderId\") Long orderId)"), out);
        assertTrue(out.contains("orderService.findById(orderId)"), out);
    }

    /**
     * Every key field takes part, so a composite key addresses two path
     * segments and a two-argument signature. 2.x separates the parameter and
     * argument lists with " ," and that spacing is reproduced.
     */
    @Test
    public void compositeKey_producesEveryPathVariable() {
        EntitySpec e = base("ProjectAssignment").meta("expose", "30").pageable(true)
                .addField(loader.parse("empId", "string(36) @id"))
                .addField(loader.parse("projectCode", "string(20) @id"));

        String out = render(e);
        assertTrue(out.contains("@GetMapping(\"/{empId}/{projectCode}\")"), out);
        assertTrue(out.contains(
                "@PathVariable(\"empId\") String empId ,@PathVariable(\"projectCode\") String projectCode"), out);
        assertTrue(out.contains("findById(empId ,projectCode)"), out);
    }
}
