package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What an aggregate boundary subtracts.
 *
 * JPA gives a repository to anything with a table. A domain model gives one to
 * an aggregate root and nothing else - a member is reached by navigating from
 * the root that owns it. The DSL already says which entities live and die
 * together, so no new declaration was needed to tell them apart.
 *
 * The bundle is the whole mechanism: ddd-springboot is the v3 bundle plus
 * rules that decline, so it can only ever produce a subset of what v3 does.
 */
public class DddAggregateTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /** An entity the DSL exposes and nobody cascades into. */
    private EntitySpec root() {
        return new EntitySpec("Order")
                .domain("shop", "Shop")
                .meta("expose", "")
                .pkg("repository", "io.acme.shop.repository")
                .pkg("entity", "io.acme.shop.entity")
                .pkg("dto", "io.acme.shop.dto")
                .pkg("service", "io.acme.shop.service")
                .addField(loader.parse("orderId", "long @id"));
    }

    /** The same, held by another entity through a cascading relation. */
    private EntitySpec member() {
        return root().containedIn("Order");
    }

    private SourceCode run(EntitySpec e, String target) {
        return RuleCatalog.standard()
                .engineFor("ddd-springboot")
                .templateSet(SourceCode.TemplateSet.V3)
                .run(e, target);
    }

    private SourceCode runV3(EntitySpec e, String target) {
        return RuleCatalog.standard()
                .engineFor("jpa-springboot-v3")
                .templateSet(SourceCode.TemplateSet.V3)
                .run(e, target);
    }

    // ---- what the boundary decides -------------------------------------

    /** A root is fetched by identity, so it has a repository. */
    @Test
    public void aRootKeepsItsRepository() {
        assertTrue(run(root(), "repository").isMake());
        assertTrue(run(root(), "service").isMake());
        assertTrue(run(root(), "control").isMake());
    }

    /**
     * A member is not. This is the difference from the JPA reading, which
     * would give it all three - so the same model produces different code
     * because a different bundle was asked for.
     */
    @Test
    public void aMemberLosesWhatOnlyARootShouldHave() {
        assertFalse(run(member(), "repository").isMake());
        assertFalse(run(member(), "service").isMake());
        assertFalse(run(member(), "control").isMake());

        // And the JPA reading still gives it those, so the subtraction is the
        // bundle's doing rather than something about this entity.
        assertTrue(runV3(member(), "repository").isMake());
    }

    /** An @embeddable has no identity, so it is not a root either. */
    @Test
    public void aValueObjectIsNoRoot() {
        EntitySpec vo = new EntitySpec("Address")
                .domain("shop", "Shop")
                .meta("embeddable", "")
                .pkg("entity", "io.acme.shop.entity");

        assertFalse(vo.aggregateRoot());
    }

    /** Cascade is what the DSL already used to say the same thing. */
    @Test
    public void containmentComesFromWhatTheDslAlreadySaid() {
        assertTrue(root().aggregateRoot());
        assertFalse(member().aggregateRoot());
    }

    // ---- the value object ----------------------------------------------

    /**
     * A value object is replaced rather than changed, so the setters that
     * would let it change are not there and equality is by value.
     *
     * This is the Java shaping of one. The boundary above holds in any
     * language; Lombok does not, which is why the two are separate rules and
     * a Kotlin project composes [jpa-springboot-kt, ddd-aggregate] instead.
     */
    @Test
    public void aValueObjectIsImmutableAndComparedByValue() {
        EntitySpec vo = new EntitySpec("Address")
                .domain("shop", "Shop")
                .meta("embeddable", "")
                .pkg("entity", "io.acme.shop.entity")
                .addField(loader.parse("city", "string(60)"))
                .addField(loader.parse("zipCode", "string(10)"));

        String out = run(vo, "entity").render();

        assertTrue(out.contains("@EqualsAndHashCode"), out);
        assertFalse(out.contains("@Setter"), out);
        assertTrue(out.contains("a value object is replaced rather than changed"), out);
    }

    // ---- when the inference is wrong ------------------------------------

    /**
     * Cascade is a persistence decision. Two aggregates can be written
     * together and still be two - so @root says outright that this one is a
     * root, and the inference gives way.
     */
    @Test
    public void anEntityCanDeclareItselfARootDespiteCascade() {
        EntitySpec e = member().meta("root", "");

        assertTrue(e.aggregateRoot());
        assertTrue(run(e, "repository").isMake());
    }

    /** A bare @aggregate is the same statement, and reads as one. */
    @Test
    public void aBareAggregateSaysTheSameAsRoot() {
        assertTrue(member().meta("aggregate", "").aggregateRoot());
    }

    /**
     * The two can sit together - @root saying what this is, @aggregate naming
     * the one it heads - and they do not contradict.
     */
    @Test
    public void rootAndAggregateCanBeWrittenTogether() {
        EntitySpec e = member().meta("root", "").meta("aggregate", "Order");

        assertTrue(e.aggregateRoot());
    }

    /**
     * And the other way: an entity nobody cascades into can still belong to
     * an aggregate, when the lifecycle is managed in code rather than by JPA.
     */
    @Test
    public void anEntityCanDeclareTheAggregateItBelongsTo() {
        EntitySpec e = root().meta("aggregate", "Customer");

        assertEquals("Customer", e.containedIn());
        assertFalse(e.aggregateRoot());
        assertFalse(run(e, "repository").isMake());
    }

    /** With nothing declared, the model is still read as before. */
    @Test
    public void withoutTheAnnotationTheInferenceStands() {
        assertTrue(root().aggregateRoot());
        assertFalse(member().aggregateRoot());
    }
}
