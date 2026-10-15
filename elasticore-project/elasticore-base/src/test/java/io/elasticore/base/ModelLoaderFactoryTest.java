package io.elasticore.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelLoaderFactory Tests")
class ModelLoaderFactoryTest {

    @Test
    @DisplayName("Should create ModelLoaderFactory")
    void testCreateModelLoaderFactory() {
        // When
        ModelLoaderFactory factory = new ModelLoaderFactory();

        // Then
        assertNotNull(factory);
    }

    @Test
    @DisplayName("Should create DEFAULT ModelLoader")
    void testCreateDefaultModelLoader() {
        // When
        ModelLoader loader = ModelLoaderFactory.getLoader(ModelLoaderFactory.Type.DEFAULT);

        // Then
        assertNotNull(loader);
    }

    @Test
    @DisplayName("Should return null for unsupported type")
    void testCreateWithDatabaseType() {
        // When
        ModelLoader loader = ModelLoaderFactory.getLoader(ModelLoaderFactory.Type.DATABASE);

        // Then
        assertNull(loader);
    }

    @Test
    @DisplayName("Should handle null type")
    void testCreateWithNullType() {
        // When
        ModelLoader loader = ModelLoaderFactory.getLoader(null);

        // Then
        assertNull(loader);
    }
}