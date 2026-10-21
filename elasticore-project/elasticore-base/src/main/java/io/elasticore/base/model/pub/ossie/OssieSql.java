package io.elasticore.base.model.pub.ossie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Which Ossie dialect an expression can honestly be declared in.
 *
 * The core spec registers a dozen dialect names, but defines the semantics of
 * one: OSSIE_SQL_2026, the portable expression language of
 * core-spec/expression_language.md. Every other name - ANSI_SQL among them -
 * identifies a vendor's language; a consumer treats those expressions as
 * opaque and may ignore them (spec.md, "Dialects"). So what this exporter
 * writes in the Ossie language is labelled OSSIE_SQL_2026, and only what that
 * language cannot say is labelled ANSI_SQL.
 *
 * What it cannot say (expression_language.md, "Not Supported in
 * Expressions"): SELECT / FROM / JOIN, GROUP BY, WHERE, subqueries, CTEs, the
 * set operations, DDL and DML. Each of those is a query, and every query
 * carries one of the words below outside its string literals; an expression
 * - columns, arithmetic, comparisons, CASE, the functions, EXTRACT(year FROM
 * d) included - carries none. A column name such as select_flag is not a
 * word on its own and does not count.
 */
final class OssieSql {

    /** The dialect whose semantics the spec defines; the default where none is named. */
    static final String PORTABLE = "OSSIE_SQL_2026";

    /** The registered name for SQL the Ossie language cannot express; opaque to consumers. */
    static final String OPAQUE = "ANSI_SQL";

    private static final Pattern QUERY_WORD = Pattern.compile(
            "(?i)(?<![A-Za-z0-9_])(select|insert|update|delete|merge|create|alter|drop|union|intersect|except|with)(?![A-Za-z0-9_])");

    private static final Pattern QUOTED = Pattern.compile("'(?:[^']|'')*'|\"(?:[^\"\\\\]|\\\\.)*\"");

    private OssieSql() {
    }

    /** Whether the expression stays inside the Ossie language: no query word outside its string literals. */
    static boolean portable(String sql) {
        if (sql == null) return true;
        Matcher quoted = QUOTED.matcher(sql);
        String bare = quoted.replaceAll(" ");
        return !QUERY_WORD.matcher(bare).find();
    }

    /** The dialect to declare the expression in. */
    static String dialect(String sql) {
        return portable(sql) ? PORTABLE : OPAQUE;
    }
}
