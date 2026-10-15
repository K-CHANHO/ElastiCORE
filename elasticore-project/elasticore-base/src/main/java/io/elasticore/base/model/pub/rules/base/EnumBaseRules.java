package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EnumSpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Lines;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.util.CodeStringBuilder;
import io.elasticore.base.util.StringList;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * Base rules for the enumeration artifact, ported from EnumFilePublisher.
 *
 * The generated members are built with the same CodeStringBuilder and
 * StringList the publisher uses, so their layout is the layout 2.x produces
 * rather than a re-derivation of it.
 *
 * An enum earns its extra members from its meta annotations: @json names the
 * field that crosses the JSON boundary and brings the Jackson serializer
 * pair, @db names the field stored in the column and brings the JPA
 * converter. Naming the same field for both produces one lookup serving both.
 */
public class EnumBaseRules implements CodeRules {

    /** Constants sit one per line, the comma leading, as 2.x writes them. */
    private static final String NEXT_CONSTANT = Lines.of("", "    ,");

    /** Members are closed and then continue on a line of their own. */
    private static final String NEXT_MEMBER = Lines.of(";", "    ");

    @Rule(value = "enum-vars", target = "enum", order = 10)
    void vars(EnumSpec e) {
        ctx(e).var("packageName", e.pkg("enumeration"))
              .var("className", e.getName())
              .var("j2eePkgName", "jakarta");
    }

    /** The constants, and the constructor the declared fields imply. */
    @Rule(value = "enum-constants", target = "enum", order = 20)
    void constants(EnumSpec e) {
        StringList constantLine = StringList.create(NEXT_CONSTANT);
        for (EnumSpec.Constant c : e.constants()) {
            StringList one = StringList.create(",");
            one.append(c.name()).append("(");
            for (int i = 0; i < c.values().size() && i < e.fields().size(); i++) {
                one.add(literal(c.values().get(i), e.fields().get(i)));
            }
            one.append(")");
            constantLine.add(one);
        }
        constantLine.append(";");

        StringList fieldLine = StringList.create(NEXT_MEMBER, ";");
        StringList argLine = StringList.create(",");
        StringList paramLine = StringList.create(NEXT_MEMBER, ";");

        for (FieldSpec f : e.fields()) {
            fieldLine.add(String.format("private final %s %s", f.typeName(), f.name()));
            argLine.add(String.format("%s %s", f.typeName(), f.name()));
            paramLine.add(String.format("this.%s = %s", f.name(), f.name()));
        }

        ctx(e).var("enumConstant", constantLine.toString())
              .var("fieldLine", fieldLine.toString())
              .var("argLine", argLine.toString())
              .var("paramLine", paramLine.toString());
    }

    /** A value declared for a string field is quoted unless it already is. */
    private String literal(String value, FieldSpec field) {
        if (!field.isStringType()) return value;
        if (value.indexOf('"') == 0) return value;
        return RuleText.quote(value);
    }

    /**
     * The lookup methods and the converters they support, plus the imports and
     * class annotations they require.
     */
    @Rule(value = "enum-converters", target = "enum", order = 30)
    void converters(EnumSpec e) {
        String enumName = e.getName();
        CodeStringBuilder body = new CodeStringBuilder("{", "}");
        CodeStringBuilder imports = new CodeStringBuilder();
        CodeStringBuilder classAnnotations = new CodeStringBuilder();

        allEnumInfo(body, e);

        FieldSpec jsonField = e.field(e.jsonFieldName());
        FieldSpec dbField = e.field(e.dbFieldName());
        boolean needsJson = false;
        boolean needsDb = false;

        if (jsonField != null || dbField != null) {
            boolean sameField = jsonField != null && dbField != null
                    && e.jsonFieldName().equals(e.dbFieldName());
            if (sameField) {
                lookup(body, enumName, jsonField, true, true);
                needsJson = true;
                needsDb = true;
            } else {
                if (jsonField != null) {
                    lookup(body, enumName, jsonField, true, false);
                    needsJson = true;
                }
                if (dbField != null) {
                    lookup(body, enumName, dbField, false, true);
                    needsDb = true;
                }
            }
        }

        imports.line("import java.util.Map;");
        imports.line("import java.util.HashMap;");
        imports.line("import java.util.List;");
        imports.line("import java.util.ArrayList;");

        if (needsJson && io.elasticore.base.model.pub.rules.RulesConfig.jackson3()) {
            // Spring Boot 4 reads JSON with Jackson 3, which looks past Jackson
            // 2's databind annotations: the enum would go out by its name.
            // These two live in jackson-annotations, the same package in both.
            imports.line("import com.fasterxml.jackson.annotation.JsonCreator;")
                   .line("import com.fasterxml.jackson.annotation.JsonValue;");
        } else if (needsJson) {
            imports.line("import com.fasterxml.jackson.core.JsonGenerator;")
                   .line("import com.fasterxml.jackson.core.JsonParser;")
                   .line("import com.fasterxml.jackson.core.JsonProcessingException;")
                   .line("import com.fasterxml.jackson.databind.DeserializationContext;")
                   .line("import com.fasterxml.jackson.databind.JsonDeserializer;")
                   .line("import com.fasterxml.jackson.databind.JsonSerializer;")
                   .line("import com.fasterxml.jackson.databind.SerializerProvider;")
                   .line("import com.fasterxml.jackson.databind.annotation.JsonDeserialize;")
                   .line("import com.fasterxml.jackson.databind.annotation.JsonSerialize;");

            classAnnotations.line("@JsonSerialize(using = %s.CustomSerializer.class)", enumName)
                            .line("@JsonDeserialize(using = %s.CustomDeserializer.class)", enumName);
        }

        if (needsDb) {
            imports.line("import jakarta.persistence.AttributeConverter;")
                   .line("import jakarta.persistence.Converter;")
                   .line("import java.io.IOException;");
        }

        ctx(e).var("extraLine", body.toString())
              .var("extraImportList", imports.toString())
              .var("classAnnotationList", classAnnotations.toString());
    }

    /** The reflective listing every generated enum carries. */
    private void allEnumInfo(CodeStringBuilder cb, EnumSpec e) {
        String enumName = e.getName();
        cb.block("");
        cb.line("public static List<Map> getAllEnumInfo()").block();
        cb.line("List<Map> list = new ArrayList();");
        cb.line("for(%s i:%s.class.getEnumConstants())", enumName, enumName).block();
        cb.line("HashMap<String,Object> map = new HashMap();");
        cb.line("map.put(\"_name\", i.name());");
        for (FieldSpec f : e.fields()) {
            cb.line("map.put(%s, i.get%s());", RuleText.quote(f.name()), f.capitalName());
        }
        cb.line("list.add(map);");
        cb.end();
        cb.line("return list;");
        cb.end();
        cb.end("");
    }

    /**
     * The from&lt;Field&gt; lookup, and whichever of the Jackson pair and the
     * JPA converter the caller asked for.
     */
    private void lookup(CodeStringBuilder cb, String enumName, FieldSpec f,
                        boolean withJson, boolean withDb) {
        cb.block("");

        String fieldName = f.name();
        String type = f.typeName();
        String capitalNm = f.capitalName();

        boolean jackson3 = withJson && io.elasticore.base.model.pub.rules.RulesConfig.jackson3();
        if (jackson3) cb.line("@JsonCreator(mode = JsonCreator.Mode.DELEGATING)");
        cb.line("public static %s from%s(%s %s)", enumName, capitalNm, type, fieldName).block();
        cb.line("if(%s ==null) return null;", fieldName);
        if ("String".equals(type)) {
            cb.line("if(%s.isEmpty()) return null;", fieldName);
        }
        cb.line("for (%s type : %s.values())", enumName, enumName).block();
        cb.line("if (type.%s.equals(%s)) return type;", fieldName, fieldName);
        cb.end();
        cb.line("throw new IllegalArgumentException(\"Unknown %s %s: \" + %s);",
                enumName, fieldName, fieldName);
        cb.end();

        if (jackson3) {
            jsonValue(cb, type, fieldName);
            propertyEditor(cb, enumName, capitalNm);
        } else if (withJson) {
            jsonMembers(cb, enumName, capitalNm);
        }
        if (withDb) {
            dbConverter(cb, enumName, type, capitalNm);
        }

        cb.end("");
    }

    private void jsonMembers(CodeStringBuilder cb, String enumName, String capitalNm) {
        cb.line("");
        cb.line("public static class CustomSerializer extends JsonSerializer<%s>", enumName);
        cb.block();
        cb.line("@Override");
        cb.line("public void serialize(%s value, JsonGenerator gen, SerializerProvider serializers) throws IOException",
                enumName);
        cb.block();
        cb.line("gen.writeString(value.get%s());", capitalNm);
        cb.end();
        cb.end();

        cb.line("");
        cb.line("public static class CustomDeserializer extends JsonDeserializer<%s>", enumName);
        cb.block();
        cb.line("@Override");
        cb.line("public %s deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException",
                enumName);
        cb.block();
        cb.line("return %s.from%s(p.getText());", enumName, capitalNm);
        cb.end();
        cb.end();

        propertyEditor(cb, enumName, capitalNm);
    }

    /** What the enum is written as in JSON, for Jackson 2 and 3 alike. */
    private void jsonValue(CodeStringBuilder cb, String type, String fieldName) {
        cb.line("");
        cb.line("@JsonValue");
        cb.line("public %s jsonValue()", type);
        cb.block();
        cb.line("return this.%s;", fieldName);
        cb.end();
    }

    /** Binds a request parameter or path variable written as the field's value. */
    private void propertyEditor(CodeStringBuilder cb, String enumName, String capitalNm) {
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

    private void dbConverter(CodeStringBuilder cb, String enumName, String type, String capitalNm) {
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
}
