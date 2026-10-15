package io.elasticore.base.model;

import java.util.Iterator;

public interface ModelComponentItems<E> extends Iterator<E> {
    
    int size();

    E find(ComponentIdentity identity);

    E findByName(String name);

    E get(int idx);
}
