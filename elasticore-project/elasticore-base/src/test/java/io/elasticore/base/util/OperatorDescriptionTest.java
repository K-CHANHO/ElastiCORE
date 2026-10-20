package io.elasticore.base.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The @Schema text a search condition is documented with. The isnull keyword
 * is known to the clean (3.0) output only; the 2.x publisher and the
 * 2.x-compatible output still read it as an equality, and their text for it
 * must not change.
 */
class OperatorDescriptionTest {

    private static final String DEFAULT_TEXT = "Use 'like' if value has %, else 'equal'";

    @Test
    void isnull_hasItsOwnTextInTheCleanOutput() {
        assertEquals("Field is empty (true) or set (false).",
                StringUtils.getOperatorDescription("isnull", true));
    }

    @Test
    void isnull_keepsTheDefaultTextFor2x() {
        assertEquals(DEFAULT_TEXT, StringUtils.getOperatorDescription("isnull"));
        assertEquals(DEFAULT_TEXT, StringUtils.getOperatorDescription("isnull", false));
    }

    @Test
    void otherKeywords_areTheSameInBothOutputs() {
        String[] keywords = {"<", ">", "<=", ">=", "=", "eq", "__=", "=__", "like", "%%", "%_%",
                "between", "%_", "_%", "%__", "__%", "%__%", "in", "", "auto", "isNull"};
        for (String k : keywords) {
            assertEquals(StringUtils.getOperatorDescription(k),
                    StringUtils.getOperatorDescription(k, true), k);
        }
        assertEquals("Field equals value.", StringUtils.getOperatorDescription("eq", true));
        assertEquals("Field is between two values (inclusive).",
                StringUtils.getOperatorDescription("between", true));
        assertEquals(DEFAULT_TEXT, StringUtils.getOperatorDescription("", true));
    }
}
