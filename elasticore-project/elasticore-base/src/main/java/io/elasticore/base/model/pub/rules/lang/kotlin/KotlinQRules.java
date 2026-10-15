package io.elasticore.base.model.pub.rules.lang.kotlin;

import io.elasticore.base.model.pub.rules.base.RuleText;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.DomainSpec;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The domain's field-metadata holder in Kotlin (kt/Q.tmpl).
 *
 * The Java Q is a class of static members whose nested types are named with a
 * leading $; neither carries over. Kotlin's singleton is an object, its nested
 * factories live in a companion, and $ is not an identifier character here, so
 * the nested type takes a Q prefix instead - Q.QProduct where Java writes
 * Q.$Product.
 *
 * One thing is not merely naming: a Kotlin property compiles to an accessor,
 * so `val productId` and `fun getProductId()` would collide on the JVM. The
 * name property is declared @JvmField, which is what the Java field it stands
 * for is anyway.
 */
public class KotlinQRules implements CodeRules {

    private static final String MEMBERS = "fieldList";

    @Rule(value = "kt-q-vars", target = "Q", order = 10)
    void vars(DomainSpec d) {
        SourceCode c = ctx(d);
        c.requireSyntax(Syntax.KOTLIN, "kt-q-vars");
        c.var("packageName", d.pkg("entity"));
        if (!d.qImports().isEmpty()) c.varLines("importList", d.qImports());

        for (EntitySpec e : d.entities()) {
            if (e.hasMeta("abstract")) continue;
            entityMeta(c, d, e);
        }
        if (!c.hasVarLines(MEMBERS)) c.dontMake();
    }

    /** The nested type that names one entity's fields. */
    private void entityMeta(SourceCode c, DomainSpec d, EntitySpec e) {
        String name = e.getName();
        String holder = String.format("Q%s", name);

        c.varLine(MEMBERS, "@JvmField val %s: %s<%s> = %s()", name, holder, name, holder)
         .varLine(MEMBERS, "@Suppress(\"UNCHECKED_CAST\")")
         .varLine(MEMBERS, "class %s<T%s> {", holder, KotlinSpring.specBound())
         .varLine(MEMBERS, "")
         .varLine(MEMBERS, "    private var p: FieldInfo<*>? = null")
         .varLine(MEMBERS, "    constructor()")
         .varLine(MEMBERS, "    constructor(p: FieldInfo<*>) { this.p = p }")
         .varLine(MEMBERS, "")
         .varLine(MEMBERS, "    companion object {");

        for (String spec : new String[]{"JoinSpec", "UpdateSpec", "DeleteSpec"}) {
            c.varLine(MEMBERS, "        fun %s(): %s<%s> = %s(%s)",
                    RuleText.uncapital(spec), spec, name, spec, entityLiteral(d, name));
        }
        c.varLine(MEMBERS, "    }");

        for (FieldSpec f : e.allFields()) {
            field(c, d, e, f);
        }

        c.varLine(MEMBERS, "}")
         .varLine(MEMBERS, "");
    }

    /** What one field contributes: its metadata, its name, and how it is queried. */
    private void field(SourceCode c, DomainSpec d, EntitySpec e, FieldSpec f) {
        String name = f.name();
        String capital = f.capitalName();

        String baseType = "null";
        if (f.isGenericType()) {
            String declared = f.baseFieldTypeName();
            if (declared != null && !declared.isEmpty()) {
                baseType = classLiteral(declared);
            }
        }

        c.varLine(MEMBERS, "")
         // T is the entity in every use, but only the Java source could say
         // so implicitly: it constructs a raw FieldInfo and lets the unchecked
         // assignment stand. Kotlin has no raw type, so the cast is written.
         .varLine(MEMBERS, "    fun get%s(): FieldInfo<T> ="
                        + " FieldInfo(%s as Class<T>, %s, %s, %s, p)",
                 capital, entityLiteral(d, e.getName()), RuleText.quote(name),
                 itemLiteral(d, e, f.coreItemType()), baseType)
         .varLine(MEMBERS, "    @JvmField val %s: String = %s", name, RuleText.quote(name));

        String type = f.coreItemType();
        if (f.isBaseType() || e.isKnownEnum(type)) {
            c.varLine(MEMBERS, "    fun %s(op: Op, value: Any?): Specification<T> ="
                            + " get%s().where(op, value)", name, capital)
             .varLine(MEMBERS, "    fun %s(op: Op, value: Any?, chkEmpty: Boolean): Specification<T> ="
                            + " get%s().where(op, value, chkEmpty)", name, capital);
            return;
        }

        // Only a single relation can be carried on into; a collection has no
        // one row to continue from.
        if (!f.isList()) {
            String qualifier = null;
            if (e.isKnownEntity(type)) {
                String prefix = d.qPackageOf(type);
                qualifier = (prefix == null || prefix.isEmpty()) ? "" : String.format("%s.Q.", prefix);
            } else {
                String pkg = foreignEntityPackage(type);
                if (pkg != null) qualifier = String.format("%s.Q.", pkg);
            }
            if (qualifier != null) {
                c.varLine(MEMBERS, "    fun %s(): %sQ%s<T> = %sQ%s(get%s())",
                        name, qualifier, type, qualifier, type, capital);
            }
        }
    }

    /** A type named as a value, which Kotlin reaches through its KClass. */
    private String classLiteral(String typeName) {
        return String.format("%s.java", Syntax.KOTLIN.classLiteral(
                Syntax.KOTLIN.typeName(typeName)));
    }

    /**
     * An entity named as a value, written with its package.
     *
     * The holder declares a property per entity - Q.Product - and in
     * expression position that property hides the class of the same name, so
     * an unqualified Product::class.java would be the holder's class rather
     * than the entity's. Java has the same two names and does not care,
     * because its type and value namespaces are separate.
     */
    private String entityLiteral(DomainSpec d, String entityName) {
        String pkg = d.pkg("entity");
        return classLiteral((pkg == null || pkg.isEmpty())
                ? entityName : String.format("%s.%s", pkg, entityName));
    }

    /** The item type of a field, qualified when it is an entity this Q names. */
    private String itemLiteral(DomainSpec d, EntitySpec e, String type) {
        if (e.isKnownEntity(type)) {
            String prefix = d.qPackageOf(type);
            if (prefix == null || prefix.isEmpty()) return entityLiteral(d, type);
            return classLiteral(String.format("%s.%s", prefix, type));
        }
        return classLiteral(type);
    }

    /** The entity package of a type another domain declares, as QBaseRules resolves it. */
    private static String foreignEntityPackage(String typeName) {
        if (typeName == null) return null;
        try {
            io.elasticore.base.ECoreModelContext ctx =
                    io.elasticore.base.model.core.BaseECoreModelContext.getContext();
            io.elasticore.base.model.entity.Entity entity = ctx == null ? null
                    : ctx.findModelComponent(typeName, io.elasticore.base.model.entity.Entity.class);
            if (entity == null) return null;
            return ctx.getDomain(entity.getIdentity().getDomainId())
                    .getModel().getNamespace("entity");
        } catch (Throwable t) {
            return null;
        }
    }
}
