package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.DomainSpec;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.Rule;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The Views class that names the Jackson view interfaces, ported from
 * ViewsSrcFilePublisher. Its whole body is template text; the only decision
 * is whether the domain needs the class at all.
 *
 * The per-field @JsonView annotations that reference these interfaces are
 * emitted by {@link DtoBaseRules}.
 */
public class ViewsBaseRules implements CodeRules {

    /**
     * A domain gets the class once anything asks for views: either the
     * environment turns them on for every DTO, or a single DTO declares
     * @jsonview for itself.
     */
    @Rule(value = "views-gate", target = "views", order = 10)
    void views(DomainSpec d) {
        if (!needsViews(d)) {
            ctx(d).dontMake();
            return;
        }
        ctx(d).var("packageName", d.pkg("dto"));
    }

    private boolean needsViews(DomainSpec d) {
        String config = d.jsonViewConfig();
        if ("auto".equalsIgnoreCase(config) || "true".equalsIgnoreCase(config)) return true;

        for (EntitySpec e : d.entities()) {
            if (!e.hasMeta("jsonview")) continue;
            if (!"false".equalsIgnoreCase(e.metaValue("jsonview"))) return true;
        }
        return false;
    }
}
