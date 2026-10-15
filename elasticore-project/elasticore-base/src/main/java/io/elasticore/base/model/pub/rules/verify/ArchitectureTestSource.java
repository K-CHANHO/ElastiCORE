package io.elasticore.base.model.pub.rules.verify;

import io.elasticore.base.model.ECoreModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An ArchUnit test that states the hexagon's boundaries as rules the build
 * checks ({@code archTest: true} in env.yml).
 *
 * Which package is which is read from the namespaces the domain already
 * declares, so the rules are about this project and not about a naming
 * convention:
 *
 *   domain       entity, enumeration, event, domainService, domainRepository
 *   application  service, dto, inboundPort, queryPort
 *   adapters     control
 *   persistence  persistence, repository
 *
 * The domain depends on none of the others; the application depends on
 * neither adapters nor persistence; nothing in the domain names the web. With
 * {@code mapping: xml} the domain also names no persistence technology.
 *
 * Written once: the file is the developer's to add rules to. It needs
 * com.tngtech.archunit:archunit-junit5 on the test classpath.
 */
final class ArchitectureTestSource {

    private ArchitectureTestSource() {
    }

    /** The package the test sits in: the longest one every namespace of the domain shares. */
    static String basePackage(ECoreModel model) {
        List<String> all = new ArrayList<String>();
        for (String key : new String[]{"entity", "service", "dto", "control", "repository"}) {
            String ns = model.getNamespace(key);
            if (ns != null && !ns.isEmpty()) all.add(ns);
        }
        if (all.isEmpty()) return null;
        String[] common = all.get(0).split("\\.");
        int n = common.length;
        for (String ns : all) {
            String[] p = ns.split("\\.");
            int i = 0;
            while (i < n && i < p.length && p[i].equals(common[i])) i++;
            n = i;
        }
        if (n == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append('.');
            sb.append(common[i]);
        }
        return sb.toString();
    }

    static String render(ECoreModel model, String className, String pkg, boolean kotlin, boolean xmlMapping) {
        Set<String> domain = packages(model, "entity", "enumeration", "event", "domainService", "domainRepository");
        Set<String> application = packages(model, "service", "dto", "inboundPort", "queryPort");
        Set<String> adapters = packages(model, "control");
        Set<String> persistence = packages(model, "persistence", "repository");
        // A namespace two layers share is neither's to forbid.
        application.removeAll(domain);
        adapters.removeAll(domain);
        adapters.removeAll(application);
        persistence.removeAll(domain);
        persistence.removeAll(application);

        List<String[]> rules = new ArrayList<String[]>();
        Set<String> outsideDomain = new LinkedHashSet<String>();
        outsideDomain.addAll(application);
        outsideDomain.addAll(adapters);
        outsideDomain.addAll(persistence);
        if (!domain.isEmpty() && !outsideDomain.isEmpty()) {
            rules.add(new String[]{"domainDependsOnNothingOutside",
                    "The domain depends on nothing outside it: not the application, not an adapter, not persistence.",
                    join(domain), join(outsideDomain)});
        }
        Set<String> outsideApplication = new LinkedHashSet<String>();
        outsideApplication.addAll(adapters);
        outsideApplication.addAll(persistence);
        if (!application.isEmpty() && !outsideApplication.isEmpty()) {
            rules.add(new String[]{"applicationDependsOnPortsNotAdapters",
                    "The application reaches the outside through its ports, never an adapter's class.",
                    join(application), join(outsideApplication)});
        }
        if (!domain.isEmpty()) {
            rules.add(new String[]{"domainNamesNoWeb",
                    "Nothing in the domain knows it is served over HTTP.",
                    join(domain), "\"org.springframework.web..\", \"jakarta.servlet..\""});
        }
        if (xmlMapping && !domain.isEmpty()) {
            // Envers has no XML form, so an audited domain keeps its annotations.
            boolean audited = audited(model);
            rules.add(new String[]{"domainNamesNoPersistence",
                    "mapping: xml - the domain names no persistence technology; its mapping is META-INF/orm.xml."
                            + (audited ? " Envers (@Audited) stays: it has no XML form." : ""),
                    join(domain), "\"jakarta.persistence..\", \"javax.persistence..\", \"org.hibernate..\"",
                    audited ? "envers" : ""});
        }
        return kotlin ? kotlin(className, pkg, rules) : java(className, pkg, rules);
    }

    private static String java(String className, String pkg, List<String[]> rules) {
        StringBuilder s = new StringBuilder();
        s.append("// Initially generated by ElastiCORE; no longer managed - do not remove this line.\n")
         .append("package ").append(pkg).append(";\n\n")
         .append("import com.tngtech.archunit.base.DescribedPredicate;\n")
         .append("import com.tngtech.archunit.core.domain.JavaClass;\n")
         .append("import com.tngtech.archunit.core.importer.ImportOption;\n")
         .append("import com.tngtech.archunit.junit.AnalyzeClasses;\n")
         .append("import com.tngtech.archunit.junit.ArchTest;\n")
         .append("import com.tngtech.archunit.lang.ArchRule;\n\n")
         .append("import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;\n\n")
         .append("/**\n")
         .append(" * The hexagon's boundaries, checked on every build. The packages are the\n")
         .append(" * namespaces the domain's env.yml declares. Yours from here: add rules.\n")
         .append(" */\n")
         .append("@AnalyzeClasses(packages = \"").append(pkg).append("\", importOptions = ImportOption.DoNotIncludeTests.class)\n")
         .append("class ").append(className).append(" {\n");
        for (String[] r : rules) {
            s.append("\n    /** ").append(r[1]).append(" */\n")
             .append("    @ArchTest\n")
             .append("    static final ArchRule ").append(r[0]).append(" = noClasses()\n")
             .append("            .that().resideInAnyPackage(").append(r[2]).append(")\n")
             .append("            .should().").append(should(r)).append(";\n");
        }
        s.append("}\n");
        return s.toString();
    }

    private static String kotlin(String className, String pkg, List<String[]> rules) {
        StringBuilder s = new StringBuilder();
        s.append("// Initially generated by ElastiCORE; no longer managed - do not remove this line.\n")
         .append("package ").append(pkg).append("\n\n")
         .append("import com.tngtech.archunit.base.DescribedPredicate\n")
         .append("import com.tngtech.archunit.core.domain.JavaClass\n")
         .append("import com.tngtech.archunit.core.importer.ImportOption\n")
         .append("import com.tngtech.archunit.junit.AnalyzeClasses\n")
         .append("import com.tngtech.archunit.junit.ArchTest\n")
         .append("import com.tngtech.archunit.lang.ArchRule\n")
         .append("import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses\n\n")
         .append("/**\n")
         .append(" * The hexagon's boundaries, checked on every build. The packages are the\n")
         .append(" * namespaces the domain's env.yml declares. Yours from here: add rules.\n")
         .append(" */\n")
         .append("@AnalyzeClasses(packages = [\"").append(pkg).append("\"], importOptions = [ImportOption.DoNotIncludeTests::class])\n")
         .append("class ").append(className).append(" {\n");
        for (String[] r : rules) {
            s.append("\n    /** ").append(r[1]).append(" */\n")
             .append("    @ArchTest\n")
             .append("    val ").append(r[0]).append(": ArchRule = noClasses()\n")
             .append("        .that().resideInAnyPackage(").append(r[2]).append(")\n")
             .append("        .should().").append(should(r)).append("\n");
        }
        s.append("}\n");
        return s.toString();
    }

    /** What the rule forbids depending on: the packages, less Envers where the domain is audited. */
    private static String should(String[] r) {
        if (r.length < 5 || !"envers".equals(r[4])) {
            return "dependOnClassesThat().resideInAnyPackage(" + r[3] + ")";
        }
        return "dependOnClassesThat(JavaClass.Predicates.resideInAnyPackage(" + r[3] + ")"
                + ".and(DescribedPredicate.not(JavaClass.Predicates.resideInAPackage(\"org.hibernate.envers..\"))))";
    }

    private static boolean audited(ECoreModel model) {
        io.elasticore.base.model.ModelComponentItems<io.elasticore.base.model.entity.Entity> all =
                model.getEntityModels().getItems();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getMetaInfo().hasMetaAnnotation("audited")) return true;
        }
        return false;
    }

    private static Set<String> packages(ECoreModel model, String... keys) {
        Set<String> out = new LinkedHashSet<String>();
        for (String k : keys) {
            String ns = model.getNamespace(k);
            if (ns != null && !ns.isEmpty()) out.add(ns);
        }
        return out;
    }

    private static String join(Set<String> packages) {
        StringBuilder sb = new StringBuilder();
        for (String p : packages) {
            // "a.b.." already covers "a.b.c..".
            boolean covered = false;
            for (String other : packages) {
                if (!other.equals(p) && p.startsWith(other + ".")) covered = true;
            }
            if (covered) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append('"').append(p).append("..\"");
        }
        return sb.toString();
    }
}
