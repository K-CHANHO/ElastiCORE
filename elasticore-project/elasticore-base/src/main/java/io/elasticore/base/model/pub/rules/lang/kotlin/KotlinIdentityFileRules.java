package io.elasticore.base.model.pub.rules.lang.kotlin;

import io.elasticore.base.model.pub.rules.base.RuleText;
import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.Syntax;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.util.CodeStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The composite primary key class an entity's @IdClass names (kt/identity.tmpl).
 *
 * It is the entity's identity fields and nothing else: the same @Column the
 * entity declares for them, and no @Id, which belongs on the entity's own
 * fields rather than on the key that stands for them.
 *
 * JPA compares keys by value, so the class states equals and hashCode. Java
 * gets them from Lombok's @EqualsAndHashCode, which does nothing for Kotlin,
 * and a data class is not an option here: JPA needs the no-arg constructor
 * that a data class with no property in its header cannot have.
 */
public class KotlinIdentityFileRules implements CodeRules {

    @Rule(value = "kt-identity-file", target = "identity", order = 90, phase = Rule.Phase.TAIL)
    void file(EntitySpec e) {
        SourceCode c = ctx(e);
        c.requireSyntax(Syntax.KOTLIN, "kt-identity-file");

        if (e.pkCount() <= 1) {
            c.dontMake();
            return;
        }

        List<FieldSpec> keys = new ArrayList<FieldSpec>();
        for (FieldSpec f : e.fields()) {
            if (f.hasDsl("id") || f.hasDsl("pk")) {
                keys.add(f);
                // The entity carries @Id; the key class only describes columns.
                c.field(f).removeAnnotation("jakarta.persistence.Id");
            } else {
                c.removeField(f);
            }
        }

        String className = String.format("%sIdentity", e.getName());

        c.ensureClassAnnotation(AnnoCode.of("jakarta.persistence.Embeddable"));

        for (FieldSpec f : keys) {
            RuleText.registerFieldTypeImport(c, e, f, e.pkg("entity"));
        }

        c.var("packageName", e.pkg("entity"))
         .var("className", className)
         .var("entityClassName", e.getName())
         .var("classDeclaration", KotlinClass.declaration(
                 className, "", null, "java.io.Serializable"))
         .var("classAnnotationList", c.renderClassAnnotations())
         .var("importList", RuleText.stripTrailing(c.renderImports()))
         .varLines("fieldList", RuleText.linesOf(RuleText.stripTrailing(c.renderFieldBlock(1))))
         .var("extraLine", constructors(className, keys) + valueEquality(className, keys));
    }

    /**
     * The no-arg constructor JPA needs and the one the service calls with the
     * key's parts. Java gets both from Lombok, which does nothing here.
     */
    private String constructors(String className, List<FieldSpec> keys) {
        CodeStringBuilder cb = new CodeStringBuilder("{", "}");
        cb.line("constructor()");
        cb.line("");
        StringBuilder params = new StringBuilder();
        for (FieldSpec f : keys) {
            if (params.length() > 0) params.append(", ");
            params.append(String.format("%s: %s?", f.name(),
                    io.elasticore.base.model.pub.rules.Syntax.KOTLIN.typeName(f.typeName())));
        }
        cb.line("constructor(%s)", params).block();
        for (FieldSpec f : keys) {
            cb.line("this.%s = %s", f.name(), f.name());
        }
        cb.end();
        cb.line("");
        return cb.toString();
    }

    /** Equality by the key's own values, which is what JPA compares. */
    private String valueEquality(String className, List<FieldSpec> keys) {
        CodeStringBuilder cb = new CodeStringBuilder("{", "}");

        cb.line("override fun equals(other: Any?): Boolean").block();
        cb.line("if (this === other) return true");
        cb.line("if (other !is %s) return false", className);
        for (int i = 0; i < keys.size(); i++) {
            String name = keys.get(i).name();
            cb.line("if (%s != other.%s) return false", name, name);
        }
        cb.line("return true");
        cb.end();

        cb.line("");
        cb.line("override fun hashCode(): Int").block();
        cb.line("return java.util.Objects.hash(%s)", names(keys));
        cb.end();

        return cb.toString();
    }

    private String names(List<FieldSpec> keys) {
        StringBuilder sb = new StringBuilder();
        for (FieldSpec f : keys) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(f.name());
        }
        return sb.toString();
    }
}
