package io.elasticore.base.model.entity;

import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies how {@link Field#getDbColumnName()} resolves the JPA {@code @Column(name=...)} value:
 * an explicit {@code @column}/{@code @col}/{@code @db} annotation must take precedence over the
 * automatic camelCase-to-snake_case conversion.
 *
 * <p>Context: the auto converter ({@code StringUtils.camelToSnake}) does NOT insert an underscore
 * at a digit-to-uppercase boundary, so {@code kind1Label} becomes {@code kind1label}. An explicit
 * annotation lets the user pin the exact column name (e.g. {@code kind1_label}).</p>
 */
class DbColumnNameTest {

    private static class TestModelLoader extends AbstractModelLoader {
        Field parse(String fieldNm, String fieldLine) {
            Field f = super.parseFieldLine(fieldNm, fieldLine, null);
            // Trigger lazy TypeInfo initialization, as the real publisher flow does
            // (EntitySrcFilePublisher calls getTypeInfo() before getDbColumnName()).
            f.getTypeInfo();
            return f;
        }
        @Override
        public Map<String, Annotation> loadAnnotationMap(String fieldLine) {
            return super.loadAnnotationMap(fieldLine);
        }
    }

    private final TestModelLoader loader = new TestModelLoader();

    @Test
    @DisplayName("@column(kind1_label) overrides auto-conversion -> exact column name")
    void explicitColumnAnnotationWins() {
        Field f = loader.parse("kind1Label", "string @column(kind1_label)");
        assertEquals("kind1_label", f.getDbColumnName(),
                "Explicit @column value must be used verbatim as the column name");
    }

    @Test
    @DisplayName("@col(kind1_label) alias also overrides")
    void colAliasWins() {
        Field f = loader.parse("kind1Label", "string @col(kind1_label)");
        assertEquals("kind1_label", f.getDbColumnName());
    }

    @Test
    @DisplayName("@db(kind1_label) alias also overrides")
    void dbAliasWins() {
        Field f = loader.parse("kind1Label", "string @db(kind1_label)");
        assertEquals("kind1_label", f.getDbColumnName());
    }

    @Test
    @DisplayName("Without annotation: digit-to-uppercase boundary loses underscore (current bug)")
    void autoConversionDropsUnderscoreAtDigitBoundary() {
        Field f = loader.parse("kind1Label", "string");
        // Documents current behavior: 'L' is preceded by digit '1', so no underscore is inserted.
        assertEquals("kind1label", f.getDbColumnName());
    }

    @Test
    @DisplayName("Without annotation: normal lower-to-upper boundary converts correctly")
    void autoConversionWorksForNormalCamelCase() {
        Field f = loader.parse("someLabel", "string");
        assertEquals("some_label", f.getDbColumnName());
    }
}
