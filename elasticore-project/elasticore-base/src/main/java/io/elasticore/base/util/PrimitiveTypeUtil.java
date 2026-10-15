package io.elasticore.base.util;

import io.elasticore.base.model.entity.BaseFieldType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling primitive types in Port DBMS adapters.
 * Provides methods to detect primitive types and determine appropriate return types
 * for SQL queries, especially for aggregate functions and single value selects.
 */
public class PrimitiveTypeUtil {

    private static final Set<String> PRIMITIVE_TYPES = new HashSet<>();
    private static final Map<String, String> PRIMITIVE_TO_WRAPPER = new HashMap<>();
    private static final Map<String, BaseFieldType> TYPE_TO_BASE_FIELD_TYPE = new HashMap<>();
    
    static {
        // Initialize primitive types
        PRIMITIVE_TYPES.addAll(Arrays.asList(
            "int", "integer", "long", "double", "float", "boolean", "string"
        ));
        
        // Initialize primitive to wrapper mapping
        PRIMITIVE_TO_WRAPPER.put("int", "Integer");
        PRIMITIVE_TO_WRAPPER.put("integer", "Integer");
        PRIMITIVE_TO_WRAPPER.put("long", "Long");
        PRIMITIVE_TO_WRAPPER.put("double", "Double");
        PRIMITIVE_TO_WRAPPER.put("float", "Float");
        PRIMITIVE_TO_WRAPPER.put("boolean", "Boolean");
        PRIMITIVE_TO_WRAPPER.put("string", "String");
        
        // Initialize type to BaseFieldType mapping
        TYPE_TO_BASE_FIELD_TYPE.put("int", BaseFieldType.P_INT);
        TYPE_TO_BASE_FIELD_TYPE.put("integer", BaseFieldType.INTEGER);
        TYPE_TO_BASE_FIELD_TYPE.put("long", BaseFieldType.P_LONG);
        TYPE_TO_BASE_FIELD_TYPE.put("double", BaseFieldType.P_DOUBLE);
        TYPE_TO_BASE_FIELD_TYPE.put("float", BaseFieldType.P_FLOAT);
        TYPE_TO_BASE_FIELD_TYPE.put("boolean", BaseFieldType.P_BOOLEAN);
        TYPE_TO_BASE_FIELD_TYPE.put("string", BaseFieldType.STRING);
    }
    
    private static final Set<String> AGGREGATE_FUNCTIONS = new HashSet<>();
    
    static {
        AGGREGATE_FUNCTIONS.addAll(Arrays.asList(
            "count", "sum", "avg", "max", "min", "average"
        ));
    }

    /**
     * Checks if the given type is a primitive type.
     * 
     * @param type the type to check
     * @return true if it's a primitive type, false otherwise
     */
    public static boolean isPrimitiveType(String type) {
        if (type == null) {
            return false;
        }
        return PRIMITIVE_TYPES.contains(type.toLowerCase());
    }

    /**
     * Gets the wrapper type for a given primitive type.
     * 
     * @param primitiveType the primitive type
     * @return the corresponding wrapper type, or the original type if not primitive
     */
    public static String getWrapperType(String primitiveType) {
        if (primitiveType == null) {
            return null;
        }
        String wrapper = PRIMITIVE_TO_WRAPPER.get(primitiveType.toLowerCase());
        return wrapper != null ? wrapper : primitiveType;
    }

    /**
     * Gets the BaseFieldType for a given type string.
     * 
     * @param type the type string
     * @return the corresponding BaseFieldType, or null if not found
     */
    public static BaseFieldType getBaseFieldType(String type) {
        if (type == null) {
            return null;
        }
        return TYPE_TO_BASE_FIELD_TYPE.get(type.toLowerCase());
    }

    /**
     * Analyzes SQL query to determine if it returns a single primitive value.
     * This method first checks if the query selects multiple columns, and only
     * considers aggregate functions for single-column queries to avoid false positives.
     * 
     * @param sqlQuery the SQL query to analyze
     * @return true if the query likely returns a single primitive value
     */
    public static boolean isSingleValueQuery(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return false;
        }
        
        String query = sqlQuery.trim().toLowerCase();
        
        // First check if this is a multi-column query
        if (isMultiColumnQuery(query)) {
            return false;  // Multi-column queries are not single value queries
        }
        
        // For single column queries, check for aggregate functions
        if (containsAggregateFunction(query)) {
            return true;
        }
        
        // Check for single column select without table joins
        if (isSingleColumnSelect(query)) {
            return true;
        }
        
        return false;
    }

    /**
     * Determines the appropriate return type based on SQL query analysis and declared return type.
     * 
     * @param sqlQuery the SQL query
     * @param declaredReturnType the declared return type from YAML
     * @return the appropriate return type for the method
     */
    public static String getSqlReturnType(String sqlQuery, String declaredReturnType) {
        if (declaredReturnType != null && isPrimitiveType(declaredReturnType)) {
            return getWrapperType(declaredReturnType);
        }
        
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return declaredReturnType;
        }
        
        String query = sqlQuery.trim().toLowerCase();
        
        // Analyze aggregate functions to determine return type
        String aggregateType = getAggregateReturnType(query);
        if (aggregateType != null) {
            return aggregateType;
        }
        
        // If it's a single value query but no specific type detected, 
        // return the declared type or default to Object
        if (isSingleValueQuery(query)) {
            return declaredReturnType != null ? declaredReturnType : "Object";
        }
        
        return declaredReturnType;
    }

    /**
     * Checks if the SQL query contains aggregate functions.
     * 
     * @param query the SQL query (should be lowercase)
     * @return true if aggregate functions are found
     */
    private static boolean containsAggregateFunction(String query) {
        Pattern pattern = Pattern.compile("\\b(" + String.join("|", AGGREGATE_FUNCTIONS) + ")\\s*\\(");
        return pattern.matcher(query).find();
    }

    /**
     * Checks if the SQL query selects multiple columns.
     * This method looks for commas in the SELECT clause to determine multi-column queries.
     * 
     * @param query the SQL query (should be lowercase)
     * @return true if the query selects multiple columns
     */
    private static boolean isMultiColumnQuery(String query) {
        // Extract SELECT clause
        Pattern selectPattern = Pattern.compile("select\\s+(.*?)\\s+from", Pattern.CASE_INSENSITIVE);
        Matcher matcher = selectPattern.matcher(query);
        
        if (matcher.find()) {
            String selectClause = matcher.group(1).trim();

            if(selectClause.length()<2)
                return true;

            
            // Check for commas indicating multiple columns
            // Simple check: if there are commas, it's likely multi-column
            if (selectClause.contains(",")) {
                return true;
            }
            
            // Special case: SELECT * is considered multi-column
            if (selectClause.equals("*")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks if the SQL query is a single column select.
     * 
     * @param query the SQL query (should be lowercase)
     * @return true if it's a single column select
     */
    private static boolean isSingleColumnSelect(String query) {
        // Simple heuristic: SELECT followed by a single identifier (no commas)
        Pattern pattern = Pattern.compile("select\\s+([^,\\s]+)\\s+from", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String selectPart = matcher.group(1).trim();
            // Check if it's not an aggregate function and not a wildcard
            return !selectPart.equals("*") && !containsAggregateFunction("select " + selectPart);
        }
        return false;
    }

    /**
     * Determines return type based on aggregate functions in the query.
     * Only processes aggregate functions for single-value queries to avoid 
     * overriding complex return types like List<DTO> for multi-column selects.
     * 
     * @param query the SQL query (should be lowercase)
     * @return the appropriate return type for aggregate functions, or null if none detected
     */
    private static String getAggregateReturnType(String query) {
        // First check if this is a single value query
        // If not, don't process aggregate functions to preserve declared return types
        if (!isSingleValueQuery(query)) {
            return null;  // Multi-column queries should keep their declared return type
        }
        
        if (query.contains("count(")) {
            return "Long";  // COUNT always returns a number, use Long for safety
        }
        if (query.contains("sum(")) {
            return "Long";  // SUM typically returns a number
        }
        if (query.contains("avg(") || query.contains("average(")) {
            return "Double";  // AVG returns decimal
        }
        if (query.contains("max(") || query.contains("min(")) {
            // MAX/MIN return type depends on the column type
            // For now, return String as a safe default
            return "String";
        }
        return null;
    }

    /**
     * Validates if a given return type is appropriate for a SQL query.
     * 
     * @param sqlQuery the SQL query
     * @param returnType the declared return type
     * @return true if the return type is appropriate, false otherwise
     */
    public static boolean isValidReturnType(String sqlQuery, String returnType) {
        if (sqlQuery == null || returnType == null) {
            return true;  // Can't validate, assume valid
        }
        
        String query = sqlQuery.trim().toLowerCase();
        String type = returnType.toLowerCase();
        
        // COUNT should return numeric types
        if (query.contains("count(") && !isNumericType(type)) {
            return false;
        }
        
        // AVG should return decimal types
        if ((query.contains("avg(") || query.contains("average(")) && 
            !isDecimalType(type) && !isNumericType(type)) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if a type is numeric (int, long, integer).
     */
    private static boolean isNumericType(String type) {
        return type.equals("int") || type.equals("integer") || type.equals("long");
    }

    /**
     * Checks if a type is decimal (double, float).
     */
    private static boolean isDecimalType(String type) {
        return type.equals("double") || type.equals("float");
    }
}