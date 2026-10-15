package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.dto.DataTransferAnnotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.util.CodeTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that nested relation DTO fields auto-added from an entity relation
 * (marked with {@link DataTransferAnnotation#META_AUTO_RELATION}) are documented
 * as READ_ONLY in the generated OpenAPI {@code @Schema} annotation, while all
 * other fields keep their existing documentation output.
 *
 * <p>Context: for {@code batch: BatchInfo @ManyToOne}, the auto-generated main DTO
 * contains both a nested {@code BatchInfoDTO batch} (populated on read) and a flattened
 * {@code batchBatchId} FK field (used on write). Marking the nested field READ_ONLY
 * removes it from the request-body schema in Swagger without changing runtime
 * (de)serialization.</p>
 */
class NestedRelationSchemaTest {

    private final SrcFilePublisher publisher = new SrcFilePublisher(null);

    private Field fieldWith(String name, String type, String... annotationNames) {
        Map<String, Annotation> map = new HashMap<>();
        for (String ann : annotationNames) {
            Annotation a = Annotation.create(ann);
            map.put(a.getName(), a);
        }
        // Provide an explicit @example so getExample() returns early; the enum-example
        // fallback needs a live publisher context, which this unit test does not build.
        Annotation example = Annotation.create("example", "sample");
        map.put(example.getName(), example);

        Field f = Field.builder().name(name).type(type).annotationMap(map).build();
        f.getTypeInfo();
        return f;
    }

    @Test
    @DisplayName("Auto relation nested DTO field gets accessMode = READ_ONLY")
    void autoRelationFieldIsReadOnly() {
        Field f = fieldWith("batch", "BatchInfoDTO",
                DataTransferAnnotation.META_AUTO_RELATION,
                DataTransferAnnotation.META_SEARCHABLE_BYPASS);

        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        publisher.setFieldDocumentation(f, p);

        String line = p.toString(0);
        assertTrue(line.startsWith("@Schema("), "Should emit a @Schema annotation: " + line);
        assertTrue(line.contains("accessMode = Schema.AccessMode.READ_ONLY"),
                "Nested relation field must be documented READ_ONLY: " + line);
    }

    @Test
    @DisplayName("Normal field does NOT get accessMode")
    void normalFieldHasNoAccessMode() {
        Field f = fieldWith("scheduleNm", "string");

        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        publisher.setFieldDocumentation(f, p);

        String line = p.toString(0);
        assertFalse(line.contains("accessMode"),
                "Regular fields must keep their existing schema output: " + line);
    }

    @Test
    @DisplayName("FK id style field (no marker) does NOT get accessMode")
    void fkIdFieldHasNoAccessMode() {
        // The flattened FK field added by checkDTOIdentity carries @ref/@searchable,
        // but not the autoRelation marker - it must remain writable in the schema.
        Field f = fieldWith("batchBatchId", "string", "ref");

        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        publisher.setFieldDocumentation(f, p);

        assertFalse(p.toString(0).contains("accessMode"));
    }

    @Test
    @DisplayName("READ_ONLY combines with required flag without corrupting the annotation")
    void readOnlyWithNotNull() {
        Field f = fieldWith("batch", "BatchInfoDTO",
                DataTransferAnnotation.META_AUTO_RELATION, "notnull");

        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        publisher.setFieldDocumentation(f, p);

        String line = p.toString(0);
        assertTrue(line.contains("requiredMode=Schema.RequiredMode.REQUIRED"));
        assertTrue(line.contains("accessMode = Schema.AccessMode.READ_ONLY"));
        assertTrue(line.endsWith(")"), "Annotation must be well-formed: " + line);
    }
}
