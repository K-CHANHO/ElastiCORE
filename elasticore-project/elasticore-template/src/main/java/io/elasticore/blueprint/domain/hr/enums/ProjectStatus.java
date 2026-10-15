//ecd:-1667931304H20260207233343_V1.0
package io.elasticore.blueprint.domain.hr.enums;

import lombok.Getter;


import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;

/**
 * ProjectStatus
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = ProjectStatus.CustomSerializer.class)
@JsonDeserialize(using = ProjectStatus.CustomDeserializer.class)
@Getter
public enum ProjectStatus {

    PLANNED("PLN","Planned")
    ,ACTIVE("ACT","Active")
    ,ON_HOLD("HLD","On Hold")
    ,COMPLETED("CMP","Completed")
    ,CANCELLED("CXL","Cancelled");

    private final String code;
    private final String name;

    ProjectStatus(String code,String name) {
        this.code = code;
    this.name = name;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(ProjectStatus i:ProjectStatus.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("name", i.getName());
            list.add(map);
        }
        return list;
    }

    public static ProjectStatus fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (ProjectStatus type : ProjectStatus.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown ProjectStatus code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<ProjectStatus>{
        @Override
        public void serialize(ProjectStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<ProjectStatus>{
        @Override
        public ProjectStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return ProjectStatus.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(ProjectStatus.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<ProjectStatus, String>{
        @Override
        public String convertToDatabaseColumn(ProjectStatus e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public ProjectStatus convertToEntityAttribute(String val){
            if (val == null) return null;
            return ProjectStatus.fromCode(val);
        }
    }

}
