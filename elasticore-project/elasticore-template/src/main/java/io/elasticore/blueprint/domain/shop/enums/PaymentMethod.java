//ecd:1665467970H20260207233345_V1.0
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
 * PaymentMethod
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = PaymentMethod.CustomSerializer.class)
@JsonDeserialize(using = PaymentMethod.CustomDeserializer.class)
@Getter
public enum PaymentMethod {

    CREDIT_CARD("CC","Credit Card")
    ,DEBIT_CARD("DC","Debit Card")
    ,BANK_TRANSFER("BT","Bank Transfer")
    ,PAYPAL("PP","PayPal")
    ,CASH("CA","Cash");

    private final String code;
    private final String name;

    PaymentMethod(String code,String name) {
        this.code = code;
    this.name = name;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(PaymentMethod i:PaymentMethod.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("name", i.getName());
            list.add(map);
        }
        return list;
    }

    public static PaymentMethod fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (PaymentMethod type : PaymentMethod.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown PaymentMethod code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<PaymentMethod>{
        @Override
        public void serialize(PaymentMethod value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<PaymentMethod>{
        @Override
        public PaymentMethod deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return PaymentMethod.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(PaymentMethod.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<PaymentMethod, String>{
        @Override
        public String convertToDatabaseColumn(PaymentMethod e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public PaymentMethod convertToEntityAttribute(String val){
            if (val == null) return null;
            return PaymentMethod.fromCode(val);
        }
    }

}
