package io.elasticore.base.util;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PropertyNode {

    private final Map<String, Object> map;

    public PropertyNode(Map<String, Object> map) {
        this.map = map;
    }

    public static PropertyNode fromYaml(InputStream is) {
        return from(new YAMLFactory(), is);
    }

    //JsonFactory
    public static PropertyNode from(JsonFactory jsonFactory, InputStream is) {

        String typeName = "json";
        try {
            ObjectMapper mapper = null;
            if(jsonFactory==null)
                mapper = new ObjectMapper();
            else {
                mapper = new ObjectMapper(jsonFactory);
                typeName = jsonFactory.getClass().getSimpleName();
            }

            Map<String, Object> parsedMap = mapper.readValue(is, LinkedHashMap.class);
            return new PropertyNode(parsedMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse "+typeName, e);
        }
    }

    public static PropertyNode fromJson(InputStream is) {
        return from(null, is);
    }

    @SuppressWarnings("unchecked")
    public PropertyNode getMap(String key) {
        String[] keys = key.split("\\.");
        PropertyNode current = this;
        for (String k : keys) {
            Object value = current.map.get(k);
            if (value instanceof Map) {
                current = new PropertyNode((Map<String, Object>) value);
            } else {
                return new PropertyNode(new LinkedHashMap<>());
            }
        }
        return current;
    }

    public String getString(String key) {
        return Optional.ofNullable(map.get(key))
                .map(Object::toString)
                .orElse(null);
    }

    public String getString(String key, String defaultValue) {
        return Optional.ofNullable(getString(key)).orElse(defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object val = map.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return val != null ? Boolean.parseBoolean(val.toString()) : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Integer) {
            return (Integer) val;
        }
        try {
            return val != null ? Integer.parseInt(val.toString()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Set<String> keys() {
        return map.keySet();
    }
}
