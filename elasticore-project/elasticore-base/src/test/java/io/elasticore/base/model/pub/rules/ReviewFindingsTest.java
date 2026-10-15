package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.RepositoryBaseRules;
import io.elasticore.base.model.pub.rules.lang.kotlin.KotlinKeywords;
import io.elasticore.base.model.repo.Method;
import io.elasticore.base.model.repo.SqlQueryInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What an independent review of the DDD work found, held shut. */
public class ReviewFindingsTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private Method method(String name, String query, boolean isNative, String returnType) {
        Items<Field> items = Items.create(Field.class);
        SqlQueryInfo info = SqlQueryInfo.creat("shop", query, isNative, true, null, returnType, true);
        return Method.builder().name(name).query(query).isNative(isNative).pageable(true)
                .returnType(returnType).params(items).queryInfo(info).build();
    }

    private EntitySpec order() {
        return new EntitySpec("Order").domain("shop", "Shop").knownDto("OrderTotalDTO", "");
    }

    @Test
    public void aPageOfAProjectionIsOnlyWhatSpringDataCanCount() {
        EntitySpec e = order();
        assertEquals("Page<OrderTotalDTO>", RepositoryBaseRules.pageableOrList(e,
                method("totals", "SELECT o.orderId AS orderId FROM Order o", false, "Page<OrderTotalDTO>"),
                "Page<OrderTotalDTO>"));
        assertEquals("List<OrderTotalDTO>", RepositoryBaseRules.pageableOrList(e,
                method("byCustomer", "SELECT o.customer.id AS id, COUNT(o) AS n FROM Order o GROUP BY o.customer.id",
                        false, "Page<OrderTotalDTO>"), "Page<OrderTotalDTO>"),
                "a grouped query's derived count is wrong");
        assertEquals("List<OrderTotalDTO>", RepositoryBaseRules.pageableOrList(e,
                method("nativeTotals", "SELECT order_id AS orderId FROM shop_order", true, "Page<OrderTotalDTO>"),
                "Page<OrderTotalDTO>"), "a native one gets no Pageable and would not start");
        assertEquals("Page<Order>", RepositoryBaseRules.pageableOrList(e,
                method("byStatus", "SELECT o FROM Order o GROUP BY o", false, "Page<Order>"), "Page<Order>"),
                "an entity page is left as it is");
    }

    @Test
    public void quotesInsideLiteralsDoNotConfuseTheKeywordQuoting() {
        assertEquals("val q = '\"'; val t: a.port.`in`.X", KotlinKeywords.escapeQualifiedNames("val q = '\"'; val t: a.port.in.X"));
        assertEquals("val s = \"say \\\"a.in.b\\\"\"; x.`in`.y", KotlinKeywords.escapeQualifiedNames("val s = \"say \\\"a.in.b\\\"\"; x.in.y"));
    }

    @Test
    public void aJavadocMentioningAClassIsNotTheClass() {
        String kt = "package a\n\n/**\n * The class that handles it {\n */\n@RestController\nclass C(\n    private val s: S\n) {\n\n"
                + "    @Operation(summary = \"Create\")\n    @PostMapping\n    fun create() = 1\n\n"
                + "    @Operation(summary = \"Read\")\n    @GetMapping(\"/{id}\")\n    fun read() = 2\n}\n";
        java.util.Set<String> excluded = new java.util.HashSet<String>();
        excluded.add("create");
        String out = MethodSections.apply(kt, excluded, java.util.Collections.<String>emptyList());
        assertTrue(!out.contains("fun create") && out.contains("fun read") && out.contains("The class that handles it"), out);
    }
}
