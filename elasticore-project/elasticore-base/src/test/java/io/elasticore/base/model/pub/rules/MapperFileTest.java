package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.lang.java.MapperFileRules;
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
 * The mapper class that holds a domain's mapping sections. The sections
 * themselves are verified against ShopMapper and ContentMapper elsewhere;
 * what this checks is the file around them - its imports, its declaration,
 * and that a section lands at the slot with the indentation of a class
 * member.
 */
public class MapperFileTest {

    private static final String SECTION =
            "    public static void mapping(Comment from, CommentDTO to, boolean isSkipNull){\n"
            + "        mapping(from,to,isSkipNull,(MappingContext) null);\n"
            + "    }\n";

    private DomainSpec contentDomain() {
        String base = "io.elasticore.blueprint.domain.content";
        return new DomainSpec("content", "Content")
                .pkg("dto", base + ".dto")
                .pkg("entity", base + ".entity")
                .pkg("enumeration", base + ".enums")
                .mapperImport(base + ".dto.*")
                .mapperImport(base + ".entity.*")
                .mapperImport(base + ".enums.*");
    }

    private String render(DomainSpec d) {
        return new RuleEngine().register(new MapperFileRules()).run(d, "mapper").render();
    }

    /** The head of the file, down to the class declaration. */
    @Test
    public void fileHead_matchesGolden() throws IOException {
        DomainSpec d = contentDomain().mapperSection(SECTION);

        List<String> golden = goldenLines();
        List<String> actual = toLines(render(d));

        // from "package" through "public class ContentMapper {"
        int end = golden.indexOf("public class ContentMapper {");
        assertTrue(end > 0, "class declaration not found in golden");
        assertEquals(join(golden.subList(0, end + 1)), join(actual.subList(0, end + 1)));
    }

    /** The helpers below the sections, which are template text throughout. */
    @Test
    public void fileTail_matchesGolden() throws IOException {
        DomainSpec d = contentDomain().mapperSection(SECTION);

        List<String> golden = goldenLines();
        List<String> actual = toLines(render(d));

        String anchor = "    protected static Specification setSpec(Specification sp,"
                + " String fieldName, String value) {";
        int gs = golden.indexOf(anchor);
        int as = actual.indexOf(anchor);
        assertTrue(gs > 0, "tail anchor not found in golden");
        assertTrue(as > 0, "tail anchor not found in rendered output");

        assertEquals(join(golden.subList(gs, golden.size())),
                join(actual.subList(as, actual.size())));
    }

    /** A section lands with the indentation of a class member, not twice it. */
    @Test
    public void section_landsIndentedOnce() {
        String out = render(contentDomain().mapperSection(SECTION));

        assertTrue(out.contains(
                "\n    public static void mapping(Comment from, CommentDTO to, boolean isSkipNull){"), out);
        assertFalse(out.contains(
                "\n        public static void mapping(Comment from, CommentDTO to,"), out);
    }

    /** A domain with no enums leaves that import line blank, as 2.x does. */
    @Test
    public void domainWithoutEnums_omitsTheEnumImport() {
        DomainSpec d = new DomainSpec("content", "Content")
                .pkg("dto", "p.dto").pkg("entity", "p.entity")
                .mapperSection(SECTION);

        String out = render(d);
        assertTrue(out.contains("import p.dto.*;"), out);
        assertFalse(out.contains("import p.enums"), out);
    }

    /** A domain with nothing to map produces no mapper. */
    @Test
    public void domainWithoutSections_producesNoFile() {
        SourceCode code = new RuleEngine().register(new MapperFileRules())
                .run(contentDomain(), "mapper");

        assertFalse(code.isMake());
    }

    // ---- golden access ----------------------------------------------------

    private List<String> goldenLines() throws IOException {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/content/dto/ContentMapper.java";
        String[] candidates = {"../" + rel, "elasticore-project/" + rel};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) {
                List<String> lines = readLines(f);
                return lines.subList(1, lines.size());   // skip the //ecd: line
            }
        }
        throw new IllegalStateException("Golden file not found: " + rel);
    }

    private static List<String> toLines(String text) {
        List<String> lines = new ArrayList<String>();
        for (String l : text.split("\n", -1)) lines.add(l);
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    private static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
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
