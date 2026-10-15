//ecd:-1067063415H20260207233345_V1.0
package io.elasticore.blueprint.domain.shop.enums;

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
 * CustomerType
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = CustomerType.CustomSerializer.class)
@JsonDeserialize(using = CustomerType.CustomDeserializer.class)
@Getter
public enum CustomerType {

    INDIVIDUAL("I","Individual")
    ,BUSINESS("B","Business")
    ,WHOLESALE("W","Wholesale");

    private final String code;
    private final String description;

    CustomerType(String code,String description) {
        this.code = code;
    this.description = description;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(CustomerType i:CustomerType.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("description", i.getDescription());
            list.add(map);
        }
        return list;
    }

    public static CustomerType fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (CustomerType type : CustomerType.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown CustomerType code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<CustomerType>{
        @Override
        public void serialize(CustomerType value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<CustomerType>{
        @Override
        public CustomerType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return CustomerType.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(CustomerType.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<CustomerType, String>{
        @Override
        public String convertToDatabaseColumn(CustomerType e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public CustomerType convertToEntityAttribute(String val){
            if (val == null) return null;
            return CustomerType.fromCode(val);
        }
    }

}
