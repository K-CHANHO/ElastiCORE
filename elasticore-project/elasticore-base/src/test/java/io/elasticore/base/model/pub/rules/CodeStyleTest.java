package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The house style a project writes its generated code in.
 *
 * What matters is that a rule never has to know about it: the same rendered
 * text comes out indented whichever way the build asked for.
 */
public class CodeStyleTest {

    private static final String RENDERED = Lines.of(
            "public class Order {",
            "    private Long orderId;",
            "",
            "    public Long getOrderId() {",
            "        return orderId;",
            "    }",
            "}") + "\n";

    /** Asking for nothing leaves the file exactly as rendered. */
    @Test
    public void unconfigured_leavesTheFileAlone() {
        assertTrue(CodeStyle.AS_WRITTEN.isAsWritten());
        assertEquals(RENDERED, CodeStyle.AS_WRITTEN.apply(RENDERED));
    }

    // ---- indentation -------------------------------------------------------

    @Test
    public void twoSpaces_halvesEveryLevel() {
        String out = CodeStyle.of("2", null, null).apply(RENDERED);

        assertTrue(out.contains("\n  private Long orderId;"), out);
        assertTrue(out.contains("\n    return orderId;"), out);
        assertTrue(out.contains("\n  }"), out);
    }

    @Test
    public void tab_writesOneTabPerLevel() {
        String out = CodeStyle.of("tab", null, null).apply(RENDERED);

        assertTrue(out.contains("\n\tprivate Long orderId;"), out);
        assertTrue(out.contains("\n\t\treturn orderId;"), out);
    }

    /** A file already written with tabs is read as one level per tab. */
    @Test
    public void tabbedSource_isMeasuredByItsTabs() {
        String tabbed = Lines.of("class Q {", "\tpublic static class $Order {", "\t\tint x;", "}") + "\n";

        String out = CodeStyle.of("2", null, null).apply(tabbed);
        assertTrue(out.contains("\n  public static class $Order {"), out);
        assertTrue(out.contains("\n    int x;"), out);
    }

    /**
     * What does not fill a level belongs to the line. That is how the space
     * opening a javadoc continuation survives a change of indent.
     */
    @Test
    public void javadocContinuation_keepsItsMarker() {
        String doc = Lines.of("    /**", "     * Finds one row.", "     */", "    public void find() {}") + "\n";

        String out = CodeStyle.of("tab", null, null).apply(doc);
        assertTrue(out.startsWith("\t/**"), out);
        assertTrue(out.contains("\n\t * Finds one row."), out);
        assertTrue(out.contains("\n\t */"), out);
    }

    /** A blank line stays blank rather than gaining an indent. */
    @Test
    public void blankLine_gainsNoIndent() {
        String out = CodeStyle.of("tab", null, null).apply(RENDERED);

        assertTrue(out.contains("orderId;\n\n\tpublic Long"), out);
    }

    // ---- line endings ------------------------------------------------------

    @Test
    public void crlf_endsEveryLine() {
        String out = CodeStyle.of(null, "crlf", null).apply(RENDERED);

        assertTrue(out.contains("public class Order {\r\n"), out);
        assertTrue(out.endsWith("}\r\n"), out);
        // Every LF is part of a CRLF, so none was left standing on its own.
        for (int i = 0; i < out.length(); i++) {
            if (out.charAt(i) != '\n') continue;
            assertTrue(i > 0 && out.charAt(i - 1) == '\r', "a bare LF at " + i);
        }
    }

    @Test
    public void lf_endsEveryLine() {
        String crlfSource = RENDERED.replace("\n", "\r\n");

        String out = CodeStyle.of(null, "lf", null).apply(crlfSource);
        assertTrue(out.indexOf('\r') < 0, out);
    }

    // ---- the final newline --------------------------------------------------

    @Test
    public void finalNewline_canBeRequired() {
        String withoutOne = "public class Order {}";

        assertTrue(CodeStyle.of(null, "lf", Boolean.TRUE).apply(withoutOne).endsWith("}\n"));
    }

    @Test
    public void finalNewline_canBeDropped() {
        String out = CodeStyle.of(null, "lf", Boolean.FALSE).apply(RENDERED);

        assertTrue(out.endsWith("}"), out);
    }

    /** Left unset, the file keeps whichever it was rendered with. */
    @Test
    public void finalNewline_isOtherwiseLeftAlone() {
        assertTrue(CodeStyle.of("tab", null, null).apply(RENDERED).endsWith("}\n"));
        assertTrue(CodeStyle.of("tab", null, null).apply("class A {}").endsWith("}"));
    }

    // ---- settings ------------------------------------------------------------

    @Test
    public void settings_areReadFromTheBuild() {
        System.setProperty("elasticore.style.indent", "tab");
        System.setProperty("elasticore.style.lineEnding", "lf");
        try {
            CodeStyle style = CodeStyle.fromSystemProperties();
            assertTrue(!style.isAsWritten());
            assertEquals("\n", style.lineEnding());
            assertTrue(style.apply(RENDERED).contains("\n\tprivate Long orderId;"));
        } finally {
            System.clearProperty("elasticore.style.indent");
            System.clearProperty("elasticore.style.lineEnding");
        }
    }

    @Test
    public void noSettings_meanNoStyle() {
        assertTrue(CodeStyle.fromSystemProperties().isAsWritten());
    }
}
