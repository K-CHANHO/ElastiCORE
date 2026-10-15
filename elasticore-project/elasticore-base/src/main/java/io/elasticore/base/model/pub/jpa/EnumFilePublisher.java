package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.enums.EnumConstant;
import io.elasticore.base.model.enums.EnumModel;
import io.elasticore.base.util.*;

import java.util.List;

/**
 *
 */
public class EnumFilePublisher extends SrcFilePublisher {

    private CodeTemplate javaClassTmpl;

    private CodePublisher publisher;

    private String packageName;

    public EnumFilePublisher(CodePublisher publisher) {
        super(publisher);

        this.publisher = publisher;

        String templatePath = this.publisher.getECoreModelContext().getDomain().getModel().getConfig("template.enum");
        if (templatePath == null)
            templatePath = "elasticore-template/enum.tmpl";
        else
            templatePath = "resource://" + templatePath;

        this.javaClassTmpl = CodeTemplate.newInstance(templatePath);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        this.packageName = model.getNamespace(ConstantParam.KEYNAME_ENUMERATION);
    }


    private String getValue(EnumConstant.ConstructParam param, Field f) {
        String baseVal = param.toString();
        if (f.getTypeInfo().isStringType()) {
            if (baseVal.indexOf("\"") == 0) {
                return baseVal;
            }
            return "\"" + baseVal + "\"";
        }
        return baseVal;

    }


    private void makeConverFunc(CodeStringBuilder cb, CodeStringBuilder importInfo, CodeStringBuilder classAnnotationList,
                                ModelDomain domain, EnumModel enumModel) {

        String enumName = enumModel.getIdentity().getName();

        Items<Field> fieldItem = enumModel.getFieldItems();
        List<Field> list = fieldItem.getItemList();

        Annotation jsonAnt = enumModel.getMetaInfo().getMetaAnnotation("json");
        Annotation dbAnt = enumModel.getMetaInfo().getMetaAnnotation("db");

        Field jsonField = null;
        Field dbField = null;
        String jsonFieldNm = null;
        String dbFieldNm = null;
        if(jsonAnt!=null) {
            jsonFieldNm = jsonAnt.getValue();
            if(jsonFieldNm!=null)
                jsonField = fieldItem.findByName(jsonFieldNm);

        }
        if(dbAnt!=null) {
            dbFieldNm = dbAnt.getValue();
            if(dbFieldNm!=null)
                dbField = fieldItem.findByName(dbFieldNm);
        }

        makeEnumInfo(cb, enumModel);

        boolean isJsonImport = false;
        boolean isDbImport = false;

        if(jsonField!=null || dbField !=null) {
            boolean isSame = false;
            if(jsonField !=null && dbField !=null && jsonFieldNm.equals(dbFieldNm)) {
                makeFindEnumCode(cb, enumName, jsonField, true, true);
                isJsonImport = true;
                isDbImport = true;
                isSame = true;
            }

            if(!isSame) {
                if(jsonField !=null) {
                    makeFindEnumCode(cb, enumName, jsonField, true, false);
                    isJsonImport = true;

                }
                if(dbField !=null) {
                    makeFindEnumCode(cb, enumName, dbField, false, true);
                    isDbImport = true;
                }
            }


        }

        importInfo.line("import java.util.Map;");
        importInfo.line("import java.util.HashMap;");

        importInfo.line("import java.util.List;");
        importInfo.line("import java.util.ArrayList;");

        if(isJsonImport && io.elasticore.base.model.pub.rules.RulesConfig.jackson3()) {
            // Spring Boot 4 reads JSON with Jackson 3, which looks past Jackson
            // 2's databind annotations; these two are the same in both.
            importInfo.line("import com.fasterxml.jackson.annotation.JsonCreator;")
                    .line("import com.fasterxml.jackson.annotation.JsonValue;");
        } else if(isJsonImport) {
            importInfo.line("import com.fasterxml.jackson.core.JsonGenerator;")
                    .line("import com.fasterxml.jackson.core.JsonParser;")
                    .line("import com.fasterxml.jackson.core.JsonProcessingException;")
                    .line("import com.fasterxml.jackson.databind.DeserializationContext;")
                    .line("import com.fasterxml.jackson.databind.JsonDeserializer;")
                    .line("import com.fasterxml.jackson.databind.JsonSerializer;")
                    .line("import com.fasterxml.jackson.databind.SerializerProvider;")
                    .line("import com.fasterxml.jackson.databind.annotation.JsonDeserialize;")
                    .line("import com.fasterxml.jackson.databind.annotation.JsonSerialize;");


            classAnnotationList.line("@JsonSerialize(using = %s.CustomSerializer.class)",enumName)
                    .line("@JsonDeserialize(using = %s.CustomDeserializer.class)",enumName);
        }


        if(isDbImport) {
            String j2eePkgName = this.getPersistentPackageName(domain);
            importInfo.line("import %s.persistence.AttributeConverter;", j2eePkgName)
                    .line("import %s.persistence.Converter;", j2eePkgName)
                    .line("import java.io.IOException;");
        }

    }


    private void makeEnumInfo(CodeStringBuilder cb, EnumModel enumModel ) {
        String enumName = enumModel.getIdentity().getName();

        cb.block("");


        cb.line("public static List<Map> getAllEnumInfo()").block();
        cb.line("List<Map> list = new ArrayList();");
        cb.line("for(%s i:%s.class.getEnumConstants())",enumName,enumName).block();
        cb.line("HashMap<String,Object> map = new HashMap();");
        cb.line("map.put(\"_name\", i.name());");

        for(Field field:enumModel.getFieldItems().getItemList()) {
            String name = field.getIdentity().getName();
            cb.line("map.put(\"%s\", i.get%s());" ,name ,StringUtils.capitalize(name));
        }

        cb.line("list.add(map);");

        cb.end();
        cb.line("return list;");
        cb.end();

        cb.end("");
    }

    /**
     * public static AaccidentType fromCode(String code) {
     * for (AaccidentType type : AaccidentType.values()) {
     * if (type.code.equals(code))
     * return type;
     * <p>
     * }
     * throw new IllegalArgumentException("Unknown code: " + code);
     * }
     *
     * @param cb
     * @param enumName
     * @param f
     */
    private void makeFindEnumCode(CodeStringBuilder cb, String enumName, Field f, boolean isJsonConv, boolean isDbConv) {

        cb.block("");

        String fieldName = f.getIdentity().getName();
        String type = f.getTypeInfo().getDefaultTypeName();

        String capitalNm = StringUtils.capitalize(fieldName);


        boolean jackson3 = isJsonConv && io.elasticore.base.model.pub.rules.RulesConfig.jackson3();
        if (jackson3) cb.line("@JsonCreator(mode = JsonCreator.Mode.DELEGATING)");
        cb.line("public static %s from%s(%s %s)", enumName, capitalNm, type, fieldName).block();
        cb.line("if(%s ==null) return null;", fieldName);
        if("String".equals(type)) {
            cb.line("if(%s.isEmpty()) return null;", fieldName);
        }

        cb.line("for (%s type : %s.values())", enumName, enumName).block();
        cb.line("if (type.%s.equals(%s)) return type;", fieldName, fieldName);
        cb.end();
        cb.line("throw new IllegalArgumentException(\"Unknown %s %s: \" + %s);", enumName, fieldName, fieldName);
        cb.end();

        if (jackson3) {
            cb.line("");
            cb.line("@JsonValue");
            cb.line("public %s jsonValue()", type);
            cb.block();
            cb.line("return this.%s;", fieldName);
            cb.end();
        }

        if (isJsonConv && !jackson3) {
            cb.line("");

            cb.line("public static class CustomSerializer extends JsonSerializer<%s>", enumName);
            cb.block();
            cb.line("@Override");
            cb.line("public void serialize(%s value, JsonGenerator gen, SerializerProvider serializers) throws IOException", enumName);
            cb.block();
            cb.line("gen.writeString(value.get%s());", capitalNm);
            cb.end();
            cb.end();

            cb.line("");
            cb.line("public static class CustomDeserializer extends JsonDeserializer<%s>", enumName);
            cb.block();
            cb.line("@Override");
            cb.line("public %s deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException", enumName);
            cb.block();
            cb.line("return %s.from%s(p.getText());", enumName, capitalNm);
            cb.end();
            cb.end();
        }

        if (isJsonConv) {
            cb.line("");
            cb.line("public static class PropertyEditor extends java.beans.PropertyEditorSupport");
            cb.block();
            cb.line("@Override");
            cb.line("public void setAsText(String txt) throws IllegalArgumentException");
            cb.block();
            cb.line("if(txt!=null && !txt.isEmpty())");
            cb.line("  setValue(%s.from%s(txt));", enumName, capitalNm);
            cb.end();
            cb.end();
        }

        if (isDbConv) {
            cb.line("");

            cb.line("@Converter(autoApply = true)");
            cb.line("public static class EntityConverter implements AttributeConverter<%s, %s>", enumName, type);
            cb.block();
            cb.line("@Override");
            cb.line("public %s convertToDatabaseColumn(%s e)", type, enumName);
            cb.block();
            cb.line("if (e == null) return null;");
            cb.line("return e.get%s();", capitalNm);
            cb.end();

            cb.line("");
            cb.line("@Override");
            cb.line("public %s convertToEntityAttribute(%s val)", enumName, type);
            cb.block();
            cb.line("if (val == null) return null;");
            cb.line("return %s.from%s(val);", enumName, capitalNm);
            cb.end();


            cb.end();
        }

        cb.end("");

    }

    public void publish(ModelDomain domain, EnumModel enumModel) {
        if (this.packageName == null) return;

        Items<EnumConstant> enumConstantItems = enumModel.getEnumConstantItems();

        if(enumConstantItems==null) {
            ConsoleLog.printWarn("No enumConstantItems found "+enumModel.getIdentity().getName());
            return;
        }

        Items<Field> fieldItem = enumModel.getFieldItems();

        StringList sbLine = StringList.create("\n    ,");
        for (EnumConstant enumConstant : enumConstantItems.getItemList()) {
            String name = enumConstant.getIdentity().getName();

            StringList sb = StringList.create(",");

            sb.append(name).append("(");

            try {
                int seq = 0;
                for (EnumConstant.ConstructParam param : enumConstant.getConstructParamList()) {
                    String val = getValue(param, fieldItem.getItemList().get(seq));
                    sb.add(val);
                    seq++;
                }

                sb.append(")");
                sbLine.add(sb);
            }catch (RuntimeException re) {
                ConsoleLog.printWarn("enum error. name: "+name);
                continue;
            }
        }
        sbLine.append(";");

        StringList fieldLine = StringList.create(";\n    ", ";");

        StringList argLine = StringList.create(",");
        StringList paramLine = StringList.create(";\n    ", ";");


        for (Field f : fieldItem.getItemList()) {
            String fieldTypeNm = f.getTypeInfo().getDefaultTypeName();
            fieldLine.add("private final " + fieldTypeNm + " " + f.getName());

            argLine.add(fieldTypeNm + " " + f.getName());

            paramLine.add("this." + f.getName() + " = " + f.getName());

        }

        CodeStringBuilder classAnnotationList = new CodeStringBuilder();
        CodeStringBuilder extraImportInfos = new CodeStringBuilder();
        CodeStringBuilder cb = new CodeStringBuilder("{", "}");
        makeConverFunc(cb, extraImportInfos, classAnnotationList, domain, enumModel);


        String j2eePkgName = this.getPersistentPackageName(domain);

        String classNm = enumModel.getIdentity().getName();

        CodeTemplate.Parameters p = CodeTemplate.newParameters();
        p
                .set("packageName", packageName)
                .set("className", classNm)
                .set("enumConstant", sbLine.toString())
                .set("classAnnotationList", classAnnotationList.toString())
                .set("fieldLine", fieldLine.toString())
                .set("argLine", argLine.toString())
                .set("extraLine", cb.toString())
                .set("j2eePkgName", j2eePkgName)
                .set("extraImportList", extraImportInfos.toString())
                .set("paramLine", paramLine.toString());


        String code = javaClassTmpl.toString(p);
        String qualifiedClassName = packageName + "." + classNm;

        this.writeSrcCode(this.publisher, enumModel, qualifiedClassName, code);
    }
}
