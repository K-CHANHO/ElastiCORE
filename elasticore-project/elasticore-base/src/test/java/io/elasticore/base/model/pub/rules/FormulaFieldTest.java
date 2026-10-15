package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.JpaEntityBaseRules;
import io.elasticore.base.model.pub.rules.base.MapperAssembleRules;
import io.elasticore.base.model.pub.rules.base.MapperBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A @formula column is computed by the database on read. Its SQL survives
 * parsing whole, the mapper reads it out to the DTO but never writes it back,
 * and a TypeScript form treats it as shown-only.
 *
 * The field is the one a user reported: a name looked up by a subquery whose
 * condition has an '=', which the parser used to cut at that '='.
 */
public class FormulaFieldTest {

    private static final String EXPR =
            "coalesce((select cbu.name from system_user cbu where cbu.user_id = created_by), created_by)";
    private static final String LINE = "string(100) @readonly @formula(\"" + EXPR + "\") -- name";

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec user() {
        EntitySpec e = new EntitySpec("SystemUser")
                .domain("account", "Account")
                .pkg("dto", "io.acme.account.dto");
        e.addField(loader.parse("userId", "string(15) @id"));
        e.addField(loader.parse("name", "string(100)"));
        e.addField(loader.parse("createdByNm", LINE));
        e.addDtoField(loader.parse("userId", "string(15) @id"));
        e.addDtoField(loader.parse("name", "string(100)"));
        e.addDtoField(loader.parse("createdByNm", LINE));
        return e;
    }

    private SourceCode mapper() {
        return new RuleEngine().register(new MapperBaseRules())
                .register(new MapperAssembleRules())
                .run(user(), "mapper");
    }

    @Test
    public void theExpressionIsReadWhole() {
        FieldSpec f = null;
        for (FieldSpec x : user().fields()) if (x.name().equals("createdByNm")) f = x;
        assertTrue(f != null, "createdByNm not found");
        assertEquals(EXPR, f.dslValue("formula"));
    }

    /**
     * Hibernate reads @Column first and then ignores @Formula, so the field
     * carries @Formula alone: no @Column from its length, and no @Comment,
     * which fails the boot on a formula.
     */
    @Test
    public void theEntityMapsTheExpressionNotAColumn() {
        String fields = new RuleEngine().register(new JpaEntityBaseRules()).run(user()).renderFieldBlock();
        int end = fields.indexOf("private String createdByNm;");
        assertTrue(end >= 0, fields);
        String formula = fields.substring(fields.lastIndexOf(';', end - 1) + 1, end);
        assertTrue(formula.contains("@Formula(\"" + EXPR + "\")"), formula);
        assertFalse(formula.contains("@Column"), formula);
        assertFalse(formula.contains("@Comment"), formula);
        int nameEnd = fields.indexOf("private String name;");
        String name = fields.substring(fields.lastIndexOf(';', nameEnd - 1) + 1, nameEnd);
        assertTrue(name.contains("@Column(") && name.contains("length = 100)"), "an ordinary field keeps its column:\n" + name);
    }

    /** Not only a declared length: a length guessed from the name (...Nm), '!' and @unique made a @Column too. */
    @Test
    public void noColumnFromAGuessedLengthARequiredMarkOrUnique() {
        EntitySpec e = new EntitySpec("SystemUser")
                .domain("account", "Account")
                .pkg("dto", "io.acme.account.dto");
        e.addField(loader.parse("userId", "string(15) @id"));
        e.addField(loader.parse("modifiedByNm", "string! @unique @formula(\"" + EXPR + "\")"));
        String fields = new RuleEngine().register(new JpaEntityBaseRules()).run(e).renderFieldBlock();
        int end = fields.indexOf("private String modifiedByNm;");
        assertTrue(end >= 0, fields);
        String formula = fields.substring(fields.lastIndexOf(';', end - 1) + 1, end);
        assertTrue(formula.contains("@Formula(\"" + EXPR + "\")"), formula);
        assertFalse(formula.contains("@Column"), formula);
    }

    @Test
    public void theMapperReadsItOut() {
        String forward = mapper().renderBlock("forward");
        assertTrue(forward.contains("setVal(from.getCreatedByNm(), to::setCreatedByNm, isSkipNull);"), forward);
    }

    @Test
    public void theMapperNeverWritesItBack() {
        String reverse = mapper().renderBlock("reverse");
        assertFalse(reverse.contains("CreatedByNm"), "neither overload, with or without an EntityManager:\n" + reverse);
        assertTrue(reverse.contains("setVal(from.getName(), to::setName, isSkipNull);"),
                "an ordinary field is still written:\n" + reverse);
    }

    /** Without @readonly beside it: @formula alone makes it shown-only. */
    @Test
    public void aTypeScriptFormShowsItOnly() {
        EntitySpec e = new EntitySpec("SystemUser")
                .domain("account", "Account")
                .pkg("dto", "io.acme.account.dto");
        e.addField(loader.parse("userId", "string(15) @id"));
        e.addField(loader.parse("createdByNm", "string(100) @formula(\"" + EXPR + "\")"));
        String dto = RuleCatalog.standard()
                .engineFor("ts-model")
                .templateSet(SourceCode.TemplateSet.TYPESCRIPT)
                .run(e, "dto")
                .render();
        String line = null;
        for (String l : dto.split("\n")) if (l.trim().startsWith("createdByNm: {")) line = l;
        assertTrue(line != null && line.contains("readOnly: true"), dto);
        assertFalse(dto.contains("value.createdByNm"), "nothing to validate before sending:\n" + dto);
    }
}
