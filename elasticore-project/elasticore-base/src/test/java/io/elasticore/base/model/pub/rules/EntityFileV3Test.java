package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-file entity rendering in the 3.0 clean mode: a v3-optimized template
 * with NO fixed imports and NO fixed class annotations. Everything is
 * rule-emitted, and the import block lists exactly the types the surviving
 * output uses - suppressing a rule removes its annotations AND its imports.
 *
 * The 2.x-compatible mode is untouched; elcoreRulesCheck stays green.
 */
public class EntityFileV3Test {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String render(EntitySpec e, RuleEngine engine) {
        return engine.run(e).render();
    }

    private RuleEngine engine() {
        return RuleCatalog.standard()
                .engineFor("jpa-entity", "jpa-entity-annotations", "jpa-entity-file")
                .templateSet(SourceCode.TemplateSet.V3);
    }

    private EntitySpec bookmark() {
        return new EntitySpec("Bookmark")
                .knownEntity("Article")
                .pkg("entity", "io.elasticore.blueprint.domain.content.entity")
                .pkg("enumeration", "io.elasticore.blueprint.domain.content.enums")
                .tablePrefix("cms_")
                .addField(loader.parse("bookmarkId", "long @id @sequence"))
                .addField(loader.parse("memo", "string(200)"))
                .addField(loader.parse("article", "Article @ManyToOne"));
    }

    // ------------------------------------------------------------------

    @Test
    public void cleanMode_rendersFullFileWithUsedImportsOnly() {
        String out = render(bookmark(), engine());

        assertTrue(out.startsWith("package io.elasticore.blueprint.domain.content.entity;"), out);

        // used-only, explicit imports - no wildcard anywhere
        assertTrue(out.contains("import jakarta.persistence.Id;"), out);
        assertTrue(out.contains("import jakarta.persistence.Column;"), out);
        assertTrue(out.contains("import jakarta.persistence.ManyToOne;"), out);
        assertTrue(out.contains("import lombok.Getter;"), out);
        assertFalse(out.contains(".*;"), "no wildcard imports in clean mode:\n" + out);

        // class annotations and declaration
        assertTrue(out.contains("@Table(name = \"cms_bookmark\")"), out);
        assertTrue(out.contains("@DynamicUpdate"), out);
        assertTrue(out.contains("@EntityListeners(AuditingEntityListener.class)"), out);
        assertTrue(out.contains("import org.springframework.data.jpa.domain.support.AuditingEntityListener;"), out);
        assertTrue(out.contains("public class Bookmark implements java.io.Serializable {"), out);

        // field block indented by the template slot (4 spaces)
        assertTrue(out.contains("	@Id"), out);
        assertTrue(out.contains("	@Column(name = \"memo\", length = 200)"), out);
        assertTrue(out.contains("	private Article article;"), out);
        assertTrue(out.trim().endsWith("}"), out);
    }

    @Test
    public void suppressLombok_removesAnnotationsAndImports() {
        RuleEngine engine = engine().suppress("v3-lombok");

        String out = render(bookmark(), engine);

        assertFalse(out.contains("@Getter"), out);
        assertFalse(out.contains("import lombok."), "lombok imports must disappear too:\n" + out);
        // rest intact
        assertTrue(out.contains("@Entity"), out);
        assertTrue(out.contains("import jakarta.persistence.Column;"), out);
    }

    @Test
    public void abstractEntity_getsMappedSuperclassAndNoTable() {
        EntitySpec e = new EntitySpec("ContentBase")
                .meta("abstract", null)
                .pkg("entity", "io.elasticore.blueprint.domain.content.entity")
                .addField(loader.parse("createdBy", "string(50) @updatable(false)"))
                .addField(loader.parse("createDate", "datetime @updatable(false)"));

        String out = render(e, engine());

        assertTrue(out.contains("@MappedSuperclass"), out);
        assertTrue(out.contains("import jakarta.persistence.MappedSuperclass;"), out);
        assertFalse(out.contains("@Table"), out);
        assertFalse(out.contains("@Entity\n"), out);
        assertTrue(out.contains("public abstract class ContentBase implements java.io.Serializable {"), out);
        assertTrue(out.contains("	@Column(name = \"created_by\", updatable = false, length = 50)"), out);
    }

    @Test
    public void compositeKey_getsIdClassAnnotation() {
        EntitySpec e = new EntitySpec("ProjectAssignment")
                .pkg("entity", "io.elasticore.blueprint.domain.hr.entity")
                .tablePrefix("hr_")
                .addField(loader.parse("empId", "string(36) @id"))
                .addField(loader.parse("projectCode", "string(20) @id"));

        String out = render(e, engine());

        assertTrue(out.contains("@IdClass(ProjectAssignmentIdentity.class)"), out);
        assertTrue(out.contains("import jakarta.persistence.IdClass;"), out);
        assertTrue(out.contains("@Table(name = \"hr_project_assignment\")"), out);
    }

    @Test
    public void enumField_importsEnumTypeAndConverter() {
        EntitySpec e = new EntitySpec("Card")
                .pkg("entity", "kr.co.acme.entity")
                .pkg("enumeration", "kr.co.acme.enums")
                .knownEnum("CardType", "1", true)
                .addField(loader.parse("cardId", "long @id @sequence"))
                .addField(loader.parse("cardType", "CardType"));

        String out = render(e, engine());

        assertTrue(out.contains("import kr.co.acme.enums.CardType;"), out);
        assertTrue(out.contains("import jakarta.persistence.Convert;"), out);
        assertTrue(out.contains("@Convert(converter = CardType.EntityConverter.class)"), out);
        assertTrue(out.contains("@Column(length = 1)"), out);
    }
}
