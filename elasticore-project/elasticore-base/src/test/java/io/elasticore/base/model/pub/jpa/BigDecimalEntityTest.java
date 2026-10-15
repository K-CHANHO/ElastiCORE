package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.model.entity.BaseFieldType;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.entity.TypeInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BigDecimalEntityTest {

    @Test
    void testFieldWithBigDecimalPrecisionScale() {
        // Given - bigdecimal(15.2) field test
        Field field = Field.builder()
                .name("price")
                .type("bigdecimal(15.2)")
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
        assertTrue(typeInfo.isBaseType());
        assertFalse(typeInfo.isStringType());
        assertFalse(typeInfo.isList());
    }

    @Test
    void testFieldWithBigDecimalLargePrecision() {
        // Given - BigDecimal with large precision test
        Field field = Field.builder()
                .name("amount")
                .type("bigdecimal(28.8)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        assertEquals(28, typeInfo.getPrecision());
        assertEquals(8, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
    }

    @Test
    void testFieldWithBigDecimalZeroScale() {
        // Given - BigDecimal with zero scale test
        Field field = Field.builder()
                .name("count")
                .type("bigdecimal(10.0)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        assertEquals(10, typeInfo.getPrecision());
        assertEquals(0, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
    }

    @Test
    void testRegularBigDecimalField() {
        // Given - Regular BigDecimal type (no precision/scale)
        Field field = Field.builder()
                .name("value")
                .type("BigDecimal")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals(-1, typeInfo.getPrecision());
        assertEquals(-1, typeInfo.getScale());
        assertFalse(typeInfo.hasPrecisionScale());
        assertTrue(typeInfo.isBigDecimal());
    }

    @Test
    void testInvalidBigDecimalFormat() {
        // Given - Invalid format BigDecimal
        Field field = Field.builder()
                .name("invalid")
                .type("bigdecimal(invalid)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        // Invalid format should be processed as UNKNOWN type
        assertEquals(BaseFieldType.UNKNOWN, typeInfo.getBaseFieldType());
        assertFalse(typeInfo.isBigDecimal());
        assertFalse(typeInfo.hasPrecisionScale());
    }

    @Test
    void testDecimalKeywordWithPrecisionScale() {
        // Given - decimal(15,2) field test (with comma separator)
        Field field = Field.builder()
                .name("overdueAmount")
                .type("decimal(15,2)")
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
        assertTrue(typeInfo.isBaseType());
        assertFalse(typeInfo.isStringType());
        assertFalse(typeInfo.isList());
    }

    @Test
    void testDecimalKeywordWithDotSeparator() {
        // Given - decimal(10.4) field test (with dot separator)
        Field field = Field.builder()
                .name("interestRate")
                .type("decimal(10.4)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo.getBaseTypeName());
        assertEquals("java.math.BigDecimal", typeInfo.getDefaultTypeName());
        assertEquals(10, typeInfo.getPrecision());
        assertEquals(4, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
        assertTrue(typeInfo.isBigDecimal());
    }

    @Test
    void testDecimalKeywordCaseInsensitive() {
        // Given - DECIMAL(uppercase) field test
        Field field = Field.builder()
                .name("totalAmount")
                .type("DECIMAL(20,3)")
                .build();
        
        // When
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Then
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo.getBaseTypeName());
        assertEquals(20, typeInfo.getPrecision());
        assertEquals(3, typeInfo.getScale());
        assertTrue(typeInfo.hasPrecisionScale());
        assertTrue(typeInfo.isBigDecimal());
    }

    @Test
    void testBigDecimalKeywordWithCommaSeparator() {
        // Given - bigdecimal(15,2) field test (with comma separator)
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
    }

    @Test
    void testDecimalTypeDebug() {
        // Debug test for decimal(15,2) type processing
        Field field = Field.builder()
                .name("overdueAmount")
                .type("decimal(15,2)")
                .build();
        
        TypeInfo typeInfo = field.getTypeInfo();
        
        // Debug output
        System.out.println("=== DECIMAL DEBUG INFO ===");
        System.out.println("Original type string: " + typeInfo.getInitTypeInfo());
        System.out.println("Base field type: " + typeInfo.getBaseFieldType());
        System.out.println("Base type name: " + typeInfo.getBaseTypeName());
        System.out.println("Default type name: " + typeInfo.getDefaultTypeName());
        System.out.println("Is BigDecimal: " + typeInfo.isBigDecimal());
        System.out.println("Is Base Type: " + typeInfo.isBaseType());
        System.out.println("Has precision/scale: " + typeInfo.hasPrecisionScale());
        System.out.println("Precision: " + typeInfo.getPrecision());
        System.out.println("Scale: " + typeInfo.getScale());
        System.out.println("=========================");
        
        // Assertions
        assertTrue(typeInfo.isBigDecimal(), "decimal(15,2) should be recognized as BigDecimal");
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo.getBaseFieldType());
        assertEquals("java.math.BigDecimal", typeInfo.getDefaultTypeName());
    }
}