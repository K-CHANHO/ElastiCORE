package io.elasticore.base.model.loader;

import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A quoted annotation value is one value, even when it contains '='.
 * Before the fix @formula("... where u.id = created_by)") was read as
 * key=value pairs and only its last fragment, created_by)", was kept.
 */
public class QuotedParameterTest {

    private final TestModelLoader loader = new TestModelLoader();

    @Test
    public void formulaWithEqualsKeepsTheWholeExpression() {
        String expr = "coalesce((select u.name from system_user u where u.user_id = created_by), created_by)";
        Field f = loader.parseFieldLine("createdByNm",
                "string(100) @readonly @formula(\"" + expr + "\") -- name");

        // The value is stored without its quotes.
        assertEquals(expr, f.getAnnotationValue("formula"));
        assertTrue(f.hasAnnotation("readonly"));
        assertEquals(100, f.getMaxLength());
    }

    @Test
    public void theGuideExampleParses() {
        Field f = loader.parseFieldLine("orderCount",
                "int @formula(\"(select count(*) from orders o where o.customer_id = id)\")");

        assertEquals("(select count(*) from orders o where o.customer_id = id)",
                f.getAnnotationValue("formula"));
    }

    @Test
    public void anSqlLiteralInsideIsPartOfTheExpression() {
        Field f = loader.parseFieldLine("activeCount",
                "int @formula(\"(select count(*) from member m where m.use_yn = 'Y')\")");

        assertEquals("(select count(*) from member m where m.use_yn = 'Y')", f.getAnnotationValue("formula"));
    }

    @Test
    public void paddingInsideTheParenthesesIsIgnored() {
        Field f = loader.parseFieldLine("x", "int @formula( \"a = 1\" )");
        assertEquals("a = 1", f.getAnnotationValue("formula"));

        Field g = loader.parseFieldLine("y", "int @formula(\"a=1\" )");
        assertEquals("a=1", g.getAnnotationValue("formula"));
    }

    @Test
    public void singleQuotedValueIsOneValueToo() {
        Field f = loader.parseFieldLine("cnt",
                "int @formula('(select o.cnt from orders o where o.customer_id = id)')");

        assertEquals("(select o.cnt from orders o where o.customer_id = id)",
                f.getAnnotationValue("formula"));
    }

    @Test
    public void aQuotedValueWithoutEqualsIsUnchanged() {
        Field f = loader.parseFieldLine("dtype", "string @formula(\"dtype\")");
        assertEquals("dtype", f.getAnnotationValue("formula"));
    }

    @Test
    public void keyValuePairsAreStillPairs() {
        Field f = loader.parseFieldLine("path", "string @length(min=7,max=23)");
        assertEquals(7, f.getMinLength());
        assertEquals(23, f.getMaxLength());

        Annotation a = loader.parseParameters("x", "name=\"a\", size=3");
        assertEquals("a", a.getProperties().getProperty("name"));
        assertEquals("3", a.getProperties().getProperty("size"));
    }

    @Test
    public void twoQuotedValuesAreNotOneValue() {
        Annotation a = loader.parseParameters("x", "\"a\", b=\"c\"");
        assertEquals("c", a.getProperties().getProperty("b"));
    }

    private static class TestModelLoader extends AbstractModelLoader {
        @Override
        public Field parseFieldLine(String fieldNm, String fieldLine) {
            return super.parseFieldLine(fieldNm, fieldLine, null);
        }

        @Override
        public Map<String, Annotation> loadAnnotationMap(String fieldLine) {
            return super.loadAnnotationMap(fieldLine);
        }

        @Override
        public Annotation parseParameters(String annotationName, String parameters) {
            return super.parseParameters(annotationName, parameters);
        }
    }
}
