package io.elasticore.base.runtime;

import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.Field;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Runtime Map-to-Map conversion engine driven by an ElastiCORE DSL Entity.
 *
 * Conversion rules (applied per field):
 *   1. Input Map key = DSL field name (caller must pre-normalize upstream keys such as Excel headers)
 *   2. @fn(name[,name2,...]) or @function(...) - apply transform chain
 *   3. @default(val) - fallback when value is null or empty string
 *   4. @target(name) - output key; when absent, DSL field name is used
 *
 * After all fields are processed, the staticValues block (if present on the entity meta)
 * is copied into each output row verbatim.
 *
 * This class is Spring-free and Excel-free. ElastiCORE does not care where the input
 * map came from (Excel, CSV, JSON, DB...).
 */
public final class EntityConverter {

    private EntityConverter() {
        // static utility
    }

    /**
     * Convert a list of source rows using the given entity's field annotations.
     *
     * @param entity     DSL entity (typically marked with @runtime(map))
     * @param sourceRows input rows keyed by DSL field name
     * @return converted rows keyed by @target value (or DSL field name when absent),
     *         with staticValues appended to each row
     */
    public static List<Map<String, Object>> toMap(Entity entity, List<Map<String, Object>> sourceRows) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (sourceRows == null || sourceRows.isEmpty()) return result;

        Map<String, Object> staticValues = extractStaticValues(entity);

        for (Map<String, Object> row : sourceRows) {
            if (row == null) continue;
            result.add(convertRow(entity, row, staticValues));
        }
        return result;
    }

    /**
     * Convert a single source row. Kept public for test convenience.
     */
    public static Map<String, Object> toMap(Entity entity, Map<String, Object> sourceRow) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        if (sourceRow == null) sourceRow = new LinkedHashMap<String, Object>();
        return convertRow(entity, sourceRow, extractStaticValues(entity));
    }

    /**
     * Extract entity-level staticValues block as a plain map.
     * Stored by EntityModelLoader as a synthetic "staticvalues" meta annotation.
     */
    public static Map<String, Object> extractStaticValues(Entity entity) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        if (entity == null || entity.getMetaInfo() == null) return out;
        Annotation sv = entity.getMetaInfo().getMetaAnnotation("staticvalues");
        if (sv == null) return out;
        Properties props = sv.getProperties();
        if (props == null) return out;
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            out.put(String.valueOf(e.getKey()), e.getValue());
        }
        return out;
    }

    private static Map<String, Object> convertRow(Entity entity, Map<String, Object> sourceRow, Map<String, Object> staticValues) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        ModelComponentItems<Field> fields = entity.getItems();
        if (fields != null) {
            int n = fields.size();
            for (int i = 0; i < n; i++) {
                Field field = fields.get(i);
                String fieldName = field.getName();
                Object value = sourceRow.get(fieldName);

                // Apply @fn / @function chain (comma-separated names)
                String chain = field.getAnnotationValue("fn", "function");
                if (chain != null && !chain.isEmpty()) {
                    String[] names = chain.split(",");
                    for (int j = 0; j < names.length; j++) {
                        String fnName = names[j].trim();
                        if (fnName.isEmpty()) continue;
                        value = FunctionRegistry.apply(fnName, value);
                    }
                }

                // Apply @default when null or empty string
                if (isNullOrEmpty(value)) {
                    String def = field.getAnnotationValue("default");
                    if (def != null) value = def;
                }

                // Determine output key: @target wins, else DSL field name
                String targetKey = field.getAnnotationValue("target");
                if (targetKey == null || targetKey.isEmpty()) targetKey = fieldName;

                result.put(targetKey, value);
            }
        }

        // Merge staticValues last (overrides any same-key field output)
        if (staticValues != null && !staticValues.isEmpty()) {
            for (Map.Entry<String, Object> e : staticValues.entrySet()) {
                result.put(e.getKey(), e.getValue());
            }
        }

        return result;
    }

    private static boolean isNullOrEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String) return ((String) value).trim().isEmpty();
        return false;
    }
}
