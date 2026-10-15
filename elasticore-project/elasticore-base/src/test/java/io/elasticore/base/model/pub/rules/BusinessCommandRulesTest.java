package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A command declared in the DSL becomes one way in, from the endpoint to the
 * aggregate's rule; the rule itself is declared abstract so it cannot be
 * forgotten.
 */
public class BusinessCommandRulesTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec order() {
        String base = "com.acme.erp";
        return new EntitySpec("SalesOrder")
                .domain("erp", "Erp")
                .meta("root", "")
                .meta("expose", "")
                .pkg("entity", base + ".domain.model")
                .pkg("dto", base + ".application.dto")
                .pkg("service", base + ".application")
                .pkg("control", base + ".adapter.in.web")
                .pkg("inboundPort", base + ".application.port.in")
                .entitySplitForBehavior("SalesOrder")
                .addField(loader.parse("orderId", "string(36) @id"))
                .addField(loader.parse("orderNo", "string(20)!"))
                .addCommand("confirm", Collections.<Field>emptyList())
                .addCommand("shipPartially", Arrays.asList(
                        loader.parse("warehouse", "string(20)!"),
                        loader.parse("amount", "bigdecimal(14.2)")));
    }

    private String render(EntitySpec e, String target) {
        return RuleCatalog.standard().engineFor("hexagonal-springboot")
                .templateSet(SourceCode.TemplateSet.V3).run(e, target).render();
    }

    /** A parameter of the domain's enum is written in full: nothing imports it there. */
    @Test
    public void anEnumParameterIsWrittenInFull() {
        EntitySpec e = order().typePackage("CancelReason", "com.acme.erp.domain.model.code")
                .addCommand("cancel", Arrays.asList(loader.parse("reason", "CancelReason!")));
        String out = render(e, "entity");
        assertTrue(out.contains("public abstract void cancel(com.acme.erp.domain.model.code.CancelReason reason);"), out);
    }

    @Test
    public void theBaseDeclaresEachCommandAbstract() {
        String out = render(order(), "entity");
        assertTrue(out.contains("public abstract void confirm();"), out);
        assertTrue(out.contains("public abstract void shipPartially(String warehouse, java.math.BigDecimal amount);"), out);
    }

    @Test
    public void theBehaviourClassStartsWithStubsThatThrow() {
        String out = render(order(), "behavior");
        assertTrue(out.contains("public void shipPartially(String warehouse, java.math.BigDecimal amount) {"), out);
        assertTrue(out.contains("throw new UnsupportedOperationException(\"SalesOrder.shipPartially: write the rule\");"), out);
        assertFalse(out.contains("${"), out);
    }

    // The service's load-call-save is rendered into the full service
    // skeleton, which needs a whole model; the DDD and ERP samples drive it.

    @Test
    public void thePortAndTheEndpointFollow() {
        String port = render(order(), "usecase");
        assertTrue(port.contains("SalesOrderDTO shipPartially(String orderId, SalesOrderShipPartiallyCommand command);"), port);
        assertTrue(port.contains("import com.acme.erp.application.dto.SalesOrderShipPartiallyCommand;"), port);

        String control = render(order(), "control");
        assertTrue(control.contains("@PostMapping(\"/{orderId}/ship-partially\")"), control);
        assertTrue(control.contains("@Valid @RequestBody com.acme.erp.application.dto.SalesOrderShipPartiallyCommand command"), control);
        assertTrue(control.contains("@PostMapping(\"/{orderId}/confirm\")"), control);
    }

    @Test
    public void aRootWithNothingEditableHasNoUpdate() {
        String control = render(order(), "control");
        assertFalse(control.contains("@PutMapping"), control);
        String port = render(order(), "usecase");
        assertFalse(port.contains(" update("), port);

        EntitySpec editable = order().addField(loader.parse("memo", "string(500) @editable"));
        assertTrue(render(editable, "control").contains("@PutMapping"));
    }
}
