package io.elasticore.base.model.pub.rules.ddd;

import io.elasticore.base.model.pub.rules.ArtifactContributor;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.ContributedArtifact;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;
import io.elasticore.base.model.pub.rules.base.RuleText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * What happened, said by the aggregate it happened to.
 *
 * The fourth of Vernon's aggregate rules - consistency outside the boundary
 * is eventual - has no way to be reached without this: the other rules cut
 * the write path between aggregates, and something has to carry the fact
 * across afterwards. An event is that something.
 *
 * The DSL says which ones exist and nothing else:
 *
 *   Order:
 *     meta: entity @root @expose(30) @event(Created, Cancelled)
 *
 * Each name becomes a class - OrderCreatedEvent, OrderCancelledEvent -
 * carrying the aggregate's identity and when it happened, and the root gains
 * a raise method per event plus the two accessors Spring Data reads to
 * publish them when the aggregate is next saved.
 *
 * What is deliberately not derived is *when*. A generator can see that an
 * order can be cancelled; it cannot see what cancelling one means, and a
 * raise call placed by guesswork would fire on rows that were merely
 * updated. So the root offers raiseCancelled() and the domain decides where
 * it is called - in the service extension the developer owns, which is where
 * that decision belongs anyway.
 *
 * Roots only. A member has no independent life to have events about, and the
 * boundary rules already took away everything else that would reach it.
 *
 * Java only: the plumbing is written as class-body lines, which is the same
 * mechanism {@link DddCollectionRules} explains it has no Kotlin form of yet.
 */
public class DddDomainEventRules implements CodeRules, ArtifactContributor {

    private static final String JPA = "jakarta.persistence.%s";
    private static final String SPRING = "org.springframework.data.domain.%s";

    /** One class per declared event, beside the domain model it belongs to. */
    @Override
    public List<ContributedArtifact> artifactsFor(EntitySpec e) {
        List<String> events = declaredEvents(e);
        if (events.isEmpty()) return Collections.emptyList();

        List<ContributedArtifact> out = new ArrayList<ContributedArtifact>();
        for (String event : events) {
            out.add(ContributedArtifact.of("event", "event", className(e, event))
                    .orNamespace("entity"));
        }
        return out;
    }

    /**
     * The event class: the identity of what it happened to, and when.
     *
     * Identity rather than the aggregate itself, for the same reason a
     * reference across the boundary is by identity - a handler receiving the
     * object would be handed a way to write through it, in a transaction
     * that has already ended.
     */
    @Rule(value = "ddd-event", target = "event", order = 10)
    void eventClass(EntitySpec e) {
        SourceCode c = ctx(e);
        c.requireSyntaxAnyOf("ddd-event", Syntax.JAVA, Syntax.KOTLIN);

        boolean kotlin = c.syntax() == Syntax.KOTLIN;
        String name = c.artifactName();
        c.addImport("java.time.Instant");

        // The identity, then what the declaration asked the event to carry:
        // @event(Confirmed(totalAmount, shipWarehouse)).
        List<FieldSpec> pks = new ArrayList<FieldSpec>(e.pkFields());
        String eventName = name.substring(e.getName().length(), name.length() - "Event".length());
        for (FieldSpec f : payloadOf(e, eventName)) {
            pks.add(f);
            RuleText.registerFieldTypeImport(c, e, f, eventPackage(e));
        }

        // What the event carries is the same in both; how a class says "these
        // are its values and they do not change" is not. Kotlin declares them
        // in the header and gets the accessors with them; Java writes the
        // field, the parameter, the assignment and the getter separately.
        if (kotlin) {
            // The skeleton owns the parentheses, because a template slot only
            // takes a whole line: a ${bodyList} sitting mid-line is dropped
            // along with the line it was in, which is how the first Kotlin
            // event came out as a javadoc and nothing else.
            for (FieldSpec f : pks) {
                c.varLine("bodyList", "val %s: %s?,", f.name(), f.typeName());
            }
            c.varLine("bodyList", "/** When the aggregate said so, not when a handler got round to it. */")
             .varLine("bodyList", "val occurredOn: Instant = Instant.now()");
            describe(c, e, name);
            return;
        }

        StringBuilder params = new StringBuilder();
        for (FieldSpec f : pks) {
            c.varLine("bodyList", "private final %s %s;", f.typeName(), f.name());
            c.varLine("bodyList", "");
            if (params.length() > 0) params.append(", ");
            params.append(String.format("%s %s", f.typeName(), f.name()));
        }
        c.varLine("bodyList", "private final Instant occurredOn;");
        c.varLine("bodyList", "");

        // The moment is handed in by the root that raised it. The event is
        // built later - at publication, once the row has its identity - so an
        // Instant.now() taken here would be the publication time and the
        // accessor below would be answering a different question than it
        // claims. The one-argument form stays for code that builds one itself.
        StringBuilder args = new StringBuilder();
        for (FieldSpec f : pks) {
            if (args.length() > 0) args.append(", ");
            args.append(f.name());
        }
        c.varLine("bodyList", "public %s(%s) {", name, params.toString());
        c.varLine("bodyList", "    this(%s%sInstant.now());", args.toString(), args.length() > 0 ? ", " : "");
        c.varLine("bodyList", "}");
        c.varLine("bodyList", "");
        c.varLine("bodyList", "public %s(%s%sInstant occurredOn) {", name, params.toString(),
                params.length() > 0 ? ", " : "");
        for (FieldSpec f : pks) {
            c.varLine("bodyList", "    this.%s = %s;", f.name(), f.name());
        }
        c.varLine("bodyList", "    this.occurredOn = occurredOn;");
        c.varLine("bodyList", "}");

        for (FieldSpec f : pks) {
            c.varLine("bodyList", "");
            c.varLine("bodyList", "public %s get%s() {", f.typeName(), f.capitalName());
            c.varLine("bodyList", "    return this.%s;", f.name());
            c.varLine("bodyList", "}");
        }
        c.varLine("bodyList", "");
        c.varLine("bodyList", "/** When the aggregate said so, not when a handler got round to it. */");
        c.varLine("bodyList", "public Instant getOccurredOn() {");
        c.varLine("bodyList", "    return this.occurredOn;");
        c.varLine("bodyList", "}");

        // An event is a value: two announcements of the same thing at the same
        // moment are equal, and a log line or a failed assertion shows what it
        // says rather than an object address.
        StringBuilder same = new StringBuilder();
        StringBuilder hashed = new StringBuilder();
        StringBuilder shown = new StringBuilder();
        for (FieldSpec f : pks) {
            same.append(String.format("java.util.Objects.equals(%s, other.%s) && ", f.name(), f.name()));
            hashed.append(f.name()).append(", ");
            shown.append(String.format("\"%s=\" + %s + \", \" + ", f.name(), f.name()));
        }
        c.varLine("bodyList", "");
        c.varLine("bodyList", "@Override");
        c.varLine("bodyList", "public boolean equals(Object o) {");
        c.varLine("bodyList", "    if (this == o) return true;");
        c.varLine("bodyList", "    if (!(o instanceof %s)) return false;", name);
        c.varLine("bodyList", "    %s other = (%s) o;", name, name);
        c.varLine("bodyList", "    return %sjava.util.Objects.equals(occurredOn, other.occurredOn);", same.toString());
        c.varLine("bodyList", "}");
        c.varLine("bodyList", "");
        c.varLine("bodyList", "@Override");
        c.varLine("bodyList", "public int hashCode() {");
        c.varLine("bodyList", "    return java.util.Objects.hash(%soccurredOn);", hashed.toString());
        c.varLine("bodyList", "}");
        c.varLine("bodyList", "");
        c.varLine("bodyList", "@Override");
        c.varLine("bodyList", "public String toString() {");
        c.varLine("bodyList", "    return \"%s{\" + %s\"occurredOn=\" + occurredOn + \"}\";", name, shown.toString());
        c.varLine("bodyList", "}");

        describe(c, e, name);
    }

    /**
     * The header every event file needs, whichever language wrote its body.
     *
     * Its own method because the Kotlin branch returns early - and the first
     * version of it returned before this ran, which produced a file with a
     * javadoc, no package, no name and no body. A shared tail is harder to
     * skip by accident than four calls repeated in two places.
     */
    private void describe(SourceCode c, EntitySpec e, String name) {
        c.var("packageName", eventPackage(e))
         .var("className", name)
         .var("classAnnotationList", c.renderClassAnnotations())
         .var("importList", RuleText.stripTrailing(c.renderImports()));
    }

    /**
     * What the root gains: somewhere to hold what it has said, a raise method
     * per declared event, and the pair of accessors Spring Data JPA reads.
     *
     * The accessors are the whole reason this is worth generating. A domain
     * event that has to be published by hand is published from wherever the
     * publisher happens to be injected, which is usually a service - and by
     * then it is a service event, raised whether or not the save succeeded.
     * Read off the aggregate at save time, it is published if and only if
     * the row was written.
     *
     * Ordered after the file shell at 90, which replaces the field slot's
     * paragraph outright and would drop anything already appended to it.
     */
    @Rule(value = "ddd-event-source", target = "entity", order = 95, phase = Rule.Phase.TAIL)
    void aggregateRaisesEvents(EntitySpec e) {
        List<String> events = declaredEvents(e);
        if (events.isEmpty()) return;

        SourceCode c = ctx(e);
        if (c.syntax() == Syntax.KOTLIN) {
            raiseEventsKt(c, e, events);
            return;
        }
        if (c.syntax() != Syntax.JAVA) return;

        c.varLine("fieldList", "")
         .varLine("fieldList", "/** What this aggregate has said and has not had published yet. */")
         .varLine("fieldList", "@Transient")
         .varLine("fieldList", "@lombok.Getter(lombok.AccessLevel.NONE)")
         .varLine("fieldList", "private final transient java.util.List<Supplier<Object>> domainEvents")
         .varLine("fieldList", "        = new java.util.ArrayList<Supplier<Object>>();")
         .varLine("fieldList", "")
         .varLine("fieldList", "/**")
         .varLine("fieldList", " * Records something that happened, to be published when this")
         .varLine("fieldList", " * aggregate is next saved.")
         .varLine("fieldList", " *")
         .varLine("fieldList", " * What is kept is how to build the event, not the event: a row")
         .varLine("fieldList", " * being inserted has no identity until it is written, and an event")
         .varLine("fieldList", " * built when the domain spoke would carry a null one. Built at")
         .varLine("fieldList", " * publication instead, it carries the identity the row now has.")
         .varLine("fieldList", " */")
         .varLine("fieldList", "public void registerEvent(Supplier<Object> event) {")
         .varLine("fieldList", "    if (event != null) this.domainEvents.add(event);")
         .varLine("fieldList", "}");

        for (String event : events) {
            c.varLine("fieldList", "")
             .varLine("fieldList", "/** Says that this %s was %s. Called by the domain, never from here. */",
                     e.getName(), event.toLowerCase())
             .varLine("fieldList", "public void raise%s() {", event)
             // The moment is taken now, the identity later: the row may not
             // have one until it is written, but it happened when it was said.
             .varLine("fieldList", "    java.time.Instant occurredOn = java.time.Instant.now();");
            // What it carries is also taken now: the state the domain was in
            // when it spoke, not whatever it is by the time of the flush.
            StringBuilder args = new StringBuilder(identityArguments(e));
            for (FieldSpec f : payloadOf(e, event)) {
                c.varLine("fieldList", "    final %s %sAtRaise = this.%s;", f.typeName(), f.name(), f.name());
                if (args.length() > 0) args.append(", ");
                args.append(f.name()).append("AtRaise");
            }
            c.varLine("fieldList", "    registerEvent(() -> new %s(%s%soccurredOn));", className(e, event), args,
                     args.length() == 0 ? "" : ", ")
             .varLine("fieldList", "}");
        }

        c.varLine("fieldList", "")
         .varLine("fieldList", "@DomainEvents")
         .varLine("fieldList", "java.util.Collection<Object> domainEvents() {")
         .varLine("fieldList", "    java.util.List<Object> raised = new java.util.ArrayList<Object>();")
         .varLine("fieldList", "    for (Supplier<Object> event : this.domainEvents) raised.add(event.get());")
         .varLine("fieldList", "    return raised;")
         .varLine("fieldList", "}")
         .varLine("fieldList", "")
         .varLine("fieldList", "@AfterDomainEventPublication")
         .varLine("fieldList", "void clearDomainEvents() {")
         .varLine("fieldList", "    this.domainEvents.clear();")
         .varLine("fieldList", "}");
    }

    /**
     * The same plumbing in Kotlin.
     *
     * The one difference that is not spelling: Lombok is what withholds the
     * getter in Java, and there is no Lombok here - the backing list is a
     * private val, which withholds it by being private. Everything else is
     * the same statement: what has been said, how to say it, and the pair of
     * accessors Spring Data reads at save time.
     */
    private void raiseEventsKt(SourceCode c, EntitySpec e, List<String> events) {
        c.varLine("fieldList", "")
         .varLine("fieldList", "/** What this aggregate has said and has not had published yet. */")
         .varLine("fieldList", "@Transient")
         .varLine("fieldList", "private val domainEvents: MutableList<Supplier<Any>> = mutableListOf()")
         .varLine("fieldList", "")
         .varLine("fieldList", "/**")
         .varLine("fieldList", " * Records something that happened, to be published when this")
         .varLine("fieldList", " * aggregate is next saved.")
         .varLine("fieldList", " *")
         .varLine("fieldList", " * What is kept is how to build the event, not the event: a row")
         .varLine("fieldList", " * being inserted has no identity until it is written, and an event")
         .varLine("fieldList", " * built when the domain spoke would carry a null one. Built at")
         .varLine("fieldList", " * publication instead, it carries the identity the row now has.")
         .varLine("fieldList", " */")
         .varLine("fieldList", "fun registerEvent(event: Supplier<Any>?) {")
         .varLine("fieldList", "    if (event != null) this.domainEvents.add(event)")
         .varLine("fieldList", "}");

        for (String event : events) {
            c.varLine("fieldList", "")
             .varLine("fieldList", "/** Says that this %s was %s. Called by the domain, never from here. */",
                     e.getName(), event.toLowerCase())
             .varLine("fieldList", "fun raise%s() {", event)
             .varLine("fieldList", "    val occurredOn = java.time.Instant.now()");
            StringBuilder args = new StringBuilder(identityArgumentsKt(e));
            for (FieldSpec f : payloadOf(e, event)) {
                c.varLine("fieldList", "    val %sAtRaise = this.%s", f.name(), f.name());
                if (args.length() > 0) args.append(", ");
                args.append(f.name()).append("AtRaise");
            }
            c.varLine("fieldList", "    registerEvent(Supplier { %s(%s%soccurredOn) })", className(e, event), args,
                     args.length() == 0 ? "" : ", ")
             .varLine("fieldList", "}");
        }

        c.varLine("fieldList", "")
         .varLine("fieldList", "@DomainEvents")
         .varLine("fieldList", "fun domainEvents(): Collection<Any> =")
         .varLine("fieldList", "    this.domainEvents.map { it.get() }")
         .varLine("fieldList", "")
         .varLine("fieldList", "@AfterDomainEventPublication")
         .varLine("fieldList", "fun clearDomainEvents() {")
         .varLine("fieldList", "    this.domainEvents.clear()")
         .varLine("fieldList", "}");
    }

    /**
     * What the plumbing refers to, registered before the shell writes the
     * import block.
     *
     * Separate from the rule that writes the lines because the two halves
     * sit on opposite sides of the file shell at 90: it renders the imports
     * and then replaces the field slot's paragraph, so an import added after
     * it is dropped and a line added before it is overwritten.
     */
    @Rule(value = "ddd-event-imports", target = "entity", order = 85, phase = Rule.Phase.TAIL)
    void eventImports(EntitySpec e) {
        List<String> events = declaredEvents(e);
        if (events.isEmpty()) return;

        SourceCode c = ctx(e);
        if (c.syntax() != Syntax.JAVA && c.syntax() != Syntax.KOTLIN) return;

        c.addImport(JPA, "Transient");
        c.addImport(SPRING, "DomainEvents");
        c.addImport(SPRING, "AfterDomainEventPublication");
        c.addImport("java.util.function.Supplier");

        // The events may be placed in a package of their own - a layered
        // project usually does - and the root that raises them is then
        // naming a type it cannot see.
        String eventPkg = eventPackage(e);
        if (eventPkg != null && !eventPkg.equals(e.pkg("entity"))) {
            for (String event : events) {
                c.addImport("%s.%s", eventPkg, className(e, event));
            }
        }
    }

    /** Where the events go: their own namespace, or the domain model's. */
    static String eventPackage(EntitySpec e) {
        return e.eventPackage();
    }

    /**
     * The event names the entity declares, in order.
     *
     * A member's declaration is ignored rather than refused: an entity moved
     * inside a boundary should not stop generating, and everything else the
     * DDD rules do to a member is a subtraction too.
     */
    static List<String> declaredEvents(EntitySpec e) {
        return e.events();
    }

    private static final java.util.Set<String> PAYLOAD_WARNED =
            java.util.Collections.synchronizedSet(new java.util.HashSet<String>());

    /** The declaration split at the commas that separate events, not those inside one's payload. */
    private static List<String> topLevel(String declared) {
        List<String> parts = new ArrayList<String>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (char ch : declared.toCharArray()) {
            if (ch == '(') depth++;
            if (ch == ')') depth--;
            if (ch == ',' && depth == 0) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        parts.add(cur.toString());
        return parts;
    }

    /**
     * The fields an event carries besides the identity, as the declaration
     * names them: Confirmed(totalAmount, shipWarehouse).
     *
     * A value of the root - a column, an enum, an embedded value - is what an
     * event can honestly carry. A reference to another aggregate or a member
     * collection is not: the event would hand out a way into the model after
     * the transaction ended. Those are refused with a DSL warning; carry the
     * other aggregate's identity in a column of its own instead.
     */
    List<FieldSpec> payloadOf(EntitySpec e, String event) {
        List<FieldSpec> out = new ArrayList<FieldSpec>();
        String declared = e.metaValue("event");
        if (declared == null) return out;
        for (String part : topLevel(declared)) {
            String t = part.trim();
            int open = t.indexOf('(');
            if (open < 0 || !t.endsWith(")")) continue;
            String name = t.substring(0, open).trim();
            if (!name.equalsIgnoreCase(event)) continue;
            for (String fieldName : t.substring(open + 1, t.length() - 1).split(",")) {
                String fn = fieldName.trim();
                if (fn.isEmpty()) continue;
                FieldSpec f = null;
                for (FieldSpec candidate : e.fields()) {
                    if (candidate.name().equals(fn)) { f = candidate; break; }
                }
                if (f == null || f.isList() || f.isEntityRef() || f.hasDslAny("id", "pk")) {
                    if (!PAYLOAD_WARNED.add(e.domainClassName() + "." + e.getName() + "." + name + "." + fn)) continue;
                    io.elasticore.base.util.ConsoleLog.storeLog("DSL_WARN", String.format(
                            "[%s] %s.@event(%s(...)): '%s' %s - an event carries values of the root only.",
                            e.domainClassName(), e.getName(), name, fn,
                            f == null ? "is not a field of it"
                                    : f.isList() ? "is a collection"
                                    : f.isEntityRef() ? "is another aggregate; carry its identity in a column"
                                    : "is the identity, which every event already carries"));
                    continue;
                }
                out.add(f);
            }
        }
        return out;
    }

    static String className(EntitySpec e, String event) {
        return e.eventClassName(event);
    }

    /** The identity handed to an event, as Kotlin names its own properties. */
    private String identityArgumentsKt(EntitySpec e) {
        StringBuilder args = new StringBuilder();
        for (FieldSpec f : e.pkFields()) {
            if (args.length() > 0) args.append(", ");
            args.append("this.").append(f.name());
        }
        return args.toString();
    }

    /** The identity handed to an event, as the root reads it off itself. */
    private String identityArguments(EntitySpec e) {
        StringBuilder args = new StringBuilder();
        for (FieldSpec f : e.pkFields()) {
            if (args.length() > 0) args.append(", ");
            args.append("this.").append(f.name());
        }
        return args.toString();
    }
}
