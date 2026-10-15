package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * money(12.2) and quantity: the runtime's value types, written in the DSL
 * like any other type. The column stays a decimal or an integer; the code
 * in between holds a Money or a Quantity.
 */
public class ValueTypeFieldTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec line() {
        return new EntitySpec("SalesOrderLine")
                .domain("erp", "Erp")
                .pkg("entity", "com.acme.erp.domain.model")
                .pkg("dto", "com.acme.erp.application.dto")
                .addField(loader.parse("lineId", "string(36) @id"))
                .addField(loader.parse("qty", "quantity! @min(1)"))
                .addField(loader.parse("unitPrice", "money(12.2)! @min(0.01, '단가는 0보다 커야 한다')"))
                .addField(loader.parse("amount", "money @default(0)"));
    }

    @Test
    public void theTypeIsReadWithItsSize() {
        Field price = loader.parse("unitPrice", "money(12.2)!");
        assertTrue(price.getTypeInfo().isMoney());
        assertEquals(12, price.getTypeInfo().getPrecision());
        assertEquals(2, price.getTypeInfo().getScale());
        Field plain = loader.parse("amount", "money");
        assertEquals(19, plain.getTypeInfo().getPrecision());
        assertTrue(loader.parse("qty", "quantity").getTypeInfo().isQuantity());
    }

    @Test
    public void theEntityStoresItThroughItsConverter() {
        String out = RuleCatalog.standard().engineFor("jpa-springboot-v3")
                .templateSet(SourceCode.TemplateSet.V3).run(line(), "entity").render();
        assertTrue(out.contains("@Convert(converter = Money.JpaConverter.class)"), out);
        assertTrue(out.contains("@Convert(converter = Quantity.JpaConverter.class)"), out);
        assertTrue(out.contains("precision = 12, scale = 2"), out);
        assertTrue(out.contains("private Money unitPrice;"), out);
        assertTrue(out.contains("private Quantity qty;"), out);
        assertTrue(out.contains("io.elasticore.springboot3.domain.Money.of(\"0\")"), out);
        assertFalse(out.contains("length = 12"), out);
    }

    @Test
    public void theDtoBoundsAreTheDecimalOnes() {
        String dto = RuleCatalog.standard().engineFor("jpa-dto", "jpa-dto-file")
                .templateSet(SourceCode.TemplateSet.V3).run(line(), "dto").render();
        assertTrue(dto.contains("message=\"단가는 0보다 커야 한다\"") || dto.contains("message = \"단가는 0보다 커야 한다\""), dto);
        assertTrue(dto.contains("@DecimalMin"), dto);
        assertTrue(dto.contains("@Min(1)"), dto);
    }

    @Test
    public void aRootChecksTheValuesOnWrite() {
        EntitySpec root = line();
        String out = RuleCatalog.standard().engineFor("ddd-invariant")
                .templateSet(SourceCode.TemplateSet.V3).run(root, "invariants").render();
        assertTrue(out.contains("getUnitPrice().compareTo(io.elasticore.springboot3.domain.Money.of(\"0.01\")) < 0"), out);
        assertTrue(out.contains("getQty().value() < 1"), out);
    }
}
