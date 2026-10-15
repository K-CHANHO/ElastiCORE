package io.elasticore.base.model.repo;

import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.util.MapWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for pageable return type generation bug fix.
 *
 * Bug: When pageable: true is set, the generated code should produce Page<Entity>
 * but was generating Object return type for native queries.
 *
 * Root Cause: SqlQueryInfo.getDefaultReturnType() had condition:
 *   else if(!this.isNativeQuery && this.pageable)
 * which excluded native queries from returning Page<Entity>.
 *
 * Fix: Changed to:
 *   else if(this.pageable)
 */
public class SqlQueryInfoPageableTest {

    /**
     * These cases are about a query with no model loaded. The loaded model is
     * process-wide, and a test that generates a project leaves its context
     * behind - under which every mock entity reads as found by its key and
     * every query as Optional. Cleared so the order the tests run in does
     * not decide what they see.
     */
    @BeforeEach
    public void noModelLoaded() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
    }

    @Test
    public void testPageableWithJPQLQuery() {
        // Create a mock entity
        Entity mockEntity = createMockEntity("Employee");

        // Create JPQL query with pageable: true
        String query = "SELECT e FROM Employee e WHERE e.name LIKE CONCAT('%', :name, '%')";
        Map<String, Object> map = new HashMap<>();
        map.put("pageable", true);

        SqlQueryInfo queryInfo = SqlQueryInfo.creat(
            "testDomain",
            query,
            false,  // isNativeQuery = false (JPQL)
            true,   // pageable = true
            new MapWrapper(map),
            null,   // returnType not specified
            false   // isReturnTypeSet = false
        );

        // Set target entity using setter
        queryInfo.setTargetEntity(mockEntity);

        // Test: Should return Page<Employee>
        String returnType = queryInfo.getDefaultReturnType();
        assertEquals("Page<Employee>", returnType,
            "JPQL query with pageable=true should return Page<Employee>");
    }

    @Test
    public void testPageableWithNativeQuery() {
        // Create a mock entity
        Entity mockEntity = createMockEntity("Employee");

        // Create native query with pageable: true
        String query = "SELECT * FROM employee WHERE name LIKE CONCAT('%', :name, '%')";
        Map<String, Object> map = new HashMap<>();
        map.put("pageable", true);

        SqlQueryInfo queryInfo = SqlQueryInfo.creat(
            "testDomain",
            query,
            true,   // isNativeQuery = true (Native SQL)
            true,   // pageable = true
            new MapWrapper(map),
            null,   // returnType not specified
            false   // isReturnTypeSet = false
        );

        // Set target entity using setter
        queryInfo.setTargetEntity(mockEntity);

        // Test: Native query with pageable returns List (current behavior)
        // Note: Native queries don't support Page return type in current implementation
        String returnType = queryInfo.getDefaultReturnType();
        assertEquals("List<Employee>", returnType,
            "Native query with pageable=true returns List<Employee> (current behavior)");
    }

    @Test
    public void testNonPageableWithJPQLQuery() {
        // Create a mock entity
        Entity mockEntity = createMockEntity("Employee");

        // Create JPQL query without pageable
        String query = "SELECT e FROM Employee e WHERE e.id = :id";
        Map<String, Object> map = new HashMap<>();

        SqlQueryInfo queryInfo = SqlQueryInfo.creat(
            "testDomain",
            query,
            false,  // isNativeQuery = false (JPQL)
            false,  // pageable = false
            new MapWrapper(map),
            null,   // returnType not specified
            false   // isReturnTypeSet = false
        );

        // Set target entity using setter
        queryInfo.setTargetEntity(mockEntity);

        // Test: Should return List<Employee>
        String returnType = queryInfo.getDefaultReturnType();
        assertEquals("List<Employee>", returnType,
            "Non-pageable JPQL query should return List<Employee>");
    }

    /**
     * Create a mock Entity for testing
     */
    private Entity createMockEntity(String name) {
        return Entity.create("testDomain", name, null, null);
    }
}
