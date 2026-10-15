package com.acme.probe;

import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.DomainSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/** A project's rule for a skeleton of its own, which has no shell rule behind it. */
public class ProbeRules implements CodeRules {

    @Rule(value = "probe", target = "projectProbe", order = 10)
    void probe(DomainSpec d) {
        SourceCode c = ctx(d);
        c.ensureClassAnnotation(AnnoCode.of("com.acme.arch.Generated"));
        c.addImport("java.util.List");
        c.var("className", "Probe");
    }
}
