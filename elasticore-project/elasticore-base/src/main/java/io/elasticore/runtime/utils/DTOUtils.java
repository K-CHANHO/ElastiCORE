package io.elasticore.runtime.utils;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DTOUtils {

    protected static Object invokeMethod(Class clazz, String methodName, Object obj) {

        if(obj==null)
            return null;

        Method method = null;

        try {
            method = clazz.getMethod(methodName);
            return method.invoke(obj);
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return null;
    }

    private static void setDescriptionInfo(Map<String, Object> fieldMetadata, Field field) {
        try {
            Class clazz = Class.forName("io.swagger.v3.oas.annotations.media.Schema");
            Annotation annotation = field.getAnnotation(clazz);

            Object val = invokeMethod(clazz, "description", annotation);
            if (val != null) {
                fieldMetadata.put("description", val);
            }

        } catch (ClassNotFoundException e) {
        }
    }

    private static void setMaxSize(Map<String, Object> fieldMetadata, Field field) {
        try {
            Class clazz = Class.forName("jakarta.validation.constraints.Size");
            Annotation annotation = field.getAnnotation(clazz);

            Object val = invokeMethod(clazz, "max", annotation);
            if (val != null) {
                fieldMetadata.put("maxSize", val);
            }

        } catch (ClassNotFoundException e) {
        }
    }


    /**
     * The declared shape of a DTO, as the generated /schema/info endpoint
     * reports it.
     *
     * Declared with a type variable - not a raw {@code Class}, and not a
     * wildcard. All three erase to the same signature, so each is binary and
     * source compatible with the others; the difference is only what a
     * caller's analyser sees, and every caller of this is generated, so it is
     * seen in somebody else's file.
     *
     * It was raw first, which made the generated line a raw-type use.
     * {@code Class<?>} removed that and an IDE still marked the call, offering
     * to change the parameter to {@code Class<ProductDTO>} - it wanted an
     * exact binding. {@code Class<T>} is that: T binds to the DTO named at the
     * call site and there is no wildcard left for anything to reason about.
     *
     * @param dtoClass the DTO type to describe
     * @param <T> the DTO type, bound from the class literal at the call site
     * @return field name to metadata, in declaration order
     */
    public static <T> Map<String, Object> getMetaInfo(Class<T> dtoClass) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        Field[] fields = dtoClass.getDeclaredFields();

        for (Field field : fields) {
            Map<String, Object> fieldMetadata = new LinkedHashMap<>();
            fieldMetadata.put("type", field.getType().getSimpleName());

            setDescriptionInfo(fieldMetadata, field);
            setMaxSize(fieldMetadata, field);


            // Handle Enum types (like BrandType enum classes)
            if (field.getType().isEnum()) {
                fieldMetadata.put("type", "enumeration");
                List<Map<String, Object>> enumValues = new ArrayList<>();

                for (Object enumConstant : field.getType().getEnumConstants()) {
                    Map<String, Object> enumInfo = new LinkedHashMap<>();

                    // enum constant name
                    enumInfo.put("enum", enumConstant.toString());

                    // Extract enum field values
                    try {
                        for (Field enumField : enumConstant.getClass().getDeclaredFields()) {
                            if (!enumField.isEnumConstant() && !Modifier.isStatic(enumField.getModifiers())) {
                                enumField.setAccessible(true);
                                String keyNm = enumField.getName();
                                if("code".equals(keyNm)) keyNm="value";
                                else if("label".equals(keyNm)) keyNm="text";

                                Object valObj = enumField.get(enumConstant);
                                enumInfo.put(keyNm, valObj);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    enumValues.add(enumInfo);
                }

                fieldMetadata.put("enums", enumValues);
            }

            metadata.put(field.getName(), fieldMetadata);
        }

        return metadata;
    }


}
