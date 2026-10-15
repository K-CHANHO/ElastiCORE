//ecd:-520884569H20260207233343_V1.0
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
 * DeptStatus
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = DeptStatus.CustomSerializer.class)
@JsonDeserialize(using = DeptStatus.CustomDeserializer.class)
@Getter
public enum DeptStatus {

    ACTIVE("A","Active")
    ,INACTIVE("I","Inactive")
    ,MERGED("M","Merged");

    private final String code;
    private final String name;

    DeptStatus(String code,String name) {
        this.code = code;
    this.name = name;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(DeptStatus i:DeptStatus.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("name", i.getName());
            list.add(map);
        }
        return list;
    }

    public static DeptStatus fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (DeptStatus type : DeptStatus.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown DeptStatus code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<DeptStatus>{
        @Override
        public void serialize(DeptStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<DeptStatus>{
        @Override
        public DeptStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return DeptStatus.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(DeptStatus.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<DeptStatus, String>{
        @Override
        public String convertToDatabaseColumn(DeptStatus e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public DeptStatus convertToEntityAttribute(String val){
            if (val == null) return null;
            return DeptStatus.fromCode(val);
        }
    }

}
