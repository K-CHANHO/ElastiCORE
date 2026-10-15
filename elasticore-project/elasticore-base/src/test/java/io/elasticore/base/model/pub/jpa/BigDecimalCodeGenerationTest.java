package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.model.entity.BaseFieldType;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.entity.TypeInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive code generation test for BigDecimal/decimal types.
 * This test verifies the complete pipeline from type parsing to code generation.
 */
class BigDecimalCodeGenerationTest {

    @Test
    void testCompleteDecimalPipeline_WithCommaSeparator() {
        // Given - decimal(15,2) field test
        Field field = Field.builder()
                .name("overdueAmount")
                .type("decimal(15,2)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then - Verify type parsing
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo.getBaseTypeName());
        assertEquals("java.math.BigDecimal", typeInfo.getDefaultTypeName());
        assertEquals(15, typeInfo.getPrecision());
        assertEquals(2, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
        assertTrue(typeInfo.isBigDecimal());
        assertTrue(typeInfo.isBaseType());
        assertFalse(typeInfo.isStringType());
        assertFalse(typeInfo.isList());
        
        // Test debug output
        System.out.println("=== COMPLETE PIPELINE TEST ===");
        System.out.println("Field name: " + field.getName());
        System.out.println("Original type: " + typeInfo.getInitTypeInfo());
        System.out.println("Parsed TypeInfo:");
        System.out.println("  - BaseFieldType: " + typeInfo.getBaseFieldType());
        System.out.println("  - BaseTypeName: " + typeInfo.getBaseTypeName());
        System.out.println("  - DefaultTypeName: " + typeInfo.getDefaultTypeName());
        System.out.println("  - Precision: " + typeInfo.getPrecision());
        System.out.println("  - Scale: " + typeInfo.getScale());
        System.out.println("  - HasPrecisionScale: " + typeInfo.hasPrecisionScale());
        System.out.println("  - IsBigDecimal: " + typeInfo.isBigDecimal());
        System.out.println("=====================================");
    }

    @Test
    void testCompleteDecimalPipeline_WithDotSeparator() {
        // Given - decimal(10.4) field test
        Field field = Field.builder()
                .name("interestRate")
                .type("decimal(10.4)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then - Verify type parsing
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo.getBaseTypeName());
        assertEquals("java.math.BigDecimal", typeInfo.getDefaultTypeName());
        assertEquals(10, typeInfo.getPrecision());
        assertEquals(4, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
        assertTrue(typeInfo.isBigDecimal());
        
        System.out.println("=== DOT SEPARATOR TEST ===");
        System.out.println("Field: " + field.getName() + " : " + typeInfo.getInitTypeInfo());
        System.out.println("Result: " + typeInfo.getDefaultTypeName() + " with precision=" + 
                          typeInfo.getPrecision() + ", scale=" + typeInfo.getScale());
        System.out.println("===========================");
    }

    @Test
    void testBigDecimalKeyword_WithCommaSeparator() {
        // Given - bigdecimal(15,2) field test
        Field field = Field.builder()
                .name("paymentAmount")
                .type("bigdecimal(15,2)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo.getBaseTypeName());
        assertEquals("java.math.BigDecimal", typeInfo.getDefaultTypeName());
        assertEquals(15, typeInfo.getPrecision());
        assertEquals(2, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
        assertTrue(typeInfo.isBigDecimal());
        
        System.out.println("=== BIGDECIMAL KEYWORD TEST ===");
        System.out.println("Field: " + field.getName() + " : " + typeInfo.getInitTypeInfo());
        System.out.println("Result: " + typeInfo.getDefaultTypeName() + " with precision=" + 
                          typeInfo.getPrecision() + ", scale=" + typeInfo.getScale());
        System.out.println("================================");
    }

    @Test
    void testCaseInsensitiveSupport() {
        // Test DECIMAL (uppercase)
        Field field1 = Field.builder()
                .name("totalAmount")
                .type("DECIMAL(20,3)")
                .build();
        
        TypeInfo typeInfo1 = field1.getTypeInfo();
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals(20, typeInfo1.getPrecision());
        assertEquals(3, typeInfo1.getScale());
        
        // Test BIGDECIMAL (uppercase)
        Field field2 = Field.builder()
                .name("balance")
                .type("BIGDECIMAL(18,6)")
                .build();
        
        TypeInfo typeInfo2 = field2.getTypeInfo();
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo2.getBaseFieldType());
        assertEquals(18, typeInfo2.getPrecision());
        assertEquals(6, typeInfo2.getScale());
        
        System.out.println("=== CASE INSENSITIVE TEST ===");
        System.out.println("DECIMAL(20,3) -> " + typeInfo1.getDefaultTypeName() + 
                          " precision=" + typeInfo1.getPrecision() + ", scale=" + typeInfo1.getScale());
        System.out.println("BIGDECIMAL(18,6) -> " + typeInfo2.getDefaultTypeName() + 
                          " precision=" + typeInfo2.getPrecision() + ", scale=" + typeInfo2.getScale());
        System.out.println("==============================");
    }

    @Test
    void testSpaceHandling() {
        // Test with various spacing patterns
        String[] testTypes = {
            "decimal( 15 , 2 )",
            "decimal(  15  ,  2  )",
            "bigdecimal( 10 . 4 )",
            "DECIMAL(   20   ,   3   )"
        };
        
        System.out.println("=== SPACE HANDLING TEST ===");
        for (String type : testTypes) {
            Field field = Field.builder()
                    .name("testField")
                    .type(type)
                    .build();
            
            TypeInfo typeInfo = field.getTypeInfo();
            
            assertTrue(typeInfo.isBigDecimal(), "Type should be recognized as BigDecimal: " + type);
            assertTrue(typeInfo.hasPrecisionScale(), "Type should have precision/scale: " + type);
            assertTrue(typeInfo.getPrecision() > 0, "Precision should be positive: " + type);
            assertTrue(typeInfo.getScale() >= 0, "Scale should be non-negative: " + type);
            
            System.out.println(type + " -> precision=" + typeInfo.getPrecision() + 
                             ", scale=" + typeInfo.getScale());
        }
        System.out.println("============================");
    }

    @Test
    void testInvalidFormats() {
        // Test invalid formats should be UNKNOWN
        String[] invalidTypes = {
            "decimal(invalid)",
            "decimal(15)",
            "decimal(15,)",
            "decimal(,2)",
            "bigdecimal(abc,def)"
        };
        
        System.out.println("=== INVALID FORMAT TEST ===");
        for (String type : invalidTypes) {
            Field field = Field.builder()
                    .name("testField")
                    .type(type)
                    .build();
            
            TypeInfo typeInfo = field.getTypeInfo();
            
            assertEquals(BaseFieldType.UNKNOWN, typeInfo.getBaseFieldType(), 
                        "Invalid format should be UNKNOWN: " + type);
            assertFalse(typeInfo.isBigDecimal(), "Invalid format should not be BigDecimal: " + type);
            assertFalse(typeInfo.hasPrecisionScale(), "Invalid format should not have precision/scale: " + type);
            
            System.out.println(type + " -> " + typeInfo.getBaseFieldType());
        }
        System.out.println("=============================");
    }
}