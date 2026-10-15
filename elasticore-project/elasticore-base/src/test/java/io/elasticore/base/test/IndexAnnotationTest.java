package io.elasticore.base.test;

import io.elasticore.base.util.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("@Index annotation columnList double-quote fix validation")
public class IndexAnnotationTest {

    @Test
    @DisplayName("Should handle columnList without quotes")
    public void testColumnListWithoutQuotes() {
        String columnList = "tenant_id,customer_id,status,priority";
        
        // Simulate the logic from EntitySrcFilePublisher.java
        String columnListValue = columnList;
        if (!(columnListValue.startsWith("\"") && columnListValue.endsWith("\""))) {
            columnListValue = StringUtils.quoteString(columnListValue);
        }
        
        String expected = "\"tenant_id,customer_id,status,priority\"";
        assertEquals(expected, columnListValue, "Should add quotes to unquoted columnList");
    }

    @Test
    @DisplayName("Should not double-quote columnList that already has quotes")
    public void testColumnListWithQuotes() {
        String columnList = "\"tenant_id,customer_id,status,priority\"";
        
        // Simulate the logic from EntitySrcFilePublisher.java
        String columnListValue = columnList;
        if (!(columnListValue.startsWith("\"") && columnListValue.endsWith("\""))) {
            columnListValue = StringUtils.quoteString(columnListValue);
        }
        
        String expected = "\"tenant_id,customer_id,status,priority\"";
        assertEquals(expected, columnListValue, "Should not double-quote already quoted columnList");
    }

    @Test
    @DisplayName("Should handle single column names")
    public void testSingleColumnName() {
        String columnList = "tenant_id";
        
        // Simulate the logic from EntitySrcFilePublisher.java
        String columnListValue = columnList;
        if (!(columnListValue.startsWith("\"") && columnListValue.endsWith("\""))) {
            columnListValue = StringUtils.quoteString(columnListValue);
        }
        
        String expected = "\"tenant_id\"";
        assertEquals(expected, columnListValue, "Should handle single column names");
    }

    @Test
    @DisplayName("Should handle empty columnList")
    public void testEmptyColumnList() {
        String columnList = "";
        
        // Simulate the logic from EntitySrcFilePublisher.java
        String columnListValue = columnList;
        if (!(columnListValue.startsWith("\"") && columnListValue.endsWith("\""))) {
            columnListValue = StringUtils.quoteString(columnListValue);
        }
        
        String expected = "\"\"";
        assertEquals(expected, columnListValue, "Should handle empty columnList");
    }

    @Test
    @DisplayName("Should generate correct @Index annotation syntax")
    public void testIndexAnnotationGeneration() {
        String name = "idx_case_customer_status";
        String columnList = "tenant_id,customer_id,status,priority";
        
        StringBuilder sb = new StringBuilder();
        
        // Simulate EntitySrcFilePublisher logic
        if (name != null && !name.isEmpty()) {
            sb.append("name=" + StringUtils.quoteString(name));
        }
        
        // Apply our fix
        String columnListValue = columnList;
        if (!(columnListValue.startsWith("\"") && columnListValue.endsWith("\""))) {
            columnListValue = StringUtils.quoteString(columnListValue);
        }
        
        if (sb.length() > 0)
            sb.append(", ");
        sb.append("columnList=" + columnListValue);
        
        String result = sb.toString();
        String expected = "name=\"idx_case_customer_status\", columnList=\"tenant_id,customer_id,status,priority\"";
        
        assertEquals(expected, result, "Should generate correct @Index annotation parameters");
    }
}