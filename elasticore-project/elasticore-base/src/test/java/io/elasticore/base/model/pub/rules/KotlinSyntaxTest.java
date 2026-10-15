package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The seam a second target language goes through.
 *
 * Everything a rule builds is either language-neutral or comes from the
 * template set; only an import and a declaration are written by the emission
 * layer itself, so those are what a language has to answer for. These state
 * both answers, and that Java's are unchanged.
 */
public class KotlinSyntaxTest {

    @Test
    public void javaWritesFieldsAndSemicolons() {
        assertEquals("import java.time.LocalDate;",
                Syntax.JAVA.importLine("java.time.LocalDate"));
        assertEquals("private String name;",
                Syntax.JAVA.declaration("private", "String", "name", "", false));
        assertEquals("private Long count = 0L;",
                Syntax.JAVA.declaration("private", "Long", "count", " = 0L", true));
    }

    @Test
    public void kotlinWritesPropertiesAndNoSemicolons() {
        assertEquals("import java.time.LocalDate",
                Syntax.KOTLIN.importLine("java.time.LocalDate"));
    }

    /**
     * A Kotlin property has to be initialised, so one the DSL did not mark
     * required is nullable and starts null rather than being left unassigned.
     */
    @Test
    public void optionalProperty_isNullableAndStartsNull() {
        assertEquals("var name: String? = null",
                Syntax.KOTLIN.declaration("var", "String", "name", "", false));
    }

    /** A required property with a default keeps both the type and the value. */
    @Test
    public void requiredPropertyWithDefault_staysNonNull() {
        assertEquals("var count: Long = 0L",
                Syntax.KOTLIN.declaration("var", "Long", "count", " = 0L", true));
    }

    /**
     * Required but with nothing to start from: the declaration still has to
     * compile, so it is nullable. Constructor properties are the idiomatic
     * answer and are not what this seam renders yet.
     */
    @Test
    public void requiredPropertyWithoutDefault_staysCompilable() {
        assertEquals("var name: String? = null",
                Syntax.KOTLIN.declaration("var", "String", "name", "", true));
    }

    /** The modifier a declaration takes by default differs by language. */
    @Test
    public void defaultModifier_followsTheLanguage() {
        assertEquals("private", Syntax.JAVA.fieldModifier());
        assertEquals("var", Syntax.KOTLIN.fieldModifier());
    }
}
