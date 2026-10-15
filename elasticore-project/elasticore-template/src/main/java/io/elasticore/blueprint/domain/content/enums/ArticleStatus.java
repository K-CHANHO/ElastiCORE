//ecd:1551531803H20260207233342_V1.0
package io.elasticore.blueprint.domain.content.enums;

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
 * ArticleStatus
 *
 * Modify this code only as specified in the ElastiCORE guidelines
 * to avoid regeneration conflicts.
 *
 * Generated and managed by ElastiCORE.
 */


@JsonSerialize(using = ArticleStatus.CustomSerializer.class)
@JsonDeserialize(using = ArticleStatus.CustomDeserializer.class)
@Getter
public enum ArticleStatus {

    DRAFT("DRF","Draft")
    ,REVIEW("REV","Review")
    ,PUBLISHED("PUB","Published")
    ,ARCHIVED("ARC","Archived");

    private final String code;
    private final String name;

    ArticleStatus(String code,String name) {
        this.code = code;
    this.name = name;
    }

    
    public static List<Map> getAllEnumInfo(){
        List<Map> list = new ArrayList();
        for(ArticleStatus i:ArticleStatus.class.getEnumConstants()){
            HashMap<String,Object> map = new HashMap();
            map.put("_name", i.name());
            map.put("code", i.getCode());
            map.put("name", i.getName());
            list.add(map);
        }
        return list;
    }

    public static ArticleStatus fromCode(String code){
        if(code ==null) return null;
        if(code.isEmpty()) return null;
        for (ArticleStatus type : ArticleStatus.values()){
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Unknown ArticleStatus code: " + code);
    }
    
    public static class CustomSerializer extends JsonSerializer<ArticleStatus>{
        @Override
        public void serialize(ArticleStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
            gen.writeString(value.getCode());
        }
    }
    
    public static class CustomDeserializer extends JsonDeserializer<ArticleStatus>{
        @Override
        public ArticleStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException{
            return ArticleStatus.fromCode(p.getText());
        }
    }
    
    public static class PropertyEditor extends java.beans.PropertyEditorSupport{
        @Override
        public void setAsText(String txt) throws IllegalArgumentException{
            if(txt!=null && !txt.isEmpty())
              setValue(ArticleStatus.fromCode(txt));
        }
    }
    
    @Converter(autoApply = true)
    public static class EntityConverter implements AttributeConverter<ArticleStatus, String>{
        @Override
        public String convertToDatabaseColumn(ArticleStatus e){
            if (e == null) return null;
            return e.getCode();
        }
        
        @Override
        public ArticleStatus convertToEntityAttribute(String val){
            if (val == null) return null;
            return ArticleStatus.fromCode(val);
        }
    }

}
