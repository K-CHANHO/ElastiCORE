package io.elasticore.base.model.entity;

import io.elasticore.base.model.ComponentIdentity;
import io.elasticore.base.model.ComponentType;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponent;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.BaseComponentIdentity;
import io.elasticore.base.util.StringUtils;
import lombok.Builder;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Getter
public class Field implements ModelComponent {


    private ComponentIdentity identity;

    private String name;

    private String type;

    private TypeInfo typeInfo;

    private String description;

    private MetaInfo parentMetaInfo;

    private boolean nullable = true;


    @Builder(builderMethodName = "builder")
    private Field(String name, String type, boolean isPrimaryKey, boolean unique, String description, Map<String, Annotation> annotationMap ,MetaInfo parentMetaInfo) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
        this.unique = unique;
        this.description = description;
        this.annotationMap = annotationMap;
        this.parentMetaInfo = parentMetaInfo;
    }

    public int getLength() {
        String lenTxt = getAnnotationValue(EntityAnnotation.LENGTH);
        if(lenTxt!=null) {
            // Handle complex parameter format: @length(min=1, max=200) or @length(200)
            if(lenTxt.contains("max=")) {
                return getComplexParameterIntValue(lenTxt, "max");
            }
            return Integer.parseInt(lenTxt);
        }

        return -1;
    }

    /**
     * Get minimum length from @length(min=1, max=200) format
     */
    public int getMinLength() {
        // Check for complex parameter format using annotation properties
        if(hasAnnotation(EntityAnnotation.LENGTH)) {
            // Try each possible annotation name (length, len, size)
            for(String lengthName : EntityAnnotation.LENGTH) {
                Annotation first = getAnnotation(lengthName);
                if(first == null) continue;
                // string(n) puts @length(n) first, so an explicit @length(min=..)
                // on the same line is a sibling of it - read all of them.
                for(Annotation lengthAnnotation : first.getSiblings()) {
                    // Check if it's a complex parameter (TYPE_KEY_VALUE with properties)
                    if(lengthAnnotation.getType() == Annotation.TYPE_KEY_VALUE && lengthAnnotation.getProperties() != null) {
                        String minValue = lengthAnnotation.getProperties().getProperty("min");
                        if(minValue != null) {
                            try {
                                return Integer.parseInt(minValue);
                            } catch (NumberFormatException e) {
                                // Continue with the next annotation
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Get maximum length from @length(min=1, max=200) format
     */
    public int getMaxLength() {
        // Check for length annotation using all possible names (length, len, size)
        if(hasAnnotation(EntityAnnotation.LENGTH)) {
            // Try each possible annotation name (length, len, size)
            for(String lengthName : EntityAnnotation.LENGTH) {
                Annotation lengthAnnotation = getAnnotation(lengthName);
                if(lengthAnnotation != null) {
                    // Check if it's a complex parameter (TYPE_KEY_VALUE with properties)
                    if(lengthAnnotation.getType() == Annotation.TYPE_KEY_VALUE && lengthAnnotation.getProperties() != null) {
                        String maxValue = lengthAnnotation.getProperties().getProperty("max");
                        if(maxValue != null) {
                            try {
                                return Integer.parseInt(maxValue);
                            } catch (NumberFormatException e) {
                                // Continue to next annotation name
                            }
                        }
                    }
                    // Check for single value format: @length(200)
                    else if(lengthAnnotation.getType() == Annotation.TYPE_SINGLE_VALUE) {
                        String lenTxt = lengthAnnotation.getValue();
                        if(lenTxt != null) {
                            try {
                                return Integer.parseInt(lenTxt);
                            } catch (NumberFormatException e) {
                                // Continue to next annotation name
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Parse complex parameter format like "min=1, max=200" or "min=1,max=200"
     */
    private int getComplexParameterIntValue(String parameterString, String paramName) {
        try {
            // Remove spaces and handle both "," and ", " separators
            String[] parts = parameterString.replaceAll("\\s+", "").split(",");
            for (String part : parts) {
                if (part.startsWith(paramName + "=")) {
                    return Integer.parseInt(part.substring(paramName.length() + 1));
                }
            }
        } catch (NumberFormatException e) {
            // Return -1 if parsing fails
        }
        return -1;
    }

    public String getDbColumnName() {

        if(!this.typeInfo.isBaseType())
            return null;

        String dbColumnName = getAnnotationValue(EntityAnnotation.COLUMN_NAME);
        if(dbColumnName!=null)
            return dbColumnName;

        return StringUtils.camelToSnake(name);
    }



    private boolean isPrimaryKey = false;

    private String generationType;

    private String columnDefinition;

    private boolean unique = false;

    private Map<String, Annotation> annotationMap;



    @Override
    public ComponentIdentity getIdentity() {
        // TODO
        return BaseComponentIdentity.create(ComponentType.FIELD, null, this.name);
    }



    public Annotation getAnnotation(String name) {
        if (annotationMap == null) {
            return null;
        }

        return annotationMap.get(name);
    }

    public String getAnnotationValue(String... names) {
        for(String name: names) {
            // searchable.entity ->  @Searchable(entity=Customer)
            String[] nmItems = name.split("\\.");
            String propertyName = null;
            if(nmItems.length==2){
                name = nmItems[0];
                propertyName = nmItems[1];
            }

            Annotation an = getAnnotation(name);
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
                    Properties props = an.getProperties();
                    if(props != null) {
                        val = props.getProperty("name");
                        if(val != null) return val;
                    }
                }
            }
        }

        return null;
    }

    public Annotation removeAnnotation(String name) {
        return annotationMap.remove(name);
    }


    public boolean hasAnnotation(String... names) {
        if (annotationMap == null || names ==null) {
            return false;
        }
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String key = name.toLowerCase(Locale.ROOT);
            if(annotationMap.containsKey(key))
                return true;
        }
        return false;
    }


    public TypeInfo getTypeInfo() {
        if (this.typeInfo == null) {
            this.typeInfo = new TypeInfo(this.type ,annotationMap);
        }
        return this.typeInfo;
    }


    private String getType() {
        return this.type;
    }

}
