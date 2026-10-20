package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.MapperAssembleRules;
import io.elasticore.base.model.pub.rules.base.MapperBaseRules;
import io.elasticore.base.model.pub.rules.base.MapperSpecRules;
import io.elasticore.base.model.pub.rules.base.MapperSpecShellRules;
import io.elasticore.base.model.pub.rules.lang.java.MapperFileRules;
import io.elasticore.base.model.pub.rules.lang.kotlin.KotlinMapperAssembleRules;
import io.elasticore.base.model.pub.rules.lang.kotlin.KotlinMapperSpecShellRules;
import io.elasticore.base.util.ConsoleLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A field-level @readonly (alias @noinput) is set by the server: shown in
 * responses, never taken from a request. In the v3 bundles the mapper does
 * not write it back, the DTO does not require it and the OpenAPI schema
 * says READ_ONLY. The 2.x-compatible bundle keeps 2.x's output.
 */
public class ServerSetFieldTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /** The library's member: joined and graded by the server. */
    private EntitySpec member() {
        return member("long @id @sequence");
    }

    private EntitySpec member(String idLine) {
        EntitySpec e = new EntitySpec("Member")
                .domain("library", "Library")
                .pkg("entity", "com.acme.library.entity")
                .pkg("dto", "com.acme.library.dto");
        String[][] fields = {
                {"memberId", idLine},
                {"name", "string(50)!"},
                {"joinedAt", "date @readonly"},
                {"grade", "string(10)! @noinput"},
        };
        for (String[] f : fields) {
            e.addField(loader.parse(f[0], f[1]));
            e.addDtoField(loader.parse(f[0], f[1]));
        }
        return e;
    }

    /**
     * The mapper section the way RulesGenRunner composes it: the bundle's
     * rules without the section shells, then the two shells by name.
     */
    static SourceCode mapper(EntitySpec e, String bundle, SourceCode.TemplateSet set) {
        boolean kotlin = set == SourceCode.TemplateSet.KOTLIN;
        RuleEngine engine = new RuleEngine().templateSet(set);
        for (CodeRules r : RuleCatalog.standard().rulesOf(bundle)) {
            if (r instanceof MapperBaseRules || r instanceof MapperAssembleRules
                    || r instanceof MapperSpecRules || r instanceof MapperSpecShellRules
                    || r instanceof MapperFileRules || r instanceof KotlinMapperAssembleRules
                    || r instanceof KotlinMapperSpecShellRules) continue;
            engine.register(r);
        }
        for (String shell : new String[]{"jpa-mapper", kotlin ? "jpa-mapper-assemble-kt" : "jpa-mapper-assemble"}) {
            for (CodeRules r : RuleCatalog.standard().rulesOf(shell)) engine.register(r);
        }
        return engine.run(e, "mapper");
    }

    private String dto(EntitySpec e, String bundle, SourceCode.TemplateSet set) {
        return RuleCatalog.standard().engineFor(bundle).templateSet(set).run(e, "dto").render();
    }

    /** The field's own declaration, from its annotations to the semicolon. */
    private static String declarationOf(String source, String field) {
        int at = source.indexOf(" " + field + ";");
        if (at < 0) at = source.indexOf(" " + field + " =");
        assertTrue(at >= 0, field + " not declared:\n" + source);
        int from = source.lastIndexOf(";", at);
        int prevBrace = source.lastIndexOf("}", at);
        return source.substring(Math.max(from, prevBrace) + 1, at);
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) n++;
        return n;
    }

    // ---- the mapper ----------------------------------------------------

    @Test
    public void theMapperReadsItOut() {
        String forward = mapper(member(), "jpa-springboot-v3", SourceCode.TemplateSet.V3).renderBlock("forward");
        assertTrue(forward.contains("setVal(from.getJoinedAt(), to::setJoinedAt, isSkipNull);"), forward);
        assertTrue(forward.contains("setVal(from.getGrade(), to::setGrade, isSkipNull);"), forward);
    }

    @Test
    public void theMapperNeverWritesItBack() {
        String reverse = mapper(member(), "jpa-springboot-v3", SourceCode.TemplateSet.V3).renderBlock("reverse");
        assertFalse(reverse.contains("JoinedAt"), "@readonly, in neither overload:\n" + reverse);
        assertFalse(reverse.contains("Grade"), "@noinput, in neither overload:\n" + reverse);
        // Written in both overloads, with and without an EntityManager.
        assertEquals(2, count(reverse, "setVal(from.getName(), to::setName, isSkipNull);"), reverse);
        assertTrue(reverse.contains("mapping(MemberDTO from, Member to, boolean isSkipNull, EntityManager em)"), reverse);
    }

    /** The key keeps its write: "@id @genid @readonly" only hides a generated key from input. */
    @Test
    public void theIdentityIsStillWritten() {
        EntitySpec e = member("long @id @sequence @readonly");
        String reverse = mapper(e, "jpa-springboot-v3", SourceCode.TemplateSet.V3).renderBlock("reverse");
        assertEquals(2, count(reverse, "setVal(from.getMemberId(), to::setMemberId, isSkipNull);"), reverse);
    }

    /** A DTO that declares the field @readonly itself: that DTO's mapping skips it. */
    @Test
    public void aDtoFieldMarkedReadonlyIsSkippedToo() {
        EntitySpec e = new EntitySpec("Member")
                .domain("library", "Library")
                .pkg("dto", "com.acme.library.dto");
        e.addField(loader.parse("memberId", "long @id @sequence"));
        e.addField(loader.parse("joinedAt", "date"));
        e.addDtoField(loader.parse("memberId", "long @id @sequence"));
        e.addDtoField(loader.parse("joinedAt", "date @readonly"));
        String reverse = mapper(e, "jpa-springboot-v3", SourceCode.TemplateSet.V3).renderBlock("reverse");
        assertFalse(reverse.contains("JoinedAt"), reverse);
    }

    /** The 2.x-compatible bundle has no write guard: its mapper is 2.x's byte for byte. */
    @Test
    public void theCompatibleMapperStillWritesIt() {
        String reverse = mapper(member(), "jpa-springboot", SourceCode.TemplateSet.COMPATIBLE).renderBlock("reverse");
        assertEquals(2, count(reverse, "setVal(from.getJoinedAt(), to::setJoinedAt, isSkipNull);"), reverse);
        assertEquals(2, count(reverse, "setVal(from.getGrade(), to::setGrade, isSkipNull);"), reverse);
    }

    @Test
    public void theKotlinMapperNeverWritesItBack() {
        SourceCode code = mapper(member(), "jpa-springboot-kt", SourceCode.TemplateSet.KOTLIN);
        String forward = code.renderBlock("forward");
        String reverse = code.renderBlock("reverse");
        assertTrue(forward.contains("setVal(from.joinedAt, { v -> to.joinedAt = v }, isSkipNull)"), forward);
        assertFalse(reverse.contains("joinedAt"), reverse);
        assertFalse(reverse.contains("grade"), reverse);
        assertEquals(2, count(reverse, "setVal(from.name, { v -> to.name = v }, isSkipNull)"), reverse);
    }

    // ---- the DTO -------------------------------------------------------

    @Test
    public void theDtoShowsItReadOnlyAndDoesNotRequireIt() {
        String out = dto(member(), "jpa-springboot-v3", SourceCode.TemplateSet.V3);
        String grade = declarationOf(out, "grade");
        assertTrue(grade.contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), grade);
        assertTrue(grade.contains("accessMode = Schema.AccessMode.READ_ONLY"), grade);
        assertFalse(grade.contains("NotNull"), "a request cannot send it, so it cannot be required:\n" + grade);
        String joinedAt = declarationOf(out, "joinedAt");
        assertTrue(joinedAt.contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), joinedAt);
        assertTrue(joinedAt.contains("accessMode = Schema.AccessMode.READ_ONLY"), joinedAt);
        // An ordinary required field is still required and writable.
        String name = declarationOf(out, "name");
        assertTrue(name.contains("NotNull"), name);
        assertFalse(name.contains("READ_ONLY"), name);
    }

    @Test
    public void aRequiredBlankCheckGoesToo() {
        EntitySpec e = new EntitySpec("Member").domain("library", "Library").pkg("dto", "com.acme.library.dto");
        e.addField(loader.parse("code", "string(10)! @notblank @readonly"));
        String code = declarationOf(dto(e, "jpa-springboot-v3", SourceCode.TemplateSet.V3), "code");
        assertFalse(code.contains("NotNull"), code);
        assertFalse(code.contains("NotBlank"), code);
    }

    /** The 2.x-compatible DTO keeps 2.x's lines: READ_ONLY in Jackson, @NotNull, no schema access mode. */
    @Test
    public void theCompatibleDtoIsUnchanged() {
        String out = dto(member(), "jpa-springboot", SourceCode.TemplateSet.COMPATIBLE);
        String grade = declarationOf(out, "grade");
        assertTrue(grade.contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), grade);
        assertTrue(grade.contains("@NotNull"), grade);
        assertFalse(out.contains("accessMode"), out);
    }

    /** An auditing field the mapper already leaves alone; @readonly makes the DTO and the schema say so. */
    @Test
    public void anAuditFieldMarkedReadonlyIsReadOnlyInTheDtoToo() {
        String line = "string(20) @jpa:org.springframework.data.annotation.CreatedBy @readonly";
        EntitySpec e = new EntitySpec("Member").domain("library", "Library").pkg("dto", "com.acme.library.dto");
        e.addField(loader.parse("memberId", "long @id @sequence"));
        e.addField(loader.parse("createdBy", line));
        e.addDtoField(loader.parse("memberId", "long @id @sequence"));
        e.addDtoField(loader.parse("createdBy", line));
        String createdBy = declarationOf(dto(e, "jpa-springboot-v3", SourceCode.TemplateSet.V3), "createdBy");
        assertTrue(createdBy.contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), createdBy);
        assertTrue(createdBy.contains("accessMode = Schema.AccessMode.READ_ONLY"), createdBy);
        SourceCode code = mapper(e, "jpa-springboot-v3", SourceCode.TemplateSet.V3);
        assertTrue(code.renderBlock("forward").contains("setVal(from.getCreatedBy(), to::setCreatedBy, isSkipNull);"));
        assertFalse(code.renderBlock("reverse").contains("CreatedBy"), code.renderBlock("reverse"));
    }

    @Test
    public void theKotlinDtoShowsItReadOnly() {
        String out = dto(member(), "jpa-springboot-kt", SourceCode.TemplateSet.KOTLIN);
        int at = out.indexOf("var grade:");
        assertTrue(at >= 0, out);
        String grade = out.substring(out.lastIndexOf("var name:", at), at);
        assertTrue(grade.contains("accessMode = Schema.AccessMode.READ_ONLY"), grade);
        assertTrue(grade.contains("JsonProperty.Access.READ_ONLY"), grade);
        assertFalse(grade.contains("NotNull"), grade);
    }

    // ---- a DDD root opened with @editable --------------------------------

    private EntitySpec editableMember() {
        EntitySpec e = new EntitySpec("Member")
                .domain("library", "Library")
                .meta("root", "")
                .meta("editable", "")
                .pkg("entity", "com.acme.library.domain.model")
                .pkg("dto", "com.acme.library.application.dto");
        e.addField(loader.parse("memberId", "long @id @sequence"));
        e.addField(loader.parse("name", "string(50)!"));
        e.addField(loader.parse("joinedAt", "date @readonly"));
        return e;
    }

    private String ddd(EntitySpec e, String target) {
        return RuleCatalog.standard().engineFor("ddd-springboot")
                .templateSet(SourceCode.TemplateSet.V3).run(e, target).render();
    }

    /** The setter stays public for the service's beforeSave; the request still cannot reach it. */
    @Test
    public void anEditableRootKeepsThePublicSetterAndTheMapperLeavesTheField() {
        String entity = ddd(editableMember(), "entity");
        assertFalse(declarationOf(entity, "joinedAt").contains("AccessLevel.PROTECTED"), entity);
        assertFalse(entity.contains("class Creation"), entity);

        String reverse = mapper(editableMember(), "ddd-springboot", SourceCode.TemplateSet.V3).renderBlock("reverse");
        assertFalse(reverse.contains("JoinedAt"), reverse);
        assertTrue(reverse.contains("setVal(from.getName(), to::setName, isSkipNull);"), reverse);

        String dto = ddd(editableMember(), "dto");
        assertTrue(declarationOf(dto, "joinedAt").contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), dto);
    }

    /** @behavior needs a split root; on an @editable one it is still only a warning. */
    @Test
    public void behaviorOnAnEditableRootIsStillAWarning() {
        EntitySpec e = editableMember();
        e.addField(loader.parse("status", "string(3) @behavior"));
        ConsoleLog.clearKey("DSL_WARN");
        try {
            String entity = ddd(e, "entity");
            assertFalse(declarationOf(entity, "status").contains("AccessLevel.PROTECTED"), entity);
            assertEquals(1, ConsoleLog.countStoredLogkey("DSL_WARN"));
        } finally {
            ConsoleLog.clearKey("DSL_WARN");
        }
    }
}
