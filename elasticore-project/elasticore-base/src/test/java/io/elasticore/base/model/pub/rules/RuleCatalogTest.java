package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the named rule-set catalog: users select sets/bundles by name
 * (the future env.yml surface) and layer custom sets on top.
 */
public class RuleCatalogTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec order() {
        return new EntitySpec("Order")
                .addField(loader.parse("orderId", "long @id @sequence"))
                .addField(loader.parse("orderNo", "string(30)"));
    }

    /** Example of a corporate rule set selectable by its class name. */
    public static class AcmeAuditRules implements CodeRules {
        @Rule(value = "acme-audit-marker", order = 200)
        void marker(EntitySpec e, FieldSpec f) {
            if (f.hasDsl("id")) {
                ctx(e).field(f).ensure(AnnoCode.raw("kr.co.acme.Audited", "@AcmeAudited"));
            }
        }
    }

    @Test
    public void bundleSelection_runsAllMemberSets() {
        SourceCode code = RuleCatalog.standard()
                .engineFor("jpa-springboot")
                .run(order());

        String out = code.renderFieldBlock();
        assertTrue(out.contains("@Id"), out);
        assertTrue(out.contains("@Column(name = \"order_no\", length = 30)"), out);
    }

    @Test
    public void userClassName_resolvesAsRuleSet() {
        SourceCode code = RuleCatalog.standard()
                .engineFor("jpa-entity", AcmeAuditRules.class.getName())
                .run(order());

        String out = code.renderFieldBlock();
        assertTrue(out.contains("@AcmeAudited"), out);
        assertTrue(out.contains("@Id"), out);
    }

    @Test
    public void unknownName_failsWithCatalogListing() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    public void execute() {
                        RuleCatalog.standard().engineFor("no-such-set");
                    }
                });
        assertTrue(ex.getMessage().contains("jpa-springboot"), ex.getMessage());
    }
}
