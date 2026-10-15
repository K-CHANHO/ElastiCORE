package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * An @embeddable crosses the API boundary as itself.
 *
 * A DTO holds an embedded value - a delivery address, a credit card - as
 * the embeddable class, not as a DTO of it. So whatever a DTO field would
 * say about a request and a reply has to be said on the embeddable's own
 * fields, or it is said nowhere:
 *
 *  - @writeonly on a card number kept it out of no reply at all, and every
 *    order read back carried the number it was paid with;
 *  - "street1: string(50)!" was a NOT NULL column and nothing else, so a
 *    missing street reached the database and came back as a constraint
 *    failure rather than as the 400 its DTO neighbours give.
 *
 * The field annotations are the DTO's, from the same rules, and the DTO
 * field holding the value is marked @Valid so they are checked with it.
 *
 * Clean output only: the compatible bundle writes what 2.x wrote.
 */
public class ValueObjectBoundaryRules implements CodeRules {

    private final DtoBaseRules dto = new DtoBaseRules();

    /** What the DSL says about a value's field in a request, on the field. */
    @Rule(value = "value-object-boundary", target = "entity", order = 21)
    void boundary(EntitySpec e, FieldSpec f) {
        if (!e.hasMeta("embeddable") || ctx(e).compatible()) return;
        dto.validation(e, f);
        dto.json(e, f);
    }

    /** A value held by a DTO is validated with it. */
    @Rule(value = "value-object-valid", target = "dto", order = 22)
    void cascade(EntitySpec e, FieldSpec f) {
        if (ctx(e).compatible() || f.isBaseType()) return;
        String type = f.coreItemType();
        if (type == null || !e.isEmbeddableType(type)) return;
        ctx(e).field(f).ensure(AnnoCode.of("jakarta.validation.Valid"));
    }
}
