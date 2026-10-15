package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.JpaEntityBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What @genid fills an empty key with, as the annotation guide states it:
 * the declared length decides. A guide once said "string(36) @id @genid"
 * makes a UUID; it makes a 36-character ElastiCORE id instead.
 */
public class GenIdTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String fields(String idLine) {
        EntitySpec e = new EntitySpec("Card")
                .addField(loader.parse("cardId", idLine))
                .addField(loader.parse("memo", "string(200)"));
        return new RuleEngine().register(new JpaEntityBaseRules()).run(e).renderFieldBlock();
    }

    @Test
    public void withoutALengthTheKeyIsAUuid() {
        String out = fields("string @id @genid");
        assertTrue(out.contains("cardId = java.util.UUID.randomUUID().toString();"), out);
        assertFalse(out.contains("IdUtils"), out);
    }

    @Test
    public void withALengthTheKeyIsAnElasticoreIdOfThatLength() {
        String out = fields("string(36) @id @genid");
        assertTrue(out.contains("cardId = io.elasticore.springboot3.util.IdUtils.newId(36);"), out);
        assertFalse(out.contains("randomUUID"), out);
    }

    @Test
    public void aNamedGeneratorIsCalled() {
        String out = fields("string(20) @id @genid(com.acme.Ids.next)");
        assertTrue(out.contains("cardId = com.acme.Ids.next();"), out);
    }
}
