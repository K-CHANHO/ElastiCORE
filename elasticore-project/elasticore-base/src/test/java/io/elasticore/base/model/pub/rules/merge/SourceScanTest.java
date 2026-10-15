package io.elasticore.base.model.pub.rules.merge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Where a brace counts and where it does not.
 *
 * A merge locates its member boundaries by brace depth, and a boundary in the
 * wrong place damages code a developer wrote. Every construct below puts a
 * brace somewhere it must be ignored, so these state that the scan ignores it.
 */
public class SourceScanTest {

    /** The depth at the end of a balanced source is back to zero. */
    private int finalDepth(String src) {
        SourceScan s = new SourceScan(src);
        return s.depthAt(s.length() - 1);
    }

    @Test
    public void bracesInCode_areCounted() {
        SourceScan s = new SourceScan("class A { void x() { } }");

        assertEquals(0, finalDepth("class A { void x() { } }"));
        assertEquals(1, s.depthAt(s.text().indexOf("void")));
        assertEquals(2, s.depthAt(s.text().indexOf("void x() {") + 9));
    }

    @Test
    public void braceInALineComment_isIgnored() {
        assertEquals(0, finalDepth("class A { } // an unmatched { here"));
    }

    @Test
    public void braceInABlockComment_isIgnored() {
        assertEquals(0, finalDepth("class A { /* { { { */ }"));
    }

    /** Javadoc is a block comment and routinely contains braces - {@code ...}. */
    @Test
    public void braceInJavadoc_isIgnored() {
        assertEquals(0, finalDepth("/** {@code new int[]{1}} */\nclass A { }"));
    }

    @Test
    public void braceInAStringLiteral_isIgnored() {
        assertEquals(0, finalDepth("class A { String s = \"{{{\"; }"));
    }

    @Test
    public void braceInACharLiteral_isIgnored() {
        assertEquals(0, finalDepth("class A { char c = '{'; }"));
    }

    /** An escaped quote does not end the string, so its braces stay ignored. */
    @Test
    public void escapedQuoteDoesNotEndAString() {
        assertEquals(0, finalDepth("class A { String s = \"a\\\"{\"; }"));
    }

    /** An escaped backslash does end the string - the next quote is real. */
    @Test
    public void escapedBackslashEndsAString() {
        assertEquals(0, finalDepth("class A { String s = \"a\\\\\"; void x() { } }"));
    }

    @Test
    public void braceInATextBlock_isIgnored() {
        String src = "class A {\n String s = \"\"\"\n   { \"json\": { } }\n   \"\"\";\n}";
        assertEquals(0, finalDepth(src));
    }

    /** A quote inside a line comment must not open a string literal. */
    @Test
    public void quoteInACommentDoesNotOpenAString() {
        assertEquals(0, finalDepth("class A { // it's fine\n void x() { } }"));
    }

    /** A comment opener inside a string is not a comment. */
    @Test
    public void commentOpenerInAStringIsNotAComment() {
        assertEquals(0, finalDepth("class A { String s = \"/*\"; void x() { } }"));
    }

    /** A lambda body is a brace pair like any other, nested one deeper. */
    @Test
    public void lambdaBraces_areCounted() {
        String src = "class A { void x() { run(() -> { y(); }); } }";
        SourceScan s = new SourceScan(src);

        assertEquals(0, finalDepth(src));
        assertEquals(3, s.depthAt(src.indexOf("y();")));
    }

    @Test
    public void isCode_marksLiteralsAndCommentsAsNotCode() {
        String src = "class A { String s = \"x\"; // c\n }";
        SourceScan s = new SourceScan(src);

        assertTrue(s.isCode(src.indexOf("class")));
        assertFalse(s.isCode(src.indexOf("// c") + 2));
        assertFalse(s.isCode(src.indexOf("\"x\"") + 1));
    }

    /** Every index carries a depth: the fill must leave no gap behind a token. */
    @Test
    public void depthIsFilledForEveryIndex() {
        String src = "class A { /* skipped */ void x() { } }";
        SourceScan s = new SourceScan(src);

        for (int i = src.indexOf("/*"); i < src.indexOf("void"); i++) {
            assertEquals(1, s.depthAt(i), "gap at " + i + " of: " + src);
        }
    }

    @Test
    public void crlfIsNormalisedSoIndexesMatchTheText() {
        SourceScan s = new SourceScan("class A {\r\n void x() { }\r\n}");

        assertEquals(-1, s.text().indexOf('\r'));
        assertEquals(0, s.depthAt(s.length() - 1));
    }

    @Test
    public void nextCode_skipsWhitespaceCommentsAndFindsTheNextRealCharacter() {
        String src = "} \n  /* gap */ \n ;";
        SourceScan s = new SourceScan(src);

        assertEquals(src.indexOf(';'), s.nextCode(src.indexOf('}') + 1));
    }
}
