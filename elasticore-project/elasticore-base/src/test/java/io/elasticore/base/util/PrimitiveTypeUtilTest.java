package io.elasticore.base.util;

import io.elasticore.base.model.entity.BaseFieldType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PrimitiveTypeUtilTest {

    @Test
    void testIsPrimitiveType() {
        // Primitive types
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("int"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("integer"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("long"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("double"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("float"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("boolean"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("string"));
        
        // Case insensitive
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("INT"));
        assertTrue(PrimitiveTypeUtil.isPrimitiveType("String"));
        
        // Non-primitive types
        assertFalse(PrimitiveTypeUtil.isPrimitiveType("List"));
        assertFalse(PrimitiveTypeUtil.isPrimitiveType("Object"));
        assertFalse(PrimitiveTypeUtil.isPrimitiveType("CustomClass"));
        assertFalse(PrimitiveTypeUtil.isPrimitiveType(null));
    }

    @Test
    void testGetWrapperType() {
        assertEquals("Integer", PrimitiveTypeUtil.getWrapperType("int"));
        assertEquals("Integer", PrimitiveTypeUtil.getWrapperType("integer"));
        assertEquals("Long", PrimitiveTypeUtil.getWrapperType("long"));
        assertEquals("Double", PrimitiveTypeUtil.getWrapperType("double"));
        assertEquals("Float", PrimitiveTypeUtil.getWrapperType("float"));
        assertEquals("Boolean", PrimitiveTypeUtil.getWrapperType("boolean"));
        assertEquals("String", PrimitiveTypeUtil.getWrapperType("string"));
        
        // Non-primitive should return original
        assertEquals("CustomClass", PrimitiveTypeUtil.getWrapperType("CustomClass"));
        assertNull(PrimitiveTypeUtil.getWrapperType(null));
    }

    @Test
    void testGetBaseFieldType() {
        assertEquals(BaseFieldType.P_INT, PrimitiveTypeUtil.getBaseFieldType("int"));
        assertEquals(BaseFieldType.INTEGER, PrimitiveTypeUtil.getBaseFieldType("integer"));
        assertEquals(BaseFieldType.P_LONG, PrimitiveTypeUtil.getBaseFieldType("long"));
        assertEquals(BaseFieldType.P_DOUBLE, PrimitiveTypeUtil.getBaseFieldType("double"));
        assertEquals(BaseFieldType.P_FLOAT, PrimitiveTypeUtil.getBaseFieldType("float"));
        assertEquals(BaseFieldType.P_BOOLEAN, PrimitiveTypeUtil.getBaseFieldType("boolean"));
        assertEquals(BaseFieldType.STRING, PrimitiveTypeUtil.getBaseFieldType("string"));
        
        assertNull(PrimitiveTypeUtil.getBaseFieldType("CustomClass"));
        assertNull(PrimitiveTypeUtil.getBaseFieldType(null));
    }

    @Test
    void testIsSingleValueQuery() {
        // Aggregate functions
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT COUNT(*) FROM users"));
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT MAX(price) FROM products"));
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT MIN(age) FROM users"));
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT SUM(amount) FROM orders"));
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT AVG(rating) FROM reviews"));
        
        // Case insensitive
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("select count(*) from users"));
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT Count(*) FROM users"));
        
        // Single column select
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("SELECT name FROM users WHERE id = 1"));
        assertTrue(PrimitiveTypeUtil.isSingleValueQuery("select email from users where active = true"));
        
        // Multi-column selects should be false
        assertFalse(PrimitiveTypeUtil.isSingleValueQuery("SELECT id, name FROM users"));
        assertFalse(PrimitiveTypeUtil.isSingleValueQuery("SELECT * FROM users"));
        
        // Empty/null queries
        assertFalse(PrimitiveTypeUtil.isSingleValueQuery(null));
        assertFalse(PrimitiveTypeUtil.isSingleValueQuery(""));
        assertFalse(PrimitiveTypeUtil.isSingleValueQuery("   "));
    }

    @Test
    void testGetSqlReturnType() {
        // COUNT queries
        assertEquals("Long", PrimitiveTypeUtil.getSqlReturnType("SELECT COUNT(*) FROM users", null));
        assertEquals("Integer", PrimitiveTypeUtil.getSqlReturnType("SELECT count(*) FROM users", "int"));
        
        // SUM queries
        assertEquals("Long", PrimitiveTypeUtil.getSqlReturnType("SELECT SUM(amount) FROM orders", null));
        
        // AVG queries
        assertEquals("Double", PrimitiveTypeUtil.getSqlReturnType("SELECT AVG(rating) FROM reviews", null));
        assertEquals("Double", PrimitiveTypeUtil.getSqlReturnType("select average(price) from products", null));
        
        // MAX/MIN queries
        assertEquals("String", PrimitiveTypeUtil.getSqlReturnType("SELECT MAX(name) FROM users", null));
        assertEquals("String", PrimitiveTypeUtil.getSqlReturnType("SELECT MIN(created_at) FROM orders", null));
        
        // Declared primitive types should be converted to wrappers
        assertEquals("Integer", PrimitiveTypeUtil.getSqlReturnType("SELECT COUNT(*) FROM users", "int"));
        assertEquals("Long", PrimitiveTypeUtil.getSqlReturnType("SELECT MAX(price) FROM products", "long"));
        assertEquals("String", PrimitiveTypeUtil.getSqlReturnType("SELECT name FROM users WHERE id = 1", "string"));
        
        // Non-aggregate queries with declared type
        assertEquals("CustomType", PrimitiveTypeUtil.getSqlReturnType("SELECT id, name FROM users", "CustomType"));
        
        // Null/empty cases
        assertNull(PrimitiveTypeUtil.getSqlReturnType(null, null));
        assertEquals("String", PrimitiveTypeUtil.getSqlReturnType("", "String"));
    }

    @Test
    void testIsValidReturnType() {
        // COUNT should work with numeric types
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT COUNT(*) FROM users", "int"));
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT COUNT(*) FROM users", "long"));
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT COUNT(*) FROM users", "Integer"));
        
        // COUNT with non-numeric should be invalid
        assertFalse(PrimitiveTypeUtil.isValidReturnType("SELECT COUNT(*) FROM users", "string"));
        assertFalse(PrimitiveTypeUtil.isValidReturnType("SELECT COUNT(*) FROM users", "boolean"));
        
        // AVG should work with decimal types
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT AVG(price) FROM products", "double"));
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT AVG(price) FROM products", "float"));
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT average(rating) FROM reviews", "double"));
        
        // AVG with integer should be valid (can be converted)
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT AVG(price) FROM products", "int"));
        
        // AVG with non-numeric should be invalid
        assertFalse(PrimitiveTypeUtil.isValidReturnType("SELECT AVG(price) FROM products", "string"));
        
        // Null cases should be valid (can't validate)
        assertTrue(PrimitiveTypeUtil.isValidReturnType(null, "int"));
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT COUNT(*) FROM users", null));
        assertTrue(PrimitiveTypeUtil.isValidReturnType(null, null));
        
        // Non-aggregate queries should be valid
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT name FROM users", "string"));
        assertTrue(PrimitiveTypeUtil.isValidReturnType("SELECT id, name FROM users", "CustomType"));
    }
}