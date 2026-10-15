package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A project rule reaches every kind of file the DSL describes, not only the
 * entity: the search DTO, the repository's queries, a transaction port and
 * its operations, an enum. The rule guide's chapter on the other YAML files
 * is written against these.
 */
public class EveryFileKindRuleTest {

    @AfterEach
    public void clear() {
        ModelCheck.reset();
    }

    private RuleEngine engine(SourceCode.TemplateSet set) {
        return RuleCatalog.standard().engineFor("hexagonal-springboot")
                .register(new com.acme.probe.EveryFileRules()).templateSet(set);
    }

    private EntitySpec order() {
        return new EntitySpec("SalesOrder")
                .domain("erp", "Erp")
                .meta("root", "")
                .pkg("entity", "com.acme.erp.domain.model")
                .pkg("repository", "com.acme.erp.infrastructure.persistence")
                .pkg("dto", "com.acme.erp.application.dto")
                .field("orderId", "string(36) @id")
                .field("orderNo", "string(20)! @search(like)")
                .query("summarize", "List<Object[]>", "SELECT o.orderNo, COUNT(o) FROM SalesOrder o GROUP BY o.orderNo")
                .query("closeAll", "int", "UPDATE SalesOrder o SET o.closed = true");
    }

    @Test
    public void aProjectMergeReachesTheWrittenSchemaLine() {
        EntitySpec credit = new EntitySpec("CreditResultDTO").domain("erp", "Erp")
                .pkg("dto", "com.acme.erp.application.dto")
                .field("creditLimit", "bigdecimal(14.2) @internal")
                .field("grade", "string(2)");
        for (SourceCode.TemplateSet set : new SourceCode.TemplateSet[]{SourceCode.TemplateSet.V3, SourceCode.TemplateSet.COMPATIBLE}) {
            String out = engine(set).run(credit, "dto").render();
            assertTrue(out.contains("@Schema(description = \"creditLimit\", hidden = true)"), out);
            assertFalse(out.contains("@Schema(description = \"grade\", hidden"), out);
        }
    }

    @Test
    public void aSearchDtoFieldIsReached() {
        String out = engine(SourceCode.TemplateSet.V3).run(order(), "srchdto").render();
        assertTrue(out.contains("@Size(min = 2)"), out);
    }

    @Test
    public void aReadingQueryGetsItsAnnotationAndAWritingOneDoesNot() {
        String out = engine(SourceCode.TemplateSet.V3).run(order(), "repository").render();
        String[] lines = out.split("\r?\n");
        int summarize = indexOf(lines, "summarize(");
        int closeAll = indexOf(lines, "closeAll(");
        assertTrue(summarize > 0 && lines[summarize - 1].contains("@Transactional(readOnly = true)"), out);
        assertFalse(lines[closeAll - 1].contains("@Transactional"), out);
        assertTrue(out.contains("import org.springframework.transaction.annotation.Transactional;"), out);
        assertTrue(ModelCheck.failures().isEmpty());
    }

    @Test
    public void theCompatibleRepositoryIsReachedToo() {
        String out = engine(SourceCode.TemplateSet.COMPATIBLE).run(order(), "repository").render();
        assertTrue(out.contains("@Transactional(readOnly = true)"), out);
        assertTrue(out.contains("import org.springframework.transaction.annotation.Transactional;"), out);
    }

    @Test
    public void aQueryThatSelectsEverythingFailsTheModelCheck() {
        EntitySpec order = order().nativeQuery("everything", "List<Object[]>", "select *  from sales_order");
        engine(SourceCode.TemplateSet.V3).run(order, "repository");
        assertEquals(1, ModelCheck.failures().size());
        assertTrue(ModelCheck.failures().get(0).contains("SalesOrder.everything"));
    }

    @Test
    public void aPortAndItsOperationsAreReached() {
        PortSpec port = new PortSpec("CreditCheckPort", "erp").protocol("http").url("https://credit.example.com/api")
                .pkg("port", "com.acme.erp.application.port.out")
                .pkg("dto", "com.acme.erp.application.dto")
                .pkg("entity", "com.acme.erp.domain.model");
        port.operation("checkCredit", "CreditResultDTO").param("String", "bizNo")
                .endpoint("/partners/{bizNo}/credit", "GET", null);
        String out = engine(SourceCode.TemplateSet.V3).run(port, "port").render();
        assertTrue(out.contains("@ExternalSystem(\"CreditCheckPort\")\npublic interface")
                || out.contains("@ExternalSystem(\"CreditCheckPort\")\r\npublic interface"), out);
        assertTrue(out.contains("@ExternalCall(\"GET /partners/{bizNo}/credit\")"), out);
        assertTrue(out.contains("import com.acme.erp.support.ExternalSystem;"), out);
        assertTrue(out.contains("import com.acme.erp.support.ExternalCall;"), out);
    }

    @Test
    public void anEnumIsReached() {
        EnumSpec status = new EnumSpec("OrderStatus").constant("DRAFT", "DRF")
                .pkg("enumeration", "com.acme.erp.domain.model");
        String out = engine(SourceCode.TemplateSet.V3).run(status, "enum").render();
        assertTrue(out.contains("@Schema(enumAsRef = true)"), out);
        assertTrue(out.contains("import io.swagger.v3.oas.annotations.media.Schema;"), out);
    }

    @Test
    public void aMethodTheFileDoesNotDeclareIsLeftAloneAndSaid() {
        EntitySpec order = order();
        RuleEngine engine = engine(SourceCode.TemplateSet.V3);
        SourceCode code = engine.run(order, "repository");
        code.method("noSuchMethod").ensure(AnnoCode.of("com.acme.arch.Marker"));
        String out = code.render();
        assertFalse(out.contains("@Marker"), out);
    }

    private static int indexOf(String[] lines, String text) {
        for (int i = 0; i < lines.length; i++) if (lines[i].contains(text)) return i;
        return -1;
    }
}
