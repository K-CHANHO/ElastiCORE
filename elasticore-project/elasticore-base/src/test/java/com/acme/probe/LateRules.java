package com.acme.probe;

import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.Rule;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/** A project's rules written with no thought for order: they run after every file shell. */
public class LateRules implements CodeRules {

    @Rule(value = "late-marker", target = "entity", order = 99, phase = Rule.Phase.TAIL)
    void marker(EntitySpec e) {
        if (e.aggregateRoot()) ctx(e).ensureClassAnnotation(AnnoCode.of("com.acme.arch.Marker"));
    }

    @Rule(value = "late-no-dynamic-update", target = "entity", order = 99, phase = Rule.Phase.TAIL)
    void noDynamicUpdate(EntitySpec e) {
        ctx(e).removeClassAnnotation("org.hibernate.annotations.DynamicUpdate");
    }
}
