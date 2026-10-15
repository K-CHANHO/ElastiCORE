package io.elasticore.base.model.pub.rules.lang.kotlin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kotlin's hard keywords, where they turn up inside a qualified name.
 *
 * A package is whatever env.yml names, and the hexagonal layout names one
 * adapter.in.web - where "in" is a keyword in Kotlin, so every package line,
 * import and qualified reference of that layer fails to parse. Kotlin quotes
 * such a segment with backticks; Java, which has no "in", needs nothing.
 *
 * Applied to rendered Kotlin text: a keyword between two dots of a dotted
 * name, or ending a package line, is quoted. String and character literals
 * are left alone, because a package named in a string (a component scan) is
 * not Kotlin syntax.
 */
public final class KotlinKeywords {

    private KotlinKeywords() {
    }

    private static final String HARD = "as|break|class|continue|do|else|false|for|fun|if|in|interface|is|"
            + "null|object|package|return|super|this|throw|true|try|typealias|typeof|val|var|when|while";

    /** A keyword segment inside a dotted name: preceded by "name." and followed by ".name" or ".*". */
    private static final Pattern INSIDE = Pattern.compile(
            "(?<=[A-Za-z0-9_`]\\.)(" + HARD + ")(?=\\.[A-Za-z_`*])");

    /** A keyword closing the name on a package or import line. */
    private static final Pattern LAST = Pattern.compile(
            "^(\\s*(?:package|import)\\s+[\\w.`]*\\.)(" + HARD + ")(\\s*)$");

    public static String escapeQualifiedNames(String source) {
        if (source == null || source.isEmpty()) return source;
        String[] lines = source.split("\n", -1);
        StringBuilder out = new StringBuilder(source.length() + 16);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) out.append('\n');
            out.append(escapeLine(lines[i]));
        }
        return out.toString();
    }

    private static String escapeLine(String line) {
        if (line.indexOf('.') < 0) return line;
        Matcher last = LAST.matcher(line);
        if (last.find()) {
            line = last.group(1) + "`" + last.group(2) + "`" + last.group(3);
        }
        // Code outside literals only. A string runs to the next unescaped
        // quote; a character literal - '"' or '\'' - is skipped whole, so a
        // quote inside one does not start a string.
        StringBuilder sb = new StringBuilder(line.length() + 8);
        StringBuilder code = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (ch == '"') {
                sb.append(INSIDE.matcher(code).replaceAll("`$1`"));
                code.setLength(0);
                int end = i + 1;
                while (end < line.length() && line.charAt(end) != '"') {
                    if (line.charAt(end) == '\\') end++;
                    end++;
                }
                end = Math.min(end + 1, line.length());
                sb.append(line, i, end);
                i = end;
            } else if (ch == '\'' && i + 2 < line.length()) {
                int end = line.charAt(i + 1) == '\\' ? i + 3 : i + 2;
                if (end < line.length() && line.charAt(end) == '\'') {
                    code.append(line, i, end + 1);
                    i = end + 1;
                } else {
                    code.append(ch);
                    i++;
                }
            } else {
                code.append(ch);
                i++;
            }
        }
        sb.append(INSIDE.matcher(code).replaceAll("`$1`"));
        return sb.toString();
    }
}
