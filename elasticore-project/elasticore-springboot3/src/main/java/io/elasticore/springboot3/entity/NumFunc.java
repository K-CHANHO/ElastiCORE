package io.elasticore.springboot3.entity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

/**
 * Enumeration of numeric functions available for JPA Criteria queries.
 * <p>
 * Each function represents a numeric aggregation or transformation operation
 * such as {@code MAX}, {@code MIN}, {@code SUM}, {@code AVG}, etc.
 * </p>
 */
public enum NumFunc {

    /** Returns the maximum value of the expression. */
    MAX(Number.class, (cb, path) -> cb.max((Path<Number>) path)),

    /** Returns the minimum value of the expression. */
    MIN(Number.class, (cb, path) -> cb.min((Path<Number>) path)),

    /** Returns the sum of the expression values. */
    SUM(Number.class, (cb, path) -> cb.sum((Path<Number>) path)),

    /** Returns the average of the expression values. */
    AVG(Double.class, (cb, path) -> cb.avg((Path<Number>) path)),

    /** Returns the absolute value of the expression. */
    ABS(Number.class, (cb, path) -> cb.abs((Path<Number>) path)),

    /** Returns the floor (largest integer less than or equal) of the expression. */
    FLOOR(Number.class, (cb, path) -> cb.floor((Path<Number>) path)),

    /** Returns the ceiling (smallest integer greater than or equal) of the expression. */
    CEIL(Number.class, (cb, path) -> cb.ceiling((Path<Number>) path)),

    /** Returns the count of non-null values. */
    COUNT(Long.class, (cb, path) -> cb.count(path)),

    /** Returns the count of distinct non-null values. */
    COUNT_DISTINCT(Long.class, (cb, path) -> cb.countDistinct(path));

    /** The expected result type of the function. */
    private final Class<?> resultType;

    /** Functional interface representing how the function is applied. */
    private final ExpressionFunction fn;

    /**
     * Constructs a numeric function.
     *
     * @param resultType the expected result type
     * @param fn         the function implementation
     */
    NumFunc(Class<?> resultType, ExpressionFunction fn) {
        this.resultType = resultType;
        this.fn = fn;
    }

    /**
     * Applies the numeric function to the given field path.
     *
     * @param cb   the CriteriaBuilder instance
     * @param path the field path to apply the function on
     * @param <T>  the result type
     * @return the resulting {@link Expression}
     */
    @SuppressWarnings("unchecked")
    public <T> Expression<T> apply(CriteriaBuilder cb, Path<?> path) {
        return (Expression<T>) fn.apply(cb, path);
    }

    /**
     * Gets the expected result type class of this function.
     *
     * @param <T> the type parameter
     * @return the result type
     */
    @SuppressWarnings("unchecked")
    public <T> Class<T> getResultType() {
        return (Class<T>) resultType;
    }
}
