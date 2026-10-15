package io.elasticore.base.model.core;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ModelComponent;

import java.util.*;
import java.util.function.Predicate;

public final class Items<T extends ModelComponent> {
    private Map<ComponentIdentity, T> itemMap = new LinkedHashMap<>();

    private List<T> newList;

    private Items() {
    }

    public static <T extends ModelComponent> Items<T> create(Class<T> type) {
        return new Items<>();
    }


    public Items<T> addItem(T item) {
        this.newList = null;
        itemMap.put(item.getIdentity(), item);
        return this;
    }


    public int size() {
        return this.itemMap.size();
    }

    public T find(ComponentIdentity identity) {
        return this.itemMap.get(identity);
    }

    public T filter(Predicate<T> predicate) {
        List<T> list = this.getItemList();

        List<T> filteredList = new ArrayList<>();
        for (T element : list) {
            if (predicate.test(element)) {
                return element;
            }
        }
        return null;
    }

    public T findByName(String name) {
        List<T> list = this.getItemList();
        for(T t: list) {
            if(t.getIdentity().getName().equalsIgnoreCase(name))
                return t;
        }
        return null;
    }

    public List<T> getItemList() {
        if(newList!=null)
            return newList;

        this.newList = new ArrayList<>();

        Object[] preItemArray = this.itemMap.values().toArray();

        for (int i = 0; i < preItemArray.length; i++) {
            newList.add((T) preItemArray[i]);
        }

        return newList;

        //return this.itemMap.values().stream().map(e -> (T) e).toArray();
        //return this.itemMap.values().toArray(new ModelComponent[0]);
    }


}

