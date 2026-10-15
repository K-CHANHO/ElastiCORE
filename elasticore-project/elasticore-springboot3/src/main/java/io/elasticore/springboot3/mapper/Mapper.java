package io.elasticore.springboot3.mapper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.*;
import java.util.Map;

/**
 * Utility class for mapping fields between source and target objects.
 * <p>
 * Supports selective field mapping, automatic mapping, null-skipping, and
 * batch conversion for lists. Also allows for field-level mapping guards.
 * </p>
 *
 * @param <T> the target object type
 */
public class Mapper<T> {

    private final Object from;
    private final Object to;
    private final Class<T> toClass;

    private final Map<String, String> fieldMap = new LinkedHashMap<>();
    private final Map<String, MappingGuard> fieldMappingGuardMap = new HashMap<>();
    private List<String> fromKeyNmList;
    private List<String> toKeyNmList;
    private boolean skipNull = false;
    private boolean autoMap = true;

    private boolean isIncludeUndefined = true;

    private Mapper(Object from, Object to) {
        this.from = from;
        this.to = to;
        this.toClass = null;
    }

    private Mapper(Object from, Class<T> toClass) {
        this.from = from;
        this.to = null;
        this.toClass =toClass;
    }

    private Mapper() {
        this.from = null;
        this.to = null;
        this.toClass = null;
    }

    /**
     * Creates a Mapper instance for object-to-object mapping.
     *
     * @param from source object
     * @param to   target object
     * @return a new Mapper instance
     */
    public static Mapper<Object> of(Object from, Object to) {
        return new Mapper(from, to);
    }

    /**
     * Creates a Mapper instance for object-to-class mapping.
     *
     * @param from    source object
     * @param toClass target class type
     * @param <T>     target type
     * @return a new Mapper instance
     */
    public static <T> Mapper<T> of(Object from, Class<T> toClass) {
        return new Mapper(from, toClass);
    }

    /**
     * Creates an empty Mapper instance.
     *
     * @return an empty Mapper instance
     */
    public static Mapper<Object> ofEmpty() {
        return new Mapper();
    }

    /**
     * Sets whether undefined fields should be included during mapping.
     *
     * @param includeUndefined true to include undefined fields
     * @return updated Mapper instance
     */
    public Mapper<T> includeUndefinedMap(boolean includeUndefined) {
        this.isIncludeUndefined = includeUndefined;
        return this;
    }

    /**
     * Adds a mapping guard for a specific field.
     *
     * @param fromFieldName source field name
     * @param mappingGuard  guard to apply
     * @return updated Mapper instance
     */
    public Mapper<T> on(String fromFieldName, MappingGuard mappingGuard) {
        fieldMappingGuardMap.put(fromFieldName, mappingGuard);
        return this;
    }

    /**
     * Maps fields with identical names between source and target.
     *
     * @param fieldNames field names to map
     * @return updated Mapper instance
     */
    public Mapper<T> map(String...fieldNames) {

        if(fromKeyNmList==null)
            fromKeyNmList = new ArrayList<>();
        if(toKeyNmList==null)
            toKeyNmList = new ArrayList<>();

        for(String fieldName : fieldNames) {
            fromKeyNmList.add(fieldName);
            toKeyNmList.add(fieldName);
        }

        this.isIncludeUndefined = false;
        return this;
    }

    /**
     * Maps a source field to a different target field.
     *
     * @param fromField source field name
     * @param toField   target field name
     * @return updated Mapper instance
     */
    public Mapper<T> map(String fromField, String toField) {
        fieldMap.put(fromField, toField);
        if(fromKeyNmList==null)
            fromKeyNmList = new ArrayList<>();
        if(toKeyNmList==null)
            toKeyNmList = new ArrayList<>();

        fromKeyNmList.add(fromField);
        toKeyNmList.add(toField);
        return this;
    }

    /**
     * Configures whether to skip null values during mapping.
     *
     * @param skip true to skip null values
     * @return updated Mapper instance
     */
    public Mapper<T> skipNull(boolean skip) {
        this.skipNull = skip;
        return this;
    }

    /**
     * Configures automatic mapping behavior.
     *
     * @param autoMap true to enable automatic field mapping
     * @return updated Mapper instance
     */
    private Mapper<T> autoMap(boolean autoMap) {
        this.autoMap = autoMap;
        return this;
    }

    /**
     * Copies fields from source to target object.
     *
     * @param src source object
     * @param to  target object
     */
    public static void copy(Object src, Object to) {
        MappingUtils.copy(src, to);
    }

    /**
     * Executes the mapping and returns the target object.
     *
     * @param <T> target object type
     * @return the mapped target object
     */
    @SuppressWarnings("unchecked")
    public <T> T execute() {
        if (from == null || (to == null && toClass == null)) {
            throw new IllegalArgumentException("Both source and target must be non-null");
        }

        if (from == to) {
            throw new IllegalArgumentException("Both source and target is same.");
        }

        Object toObject;
        if (toClass != null) {
            try {
                toObject = toClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create instance of target class: " + toClass.getName(), e);
            }
        } else {
            toObject = to;
        }

        MappingUtils.copy(from, toObject, fromKeyNmList, toKeyNmList, isIncludeUndefined, fieldMappingGuardMap, skipNull);
        return (T) toObject;
    }

    /**
     * Converts a list of source objects to a list of mapped target objects.
     *
     * @param <T> target object type
     * @return a list of mapped target objects, or {@code null} if the source is not a list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> toList() {

        if(from instanceof List<?>) {
            List originList = (List) from;
            List<T> result = new ArrayList<>(originList.size());

            for (Object source : originList) {
                try {
                    T target = (T) toClass.getDeclaredConstructor().newInstance();
                     MappingUtils.copy(source, target, fromKeyNmList, toKeyNmList, isIncludeUndefined, fieldMappingGuardMap, skipNull);
                    result.add(target);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("Failed to create instance of " + toClass.getName(), e);
                }
            }
            return result;

        }else {

        }

        return null;
    }

    /**
     * Creates and returns a new target object by instantiating the target class
     * and copying mapped fields from the source object.
     * 
     * @param <T> the target object type
     * @return a new instance of the target class with mapped field values
     * @throws RuntimeException if the target class cannot be instantiated
     *         (e.g., no default constructor, access restrictions)
     */
    @SuppressWarnings("unchecked")
    public <T> T toObject() {
        T target = null;
        try {
            target = (T) toClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

         MappingUtils.copy(from, target, fromKeyNmList, toKeyNmList, isIncludeUndefined, fieldMappingGuardMap, skipNull);
        return target;
    }
}