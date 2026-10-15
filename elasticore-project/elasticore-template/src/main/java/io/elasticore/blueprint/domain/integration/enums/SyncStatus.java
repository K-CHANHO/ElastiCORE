//ecd:-460677074H20260207233344_V1.0
package io.elasticore.blueprint.domain.integration.enums;

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
 * SyncStatus
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = SyncStatus.CustomSerializer.class)
@JsonDeserialize(using = SyncStatus.CustomDeserializer.class)
@Getter
public enum SyncStatus {

    PENDING("PND","Pending")
    ,IN_PROGRESS("IPR","In Progress")
    ,SUCCESS("SUC","Success")
    ,FAILED("FAL","Failed")
    ,SKIPPED("SKP","Skipped");

    private final String code;
    private final String name;

    SyncStatus(String code,String name) {
        this.code = code;
    this.name = name;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(SyncStatus i:SyncStatus.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("name", i.getName());
            list.add(map);
        }
        return list;
    }

    public static SyncStatus fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (SyncStatus type : SyncStatus.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown SyncStatus code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<SyncStatus>{
        @Override
        public void serialize(SyncStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<SyncStatus>{
        @Override
        public SyncStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return SyncStatus.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(SyncStatus.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<SyncStatus, String>{
        @Override
        public String convertToDatabaseColumn(SyncStatus e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public SyncStatus convertToEntityAttribute(String val){
            if (val == null) return null;
            return SyncStatus.fromCode(val);
        }
    }

}
