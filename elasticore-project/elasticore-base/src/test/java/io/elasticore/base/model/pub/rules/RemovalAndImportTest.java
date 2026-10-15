package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.JpaEntityBaseRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Answers two extension-surface guarantees:
 *
 * 1. User rules can REMOVE what base rules produced - individual
 *    annotations (removeAnnotation), whole fields (removeField), trailing
 *    members such as @PrePersist (removeAfter), and entire base rules
 *    (suppress).
 *
 * 2. Import collection reflects only what actually survived: removed or
 *    suppressed emissions leave no import behind (clean mode foundation;
 *    the template-owned import block remains the byte-compatible default).
 */
public class RemovalAndImportTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec card() {
        return new EntitySpec("Card")
                .addField(loader.parse("cardId", "string(36) @id @genid"))
                .addField(loader.parse("cardNo", "string(50)"))
                .addField(loader.parse("memo", "string(200)"));
    }

    // ------------------------------------------------------------------
    // 1. removal of base-rule output
    // ------------------------------------------------------------------

    /** Corporate rules that strip parts of the base output. */
    public static class StripRules implements CodeRules {

        @Rule(value = "strip-column-of-memo", order = 100)
        void stripColumn(EntitySpec e, FieldSpec f) {
            if (!f.name().equals("memo")) return;
            // Removes the @Column the base rule just added for this field.
            ctx(e).field(f).removeAnnotation("jakarta.persistence.Column");
        }

        @Rule(value = "strip-pre-persist", order = 110)
        void stripPrePersist(EntitySpec e, FieldSpec f) {
            if (!f.hasDsl("genid")) return;
            // Removes the @PrePersist member the base id rule attached.
            ctx(e).field(f).removeAfter();
        }
    }

    @Test
    public void userRule_removesBaseAnnotationAndTrailingMember() {
        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .register(new StripRules())
                .run(card());

        String out = code.renderFieldBlock();

        // memo keeps its declaration but lost the base @Column
        assertTrue(out.contains("private String memo;"), out);
        assertFalse(out.contains("@Column(name = \"memo\""), "base @Column must be removed:\n" + out);
        // other fields keep theirs
        assertTrue(out.contains("@Column(name = \"card_no\", length = 50)"), out);
        // the @PrePersist member of the genid pk is gone
        assertFalse(out.contains("@PrePersist"), "base @PrePersist must be removed:\n" + out);
        assertTrue(out.contains("@Id"), out);
    }

    // ------------------------------------------------------------------
    // 2. used-imports collection follows removals
    // ------------------------------------------------------------------

    @Test
    public void usedImports_containOnlySurvivingAnnotationTypes() {
        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .run(card());

        List<String> imports = code.usedImports();

        assertTrue(imports.contains("jakarta.persistence.Id"), String.valueOf(imports));
        assertTrue(imports.contains("jakarta.persistence.Column"), String.valueOf(imports));
    }

    @Test
    public void usedImports_dropWhatRemovalAndSuppressDropped() {
        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .register(new StripRules())
                .suppress("jpa-id")
                .run(card());

        List<String> imports = code.usedImports();

        // jpa-id suppressed -> no @Id anywhere -> no Id import
        assertFalse(imports.contains("jakarta.persistence.Id"), String.valueOf(imports));
        // memo lost @Column but cardNo still has one -> Column import stays
        assertTrue(imports.contains("jakarta.persistence.Column"), String.valueOf(imports));
    }

    @Test
    public void usedImports_ignoreRawMarkersAndRemovedFields() {
        EntitySpec e = card();
        SourceCode code = new RuleEngine()
                .register(new JpaEntityBaseRules())
                .run(e);

        // Simulate a raw native annotation and a removed field afterwards.
        FieldSpec memo = e.fields().get(2);
        code.field(memo).ensure(AnnoCode.raw("native:@Version", "@Version"));
        code.removeField(e.fields().get(1));            // cardNo out entirely

        List<String> imports = code.usedImports();

        for (String i : imports) {
            assertFalse(i.contains("@") || i.contains(":"),
                    "raw markers must not leak into imports: " + i);
        }
        // cardNo removed, but memo/cardId still carry @Column
        assertTrue(imports.contains("jakarta.persistence.Column"), String.valueOf(imports));
    }
}
