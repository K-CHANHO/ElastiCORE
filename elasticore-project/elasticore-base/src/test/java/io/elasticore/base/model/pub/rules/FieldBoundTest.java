package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A rule about one field is written on that field - @min(0.01, '...') - and
 * holds everywhere: in the DTO (400) and, for a root, on every write (422).
 */
public class FieldBoundTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec item() {
        String base = "com.acme.erp";
        return new EntitySpec("Item")
                .domain("erp", "Erp")
                .pkg("entity", base + ".domain.model")
                .pkg("dto", base + ".application.dto")
                .addField(loader.parse("itemCode", "string(20) @id"))
                .addField(loader.parse("unitPrice", "bigdecimal(12.2)! @min(0.01, '단가는 0보다 커야 한다')"))
                .addField(loader.parse("stock", "int @min(0) @max(9999)"));
    }

    @Test
    public void theValueAndTheMessageAreRead() {
        assertArrayEquals(new String[]{"0.01", "단가는 0보다 커야 한다"},
                DtoBaseRules.Bound.split("0.01, '단가는 0보다 커야 한다'"));
        assertArrayEquals(new String[]{"0", null}, DtoBaseRules.Bound.split("0"));
        assertArrayEquals(new String[]{"1", "a, b"}, DtoBaseRules.Bound.split("1, 'a, b'"));
    }

    @Test
    public void theDtoCarriesTheMessageAndABareBoundStaysAs2xWroteIt() {
        String dto = RuleCatalog.standard().engineFor("jpa-dto", "jpa-dto-file")
                .templateSet(SourceCode.TemplateSet.V3).run(item(), "dto").render();
        assertTrue(dto.contains("@DecimalMin(value=\"0.01\", message=\"단가는 0보다 커야 한다\")")
                || dto.contains("@DecimalMin(value = \"0.01\", message = \"단가는 0보다 커야 한다\")"), dto);
        assertTrue(dto.contains("@Min(0)"), dto);
        assertTrue(dto.contains("@Max(9999)"), dto);
    }

    /** How many members a collection holds is a rule about that field too. */
    @Test
    public void aCollectionSaysHowManyItHolds() {
        EntitySpec order = new EntitySpec("SalesOrder")
                .domain("erp", "Erp")
                .pkg("entity", "com.acme.erp.domain.model")
                .pkg("dto", "com.acme.erp.application.dto")
                .addField(loader.parse("orderId", "string(36) @id"))
                .addField(loader.parse("lines",
                        "List<SalesOrderLine> @OneToMany(mappedBy=\"order\") @minsize(1, '수주에는 품목이 하나 이상 있어야 한다')"));
        String out = RuleCatalog.standard().engineFor("ddd-invariant")
                .templateSet(SourceCode.TemplateSet.V3).run(order, "invariants").render();
        assertTrue(out.contains("salesOrder.getLines() == null || salesOrder.getLines().size() <= 0"), out);
        assertTrue(out.contains("\"수주에는 품목이 하나 이상 있어야 한다\""), out);

        String dto = RuleCatalog.standard().engineFor("jpa-dto", "jpa-dto-file")
                .templateSet(SourceCode.TemplateSet.V3).run(order, "dto").render();
        assertTrue(dto.contains("@Size(min=1, message=\"수주에는 품목이 하나 이상 있어야 한다\")"), dto);
    }

    @Test
    public void aRootChecksItsFieldBoundsOnEveryWrite() {
        String out = RuleCatalog.standard().engineFor("ddd-invariant")
                .templateSet(SourceCode.TemplateSet.V3).run(item(), "invariants").render();
        assertTrue(out.contains("item.getUnitPrice().compareTo(new java.math.BigDecimal(\"0.01\")) < 0"), out);
        assertTrue(out.contains("\"단가는 0보다 커야 한다\""), out);
        assertTrue(out.contains("item.getStock() < 0"), out);
        assertTrue(out.contains("stock must be at most 9999"), out);
        assertFalse(out.contains("${"), out);
    }
}
