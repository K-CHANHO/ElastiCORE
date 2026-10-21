package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A DDD root is sealed by default: a request gives its fields their values
 * when it is made, and after that they change through its own methods. Only
 * what the DSL opens with @editable keeps a public setter.
 */
public class SealedRootTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec order() {
        return new EntitySpec("SalesOrder")
                .domain("erp", "Erp")
                .meta("root", "")
                .meta("expose", "")
                .pkg("entity", "com.acme.erp.domain.model")
                .pkg("dto", "com.acme.erp.application.dto")
                .entitySplitForBehavior("SalesOrder")
                .addField(loader.parse("orderId", "string(36) @id"))
                .addField(loader.parse("orderNo", "string(20)!"))
                .addField(loader.parse("totalAmount", "bigdecimal(14.2) @default(0)"))
                .addField(loader.parse("status", "string(3) @behavior"))
                .addField(loader.parse("memo", "string(500) @editable"))
                .addField(loader.parse("version", "long @jpa:Version"));
    }

    private String render(EntitySpec e, String target) {
        return RuleCatalog.standard().engineFor("ddd-springboot")
                .templateSet(SourceCode.TemplateSet.V3).run(e, target).render();
    }

    /** The field's own declaration, from its annotations to the semicolon. */
    private String declarationOf(String source, String field) {
        int at = source.indexOf(" " + field + ";");
        if (at < 0) at = source.indexOf(" " + field + " =");
        int from = source.lastIndexOf(";", at);
        int prevBrace = source.lastIndexOf("}", at);
        return source.substring(Math.max(from, prevBrace) + 1, at);
    }

    @Test
    public void aSealedFieldHasNoPublicSetterAndAnEditableOneKeepsIt() {
        String out = render(order(), "entity");
        assertTrue(declarationOf(out, "orderNo").contains("@Setter(AccessLevel.PROTECTED)"), out);
        assertTrue(declarationOf(out, "totalAmount").contains("@Setter(AccessLevel.PROTECTED)"), out);
        assertFalse(declarationOf(out, "memo").contains("AccessLevel.PROTECTED"), out);
        // What the persistence layer writes stays as it was.
        assertFalse(declarationOf(out, "version").contains("AccessLevel.PROTECTED"), out);
    }

    @Test
    public void theCreationWriterTakesTheSealedFieldsOnly() {
        String out = render(order(), "entity");
        assertTrue(out.contains("public final class Creation {"), out);
        assertTrue(out.contains("public void setOrderNo(String value) {"), out);
        assertTrue(out.contains("FixedAtCreation.check(settled, \"SalesOrder\", \"orderNo\", orderNo, value);"), out);
        assertTrue(out.contains("if (!settled && value == null) return;"), out);
        assertTrue(out.contains("@jakarta.persistence.PostLoad"), out);
        assertFalse(out.contains("public void setMemo(String value) {"), out);
        assertFalse(out.contains("public void setStatus("), out);
        assertFalse(out.contains("public void setOrderId("), out);
    }

    @Test
    public void theDtoDoesNotCarryTheDefaultOfAFieldTheRootKeeps() {
        String dto = render(order(), "dto");
        assertFalse(dto.contains("totalAmount = "), dto);
    }

    /** The mapper's reverse block, composed as RulesGenRunner composes it. */
    private String reverse(EntitySpec e) {
        return ServerSetFieldTest.mapper(e, "ddd-springboot", SourceCode.TemplateSet.V3).renderBlock("reverse");
    }

    /**
     * A sealed field is written through the creation writer; a @behavior
     * one is not written at all, and the DTO shows it read-only.
     */
    @Test
    public void theMapperWritesASealedFieldAtCreationAndABehaviorFieldNever() {
        String reverse = reverse(order());
        assertTrue(reverse.contains("setVal(from.getOrderNo(), to.creation()::setOrderNo, isSkipNull);"), reverse);
        assertFalse(reverse.contains("Status"), reverse);
        assertTrue(declarationOf(render(order(), "dto"), "status")
                .contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), render(order(), "dto"));
    }

    /**
     * A @readonly field of a split root is still sealed - server code sets it
     * through creation() while the root is new - but the mapper no longer
     * routes a request's value there.
     */
    @Test
    public void aReadonlyFieldStaysSealedButTheMapperLeavesIt() {
        EntitySpec e = order().addField(loader.parse("returnedDate", "date @readonly"));
        String entity = render(e, "entity");
        assertTrue(declarationOf(entity, "returnedDate").contains("@Setter(AccessLevel.PROTECTED)"), entity);
        assertTrue(entity.contains("public void setReturnedDate(java.time.LocalDate value) {"), entity);

        String reverse = reverse(e);
        assertFalse(reverse.contains("ReturnedDate"), reverse);
        assertTrue(reverse.contains("setVal(from.getOrderNo(), to.creation()::setOrderNo, isSkipNull);"), reverse);

        String dto = declarationOf(render(e, "dto"), "returnedDate");
        assertTrue(dto.contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), dto);
        assertTrue(dto.contains("accessMode = Schema.AccessMode.READ_ONLY"), dto);
    }

    @Test
    public void anEditableRootIsNotSealed() {
        EntitySpec plain = new EntitySpec("Item")
                .domain("erp", "Erp")
                .meta("root", "")
                .meta("editable", "")
                .pkg("entity", "com.acme.erp.domain.model")
                .addField(loader.parse("itemCode", "string(20) @id"))
                .addField(loader.parse("name", "string(100)!"));
        String out = render(plain, "entity");
        assertFalse(out.contains("AccessLevel.PROTECTED"), out);
        assertFalse(out.contains("class Creation"), out);
    }
}
