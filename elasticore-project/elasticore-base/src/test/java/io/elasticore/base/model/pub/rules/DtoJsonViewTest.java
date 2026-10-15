package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Field-level @JsonView, ported from setJsonViewInfo /
 * generateJsonViewAnnotation / inferJsonViewFromAnnotations.
 *
 * The expected text comes from the committed jsonview_demo domain:
 * BlogPostDTO exercises @jsonview(auto) inference (env.yml declares
 * "jsonview: auto"), MemberDTO exercises explicit @view annotations
 * (the entity declares a bare @jsonview).
 */
public class DtoJsonViewTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String render(EntitySpec e) {
        return new RuleEngine().register(new DtoBaseRules())
                .run(e, "dto").renderFieldBlock(1);
    }

    /** A DTO in auto mode, as jsonview_demo/env.yml declares. */
    private EntitySpec auto(String name) {
        return new EntitySpec(name).jsonViewConfig("auto");
    }

    /** A DTO in manual mode: the entity carries a bare @jsonview. */
    private EntitySpec manual(String name) {
        return new EntitySpec(name).meta("jsonview", null);
    }

    // ---- auto inference (BlogPostDTO) -----------------------------------

    /**
     * BlogPost.id is "long @id @sequence": the server generates it, so it
     * leaves the create view but still identifies the row on update.
     */
    @Test
    public void auto_generatedId_dropsCreate() {
        EntitySpec e = auto("BlogPost").addField(loader.parse("id", "long @id @sequence"));

        assertTrue(render(e).contains("@JsonView({Views.Update.class, Views.Read.class})"), render(e));
    }

    /** BlogPost.title is unconstrained, so it stays visible everywhere. */
    @Test
    public void auto_plainField_emitsNothing() {
        EntitySpec e = auto("BlogPost").addField(loader.parse("title", "string(200)!"));

        assertFalse(render(e).contains("@JsonView"), render(e));
    }

    /** BlogPost.viewCount is "@updatable(false)", so it leaves the update view. */
    @Test
    public void auto_updatableFalse_dropsUpdate() {
        EntitySpec e = auto("BlogPost")
                .addField(loader.parse("viewCount", "int @default(0) @updatable(false)"));

        assertTrue(render(e).contains("@JsonView({Views.Create.class, Views.Read.class})"), render(e));
    }

    /** An audited timestamp is written by the server alone: read only. */
    @Test
    public void auto_serverManagedField_isReadOnly() {
        EntitySpec e = auto("BlogPost")
                .addField(loader.parse("createDate", "datetime @jpa:createdDate"));

        assertTrue(render(e).contains("@JsonView(Views.Read.class)"), render(e));
    }

    /** A write-only field is accepted but never returned. */
    @Test
    public void auto_writeonlyField_dropsRead() {
        EntitySpec e = auto("BlogPost").addField(loader.parse("secret", "string(200) @writeonly"));

        assertTrue(render(e).contains("@JsonView({Views.Create.class, Views.Update.class})"), render(e));
    }

    /**
     * A read-only field that is also write-only would be visible nowhere,
     * which 2.x declines to express.
     */
    @Test
    public void auto_visibleNowhere_emitsNothing() {
        EntitySpec e = auto("BlogPost")
                .addField(loader.parse("odd", "string(10) @readonly @writeonly"));

        assertFalse(render(e).contains("@JsonView"), render(e));
    }

    // ---- explicit @view (MemberDTO) --------------------------------------

    /** Member.id is "@view(r)": a single view renders bare, not as an array. */
    @Test
    public void explicit_singleView_rendersBare() {
        EntitySpec e = manual("Member").addField(loader.parse("id", "long @id @sequence @view(r)"));

        assertTrue(render(e).contains("@JsonView(Views.Read.class)"), render(e));
    }

    /** Member.email is "@view(cru)", and the views keep the declared order. */
    @Test
    public void explicit_allThreeViews_keepDeclaredOrder() {
        EntitySpec e = manual("Member").addField(loader.parse("email", "string(100)! @view(cru)"));

        assertTrue(render(e).contains(
                "@JsonView({Views.Create.class, Views.Read.class, Views.Update.class})"), render(e));
    }

    /** Member.bio is "@view(cu)": accepted on input, absent from the response. */
    @Test
    public void explicit_twoViews_renderAsArray() {
        EntitySpec e = manual("Member").addField(loader.parse("bio", "string(500) @view(cu)"));

        assertTrue(render(e).contains("@JsonView({Views.Create.class, Views.Update.class})"), render(e));
    }

    /** The comma-separated form names the views in full. */
    @Test
    public void explicit_commaForm_isEquivalent() {
        EntitySpec e = manual("Member")
                .addField(loader.parse("nickname", "string(50) @view(create,read)"));

        assertTrue(render(e).contains("@JsonView({Views.Create.class, Views.Read.class})"), render(e));
    }

    /** An explicit @view overrides what auto inference would have chosen. */
    @Test
    public void explicit_overridesAutoInference() {
        EntitySpec e = auto("Member").addField(loader.parse("id", "long @id @sequence @view(r)"));

        String out = render(e);
        assertTrue(out.contains("@JsonView(Views.Read.class)"), out);
        assertFalse(out.contains("Views.Update.class"), out);
    }

    // ---- mode resolution --------------------------------------------------

    /** Manual mode leaves a field without @view visible in every view. */
    @Test
    public void manualMode_withoutView_emitsNothing() {
        EntitySpec e = manual("Member").addField(loader.parse("username", "string(50)!"));

        assertFalse(render(e).contains("@JsonView"), render(e));
    }

    /** With the feature off nothing is emitted, not even for an explicit @view. */
    @Test
    public void disabledMode_emitsNothingEvenWithView() {
        EntitySpec e = new EntitySpec("Member")
                .addField(loader.parse("id", "long @id @view(r)"));

        assertFalse(render(e).contains("@JsonView"), render(e));
    }

    /** The DTO's own @jsonview(false) overrides an env.yml that says "auto". */
    @Test
    public void dtoAnnotation_overridesEnvConfig() {
        EntitySpec e = new EntitySpec("BlogPost").jsonViewConfig("auto")
                .meta("jsonview", "false")
                .addField(loader.parse("id", "long @id @sequence"));

        assertFalse(render(e).contains("@JsonView"), render(e));
    }

    /** An env.yml of "true" selects manual mode, so only @view fields annotate. */
    @Test
    public void envConfigTrue_selectsManualMode() {
        EntitySpec e = new EntitySpec("Member").jsonViewConfig("true")
                .addField(loader.parse("id", "long @id @sequence"));

        assertFalse(render(e).contains("@JsonView"), render(e));
    }

    // ---- position among the other annotations ------------------------------

    /**
     * MemberDTO.role shows @JsonView after the validation annotations and
     * before @Builder.Default.
     */
    @Test
    public void jsonView_sitsAfterValidationAndBeforeBuilderDefault() {
        EntitySpec e = manual("Member")
                .addField(loader.parse("role", "string(20) @default(\"USER\") @view(r)"));

        String out = render(e);
        assertTrue(out.indexOf("@Size(max=20)") < out.indexOf("@JsonView"), out);
        assertTrue(out.indexOf("@JsonView") < out.indexOf("@Builder.Default"), out);
    }

    /**
     * MemberDTO.lastLoginAt shows @JsonView before the date format pair, which
     * DtoSrcFilePublisher emits last.
     */
    @Test
    public void jsonView_sitsBeforeTheDateFormatPair() {
        EntitySpec e = manual("Member").addField(loader.parse("lastLoginAt", "datetime @view(r)"));

        String out = render(e);
        assertTrue(out.contains("@JsonView(Views.Read.class)"), out);
        assertTrue(out.indexOf("@JsonView")
                < out.indexOf("@org.springframework.format.annotation.DateTimeFormat"), out);
    }

    /**
     * BlogPostDTO.author shows the required-field validation ahead of the
     * inferred @JsonView.
     */
    @Test
    public void requiredField_keepsValidationAheadOfJsonView() {
        EntitySpec e = auto("BlogPost")
                .addField(loader.parse("author", "string(100)! @updatable(false)"));

        String out = render(e);
        assertTrue(out.indexOf("@NotNull") < out.indexOf("@JsonView"), out);
        assertTrue(out.indexOf("@Size(max=100)") < out.indexOf("@JsonView"), out);
    }
}
