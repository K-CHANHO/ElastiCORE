package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.MapperSpecRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JPA Specification generation, the largest single gap in the mapper layer:
 * makeSpecificationFieldCode is 240 lines of 2.x that had no counterpart.
 *
 * The primary case compares against the toSpec pair committed in
 * ContentMapper.java. The search DTO fed to the rules mirrors what the
 * search-DTO publisher derives from the entity - string fields get the
 * automatic form, date-times get between, everything else eq - because
 * that derivation happens upstream of emission.
 */
public class MapperSpecTest {

    private static final String GOLDEN =
            "elasticore-template/src/main/java/io/elasticore/blueprint/domain/content/dto/ContentMapper.java";

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String render(EntitySpec e) {
        return new RuleEngine().register(new MapperSpecRules())
                .register(new io.elasticore.base.model.pub.rules.base.MapperSpecShellRules())
                .run(e, "mapper").renderBlock("spec");
    }

    /** CommentSrchDTO as the search-DTO publisher derives it. */
    private EntitySpec commentSearchDto() {
        return new EntitySpec("Comment")
                .addField(loader.parse("articleArticleId", "string(36) @search(eq) @ref(article.articleId)"))
                .addField(loader.parse("commentId", "long @search(eq)"))
                .addField(loader.parse("content", "text @search"))
                .addField(loader.parse("authorName", "string(100) @search(like)"))
                .addField(loader.parse("authorEmail", "string(100) @search(eq)"))
                .addField(loader.parse("approved", "boolean @search(eq)"))
                .addField(loader.parse("createdBy", "string(50) @search"))
                .addField(loader.parse("createDate", "datetime @search(between)"))
                .addField(loader.parse("lastModifiedBy", "string(50) @search"))
                .addField(loader.parse("lastModifiedDate", "datetime @search(between)"));
    }

    // ------------------------------------------------------------------

    @Test
    public void toSpec_matchesCommittedContentMapper() throws IOException {
        String actual = render(commentSearchDto());
        String expected = extractToSpec();

        assertEquals(expected, actual);
    }

    /** From the toSpec(searchDTO) declaration to the close of the second overload. */
    private String extractToSpec() throws IOException {
        List<String> lines = readLines(golden());
        String start = "    public static Specification<Comment> toSpec(CommentSrchDTO searchDTO){";
        int s = lines.indexOf(start);
        assertTrue(s >= 0, "toSpec not found in ContentMapper.java");

        int closers = 0;
        int end = -1;
        for (int i = s; i < lines.size(); i++) {
            if (lines.get(i).equals("    }")) {
                closers++;
                if (closers == 2) { end = i; break; }
            }
        }
        assertTrue(end > s, "toSpec end not found");

        StringBuilder sb = new StringBuilder();
        for (int i = s; i <= end; i++) {
            sb.append(lines.get(i)).append('\n');
        }
        return sb.toString();
    }

    // ---- condition coverage beyond what the golden exercises ----------

    private String oneField(String dsl) {
        EntitySpec e = new EntitySpec("Order").addField(loader.parse("f", dsl));
        return render(e);
    }

    @Test
    public void comparisonConditions() {
        assertTrue(oneField("long @search(>=)").contains(
                "sp = sp.and((r,q,c) -> c.greaterThanOrEqualTo(r.get(\"f\"),f));"), oneField("long @search(>=)"));
        assertTrue(oneField("long @search(<=)").contains(
                "sp = sp.and((r,q,c) -> c.lessThanOrEqualTo(r.get(\"f\"),f));"), oneField("long @search(<=)"));
        assertTrue(oneField("long @search(>)").contains(
                "sp = sp.and((r,q,c) -> c.greaterThan(r.get(\"f\"),f));"), oneField("long @search(>)"));
        assertTrue(oneField("long @search(<)").contains(
                "sp = sp.and((r,q,c) -> c.lessThan(r.get(\"f\"),f));"), oneField("long @search(<)"));
    }

    @Test
    public void likeVariants() {
        assertTrue(oneField("string(30) @search(like)").contains(
                "c.like(r.get(\"f\"),\"%\" +trim(f)+ \"%\")"), oneField("string(30) @search(like)"));

        String ignoreCase = oneField("string(30) @search(%__%)");
        assertTrue(ignoreCase.contains(
                "c.like(c.lower(r.get(\"f\")),\"%\" +f.toLowerCase()+ \"%\")"), ignoreCase);

        String eqIgnoreCase = oneField("string(30) @search(__=)");
        assertTrue(eqIgnoreCase.contains(
                "c.equal(c.lower(r.get(\"f\")),f.toLowerCase())"), eqIgnoreCase);

        String suffix = oneField("string(30) @search(%_)");
        assertTrue(suffix.contains("c.like(r.get(\"f\"),\"%\" +trim(f))"), suffix);
    }

    @Test
    public void inCondition_onPlainField() {
        String out = oneField("string(30) @search(in)");
        assertTrue(out.contains("sp = sp.and((r, q, c) -> r.get(\"f\").in(f));"), out);
    }

    /** A collection field with in() joins the association. */
    @Test
    public void inCondition_onCollectionJoinsAssociation() {
        EntitySpec e = new EntitySpec("Article")
                .addField(loader.parse("tags", "List<Tag> @search(in)"));

        String out = render(e);
        assertTrue(out.contains("Join<Article, Tag> join = root.join(\"tags\");"), out);
        assertTrue(out.contains("return join.in(tags);"), out);
    }

    /** A collection field with eq joins on a single item instead. */
    @Test
    public void eqCondition_onCollectionJoinsSingleItem() {
        EntitySpec e = new EntitySpec("Article")
                .addField(loader.parse("tags", "List<Tag> @search(eq)"));

        String out = render(e);
        assertTrue(out.contains("Tag tagsItem = searchDTO.getTagsItem();"), out);
        assertTrue(out.contains("Join<Article, Tag> join = root.join(\"tags\");"), out);
        assertTrue(out.contains("return criteriaBuilder.equal(join, tagsItem);"), out);
    }

    /** LocalDate between shifts the upper bound by a day and uses lessThan. */
    @Test
    public void betweenOnLocalDate_shiftsUpperBound() {
        EntitySpec e = new EntitySpec("Order")
                .addField(loader.parse("orderDate", "date @search(between)"));

        String out = render(e);
        assertTrue(out.contains("java.time.LocalDate orderDateTo_2 = orderDateTo.plusDays(1);"), out);
        assertTrue(out.contains("c.between(r.get(\"orderDate\"),orderDateFrom,orderDateTo_2)"), out);
        assertTrue(out.contains("c.lessThan(r.get(\"orderDate\"),orderDateTo_2)"), out);
    }

    /** A custom entity field name may follow the condition, comma separated. */
    @Test
    public void conditionWithCustomFieldName() {
        String out = oneField("string(30) @search(eq,otherField)");
        assertTrue(out.contains("String otherField = searchDTO.getOtherField();"), out);
        assertTrue(out.contains("c.equal(r.get(\"otherField\"),otherField)"), out);
    }

    /** Fields without a search annotation take no part. */
    @Test
    public void fieldWithoutSearchAnnotation_isSkipped() {
        EntitySpec e = new EntitySpec("Order")
                .addField(loader.parse("hidden", "string(30)"))
                .addField(loader.parse("shown", "string(30) @search(eq)"));

        String out = render(e);
        assertTrue(out.contains("shown"), out);
        assertTrue(!out.contains("hidden"), out);
    }

    // ---- helpers -------------------------------------------------------

    private File golden() {
        String[] candidates = {"../" + GOLDEN, "elasticore-project/" + GOLDEN};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) return f;
        }
        throw new IllegalStateException("ContentMapper.java not found from "
                + new File(".").getAbsolutePath());
    }

    private List<String> readLines(File f) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
        } finally {
            r.close();
        }
        return lines;
    }
}
