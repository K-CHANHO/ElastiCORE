package io.elasticore.base.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TypeInfoTest {

    @Test
    void testBigDecimalTypeInfo_WithPrecisionAndScale() {
        // Given
        String typeInfo = "bigdecimal(15.2)";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo1.getBaseTypeName());
        assertEquals(15, typeInfo1.getPrecision());
        assertEquals(2, typeInfo1.getScale());
        assertTrue(typeInfo1.hasPrecisionScale());
        assertEquals("java.math.BigDecimal", typeInfo1.getDefaultTypeName());
    }

    @Test
    void testBigDecimalTypeInfo_CaseInsensitive() {
        // Given
        String typeInfo = "BIGDECIMAL(10.4)";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals(10, typeInfo1.getPrecision());
        assertEquals(4, typeInfo1.getScale());
    }

    @Test
    void testBigDecimalTypeInfo_WithSpaces() {
        // Given
        String typeInfo = "bigdecimal( 20 . 3 )";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(20, typeInfo1.getPrecision());
        assertEquals(3, typeInfo1.getScale());
    }

    @Test
    void testRegularBigDecimalType() {
        // Given
        String typeInfo = "BigDecimal";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo1.getBaseTypeName());
        assertEquals(-1, typeInfo1.getPrecision());
        assertEquals(-1, typeInfo1.getScale());
        assertFalse(typeInfo1.hasPrecisionScale());
    }

    @Test
    void testBigDecimalTypeInfo_WithCommaSeparator() {
        // Given
        String typeInfo = "bigdecimal(15,2)";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo1.getBaseTypeName());
        assertEquals(15, typeInfo1.getPrecision());
        assertEquals(2, typeInfo1.getScale());
        assertTrue(typeInfo1.hasPrecisionScale());
        assertEquals("java.math.BigDecimal", typeInfo1.getDefaultTypeName());
    }

    @Test
    void testDecimalTypeInfo_WithPrecisionAndScale() {
        // Given
        String typeInfo = "decimal(12,4)";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo1.getBaseTypeName());
        assertEquals(12, typeInfo1.getPrecision());
        assertEquals(4, typeInfo1.getScale());
        assertTrue(typeInfo1.hasPrecisionScale());
        assertEquals("java.math.BigDecimal", typeInfo1.getDefaultTypeName());
    }

    @Test
    void testDecimalTypeInfo_WithDotSeparator() {
        // Given
        String typeInfo = "decimal(8.3)";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo1.getBaseTypeName());
        assertEquals(8, typeInfo1.getPrecision());
        assertEquals(3, typeInfo1.getScale());
        assertTrue(typeInfo1.hasPrecisionScale());
    }

    @Test
    void testDecimalTypeInfo_CaseInsensitive() {
        // Given
        String typeInfo = "DECIMAL(25,5)";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals("BigDecimal", typeInfo1.getBaseTypeName());
        assertEquals(25, typeInfo1.getPrecision());
        assertEquals(5, typeInfo1.getScale());
        assertTrue(typeInfo1.hasPrecisionScale());
    }

    @Test
    void testBigDecimalTypeInfo_WithSpacesAndComma() {
        // Given
        String typeInfo = "bigdecimal( 18 , 6 )";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertTrue(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.BIGDECIMAL, typeInfo1.getBaseFieldType());
        assertEquals(18, typeInfo1.getPrecision());
        assertEquals(6, typeInfo1.getScale());
        assertTrue(typeInfo1.hasPrecisionScale());
    }

    @Test
    void testStringType() {
        // Given
        String typeInfo = "string";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertFalse(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.STRING, typeInfo1.getBaseFieldType());
        assertTrue(typeInfo1.isStringType());
    }

    @Test
    void testIntegerType() {
        // Given
        String typeInfo = "int";
        
        // When
        TypeInfo typeInfo1 = new TypeInfo(typeInfo);
        
        // Then
        assertFalse(typeInfo1.isBigDecimal());
        assertEquals(BaseFieldType.P_INT, typeInfo1.getBaseFieldType());
        assertFalse(typeInfo1.isStringType());
    }
}