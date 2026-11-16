package io.elasticore.sample.guide.signin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The code the annotation guide shows around the generated sign-in is the code
 * of this package, which PasswordResetGuideTest runs: every block's lines stand,
 * in their order, in one of its files, and the Korean guide shows the same blocks.
 */
class GuideCodeTest {

    private static final String EN = "/elasticore-guide/elasticore-annotation-guide.md";
    private static final String KO = "/elasticore-guide/ko/elasticore-annotation-guide.md";
    private static final Path SOURCES = Paths.get("src/test/java/io/elasticore/sample/guide/signin");

    @Test
    void everyBlockOfTheSectionIsCodeThatRuns() throws IOException {
        List<List<String>> blocks = blocks(section(EN, "### Around the generated sign-in"));
        assertEquals(4, blocks.size(), "the reset service, the token, the secret, the controller");

        List<List<String>> sources = new ArrayList<List<String>>();
        try (Stream<Path> files = Files.list(SOURCES)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList())) {
                sources.add(Files.readAllLines(file, StandardCharsets.UTF_8));
            }
        }
        for (List<String> block : blocks) {
            boolean found = false;
            for (List<String> source : sources) found |= inOrder(block, source);
            assertTrue(found, "not in the code PasswordResetGuideTest runs:\n" + String.join("\n", block));
        }
    }

    @Test
    void theKoreanGuideShowsTheSameCode() throws IOException {
        assertEquals(blocks(section(EN, "### Around the generated sign-in")),
                blocks(section(KO, "### 생성된 로그인 곁에서")));
    }

    /** Whether the block's lines, blank ones aside and trimmed, stand in this order in the source. */
    static boolean inOrder(List<String> block, List<String> source) {
        int at = 0;
        for (String line : block) {
            String wanted = line.trim();
            if (wanted.isEmpty()) continue;
            while (at < source.size() && !source.get(at).trim().equals(wanted)) at++;
            if (at == source.size()) return false;
            at++;
        }
        return true;
    }

    /** The lines from the heading to the next heading of its level or above. */
    private static List<String> section(String resource, String heading) throws IOException {
        List<String> lines = lines(resource);
        int start = -1;
        for (int i = 0; i < lines.size(); i++) if (lines.get(i).startsWith(heading)) start = i;
        assertTrue(start >= 0, heading + " is not in " + resource);
        int end = start + 1;
        while (end < lines.size() && !lines.get(end).startsWith("## ") && !lines.get(end).startsWith("### ")) end++;
        return lines.subList(start, end);
    }

    private static List<List<String>> blocks(List<String> section) {
        List<List<String>> blocks = new ArrayList<List<String>>();
        List<String> open = null;
        for (String line : section) {
            if (open == null && line.equals("```java")) {
                open = new ArrayList<String>();
            } else if (open != null && line.equals("```")) {
                blocks.add(open);
                open = null;
            } else if (open != null) {
                open.add(line);
            }
        }
        return blocks;
    }

    private static List<String> lines(String resource) throws IOException {
        try (InputStream in = GuideCodeTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource + " is not on the classpath");
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<String> lines = new ArrayList<String>();
            for (String line : text.split("\r?\n", -1)) lines.add(line);
            return lines;
        }
    }
}
