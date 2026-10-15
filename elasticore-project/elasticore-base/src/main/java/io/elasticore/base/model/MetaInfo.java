package io.elasticore.base.model;

import io.elasticore.base.model.core.Annotation;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class MetaInfo {

    private Map<String, Annotation> infoAnnotationMap;

    private Map<String, Annotation> metaAnnotationMap;

    private String description;

    private MetaInfo() {
        this.infoAnnotationMap = null;
        this.metaAnnotationMap = null;

    }

    private MetaInfo(Map<String, Annotation> infoAnnotationMap, Map<String, Annotation> metaAnnotationMap, String description) {
        this.infoAnnotationMap = infoAnnotationMap;
        this.metaAnnotationMap = metaAnnotationMap;
        this.description = description;
    }

    public static MetaInfo creat(Map<String, Annotation> infoAnnotationMap, Map<String, Annotation> metaAnnotationMap, String description) {
        return new MetaInfo(infoAnnotationMap, metaAnnotationMap,description);
    }

    public static MetaInfo createEmpty(){
        return new MetaInfo();
    }

    public Annotation getInfoAnnotation(String name) {
        if (infoAnnotationMap == null) {
            return null;
        }

        return infoAnnotationMap.get(name);
    }


    public String getInfoAnnotationValue(String... names) {
        for(String name: names) {
            // searchable.entity ->  @Searchable(entity=Customer)
            String[] nmItems = name.split("\\.");
            String propertyName = null;
            if(nmItems.length==2){
                name = nmItems[0];
                propertyName = nmItems[1];
            }

            Annotation an = getInfoAnnotation(name);
            if(an!=null) {
                if(propertyName!=null) {
                    Properties props = an.getProperties();
                    if(props!=null) {
                        String val = props.getProperty(propertyName);
                        if(val!=null) return val;
                    }
                }else {
                    String val = an.getValue();
                    if(val !=null) return val;
                }
            }
        }

        return null;
    }

    public boolean hasInfoAnnotation(String name) {
        if (infoAnnotationMap == null) {
            return false;
        }
        return infoAnnotationMap.containsKey(name);
    }

    public Map<String, Annotation> getMetaAnnotationMap() {
        return metaAnnotationMap;
    }

    public Annotation getMetaAnnotation(String... names) {
        if (metaAnnotationMap == null || names ==null) {
            return null;
        }
        for(String name: names) {
            String key = name.toLowerCase(Locale.ROOT);
            Annotation annotation = metaAnnotationMap.get(key);
            if(annotation!=null)
                return annotation;
        }
        return null;
    }

    public boolean hasMetaAnnotation(String... names) {

        if (metaAnnotationMap == null|| names ==null) {
            return false;
        }

        for(String name: names) {
            String key = name.toLowerCase(Locale.ROOT);
            if(metaAnnotationMap.containsKey(key))
                return true;
        }

        return false;
    }

    public String getMetaAnnotationValue(String... names) {
        for(String name: names) {
            // searchable.entity ->  @Searchable(entity=Customer)
            String[] nmItems = name.split("\\.");
            String propertyName = null;
            if(nmItems.length==2){
                name = nmItems[0];
                propertyName = nmItems[1];
            }

            Annotation an = getMetaAnnotation(name);
            if(an!=null) {
                if(propertyName!=null) {
                    Properties props = an.getProperties();
                    if(props!=null) {
                        String val = props.getProperty(propertyName);
                        if(val!=null) return val;
                    }
                }else {
                    String val = an.getValue();
                    if(val !=null) return val;
                }
            }
        }

        return null;
    }

    public String getLabel() {
        return getMetaAnnotationValue("label");
    }

    public String getDescription() {
        return this.description;
    }
}
