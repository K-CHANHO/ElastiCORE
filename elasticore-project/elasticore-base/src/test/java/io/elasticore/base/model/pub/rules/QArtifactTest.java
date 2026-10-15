package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.QBaseRules;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Q class, compared against the one committed for jsonview_demo.
 *
 * It is the second artifact EntitySrcFilePublisher emits - one per domain,
 * naming every entity's fields for the Specification API.
 */
public class QArtifactTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec entity(String name, String[][] fields) {
        EntitySpec e = new EntitySpec(name);
        for (String[] f : fields) e.addField(loader.parse(f[0], f[1]));
        return e;
    }

    /** blueprint/jsonview_demo/model/entity.yml. */
    private DomainSpec jsonviewDemo() {
        return new DomainSpec("jsonview_demo", "Jsonview_demo")
                .pkg("entity", "io.elasticore.blueprint.domain.jsonview_demo.entity")
                .entity(entity("BlogPost", new String[][]{
                        {"id", "long @id @sequence"},
                        {"title", "string(200)!"},
                        {"content", "text"},
                        {"summary", "string(500)"},
                        {"status", "string(20) @default(\"DRAFT\")"},
                        {"viewCount", "int @default(0) @updatable(false)"},
                        {"author", "string(100)! @updatable(false)"},
                        {"publishedAt", "datetime"},
                        {"modifiedAt", "datetime"}}))
                .entity(entity("Member", new String[][]{
                        {"id", "long @id @sequence @view(r)"},
                        {"username", "string(50)! @view(cr)"},
                        {"password", "string(200) @view(c)"},
                        {"email", "string(100)! @view(cru)"},
                        {"nickname", "string(50) @view(cru)"},
                        {"role", "string(20) @default(\"USER\") @view(r)"},
                        {"loginCount", "int @default(0) @view(r)"},
                        {"lastLoginAt", "datetime @view(r)"},
                        {"bio", "string(500) @view(cu)"},
                        {"createdAt", "datetime @view(r)"}}));
    }

    private String render(DomainSpec d) {
        String out = new RuleEngine().register(new QBaseRules()).run(d, "Q").render();
        return out.endsWith("\n") ? out.substring(0, out.length() - 1) : out;
    }

    @Test
    public void qClass_matchesGolden() throws IOException {
        List<String> expected = goldenLines();
        assertEquals(join(expected), render(jsonviewDemo()));
    }

    // ---- the individual decisions -----------------------------------------

    /** A field offers its metadata, its own name, and how it is queried. */
    @Test
    public void baseField_offersMetadataNameAndQuery() {
        String out = render(jsonviewDemo());

        assertTrue(out.contains(
                "public final FieldInfo<T> getId() {return new FieldInfo(BlogPost.class,\"id\",Long.class, null,p);}"),
                out);
        assertTrue(out.contains("public final String id=\"id\";"), out);
        assertTrue(out.contains(
                "public Specification<T> id(Op op,Object value) {return getId().where(op,value);}"), out);
    }

    /** An entity with no table to query contributes nothing. */
    @Test
    public void abstractEntity_isLeftOut() {
        DomainSpec d = new DomainSpec("content", "Content")
                .pkg("entity", "p.entity")
                .entity(new EntitySpec("ContentBase").meta("abstract", null)
                        .addField(loader.parse("createdBy", "string(50)")))
                .entity(entity("Comment", new String[][]{{"commentId", "long @id"}}));

        String out = render(d);
        assertFalse(out.contains("ContentBase"), out);
        assertTrue(out.contains("public static class $Comment<T> {"), out);
    }

    /** A relation carries on into the entity it points at. */
    @Test
    public void relationField_continuesIntoTheOtherEntity() {
        DomainSpec d = new DomainSpec("content", "Content")
                .pkg("entity", "p.entity")
                .entity(entity("Comment", new String[][]{
                        {"commentId", "long @id"},
                        {"article", "Article @ManyToOne"}})
                        .knownEntity("Article"))
                .entity(entity("Article", new String[][]{{"articleId", "long @id"}}));

        assertTrue(render(d).contains(
                "public final $Article<T> article() {return new $Article(getArticle());}"), render(d));
    }

    /** A relation into another domain goes through that domain's Q. */
    @Test
    public void foreignRelation_goesThroughTheOtherDomainQ() {
        DomainSpec d = new DomainSpec("shop", "Shop")
                .pkg("entity", "p.entity")
                .foreignEntity("Employee", "kr.co.acme.hr.entity")
                .entity(entity("Order", new String[][]{
                        {"orderId", "long @id"},
                        {"owner", "Employee @ManyToOne"}})
                        .knownEntity("Employee"));

        assertTrue(render(d).contains(
                "public final kr.co.acme.hr.entity.Q.$Employee<T> owner()"
                        + " {return new kr.co.acme.hr.entity.Q.$Employee(getOwner());}"), render(d));
    }

    /** An enum is queried like a value, not followed like a relation. */
    @Test
    public void enumField_isQueriedLikeAValue() {
        DomainSpec d = new DomainSpec("shop", "Shop")
                .pkg("entity", "p.entity")
                .entity(entity("Customer", new String[][]{
                        {"customerId", "long @id"},
                        {"customerType", "CustomerType"}})
                        .knownEnum("CustomerType", "1", true));

        String out = render(d);
        assertTrue(out.contains("public Specification<T> customerType(Op op,Object value)"), out);
        assertFalse(out.contains("$CustomerType"), out);
    }

    /** A domain with nothing to name produces no file. */
    @Test
    public void domainWithoutEntities_producesNoFile() {
        DomainSpec d = new DomainSpec("empty", "Empty").pkg("entity", "p.entity")
                .entity(new EntitySpec("Base").meta("abstract", null));

        SourceCode code = new RuleEngine().register(new QBaseRules()).run(d, "Q");
        assertFalse(code.isMake());
    }

    // ---- golden access ----------------------------------------------------

    private List<String> goldenLines() throws IOException {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/jsonview_demo/entity/Q.java";
        String[] candidates = {"../" + rel, "elasticore-project/" + rel};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) {
                List<String> lines = readLines(f);
                return lines.subList(1, lines.size());   // skip the //ecd: line
            }
        }
        throw new IllegalStateException("Golden file not found: " + rel);
    }

    private static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
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
