package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Composing a bundle with a file shell for the wrong language.
 *
 * A bundle is a list of names and a template is text, so nothing downstream
 * notices when the two disagree: the Kotlin shell fills a Java skeleton and
 * the file is written. It is found by a compiler, if one runs, and this
 * session found it three times - once by generating Kotlin rules into .ts
 * files and twice by generating Lombok into .kt.
 *
 * So the shells say which language they write, and the mismatch is named
 * where it happens.
 */
public class WrongLanguageBundleTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec article() {
        return new EntitySpec("Article")
                .domain("content", "Content")
                .pkg("dto", "io.acme.content.dto")
                .pkg("entity", "io.acme.content.entity")
                .addField(loader.parse("articleId", "string(36) @id"));
    }

    /** The Kotlin DTO shell asked to fill a Java skeleton. */
    @Test
    public void aKotlinShellRefusesToWriteJava() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                RuleCatalog.standard()
                        .engineFor("jpa-dto", "jpa-dto-file-kt")
                        .templateSet(SourceCode.TemplateSet.V3)
                        .run(article(), "dto")
                        .render());

        assertTrue(ex.getMessage().contains("kt-dto-file"), ex.getMessage());
        assertTrue(ex.getMessage().contains("publisher.rules"), ex.getMessage());
    }

    /** And the TypeScript one, asked to fill the Kotlin skeleton. */
    @Test
    public void aTypeScriptShellRefusesToWriteKotlin() {
        assertThrows(IllegalStateException.class, () ->
                RuleCatalog.standard()
                        .engineFor("jpa-dto", "jpa-dto-file-ts")
                        .templateSet(SourceCode.TemplateSet.KOTLIN)
                        .run(article(), "dto")
                        .render());
    }

    /** Composed correctly, nothing complains. */
    @Test
    public void theRightCompositionIsSilent() {
        String out = RuleCatalog.standard()
                .engineFor("jpa-dto", "jpa-dto-file-kt")
                .templateSet(SourceCode.TemplateSet.KOTLIN)
                .run(article(), "dto")
                .render();

        assertTrue(out.contains("open class ArticleDTO"), out);
    }
}
