package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import io.elasticore.base.model.pub.rules.lang.java.DtoFileRules;
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
 * The DTO class around the field block. Field annotations are verified
 * elsewhere; what this checks is the file - its packages, its declaration,
 * and that the fields land inside the class body.
 *
 * BlogPostDTO of the jsonview_demo domain is the comparison, whose entity
 * declares nothing beyond its fields.
 */
public class DtoFileTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec blogPost() {
        String base = "io.elasticore.blueprint.domain.jsonview_demo";
        return new EntitySpec("BlogPost")
                .jsonViewConfig("auto")
                .pkg("dto", base + ".dto")
                .pkg("entity", base + ".entity")
                // What the committed file imports and annotates the class
                // with: the view interfaces its fields reference - listed
                // twice, as 2.x lists them - and the inclusion policy.
                .dtoImport("com.fasterxml.jackson.annotation.JsonView")
                .dtoImport("com.fasterxml.jackson.annotation.JsonView")
                .dtoImport("com.fasterxml.jackson.annotation.JsonInclude")
                .dtoImport("com.fasterxml.jackson.annotation.JsonInclude.Include")
                .nativeClassAnnotation("@JsonInclude(Include.NON_NULL)")
                .addField(loader.parse("id", "long @id @sequence"))
                .addField(loader.parse("title", "string(200)!"));
    }

    private String render(EntitySpec e) {
        return new RuleEngine()
                .register(new DtoBaseRules())
                .register(new DtoFileRules())
                .run(e, "dto").render();
    }

    /** The head of the file, down to the class declaration. */
    @Test
    public void fileHead_matchesGolden() throws IOException {
        List<String> golden = goldenLines("BlogPostDTO.java");
        List<String> actual = toLines(render(blogPost()));

        String declaration = "public  class BlogPostDTO  implements java.io.Serializable  {";
        int end = golden.indexOf(declaration);
        assertTrue(end > 0, "class declaration not found in golden");
        assertTrue(actual.indexOf(declaration) == end,
                "declaration at line " + actual.indexOf(declaration) + ", golden has " + end
                        + "\n" + render(blogPost()));
        assertEquals(join(golden.subList(0, end + 1)), join(actual.subList(0, end + 1)));
    }

    /** The fields land inside the class body, tab-indented as 2.x writes them. */
    @Test
    public void fields_landInsideTheClassBody() {
        String out = render(blogPost());

        assertTrue(out.contains("\n\t@Schema(description = \"id\"  )"), out);
        assertTrue(out.contains("\n\tprivate Long id;"), out);
    }

    // ---- the declaration -------------------------------------------------

    /** An @extend DTO names its parent. */
    @Test
    public void extendedDto_namesItsParent() {
        EntitySpec e = blogPost().meta("extend", "BaseDTO");

        assertTrue(render(e).contains("class BlogPostDTO extends BaseDTO"), render(e));
    }

    /** A DTO declared in its own right extends nothing, whatever @extend says. */
    @Test
    public void ownDto_extendsNothing() {
        EntitySpec e = blogPost().meta("dto", null).meta("extend", "BaseDTO");

        assertFalse(render(e).contains("extends"), render(e));
    }

    /** @implements adds to Serializable rather than replacing it. */
    @Test
    public void implementsAnnotation_addsToSerializable() {
        EntitySpec e = blogPost().meta("implements", "Auditable");

        assertTrue(render(e).contains("implements java.io.Serializable,Auditable"), render(e));
    }

    /** An @abstract DTO is declared abstract. */
    @Test
    public void abstractDto_isDeclaredAbstract() {
        EntitySpec e = blogPost().meta("abstract", null);

        assertTrue(render(e).contains("public abstract class BlogPostDTO"), render(e));
    }

    /** A class annotation the DSL carries is emitted above the declaration. */
    @Test
    public void nativeClassAnnotation_isEmittedAboveTheDeclaration() {
        EntitySpec e = blogPost().nativeClassAnnotation("@JsonInclude(Include.NON_NULL)");

        String out = render(e);
        assertTrue(out.contains("@JsonInclude(Include.NON_NULL)\n@Getter"), out);
    }

    /** A search DTO is a different artifact, with its own template. */
    @Test
    public void searchDto_isNotProducedHere() {
        EntitySpec e = blogPost().meta("searchable", "entity=BlogPost");

        SourceCode code = new RuleEngine()
                .register(new DtoBaseRules()).register(new DtoFileRules())
                .run(e, "dto");
        assertFalse(code.isMake());
    }

    // ---- golden access ----------------------------------------------------

    private List<String> goldenLines(String fileName) throws IOException {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/jsonview_demo/dto/" + fileName;
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
