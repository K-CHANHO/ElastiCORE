package io.elasticore.springboot3.mapper;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for object field mapping with support for gRPC-style objects.
 * Supports reflection-based field copying, List mapping, nested object mapping,
 * and gRPC Count/Get pattern recognition without protobuf dependencies.
 */
class MappingUtils {

    /**
     * Converts a list of source objects to a list of target objects.
     *
     * @param originList source object list
     * @param targetClass target class type
     * @param <T> target type
     * @return converted list of target objects
     */
    public static <T> List<T> toList(List<?> originList, Class<T> targetClass) {
        return toList(originList, targetClass, null);
    }

    /**
     * Converts a list of source objects to a list of target objects with field mapping.
     *
     * @param originList source object list
     * @param targetClass target class type
     * @param fieldNames field names to map
     * @param <T> target type
     * @return converted list of target objects
     */
    public static <T> List<T> toList(List<?> originList, Class<T> targetClass, List<String> fieldNames){
        return toList(originList, targetClass, fieldNames, null);
    }

    /**
     * Converts a list of source objects to a list of target objects with field mapping.
     *
     * @param originList source object list
     * @param targetClass target class type
     * @param fieldNames source field names to map
     * @param toFieldNames target field names to map to
     * @param <T> target type
     * @return converted list of target objects
     */
    public static <T> List<T> toList(List<?> originList, Class<T> targetClass, List<String> fieldNames , List<String> toFieldNames) {
        if (originList == null || targetClass == null ) {
            throw new IllegalArgumentException("originList, targetClass  must not be null");
        }

        List<T> result = new ArrayList<>(originList.size());

        for (Object source : originList) {
            try {
                T target = targetClass.getDeclaredConstructor().newInstance();
                copy(source, target, fieldNames , toFieldNames);
                result.add(target);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Failed to create instance of " + targetClass.getName(), e);
            }
        }

        return result;
    }

    /**
     * Copies fields from source object to target object.
     *
     * @param from source object
     * @param to target object
     * @param fieldNames field names to copy
     */
    public static void copy(Object from, Object to, List<String> fieldNames) {
        copy(from, to, fieldNames, null);
    }

    /**
     * Extracts field names from the given object.
     *
     * @param obj object to extract field names from
     * @return list of field names
     */
    private static List<String> extractFieldNames(Object obj) {
        return extractFieldNames(obj, null);
    }

    /**
     * Extracts field names from the given object using specified method type.
     *
     * @param obj object to extract field names from
     * @param methodType method type filter ("get", "set", or null for fields)
     * @return list of field names
     */
    public static List<String> extractFieldNames(Object obj, String methodType) {
        Set<String> result = new LinkedHashSet<>();

        if (obj instanceof Map) {
            result.addAll(((Map<?, ?>) obj).keySet().stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet()));
        } else {
            Class<?> clazz = obj.getClass();

            if ("get".equalsIgnoreCase(methodType) || "set".equalsIgnoreCase(methodType)) {
                for (Method method : clazz.getMethods()) {
                    String name = method.getName();
                    if (method.getParameterCount() == 0 && name.startsWith("get") && "get".equalsIgnoreCase(methodType)) {
                        result.add(decapitalize(name.substring(3)));
                    } else if (method.getParameterCount() == 1 && name.startsWith("set") && "set".equalsIgnoreCase(methodType)) {
                        result.add(decapitalize(name.substring(3)));
                    }
                }
            } else {
                // Extract only accessible fields for safety
                for (Field field : clazz.getDeclaredFields()) {
                    if (isAccessibleField(field)) {
                        result.add(field.getName());
                    }
                }
            }
        }

        return new ArrayList<>(result);
    }


    static String getSuperclassName(Object obj) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        Class<?> superClass = clazz.getSuperclass();
        return (superClass != null) ? superClass.getName() : null;
    }

    static boolean isProtobufObject(Object obj) {
        String superclassName = getSuperclassName(obj);
        if(superclassName==null) return false;
        if(superclassName.indexOf("com.google.protobuf.GeneratedMessage")==0) return true;
        return false;
    }

    /**
     * Extracts field name mapping from the given object using specified method type.
     *
     * @param obj object to extract field mapping from
     * @param methodType method type filter ("get", "set", or null for fields)
     * @return map of field names to field/method objects
     */
    public static Map<String, Object> extractFieldNameMap(Object obj, String methodType) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (obj instanceof Map) {
            ((Map<?, ?>) obj).keySet().forEach(k -> result.put(k.toString(), k.toString()));
        } else {
            Class<?> clazz = obj.getClass();

            // Extract only accessible fields for safety
            for (Field field : clazz.getDeclaredFields()) {
                if (isAccessibleField(field)) {
                    result.putIfAbsent(field.getName(), field);
                }
            }

            if ("get".equalsIgnoreCase(methodType) || "set".equalsIgnoreCase(methodType)) {

                boolean isProtobufCheck = false;
                if("get".equalsIgnoreCase(methodType) && isProtobufObject(obj)) {
                    isProtobufCheck = true;
                }

                for (Method method : clazz.getMethods()) {
                    String name = method.getName();
                    String fieldName = null;
                    if (method.getParameterCount() == 0 && name.startsWith("get") && "get".equalsIgnoreCase(methodType)) {
                        fieldName = decapitalize(name.substring(3));
                        //result.put(fieldName, method);
                    } else if (method.getParameterCount() == 1 && name.startsWith("set") && "set".equalsIgnoreCase(methodType)) {
                        fieldName = decapitalize(name.substring(3));
                        //result.put(fieldName, method);
                    }

                    if(fieldName!=null) {
                        result.put(fieldName, method);
                        if(isProtobufCheck) {
                            int p = fieldName.lastIndexOf("List");
                            if(p>0) {
                                String fieldName2 = fieldName.substring(0,p);
                                result.put(fieldName2 ,method);
                            }
                        }
                    }
                }


                // Check for gRPC-style setter patterns (addXxx, addAllXxx)
                if ("set".equalsIgnoreCase(methodType)) {
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (name.startsWith("addAll") && method.getParameterCount() == 1) {
                            String fieldName = decapitalize(name.substring(6)); // Remove "addAll"
                            Class<?> paramType = method.getParameterTypes()[0];
                            if (Iterable.class.isAssignableFrom(paramType)) {
                                // Prioritize addAll methods over regular setters
                                result.put(fieldName, new GrpcListSetter(method, GrpcListSetterType.ADD_ALL));
                            }
                        } else if (name.startsWith("add") && method.getParameterCount() == 1 && !name.startsWith("addAll")) {
                            String fieldName = decapitalize(name.substring(3)); // Remove "add"
                            // Only add if no addAll method exists for this field
                            if (!result.containsKey(fieldName)) {
                                result.put(fieldName, new GrpcListSetter(method, GrpcListSetterType.ADD_SINGLE));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Enum for gRPC list setter types.
     */
    enum GrpcListSetterType {
        ADD_SINGLE,  // addXxx(item)
        ADD_ALL      // addAllXxx(Iterable)
    }

    /**
     * Wrapper class for gRPC-style list setter methods.
     */
    static class GrpcListSetter {
        private final Method method;
        private final GrpcListSetterType type;

        public GrpcListSetter(Method method, GrpcListSetterType type) {
            this.method = method;
            this.type = type;
        }

        public Method getMethod() {
            return method;
        }

        public GrpcListSetterType getType() {
            return type;
        }
    }

    /**
     * Checks if the object has gRPC-style list pattern (Count/Get methods).
     * This method detects gRPC generated objects without protobuf dependencies
     * by looking for getXxxCount() and getXxx(int) method patterns.
     *
     * @param obj object to check
     * @param fieldName field name to check for gRPC pattern
     * @return true if gRPC list pattern exists
     */
    private static boolean hasGrpcListPattern(Object obj, String fieldName) {
        if (obj == null || fieldName == null) return false;

        try {
            Class<?> clazz = obj.getClass();
            String countMethodName = "get" + capitalize(fieldName) + "Count";
            String getMethodName = "get" + capitalize(fieldName);

            Method countMethod = clazz.getMethod(countMethodName);
            Method getMethod = clazz.getMethod(getMethodName, int.class);

            // Both methods must exist and countMethod should return int
            return countMethod.getReturnType() == int.class || countMethod.getReturnType() == Integer.class;

        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Extracts list data from gRPC-style object using Count/Get pattern.
     *
     * @param obj gRPC-style object
     * @param fieldName field name to extract
     * @return list of objects extracted from gRPC pattern
     */
    private static List<Object> extractGrpcStyleList(Object obj, String fieldName) {
        try {
            Class<?> clazz = obj.getClass();
            String countMethodName = "get" + capitalize(fieldName) + "Count";
            String getMethodName = "get" + capitalize(fieldName);

            Method countMethod = clazz.getMethod(countMethodName);
            Method getMethod = clazz.getMethod(getMethodName, int.class);

            int count = (Integer) countMethod.invoke(obj);
            List<Object> result = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                Object item = getMethod.invoke(obj, i);
                result.add(item);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Failed to extract gRPC style list for field: " + fieldName + ", error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a field can be safely accessed.
     * Filters out Java internal fields and synthetic fields for security.
     *
     * @param field field to check
     * @return true if field is accessible
     */
    private static boolean isAccessibleField(Field field) {
        // Skip Java internal class special fields
        if (field.getName().equals("serialVersionUID")) {
            return false;
        }

        // Skip synthetic fields (compiler-generated fields)
        if (field.isSynthetic()) {
            return false;
        }

        // Handle Java internal package classes carefully
        String className = field.getDeclaringClass().getName();
        if (className.startsWith("java.") || className.startsWith("javax.") ||
                className.startsWith("sun.") || className.startsWith("com.sun.")) {
            return false;
        }

        return true;
    }

    /**
     * Safely attempts to make a field accessible.
     * Handles Java 9+ module system restrictions gracefully.
     *
     * @param field field to make accessible
     * @return true if field access was successfully enabled
     */
    private static boolean trySetAccessible(Field field) {
        try {
            field.setAccessible(true);
            return true;
        } catch (InaccessibleObjectException | SecurityException e) {
            // Java 9+ module system or security constraints prevent access
            return false;
        }
    }

    /**
     * Converts first character of string to lowercase following JavaBean conventions.
     *
     * @param name string to decapitalize
     * @return decapitalized string
     */
    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Converts first character of string to uppercase.
     *
     * @param name string to capitalize
     * @return capitalized string
     */
    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Copies fields from source to target with field name mapping.
     *
     * @param from source object
     * @param to target object
     * @param fieldNames source field names
     * @param toFieldNames target field names
     */
    public static void copy(Object from, Object to, List<String> fieldNames, List<String> toFieldNames) {
        boolean isIncludeUndefined = false;
        if(fieldNames==null)
            isIncludeUndefined = true;
        copy(from ,to,fieldNames, toFieldNames,isIncludeUndefined);
    }

    /**
     * Copies fields from source to target with undefined field inclusion option.
     *
     * @param from source object
     * @param to target object
     * @param fieldNames source field names
     * @param toFieldNames target field names
     * @param isIncludeUndefined whether to include undefined fields
     */
    public static void copy(Object from, Object to, List<String> fieldNames, List<String> toFieldNames, boolean isIncludeUndefined) {
        copy(from ,to,fieldNames, toFieldNames,isIncludeUndefined , null);
    }

    /**
     * Copies fields from source to target with mapping guards.
     *
     * @param from source object
     * @param to target object
     * @param fieldNames source field names
     * @param toFieldNames target field names
     * @param isIncludeUndefined whether to include undefined fields
     * @param guardMap mapping guards for field-level control
     */
    public static void copy(Object from, Object to, List<String> fieldNames, List<String> toFieldNames, boolean isIncludeUndefined, Map<String,MappingGuard> guardMap) {
        copy(from, to, fieldNames, toFieldNames, isIncludeUndefined, guardMap, false); // default skipNull = false
    }

    /**
     * Main copy method with full feature support including skipNull option.
     * Supports gRPC-style objects, nested mapping, List conversion, and null handling.
     *
     * @param from source object
     * @param to target object
     * @param fieldNames source field names
     * @param toFieldNames target field names
     * @param isIncludeUndefined whether to include undefined fields
     * @param guardMap mapping guards for field-level control
     * @param skipNull whether to skip null values during mapping
     */
    public static void copy(Object from, Object to, List<String> fieldNames, List<String> toFieldNames, boolean isIncludeUndefined, Map<String,MappingGuard> guardMap, boolean skipNull) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Source and target must not be null");
        }

        Map<String, String> definedMappings = new LinkedHashMap<>();
        if (fieldNames != null ) {
            if(toFieldNames == null)
                toFieldNames=fieldNames;

            for (int i = 0; i < Math.min(fieldNames.size(), toFieldNames.size()); i++) {
                definedMappings.put(fieldNames.get(i), toFieldNames.get(i));
            }
        }

        Set<String> allTargetFieldNames = new LinkedHashSet<>(definedMappings.values());

        Map<String, Object> fromFieldInfoMap = extractFieldNameMap(from, "get");
        Map<String, Object> toFieldInfoMap = extractFieldNameMap(to, "set");

        if (isIncludeUndefined) {
            if(to instanceof Map) {
                for(String keyNm : fromFieldInfoMap.keySet()) {
                    if(!definedMappings.containsKey(keyNm)) {
                        definedMappings.put(keyNm, keyNm);
                        allTargetFieldNames.add(keyNm);
                    }
                }
            } else {
                for (String name : toFieldInfoMap.keySet()) {
                    if (!allTargetFieldNames.contains(name) && fromFieldInfoMap.containsKey(name)) {
                        definedMappings.put(name, name);
                        allTargetFieldNames.add(name);
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : definedMappings.entrySet()) {
            String fromName = entry.getKey();
            String toName = entry.getValue();

            MappingGuard mappingGuard = null;
            MappingContext ctx = null;
            if(guardMap!=null) {
                mappingGuard = guardMap.get(fromName);
                ctx = new MappingContext(1, from,to, fromName, mappingGuard);

                if(!ctx.checkEnable())
                    continue;
            }

            try {
                Object value = null;

                if (from instanceof Map) {
                    if (!((Map<?, ?>) from).containsKey(fromName)) continue;
                    value = ((Map<?, ?>) from).get(fromName);
                } else {
                    Object refType = fromFieldInfoMap.get(fromName);

                    if(refType==null) {
                        // Check for gRPC-style object Count/Get pattern
                        if (hasGrpcListPattern(from, fromName)) {
                            List<Object> grpcStyleList = extractGrpcStyleList(from, fromName);
                            if (grpcStyleList != null) {
                                value = grpcStyleList;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if(refType instanceof Field) {
                        Field sourceField = (Field) refType;
                        if (!trySetAccessible(sourceField)) {
                            continue;
                        }
                        value = sourceField.get(from);
                    } else if(refType instanceof Method) {
                        Method getter = (Method) refType;
                        value = getter.invoke(from);
                    }
                }

                // Skip null values if skipNull is enabled
                if (value == null && skipNull) {
                    continue; // Skip this field if value is null and skipNull is true
                }

                if (to instanceof Map) {
                    ((Map<String, Object>) to).put(toName, value);
                } else {
                    Object refToType = toFieldInfoMap.get(toName);
                    Field targetField2 = null;
                    Object setter = null; // Can be Method or GrpcListSetter

                    Class fieldType = null;
                    if(refToType==null) {
                        continue;
                    } else if(refToType instanceof Field) {
                        targetField2 = (Field) refToType;
                        if (!trySetAccessible(targetField2)) {
                            continue;
                        }
                        fieldType = targetField2.getType();
                    } else if(refToType instanceof Method) {
                        setter = refToType;
                        Method setterMethod = (Method) refToType;
                        Class<?>[] parameterTypes = setterMethod.getParameterTypes();
                        if(parameterTypes==null ||parameterTypes.length>1) {
                            continue;
                        }
                        fieldType = parameterTypes[0];
                    } else if(refToType instanceof GrpcListSetter) {
                        setter = refToType;
                        GrpcListSetter grpcSetter = (GrpcListSetter) refToType;
                        Method setterMethod = grpcSetter.getMethod();
                        Class<?>[] parameterTypes = setterMethod.getParameterTypes();
                        if(parameterTypes==null ||parameterTypes.length>1) {
                            continue;
                        }
                        fieldType = parameterTypes[0];

                        // For gRPC addAll methods, treat as List type
                        if (grpcSetter.getType() == GrpcListSetterType.ADD_ALL &&
                                Iterable.class.isAssignableFrom(fieldType)) {
                            fieldType = List.class;
                        }
                    }

                    if (value == null) {
                        setValue(to, null, targetField2, setter);
                        continue;
                    }

                    if (fieldType !=null && List.class.isAssignableFrom(fieldType) && value instanceof List) {
                        ParameterizedType genericType = null;
                        if(targetField2!=null) {
                            Type type = targetField2.getGenericType();
                            if (type instanceof ParameterizedType) {
                                genericType = (ParameterizedType) type;
                            }
                        } else if(setter instanceof Method) {
                            Method setterMethod = (Method) setter;
                            Type parameterType = setterMethod.getGenericParameterTypes()[0];
                            if(parameterType instanceof ParameterizedType) {
                                genericType = (ParameterizedType)parameterType;
                            }
                        } else if(setter instanceof GrpcListSetter) {
                            GrpcListSetter grpcSetter = (GrpcListSetter) setter;
                            Method setterMethod = grpcSetter.getMethod();
                            Type parameterType = setterMethod.getGenericParameterTypes()[0];
                            if(parameterType instanceof ParameterizedType) {
                                genericType = (ParameterizedType)parameterType;
                            }
                        }

                        if (genericType != null) {
                            // Handle generic type processing
                            Type actualTypeArgument = genericType.getActualTypeArguments()[0];

                            List<?> sourceList = (List<?>) value;
                            List<Object> targetList = new ArrayList<>();

                            for (Object item : sourceList) {
                                if (item == null) {
                                    targetList.add(null);
                                    continue;
                                }

                                if (actualTypeArgument instanceof Class) {
                                    // Simple types: List<String>, List<Integer>, etc.
                                    Class<?> itemType = (Class<?>) actualTypeArgument;

                                    if (isPrimitiveOrWrapperOrString(itemType) || itemType.isAssignableFrom(item.getClass())) {
                                        targetList.add(item);
                                    } else {
                                        // Convert regular objects or gRPC-style objects to DTOs
                                        Object targetItem = itemType.getDeclaredConstructor().newInstance();
                                        copy(item, targetItem, null, null, isIncludeUndefined, guardMap, skipNull);
                                        targetList.add(targetItem);
                                    }

                                } else if (actualTypeArgument instanceof ParameterizedType) {
                                    // Nested types: List<List<String>>, List<Map<String, Object>>, etc.
                                    ParameterizedType paramType = (ParameterizedType) actualTypeArgument;
                                    Class<?> rawType = (Class<?>) paramType.getRawType();

                                    if (List.class.isAssignableFrom(rawType)) {
                                        // List<List<String>> case
                                        List<Object> innerTargetList = new ArrayList<>();

                                        if (item instanceof List) {
                                            List<?> innerSourceList = (List<?>) item;
                                            Type innerType = paramType.getActualTypeArguments()[0];

                                            for (Object innerItem : innerSourceList) {
                                                if (innerItem == null) {
                                                    innerTargetList.add(null);
                                                } else if (innerType instanceof Class) {
                                                    Class<?> innerItemType = (Class<?>) innerType;
                                                    if (isPrimitiveOrWrapperOrString(innerItemType) ||
                                                            innerItemType.isAssignableFrom(innerItem.getClass())) {
                                                        innerTargetList.add(innerItem);
                                                    } else {
                                                        Object innerTargetItem = innerItemType.getDeclaredConstructor().newInstance();
                                                        copy(innerItem, innerTargetItem, null, null, isIncludeUndefined, guardMap, skipNull);
                                                        innerTargetList.add(innerTargetItem);
                                                    }
                                                } else {
                                                    // Handle deeper nesting recursively
                                                    innerTargetList.add(innerItem);
                                                }
                                            }
                                        }
                                        targetList.add(innerTargetList);

                                    } else if (Set.class.isAssignableFrom(rawType)) {
                                        Set<Object> targetSet = new HashSet<>();
                                        if (item instanceof Collection) {
                                            Collection<?> sourceCollection = (Collection<?>) item;
                                            for (Object collectionItem : sourceCollection) {
                                                targetSet.add(collectionItem);
                                            }
                                        }
                                        targetList.add(targetSet);

                                    } else if (Map.class.isAssignableFrom(rawType)) {
                                        Map<Object, Object> targetMap = new HashMap<>();
                                        if (item instanceof Map) {
                                            Map<?, ?> sourceMap = (Map<?, ?>) item;
                                            targetMap.putAll(sourceMap);
                                        }
                                        targetList.add(targetMap);

                                    } else {
                                        // Other complex types
                                        Object targetItem = rawType.getDeclaredConstructor().newInstance();
                                        copy(item, targetItem, null, null, isIncludeUndefined, guardMap, skipNull);
                                        targetList.add(targetItem);
                                    }

                                } else {
                                    // Other complex types - copy as is
                                    targetList.add(item);
                                }

                                if(guardMap!=null) {
                                    ctx = new MappingContext(MappingEventType.AFTER_ADD_LIST, from, item, to,
                                            targetList.get(targetList.size() - 1), fromName, mappingGuard);
                                    if(!ctx.checkEnable()) {
                                        targetList.remove(targetList.size() - 1);
                                    }
                                }
                            }

                            setValue(to, targetList, targetField2, setter);
                        } else {
                            // Simple copy when no generic information available
                            Object convertedValue = convertType(value, fieldType);
                            setValue(to, convertedValue, targetField2, setter);
                        }

                    } else if (value instanceof Map && !fieldType.isAssignableFrom(Map.class)) {
                        Object nestedObj = fieldType.getDeclaredConstructor().newInstance();
                        copy(value, nestedObj, null, null, isIncludeUndefined, guardMap, skipNull);
                        setValue(to, nestedObj, targetField2, setter);

                    } else {
                        if( !isPrimitiveOrWrapperOrString(fieldType) && fieldType!= value.getClass()) {
                            if(fieldType.isArray()) {
                                Class<?> componentType = fieldType.getComponentType();

                                if (value instanceof List) {
                                    List<?> list = (List<?>) value;
                                    Object array = java.lang.reflect.Array.newInstance(componentType, list.size());

                                    for (int i = 0; i < list.size(); i++) {
                                        Object elem = list.get(i);
                                        java.lang.reflect.Array.set(array, i, elem);
                                    }
                                    setValue(to, array, targetField2, setter);
                                }
                            } else {
                                Object nestedObj = fieldType.newInstance();
                                copy(value, nestedObj, null, null, isIncludeUndefined, guardMap, skipNull);
                                setValue(to, nestedObj, targetField2, setter);
                            }
                        } else {
                            Object convertedValue = convertType(value, fieldType);
                            setValue(to, convertedValue, targetField2, setter);
                        }
                    }
                }

            } catch (Exception e) {
                // Log individual field mapping failures and continue processing
                System.err.println("Warning: Failed to copy field " + fromName + " -> " + toName + ": " + e.getMessage());
                // throw new RuntimeException("copy error: " + fromName + " -> " + toName, e);
            }
        }
    }

    /**
     * Sets value to target object using either field or method access.
     * Supports gRPC-style list setters (addXxx, addAllXxx).
     *
     * @param target target object
     * @param value value to set
     * @param field field to set (if available)
     * @param method setter method to use (if available)
     */
    private static void setValue(Object target, Object value, Field field, Method method) {
        try {
            if (method != null) {
                method.invoke(target, value);
            } else if (field != null) {
                field.set(target, value);
            } else if( target instanceof  Map) {
                Map tartgetMap = (Map) target;
                // Map handling would go here if needed
            }
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException re) {
            // Exception handling - silent for now
        }
    }

    /**
     * Sets value to target object with support for gRPC-style list setters.
     *
     * @param target target object
     * @param value value to set
     * @param field field to set (if available)
     * @param methodOrSetter setter method or GrpcListSetter to use
     */
    private static void setValue(Object target, Object value, Field field, Object methodOrSetter) {
        try {
            if (methodOrSetter instanceof Method) {
                Method method = (Method) methodOrSetter;
                method.invoke(target, value);
            } else if (methodOrSetter instanceof GrpcListSetter) {
                GrpcListSetter grpcSetter = (GrpcListSetter) methodOrSetter;
                Method method = grpcSetter.getMethod();

                if (value instanceof List) {
                    List<?> listValue = (List<?>) value;

                    if (grpcSetter.getType() == GrpcListSetterType.ADD_ALL) {
                        // Use addAllXxx for List values - convert items if needed
                        List<Object> convertedList = new ArrayList<>();
                        Class<?> paramType = method.getParameterTypes()[0];

                        // Get the generic type of the Iterable parameter
                        Type genericParamType = method.getGenericParameterTypes()[0];
                        if (genericParamType instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) genericParamType;
                            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];

                            if (actualTypeArgument instanceof Class) {
                                Class<?> itemType = (Class<?>) actualTypeArgument;

                                for (Object item : listValue) {
                                    if (item == null) {
                                        convertedList.add(null);
                                    } else if (isPrimitiveOrWrapperOrString(itemType) || itemType.isAssignableFrom(item.getClass())) {
                                        convertedList.add(item);
                                    } else {
                                        // Convert DTO to gRPC object
                                        Object convertedItem = itemType.getDeclaredConstructor().newInstance();
                                        copy(item, convertedItem, null, null, true, null, false);
                                        convertedList.add(convertedItem);
                                    }
                                }
                            } else {
                                // If we can't determine the type, just copy as is
                                convertedList.addAll(listValue);
                            }
                        } else {
                            // No generic type info, copy as is
                            convertedList.addAll(listValue);
                        }

                        method.invoke(target, convertedList);

                    } else if (grpcSetter.getType() == GrpcListSetterType.ADD_SINGLE) {
                        // Use addXxx for each item in the list
                        Class<?> paramType = method.getParameterTypes()[0];

                        for (Object item : listValue) {
                            if (item != null) {
                                if (isPrimitiveOrWrapperOrString(paramType) || paramType.isAssignableFrom(item.getClass())) {
                                    method.invoke(target, item);
                                } else {
                                    // Convert DTO to gRPC object
                                    Object convertedItem = paramType.getDeclaredConstructor().newInstance();
                                    copy(item, convertedItem, null, null, true, null, false);
                                    method.invoke(target, convertedItem);
                                }
                            }
                        }
                    }
                } else if (grpcSetter.getType() == GrpcListSetterType.ADD_SINGLE && value != null) {
                    // Use addXxx for single item
                    method.invoke(target, value);
                }
            } else if (field != null) {
                field.set(target, value);
            } else if( target instanceof  Map) {
                Map tartgetMap = (Map) target;
                // Map handling would go here if needed
            }
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException re) {
            //System.err.println("Error in setValue: " + re.getMessage());
            // Exception handling - continue processing
        }
    }

    /**
     * Converts value to target type with support for Enum conversions.
     */
    private static Object convertType(Object value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return value;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        // Enum -> String
        if (value instanceof Enum<?> && targetType == String.class) {
            return convertEnumToString((Enum<?>) value);
        }
        // String -> Enum
        if (value instanceof String && targetType.isEnum()) {
            return convertStringToEnum((String) value, targetType);
        }
        // Number conversions
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == Integer.class || targetType == int.class) return num.intValue();
            if (targetType == Long.class || targetType == long.class) return num.longValue();
            if (targetType == Double.class || targetType == double.class) return num.doubleValue();
            if (targetType == Float.class || targetType == float.class) return num.floatValue();
            if (targetType == String.class) return num.toString();
        }
        // Any -> String
        if (targetType == String.class) {
            return value.toString();
        }
        return value;
    }

    /**
     * Converts Enum to String using getCode() if available, otherwise name().
     */
    private static String convertEnumToString(Enum<?> enumValue) {
        try {
            Method getCodeMethod = enumValue.getClass().getMethod("getCode");
            Object code = getCodeMethod.invoke(enumValue);
            if (code != null) return code.toString();
        } catch (Exception e) {
            // ignore
        }
        return enumValue.name();
    }

    /**
     * Converts String to Enum using fromCode() if available, otherwise valueOf().
     */
    private static Object convertStringToEnum(String value, Class<?> enumClass) {
        try {
            Method fromCodeMethod = enumClass.getMethod("fromCode", String.class);
            return fromCodeMethod.invoke(null, value);
        } catch (Exception e) {
            // ignore
        }
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumValue = Enum.valueOf((Class<Enum>) enumClass, value);
            return enumValue;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if a class is a primitive, wrapper, or String type.
     *
     * @param clazz class to check
     * @return true if it's a primitive, wrapper, or String
     */
    private static boolean isPrimitiveOrWrapperOrString(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Double.class
                || clazz == Float.class
                || clazz == Boolean.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Character.class
                || clazz == String.class;
    }

    /**
     * Simple copy method without field specification.
     *
     * @param from source object
     * @param to target object
     */
    public static void copy(Object from, Object to) {
        copy(from, to, null);
    }
}