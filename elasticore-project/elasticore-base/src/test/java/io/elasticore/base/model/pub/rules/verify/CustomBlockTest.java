package io.elasticore.base.model.pub.rules.verify;

import io.elasticore.base.util.HashUtils;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A custom block survives a v3 rewrite. Before, the blocks were left out of
 * the comparison (so the file read as unchanged) and out of the rewrite (so a
 * DSL change deleted them).
 */
public class CustomBlockTest {

    private static final String OLD =
            "package a;\n"
            + "\n"
            + "// !---------- imports\n"
            + "import java.util.Map;\n"
            + "// ------------!\n"
            + "public class Order {\n"
            + "    private String name;\n"
            + "// !---------- behaviour\n"
            + "    public void confirm() { }\n"
            + "// ------------!\n"
            + "}\n";

    private static HashUtils.Response statusOf(String body) {
        String generatedOnly = body.replaceAll("(?s)// !----------.*?------------!\\n", "");
        return HashUtils.checkContentModified(new StringReader(HashUtils.makeEcdCode(generatedOnly) + body));
    }

    @Test
    public void theBlocksAreSeenAndLeftOutOfTheHash() {
        HashUtils.Response st = statusOf(OLD);
        assertEquals(HashUtils.NO_MODIFIED, st.getStatus(), "a block is not a user edit of the generated text");
        assertTrue(st.hasCustomizedScopeContent());
    }

    @Test
    public void aRewriteKeepsThem() {
        HashUtils.Response st = statusOf(OLD);
        String regenerated = "package a;\n\npublic class Order {\n    private String name;\n    private String memo;\n}\n";

        String written = RulesGenRunner.withCustomBlocks(regenerated, st);

        assertTrue(written.contains("private String memo;"), written);
        assertTrue(written.contains("public void confirm() { }"), written);
        assertTrue(written.contains("import java.util.Map;"), written);
        assertTrue(written.indexOf("import java.util.Map;") < written.indexOf("public class Order"), written);
        assertTrue(written.indexOf("public void confirm()") < written.lastIndexOf("}"), written);

        // And the rewritten file reads back as unchanged generated text plus its blocks.
        HashUtils.Response again = HashUtils.checkContentModified(
                new StringReader(HashUtils.makeEcdCode(regenerated) + written));
        assertEquals(HashUtils.NO_MODIFIED, again.getStatus());
        assertEquals(regenerated, again.getPreSourceContent());
    }
}
