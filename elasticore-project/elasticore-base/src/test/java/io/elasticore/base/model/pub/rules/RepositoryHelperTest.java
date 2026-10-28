package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.RepositoryHelperBaseRules;
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
 * The per-domain repository helper, compared against the committed helpers of
 * the content and shop domains. Everything but the injected repositories is
 * template text, so the comparison is over the whole file.
 */
public class RepositoryHelperTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /** An entity that owns a table: it has a primary key. */
    private EntitySpec keyed(String name) {
        return new EntitySpec(name).addField(loader.parse("id", "long @id"));
    }

    private String render(DomainSpec d) {
        return new RuleEngine().register(new RepositoryHelperBaseRules())
                .run(d, "repositoryHelper").render();
    }

    /**
     * blueprint/content: ContentBase is abstract with no key of its own, so
     * the helper injects the six entities that do have one.
     */
    private DomainSpec contentDomain() {
        String base = "io.elasticore.blueprint.domain.content";
        return new DomainSpec("content", "Content")
                .pkg("repository", base + ".repository")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                .entity(new EntitySpec("ContentBase").meta("abstract", null))
                .entity(keyed("ContentCategory"))
                .entity(keyed("Tag"))
                .entity(keyed("Article"))
                .entity(keyed("Comment"))
                .entity(keyed("Attachment"))
                .entity(keyed("Bookmark"));
    }

    @Test
    public void contentHelper_matchesGolden() throws IOException {
        List<String> expected = readLines(golden("content", "ContentRepositoryHelper.java"));
        expected = expected.subList(1, expected.size());   // skip the //ecd: line

        assertEquals(join(expected), asFileText(render(contentDomain())));
    }

    /**
     * blueprint/shop: Address is @embeddable and AuditEntity /
     * AuditableWithEnum are abstract without a key, leaving six entities.
     */
    @Test
    public void shopHelper_matchesGolden() throws IOException {
        String base = "io.elasticore.blueprint.domain.shop";
        DomainSpec d = new DomainSpec("shop", "Shop")
                .pkg("repository", base + ".repository")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                // model/coverage.yml is read before model/entity.yml: name order.
                .entity(keyed("Ledger"))
                .entity(new EntitySpec("Address").meta("embeddable", null)
                        .addField(loader.parse("city", "string(50)")))
                .entity(keyed("Category"))
                .entity(keyed("Product"))
                .entity(keyed("Customer"))
                .entity(keyed("VipProfile"))
                .entity(keyed("Order"))
                .entity(keyed("OrderItem"))
                .entity(keyed("Coupon"))
                .entity(keyed("GiftCard"))
                .entity(new EntitySpec("AuditEntity").meta("abstract", null))
                .entity(new EntitySpec("AuditableWithEnum").meta("abstract", null));

        List<String> expected = readLines(golden("shop", "ShopRepositoryHelper.java"));
        expected = expected.subList(1, expected.size());

        assertEquals(join(expected), asFileText(render(d)));
    }

    // ---- the individual filters, so a failure names itself ----------------

    /** An entity with no key of its own has no repository to inject. */
    @Test
    public void keylessEntity_isNotInjected() {
        assertFalse(render(contentDomain()).contains("ContentBaseRepository"),
                render(contentDomain()));
    }

    /** An @embeddable type is part of another table, not one of its own. */
    @Test
    public void embeddableType_isNotInjected() {
        DomainSpec d = new DomainSpec("shop", "Shop")
                .pkg("repository", "p.repository").pkg("entity", "p.entity")
                .entity(new EntitySpec("Address").meta("embeddable", null)
                        .addField(loader.parse("city", "string(50)")))
                .entity(keyed("Order"));

        String out = render(d);
        assertFalse(out.contains("AddressRepository"), out);
        assertTrue(out.contains("private final OrderRepository order;"), out);
    }

    /** An entity of another domain is injected and imported by name. */
    @Test
    public void externalEntity_isInjectedAndImported() {
        DomainSpec d = new DomainSpec("shop", "Shop")
                .pkg("repository", "p.repository").pkg("entity", "p.entity")
                .entity(keyed("Order"))
                .externalEntity("Employee", "kr.co.acme.hr.repository");

        String out = render(d);
        assertTrue(out.contains("private final EmployeeRepository employee;"), out);
        assertTrue(out.contains("import kr.co.acme.hr.repository.EmployeeRepository;"), out);
    }

    /** With nothing to inject there is no helper worth writing. */
    @Test
    public void domainWithoutKeyedEntities_producesNoFile() {
        DomainSpec d = new DomainSpec("empty", "Empty")
                .pkg("repository", "p.repository").pkg("entity", "p.entity")
                .entity(new EntitySpec("Base").meta("abstract", null));

        SourceCode code = new RuleEngine().register(new RepositoryHelperBaseRules())
                .run(d, "repositoryHelper");
        assertFalse(code.isMake(), "helper should not be written");
    }

    /** A domain name that is not camel case keeps its shape in the class name. */
    @Test
    public void domainClassName_followsTheDomainName() {
        DomainSpec d = new DomainSpec("jsonview_demo", "Jsonview_demo")
                .pkg("repository", "p.repository").pkg("entity", "p.entity")
                .entity(keyed("BlogPost"));

        assertTrue(render(d).contains("public class Jsonview_demoRepositoryHelper {"), render(d));
    }

    // ---- golden access ----------------------------------------------------

    private File golden(String domain, String fileName) {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/domain/"
                + domain + "/repository/" + fileName;
        String[] candidates = {"../" + rel, "elasticore-project/" + rel};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) return f;
        }
        throw new IllegalStateException("Golden file not found: " + rel
                + " from cwd " + new File(".").getAbsolutePath());
    }

    /**
     * The rendered text as a reader would see it: the trailing newline that
     * ends the file is not a line of its own.
     */
    private static String asFileText(String rendered) {
        return rendered.endsWith("\n") ? rendered.substring(0, rendered.length() - 1) : rendered;
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
