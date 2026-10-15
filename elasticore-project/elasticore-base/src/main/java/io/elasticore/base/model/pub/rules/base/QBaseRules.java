package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.DomainSpec;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The Q class, ported from the second artifact EntitySrcFilePublisher emits.
 *
 * One class per domain, holding a nested type per entity that names the
 * entity's fields: each field offers the metadata a Specification is built
 * from, its own name as a constant, and - for a relation - a way to carry on
 * into the entity it points at.
 *
 * An @abstract entity has no table to query, so it contributes nothing.
 */
public class QBaseRules implements CodeRules {

    /** The slot the Q class's members are written into. */
    private static final String MEMBERS = "fieldList";

    /** The entity package of a cross-domain relation target, or null. */
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

    @Rule(value = "q-vars", target = "Q", order = 10)
    void vars(DomainSpec d) {
        SourceCode c = ctx(d);

        for (EntitySpec e : d.entities()) {
            if (e.hasMeta("abstract")) continue;
            entityMeta(c, d, e);
        }

        if (!c.hasVarLines(MEMBERS)) {
            c.dontMake();
            return;
        }

        c.var("packageName", d.pkg("entity"));

        if (!d.qImports().isEmpty()) c.varLines("importList", d.qImports());
    }

    /** The nested type that names one entity's fields. */
    private void entityMeta(SourceCode c, DomainSpec d, EntitySpec e) {
        String name = e.getName();

        c.varLine(MEMBERS, "public static $%s<%s> %s=new $%s<>();", name, name, name, name)
         .varLine(MEMBERS, "public static class $%s<T> {", name)
         .varLine(MEMBERS, "")
         .varLine(MEMBERS, "    private FieldInfo p=null;")
         .varLine(MEMBERS, "    public $%s() {}", name)
         .varLine(MEMBERS, "    public $%s(FieldInfo p) {this.p=p;}", name);

        for (String spec : new String[]{"JoinSpec", "UpdateSpec", "DeleteSpec"}) {
            c.varLine(MEMBERS, "    public static %s<%s> %s() {", spec, name, RuleText.uncapital(spec))
             .varLine(MEMBERS, "    \treturn new %s<>(%s.class);", spec, name)
             .varLine(MEMBERS, "    }");
        }

        // Inherited columns are named too: the class describes the table,
        // not the declaration.
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

        // Only a generic declaration carries a base type worth naming.
        String baseType = "null";
        if (f.isGenericType()) {
            String declared = f.baseFieldTypeName();
            if (declared != null && !declared.isEmpty()) {
                baseType = String.format("%s.class", declared);
            }
        }

        c.varLine(MEMBERS, "")
         .varLine(MEMBERS, "    public final FieldInfo<T> get%s()"
                        + " {return new FieldInfo(%s.class,%s,%s.class, %s,p);}",
                 capital, e.getName(), RuleText.quote(name), f.coreItemType(), baseType)
         .varLine(MEMBERS, "    public final String %s=%s;", name, RuleText.quote(name));

        String type = f.coreItemType();
        if (f.isBaseType() || e.isKnownEnum(type)) {
            c.varLine(MEMBERS, "    public Specification<T> %s(Op op,Object value)"
                            + " {return get%s().where(op,value);}", name, capital)
             .varLine(MEMBERS, "    public Specification<T> %s(Op op,Object value,boolean chkEmpty)"
                            + " {return get%s().where(op,value,chkEmpty);}", name, capital);
            return;
        }

        // Only a single relation can be carried on into. A collection has no
        // one row to continue from, and 2.x resolves the reference from the
        // declared type, which for a list never names an entity.
        //
        // A self-reference is a relation like any other, and is named too.
        if (!f.isList()) {
            // A relation continues into the other entity's own nested type.
            // One in another domain is reached through that domain's Q, since
            // an unqualified name would resolve to this one. The entity
            // resolves across every loaded domain, as 2.x resolves it.
            String qualifier = null;
            if (e.isKnownEntity(type)) {
                String prefix = d.qPackageOf(type);
                qualifier = (prefix == null || prefix.isEmpty()) ? "" : String.format("%s.Q.", prefix);
            } else {
                String pkg = foreignEntityPackage(type);
                if (pkg != null) qualifier = String.format("%s.Q.", pkg);
            }
            if (qualifier != null) {
                c.varLine(MEMBERS, "    public final %s$%s<T> %s() {return new %s$%s(get%s());}",
                        qualifier, type, name, qualifier, type, capital);
            }
        }
    }
}
