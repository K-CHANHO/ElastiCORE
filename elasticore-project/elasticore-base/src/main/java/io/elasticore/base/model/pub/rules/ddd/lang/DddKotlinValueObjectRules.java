package io.elasticore.base.model.pub.rules.ddd.lang;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;
import io.elasticore.base.util.StringList;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * A value object, in Kotlin.
 *
 * The counterpart the Java rule's own comment asks for. What makes a value
 * object one is that two of them holding the same values are the same thing -
 * an address is not a different address because it was read twice. Without
 * that, the class is a row with no identity, which is the one thing a value
 * object may not be.
 *
 * Java gets this from Lombok, which generates nothing for Kotlin, so the
 * members are written here. They cannot come from a data class either: JPA
 * constructs an embeddable through a no-arg constructor and sets its state
 * afterwards, and a data class has to declare its properties in a primary
 * constructor.
 *
 * The properties stay mutable for the same reason - Hibernate writes them.
 * What is stated instead is that changing one is not how a value object is
 * used, which is what the Java rule says by dropping its setter.
 *
 * Ordered after the file shell at 90, so the members land under the fields it
 * rendered.
 */
public class DddKotlinValueObjectRules implements CodeRules,
        io.elasticore.base.model.pub.rules.ImmutableValueObjects {

    @Rule(value = "kt-value-object", target = "entity", order = 95, phase = Rule.Phase.TAIL)
    void valueObject(EntitySpec e) {
        if (!e.hasMeta("embeddable")) return;

        SourceCode c = ctx(e);
        c.requireSyntax(Syntax.KOTLIN, "kt-value-object");

        if (e.fields().isEmpty()) return;

        StringList names = StringList.create(", ");
        for (FieldSpec f : e.fields()) {
            names.add(f.name());
        }

        for (FieldSpec f : e.fields()) {
            c.field(f).comment("Set once; a value object is replaced rather than changed.");
        }

        equals(c, e);
        hashCode(c, names.toString());
    }

    /**
     * Equality over every property.
     *
     * The type is checked with "is" rather than by comparing classes, since
     * an embeddable is not proxied - what Hibernate hands back is the class
     * itself.
     */
    private void equals(SourceCode c, EntitySpec e) {
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Two of these are the same where every value is. */")
         .varLine("fieldList", "override fun equals(other: Any?): Boolean {")
         .varLine("fieldList", "    if (this === other) return true")
         .varLine("fieldList", "    if (other !is %s) return false", e.getName())
         .varLine("fieldList", "    return %s", comparisons(e));

        c.varLine("fieldList", "}");
    }

    /** One property per line, so a wide value object stays readable. */
    private String comparisons(EntitySpec e) {
        StringList lines = StringList.create(String.format("%n            && "));
        for (FieldSpec f : e.fields()) {
            lines.add(String.format("%s == other.%s", f.name(), f.name()));
        }
        return lines.toString();
    }

    private void hashCode(SourceCode c, String names) {
        c.varLine("fieldList", "")
         .varLine("fieldList", "override fun hashCode(): Int = java.util.Objects.hash(%s)", names);
    }
}
