package io.elasticore.base.model.pub.rules.lang.java;

import io.elasticore.base.model.pub.rules.base.RuleText;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.util.CodeTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The entity class around the field block, ported from the file-level part of
 * EntitySrcFilePublisher. The field annotations come from
 * {@link JpaEntityBaseRules} and the class annotations from
 * {@link JpaEntityClassRules}; these rules name the packages and shape the
 * declaration.
 *
 * This is the 2.x-compatible file. {@link JpaEntityFileRules} writes the v3
 * one, which imports only what it uses.
 */
public class JpaEntityCompatFileRules implements CodeRules {

    @Rule(value = "entity-file", target = "entity", order = 90, phase = Rule.Phase.TAIL)
    void file(EntitySpec e) {
        SourceCode c = ctx(e);

        c.var("packageName", e.pkg("entity"))
         .var("j2eePkgName", "jakarta")
         .var("className", e.getName())
         .var("abstract", e.hasMeta("abstract") ? "abstract" : "")
         .var("extendInfo", extendClause(e))
         .var("implementInfo", "implements java.io.Serializable")
         .var("description", description(e))
         // JpaEntityClassRules collects them as lines rather than as
         // AnnoCode, because 2.x renders several of them multi-line.
         .var("classAnnotationList", RuleText.stripTrailing(c.renderBlock("classAnnotations")))
         ;

        body(e, c);

        if (e.pkg("enumeration") != null) c.var("enumPackageName", e.pkg("enumeration"));

        // The import list 2.x assembles: the referenced-type set the spec
        // carries, the @Entity import when the class annotations declared
        // one, and a cross-domain parent last. Unique, as 2.x's paragraph is.
        List<String> imports = new ArrayList<String>();
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (String imp : e.dtoImports()) {
            if (seen.add(imp)) imports.add(imp);
        }
        if (c.renderBlock("classAnnotations").contains("@Entity")
                && seen.add("jakarta.persistence.Entity")) {
            imports.add("jakarta.persistence.Entity");
        }
        String parentImport = e.parentImport();
        if (parentImport != null && seen.add(parentImport)) {
            imports.add(parentImport);
        }
        if (!imports.isEmpty()) c.varLines("importList", imports);
    }

    /**
     * An entity extends what @extend names, or the entity a rolled-up one
     * folds into.
     */
    private String extendClause(EntitySpec e) {
        String parent = e.parentName();
        if (parent == null || parent.isEmpty()) parent = e.metaValue("rollupTarget");
        return (parent == null || parent.isEmpty()) ? "" : String.format("extends %s", parent);
    }

    /**
     * The description block of the class javadoc. 2.x renders a missing
     * description as a bare continuation line rather than dropping it.
     */
    private String description(EntitySpec e) {
        String label = e.docLabel();
        String desc = e.docDescription();
        if (io.elasticore.base.util.StringUtils.hasValue(label)
                || io.elasticore.base.util.StringUtils.hasValue(desc)) {
            String text = io.elasticore.base.util.StringUtils.fixNull(label)
                    + "\n" + io.elasticore.base.util.StringUtils.fixNull(desc);
            return io.elasticore.base.util.StringUtils.addPrefixToEachLine(text, " * ");
        }
        return " *";
    }

    /**
     * The class body: the key constructor, then the fields. The slot sits
     * inside the class, so each line takes its indentation - which is what a
     * paragraph does, and it splits the rendered field block into its lines
     * on the way in.
     */
    private void body(EntitySpec e, SourceCode c) {
        CodeTemplate.Paragraph body = c.paragraph("fieldList");
        pkConstructor(body, e);
        // Both trailing blanks are content: the entity template puts its
        // closing brace right after the slot.
        for (String line : RuleText.linesOf(c.renderFieldBlock())) {
            body.add((Object) line);
        }
    }

    /**
     * The convenience constructor that takes the primary key. Inherited key
     * fields go to the parent through super(), the entity's own are assigned
     * directly, and an entity without a key gets no constructor.clear
     */
    private void pkConstructor(CodeTemplate.Paragraph body, EntitySpec e) {
        List<io.elasticore.base.model.pub.rules.FieldSpec> inherited =
                new ArrayList<io.elasticore.base.model.pub.rules.FieldSpec>();
        List<io.elasticore.base.model.pub.rules.FieldSpec> own =
                new ArrayList<io.elasticore.base.model.pub.rules.FieldSpec>();

        // The inherited keys live in the full field list; the declared ones
        // in the entity's own. 2.x collects both (collectInheritedPkFields).
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (io.elasticore.base.model.pub.rules.FieldSpec f : e.fields()) {
            declared.add(f.name());
            if (!f.hasDsl("id") && !f.hasDsl("pk")) continue;
            if (f.isParentField()) inherited.add(f); else own.add(f);
        }
        for (io.elasticore.base.model.pub.rules.FieldSpec f : e.allFields()) {
            if (declared.contains(f.name())) continue;
            if (!f.hasDsl("id") && !f.hasDsl("pk")) continue;
            inherited.add(f);
        }

        if (inherited.isEmpty() && own.isEmpty()) return;

        StringBuilder params = new StringBuilder();
        for (io.elasticore.base.model.pub.rules.FieldSpec f : inherited) append(params, f);
        for (io.elasticore.base.model.pub.rules.FieldSpec f : own) append(params, f);

        body.add("public %s(%s) {", e.getName(), params);

        if (!inherited.isEmpty()) {
            StringBuilder args = new StringBuilder();
            for (io.elasticore.base.model.pub.rules.FieldSpec f : inherited) {
                if (args.length() > 0) args.append(", ");
                args.append(f.name());
            }
            body.add("    super(%s);", args);
        }
        for (io.elasticore.base.model.pub.rules.FieldSpec f : own) {
            body.add("    this.%s = %s;", f.name(), f.name());
        }

        body.add((Object) "}");
        body.add((Object) "");
    }

    private static void append(StringBuilder params,
                               io.elasticore.base.model.pub.rules.FieldSpec f) {
        if (params.length() > 0) params.append(", ");
        params.append(f.coreItemType()).append(" ").append(f.name());
    }

}
