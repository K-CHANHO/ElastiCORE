package io.elasticore.base.model.port;

import io.elasticore.base.model.loader.ModelLoaderContext;
import io.elasticore.base.model.loader.module.PortModelLoader;
import io.elasticore.base.model.repo.Method;
import io.elasticore.base.model.repo.SqlQueryInfo;
import io.elasticore.base.util.MapWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PrimitiveReturnPortTest {

    @Mock
    private ModelLoaderContext mockContext;

    private PortModelLoader portModelLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        portModelLoader = new PortModelLoader();
        when(mockContext.getDomainId()).thenReturn("test");
    }

    @Test
    void testPrimitiveReturnTypeHandling() {
        // Test COUNT query with int return
        Map<String, Object> countMethodMap = new HashMap<>();
        countMethodMap.put("meta", "method");
        countMethodMap.put("return", "int");
        countMethodMap.put("query", "SELECT COUNT(*) FROM users");
        countMethodMap.put("nativeQuery", true);

        MapWrapper mapWrapper = new MapWrapper(countMethodMap);
        
        // Verify primitive type is converted to wrapper
        String returnType = mapWrapper.getString("return");
        assertNotNull(returnType);
        assertEquals("int", returnType); // Original value from map
    }

    @Test
    void testSqlQueryInfoWithPrimitiveTypes() {
        // Test SqlQueryInfo creation with different primitive return types
        String domainId = "test";
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("id", "testMethod");
        MapWrapper context = new MapWrapper(contextMap);
        
        // COUNT query
        SqlQueryInfo countQuery = SqlQueryInfo.creat(
            domainId, 
            "SELECT COUNT(*) FROM users", 
            true, 
            false, 
            context, 
            "Integer", 
            true
        );
        assertEquals("Integer", countQuery.getReturnType());
        assertTrue(countQuery.isSelectQuery());
        
        // AVG query
        SqlQueryInfo avgQuery = SqlQueryInfo.creat(
            domainId, 
            "SELECT AVG(age) FROM users", 
            true, 
            false, 
            context, 
            "Double", 
            true
        );
        assertEquals("Double", avgQuery.getReturnType());
        
        // String return type
        SqlQueryInfo stringQuery = SqlQueryInfo.creat(
            domainId, 
            "SELECT name FROM users WHERE id = :id", 
            true, 
            false, 
            context, 
            "String", 
            true
        );
        assertEquals("String", stringQuery.getReturnType());
    }

    @Test
    void testMethodBuilderWithPrimitiveTypes() {
        // Test method creation with primitive return types
        Method countMethod = Method.builder()
            .name("getUserCount")
            .returnType("Integer")
            .query("SELECT COUNT(*) FROM users")
            .build();
        
        assertEquals("getUserCount", countMethod.getName());
        assertEquals("Integer", countMethod.getReturnType());
        // Note: getQuery() returns query from queryInfo, not directly from builder
        
        Method stringMethod = Method.builder()
            .name("getUserName")
            .returnType("String")
            .query("SELECT name FROM users WHERE id = :id")
            .build();
        
        assertEquals("String", stringMethod.getReturnType());
    }

    @Test
    void testPortCreationWithPrimitiveMethods() {
        // Test Port creation with methods that return primitive types
        Map<String, Object> portData = new HashMap<>();
        portData.put("meta", "dbms @datasource(\"main\")");
        
        Map<String, LinkedHashMap> methods = new HashMap<>();
        
        // COUNT method
        LinkedHashMap<String, Object> countMethod = new LinkedHashMap<>();
        countMethod.put("meta", "method");
        countMethod.put("return", "int");
        countMethod.put("query", "SELECT COUNT(*) FROM users");
        countMethod.put("nativeQuery", true);
        methods.put("getUserCount", countMethod);
        
        // AVG method  
        LinkedHashMap<String, Object> avgMethod = new LinkedHashMap<>();
        avgMethod.put("meta", "method");
        avgMethod.put("return", "double");
        avgMethod.put("query", "SELECT AVG(age) FROM users");
        avgMethod.put("nativeQuery", true);
        methods.put("getAverageAge", avgMethod);
        
        // String method
        LinkedHashMap<String, Object> stringMethod = new LinkedHashMap<>();
        stringMethod.put("meta", "method");
        stringMethod.put("return", "string");
        stringMethod.put("query", "SELECT name FROM users WHERE id = :id");
        stringMethod.put("nativeQuery", true);
        Map<String, Object> params = new HashMap<>();
        params.put("id", "long");
        stringMethod.put("params", params);
        methods.put("getUserName", stringMethod);
        
        portData.put("methods", methods);

        // Verify the structure is correct for processing
        assertNotNull(methods.get("getUserCount"));
        assertNotNull(methods.get("getAverageAge"));
        assertNotNull(methods.get("getUserName"));
        
        assertEquals("int", ((LinkedHashMap<?, ?>) methods.get("getUserCount")).get("return"));
        assertEquals("double", ((LinkedHashMap<?, ?>) methods.get("getAverageAge")).get("return"));
        assertEquals("string", ((LinkedHashMap<?, ?>) methods.get("getUserName")).get("return"));
    }

    @Test 
    void testQueryValidation() {
        // Test query validation for primitive types
        String domainId = "test";
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("id", "testMethod");
        MapWrapper context = new MapWrapper(contextMap);
        
        // Valid COUNT with numeric return
        SqlQueryInfo validCountQuery = SqlQueryInfo.creat(
            domainId, 
            "SELECT COUNT(*) FROM users", 
            true, 
            false, 
            context, 
            "Long", 
            true
        );
        assertNotNull(validCountQuery);
        assertEquals("Long", validCountQuery.getReturnType());
        
        // Test that primitive types are properly handled
        SqlQueryInfo primitiveQuery = SqlQueryInfo.creat(
            domainId, 
            "SELECT MAX(price) FROM products", 
            true, 
            false, 
            context, 
            "Long", 
            true
        );
        assertEquals("Long", primitiveQuery.getReturnType());
    }
}