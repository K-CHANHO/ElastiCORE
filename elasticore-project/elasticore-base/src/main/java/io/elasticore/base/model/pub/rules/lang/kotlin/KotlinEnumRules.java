package io.elasticore.base.model.pub.rules.lang.kotlin;

import io.elasticore.base.model.pub.rules.base.RuleText;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EnumSpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Lines;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.Syntax;
import io.elasticore.base.util.CodeStringBuilder;
import io.elasticore.base.util.StringList;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The enumeration artifact in Kotlin (kt/enum.tmpl).
 *
 * Not a file shell over shared rules like the entity and DTO: an enum's
 * members are code, and the two languages state them differently enough that
 * there is nothing left to share. Kotlin declares the fields in the class
 * header rather than as a constructor and a field block, puts the static
 * members in a companion object, and needs no explicit accessors - so the
 * Java rule's fieldLine, paramLine and get* calls have no counterpart here.
 *
 * What is the same is which members an enum earns: @json names the field that
 * crosses the JSON boundary and brings the Jackson pair, @db names the field
 * stored in the column and brings the JPA converter.
 */
public class KotlinEnumRules implements CodeRules {

    /** Constants sit one per line, the comma leading, as the Java rule writes them. */
    private static final String NEXT_CONSTANT = Lines.of("", "    ,");

    @Rule(value = "kt-enum-vars", target = "enum", order = 10)
    void vars(EnumSpec e) {
        ctx(e).requireSyntax(Syntax.KOTLIN, "kt-enum-vars");
        ctx(e).var("packageName", e.pkg("enumeration"))
              .var("className", e.getName())
              .var("j2eePkgName", "jakarta");
    }

    /**
     * The constants, and the header that declares what they carry. A Kotlin
     * enum names its properties in the class header, so one list serves where
     * Java needs a field block, a parameter list and an assignment block.
     */
    @Rule(value = "kt-enum-constants", target = "enum", order = 20)
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

        StringList argLine = StringList.create(", ");
        for (FieldSpec f : e.fields()) {
            argLine.add(String.format("val %s: %s", propertyName(f.name()),
                    io.elasticore.base.model.pub.rules.Syntax.KOTLIN.typeName(f.typeName())));
        }

        ctx(e).var("enumConstant", constantLine.toString())
              .var("argLine", argLine.toString());
    }

    /**
     * The property name a field takes on the Kotlin enum.
     *
     * Every Kotlin enum entry already has name and ordinal from Enum, and
     * neither can be overridden - so a DSL field called one of them has to be
     * declared under another name. Java has the same two members but reaches
     * them through getName(), so it never collides and keeps the DSL's name.
     */
    static String propertyName(String fieldName) {
        if ("name".equals(fieldName) || "ordinal".equals(fieldName)) {
            return String.format("%sValue", fieldName);
        }
        return fieldName;
    }

    /** A value declared for a string field is quoted unless it already is. */
    private String literal(String value, FieldSpec field) {
        if (!field.isStringType()) return value;
        if (value.indexOf('"') == 0) return value;
        return RuleText.quote(value);
    }

    /**
     * The lookup functions and the converters they support, plus the imports
     * and class annotations they require.
     */
    @Rule(value = "kt-enum-converters", target = "enum", order = 30)
    void converters(EnumSpec e) {
        String enumName = e.getName();
        CodeStringBuilder body = new CodeStringBuilder("{", "}");
        CodeStringBuilder imports = new CodeStringBuilder();
        CodeStringBuilder classAnnotations = new CodeStringBuilder();

        FieldSpec jsonField = e.field(e.jsonFieldName());
        FieldSpec dbField = e.field(e.dbFieldName());
        boolean sameField = jsonField != null && dbField != null
                && e.jsonFieldName().equals(e.dbFieldName());
        boolean needsJson = jsonField != null;
        boolean needsDb = dbField != null;

        companion(body, e, jsonField, dbField, sameField);

        if (needsJson && io.elasticore.base.model.pub.rules.RulesConfig.jackson3()) {
            // Spring Boot 4 reads JSON with Jackson 3, which looks past Jackson
            // 2's databind annotations; these two are the same in both.
            jsonValue(body, jsonField);
            imports.line("import com.fasterxml.jackson.annotation.JsonCreator")
                   .line("import com.fasterxml.jackson.annotation.JsonValue");
        } else if (needsJson) {
            jsonMembers(body, enumName, jsonField);
            imports.line("import com.fasterxml.jackson.core.JsonGenerator")
                   .line("import com.fasterxml.jackson.core.JsonParser")
                   .line("import com.fasterxml.jackson.databind.DeserializationContext")
                   .line("import com.fasterxml.jackson.databind.JsonDeserializer")
                   .line("import com.fasterxml.jackson.databind.JsonSerializer")
                   .line("import com.fasterxml.jackson.databind.SerializerProvider")
                   .line("import com.fasterxml.jackson.databind.annotation.JsonDeserialize")
                   .line("import com.fasterxml.jackson.databind.annotation.JsonSerialize");

            classAnnotations.line("@JsonSerialize(using = %s.CustomSerializer::class)", enumName)
                            .line("@JsonDeserialize(using = %s.CustomDeserializer::class)", enumName);
        }

        if (needsDb) {
            dbConverter(body, enumName, dbField);
            imports.line("import jakarta.persistence.AttributeConverter")
                   .line("import jakarta.persistence.Converter");
        }

        ctx(e).var("extraLine", body.toString())
              .var("extraImportList", imports.toString())
              .var("classAnnotationList", classAnnotations.toString());
    }

    /**
     * The static members, which Kotlin keeps in a companion object: the
     * reflective listing every generated enum carries, and one lookup per
     * field that names itself across a boundary.
     */
    private void companion(CodeStringBuilder cb, EnumSpec e,
                           FieldSpec jsonField, FieldSpec dbField, boolean sameField) {
        String enumName = e.getName();

        cb.line("companion object").block();

        cb.line("@JvmStatic");
        cb.line("fun getAllEnumInfo(): List<Map<String, Any?>>").block();
        cb.line("val list = ArrayList<Map<String, Any?>>()");
        cb.line("for (i in values())").block();
        cb.line("val map = HashMap<String, Any?>()");
        cb.line("map[\"_name\"] = i.name");
        for (FieldSpec f : e.fields()) {
            cb.line("map[%s] = i.%s", RuleText.quote(f.name()), propertyName(f.name()));
        }
        cb.line("list.add(map)");
        cb.end();
        cb.line("return list");
        cb.end();

        if (jsonField != null) lookup(cb, enumName, jsonField,
                io.elasticore.base.model.pub.rules.RulesConfig.jackson3());
        if (dbField != null && !sameField) lookup(cb, enumName, dbField, false);

        cb.end();
    }

    /** The from&lt;Field&gt; lookup of one field. */
    private void lookup(CodeStringBuilder cb, String enumName, FieldSpec f, boolean creator) {
        String name = propertyName(f.name());
        String type = io.elasticore.base.model.pub.rules.Syntax.KOTLIN.typeName(f.typeName());

        cb.line("");
        if (creator) cb.line("@JsonCreator(mode = JsonCreator.Mode.DELEGATING)");
        cb.line("@JvmStatic");
        cb.line("fun from%s(%s: %s?): %s?", f.capitalName(), name, type, enumName).block();
        cb.line("if (%s == null) return null", name);
        if ("String".equals(type)) {
            cb.line("if (%s.isEmpty()) return null", name);
        }
        cb.line("for (type in values())").block();
        cb.line("if (type.%s == %s) return type", name, name);
        cb.end();
        cb.line("throw IllegalArgumentException(\"Unknown %s %s: $%s\")", enumName, name, name);
        cb.end();
    }

    /** What the enum is written as in JSON, for Jackson 2 and 3 alike. */
    private void jsonValue(CodeStringBuilder cb, FieldSpec f) {
        String type = io.elasticore.base.model.pub.rules.Syntax.KOTLIN.typeName(f.typeName());
        cb.line("");
        cb.line("@JsonValue");
        cb.line("fun jsonValue(): %s = %s", type, propertyName(f.name()));
    }

    /** The Jackson pair, as nested classes of the enum. */
    private void jsonMembers(CodeStringBuilder cb, String enumName, FieldSpec f) {
        String name = f.name();

        cb.line("");
        cb.line("class CustomSerializer : JsonSerializer<%s>()", enumName).block();
        cb.line("override fun serialize(value: %s, gen: JsonGenerator, serializers: SerializerProvider)",
                enumName).block();
        cb.line("gen.writeString(value.%s)", propertyName(name));
        cb.end();
        cb.end();

        cb.line("");
        cb.line("class CustomDeserializer : JsonDeserializer<%s>()", enumName).block();
        cb.line("override fun deserialize(p: JsonParser, ctxt: DeserializationContext): %s?",
                enumName).block();
        cb.line("return from%s(p.text)", f.capitalName());
        cb.end();
        cb.end();
    }

    /**
     * The JPA converter. The Java rule calls its parameter "val", which is a
     * keyword here, so it is named for what it holds instead.
     */
    private void dbConverter(CodeStringBuilder cb, String enumName, FieldSpec f) {
        String type = io.elasticore.base.model.pub.rules.Syntax.KOTLIN.typeName(f.typeName());

        cb.line("");
        cb.line("@Converter(autoApply = true)");
        cb.line("class EntityConverter : AttributeConverter<%s, %s>", enumName, type).block();

        cb.line("override fun convertToDatabaseColumn(attribute: %s?): %s?", enumName, type).block();
        cb.line("return attribute?.%s", propertyName(f.name()));
        cb.end();

        cb.line("");
        cb.line("override fun convertToEntityAttribute(dbData: %s?): %s?", type, enumName).block();
        cb.line("return from%s(dbData)", f.capitalName());
        cb.end();

        cb.end();
    }
}
