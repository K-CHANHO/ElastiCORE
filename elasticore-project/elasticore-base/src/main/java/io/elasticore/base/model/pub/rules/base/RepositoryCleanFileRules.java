package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The 3.0 clean-mode addition to the repository artifact: the one import
 * {@link RepositoryBaseRules} never reaches for - the generated
 * &lt;Entity&gt;Identity class a composite key's JpaRepository&lt;T, ID&gt;
 * names. Everything else about the interface is mode-agnostic and comes
 * from the shared rule set; 2.x's own template gets there with a wildcard
 * import of the whole entity package, which the clean mode does not use.
 */
public class RepositoryCleanFileRules implements CodeRules {

    @Rule(value = "v3-repository-file", target = "repository", order = 90, phase = Rule.Phase.TAIL)
    void file(EntitySpec e) {
        SourceCode c = ctx(e);
        boolean kotlin = c.syntax() == io.elasticore.base.model.pub.rules.Syntax.KOTLIN;
        boolean any = false;

        if (e.pkCount() > 1) {
            c.addImport("%s.%sIdentity", e.pkg("entity"), e.getName());
            any = true;
        }

        // What the declared queries name. The skeleton imports the Spring
        // Data types; the collection, the Optional and the model's own types
        // a signature uses are this artifact's to add.
        for (io.elasticore.base.model.repo.Method m : e.queryMethods()) {
            if (!m.isEnable()) continue;
            String rt = m.getReturnType();
            if (rt != null && rt.contains("Optional<")) { c.addImport("java.util.Optional"); any = true; }
            if (!kotlin && rt != null && rt.contains("List<")) { c.addImport("java.util.List"); any = true; }
            String arg = RepositoryBaseRules.typeArgument(rt);
            any |= importModelType(c, e, arg);
            if (arg == null) any |= importModelType(c, e, rt);
            io.elasticore.base.model.core.Items<io.elasticore.base.model.entity.Field> ps = m.getParams();
            if (ps == null) continue;
            for (io.elasticore.base.model.entity.Field f : ps.getItemList()) {
                any |= importModelType(c, e, f.getTypeInfo().getDefaultTypeName());
            }
        }
        if (any) c.var("importList", RuleText.stripTrailing(c.renderImports()));
    }

    /** An entity, enum or DTO of the model named by simple name, imported from its package. */
    private boolean importModelType(SourceCode c, EntitySpec e, String type) {
        if (type == null) return false;
        String t = type.trim();
        if (t.isEmpty() || t.contains("<") || t.contains("[") || !Character.isUpperCase(t.charAt(0))) return false;
        if (t.equals(e.getName())) return false;
        String pkg = RuleText.componentPackage(t);
        if (pkg == null || pkg.isEmpty() || pkg.equals(e.pkg("repository"))) return false;
        c.addImport("%s.%s", pkg, t);
        return true;
    }
}
