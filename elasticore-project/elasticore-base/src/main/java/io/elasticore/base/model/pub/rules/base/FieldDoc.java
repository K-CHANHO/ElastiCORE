package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.EnumSpec;
import io.elasticore.base.model.pub.rules.FieldSpec;

import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The documentation a field carries into every DTO shape: the date pattern it
 * formats with, and the example Swagger shows for it.
 *
 * Shared by the plain DTO and the search DTO because 2.x derives both from
 * the same getExample / setFormatAnnotation pair.
 */
public final class FieldDoc {

    static final String DATE = "yyyy-MM-dd";
    static final String DATETIME = "yyyy-MM-dd HH:mm:ss";

    private FieldDoc() {
    }

    /**
     * The pattern a temporal field formats with - an explicit @format wins -
     * or null for a field that is not temporal.
     */
    public static String formatPattern(FieldSpec f) {
        String explicit = f.dslValueAny("format", "pattern");
        if (f.isDate()) return explicit == null ? DATE : explicit;
        if (f.isDateTime()) return explicit == null ? DATETIME : explicit;
        return null;
    }

    /**
     * The example Swagger shows: an explicit @example, otherwise the constants
     * of an enum type, otherwise the date pattern. Null when there is none.
     */
    public static String example(EntitySpec e, FieldSpec f) {
        String explicit = f.dslValue("example");
        if (explicit != null) return explicit;

        String enumExample = enumConstants(e, f);
        if (enumExample != null) return enumExample;

        // A declared @format / @pattern documents any field; the temporal
        // defaults apply only to temporal ones.
        String format = f.dslValueAny("format", "pattern");
        // On a string, the clean output reads @pattern as the regex the value
        // must match (DtoBaseRules writes it as @Pattern), not as an example:
        // offered as a value, it would fail its own check, and its positional
        // form 'regex', 'message' is not one value. The compatible output
        // keeps 2.x's example.
        if (format != null && f.isStringType() && !f.hasDsl("format") && !ctx(e).compatible()) {
            format = null;
        }
        if (format == null) {
            if (f.isDate()) format = DATE;
            else if (f.isDateTime()) format = DATETIME;
        }
        return format;
    }

    /**
     * An enum documents itself by listing its constants: the value that
     * crosses the JSON boundary, then the rest of the declared values.
     */
    private static String enumConstants(EntitySpec e, FieldSpec f) {
        EnumSpec spec = e.enumSpec(f.coreItemType());
        if (spec == null || spec.constants().isEmpty()) return null;

        int jsonIndex = -1;
        for (int i = 0; i < spec.fields().size(); i++) {
            if (spec.fields().get(i).name().equals(spec.jsonFieldName())) {
                jsonIndex = i;
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (EnumSpec.Constant c : spec.constants()) {
            if (sb.length() > 0) sb.append(" | ");

            List<String> values = c.values();
            if (jsonIndex < 0 || jsonIndex >= values.size()) {
                sb.append(c.name());
            } else {
                sb.append(values.get(jsonIndex));
            }
            sb.append(":");

            for (int i = 0; i < values.size(); i++) {
                if (i == jsonIndex) continue;
                sb.append(" ").append(values.get(i));
            }
        }
        return sb.toString();
    }

    /** The example rendered as the @Schema attribute, or "" when there is none. */
    public static String exampleAttribute(EntitySpec e, FieldSpec f) {
        String example = example(e, f);
        return example == null ? "" : String.format(", example=%s", RuleText.quote(escapeQuotes(example)));
    }

    private static String escapeQuotes(String s) {
        return s.replace("\"", "\\\"");
    }
}
