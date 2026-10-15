package io.elasticore.springboot3.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe fluent builder for JPA CriteriaDelete operations.
 * Analogous to {@link UpdateSpec} for UPDATE and {@link JoinSpec} for SELECT.
 *
 * <p>Usage example:</p>
 * <pre>
 * int deleted = Q.AuditLog.deleteSpec()
 *     .where(Q.AuditLog.status(Op.EQ, "ARCHIVED"))
 *     .where(Q.AuditLog.createdDate(Op.LT, cutoffDate))
 *     .execute(entityManager);
 * </pre>
 *
 * @param <T> the entity type
 */
public class DeleteSpec<T> {

    private final Class<T> entityClass;
    private final List<Specification<T>> conditions = new ArrayList<>();

    /**
     * Constructs a DeleteSpec for the given entity class.
     *
     * @param entityClass the entity class to delete from
     */
    public DeleteSpec(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ===== WHERE conditions =====

    /**
     * Add WHERE conditions. Reuses existing Q-based Specifications.
     *
     * @param specs one or more Specification conditions
     * @return this DeleteSpec for chaining
     */
    @SafeVarargs
    public final DeleteSpec<T> where(Specification<T>... specs) {
        for (Specification<T> spec : specs) {
            if (spec != null) conditions.add(spec);
        }
        return this;
    }

    /**
     * Explicitly allow deleting all rows (no WHERE clause).
     * Must be called consciously to prevent accidental full-table deletes.
     *
     * @return this DeleteSpec for chaining
     */
    public DeleteSpec<T> whereAll() {
        conditions.add((root, query, cb) -> cb.conjunction());
        return this;
    }

    // ===== Execute =====

    /**
     * Build and execute the CriteriaDelete query.
     *
     * @param em the EntityManager
     * @return number of rows deleted
     * @throws IllegalArgumentException if no WHERE conditions defined
     */
    public int execute(EntityManager em) {
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Delete condition is missing. Full table delete is not allowed. " +
                            "Use whereAll() explicitly to delete all rows.");
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClass);
        Root<T> root = delete.from(entityClass);

        Predicate combined = cb.conjunction();
        for (Specification<T> spec : conditions) {
            Predicate p = spec.toPredicate(root, cb.createQuery(entityClass), cb);
            if (p != null) {
                combined = cb.and(combined, p);
            }
        }
        delete.where(combined);

        return em.createQuery(delete).executeUpdate();
    }

    /**
     * Count how many rows would be affected before deleting.
     * Useful for previewing the impact of a delete operation.
     *
     * @param em the EntityManager
     * @return count of matching rows
     */
    public long count(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));

        Predicate combined = cb.conjunction();
        for (Specification<T> spec : conditions) {
            Predicate p = spec.toPredicate(root, query, cb);
            if (p != null) {
                combined = cb.and(combined, p);
            }
        }
        query.where(combined);

        return em.createQuery(query).getSingleResult();
    }
}
