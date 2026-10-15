package io.elasticore.base;

import io.elasticore.base.model.loader.FileBasedModelLoader;

/**
 * A factory class for creating instances of {@link ModelLoader}.
 * This class provides a static method to obtain a ModelLoader instance based on the specified type.
 * Currently, it supports creating a file-based model loader but can be extended to support other types.
 */
public class ModelLoaderFactory {

    /**
     * Retrieves a {@link ModelLoader} instance based on the provided type.
     * The method currently supports creating a file-based model loader. This approach
     * allows for easy extension to support additional loader types in the future.
     *
     * @param type The type of loader to create, as defined in the {@link Type} enum.
     * @return An instance of {@link ModelLoader} corresponding to the specified type, or null if the type is not supported.
     */
    public static ModelLoader getLoader(Type type) {
        if (type == Type.DEFAULT) {
            // Example implementation with a file-based model loader
            return new FileBasedModelLoader();
        }
        // Placeholder for future extension to support additional loader types
        return null;
    }

    /**
     * Defines the types of model loaders supported by the factory.
     * This enumeration can be expanded to include additional loader types as needed.
     */
    public static enum Type {
        DEFAULT,  // Represents a default model loading strategy, currently mapped to a file-based loader.
        DATABASE; // Placeholder for future implementation of a database-backed model loader.
    }
}
