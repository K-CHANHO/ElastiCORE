package io.elasticore.base.model.core;

import java.util.*;

public class ListMap<K, V> extends LinkedHashMap<K,V>{

    private List<V> list;

    public ListMap() {
        super();
    }

    @Override
    public V put(K key, V value) {
        list = null;
        return super.put(key,value);
    }

    public V remove(Object key) {
        list = null;
        return super.remove(key);
    }


    public List<V> getList() {
        if(this.list ==null) {
            Collection<V> values = this.values();
            this.list = new ArrayList<>(values);
        }

        return this.list;
    }

    /**
     * Retrieves the value associated with the specified key, ignoring case sensitivity.
     * This method only works if K is of type String.
     *
     * @param keyToCheck the key to search for
     * @return the value associated with the key, or null if the key does not exist
     */
    public V getIgnoreCase(String keyToCheck) {
        if (keyToCheck == null) {
            return null; // Null keys cannot be compared case-insensitively
        }
        for (K key : this.keySet()) {
            if (key instanceof String && ((String) key).equalsIgnoreCase(keyToCheck)) {
                return this.get(key); // Return the value for the matching key
            }
        }
        return null; // No matching key found
    }

}
