package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.BaseFieldType;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import io.elasticore.base.util.ConsoleLog;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @pattern on a field: a regular expression the value must match.
 *
 * The clean output writes Jakarta's @Pattern with regexp and message - the
 * 2.x one spelled the message attribute msg, which Jakarta does not have, so
 * the DTO did not compile - and reads the positional form @pattern('regex',
 * 'message') as @min reads its value and message. The compatible output keeps
 * what 2.x writes (DtoGapCoverageTest holds that golden).
 */
public class PatternAnnotationTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }

        void fields(Map<String, String> fields) {
            super.parseField(fields, "PatternBook", null);
        }

        @Override
        protected boolean isValidFieldName(String fieldName) {
            return true;
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec book(String isbnLine) {
        return new EntitySpec("Book")
                .domain("library", "Library")
                .pkg("entity", "com.acme.library.entity")
                .pkg("dto", "com.acme.library.dto")
                .addField(loader.parse("isbn", isbnLine));
    }

    private FieldSpec field(String line) {
        return new EntitySpec("Book").addField(loader.parse("code", line)).fields().get(0);
    }

    private static String java(EntitySpec e) {
        return RuleCatalog.standard().engineFor("jpa-dto", "jpa-dto-file")
                .templateSet(SourceCode.TemplateSet.V3).run(e, "dto").render();
    }

    private static String kotlin(EntitySpec e) {
        return RuleCatalog.standard().engineFor("jpa-springboot-kt")
                .templateSet(SourceCode.TemplateSet.KOTLIN).run(e, "dto").render();
    }

    private static String typescript(EntitySpec e) {
        return RuleCatalog.standard().engineFor("ts-model")
                .templateSet(SourceCode.TemplateSet.TYPESCRIPT).run(e, "dto").render();
    }

    private static String compatible(EntitySpec e) {
        return new RuleEngine().register(new DtoBaseRules()).run(e, "dto").renderFieldBlock(1);
    }

    // ---- reading the DSL -------------------------------------------------

    @Test
    public void theNamedFormIsRead() {
        FieldSpec f = field("string @pattern(regexp=\"^[0-9]{10,13}$\", message=\"ISBN is 10 or 13 digits\")");
        assertEquals("^[0-9]{10,13}$", f.patternRegexp());
        assertEquals("ISBN is 10 or 13 digits", f.patternMessage());

        FieldSpec single = field("string @pattern(regexp='^[A-Z]+$')");
        assertEquals("^[A-Z]+$", single.patternRegexp());
        assertNull(single.patternMessage());
    }

    @Test
    public void thePositionalFormIsReadWithAndWithoutAMessage() {
        FieldSpec two = field("string @pattern('^[0-9]{13}$', 'ISBN must be 13 digits')");
        assertEquals("^[0-9]{13}$", two.patternRegexp());
        assertEquals("ISBN must be 13 digits", two.patternMessage());

        FieldSpec one = field("string @pattern('^[A-Z]+$')");
        assertEquals("^[A-Z]+$", one.patternRegexp());
        assertNull(one.patternMessage());

        FieldSpec doubleQuoted = field("string @pattern(\"^[a-z']+$\", \"letters, or an apostrophe\")");
        assertEquals("^[a-z']+$", doubleQuoted.patternRegexp());
        assertEquals("letters, or an apostrophe", doubleQuoted.patternMessage());
    }

    /** {10,13} is a quantifier, not the comma that separates the message. */
    @Test
    public void aCommaInsideTheRegexStaysInTheRegex() {
        assertEquals("^[0-9]{10,13}$", field("string @pattern('^[0-9]{10,13}$', 'x')").patternRegexp());
        assertEquals("x", field("string @pattern('^[0-9]{10,13}$', 'x')").patternMessage());
        assertEquals("^[0-9]{10,13}$", field("string @pattern(^[0-9]{10,13}$)").patternRegexp());
        assertNull(field("string @pattern(^[0-9]{10,13}$)").patternMessage());
    }

    @Test
    public void aDoubleQuoteInTheMessageBecomesASingleOne() {
        assertEquals("say 'x'", field("string @pattern('^x$', 'say \"x\"')").patternMessage());
    }

    @Test
    public void withoutPatternBothAreNull() {
        FieldSpec f = field("string(10)");
        assertNull(f.patternRegexp());
        assertNull(f.patternMessage());
    }

    // ---- the Java DTO ------------------------------------------------------

    @Test
    public void cleanJava_namedForm_writesMessageOnOneLine() {
        String out = java(book("string(10) @pattern(regexp=abc,message=bad)"));
        assertTrue(out.contains("\t@Pattern(regexp = \"abc\", message = \"bad\")\n"), out);
        assertFalse(out.contains("msg="), out);
    }

    /** The library's ISBN: @NotNull, then @Pattern, then the @Size of string(13). */
    @Test
    public void cleanJava_positionalForm_inValidationOrder() {
        String out = java(book("string(13)! @id @pattern('^[0-9]{13}$', 'ISBN must be 13 digits') -- ISBN, 13 digits"));
        String block = "\t@Schema(description = \"ISBN, 13 digits\" , requiredMode=Schema.RequiredMode.REQUIRED )\n"
                + "\t@NotNull\n"
                + "\t@Pattern(regexp = \"^[0-9]{13}$\", message = \"ISBN must be 13 digits\")\n"
                + "\t@Size(max=13)\n"
                + "\tprivate String isbn;\n";
        assertTrue(out.contains(block), out);
    }

    @Test
    public void cleanJava_withoutAMessage_saysWhatTheClientSays() {
        String out = java(book("string(10) @pattern('^[A-Z]+$') -- ISBN"));
        assertTrue(out.contains("\t@Pattern(regexp = \"^[A-Z]+$\", message = \"ISBN is not in the expected form\")\n"), out);
    }

    @Test
    public void cleanJava_aCommaInsideTheRegexIsKept() {
        String named = java(book("string(13) @pattern(regexp=\"^[0-9]{10,13}$\", message=\"x\")"));
        String positional = java(book("string(13) @pattern('^[0-9]{10,13}$', 'x')"));
        String line = "\t@Pattern(regexp = \"^[0-9]{10,13}$\", message = \"x\")\n";
        assertTrue(named.contains(line), named);
        assertTrue(positional.contains(line), positional);
    }

    /** The annotation guide's example: an @ and a {2,} inside a double-quoted regexp. */
    @Test
    public void cleanJava_theGuidesEmailExample() {
        String out = java(book("string @pattern(regexp=\"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$\", message=\"Invalid email\")"));
        assertTrue(out.contains("\t@Pattern(regexp = \"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$\", message = \"Invalid email\")\n"), out);
    }

    /**
     * Swagger offers the example as a value to send; the regex is not one, and
     * the positional form is not even one string. The clean output leaves it
     * out; the compatible one keeps 2.x's example.
     */
    @Test
    public void theRegexIsNotTheSchemaExample() {
        String clean = java(book("string(13) @pattern('^[0-9]{13}$', 'ISBN must be 13 digits')"));
        assertTrue(clean.contains("\t@Schema(description = \"isbn\"  )\n"), clean);
        assertFalse(clean.contains("example="), clean);

        String compatible = compatible(book("string(10) @pattern('^[A-Z]+$')"));
        assertTrue(compatible.contains("example=\"^[A-Z]+$\""), compatible);
    }

    /** On a date, @pattern is the format the date is read and written in, as before. */
    @Test
    public void onADateThePatternStaysTheFormat() {
        String out = java(book("date @pattern(yyyyMMdd)"));
        assertFalse(out.contains("@Pattern("), out);
        assertTrue(out.contains("@JsonFormat(pattern = \"yyyyMMdd\")")
                || out.contains("@com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyyMMdd\")"), out);
    }

    /** The compatible output is 2.x's: the positional form wrote nothing there, and still writes nothing. */
    @Test
    public void compatible_positionalForm_staysUnwritten() {
        String out = compatible(book("string(13) @pattern('^[0-9]{13}$', 'ISBN must be 13 digits')"));
        assertFalse(out.contains("validation.constraints.Pattern"), out);
    }

    // ---- the Kotlin DTO ----------------------------------------------------

    /**
     * A dollar before a name would start a string template; the end anchor
     * before the closing quote would not, and stays as written.
     */
    @Test
    public void kotlin_escapesOnlyTheDollarThatStartsATemplate() {
        String out = kotlin(book("string(20) @pattern('^\\\\$USD[0-9]+$', 'write it as $USD100')"));
        assertTrue(out.contains("    @Pattern(regexp = \"^\\\\\\$USD[0-9]+$\", message = \"write it as \\$USD100\")\n"), out);

        String isbn = kotlin(book("string(13)! @pattern('^[0-9]{13}$', 'ISBN must be 13 digits')"));
        assertTrue(isbn.contains("    @Pattern(regexp = \"^[0-9]{13}$\", message = \"ISBN must be 13 digits\")\n"), isbn);
        assertTrue(isbn.contains("import jakarta.validation.constraints.Pattern"), isbn);
        assertFalse(isbn.contains("msg="), isbn);
    }

    @Test
    public void java_leavesTheDollarAlone() {
        String out = java(book("string(20) @pattern('^\\\\$USD[0-9]+$', 'write it as $USD100')"));
        assertTrue(out.contains("\t@Pattern(regexp = \"^\\\\$USD[0-9]+$\", message = \"write it as $USD100\")\n"), out);
    }

    // ---- the TypeScript client --------------------------------------------

    @Test
    public void typescript_validatesWithTheSameRegexAndMessage() {
        String out = typescript(book("string(13)! @pattern('^[0-9]{13}$', 'ISBN must be 13 digits')"));
        assertTrue(out.contains("else if (value.isbn && !new RegExp(\"^(?:^[0-9]{13}$)$\").test(String(value.isbn))) "
                + "errors.isbn = \"ISBN must be 13 digits\";"), out);

        String fallback = typescript(book("string(10) @pattern('^[A-Z]+$') -- ISBN"));
        assertTrue(fallback.contains("errors.isbn = \"ISBN is not in the expected form\";"), fallback);
        assertTrue(fallback.contains("new RegExp(\"^(?:^[A-Z]+$)$\")"), fallback);
    }

    // ---- the loader --------------------------------------------------------

    /** \d in a string literal is not a backslash and a d: said by the rule engine, not by 2.x. */
    @Test
    public void aLoneBackslashIsSaidByTheRuleEngineOnly() {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        fields.put("isbn", "string(13) @pattern('^\\d{13}$', 'm')");
        fields.put("code", "string(13) @pattern('^\\\\d{13}$')");
        String prefix = "PatternBook.";

        int before = ConsoleLog.countByPrefix("DSL_WARN", prefix);
        loader.fields(fields);
        assertEquals(before, ConsoleLog.countByPrefix("DSL_WARN", prefix));

        BaseFieldType.ruleEngineTypes(true);
        try {
            loader.fields(fields);
        } finally {
            BaseFieldType.ruleEngineTypes(false);
        }
        assertEquals(before + 1, ConsoleLog.countByPrefix("DSL_WARN", prefix));
        assertEquals(1, ConsoleLog.countByPrefix("DSL_WARN",
                "PatternBook.isbn: @pattern has \\d, which a string literal does not read as a backslash. Write it doubled (\\\\d)"));
        ConsoleLog.clearKey("DSL_WARN");
    }
}
