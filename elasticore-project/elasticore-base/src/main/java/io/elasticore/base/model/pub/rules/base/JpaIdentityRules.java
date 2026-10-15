package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;
import io.elasticore.base.util.CodeTemplate;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The composite primary key class an entity's @IdClass names, in Java.
 *
 * Ported from EntitySrcFilePublisher.publishEntityIdentityClass: the entity's
 * key fields with their description and @Column, and nothing else - no @Id,
 * which belongs on the entity's own fields. Lombok supplies equals and
 * hashCode, which JPA compares keys by.
 *
 * The 2.x-compatible file renders through the entity skeleton, as 2.x did;
 * the v3 one through a skeleton that imports only what it uses.
 */
public class JpaIdentityRules extends JpaEntityBaseRules {

    @Rule(value = "jpa-identity-desc", target = "identity", order = 5)
    void identityDesc(EntitySpec e, FieldSpec f) {
        if (isKey(f)) fieldDesc(e, f);
    }

    @Rule(value = "jpa-identity-column", target = "identity", order = 30)
    void identityColumn(EntitySpec e, FieldSpec f) {
        if (isKey(f)) column(e, f);
    }

    @Rule(value = "jpa-identity-file", target = "identity", order = 90, phase = Rule.Phase.TAIL)
    void file(EntitySpec e) {
        SourceCode c = ctx(e);
        c.requireSyntax(Syntax.JAVA, "jpa-identity-file");

        if (e.pkCount() <= 1) {
            c.dontMake();
            return;
        }
        for (FieldSpec f : e.fields()) {
            if (!isKey(f)) c.removeField(f);
        }

        String className = String.format("%sIdentity", e.getName());
        c.var("packageName", e.pkg("entity"))
         .var("className", className);

        if (c.compatible()) {
            // The entity skeleton, filled the way 2.x filled it for the key.
            CodeTemplate.Paragraph annotations = c.paragraph("classAnnotationList");
            annotations.add((Object) "@Embeddable");
            annotations.add((Object) "@lombok.EqualsAndHashCode");
            annotations.add((Object) "@AllArgsConstructor");
            c.template("entity")
             .var("j2eePkgName", "jakarta")
             .var("abstract", "")
             .var("extendInfo", "")
             .var("implementInfo", "implements java.io.Serializable");
            CodeTemplate.Paragraph body = c.paragraph("fieldList");
            for (String line : RuleText.linesOf(c.renderFieldBlock(1))) {
                body.add((Object) line);
            }
            return;
        }

        c.ensureClassAnnotation(AnnoCode.of("jakarta.persistence.Embeddable"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.Getter"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.Setter"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.NoArgsConstructor"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.AllArgsConstructor"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.EqualsAndHashCode"));
        for (FieldSpec f : e.fields()) {
            if (isKey(f)) RuleText.registerFieldTypeImport(c, e, f, e.pkg("entity"));
        }
        c.var("entityClassName", e.getName())
         .var("classAnnotationList", c.renderClassAnnotations())
         .var("importList", RuleText.stripTrailing(c.renderImports()))
         .varLines("fieldList", RuleText.linesOf(RuleText.stripTrailing(c.renderFieldBlock(1))));
    }

    private static boolean isKey(FieldSpec f) {
        return f.hasDsl("id") || f.hasDsl("pk");
    }
}
