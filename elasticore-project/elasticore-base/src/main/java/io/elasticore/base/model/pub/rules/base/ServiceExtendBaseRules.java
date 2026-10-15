package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * Base rules for the developer-owned service extension class.
 * Skeleton stays in service_extend.tmpl; rules provide the variables.
 */
public class ServiceExtendBaseRules implements CodeRules {

    @Rule(value = "service-extend-vars", target = "service_extend", order = 10)
    void vars(EntitySpec e) {
        SourceCode c = ctx(e);

        // A domain that asked for AI support gets its extension written as
        // Spring AI tools over the core service, so a model can call what the
        // DSL declared. Same artifact, a different skeleton - which is what a
        // template name is for.
        if (e.supportAI()) c.template("service_extend_ai");

        String searchResultDto = e.searchResultDtoName();
        // The DTOs 2.x chose for the service pair, when the runner found them
        // through @template / @searchable; the conventional names otherwise.
        String dtoName = e.config(io.elasticore.base.model.pub.rules.verify.RulesGenRunner.COMPAT_SERVICE_DTO);
        if (dtoName == null) dtoName = String.format("%sDTO", e.getName());
        String searchDtoName = e.config(io.elasticore.base.model.pub.rules.verify.RulesGenRunner.COMPAT_SERVICE_SRCH_DTO);
        if (searchDtoName == null) searchDtoName = e.searchDtoClassName();
        if (searchResultDto == null) searchResultDto = dtoName;

        String label = e.metaValue("label");

        c.var("packageName", e.pkg("service"))
         .var("entityPackageName", e.pkg("entity"))
         .var("dtoPackageName", e.pkg("dto"))
         .var("repositoryPackageName", e.pkg("repository"))
         .var("entityClassName", e.getName())
         .var("entityDescName", (label == null || label.isEmpty()) ? e.getName() : label)
         .var("domainName", e.domainClassName())
         .var("dtoClassName", dtoName)
         .var("keyDtoClassName", "%sKeyDTO", e.getName())
         .var("searchDTOClassName", searchDtoName)
         .var("searchResultDTOClassName", searchResultDto)
         // The v3 skeleton carries an implements slot so a composed rule set
         // (the hexagonal one) can name an interface without owning the
         // template. Empty by default - a slot left unresolved would blank
         // the whole declaration line, and most projects compose nothing.
         .var("implementsClause", "")
         // What the extension is constructed with and hands to the core
         // service. The helper, unless a composed rule set gives the core
         // service ports instead (DddRepositoryPortRules) - the two
         // constructors have to agree, and this is where they are kept so.
         .var("extendImport", "%s.%sRepositoryHelper", e.pkg("repository"), e.domainClassName())
         .var("extendCtorParams", c.syntax() == io.elasticore.base.model.pub.rules.Syntax.KOTLIN
                 ? String.format("helper: %sRepositoryHelper", e.domainClassName())
                 : String.format("%sRepositoryHelper helper", e.domainClassName()))
         .var("extendSuperArgs", "helper")
         // The rule pipeline only writes a service for an entity that is
         // @expose or @service, so the tool set is always the exposed one.
         // The list, page and custom-projection arms of the 2.x skeleton
         // cannot be reached from here.
         .var("isExposeOutput", "true")
         .var("isListOutput", "false")
         .var("isPageOutput", "false")
         .var("isCustomListOutput", "false")
         .var("isCustomPageOutput", "false");

        // An AI tool calls the core service method of the same name, so a
        // tool is offered only where that method exists: a @readonly entity
        // keeps its queries alone, and @restExclude takes single methods off
        // (the v3-service-method-filter rule says the same of the service).
        boolean writable = !e.hasMeta("readonly");
        java.util.Set<String> excluded = new java.util.HashSet<String>();
        String exclude = e.metaValue("restexclude");
        if (exclude != null) {
            for (String id : exclude.split("[,\\s]+")) if (!id.isEmpty()) excluded.add(id);
        }
        c.var("aiSave", String.valueOf(writable && !excluded.contains("save")))
         .var("aiUpdate", String.valueOf(writable && !excluded.contains("update")))
         .var("aiDelete", String.valueOf(writable && !excluded.contains("delete")))
         .var("aiUpdateBySpec", String.valueOf(writable && !excluded.contains("updateBySpecification")));
    }
}
