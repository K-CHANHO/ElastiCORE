package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The entity artifact rendered in Kotlin, through the same field rules the
 * Java modes use.
 *
 * The point of the seam is that only the file shell is per-language, so
 * these check that the persistence annotations arrive unchanged and that
 * the declaration, imports and properties are Kotlin's.
 */
public class KotlinEntityFileTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec bookmark() {
        return new EntitySpec("Bookmark")
                .domain("content", "Content")
                .pkg("entity", "io.elasticore.blueprint.domain.content.entity")
                .addField(loader.parse("bookmarkId", "long @id @sequence"))
                .addField(loader.parse("memo", "string(200)"));
    }

    private String render(EntitySpec e) {
        return RuleCatalog.standard()
                .engineFor("jpa-springboot-kt")
                .templateSet(SourceCode.TemplateSet.KOTLIN)
                .run(e, "entity")
                .render();
    }

    @Test
    public void packageAndImports_carryNoSemicolon() {
        String out = render(bookmark());

        assertTrue(out.contains("package io.elasticore.blueprint.domain.content.entity\n"), out);
        assertTrue(out.contains("import jakarta.persistence.Column\n"), out);
        assertFalse(out.contains("import jakarta.persistence.Column;"), out);
    }

    /** The supertype list follows the class name in Kotlin. */
    @Test
    public void classDeclaration_isKotlins() {
        assertTrue(render(bookmark()).contains("class Bookmark : java.io.Serializable {"),
                render(bookmark()));
    }

    /** A parent is constructed where an interface is only named. */
    @Test
    public void parent_isConstructedInTheSupertypeList() {
        String out = render(bookmark().parent("ContentBase"));
        assertTrue(out.contains("class Bookmark : ContentBase(), java.io.Serializable {"), out);
    }

    /**
     * Fields become properties: the type follows the name, and one the DSL
     * did not mark required is nullable and starts null.
     */
    @Test
    public void fields_becomeNullableProperties() {
        String out = render(bookmark());

        assertTrue(out.contains("var memo: String? = null"), out);
        assertFalse(out.contains("private String memo;"), out);
    }

    /** The persistence rules are shared, so their annotations arrive intact. */
    @Test
    public void persistenceAnnotations_comeFromTheSharedRules() {
        String out = render(bookmark());

        assertTrue(out.contains("@Id"), out);
        assertTrue(out.contains("@Column(name = \"memo\", length = 200)"), out);
        assertTrue(out.contains("@Entity"), out);
        assertTrue(out.contains("@Table(name = \"bookmark\")"), out);
        assertTrue(out.contains("@EntityListeners(AuditingEntityListener::class)"), out);
        assertFalse(out.contains("AuditingEntityListener.class"),
                "a class literal is written ::class in Kotlin: " + out);
    }

    /**
     * Lombok is a Java annotation processor and does nothing for Kotlin, which
     * has its own accessors - so the accessor rule is not in this bundle.
     */
    @Test
    public void lombokIsNotEmittedForKotlin() {
        String out = render(bookmark());

        assertFalse(out.contains("@Getter"), out);
        assertFalse(out.contains("@Setter"), out);
        assertFalse(out.contains("lombok"), out);
    }

    /**
     * A target with no Kotlin skeleton must not reach the Java one: a
     * template is only text, and the fallback would be emitted as-is.
     *
     * The target is a name no skeleton will ever have, so this keeps testing
     * the guard rather than whichever artifact has not been ported yet.
     */
    @Test
    public void missingKotlinSkeleton_refusesTheJavaFallback() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                RuleCatalog.standard()
                        .engineFor("jpa-springboot-kt")
                        .templateSet(SourceCode.TemplateSet.KOTLIN)
                        .run(bookmark(), "no-such-artifact")
                        .render());

        assertTrue(ex.getMessage().contains("no-such-artifact"), ex.getMessage());
    }

    /** Java is what it was: the same entity through the v3 mode is unchanged. */
    @Test
    public void javaModeStillWritesJava() {
        String out = RuleCatalog.standard()
                .engineFor("jpa-entity", "jpa-entity-annotations", "jpa-entity-file")
                .templateSet(SourceCode.TemplateSet.V3)
                .run(bookmark(), "entity")
                .render();

        assertTrue(out.contains("import jakarta.persistence.Column;"), out);
        assertTrue(out.contains("public class Bookmark implements java.io.Serializable {"), out);
        assertEquals(-1, out.indexOf("var memo"), out);
    }
}
