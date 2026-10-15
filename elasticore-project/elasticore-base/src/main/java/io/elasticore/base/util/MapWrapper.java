package io.elasticore.base.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapWrapper {

    private Map mapData;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MapWrapper(Map map) {
        this.mapData = map;
    }

    public String getString(String key) {
        return objectMapper.convertValue(mapData.get(key), String.class);

    }

    public Integer getInteger(String key) {
        return objectMapper.convertValue(mapData.get(key), Integer.class);
    }

    public Long getLong(String key) {
        return objectMapper.convertValue(mapData.get(key), Long.class);
    }

    public Double getDouble(String key) {
        return objectMapper.convertValue(mapData.get(key), Double.class);
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        try {
            return objectMapper.convertValue(mapData.get(key), Boolean.class);
        }catch (NullPointerException npe) {}
        return defaultVal;
    }

    public List<MapWrapper> getList(String key) {
        Object obj = this.mapData.get(key);
        if (obj instanceof List) {
            List<MapWrapper> newList = new ArrayList<>();
            List list = (List) obj;
            for (Object item : list) {
                if (item instanceof Map) {
                    newList.add(new MapWrapper((Map) item));
                }
            }

            return newList;
        }
        return null;
    }

    public MapWrapper getMapWrapper(String key) {
        Object obj = this.mapData.get(key);
        if (obj instanceof Map) {
            Map map = (Map) obj;
            return new MapWrapper(map);
        }
        return null;
    }

}
