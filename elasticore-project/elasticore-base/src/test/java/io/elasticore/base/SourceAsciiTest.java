package io.elasticore.base;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * elasticore-base main sources are ASCII only (comments in English, Korean in
 * the guides and YAML). A Korean comment made its way into DtoBaseRules and
 * nothing noticed: the rule was written down but not checked.
 */
class SourceAsciiTest {

    @Test
    void mainSourcesAreAscii() throws IOException {
        Path root = Paths.get("src", "main", "java");
        assertTrue(Files.isDirectory(root), "runs from the elasticore-base directory: " + root.toAbsolutePath());

        List<Path> sources;
        try (Stream<Path> walk = Files.walk(root)) {
            sources = walk.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
        assertTrue(sources.size() > 100, "found only " + sources.size() + " sources under " + root);

        List<String> offending = new ArrayList<>();
        for (Path source : sources) {
            byte[] bytes = Files.readAllBytes(source);
            String[] lines = new String(bytes, StandardCharsets.UTF_8).split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                for (char c : lines[i].toCharArray()) {
                    if (c > 0x7f) {
                        offending.add(root.relativize(source) + ":" + (i + 1));
                        break;
                    }
                }
            }
        }
        assertTrue(offending.isEmpty(), "non-ASCII characters (write English, and '->' for arrows): " + offending);
    }
}
