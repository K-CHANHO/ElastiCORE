package io.elasticore.base.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BaseFieldTypeAggregateTest {

    @Test
    void testIsCountCompatible() {
        // Count compatible types
        assertTrue(BaseFieldType.P_INT.isCountCompatible());
        assertTrue(BaseFieldType.P_LONG.isCountCompatible());
        assertTrue(BaseFieldType.INTEGER.isCountCompatible());
        assertTrue(BaseFieldType.LONG.isCountCompatible());
        
        // Non-count compatible types
        assertFalse(BaseFieldType.STRING.isCountCompatible());
        assertFalse(BaseFieldType.P_DOUBLE.isCountCompatible());
        assertFalse(BaseFieldType.P_BOOLEAN.isCountCompatible());
        assertFalse(BaseFieldType.LIST.isCountCompatible());
        assertFalse(BaseFieldType.UNKNOWN.isCountCompatible());
    }

    @Test
    void testIsSumCompatible() {
        // Sum compatible numeric types
        assertTrue(BaseFieldType.P_INT.isSumCompatible());
        assertTrue(BaseFieldType.P_LONG.isSumCompatible());
        assertTrue(BaseFieldType.INTEGER.isSumCompatible());
        assertTrue(BaseFieldType.LONG.isSumCompatible());
        assertTrue(BaseFieldType.P_DOUBLE.isSumCompatible());
        assertTrue(BaseFieldType.DOUBLE.isSumCompatible());
        assertTrue(BaseFieldType.P_FLOAT.isSumCompatible());
        assertTrue(BaseFieldType.FLOAT.isSumCompatible());
        assertTrue(BaseFieldType.BIGDECIMAL.isSumCompatible());
        
        // Non-sum compatible types
        assertFalse(BaseFieldType.STRING.isSumCompatible());
        assertFalse(BaseFieldType.P_BOOLEAN.isSumCompatible());
        assertFalse(BaseFieldType.LIST.isSumCompatible());
        assertFalse(BaseFieldType.UNKNOWN.isSumCompatible());
    }

    @Test
    void testIsAvgCompatible() {
        // Avg compatible decimal types
        assertTrue(BaseFieldType.P_DOUBLE.isAvgCompatible());
        assertTrue(BaseFieldType.DOUBLE.isAvgCompatible());
        assertTrue(BaseFieldType.P_FLOAT.isAvgCompatible());
        assertTrue(BaseFieldType.FLOAT.isAvgCompatible());
        assertTrue(BaseFieldType.BIGDECIMAL.isAvgCompatible());
        
        // Non-avg compatible types (including integers)
        assertFalse(BaseFieldType.P_INT.isAvgCompatible());
        assertFalse(BaseFieldType.INTEGER.isAvgCompatible());
        assertFalse(BaseFieldType.STRING.isAvgCompatible());
        assertFalse(BaseFieldType.P_BOOLEAN.isAvgCompatible());
        assertFalse(BaseFieldType.LIST.isAvgCompatible());
    }

    @Test
    void testIsMaxMinCompatible() {
        // Most types should be MAX/MIN compatible
        assertTrue(BaseFieldType.P_INT.isMaxMinCompatible());
        assertTrue(BaseFieldType.STRING.isMaxMinCompatible());
        assertTrue(BaseFieldType.P_DOUBLE.isMaxMinCompatible());
        assertTrue(BaseFieldType.DATETIME.isMaxMinCompatible());
        assertTrue(BaseFieldType.DATE.isMaxMinCompatible());
        assertTrue(BaseFieldType.BIGDECIMAL.isMaxMinCompatible());
        
        // Non-compatible types
        assertFalse(BaseFieldType.UNKNOWN.isMaxMinCompatible());
        assertFalse(BaseFieldType.OBJECT.isMaxMinCompatible());
        assertFalse(BaseFieldType.LIST.isMaxMinCompatible());
    }

    @Test
    void testGetAggregateReturnType() {
        // COUNT functions
        assertEquals(BaseFieldType.LONG, BaseFieldType.getAggregateReturnType("count"));
        assertEquals(BaseFieldType.LONG, BaseFieldType.getAggregateReturnType("COUNT(*)"));
        assertEquals(BaseFieldType.LONG, BaseFieldType.getAggregateReturnType("select count(id) from"));
        
        // SUM functions
        assertEquals(BaseFieldType.LONG, BaseFieldType.getAggregateReturnType("sum"));
        assertEquals(BaseFieldType.LONG, BaseFieldType.getAggregateReturnType("SUM(amount)"));
        
        // AVG functions
        assertEquals(BaseFieldType.DOUBLE, BaseFieldType.getAggregateReturnType("avg"));
        assertEquals(BaseFieldType.DOUBLE, BaseFieldType.getAggregateReturnType("average"));
        assertEquals(BaseFieldType.DOUBLE, BaseFieldType.getAggregateReturnType("AVG(price)"));
        assertEquals(BaseFieldType.DOUBLE, BaseFieldType.getAggregateReturnType("AVERAGE(rating)"));
        
        // MAX/MIN functions
        assertEquals(BaseFieldType.STRING, BaseFieldType.getAggregateReturnType("max"));
        assertEquals(BaseFieldType.STRING, BaseFieldType.getAggregateReturnType("min"));
        assertEquals(BaseFieldType.STRING, BaseFieldType.getAggregateReturnType("MAX(name)"));
        assertEquals(BaseFieldType.STRING, BaseFieldType.getAggregateReturnType("MIN(created_at)"));
        
        // Unknown functions
        assertEquals(BaseFieldType.UNKNOWN, BaseFieldType.getAggregateReturnType("unknown_func"));
        assertEquals(BaseFieldType.UNKNOWN, BaseFieldType.getAggregateReturnType(""));
        assertEquals(BaseFieldType.UNKNOWN, BaseFieldType.getAggregateReturnType(null));
    }

    @Test
    void testIsPrimitiveNumeric() {
        // Primitive numeric types
        assertTrue(BaseFieldType.P_INT.isPrimitiveNumeric());
        assertTrue(BaseFieldType.P_LONG.isPrimitiveNumeric());
        assertTrue(BaseFieldType.P_DOUBLE.isPrimitiveNumeric());
        assertTrue(BaseFieldType.P_FLOAT.isPrimitiveNumeric());
        assertTrue(BaseFieldType.INTEGER.isPrimitiveNumeric());
        assertTrue(BaseFieldType.LONG.isPrimitiveNumeric());
        assertTrue(BaseFieldType.DOUBLE.isPrimitiveNumeric());
        assertTrue(BaseFieldType.FLOAT.isPrimitiveNumeric());
        
        // Non-numeric types
        assertFalse(BaseFieldType.STRING.isPrimitiveNumeric());
        assertFalse(BaseFieldType.P_BOOLEAN.isPrimitiveNumeric());
        assertFalse(BaseFieldType.BIGDECIMAL.isPrimitiveNumeric()); // Not primitive
        assertFalse(BaseFieldType.LIST.isPrimitiveNumeric());
        assertFalse(BaseFieldType.UNKNOWN.isPrimitiveNumeric());
    }

    @Test
    void testIsSingleValueCompatible() {
        // Single value compatible types
        assertTrue(BaseFieldType.P_INT.isSingleValueCompatible());
        assertTrue(BaseFieldType.STRING.isSingleValueCompatible());
        assertTrue(BaseFieldType.P_DOUBLE.isSingleValueCompatible());
        assertTrue(BaseFieldType.DATETIME.isSingleValueCompatible());
        assertTrue(BaseFieldType.BIGDECIMAL.isSingleValueCompatible());
        assertTrue(BaseFieldType.P_BOOLEAN.isSingleValueCompatible());
        
        // Non-single value compatible types
        assertFalse(BaseFieldType.LIST.isSingleValueCompatible());
        assertFalse(BaseFieldType.UNKNOWN.isSingleValueCompatible());
        assertFalse(BaseFieldType.OBJECT.isSingleValueCompatible());
    }
}