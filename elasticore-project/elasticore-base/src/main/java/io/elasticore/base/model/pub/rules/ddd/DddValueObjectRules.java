package io.elasticore.base.model.pub.rules.ddd;

import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * A value object, in Java.
 *
 * It has no identity and no life of its own, so it does not change: it is
 * replaced. The setter that would let it change is not emitted and equality
 * is by value rather than by reference.
 *
 * Separate from the aggregate boundary because this is not the same kind of
 * statement. The boundary is a fact about the model and holds in any
 * language; how a class is made immutable is a fact about the tools - here,
 * Lombok, which is a Java annotation processor and does nothing elsewhere.
 * A language that shapes a value object differently supplies its own rule.
 *
 * Ordered before the file shell, which renders the class annotations at 90.
 */
public class DddValueObjectRules implements CodeRules,
        io.elasticore.base.model.pub.rules.ImmutableValueObjects {

    @Rule(value = "ddd-value-object", target = "entity", order = 80, phase = Rule.Phase.TAIL)
    void valueObject(EntitySpec e) {
        if (!e.hasMeta("embeddable")) return;

        SourceCode c = ctx(e);
        // Everything below is Lombok, and Lombok is Java. Said here because
        // a bundle is only a list of names: this set can be selected next to
        // a Kotlin file shell, and the annotations would then be written
        // into a .kt file that no annotation processor ever reads.
        // ddd-value-object-kt is the counterpart to select instead.
        c.requireSyntax(Syntax.JAVA, "ddd-value-object");

        c.removeClassAnnotation("lombok.Setter");
        c.ensureClassAnnotation(AnnoCode.of("lombok.EqualsAndHashCode"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.AllArgsConstructor"));
        c.ensureClassAnnotation(AnnoCode.of("lombok.NoArgsConstructor"));

        for (FieldSpec f : e.fields()) {
            c.field(f).comment("Set once; a value object is replaced rather than changed.");
        }
    }

    /**
     * The replacement the class has been claiming all along.
     *
     * Withholding the setter says a value object does not change. It did not
     * say how to get a different one, and without an answer the honest way to
     * vary a single field was the all-args constructor - every other field
     * repeated at the call site, in positional order, which is how the wrong
     * two get swapped. So each field gets a with-method returning a new
     * instance, and "replaced rather than changed" becomes something the code
     * can actually do rather than only something the comment asserts.
     *
     * Ordered after the file shell at 90, which writes the field slot by
     * replacing its paragraph outright and would drop anything appended
     * before it ran - the same seam {@link DddCollectionRules} sits either
     * side of.
     */
    @Rule(value = "ddd-value-object-with", target = "entity", order = 95, phase = Rule.Phase.TAIL)
    void replacement(EntitySpec e) {
        if (!e.hasMeta("embeddable")) return;

        SourceCode c = ctx(e);
        if (c.syntax() != Syntax.JAVA) return;

        java.util.List<FieldSpec> fields = new java.util.ArrayList<FieldSpec>();
        for (FieldSpec f : e.fields()) {
            if (!f.isList()) fields.add(f);
        }
        if (fields.isEmpty()) return;

        String type = e.getName();
        for (FieldSpec target : fields) {
            StringBuilder args = new StringBuilder();
            for (FieldSpec f : fields) {
                if (args.length() > 0) args.append(", ");
                args.append(f == target ? f.name() : "this." + f.name());
            }

            c.varLine("fieldList", "")
             .varLine("fieldList", "/** The same value with a different %s. This one is left as it was. */",
                     target.name())
             .varLine("fieldList", "public %s with%s(%s %s) {",
                     type, target.capitalName(), target.typeName(), target.name())
             .varLine("fieldList", "    return new %s(%s);", type, args)
             .varLine("fieldList", "}");
        }
    }
}
