package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rule elasticore-rule-guide.md opens with, compiled and run.
 *
 * A guide that shows an API is a claim about it, and a claim nobody checks
 * goes stale the first time a signature moves. This is the guide's example
 * verbatim, so the two cannot drift apart without a test failing.
 */
public class RuleGuideExampleTest {

    /** Section 3.1 of the guide, copied. */
    public static class AcmeRules implements CodeRules {

        @Rule(value = "acme-masked", target = "dto", order = 50)
        void masked(EntitySpec e, FieldSpec f) {
            if (!f.hasDsl("masked")) return;
            ctx(e).field(f).ensure(AnnoCode.of("kr.co.acme.security.Masked"));
        }
    }

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    @Test
    public void theGuidesFirstExample_emitsWhatItSaysItDoes() {
        EntitySpec e = new EntitySpec("Member")
                .domain("acme", "Acme")
                .pkg("dto", "kr.co.acme.dto")
                .addField(loader.parse("memberId", "long @id"))
                .addField(loader.parse("ssn", "string(13) @masked"));

        String out = RuleCatalog.standard()
                .engineFor("jpa-springboot-v3")
                .templateSet(SourceCode.TemplateSet.V3)
                .register(new AcmeRules())
                .run(e, "dto")
                .render();

        assertTrue(out.contains("@Masked"), out);
        assertTrue(out.contains("import kr.co.acme.security.Masked;"), out);
    }
}
