package io.elasticore.base.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance converter that transforms Java types to gRPC Protobuf types
 * JDK 8 compatible version
 *
 * @author CTO Software Architect
 */
public class JavaToProtobufTypeConverter {

    /**
     * Immutable object containing Protobuf type conversion results
     */
    public static class ProtobufTypeInfo {
        private final String type;
        private final String[] typeList;

        public ProtobufTypeInfo(String type, Set<String> additionalMessages) {
            this.type = type;
            this.typeList = additionalMessages.toArray(new String[0]);
        }

        public String getType() {
            return type;
        }

        public String[] getTypeList() {
            return typeList;
        }

        @Override
        public String toString() {
            return String.format("ProtobufTypeInfo{type='%s', typeList=%s}",
                    type, Arrays.toString(typeList));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ProtobufTypeInfo that = (ProtobufTypeInfo) obj;
            return Objects.equals(type, that.type) && Arrays.equals(typeList, that.typeList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, Arrays.hashCode(typeList));
        }
    }

    // Java primitive types -> Protobuf types mapping (JDK 8 compatible)
    private static final Map<String, String> PRIMITIVE_TYPE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("boolean", "bool");
        map.put("Boolean", "bool");
        map.put("byte", "int32");
        map.put("Byte", "int32");
        map.put("short", "int32");
        map.put("Short", "int32");
        map.put("int", "int32");
        map.put("Integer", "int32");
        map.put("long", "int64");
        map.put("Long", "int64");
        map.put("float", "float");
        map.put("Float", "float");
        map.put("double", "double");
        map.put("Double", "double");
        map.put("String", "string");
        PRIMITIVE_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    // Collection type patterns (pre-compiled for performance optimization)
    private static final Pattern GENERIC_PATTERN =
            Pattern.compile("([^<>]+)(?:<(.+)>)?");

    /**
     * Converts Java type string to Protobuf type
     *
     * @param javaTypeString Java type string (e.g., "java.util.Map<String,List<String>>")
     * @return ProtobufTypeInfo converted protobuf type information
     */
    public static ProtobufTypeInfo convertJavaTypeToProtobuf(String javaTypeString) {
        if (javaTypeString == null || javaTypeString.trim().isEmpty()) {
            throw new IllegalArgumentException("Java type string cannot be null or empty");
        }

        // Remove whitespace and normalize
        String normalizedType = javaTypeString.replaceAll("\\s+", "");

        // Set to store additional message types (removes duplicates)
        Set<String> additionalMessages = new LinkedHashSet<>();

        // Perform type conversion
        String protobufType = convertType(normalizedType, additionalMessages);

        return new ProtobufTypeInfo(protobufType, additionalMessages);
    }

    /**
     * Core method that recursively converts types
     */
    private static String convertType(String javaType, Set<String> additionalMessages) {
        return convertTypeInternal(javaType, javaType, additionalMessages);
    }

    /**
     * Internal method that keeps track of original type name for wrapper generation
     */
    private static String convertTypeInternal(String javaType, String originalType, Set<String> additionalMessages) {
        // Remove package names (e.g., java.util.Map -> Map)
        String simpleName = extractSimpleName(javaType);

        Matcher matcher = GENERIC_PATTERN.matcher(simpleName);
        if (!matcher.matches()) {
            return handlePrimitiveType(simpleName, javaType);
        }

        String baseType = matcher.group(1);
        String genericPart = matcher.group(2);

        // Handle primitive types
        if (genericPart == null) {
            return handlePrimitiveType(baseType, javaType);
        }

        return handleGenericType(baseType, genericPart, originalType, additionalMessages);
    }

    /**
     * Handle generic types
     */
    private static String handleGenericType(String baseType, String genericPart, String originalType,
                                            Set<String> additionalMessages) {
        // Normalize collection types (HashMap -> Map, ArrayList -> List, etc.)
        String normalizedBaseType = normalizeCollectionType(baseType);

        List<String> genericTypes = parseGenericTypes(genericPart);

        switch (normalizedBaseType.toLowerCase()) {
            case "map":
                return handleMapType(genericTypes, originalType, additionalMessages);

            case "list":
                return handleListType(genericTypes, originalType, additionalMessages);

            case "set":
                return handleSetType(genericTypes, originalType, additionalMessages);

            default:
                // Custom generic types
                return handleCustomGenericType(normalizedBaseType, genericTypes, additionalMessages);
        }
    }

    /**
     * Normalize collection type names
     */
    private static String normalizeCollectionType(String typeName) {
        String lowerTypeName = typeName.toLowerCase();

        // Map family
        if (lowerTypeName.equals("hashmap") || lowerTypeName.equals("linkedhashmap") ||
                lowerTypeName.equals("treemap") || lowerTypeName.equals("concurrenthashmap")) {
            return "Map";
        }

        // List family
        if (lowerTypeName.equals("arraylist") || lowerTypeName.equals("linkedlist") ||
                lowerTypeName.equals("vector")) {
            return "List";
        }

        // Set family
        if (lowerTypeName.equals("hashset") || lowerTypeName.equals("linkedhashset") ||
                lowerTypeName.equals("treeset")) {
            return "Set";
        }

        return typeName; // Return original if not a known collection type
    }

    /**
     * Handle Map types: map<key_type, value_type>
     */
    private static String handleMapType(List<String> genericTypes, String originalType, Set<String> additionalMessages) {
        if (genericTypes.size() != 2) {
            throw new IllegalArgumentException(
                    String.format("Map must have exactly 2 generic parameters, but got %d: %s",
                            genericTypes.size(), genericTypes));
        }

        String keyType = convertTypeInternal(genericTypes.get(0), genericTypes.get(0), additionalMessages);
        String valueType = convertTypeInternal(genericTypes.get(1), genericTypes.get(1), additionalMessages);

        // Create separate message for complex value types
        if (isComplexType(valueType)) {
            String wrapperMessageName = generateWrapperMessageName(genericTypes.get(1)); // Use original Java type
            additionalMessages.add(wrapperMessageName);
            return String.format("map<%s, %s>", keyType, wrapperMessageName);
        }

        return String.format("map<%s, %s>", keyType, valueType);
    }

    /**
     * Handle List types: repeated element_type
     */
    private static String handleListType(List<String> genericTypes, String originalType, Set<String> additionalMessages) {
        if (genericTypes.size() != 1) {
            throw new IllegalArgumentException(
                    String.format("List must have exactly 1 generic parameter, but got %d: %s",
                            genericTypes.size(), genericTypes));
        }

        String elementType = convertTypeInternal(genericTypes.get(0), genericTypes.get(0), additionalMessages);

        // Create separate message for complex element types
        if (isComplexType(elementType)) {
            String wrapperMessageName = generateWrapperMessageName(genericTypes.get(0)); // Use original Java type
            additionalMessages.add(wrapperMessageName);
            return String.format("repeated %s", wrapperMessageName);
        }

        return String.format("repeated %s", elementType);
    }

    /**
     * Handle Set types: repeated element_type (treated same as List in protobuf)
     */
    private static String handleSetType(List<String> genericTypes, String originalType, Set<String> additionalMessages) {
        return handleListType(genericTypes, originalType, additionalMessages);
    }

    /**
     * Handle custom generic types
     */
    private static String handleCustomGenericType(String baseType, List<String> genericTypes,
                                                  Set<String> additionalMessages) {
        // Custom types are handled as messages
        additionalMessages.add(baseType);
        return baseType;
    }

    /**
     * Handle primitive types
     * Modified section: Handles original type information, treating types with package names as "unknown"
     */
    private static String handlePrimitiveType(String typeName, String originalJavaType) {
        String protobufType = PRIMITIVE_TYPE_MAP.get(typeName);
        if (protobufType != null) {
            return protobufType;
        }

        // Core modification: Handle types with package names (containing dots) as "unknown"
        if (originalJavaType.contains(".")) {
            return "unknown";
        }

        // For unknown types without package name, preserve original case (likely custom types)
        return typeName;
    }

    /**
     * Parse generic types (supports nested generics)
     * Uses bracket balance for accurate parsing
     */
    private static List<String> parseGenericTypes(String genericPart) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < genericPart.length(); i++) {
            char c = genericPart.charAt(i);

            if (c == '<') {
                depth++;
                current.append(c);
            } else if (c == '>') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                // Only recognize commas at the top level as separators
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        // Add the last part
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    /**
     * Extract simple class name by removing package names
     */
    private static String extractSimpleName(String fullClassName) {
        int lastDotIndex = fullClassName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fullClassName.substring(lastDotIndex + 1) : fullClassName;
    }

    /**
     * Determine if type is complex
     */
    private static boolean isComplexType(String type) {
        return type.contains("repeated") || type.contains("map<") ||
                (!PRIMITIVE_TYPE_MAP.containsValue(type) && !isPrimitiveTypeName(type));
    }

    /**
     * Check if type name is a primitive type
     */
    private static boolean isPrimitiveTypeName(String type) {
        // Convert to lowercase and compare with primitive types
        String lowerType = type.toLowerCase();
        return lowerType.equals("bool") || lowerType.equals("int32") ||
                lowerType.equals("int64") || lowerType.equals("float") ||
                lowerType.equals("double") || lowerType.equals("string");
    }

    /**
     * Generate wrapper message names based on original Java type names
     */
    private static String generateWrapperMessageName(String originalJavaType) {
        // Remove package names and extract simple name
        String simpleName = extractSimpleName(originalJavaType);

        // Handle generic types
        Matcher matcher = GENERIC_PATTERN.matcher(simpleName);
        if (matcher.matches()) {
            String baseType = matcher.group(1);
            String genericPart = matcher.group(2);

            if (genericPart != null) {
                // For List<SomeType> -> SomeTypeList
                String normalizedBaseType = normalizeCollectionType(baseType);
                if (normalizedBaseType.equalsIgnoreCase("List") || normalizedBaseType.equalsIgnoreCase("Set")) {
                    List<String> genericTypes = parseGenericTypes(genericPart);
                    if (genericTypes.size() == 1) {
                        String elementTypeName = extractSimpleName(genericTypes.get(0));
                        return capitalizeFirst(elementTypeName) + "List";
                    }
                }

                // For Map<K,V> -> MapEntry (or specific name if needed)
                if (normalizedBaseType.equalsIgnoreCase("Map")) {
                    return "MapEntry";
                }
            }

            return capitalizeFirst(baseType);
        }

        // For simple types, just capitalize
        return capitalizeFirst(simpleName);
    }

    /**
     * Capitalize first letter
     */
    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

// ============== Test Methods ==============

    /**
     * Main method for testing the converter
     */
    public static void main(String[] args) {
        // Test cases
        String[] testCases = {
                "java.util.Map<String,List<String>>",
                "java.util.List<String>",
                "java.util.Map<Integer,Map<String,List<Double>>>",
                "String",
                "java.util.Set<Integer>",
                "java.util.Map<String,CustomObject>",
                "java.util.List<java.util.Map<String,Integer>>",
                "java.util.Map<String,List<com.test.TestObj>>"  // Additional test case
        };

        System.out.println("=== Java to Protobuf Type Converter Test ===\n");

        for (String testCase : testCases) {
            try {
                ProtobufTypeInfo result = convertJavaTypeToProtobuf(testCase);
                System.out.printf("Input:  %s%n", testCase);
                System.out.printf("Output: %s%n", result.getType());
                System.out.printf("Additional Messages: %s%n", Arrays.toString(result.getTypeList()));
                System.out.println("---");
            } catch (Exception e) {
                System.err.printf("Error processing %s: %s%n", testCase, e.getMessage());
            }
        }
    }
}