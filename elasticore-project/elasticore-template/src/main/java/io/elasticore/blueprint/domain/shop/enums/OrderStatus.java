//ecd:1820417352H20260207233345_V1.0
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
 * OrderStatus
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = OrderStatus.CustomSerializer.class)
@JsonDeserialize(using = OrderStatus.CustomDeserializer.class)
@Getter
public enum OrderStatus {

    PENDING("PEN","Pending")
    ,CONFIRMED("CFM","Confirmed")
    ,PROCESSING("PRC","Processing")
    ,SHIPPED("SHP","Shipped")
    ,DELIVERED("DLV","Delivered")
    ,CANCELLED("CAN","Cancelled")
    ,REFUNDED("RFD","Refunded");

    private final String code;
    private final String name;

    OrderStatus(String code,String name) {
        this.code = code;
    this.name = name;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(OrderStatus i:OrderStatus.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("name", i.getName());
            list.add(map);
        }
        return list;
    }

    public static OrderStatus fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (OrderStatus type : OrderStatus.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<OrderStatus>{
        @Override
        public void serialize(OrderStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<OrderStatus>{
        @Override
        public OrderStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return OrderStatus.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(OrderStatus.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<OrderStatus, String>{
        @Override
        public String convertToDatabaseColumn(OrderStatus e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public OrderStatus convertToEntityAttribute(String val){
            if (val == null) return null;
            return OrderStatus.fromCode(val);
        }
    }

}
