package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.base.SupportTypeBaseRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two interfaces the generated DTOs implement, compared against the ones
 * committed for the content domain.
 */
public class SupportTypeTest {

    private SourceCode run(DomainSpec d, String target) {
        return new RuleEngine().register(new SupportTypeBaseRules()).run(d, target);
    }

    private DomainSpec contentDomain() {
        return new DomainSpec("content", "Content")
                .pkg("dto", "io.elasticore.blueprint.domain.content.dto");
    }

    @Test
    public void pageableObject_matchesGolden() throws IOException {
        assertEquals(goldenText("PageableObject.java"),
                render(run(contentDomain(), "pageableObject")));
    }

    @Test
    public void sortableObject_matchesGolden() throws IOException {
        assertEquals(goldenText("SortableObject.java"),
                render(run(contentDomain(), "sortableObject")));
    }

    /** A project with its own paging interface keeps the one it has. */
    @Test
    public void customPageableInterface_suppressesTheGeneratedOne() {
        DomainSpec d = contentDomain().customPageableInterface("kr.co.acme.Paged");

        assertFalse(run(d, "pageableObject").isMake());
        // the sort interface is decided separately
        assertTrue(run(d, "sortableObject").isMake());
    }

    /** And likewise for the sort interface. */
    @Test
    public void customSortableInterface_suppressesTheGeneratedOne() {
        DomainSpec d = contentDomain().customSortableInterface("kr.co.acme.Sorted");

        assertFalse(run(d, "sortableObject").isMake());
        assertTrue(run(d, "pageableObject").isMake());
    }

    // ---- golden access ----------------------------------------------------

    private static String render(SourceCode code) {
        String out = code.render();
        return out.endsWith("\n") ? out.substring(0, out.length() - 1) : out;
    }

    private String goldenText(String fileName) throws IOException {
        List<String> lines = readLines(golden(fileName));
        lines = lines.subList(1, lines.size());   // skip the //ecd: line

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private File golden(String fileName) {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/content/dto/" + fileName;
        String[] candidates = {"../" + rel, "elasticore-project/" + rel};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) return f;
        }
        throw new IllegalStateException("Golden file not found: " + rel
                + " from cwd " + new File(".").getAbsolutePath());
    }

    private List<String> readLines(File f) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
        } finally {
            r.close();
        }
        return lines;
    }
}
