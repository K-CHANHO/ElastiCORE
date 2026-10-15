package io.elasticore.springboot3.mapper;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

class MappingUtils2 extends MappingUtils {

    public static <T> List<T> toList(List<?> originList, Class<T> targetClass) {
        return toList(originList, targetClass, null);
    }

    public static <T> List<T> toList(List<?> originList, Class<T> targetClass, List<String> fieldNames) {
        return toList(originList, targetClass, fieldNames, null);
    }

    public static <T> List<T> toList(List<?> originList, Class<T> targetClass, List<String> fieldNames, List<String> toFieldNames) {
        if (originList == null || targetClass == null) {
            throw new IllegalArgumentException("originList, targetClass must not be null");
        }

        List<T> result = new ArrayList<>(originList.size());

        for (Object source : originList) {
            try {
                T target = targetClass.getDeclaredConstructor().newInstance();
                copy(source, target, fieldNames, toFieldNames);
                result.add(target);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Failed to create instance of " + targetClass.getName(), e);
            }
        }

        return result;
    }

    public static void copy(Object from, Object to) {
        copy(from, to, null);
    }

    public static void copy(Object from, Object to, List<String> fieldNames) {
        copy(from, to, fieldNames, null);
    }

    public static void copy(Object from, Object to, List<String> fieldNames, List<String> toFieldNames) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Source and target must not be null");
        }

        Map<String, String> definedMappings = new LinkedHashMap<>();
        if (fieldNames != null) {
            if (toFieldNames == null) toFieldNames = fieldNames;
            for (int i = 0; i < Math.min(fieldNames.size(), toFieldNames.size()); i++) {
                definedMappings.put(fieldNames.get(i), toFieldNames.get(i));
            }
        } else {
            for (PropertyDescriptor pd : getPropertyDescriptors(from.getClass())) {
                if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                    definedMappings.put(pd.getName(), pd.getName());
                }
            }
        }

        Map<String, Method> sourceGetters = getReadMethods(from.getClass());
        Map<String, Method> targetSetters = getWriteMethods(to.getClass());

        for (Map.Entry<String, String> entry : definedMappings.entrySet()) {
            String fromName = entry.getKey();
            String toName = entry.getValue();

            try {
                Method getter = sourceGetters.get(fromName);
                Method setter = targetSetters.get(toName);
                if (getter == null || setter == null) continue;

                Object value = getter.invoke(from);

                if (value != null && List.class.isAssignableFrom(setter.getParameterTypes()[0]) && value instanceof List) {
                    ParameterizedType paramType = (ParameterizedType) setter.getGenericParameterTypes()[0];
                    Class<?> itemType = (Class<?>) paramType.getActualTypeArguments()[0];
                    List<?> sourceList = (List<?>) value;
                    List<Object> targetList = new ArrayList<>();
                    for (Object item : sourceList) {
                        if (item == null || isPrimitiveOrWrapperOrString(item.getClass())) {
                            targetList.add(item);
                        } else {
                            Object targetItem = itemType.getDeclaredConstructor().newInstance();
                            copy(item, targetItem);
                            targetList.add(targetItem);
                        }
                    }
                    setter.invoke(to, targetList);
                } else {
                    setter.invoke(to, value);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("copy error: " + fromName + " -> " + toName, e);
            }
        }
    }

    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        try {
            return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        } catch (Exception e) {
            throw new RuntimeException("Failed to introspect class: " + clazz.getName(), e);
        }
    }

    private static Map<String, Method> getReadMethods(Class<?> clazz) {
        return Arrays.stream(getPropertyDescriptors(clazz))
                .filter(pd -> pd.getReadMethod() != null)
                .collect(Collectors.toMap(PropertyDescriptor::getName, PropertyDescriptor::getReadMethod));
    }

    private static Map<String, Method> getWriteMethods(Class<?> clazz) {
        return Arrays.stream(getPropertyDescriptors(clazz))
                .filter(pd -> pd.getWriteMethod() != null)
                .collect(Collectors.toMap(PropertyDescriptor::getName, PropertyDescriptor::getWriteMethod));
    }

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




}
