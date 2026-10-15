package io.elasticore.springboot3.domain;

/**
 * Finds a row by its identity, for an entity filling in a @snapshot value
 * from the row a reference points at. The service hands it
 * {@code entityManager::find}; a test hands it a map.
 *
 * Not generic, so a Kotlin lambda can be one.
 */
@FunctionalInterface
public interface Lookup {

    /** The row of this type with this identity, or null. */
    Object find(Class<?> type, Object id);
}
