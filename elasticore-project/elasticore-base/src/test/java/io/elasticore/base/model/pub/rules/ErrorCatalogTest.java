package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/** The codes a refusal is translated by, and the file that lists them. */
public class ErrorCatalogTest {

    @Test
    public void aConditionBecomesAReadableCode() {
        assertEquals("total-ge-0", ErrorCatalog.slug("total >= 0"));
        assertEquals("items-size-gt-0", ErrorCatalog.slug("items.size > 0"));
        assertEquals("start-le-end-and-end-ne-null", ErrorCatalog.slug("start <= end && end != null"));
    }

    /** Two conditions that differ only in an operator or in non-ASCII text must not share a code. */
    @Test
    public void conditionsThatDifferGetDifferentCodes() {
        assertEquals("a-plus-b-gt-0", ErrorCatalog.slug("a + b > 0"));
        assertEquals("a-minus-b-gt-0", ErrorCatalog.slug("a - b > 0"));
        assertNotEquals(ErrorCatalog.slug("grade != '탈퇴'"), ErrorCatalog.slug("grade != '휴면'"));
    }

    /** What the generated file says is what a ResourceBundle reads back, Korean and all. */
    @Test
    public void thePropertiesFileReadsBackAsWritten() throws Exception {
        Map<String, String> domain = new LinkedHashMap<String, String>();
        domain.put("invariant.Order.total-ge-0", "합계는 0 이상: it can't be negative");
        domain.put("state.Order.status.ship", "Order cannot ship when status is {0}; it must be {1}");
        domain.put("odd key=with:chars", "  leading spaces and a back\\slash");
        String text = ErrorCatalog.properties("shop", Collections.<String, String>emptyMap(), domain);

        for (char c : text.toCharArray()) {
            if (c > 0x7e && c != '\n') throw new AssertionError("not ASCII: " + text);
        }
        Properties read = new Properties();
        read.load(new StringReader(text));
        for (Map.Entry<String, String> en : domain.entrySet()) {
            assertEquals(en.getValue(), read.getProperty(en.getKey()), en.getKey());
        }
    }
}
