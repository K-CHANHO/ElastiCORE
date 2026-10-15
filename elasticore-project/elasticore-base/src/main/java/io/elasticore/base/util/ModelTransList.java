package io.elasticore.base.util;

import lombok.SneakyThrows;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ModelTransList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    private final List<Object[]> inputList;
    private List<E> newList = null;
    private final Class itemClass;

    private List<Field> fieldList;
    private List<PropertyEditor> propertyEditors;



    private E createInstance(Object[] objects) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if(this.fieldList!=null) {
            Object obj = this.itemClass.newInstance();

            int sz = fieldList.size();
            for(int i=0;i<sz;i++) {
                Field f = fieldList.get(i);
                PropertyEditor e = propertyEditors.get(i);

                if(objects[i]!=null && e!=null) {
                    e.setAsText(objects[i].toString());
                    f.set(obj, e.getValue());
                }

            }

            return (E) obj;

        }

        Constructor<E> constructor = itemClass.getConstructor(Object[].class);

        return constructor.newInstance((Object) objects);
    }



    public ModelTransList(List<?> inputParamList, Class itemClass) {
        this.itemClass = itemClass;
        this.inputList = toListOfObjectArray(inputParamList);;
        this.newList = new ArrayList<>();
    }



    @SneakyThrows
    public ModelTransList(List<?> inputParamList, Class itemClass, String[] fieldNames) {
        this.itemClass = itemClass;
        this.inputList = toListOfObjectArray(inputParamList);
        this.newList = new ArrayList<>();

        this.fieldList = new ArrayList<>();
        this.propertyEditors = new ArrayList<>();

        for(String fieldName : fieldNames) {
            try {
                Field f = itemClass.getDeclaredField(fieldName);
                f.setAccessible(true);
                fieldList.add(f);

                PropertyEditor editor = PropertyEditorManager.findEditor(f.getType());
                propertyEditors.add(editor);
            }catch (NoSuchFieldException nsfe) {}
        }
    }

    private static List<Object[]> toListOfObjectArray(List<?> source) {
        List<Object[]> result = new ArrayList<>();

        for (Object element : source) {
            if (element instanceof Object[]) {
                // Already Object[] format, use as is
                result.add((Object[]) element);
            } else {
                // Wrap single object in Object[] array
                // Handle null elements safely with new Object[] { element }
                result.add(new Object[] { element });
            }
        }

        return result;
    }

    @SneakyThrows
    @Override
    public E get(int index) {
        if (this.newList.size() <= index) {
            E e = createInstance(this.inputList.get(index));
            this.newList.add(e);
            return e;
        }
        return this.newList.get(index);
    }

    @Override
    public int size() {
        return this.inputList.size();
    }


    @SneakyThrows
    private void replaceAll() {
        if (this.inputList.size() == this.newList.size()) return;
        this.newList = new ArrayList<>();
        for (Object[] obj : this.inputList) {
            E e = createInstance(obj);
            this.newList.add(e);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        replaceAll();
        this.newList.replaceAll(operator);

    }

    @Override
    public void sort(Comparator<? super E> c) {
        replaceAll();
        this.newList.sort(c);
    }

    @Override
    public Spliterator<E> spliterator() {
        replaceAll();
        return this.newList.spliterator();

    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        replaceAll();
        return this.newList.removeIf(filter);
    }

    @Override
    public Stream<E> stream() {
        replaceAll();
        return this.newList.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        replaceAll();
        return this.newList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        replaceAll();
        this.newList.forEach(action);
    }


    public static String replace(String template, Map<String, Object> placeholders) {
        if(placeholders==null)
            return template;
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = "/*${" + entry.getKey() + "}*/";
            String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
            template = template.replace(placeholder, replacement);
        }
        return template;
    }
}
