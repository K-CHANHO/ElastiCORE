package io.elasticore.blueprint.ingest;

import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.runtime.DslLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies DSL loading via classpath and filesystem paths, plus caching.
 */
class DslLoaderTest {

    private static final String DSL_URI = "classpath:ingest-dsl/sales-monthly.yml";
    private static final String ENTITY_NAME = "SalesMonthly";

    @BeforeEach
    void reset() {
        DslLoader.getInstance().invalidateAll();
    }

    @Test
    void loadsDslFromClasspath() {
        ECoreModel model = DslLoader.getInstance().load(DSL_URI);
        assertThat(model).isNotNull();
        assertThat(model.getEntityModels()).isNotNull();

        Entity entity = model.getEntityModels().findByName(ENTITY_NAME);
        assertThat(entity).isNotNull();
        assertThat(entity.getItems().size()).isGreaterThan(20);
        assertThat(entity.getMetaInfo().hasMetaAnnotation("runtime")).isTrue();
    }

    @Test
    void secondLoadHitsCache() {
        ECoreModel first = DslLoader.getInstance().load(DSL_URI);
        int sizeAfterFirst = DslLoader.getInstance().cacheSize();

        ECoreModel second = DslLoader.getInstance().load(DSL_URI);

        assertThat(second).isSameAs(first);
        assertThat(DslLoader.getInstance().cacheSize()).isEqualTo(sizeAfterFirst);
    }

    @Test
    void invalidateForcesReload() {
        ECoreModel first = DslLoader.getInstance().load(DSL_URI);
        DslLoader.getInstance().invalidate(DSL_URI);
        ECoreModel second = DslLoader.getInstance().load(DSL_URI);
        assertThat(second).isNotSameAs(first);
    }

    @Test
    void loadsDslFromFilesystem() throws IOException {
        Path tmp = Files.createTempFile("elcore-dsl-test-", ".yml");
        Files.write(tmp, minimalDsl().getBytes(StandardCharsets.UTF_8));
        try {
            ECoreModel model = DslLoader.getInstance().load(tmp.toAbsolutePath().toString());
            Entity entity = model.getEntityModels().findByName("TestRuntimeEntity");
            assertThat(entity).isNotNull();
            assertThat(entity.getMetaInfo().hasMetaAnnotation("runtime")).isTrue();
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void missingFileThrows() {
        assertThatThrownBy(() -> DslLoader.getInstance().load("/no/such/file.yml"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void missingClasspathResourceThrows() {
        assertThatThrownBy(() -> DslLoader.getInstance().load("classpath:does/not/exist.yml"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    private static String minimalDsl() {
        return "entity:\n" +
                "  TestRuntimeEntity:\n" +
                "    meta: entity @runtime(map) @target(TargetFoo)\n" +
                "    fields:\n" +
                "      id:   string  @id  @target(id)\n" +
                "      name: string       @target(name)\n" +
                "    staticValues:\n" +
                "      flag: \"Y\"\n";
    }
}
