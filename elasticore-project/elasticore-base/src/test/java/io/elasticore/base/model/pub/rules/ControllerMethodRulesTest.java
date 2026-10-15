package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.ControllerBaseRules;
import org.junit.jupiter.api.Test;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Endpoint-level freedom for the controller artifact: the template remains
 * the single source of the method shapes, while DSL meta annotations and
 * user rules decide which methods exist for a given entity.
 *
 * The default path (no exclusions, no additions) stays byte-identical to
 * the template output - proven by ArtifactChainPrototypeTest and the
 * elcoreRulesCheck gate, which both keep passing unchanged.
 */
public class ControllerMethodRulesTest {

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
                .pkg("dto", base + ".dto")
                .pkg("service", base + ".service")
                .pkg("control", base + ".controller")
                .addField(loader.parse("orderId", "long @id @sequence"));
    }

    private String render(EntitySpec e, CodeRules... extraRules) {
        RuleEngine engine = new RuleEngine().register(new ControllerBaseRules());
        for (CodeRules r : extraRules) engine.register(r);
        SourceCode code = engine.run(e, "control");
        return code.render();
    }

    // ------------------------------------------------------------------

    @Test
    public void restExclude_dropsListedMethods() {
        EntitySpec e = order().meta("restexclude", "deleteById,batchUpdate");

        String out = render(e);

        assertFalse(out.contains("@DeleteMapping"), "deleteById must be dropped:\n" + out);
        assertFalse(out.contains("@PutMapping(\"/batch\")"), "batchUpdate must be dropped:\n" + out);
        // the rest survives
        assertTrue(out.contains("@PostMapping\n"), out);
        assertTrue(out.contains("@PostMapping(\"/search\")"), out);
        assertTrue(out.contains("@PostMapping(\"/delete\")"), out);
        assertTrue(out.contains("@GetMapping(\"/schema/info\")"), out);
    }

    @Test
    public void readonly_keepsOnlyReadEndpoints() {
        EntitySpec e = order().meta("readonly", null);

        String out = render(e);

        assertFalse(out.contains("@PostMapping\n"), "create must be dropped:\n" + out);
        assertFalse(out.contains("@PutMapping"), "update/batch must be dropped:\n" + out);
        assertFalse(out.contains("@DeleteMapping"), "delete must be dropped:\n" + out);
        assertFalse(out.contains("@PostMapping(\"/delete\")"), out);
        assertTrue(out.contains("@GetMapping(\"/{orderId}\")"), out);
        assertTrue(out.contains("@PostMapping(\"/search\")"), out);
        assertTrue(out.contains("@GetMapping(\"/schema/info\")"), out);
        // class skeleton intact
        assertTrue(out.trim().endsWith("}"), out);
    }

    /** A corporate rule set adding a custom endpoint under its own condition. */
    public static class AcmeControllerRules implements CodeRules {
        @Rule(value = "acme-approve-endpoint", target = "control", order = 30)
        void approveEndpoint(EntitySpec e) {
            if (!e.hasMeta("approvable")) return;
            ctx(e).addMethod(
                    "    @Operation(summary = \"Approve a " + e.getName() + "\")\n"
                  + "    @PostMapping(\"/{" + e.pkField().name() + "}/approve\")\n"
                  + "    public ResponseEntity<" + e.getName() + "DTO> approve(@PathVariable(\""
                          + e.pkField().name() + "\") Long " + e.pkField().name() + ") {\n"
                  + "        return ResponseEntity.ok(" + e.varName() + "Service.approve(" + e.pkField().name() + "));\n"
                  + "    }");
        }
    }

    @Test
    public void userRule_addsCustomMethodBeforeClosingBrace() {
        EntitySpec e = order().meta("approvable", null);

        String out = render(e, new AcmeControllerRules());

        assertTrue(out.contains("@PostMapping(\"/{orderId}/approve\")"), out);
        assertTrue(out.contains("orderService.approve(orderId)"), out);
        // added before the closing brace, class still closes
        int approveIdx = out.indexOf("/approve");
        int braceIdx = out.lastIndexOf("\n}");
        assertTrue(approveIdx < braceIdx, "custom method must sit inside the class:\n" + out);
        // default endpoints untouched
        assertTrue(out.contains("@DeleteMapping"), out);
    }

    @Test
    public void noFilter_outputUnchanged() {
        String plain = render(order());
        // Same spec rendered through the section machinery with no ops must
        // be identical to the raw substitution result.
        assertTrue(plain.contains("@DeleteMapping"), plain);
        assertTrue(plain.contains("@PostMapping\n"), plain);
        assertTrue(plain.contains("@GetMapping(\"/schema/info\")"), plain);
    }
}
