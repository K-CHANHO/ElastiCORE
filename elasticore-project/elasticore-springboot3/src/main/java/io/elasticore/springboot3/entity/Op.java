package io.elasticore.springboot3.entity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.List;

/**
 * Enumeration of comparison operations for dynamic query generation.
 * <p>
 * Each operation maps to a corresponding {@link CriteriaBuilder} method.
 * </p>
 */
public enum Op {

    /** Equal to ( = ). */
    EQ((cb, path, value) -> cb.equal(path, value)),

    /** Not equal to ( != ). */
    NEQ((cb, path, value) -> cb.notEqual(path, value)),

    /** LIKE (pattern matching with wildcards). */
    LIKE((cb, path, value) -> cb.like((Path<String>) path, "%" + value + "%")),

    /** NOT LIKE (negated pattern matching). */
    NOT_LIKE((cb, path, value) -> cb.notLike((Path<String>) path, "%" + value + "%")),

    /** STARTS WITH (prefix pattern matching). */
    STARTS_WITH((cb, path, value) -> cb.like((Path<String>) path, value + "%")),

    /** ENDS WITH (suffix pattern matching). */
    ENDS_WITH((cb, path, value) -> cb.like((Path<String>) path, "%" + value)),

    /**
     * Case-insensitive LIKE ( LOWER(col) LIKE LOWER(%value%) ).
     * Database-portable case-insensitive contains search; mirrors the DSL {@code %__%} pattern.
     */
    ILIKE((cb, path, value) -> cb.like(cb.lower((Path<String>) path), "%" + String.valueOf(value).toLowerCase() + "%")),

    /** Case-insensitive NOT LIKE ( negated LOWER(col) LIKE LOWER(%value%) ). */
    NOT_ILIKE((cb, path, value) -> cb.notLike(cb.lower((Path<String>) path), "%" + String.valueOf(value).toLowerCase() + "%")),

    /** Case-insensitive STARTS WITH ( LOWER(col) LIKE LOWER(value%) ). */
    ISTARTS_WITH((cb, path, value) -> cb.like(cb.lower((Path<String>) path), String.valueOf(value).toLowerCase() + "%")),

    /** Case-insensitive ENDS WITH ( LOWER(col) LIKE LOWER(%value) ). */
    IENDS_WITH((cb, path, value) -> cb.like(cb.lower((Path<String>) path), "%" + String.valueOf(value).toLowerCase())),

    /** Case-insensitive equals ( LOWER(col) = LOWER(value) ). */
    IEQ((cb, path, value) -> cb.equal(cb.lower((Path<String>) path), String.valueOf(value).toLowerCase())),


    /** Greater than ( &gt; ). */
    GT((cb, path, value) -> {
        Path<Comparable> p = (Path<Comparable>) path;
        return cb.greaterThan(p, (Comparable) value);
    }),

    /** Greater than or equal to ( &gt;= ). */
    GTE((cb, path, value) -> {
        Path<? extends Comparable> p = (Path<? extends Comparable>) path;
        return cb.greaterThanOrEqualTo(p, (Comparable) value);
    }),

    /** Less than or equal to (&lt;=). */
    LTE((cb, path, value) -> {
        Path<? extends Comparable> p = (Path<? extends Comparable>) path;
        return cb.lessThanOrEqualTo(p, (Comparable) value);
    }),

    /** Less than ( &lt; ). */
    LT((cb, path, value) -> {
        Path<? extends Comparable> p = (Path<? extends Comparable>) path;
        return cb.lessThan(p, (Comparable) value);
    }),

    /** IN (value exists in collection). */
    IN((cb, path, value) -> path.in((Collection<?>) value)),

    /** NOT IN (value does not exist in collection). */
    NOT_IN((cb, path, value) -> cb.not(path.in((Collection<?>) value))),

    /** IS NULL (field is null). */
    IS_NULL((cb, path, value) -> cb.isNull(path)),

    /** IS NOT NULL (field is not null). */
    IS_NOT_NULL((cb, path, value) -> cb.isNotNull(path)),

    /** BETWEEN (value is within a range). */
    BETWEEN((cb, path, value) -> {
        List<?> list = (List<?>) value;
        Path<? extends Comparable> compPath = (Path<? extends Comparable>) path;
        return cb.between(compPath, (Comparable) list.get(0), (Comparable) list.get(1));
    });

    /** Function that creates a {@link Predicate} from CriteriaBuilder, Path, and value. */
    private final TriFunction<CriteriaBuilder, Path<?>, Object, Predicate> expr;

    /**
     * Constructs an operation with the corresponding CriteriaBuilder logic.
     *
     * @param expr function that builds the predicate
     */
    Op(TriFunction<CriteriaBuilder, Path<?>, Object, Predicate> expr) {
        this.expr = expr;
    }

    /**
     * Converts this operation into a {@link Predicate}.
     *
     * @param cb    CriteriaBuilder instance
     * @param path  field path
     * @param value value to compare
     * @return constructed Predicate
     */
    public Predicate toPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        return expr.apply(cb, path, value);
    }

    /**
     * Functional interface representing a function that takes three arguments and returns a result.
     *
     * @param <A> type of the first argument
     * @param <B> type of the second argument
     * @param <C> type of the third argument
     * @param <R> type of the result
     */
    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {

        /**
         * Applies this function to the given arguments.
         *
         * @param a the first function argument
         * @param b the second function argument
         * @param c the third function argument
         * @return the function result
         */
        R apply(A a, B b, C c);
    }
}
