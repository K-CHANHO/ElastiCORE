package io.elasticore.springboot3.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe fluent builder for JPA CriteriaUpdate operations.
 * Analogous to {@link JoinSpec} for SELECT queries.
 *
 * <p>Usage example:</p>
 * <pre>
 * int count = Q.ClaimInfo.updateSpec()
 *     .set(Q.ClaimInfo.getStatus(), "APPROVED")
 *     .set(Q.ClaimInfo.getProcessedDate(), LocalDate.now())
 *     .where(Q.ClaimInfo.status(Op.EQ, "PENDING"))
 *     .execute(entityManager);
 * </pre>
 *
 * @param <T> the entity type
 */
public class UpdateSpec<T> {

    private final Class<T> entityClass;
    private final List<SetClause<T>> setClauses = new ArrayList<>();
    private final List<Specification<T>> conditions = new ArrayList<>();

    /**
     * Constructs an UpdateSpec for the given entity class.
     *
     * @param entityClass the entity class to update
     */
    public UpdateSpec(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ===== SET operations =====

    /**
     * Set a field to a literal value.
     *
     * @param <V>   the value type
     * @param field the target field (from Q class)
     * @param value the value to set
     * @return this UpdateSpec for chaining
     */
    public <V> UpdateSpec<T> set(FieldInfo<T> field, V value) {
        setClauses.add((update, root, cb) ->
                update.set(root.get(field.getName()), value));
        return this;
    }

    /**
     * Set a field to null explicitly.
     *
     * @param field the target field (from Q class)
     * @return this UpdateSpec for chaining
     */
    public UpdateSpec<T> setNull(FieldInfo<T> field) {
        setClauses.add((update, root, cb) ->
                update.set(root.<Object>get(field.getName()), (Object) null));
        return this;
    }

    /**
     * Set a field to the value of another field.
     *
     * @param targetField the field to update
     * @param sourceField the field to copy value from
     * @return this UpdateSpec for chaining
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public UpdateSpec<T> set(FieldInfo<T> targetField, FieldInfo<T> sourceField) {
        setClauses.add((update, root, cb) ->
                ((CriteriaUpdate) update).set(
                        root.get(targetField.getName()),
                        (Expression) root.get(sourceField.getName())));
        return this;
    }

    /**
     * Set a field using a CriteriaBuilder expression.
     *
     * <p>Example: field = field * 1.1</p>
     * <pre>
     * .set(Q.Product.getPrice(), (root, cb) -&gt;
     *     cb.prod(root.get("price"), cb.literal(1.1)))
     * </pre>
     *
     * @param <V>        the expression result type
     * @param field      the target field
     * @param expression function that builds the expression
     * @return this UpdateSpec for chaining
     */
    @SuppressWarnings("unchecked")
    public <V> UpdateSpec<T> set(FieldInfo<T> field, SetExpression<T, V> expression) {
        setClauses.add((update, root, cb) -> {
            Expression<V> expr = expression.toExpression(root, cb);
            update.set((Path<V>) root.get(field.getName()), expr);
        });
        return this;
    }

    /**
     * Increment a numeric field by the given amount.
     *
     * @param field  the numeric field
     * @param amount the amount to add
     * @return this UpdateSpec for chaining
     */
    @SuppressWarnings("unchecked")
    public UpdateSpec<T> increment(FieldInfo<T> field, Number amount) {
        setClauses.add((update, root, cb) -> {
            Path<Number> path = root.get(field.getName());
            update.set(path, (Expression<? extends Number>) cb.sum(path, amount));
        });
        return this;
    }

    /**
     * Decrement a numeric field by the given amount.
     *
     * @param field  the numeric field
     * @param amount the amount to subtract
     * @return this UpdateSpec for chaining
     */
    @SuppressWarnings("unchecked")
    public UpdateSpec<T> decrement(FieldInfo<T> field, Number amount) {
        setClauses.add((update, root, cb) -> {
            Path<Number> path = root.get(field.getName());
            update.set(path, (Expression<? extends Number>) cb.diff(path, amount));
        });
        return this;
    }

    /**
     * Set fields from a DTO object. Only non-null fields are applied.
     * This is a convenience bridge for backward compatibility with the
     * reflection-based approach.
     *
     * @param dto the DTO containing field values
     * @return this UpdateSpec for chaining
     */
    public UpdateSpec<T> setFromDTO(Object dto) {
        return setFromDTO(dto, false);
    }

    /**
     * Set fields from a DTO object.
     *
     * @param dto          the DTO containing field values
     * @param includeNulls if true, null fields are also set (as NULL)
     * @return this UpdateSpec for chaining
     */
    public UpdateSpec<T> setFromDTO(Object dto, boolean includeNulls) {
        setClauses.add((update, root, cb) -> {
            Field[] fields = dto.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(dto);
                    if (value != null) {
                        update.set(root.get(field.getName()), value);
                    } else if (includeNulls) {
                        update.set(root.<Object>get(field.getName()), (Object) null);
                    }
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    // Skip fields that don't exist on entity
                }
            }
        });
        return this;
    }

    // ===== WHERE conditions =====

    /**
     * Add WHERE conditions. Reuses existing Q-based Specifications.
     *
     * @param specs one or more Specification conditions
     * @return this UpdateSpec for chaining
     */
    @SafeVarargs
    public final UpdateSpec<T> where(Specification<T>... specs) {
        for (Specification<T> spec : specs) {
            if (spec != null) conditions.add(spec);
        }
        return this;
    }

    /**
     * Explicitly allow updating all rows (no WHERE clause).
     * Must be called consciously to prevent accidental full-table updates.
     *
     * @return this UpdateSpec for chaining
     */
    public UpdateSpec<T> whereAll() {
        conditions.add((root, query, cb) -> cb.conjunction());
        return this;
    }

    // ===== Execute =====

    /**
     * Build and execute the CriteriaUpdate query.
     *
     * @param em the EntityManager
     * @return number of rows updated
     * @throws IllegalStateException    if no SET clauses defined
     * @throws IllegalArgumentException if no WHERE conditions defined
     */
    public int execute(EntityManager em) {
        if (setClauses.isEmpty()) {
            throw new IllegalStateException(
                    "No SET clauses defined. Call set() before execute().");
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Update condition is missing. Full table update is not allowed. " +
                            "Use whereAll() explicitly to update all rows.");
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(entityClass);
        Root<T> root = update.from(entityClass);

        // Apply SET clauses
        for (SetClause<T> clause : setClauses) {
            clause.apply(update, root, cb);
        }

        // Apply WHERE conditions
        Predicate combined = cb.conjunction();
        for (Specification<T> spec : conditions) {
            Predicate p = spec.toPredicate(root, cb.createQuery(entityClass), cb);
            if (p != null) {
                combined = cb.and(combined, p);
            }
        }
        update.where(combined);

        return em.createQuery(update).executeUpdate();
    }

    // ===== Inner types =====

    /**
     * Functional interface for expression-based SET values.
     *
     * @param <T> the entity type
     * @param <V> the expression result type
     */
    @FunctionalInterface
    public interface SetExpression<T, V> {
        /**
         * Builds the expression for the SET value.
         *
         * @param root the query root
         * @param cb   the CriteriaBuilder
         * @return the expression to assign to the field
         */
        Expression<V> toExpression(Root<T> root, CriteriaBuilder cb);
    }

    /**
     * Internal interface for SET clause implementations.
     */
    @FunctionalInterface
    private interface SetClause<T> {
        void apply(CriteriaUpdate<T> update, Root<T> root, CriteriaBuilder cb);
    }
}
