package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.JpaEntityBaseRules;
import io.elasticore.base.model.pub.rules.lang.java.JpaEntityClassRules;
import io.elasticore.base.model.pub.rules.lang.java.JpaEntityCompatFileRules;
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
 * The entity class around the field block. The field annotations are verified
 * byte for byte by the rules gate, which starts at the first @Id and so never
 * saw the file around them - nor the key constructor above them.
 *
 * Comment of the content domain is the comparison.
 */
public class EntityCompatFileTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec comment() {
        String base = "io.elasticore.blueprint.domain.content";
        return new EntitySpec("Comment")
                .knownEntity("Article")
                .parent("ContentBase")
                .pkg("entity", base + ".entity")
                // What the committed file imports.
                .dtoImport(base + ".entity.Comment")
                .dtoImport(base + ".entity.Article")
                .dtoImport("jakarta.persistence.Entity")
                .meta("table", "cms_comment")
                .addField(loader.parse("commentId", "long @id @sequence"));
    }

    private String render(EntitySpec e) {
        return new RuleEngine()
                .register(new JpaEntityBaseRules())
                .register(new JpaEntityClassRules())
                .register(new JpaEntityCompatFileRules())
                .run(e, "entity").render();
    }

    /** The head of the file, down to the class declaration. */
    @Test
    public void fileHead_matchesGolden() throws IOException {
        List<String> golden = goldenLines();
        List<String> actual = toLines(render(comment()));

        String declaration =
                "public  class Comment extends ContentBase implements java.io.Serializable  {";
        int end = golden.indexOf(declaration);
        assertTrue(end > 0, "class declaration not found in golden");
        assertEquals(join(golden.subList(0, end + 1)), join(actual.subList(0, end + 1)));
    }

    /**
     * The key constructor opens the class body, exactly as committed:
     *   public Comment(Long commentId) {
     *       this.commentId = commentId;
     *   }
     */
    @Test
    public void keyConstructor_opensTheClassBody() {
        String out = render(comment());

        assertTrue(out.contains("\tpublic Comment(Long commentId) {\n"
                + "\t    this.commentId = commentId;\n"
                + "\t}"), out);
    }

    /** An inherited key is handed to the parent instead of assigned. */
    @Test
    public void inheritedKey_isHandedToTheParent() {
        EntitySpec e = new EntitySpec("Reply")
                .parent("Comment")
                .pkg("entity", "p.entity")
                .addParentField(loader.parse("commentId", "long @id"))
                .addField(loader.parse("replySeq", "int @id"));

        String out = render(e);
        assertTrue(out.contains("public Reply(Long commentId, Integer replySeq) {"), out);
        assertTrue(out.contains("    super(commentId);"), out);
        assertTrue(out.contains("    this.replySeq = replySeq;"), out);
    }

    /** An entity with no key of its own gets no constructor. */
    @Test
    public void keylessEntity_getsNoConstructor() {
        EntitySpec e = new EntitySpec("ContentBase")
                .meta("abstract", null)
                .pkg("entity", "p.entity")
                .addField(loader.parse("createdBy", "string(50)"));

        String out = render(e);
        assertFalse(out.contains("public ContentBase("), out);
        assertTrue(out.contains("public abstract class ContentBase"), out);
    }

    // ---- golden access ----------------------------------------------------

    private List<String> goldenLines() throws IOException {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/content/entity/Comment.java";
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
