package io.elasticore.springboot3.entity;

import jakarta.persistence.criteria.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JoinContext - Manages join references during query building.
 * Provides access to created joins and cross-join roots.
 */
public class JoinContext {

    private final Root<?> mainRoot;
    private final Map<String, Join<?, ?>> joins = new HashMap<>();
    private final Map<String, Root<?>> roots = new HashMap<>();

    /**
     * Constructs a JoinContext with the given main query root.
     *
     * @param mainRoot the main query root
     */
    public JoinContext(Root<?> mainRoot) {
        this.mainRoot = mainRoot;
    }

    /**
     * Registers a join under the given path key.
     *
     * @param path the join path key
     * @param join the join to register
     */
    public void addJoin(String path, Join<?, ?> join) {
        joins.put(path, join);
    }

    /**
     * Registers a cross-join root under the given alias.
     *
     * @param alias the alias for the root
     * @param root  the root to register
     */
    public void addRoot(String alias, Root<?> root) {
        roots.put(alias, root);
    }

    /**
     * Returns whether a join exists for the given path.
     *
     * @param path the join path to check
     * @return true if a join exists for the path, false otherwise
     */
    public boolean hasJoin(String path) {
        return joins.containsKey(path);
    }

    /**
     * Returns the join registered for the given path.
     *
     * @param <X>  the source type of the join
     * @param <Y>  the target type of the join
     * @param path the join path key
     * @return the join associated with the path
     * @throws IllegalArgumentException if no join is found for the path
     */
    @SuppressWarnings("unchecked")
    public <X, Y> Join<X, Y> getJoin(String path) {
        Join<?, ?> join = joins.get(path);
        if (join == null) {
            throw new IllegalArgumentException("Join not found: " + path);
        }
        return (Join<X, Y>) join;
    }

    /**
     * Returns the cross-join root registered under the given alias.
     *
     * @param <X>   the entity type of the root
     * @param alias the alias under which the root was registered
     * @return the root associated with the alias
     * @throws IllegalArgumentException if no root is found for the alias
     */
    @SuppressWarnings("unchecked")
    public <X> Root<X> getRoot(String alias) {
        Root<?> root = roots.get(alias);
        if (root == null) {
            throw new IllegalArgumentException("Root not found: " + alias);
        }
        return (Root<X>) root;
    }

    /**
     * Returns the main query root.
     *
     * @return the main root
     */
    public Root<?> getMainRoot() {
        return mainRoot;
    }

    /**
     * Returns the path to a field on a joined entity.
     *
     * @param <Y>       the field type
     * @param joinPath  the join path key
     * @param fieldName the field name on the joined entity
     * @return the Path for the field
     */
    public <Y> Path<Y> path(String joinPath, String fieldName) {
        return getJoin(joinPath).get(fieldName);
    }

    /**
     * Returns the path to a field on a cross-joined root.
     *
     * @param <Y>       the field type
     * @param alias     the alias of the cross-joined root
     * @param fieldName the field name on the cross-joined root
     * @return the Path for the field
     */
    public <Y> Path<Y> rootPath(String alias, String fieldName) {
        return getRoot(alias).get(fieldName);
    }
}