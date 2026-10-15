package io.elasticore.base.model.pub.rules.lang.java;

import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import io.elasticore.base.model.pub.rules.base.RuleText;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import java.util.ArrayList;
import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The DTO class around the field block, ported from the file-level part of
 * DtoSrcFilePublisher. The field annotations come from {@link DtoBaseRules};
 * these rules name the packages and shape the declaration.
 *
 * A search DTO is a different artifact with its own template, so it is not
 * produced here - 2.x returns early for it too.
 */
public class DtoFileRules implements CodeRules {

    @Rule(value = "dto-file", target = "dto", order = 90, phase = Rule.Phase.TAIL)
    void file(EntitySpec e) {
        if (e.hasMeta("searchable")) {
            ctx(e).dontMake();
            return;
        }

        SourceCode c = ctx(e);
        String className = e.dtoClassName();

        // The key DTO carries the DTO's own imports and class annotations -
        // 2.x re-renders the same parameters with the field list swapped -
        // so only the name and the fields differ.
        String keyName = e.keyDtoClassName();
        if (keyName != null) {
            className = keyName;
            boolean anyKey = false;
            for (FieldSpec f : e.fields()) {
                if (f.hasDsl("id") || f.hasDsl("pk")) {
                    anyKey = true;
                } else {
                    c.removeField(f);
                }
            }
            if (!anyKey) {
                c.dontMake();
                return;
            }
        }

        // A DTO leaves nulls out of its JSON unless the model asks for them.
        // The spec outlives this rule, so say it only once.
        String nonNull = "@JsonInclude(Include.NON_NULL)";
        if (!e.hasMeta("includenull") && !e.nativeClassAnnotationLines().contains(nonNull)) {
            e.nativeClassAnnotation(nonNull);
            e.dtoImportOnce("com.fasterxml.jackson.annotation.JsonInclude");
            e.dtoImportOnce("com.fasterxml.jackson.annotation.JsonInclude.Include");
        }

        c.var("packageName", e.pkg("dto"))
         .var("j2eePkgName", "jakarta")
         .var("className", className)
         .var("abstract", e.hasMeta("abstract") ? "abstract" : "")
         .var("extendInfo", extendClause(e))
         .var("implementInfo", implementClause(e))
         .var("isSearchDTO", "false")
         .varLines("classAnnotationList", e.nativeClassAnnotationLines())
         // The field slot sits inside the class body, so each line takes the
         // slot's indentation rather than carrying its own.
         .varLines("fieldList", RuleText.linesOf(c.renderFieldBlock(1)));

        // 2.x names the enum package only when the domain declares enums,
        // so a domain without any keeps that import line blank.
        if (e.hasEnums()) c.var("enumPackageName", e.pkg("enumeration"));
        if (!e.dtoImports().isEmpty()) c.varLines("importList", e.dtoImports());
    }

    /**
     * A DTO of its own declares no parent; one derived from a template
     * inherits whatever the DSL's @extend names.
     */
    private String extendClause(EntitySpec e) {
        if (e.hasMeta("dto")) return "";
        String parent = e.metaValue("extend");
        return parent == null ? "" : String.format("extends %s", parent);
    }

    /** Serializable always, plus whatever @implements adds. */
    private String implementClause(EntitySpec e) {
        String declared = e.metaValue("implements");
        String extra = (declared == null || declared.isEmpty()) ? "" : String.format(",%s", declared);
        return String.format("implements java.io.Serializable%s", extra);
    }


}
