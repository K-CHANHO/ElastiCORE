package io.elasticore.base.model.loader;

import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for debugging complex length parameter parsing
 */
public class ComplexLengthParameterTest {

    @Test
    public void testComplexLengthParameterParsing() {
        // Create a test loader
        TestModelLoader loader = new TestModelLoader();

        // Test simple length parameter
        String simpleFieldLine = "string @length(23)";
        Field simpleField = loader.parseFieldLine("archiveFilePath", simpleFieldLine);

        System.out.println("=== Simple Length Test ===");
        System.out.println("Field line: " + simpleFieldLine);
        System.out.println("Has length annotation: " + simpleField.hasAnnotation("length"));
        System.out.println("Length annotation value: " + simpleField.getAnnotationValue("length"));
        System.out.println("getMaxLength(): " + simpleField.getMaxLength());

        // Test complex length parameter
        String complexFieldLine = "string @length(min=7,max=23)";
        Field complexField = loader.parseFieldLine("archiveFilePath", complexFieldLine);

        System.out.println("\n=== Complex Length Test ===");
        System.out.println("Field line: " + complexFieldLine);
        System.out.println("Has length annotation: " + complexField.hasAnnotation("length"));
        System.out.println("Length annotation value: " + complexField.getAnnotationValue("length"));
        System.out.println("getMaxLength(): " + complexField.getMaxLength());
        System.out.println("getMinLength(): " + complexField.getMinLength());

        // Check annotation type and properties
        Annotation lengthAnnotation = complexField.getAnnotation("length");
        if (lengthAnnotation != null) {
            System.out.println("Annotation type: " + lengthAnnotation.getType());
            System.out.println("Annotation value: " + lengthAnnotation.getValue());
            Properties props = lengthAnnotation.getProperties();
            if (props != null) {
                System.out.println("Properties: " + props);
                System.out.println("Min property: " + props.getProperty("min"));
                System.out.println("Max property: " + props.getProperty("max"));
            }
        }

        // Test with alternative alias 'len'
        String lenFieldLine = "string @len(min=1,max=100)";
        Field lenField = loader.parseFieldLine("lenTest", lenFieldLine);

        System.out.println("\n=== Alternative Alias Test (@len) ===");
        System.out.println("Field line: " + lenFieldLine);
        System.out.println("Has len annotation: " + lenField.hasAnnotation("len"));
        System.out.println("getMaxLength(): " + lenField.getMaxLength());
        System.out.println("getMinLength(): " + lenField.getMinLength());

        // Assertions
        assertTrue(simpleField.hasAnnotation("length"), "Simple length annotation should exist");
        assertEquals(23, simpleField.getMaxLength(), "Simple max length should be 23");

        assertTrue(complexField.hasAnnotation("length"), "Complex length annotation should exist");
        assertEquals(23, complexField.getMaxLength(), "Max length should be 23");
        assertEquals(7, complexField.getMinLength(), "Min length should be 7");

        assertTrue(lenField.hasAnnotation("len"), "Len annotation should exist");
        assertEquals(100, lenField.getMaxLength(), "Len max length should be 100");
        assertEquals(1, lenField.getMinLength(), "Len min length should be 1");
    }

    /**
     * string(255) puts @length(255) in front of the line, so an explicit
     * @length(min=8, max=255) after it became a second @length that nobody
     * read: the minimum was dropped without a word.
     */
    @Test
    public void aMinimumNextToATypeLengthIsKept() {
        TestModelLoader loader = new TestModelLoader();

        Field password = loader.parseFieldLine("password", "string(255)! @length(min=8, max=255)");
        assertEquals(8, password.getMinLength());
        assertEquals(255, password.getMaxLength());

        Field code = loader.parseFieldLine("code", "string(20) @length(min=3)");
        assertEquals(3, code.getMinLength());
        assertEquals(20, code.getMaxLength());

        // Unchanged: a type length alone has no minimum.
        assertEquals(-1, loader.parseFieldLine("name", "string(30)").getMinLength());
    }

    /** string(255) @length(max=64): the 64 is not used, and the build says so. */
    @Test
    public void aMaxThatDisagreesWithTheTypeLengthIsSaid() {
        TestModelLoader loader = new TestModelLoader();
        io.elasticore.base.util.ConsoleLog.clearKey("DSL_WARN");
        java.util.Map<String, String> fields = new java.util.LinkedHashMap<String, String>();
        fields.put("password", "string(255)! @length(min=8, max=64)");
        fields.put("login", "string(30) @length(min=3, max=30)");
        // The 2.x extractor fails on any DSL warning, and this DSL built there: not said.
        loader.fields(fields);
        assertEquals(0, io.elasticore.base.util.ConsoleLog.countStoredLogkey("DSL_WARN"));

        // The rule engine says it.
        io.elasticore.base.model.entity.BaseFieldType.ruleEngineTypes(true);
        try {
            loader.fields(fields);
        } finally {
            io.elasticore.base.model.entity.BaseFieldType.ruleEngineTypes(false);
        }
        assertEquals(1, io.elasticore.base.util.ConsoleLog.countStoredLogkey("DSL_WARN"));
        io.elasticore.base.util.ConsoleLog.clearKey("DSL_WARN");
    }

    /**
     * Test model loader that extends AbstractModelLoader
     */
    private static class TestModelLoader extends AbstractModelLoader {
        void fields(java.util.Map<String, String> fields) {
            super.parseField(fields, "Customer", null);
        }

        // No loader context here, so no configured name pattern to check against.
        @Override
        protected boolean isValidFieldName(String fieldName) {
            return true;
        }

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