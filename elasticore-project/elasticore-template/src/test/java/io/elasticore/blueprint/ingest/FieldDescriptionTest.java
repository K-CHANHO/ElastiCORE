package io.elasticore.blueprint.ingest;

import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.runtime.DslLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the bug "runtime DSL loader does not populate Field.getDescription()".
 * "--" inline comments are parsed into @label / @desc annotations by AbstractModelLoader.
 * This test asserts that Field.getDescription() also reflects those annotations, so callers
 * do not have to reach into the annotation map directly.
 */
class FieldDescriptionTest {

    private static final String DSL_URI = "classpath:ingest-dsl/desc-comment-fixture.yml";
    private static final String ENTITY_NAME = "DescCommentFixture";

    private static Entity entity;

    @BeforeAll
    static void loadEntity() {
        DslLoader.getInstance().invalidateAll();
        ECoreModel model = DslLoader.getInstance().load(DSL_URI);
        entity = model.getEntityModels().findByName(ENTITY_NAME);
        assertThat(entity).as("fixture entity loaded").isNotNull();
    }

    @Test
    void plainDashCommentBecomesFieldDescription() {
        Field f = findField("plainLabel");
        assertThat(f.getDescription()).isEqualTo("Plain label");
        // Backward-compat: annotation-map access must still work.
        assertThat(f.getAnnotationValue("label")).isEqualTo("Plain label");
    }

    @Test
    void labelPipeDescExtractsLabelIntoDescription() {
        Field f = findField("labelAndDesc");
        // loadAnnotationMap splits "label | desc" into @label + @desc.
        // Field.getDescription() returns @label per convention (matches SchemaCodePublisher).
        assertThat(f.getDescription()).isEqualTo("Short label");
        assertThat(f.getAnnotationValue("label")).isEqualTo("Short label");
        assertThat(f.getAnnotationValue("desc")).isEqualTo("Detailed description text");
    }

    @Test
    void fieldWithoutDashCommentHasNullDescription() {
        Field f = findField("noComment");
        assertThat(f.getDescription()).isNull();
    }

    @Test
    void dashCommentCoexistsWithFnAnnotation() {
        Field f = findField("withFn");
        assertThat(f.getDescription()).isEqualTo("Trimmed value");
        assertThat(f.getAnnotationValue("fn", "function")).isEqualTo("trim");
    }

    private static Field findField(String name) {
        ModelComponentItems<Field> items = entity.getItems();
        for (int i = 0; i < items.size(); i++) {
            Field f = items.get(i);
            if (name.equals(f.getName())) return f;
        }
        throw new AssertionError("field not found: " + name);
    }
}
