package io.elasticore.springboot3.entity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

/**
 * Enum representing string-based functions used in JPA Criteria queries.
 * <p>
 * Examples include LOWER, UPPER, TRIM, LEAST, and GREATEST. These functions
 * transform or compare string values in a type-safe and reusable way during query building.
 */
public enum StrFunc {
    /** Returns the least (minimum) value among string expressions */
    LEAST((cb, path) -> cb.least((Path<String>) path), String.class),
    /** Returns the greatest (maximum) value among string expressions */
    GREATEST((cb, path) -> cb.greatest((Path<String>) path), String.class),
    /** Converts string to lowercase */
    LOWER((cb, path) -> cb.lower((Path<String>) path), String.class),
    /** Converts string to uppercase */
    UPPER((cb, path) -> cb.upper((Path<String>) path), String.class),
    /** Removes leading and trailing whitespace from string */
    TRIM((cb, path) -> cb.trim((Path<String>) path), String.class);

    private final ExpressionFunction fn;
    private final Class<?> resultType;

    StrFunc(ExpressionFunction fn, Class<?> resultType) {
        this.fn = fn;
        this.resultType = resultType;
    }

    /**
     * Applies the string function to the given field path.
     *
     * @param cb   the CriteriaBuilder instance
     * @param path the path representing the string field
     * @param <T>  the expected result type (typically String)
     * @return the expression resulting from applying the function
     */
    @SuppressWarnings("unchecked")
    public <T> Expression<T> apply(CriteriaBuilder cb, Path<?> path) {
        return (Expression<T>) fn.apply(cb, path);
    }

    /**
     * Gets the return type of the function.
     *
     * @param <T> the result type
     * @return the class of the result type (usually String.class)
     */
    @SuppressWarnings("unchecked")
    public <T> Class<T> getResultType() {
        return (Class<T>) resultType;
    }
}
