package io.elasticore.base.model.pub.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Puts annotations on the declarations of a rendered source file: on its type
 * and on its methods, found by name, with the imports they need.
 *
 * A rule sees the model, not the template, and many templates write their
 * methods as text a rule never touches - the repository's queries, the port's
 * operations. Placing what a project rule asked for on the written text is
 * what lets the rule say "this method" without knowing how the method got
 * there.
 */
final class DeclarationText {

    private static final Pattern NOT_A_DECLARATION = Pattern.compile(
            "^\\s*(?:return|new|throw|else|if|for|while|case|assert|yield)\\b.*");

    /**
     * Class#method pairs already reported as not found, so a warning is said
     * once per run. Cleared at the start of each run ({@link #resetWarnings}):
     * a daemon that outlives one build must say it again in the next.
     */
    private static final Set<String> WARNED = java.util.Collections.synchronizedSet(new HashSet<String>());

    /** Stands for a type the file declares itself: its name is taken, and nothing imports it. */
    private static final String DECLARED_HERE = "";

    static void resetWarnings() {
        WARNED.clear();
    }

    private final List<String> lines;
    private final String eol;
    private final Syntax syntax;
    private final String target;
    private final String typeName;
    private final Set<String> newImports = new LinkedHashSet<String>();
    private final Map<String, String> importedBySimpleName = new HashMap<String, String>();
    private final Set<String> wildcardPackages = new HashSet<String>();

    DeclarationText(String text, Syntax syntax, String target) {
        this.eol = text.contains("\r\n") ? "\r\n" : "\n";
        this.lines = new ArrayList<String>(java.util.Arrays.asList(text.split("\r?\n", -1)));
        this.syntax = syntax;
        this.target = target;
        Pattern type = syntax == Syntax.KOTLIN ? KOTLIN_TYPE_NAME : JAVA_TYPE_NAME;
        String first = null;
        for (String line : lines) {
            String t = line.trim();
            Matcher declared = type.matcher(line);
            if (declared.find()) {
                if (first == null) first = declared.group(1);
                importedBySimpleName.put(declared.group(1), DECLARED_HERE);
            }
            if (!t.startsWith("import ")) continue;
            String fqcn = t.substring("import ".length()).replace(";", "").trim();
            if (fqcn.startsWith("static ")) continue;
            int alias = fqcn.indexOf(" as ");
            if (alias > 0) {
                // Kotlin's import a.B as C takes the name C.
                importedBySimpleName.put(fqcn.substring(alias + 4).trim(), fqcn.substring(0, alias).trim());
                continue;
            }
            if (fqcn.endsWith(".*")) {
                wildcardPackages.add(fqcn.substring(0, fqcn.length() - 2));
            } else {
                importedBySimpleName.put(simpleName(fqcn), fqcn);
            }
        }
        this.typeName = first == null ? target : first;
    }

    private static final Pattern JAVA_TYPE_NAME = Pattern.compile(
            "^\\s*(?:@[\\w.]+(?:\\([^()]*\\))?\\s+)*(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp)\\s+)*"
                    + "(?:class|interface|enum|record|@interface)\\s+(\\w+)");
    private static final Pattern KOTLIN_TYPE_NAME = Pattern.compile(
            "^\\s*(?:@[\\w.]+(?:\\([^()]*\\))?\\s+)*(?:(?:public|internal|private|open|abstract|final|sealed|data|enum|annotation|inner|value)\\s+)*"
                    + "(?:class|interface|object)\\s+(\\w+)");

    /** Imports the types unless the file imports them, or another type of the same name, already. */
    DeclarationText ensureImports(Collection<String> types) {
        for (String fqcn : types) {
            if (importedBySimpleName.containsKey(simpleName(fqcn))) continue;
            if (coveredByWildcard(fqcn) || !needsImport(fqcn)) continue;
            newImports.add(fqcn);
            importedBySimpleName.put(simpleName(fqcn), fqcn);
        }
        return this;
    }

    /** The annotations above the file's first type declaration. */
    DeclarationText annotateType(Collection<AnnoCode> annotations) {
        if (annotations.isEmpty()) return this;
        Pattern type = syntax == Syntax.KOTLIN ? KOTLIN_TYPE_NAME : JAVA_TYPE_NAME;
        for (int i = 0; i < lines.size(); i++) {
            if (type.matcher(lines.get(i)).find()) {
                insertAbove(i, annotations);
                return this;
            }
        }
        if (WARNED.add(typeName + "#class")) {
            io.elasticore.base.util.ConsoleLog.printWarn(String.format(
                    "[rule] no class declaration found in %s (%s file); its class annotations were not written",
                    typeName, target));
        }
        return this;
    }

    /** The annotations above every declaration of each named method. */
    DeclarationText annotateMethods(Map<String, List<AnnoCode>> byMethod) {
        for (Map.Entry<String, List<AnnoCode>> en : byMethod.entrySet()) {
            Pattern declaration = methodPattern(en.getKey());
            List<Integer> found = new ArrayList<Integer>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!declaration.matcher(line).matches()) continue;
                if (NOT_A_DECLARATION.matcher(line).matches()) continue;
                Matcher at = declaration.matcher(line);
                at.matches();
                // What precedes the name, less the annotations sharing the
                // line: their arguments (readOnly = true) are not an assignment.
                String head = line.substring(0, at.start(1))
                        .replaceFirst("^\\s*(?:@[\\w.]+(?:\\([^()\"]*\\))?\\s+)*", "");
                // An assignment, a comment, or a name inside a string - an
                // annotation's query text calling count( or sum( - is not
                // the method.
                if (head.contains("=") || head.contains("\"") || head.trim().startsWith("*")
                        || head.trim().startsWith("//")) continue;
                found.add(i);
            }
            if (found.isEmpty()) {
                if (WARNED.add(typeName + "#" + en.getKey())) {
                    io.elasticore.base.util.ConsoleLog.printWarn(String.format(
                            "[rule] method '%s' is not declared in %s (%s file); its annotations were not written",
                            en.getKey(), typeName, target));
                }
                continue;
            }
            // From the bottom, so an insertion does not move the lines still to visit.
            for (int k = found.size() - 1; k >= 0; k--) insertAbove(found.get(k), en.getValue());
        }
        return this;
    }

    String text() {
        if (!newImports.isEmpty()) addImports();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append(eol);
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private Pattern methodPattern(String name) {
        String quoted = Pattern.quote(name);
        // Annotations may share the declaration's line, with arguments that
        // hold no string: a quoted argument is a query, not a modifier.
        String annotations = "(?:@[\\w.]+(?:\\([^()\"]*\\))?\\s+)*";
        if (syntax == Syntax.KOTLIN) {
            return Pattern.compile("^\\s*" + annotations
                    + "(?:(?:override|open|suspend|public|private|protected|internal|abstract|final|inline|operator"
                    + "|tailrec|infix|external)\\s+)*"
                    + "fun\\s+(?:<[^>]*>\\s*)?(?:[\\w.<>?]+\\.)?(`?" + quoted + "`?)\\s*\\(.*");
        }
        return Pattern.compile("^\\s*" + annotations + "[\\w$.<>\\[\\]?,\\s]*[\\w$>\\]?]\\s+(" + quoted + ")\\s*\\(.*");
    }

    /** Inserts each annotation the declaration does not carry yet, in its indent. */
    private void insertAbove(int index, Collection<AnnoCode> annotations) {
        String declaration = lines.get(index);
        Matcher m = Pattern.compile("^(\\s*)").matcher(declaration);
        String indent = m.find() ? m.group(1) : "";
        List<String> present = annotationsAbove(index);
        List<String> add = new ArrayList<String>();
        for (AnnoCode a : annotations) {
            if (present.contains(a.getFqcn())) continue;
            if (present.contains(a.simpleName())) {
                // The same simple name is only the same annotation when the
                // file's name for it is this type: an other.Timed already on
                // the method does not stand for micrometer's.
                String known = importedBySimpleName.get(a.simpleName());
                if (known == null || known.equals(a.getFqcn()) || coveredByWildcard(a.getFqcn())) continue;
            }
            add.add(indent + render(a));
        }
        lines.addAll(index, add);
    }

    /** The names of the annotations directly above a declaration. */
    private List<String> annotationsAbove(int index) {
        List<String> names = new ArrayList<String>();
        for (int i = index - 1; i >= 0; i--) {
            String t = lines.get(i).trim();
            // Only a line that is an annotation and nothing else: the one
            // above may be a declaration of its own that starts with one.
            if (!t.matches("@[\\w.]+(?:\\(.*\\))?")) break;
            Matcher m = Pattern.compile("^@([\\w.]+)").matcher(t);
            if (m.find()) names.add(m.group(1));
        }
        // An annotation written on the declaration's own line.
        Matcher inline = Pattern.compile("@([\\w.]+)").matcher(lines.get(index));
        while (inline.find()) names.add(inline.group(1));
        return names;
    }

    /** Written by simple name where the file can import it, in full where it cannot. */
    private String render(AnnoCode a) {
        Set<String> types = new LinkedHashSet<String>();
        a.collectTypes(types);
        final Set<String> qualified = new HashSet<String>();
        for (String fqcn : types) {
            String simple = simpleName(fqcn);
            String taken = importedBySimpleName.get(simple);
            if (taken != null && !taken.equals(fqcn)) {
                qualified.add(fqcn);
            } else if (taken == null && !coveredByWildcard(fqcn) && needsImport(fqcn)) {
                newImports.add(fqcn);
                importedBySimpleName.put(simple, fqcn);
            }
        }
        return a.render(new AnnoCode.NameStyle() {
            public Syntax syntax() {
                return syntax;
            }

            public boolean useSimpleName(String fqcn, boolean ruleAsked) {
                return !ruleAsked && !qualified.contains(fqcn);
            }

            public boolean simplifiesDeclaredTypes() {
                return false;
            }
        });
    }

    private boolean coveredByWildcard(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot > 0 && wildcardPackages.contains(fqcn.substring(0, dot));
    }

    /** After the last import, or after the package line when there is none. */
    private void addImports() {
        int last = -1;
        int pkg = -1;
        for (int i = 0; i < lines.size(); i++) {
            String t = lines.get(i).trim();
            if (t.startsWith("import ")) last = i;
            if (pkg < 0 && t.startsWith("package ")) pkg = i;
        }
        List<String> add = new ArrayList<String>();
        for (String fqcn : newImports) add.add(syntax.importLine(fqcn));
        if (last >= 0) {
            lines.addAll(last + 1, add);
        } else {
            add.add(0, "");
            lines.addAll(pkg + 1, add);
        }
    }

    private static String simpleName(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? fqcn : fqcn.substring(dot + 1);
    }

    /**
     * Only java.lang's own types are in scope unimported; one of its
     * subpackages - java.lang.annotation.Retention - needs an import like
     * any other type.
     */
    private boolean needsImport(String fqcn) {
        if (fqcn.startsWith("java.lang.")) return fqcn.indexOf('.', "java.lang.".length()) > 0;
        return syntax.needsImport(fqcn);
    }
}
