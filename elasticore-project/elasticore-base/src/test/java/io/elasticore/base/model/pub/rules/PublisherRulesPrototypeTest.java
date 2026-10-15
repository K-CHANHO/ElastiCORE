package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.JpaEntityBaseRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prototype verification for the 3.0 rule-based publisher extension.
 *
 * Case 1 proves the rule engine reproduces the field block of an actually
 * generated source file (Bookmark.java in elasticore-template) byte-identically,
 * starting from the same DSL field lines parsed by the real model loader.
 *
 * Case 2/3 demonstrate the extension surface: custom corporate rules layered
 * on top of the base rules, and suppression of an individual base rule.
 */
public class PublisherRulesPrototypeTest {

    /** Exposes the real loader parsing so the test model equals production models. */
    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /** Builds the Bookmark entity exactly as declared in blueprint/content/model/entity.yml. */
    private EntitySpec bookmark() {
        return new EntitySpec("Bookmark")
                .knownEntity("Article")
                .addField(loader.parse("bookmarkId", "long @id @sequence"))
                .addField(loader.parse("memo", "string(200)"))
                .addField(loader.parse("article", "Article @ManyToOne"));
    }

    // ------------------------------------------------------------------
    // Case 1: byte-identical reproduction of the existing generated code
    // ------------------------------------------------------------------

    @Test
    public void bookmarkFieldBlock_matchesExistingGeneratedSource() throws IOException {
        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .run(bookmark());

        String actual = code.renderFieldBlock();
        String expected = extractFieldBlockFromGeneratedFile();

        assertEquals(expected, actual,
                "Rule engine output must equal the field block of the generated Bookmark.java");
    }

    /**
     * Extracts the field block from the committed generated source
     * (from the first "@Id" line up to, not including, the closing brace),
     * with the one-tab template indentation removed.
     */
    private String extractFieldBlockFromGeneratedFile() throws IOException {
        File src = findGeneratedBookmark();
        List<String> lines = readAllLines(src);

        int start = -1;
        int end = -1;
        for (int i = 0; i < lines.size(); i++) {
            String t = lines.get(i).trim();
            if (start < 0 && t.equals("@Id")) start = i;
            if (t.equals("}")) end = i;   // last closing brace wins
        }
        assertTrue(start > 0, "@Id line not found in " + src);
        assertTrue(end > start, "closing brace not found in " + src);

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            String line = lines.get(i);
            if (line.startsWith("\t")) line = line.substring(1);
            // The generated file pads blank lines with a tab; normalize to empty.
            if (line.trim().isEmpty()) line = "";
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private File findGeneratedBookmark() {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/domain/content/entity/Bookmark.java";
        String[] candidates = {
                "../" + rel,          // cwd = elasticore-project/elasticore-base
                "elasticore-project/" + rel,  // cwd = repository root
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) return f;
        }
        throw new IllegalStateException("Generated Bookmark.java not found from cwd "
                + new File(".").getAbsolutePath());
    }

    private List<String> readAllLines(File f) throws IOException {
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

    // ------------------------------------------------------------------
    // Case 2: corporate rules layered on top of the base rules
    // ------------------------------------------------------------------

    /** Example corporate rule set: column prefix, encryption converter, field removal. */
    public static class AcmeRules implements CodeRules {

        @Rule(value = "acme-column-prefix", order = 100)
        void columnPrefix(EntitySpec e, FieldSpec f) {
            if (f.isEntityRef()) return;
            // merge overrides only the name attribute; length from the base rule survives
            ctx(e).field(f).merge(AnnoCode.of("jakarta.persistence.Column")
                    .val("name", "acme_" + f.dbColumnName()));
        }

        @Rule(value = "acme-encrypted", order = 110)
        void encrypted(EntitySpec e, FieldSpec f) {
            if (!f.hasDsl("encrypted")) return;
            ctx(e).field(f).ensure(AnnoCode.of("jakarta.persistence.Convert")
                    .valRaw("converter", "AcmeCryptoConverter.class"));
        }

        @Rule(value = "acme-drop-internal", order = 120)
        void dropInternal(EntitySpec e, FieldSpec f) {
            if (f.hasDsl("internal")) {
                ctx(e).removeField(f);
            }
        }
    }

    @Test
    public void acmeRules_overrideAndExtendBaseOutput() {
        EntitySpec e = new EntitySpec("Card")
                .addField(loader.parse("cardId", "long @id @sequence"))
                .addField(loader.parse("cardNo", "string(50) @encrypted"))
                .addField(loader.parse("internalMemo", "string(100) @internal"));

        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .register(new AcmeRules())
                .run(e);

        String out = code.renderFieldBlock();

        // name overridden by merge, length from the base rule preserved
        assertTrue(out.contains("@Column(name = \"acme_card_no\", length = 50)"),
                "merge must override name and keep length:\n" + out);
        // ensure adds the corporate converter annotation
        assertTrue(out.contains("@Convert(converter = AcmeCryptoConverter.class)"),
                "encrypted field must get the converter:\n" + out);
        // removed field does not appear at all
        assertFalse(out.contains("internalMemo"),
                "@internal field must be removed:\n" + out);
    }

    // ------------------------------------------------------------------
    // Case 3: suppressing an individual base rule
    // ------------------------------------------------------------------

    @Test
    public void suppressedBaseRule_isNotApplied() {
        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .suppress("jpa-column")
                .run(bookmark());

        String out = code.renderFieldBlock();

        assertFalse(out.contains("@Column"), "suppressed jpa-column must not emit:\n" + out);
        assertTrue(out.contains("@Id"), "other base rules keep working:\n" + out);
        assertTrue(out.contains("@ManyToOne"), "other base rules keep working:\n" + out);
    }
}
