package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.DomainSpec;
import io.elasticore.base.model.pub.rules.Rule;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The two interfaces the generated DTOs implement, ported from
 * EtcSrcFilePublisher: PageableObject carries the paging parameters of a
 * search, SortableObject its sort order.
 *
 * A project that already has its own is told so in env.yml, and then keeps
 * the one it has.
 */
public class SupportTypeBaseRules implements CodeRules {

    @Rule(value = "pageable-object", target = "pageableObject", order = 10)
    void pageableObject(DomainSpec d) {
        supportType(d, d.customPageableInterface(), "java_PageableObject");
    }

    @Rule(value = "sortable-object", target = "sortableObject", order = 10)
    void sortableObject(DomainSpec d) {
        supportType(d, d.customSortableInterface(), "java_SortableObject");
    }

    /**
     * 2.x named these templates independently of the classes they produce, so
     * the target points at its own.
     */
    private void supportType(DomainSpec d, String customInterface, String templateName) {
        if (customInterface != null) {
            ctx(d).dontMake();
            return;
        }
        ctx(d).template(templateName).var("packageName", d.pkg("dto"));
    }
}
