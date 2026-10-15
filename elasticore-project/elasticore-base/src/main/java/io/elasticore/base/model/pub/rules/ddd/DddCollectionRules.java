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
 * The members of an aggregate, reached only through its root.
 *
 * A root that hands out a setter for the collection holding its members hands
 * out the aggregate itself: the caller can swap the whole list, and whatever
 * the root promises about its contents stops being the root's to keep. So the
 * setter goes, and what replaces it is the two operations that keep the
 * promise - one member in, one member out.
 *
 * Which collections are the aggregate's own is already declared. A cascading
 * to-many is the DSL saying these rows live and die with mine, which is the
 * same statement as membership; a to-many without it points outside and is
 * left alone.
 *
 * The two halves reach different languages. Cascade and orphan removal are
 * what belonging means to the ORM and are written in both - a model that
 * declares the boundary should not have to declare it twice, in either
 * language. The encapsulation is Java only, and by nature rather than by
 * omission. Removing a setter here
 * means telling Lombok not to generate one, and the collection that the added
 * methods mutate has to be mutable - in Kotlin the generated property is a
 * read-only List, so the same two methods would have to replace the instance
 * instead of changing it, which is exactly what Hibernate's dirty tracking on
 * a managed collection cannot follow. Kotlin needs the field's type changed
 * first, and that is a spec-level change; see {@link DddReferenceRules} for
 * the same boundary.
 *
 * Split in two around the file shell at 90, because the two halves cannot
 * both sit on one side of it. The annotation has to be on the field before
 * the shell renders the field block; the methods have to come after, because
 * the shell writes that block by replacing the slot's paragraph outright and
 * would drop anything already appended to it.
 */
public class DddCollectionRules implements CodeRules {

    private static final String JPA = "jakarta.persistence.%s";

    /** The setter Lombok would have generated for the collection, withheld. */
    @Rule(value = "ddd-collection", target = "entity", order = 84, phase = Rule.Phase.TAIL)
    void sealCollection(EntitySpec e) {
        SourceCode c = ctx(e);
        if (e.hasMeta("embeddable")) return;

        for (FieldSpec f : e.fields()) {
            if (!owns(e, f)) continue;

            // What membership means to the ORM is the same statement in
            // either language, so it is made in either. Withholding the
            // setter is not - see the note on this class.
            persistWithTheRoot(c, f);

            if (c.syntax() == Syntax.KOTLIN) {
                // Two statements, both needed and neither sufficient.
                //
                // Mutable, so the methods below can change the list at all -
                // the read-only List the renderer produces by default reads
                // as encapsulation and is not one, since the root cannot
                // change it either.
                //
                // And "final var ... private set", which is how this language
                // withholds a setter. The first attempt was "private set"
                // alone and the compiler refused it: "Private setters are not
                // allowed for open properties". A JPA entity's properties are
                // open because the all-open plugin opens them, which is what
                // lets Hibernate proxy the class - so the ORM's requirement
                // and the encapsulation looked mutually exclusive.
                //
                // They are not: the plugin opens what would otherwise be
                // final by default and leaves an explicit "final" alone. The
                // property is sealed and the class stays open, which is what
                // each of the two actually needed. Hibernate writes the field
                // rather than the property, so nothing it does is affected.
                // And read as a view: the getter hands out a list that refuses
                // add and remove, so a member cannot be put in past the methods
                // that also give it its owner. Kotlin cannot reach the backing
                // field outside its accessor, so the methods below reach the
                // real list through MemberList.backing() instead; Hibernate
                // reads and writes the field directly and never sees the view.
                c.field(f)
                 .type(String.format("MutableList<%s>", f.coreItemType()))
                 .modifier("final var")
                 .accessor("    get() = io.elasticore.springboot3.entity.MemberList.view(field)\n    private set")
                 .comment("Members of this aggregate. Changed one at a time, through this root.");
                continue;
            }
            if (c.syntax() != Syntax.JAVA) {
                c.field(f).comment("Members of this aggregate. Saved and removed with this root.");
                continue;
            }

            c.addImport("lombok.AccessLevel");
            // The getter is withheld too and written by hand below: Lombok's
            // hands out the live list, and a member added to it directly is
            // never given its owner - the ORM then drops it without a word on
            // the next save. The one written below is a read-only view.
            c.field(f)
             .add(AnnoCode.raw("lombok.Setter", "@Setter(AccessLevel.NONE)"))
             .add(AnnoCode.raw("lombok.Getter", "@Getter(AccessLevel.NONE)"))
             .comment("Members of this aggregate. Changed one at a time, through this root.");
        }
    }

    /** What is offered in the setter's place. */
    @Rule(value = "ddd-collection-methods", target = "entity", order = 95, phase = Rule.Phase.TAIL)
    void memberMethods(EntitySpec e) {
        SourceCode c = ctx(e);
        if (!applies(e, c)) return;

        for (FieldSpec f : e.fields()) {
            if (!owns(e, f)) continue;
            if (c.syntax() == Syntax.KOTLIN) {
                addKt(c, f);
                removeKt(c, f);
                replaceKt(c, f);
                continue;
            }
            view(c, f);
            add(c, f);
            remove(c, f);
            replace(c, f);
        }
    }

    /**
     * The same three operations in Kotlin.
     *
     * Written out rather than routed through the language seam because these
     * are bodies, not declarations, and the two languages differ in more than
     * spelling here: the collection is nullable, so every access carries the
     * assertion, and assigning through the member's property is what Java
     * says with a setter call.
     */
    private void addKt(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Takes a member in. The list itself is never handed out to be replaced. */")
         .varLine("fieldList", "fun add%s(member: %s?) {", item, item)
         .varLine("fieldList", "    if (member == null) return")
         .varLine("fieldList", "    if (this.%s == null) this.%s = mutableListOf()", f.name(), f.name())
         .varLine("fieldList", "    %s!!.add(member)", backing(f));

        String owner = ownerField(f);
        if (owner != null) {
            c.varLine("fieldList", "    member.%s = %s", owner, DddBehaviorRules.self(f.entity(), c.syntax()));
        }
        c.varLine("fieldList", "}");
    }

    /** The list itself, from inside the root, behind the view its getter returns. */
    private String backing(FieldSpec f) {
        return String.format("io.elasticore.springboot3.entity.MemberList.backing(this.%s)", f.name());
    }

    private void removeKt(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Lets a member go. Removing it here is what removes it at all. */")
         .varLine("fieldList", "fun remove%s(member: %s?) {", item, item)
         .varLine("fieldList", "    if (this.%s == null || member == null) return", f.name())
         .varLine("fieldList", "    %s!!.remove(member)", backing(f));
        // Kept, as in Java: the member is deleted at the next flush, and the
        // root's promises are checked then through this reference.
        c.varLine("fieldList", "}");
    }

    private void replaceKt(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Sets the members to exactly these. Nothing outside holds one, so none is orphaned by surprise. */")
         .varLine("fieldList", "fun replace%s(members: List<%s>?) {", f.capitalName(), item)
         .varLine("fieldList", "    this.%s?.toList()?.forEach { remove%s(it) }", f.name(), item)
         .varLine("fieldList", "    if (members == null) return")
         .varLine("fieldList", "    members.forEach { add%s(it) }", item)
         .varLine("fieldList", "}");
    }

    /**
     * The members set to exactly what was given.
     *
     * Needed because the aggregate has one way in. A member has no endpoint
     * of its own - the boundary took it away - so a caller sending the root
     * has no second request to send the members with, and something has to
     * accept them all at once.
     *
     * Replacing wholesale is safe for the same reason it is necessary:
     * nothing outside the aggregate may hold a member, so no reference
     * survives being replaced. It is the boundary that makes this the cheap
     * option rather than the dangerous one.
     */
    private void replace(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Sets the members to exactly these. Nothing outside holds one, so none is orphaned by surprise. */")
         .varLine("fieldList", "public void replace%s(java.util.List<%s> members) {", f.capitalName(), item)
         .varLine("fieldList", "    if (this.%s != null) {", f.name())
         .varLine("fieldList", "        for (%s existing : new java.util.ArrayList<%s>(this.%s)) remove%s(existing);",
                 item, item, f.name(), item)
         .varLine("fieldList", "    }")
         .varLine("fieldList", "    if (members == null) return;")
         .varLine("fieldList", "    for (%s member : members) add%s(member);", item, item)
         .varLine("fieldList", "}");
    }

    /**
     * What belonging to an aggregate means to the ORM.
     *
     * The domain says these rows are the root's with @aggregate; cascade and
     * orphan removal are how a persistence provider is told the same thing.
     * Deriving one from the other is the whole point of declaring it - a
     * model should not have to state the boundary twice, once for the reader
     * and once for Hibernate, and get a different answer when the second is
     * forgotten.
     *
     * The relation is rewritten rather than merged because the base rule
     * emits it as one line, so there is no attribute to merge into.
     */
    private void persistWithTheRoot(SourceCode c, FieldSpec f) {
        String relation = f.hasDsl("manytomany") ? "ManyToMany" : "OneToMany";
        String owner = ownerField(f);

        c.addImport(JPA, "FetchType");
        c.addImport(JPA, "CascadeType");
        // An annotation reads the same in both languages except where one of
        // its attributes is an array: Java takes the single value, Kotlin
        // wants it bracketed.
        String cascade = c.syntax() == Syntax.KOTLIN
                ? "[CascadeType.ALL]" : "CascadeType.ALL";
        c.field(f).replace(AnnoCode.raw(String.format(JPA, relation),
                "@%s(fetch = FetchType.LAZY, cascade = %s, orphanRemoval = true%s)",
                relation, cascade,
                owner == null ? "" : String.format(", mappedBy = \"%s\"", owner)));
    }

    /**
     * The member's own field pointing back at the root, or null.
     *
     * A mapped-by collection is the inverse side, so the column that decides
     * membership lives on the member. Adding to the list alone leaves that
     * column unset and the row is written belonging to nobody - which is not
     * something the source can be read for, and is what a database said
     * instead: the members persisted and came back unattached.
     *
     * Clearing it on removal is the same statement in reverse, and is what
     * lets orphanRemoval see the member has left.
     */
    private String ownerField(FieldSpec f) {
        String mappedBy = f.dslProperty("onetomany", "mappedBy");
        if (mappedBy == null) mappedBy = f.dslProperty("manytomany", "mappedBy");
        return (mappedBy == null || mappedBy.trim().isEmpty()) ? null : mappedBy.trim();
    }

    /**
     * The methods only exist in Java. The reason is at the top of this class
     * and is about the language, not about this rule set being unfinished in
     * one of the two.
     */
    private boolean applies(EntitySpec e, SourceCode c) {
        return !e.hasMeta("embeddable")
                && (c.syntax() == Syntax.JAVA || c.syntax() == Syntax.KOTLIN);
    }

    /**
     * A to-many whose members belong to this same aggregate.
     *
     * Asked of the model rather than of the relation. A cascading relation
     * usually is one, but cascade is the ORM being told what to write with
     * what, and a model that states the boundary with @aggregate is stating
     * it - so the statement is what is read, and cascade only answers where
     * nothing was said.
     */
    static boolean owns(EntitySpec e, FieldSpec f) {
        if (!f.isList() || !f.isEntityRef()) return false;

        return e.knowsAggregateOf(f.coreItemType())
                ? e.sameAggregate(f.coreItemType())
                : f.hasDsl("cascade");
    }

    /**
     * The method is named for the element type, not the field.
     *
     * The field names the many ("items", "orderLines") and a method taking a
     * single one should not; the element type is the entity's own name, so
     * the field's plural never has to be guessed at.
     */
    private void add(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Takes a member in. The list itself is never handed out to be replaced. */")
         .varLine("fieldList", "public void add%s(%s member) {", item, item)
         .varLine("fieldList", "    if (member == null) return;")
         .varLine("fieldList", "    if (this.%s == null) this.%s = new java.util.ArrayList<%s>();",
                 f.name(), f.name(), item)
         .varLine("fieldList", "    this.%s.add(member);", f.name());

        String owner = ownerField(f);
        if (owner != null) {
            c.varLine("fieldList", "    %s;", c.syntax().setter("member", owner,
                    DddBehaviorRules.self(f.entity(), c.syntax())));
        }
        c.varLine("fieldList", "}");
    }

    /**
     * The members, to read.
     *
     * A view rather than the list: what the caller holds cannot be added to,
     * so the only ways in are the methods that also set the member's owner.
     * Null stays null, so a root that never had members reads as it did.
     */
    private void view(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** The members, to read. Changed only through add, remove and replace. */")
         .varLine("fieldList", "public java.util.List<%s> get%s() {", item, f.capitalName())
         .varLine("fieldList", "    return this.%s == null ? null : java.util.Collections.unmodifiableList(this.%s);",
                 f.name(), f.name())
         .varLine("fieldList", "}");
    }

    private void remove(SourceCode c, FieldSpec f) {
        String item = f.coreItemType();
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** Lets a member go. Removing it here is what removes it at all. */")
         .varLine("fieldList", "public void remove%s(%s member) {", item, item)
         .varLine("fieldList", "    if (this.%s == null || member == null) return;", f.name())
         .varLine("fieldList", "    this.%s.remove(member);", f.name());
        // The member keeps its reference to this root: it is deleted at the
        // next flush, and a root that promises something is checked then,
        // on what it still holds - which needs the root to be findable.
        c.varLine("fieldList", "}");
    }
}
