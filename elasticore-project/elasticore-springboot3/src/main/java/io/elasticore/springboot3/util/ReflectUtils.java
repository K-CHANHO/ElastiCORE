package io.elasticore.springboot3.util;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.*;


/**
 * Utility class for handling reflection-related operations.
 * <p>
 * Provides optimized access to class fields with caching for better performance.
 * Field name lookups are case-insensitive.
 * </p>
 */
public class ReflectUtils {

    /** Cache of class-to-field mappings to improve lookup performance. */
    private static Map<Class, Map<String, Field>> classFieldMap = new HashMap<>();

    /**
     * Retrieves a {@link Field} object for the given class and field name.
     * <p>
     * The lookup is case-insensitive. Fields are cached after the first lookup
     * to avoid repeated reflection overhead.
     * </p>
     *
     * @param c     the class to retrieve the field from
     * @param fieldName the name of the field (case-insensitive)
     * @return the {@link Field} object, or {@code null} if not found
     */
    public static Field getField(Class c, String fieldName) {
        String lowcaseFieldName = fieldName.toLowerCase();
        Map<String, Field> fieldMap = classFieldMap.get(c);
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
            classFieldMap.put(c, fieldMap);
            Field[] fds = c.getDeclaredFields();
            for (Field f : fds) {
                String lowcaseFieldNm = f.getName().toLowerCase();
                try {
                    f.setAccessible(true);
                    fieldMap.put(lowcaseFieldNm, f);
                }catch (InaccessibleObjectException ie) {
                }
            }
        }

        return fieldMap.get(lowcaseFieldName);
    }

}
