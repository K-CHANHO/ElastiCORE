package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a project's own rule set leans on: the model API it reads, the way it
 * is added to a bundle, and suppression that leaves no half-written file.
 * elasticore-ddd-customize-guide.md is written against these.
 */
public class ProjectRuleSetSupportTest {

    @AfterEach
    public void clear() {
        System.clearProperty(RulesConfig.PROP_RULES_ADD);
    }

    private EntitySpec order() {
        return new EntitySpec("SalesOrder")
                .domain("erp", "Erp")
                .meta("root", "")
                .meta("event", "Confirmed(totalAmount), shipped, PartiallyShipped(orderNo, qty)")
                .pkg("entity", "com.acme.erp.domain.model")
                .field("orderId", "string(36) @id")
                .field("orderNo", "string(20)!");
    }

    @Test
    public void aProjectSkeletonGetsItsPackageImportsAndAnnotations() {
        DomainSpec erp = new DomainSpec("erp", "Erp");
        String out = new RuleEngine().register(new com.acme.probe.ProbeRules()).templateSet(SourceCode.TemplateSet.V3)
                .packageName("com.acme.erp.messaging").run(erp, "projectProbe").render();
        assertTrue(out.contains("package com.acme.erp.messaging;"), out);
        assertTrue(out.contains("import com.acme.arch.Generated;"), out);
        assertTrue(out.contains("import java.util.List;"), out);
        assertTrue(out.contains("@Generated"), out);
    }

    @Test
    public void everyNamespaceKeyIsAnsweredApartFromPkg() {
        java.util.Map<String, String> declared = new java.util.HashMap<String, String>();
        declared.put("messaging", "com.acme.erp.infrastructure.messaging");
        DomainSpec erp = new DomainSpec("erp", "Erp").namespaces(declared);
        erp.pkg("entity", "com.acme.erp.domain.model");
        assertEquals("com.acme.erp.infrastructure.messaging", erp.namespace("messaging"));
        assertEquals("com.acme.erp.domain.model", erp.namespace("entity"));
        // pkg() keeps answering only what the generator set: built-in rules read its null as a fallback.
        assertEquals(null, erp.pkg("messaging"));
    }

    @Test
    public void aProjectRuleNeedNotKnowWhenTheShellDrewTheClass() {
        RuleEngine engine = RuleCatalog.standard().engineFor("hexagonal-springboot")
                .register(new com.acme.probe.LateRules()).templateSet(SourceCode.TemplateSet.V3);
        String plain = engine.run(order(), "entity").render();
        assertTrue(plain.contains("@Marker"), plain);
        assertTrue(plain.contains("import com.acme.arch.Marker;"), plain);
        assertFalse(plain.contains("@DynamicUpdate"), plain);

        // Split in two: the developer's class gets what the entity rule says.
        EntitySpec split = order().entitySplitForBehavior("SalesOrder");
        String mine = engine.run(split, "behavior").render();
        assertTrue(mine.contains("@Marker"), mine);
        assertTrue(mine.contains("import com.acme.arch.Marker;"), mine);
    }

    @Test
    public void builtInOutputIsUntouchedWithoutProjectRules() {
        String out = RuleCatalog.standard().engineFor("hexagonal-springboot")
                .templateSet(SourceCode.TemplateSet.V3).run(order(), "entity").render();
        assertTrue(out.contains("@DynamicUpdate"), out);
        assertFalse(out.contains("Marker"), out);
    }

    @Test
    public void anUnfilledSlotIsReported() {
        DomainSpec erp = new DomainSpec("erp", "Erp");
        RuleEngine engine = new RuleEngine().register(new com.acme.probe.ProbeRules()).templateSet(SourceCode.TemplateSet.V3);
        SourceCode filled = engine.packageName("a.b").run(erp, "projectProbe");
        filled.render();
        assertTrue(filled.unfilledSlots().isEmpty(), String.valueOf(filled.unfilledSlots()));

        SourceCode missing = engine.packageName(null).run(erp, "projectProbe");
        missing.render();
        assertEquals(Collections.singletonList("packageName"), missing.unfilledSlots());
    }

    @Test
    public void aCheckIsSaidOnceAndAFailureIsKept() {
        ModelCheck.reset();
        try {
            ModelCheck.warn("w");
            ModelCheck.warn("w");
            ModelCheck.fail("f");
            ModelCheck.fail("f");
            assertEquals(Collections.singletonList("f"), ModelCheck.failures());
        } finally {
            ModelCheck.reset();
        }
        assertTrue(ModelCheck.failures().isEmpty());
    }

    @Test
    public void aSettingIsReadFromTheSpecOrItsSource() {
        EntitySpec e = order().config("acme.requireTopic", "true");
        assertEquals("true", e.config("acme.requireTopic"));
        assertEquals(null, e.config("acme.other"));

        e.configSource(new ConfigSource() {
            public String get(String key) {
                return "acme.other".equals(key) ? "x" : null;
            }
        });
        assertEquals("x", e.config("acme.other"));
        assertEquals("true", e.config("acme.requireTopic"));
    }

    @Test
    public void aFieldIsWrittenAsTheDslWritesIt() {
        EntitySpec e = order();
        assertEquals("orderId", e.pkField().name());
        assertTrue(e.fields().get(1).required());
    }

    @Test
    public void theEventsOfARootAreNamedAsTheirClasses() {
        EntitySpec e = order();
        assertEquals(Arrays.asList("Confirmed", "Shipped", "PartiallyShipped"), e.events());
        assertEquals("SalesOrderShippedEvent", e.eventClassName("Shipped"));
        // No event namespace: the entity's package.
        assertEquals("com.acme.erp.domain.model", e.eventPackage());
        assertEquals("com.acme.erp.domain.event",
                order().pkg("event", "com.acme.erp.domain.event").eventPackage());
    }

    @Test
    public void aMemberRaisesNone() {
        EntitySpec line = new EntitySpec("SalesOrderLine").meta("event", "Added")
                .containedIn("SalesOrder").field("lineId", "long @id");
        assertEquals(Collections.<String>emptyList(), line.events());
    }

    @Test
    public void addedRulesAreReadInOrder() {
        System.setProperty(RulesConfig.PROP_RULES_ADD, "com.acme.A, com.acme.B");
        assertEquals(Arrays.asList("com.acme.A", "com.acme.B"), RulesConfig.addedRules());
    }

    @Test
    public void noAddedRulesIsAnEmptyList() {
        assertTrue(RulesConfig.addedRules().isEmpty());
    }

    @Test
    public void anArtifactWhoseRulesAreAllSuppressedIsNotMade() {
        DomainSpec erp = new DomainSpec("erp", "Erp").entity(order().meta("expose", ""));
        erp.pkg("control", "com.acme.erp.adapter.in.web");

        RuleEngine engine = RuleCatalog.standard().engineFor("hexagonal-springboot")
                .templateSet(SourceCode.TemplateSet.V3);
        assertTrue(engine.run(erp, "apiErrors").isMake());

        engine.suppress("spring-api-errors");
        assertFalse(engine.run(erp, "apiErrors").isMake());
    }
}
