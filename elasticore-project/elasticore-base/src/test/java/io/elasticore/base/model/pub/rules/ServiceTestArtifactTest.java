package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.ServiceTestBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The generated service test, compared against the committed
 * CommentServiceTest of the content domain.
 */
public class ServiceTestArtifactTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /** blueprint/content Comment, whose parent contributes the audit fields. */
    private EntitySpec comment() {
        String base = "io.elasticore.blueprint.domain.content";
        return new EntitySpec("Comment")
                .domain("content", "Content")
                .meta("expose", "30").pageable(true)
                .pkg("service", base + ".service")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                .pkg("repository", base + ".repository")
                .addField(loader.parse("commentId", "long @id @sequence"))
                .addField(loader.parse("content", "text!"))
                .addField(loader.parse("authorName", "string(100)!"))
                .addField(loader.parse("authorEmail", "string(100)"))
                .addField(loader.parse("approved", "boolean @default(false)"))
                .addParentField(loader.parse("createdBy", "string(50)"))
                .addParentField(loader.parse("createDate", "datetime"))
                .addParentField(loader.parse("lastModifiedBy", "string(50)"))
                .addParentField(loader.parse("lastModifiedDate", "datetime"));
    }

    /**
     * An entity with @access is tested as the application itself in the 3.0
     * output; the 2.x one, which every 2.x project's generator shares, is
     * left exactly as it was.
     */
    @Test
    public void aGuardedEntitysTestRunsAsTheApplication() {
        EntitySpec guarded = comment().meta("access", "read=public");
        String v3 = new RuleEngine().register(new ServiceTestBaseRules())
                .templateSet(SourceCode.TemplateSet.V3).run(guarded, "service_test").render();
        assertTrue(v3.contains("@ExtendWith({MockitoExtension.class, io.elasticore.springboot3.security.RunAsSystemExtension.class})"), v3);
        String plain = new RuleEngine().register(new ServiceTestBaseRules())
                .templateSet(SourceCode.TemplateSet.V3).run(comment(), "service_test").render();
        assertTrue(plain.contains("@ExtendWith(MockitoExtension.class)"), plain);
        assertTrue(render(guarded).contains("@ExtendWith(MockitoExtension.class)"), "2.x skeleton unchanged");
    }

    private String render(EntitySpec e) {
        return new RuleEngine().register(new ServiceTestBaseRules())
                .run(e, "service_test").render();
    }

    /**
     * The entity fixture, exactly as committed:
     *   entity.setCommentId(1L);
     *   entity.setContent("SampleContent");
     *   entity.setAuthorName("SampleAuthorName");
     *   entity.setAuthorEmail("test@example.com");
     *   entity.setApproved(true);
     */
    @Test
    public void entityFixture_matchesGolden() {
        String out = render(comment());

        assertTrue(out.contains("        entity.setCommentId(1L);"), out);
        assertTrue(out.contains("        entity.setContent(\"SampleContent\");"), out);
        assertTrue(out.contains("        entity.setAuthorName(\"SampleAuthorName\");"), out);
        assertTrue(out.contains("        entity.setAuthorEmail(\"test@example.com\");"), out);
        assertTrue(out.contains("        entity.setApproved(true);"), out);
    }

    /** The DTO fixture carries the same values. */
    @Test
    public void dtoFixture_matchesGolden() {
        String out = render(comment());

        assertTrue(out.contains("        dto.setCommentId(1L);"), out);
        assertTrue(out.contains("        dto.setAuthorEmail(\"test@example.com\");"), out);
    }

    /**
     * A temporal field is given a value of its own type.
     *
     * The committed tests carry nulls here: 2.x matched the whole type name,
     * which for a temporal is qualified, so every one of its temporal cases
     * fell through. Regenerating a service test changes those lines.
     */
    @Test
    public void temporalField_isGivenAValueOfItsType() {
        String out = render(comment());

        assertTrue(out.contains("        entity.setCreateDate(java.time.LocalDateTime.now());"), out);
        assertTrue(out.contains("        dto.setLastModifiedDate(java.time.LocalDateTime.now());"), out);
    }

    /** A date without a time part gets one of its own. */
    @Test
    public void dateField_isGivenADate() {
        EntitySpec e = comment().addField(loader.parse("publishOn", "date"));

        assertTrue(render(e).contains("        entity.setPublishOn(java.time.LocalDate.now());"),
                render(e));
    }

    /** A decimal, whose type name is qualified too, is reached as well. */
    @Test
    public void decimalField_isGivenADecimal() {
        EntitySpec e = comment().addField(loader.parse("price", "bigdecimal(12.2)"));

        assertTrue(render(e).contains("entity.setPrice(new java.math.BigDecimal(\"1.0\"));"),
                render(e));
    }

    /** A key the database generates is left for it to fill in. */
    @Test
    public void generatedKey_isNotGivenAValue() {
        EntitySpec e = new EntitySpec("Article")
                .domain("content", "Content")
                .pkg("service", "p.service").pkg("entity", "p.entity")
                .pkg("dto", "p.dto").pkg("repository", "p.repository")
                .addField(loader.parse("articleId", "string(36) @id @genid"))
                .addField(loader.parse("title", "string(200)"));

        String out = render(e);
        assertFalse(out.contains("entity.setArticleId("), out);
        assertTrue(out.contains("entity.setTitle(\"SampleTitle\");"), out);
    }

    /** A relation has no value the test can invent. */
    @Test
    public void relationField_isNotGivenAValue() {
        EntitySpec e = comment().addField(loader.parse("article", "Article @ManyToOne"));

        assertFalse(render(e).contains("entity.setArticle("), render(e));
    }

    // ---- the key ----------------------------------------------------------

    /**
     * A single key is read back off the fixture, exactly as committed:
     *   Long id = mockEntity.getCommentId();
     */
    @Test
    public void singleKey_isReadOffTheFixture() {
        String out = render(comment());

        assertTrue(out.contains("        Long id = mockEntity.getCommentId();"), out);
    }

    /** A composite key is built from a variable per field. */
    @Test
    public void compositeKey_isBuiltFromAVariablePerField() {
        EntitySpec e = new EntitySpec("ProjectAssignment")
                .domain("hr", "Hr")
                .pkg("service", "p.service").pkg("entity", "p.entity")
                .pkg("dto", "p.dto").pkg("repository", "p.repository")
                .addField(loader.parse("empId", "string(36) @id"))
                .addField(loader.parse("projectCode", "string(20) @id"));

        String out = render(e);
        // The declaration closes itself: the slot used to leave the last one
        // open for the template to terminate, and both terminating would read
        // as a stray empty statement.
        assertTrue(out.contains("String empId = \"SampleEmpId\";"), out);
        assertFalse(out.contains(";;"), out);
        assertTrue(out.contains("String projectCode = \"SampleProjectCode\""), out);
        assertTrue(out.contains("//when(mockDto.getEmpId()).thenReturn(empId);"), out);
    }

    // ---- names -------------------------------------------------------------

    /** The test refers to the service, its DTOs and the domain's mapper. */
    @Test
    public void names_followTheEntity() {
        String out = render(comment());

        assertTrue(out.contains("class CommentServiceTest"), out);
        assertTrue(out.contains("ContentRepositoryHelper"), out);
        assertTrue(out.contains("CommentDTO"), out);
    }

    /**
     * A dedicated search-result DTO is the one the search returns.
     *
     * Asserted on an entity that declares neither @expose nor @service: only
     * then does the test cover the search itself. An exposed entity is tested
     * through its extended service, and 2.x asserts nothing about the shape of
     * that search result - ServiceTestPublisher leaves both isListOutput and
     * isPageOutput false in that branch.
     */
    @Test
    public void searchResultDto_overridesThePlainOne() {
        String base = "io.elasticore.blueprint.domain.content";
        EntitySpec e = new EntitySpec("Comment")
                .domain("content", "Content")
                .pkg("service", base + ".service")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                .pkg("repository", base + ".repository")
                .addField(loader.parse("commentId", "long @id @sequence"))
                .searchResultDtoName("CommentSummaryDTO");

        assertTrue(render(e).contains("CommentSummaryDTO"), render(e));
    }
}
