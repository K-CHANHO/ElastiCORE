package io.elasticore.base.model.core;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ModelComponent;
import io.elasticore.base.model.ModelComponentItems;

import java.util.List;

public final class BaseModelComponentItem<T extends ModelComponent> implements ModelComponentItems {
    private final Items<T> items;

    private List<T> itemList;

    public BaseModelComponentItem(Items<T> items) {
        if(items==null) {
            this.items = null;
            this.itemList = null;
        }else {
            this.items = items;
            this.itemList = items.getItemList();
        }

    }

    public int size() {
        if(this.items==null) return 0;
        return this.itemList.size();
    }

    public T find(ComponentIdentity identity) {
        if(this.items==null) return null;
        return this.items.find(identity);
    }

    public T findByName(String name) {
        if(this.items==null) return null;
        for(T t:this.itemList) {
            if( t.getIdentity().getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    public T get(int idx) {
        if(this.items==null) return null;
        try {
            return this.itemList.get(idx);
        }catch (RuntimeException re) {
            return null;
        }
    }


    private int point = 0;
    @Override
    public boolean hasNext() {
        if(this.items==null) return false;

        if(this.size()>point){
            return true;
        }

        return false;
    }

    @Override
    public Object next() {
        if(this.items==null) return null;
        int p = this.point;
        this.point=this.point+1;
        return this.get(p);
    }
}
