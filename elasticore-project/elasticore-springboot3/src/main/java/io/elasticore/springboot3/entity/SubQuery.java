package io.elasticore.springboot3.entity;

import org.springframework.data.jpa.domain.Specification;
import java.util.*;

/**
 * SubQuery Builder Class
 */
public class SubQuery<T> {

    // ===== Operation Enum =====
    /**
     * Comparison operators for subquery predicates.
     */
    public enum Op {
        /** field IN (SELECT ...) */
        IN,
        /** field NOT IN (SELECT ...) */
        NOT_IN,
        /** field = (SELECT ...) */
        EQ,
        /** field &lt;&gt; (SELECT ...) */
        NE,
        /** field &gt; (SELECT ...) */
        GT,
        /** field &gt;= (SELECT ...) */
        GTE,
        /** field &lt; (SELECT ...) */
        LT,
        /** field &lt;= (SELECT ...) */
        LTE
    }

    /**
     * Aggregate function types for subquery select expressions.
     */
    public enum AggregateType {
        /** COUNT aggregate. */
        COUNT,
        /** SUM aggregate. */
        SUM,
        /** AVG aggregate. */
        AVG,
        /** MAX aggregate. */
        MAX,
        /** MIN aggregate. */
        MIN
    }

    // ===== Fields  =====
    private FieldInfo<?> selectField;
    private Class<?> fromEntity;
    private AggregateType aggregateType;
    private final List<Specification<?>> conditions = new ArrayList<>();
    private final List<Correlation> correlations = new ArrayList<>();

    private SubQuery() {}

    // ===== Static Factory Methods =====

    /**
     * Creates a subquery that selects a specific field.
     *
     * @param <T>   the field type
     * @param field the field to select in the subquery
     * @return a new SubQuery instance
     */
    public static <T> SubQuery<T> select(FieldInfo<T> field) {
        SubQuery<T> sq = new SubQuery<>();
        sq.selectField = field;
        sq.fromEntity = field.getEntityType();
        return sq;
    }

    /**
     * Creates a subquery that selects COUNT(*) from the given entity.
     *
     * @param entity the entity class to count from
     * @return a new SubQuery instance with COUNT aggregate
     */
    public static SubQuery<Long> count(Class<?> entity) {
        SubQuery<Long> sq = new SubQuery<>();
        sq.fromEntity = entity;
        sq.aggregateType = AggregateType.COUNT;
        return sq;
    }

    /**
     * Creates a subquery that selects SUM of the given field.
     *
     * @param field the numeric field to sum
     * @return a new SubQuery instance with SUM aggregate
     */
    public static SubQuery<Number> sum(FieldInfo<? extends Number> field) {
        SubQuery<Number> sq = new SubQuery<>();
        sq.selectField = field;
        sq.fromEntity = field.getEntityType();
        sq.aggregateType = AggregateType.SUM;
        return sq;
    }

    /**
     * Creates a subquery that selects AVG of the given field.
     *
     * @param field the numeric field to average
     * @return a new SubQuery instance with AVG aggregate
     */
    public static SubQuery<Number> avg(FieldInfo<? extends Number> field) {
        SubQuery<Number> sq = new SubQuery<>();
        sq.selectField = field;
        sq.fromEntity = field.getEntityType();
        sq.aggregateType = AggregateType.AVG;
        return sq;
    }

    /**
     * Creates a subquery that selects MAX of the given field.
     *
     * @param <T>   the comparable field type
     * @param field the field to find the maximum of
     * @return a new SubQuery instance with MAX aggregate
     */
    public static <T extends Comparable<T>> SubQuery<T> max(FieldInfo<T> field) {
        SubQuery<T> sq = new SubQuery<>();
        sq.selectField = field;
        sq.fromEntity = field.getEntityType();
        sq.aggregateType = AggregateType.MAX;
        return sq;
    }

    /**
     * Creates a subquery that selects MIN of the given field.
     *
     * @param <T>   the comparable field type
     * @param field the field to find the minimum of
     * @return a new SubQuery instance with MIN aggregate
     */
    public static <T extends Comparable<T>> SubQuery<T> min(FieldInfo<T> field) {
        SubQuery<T> sq = new SubQuery<>();
        sq.selectField = field;
        sq.fromEntity = field.getEntityType();
        sq.aggregateType = AggregateType.MIN;
        return sq;
    }

    /**
     * Creates a subquery from the given entity class, used for EXISTS checks.
     *
     * @param entity the entity class to select from
     * @return a new SubQuery instance
     */
    public static SubQuery<Integer> from(Class<?> entity) {
        SubQuery<Integer> sq = new SubQuery<>();
        sq.fromEntity = entity;
        return sq;
    }

    // ===== Condition methods =====

    /**
     * Adds WHERE conditions to this subquery.
     *
     * @param specs one or more Specification conditions to apply
     * @return this SubQuery for chaining
     */
    @SafeVarargs
    public final SubQuery<T> where(Specification<?>... specs) {
        for (Specification<?> spec : specs) {
            if (spec != null) {
                this.conditions.add(spec);
            }
        }
        return this;
    }

    /**
     * Adds a correlation between a subquery field and a main query field.
     *
     * @param subField  the field in the subquery to correlate
     * @param mainField the field in the main query to correlate with
     * @return this SubQuery for chaining
     */
    public SubQuery<T> correlate(FieldInfo<?> subField, FieldInfo<?> mainField) {
        this.correlations.add(new Correlation(subField, mainField));
        return this;
    }

    // ===== Getters =====

    /**
     * Returns the field selected by this subquery, or null for aggregate-only subqueries.
     *
     * @return the selected field, or null
     */
    public FieldInfo<?> getSelectField() {
        return selectField;
    }

    /**
     * Returns the entity class this subquery queries from.
     *
     * @return the from-entity class
     */
    public Class<?> getFromEntity() {
        return fromEntity;
    }

    /**
     * Returns the aggregate type applied to this subquery, or null if none.
     *
     * @return the aggregate type, or null
     */
    public AggregateType getAggregateType() {
        return aggregateType;
    }

    /**
     * Returns the list of WHERE conditions applied to this subquery.
     *
     * @return the list of conditions
     */
    public List<Specification<?>> getConditions() {
        return conditions;
    }

    /**
     * Returns the list of correlations linking this subquery to the main query.
     *
     * @return the list of correlations
     */
    public List<Correlation> getCorrelations() {
        return correlations;
    }

    // ===== Inner Class =====

    /**
     * Represents a correlation between a subquery field and a main query field.
     *
     * @param subField  the field in the subquery
     * @param mainField the corresponding field in the main query
     */
    public record Correlation(FieldInfo<?> subField, FieldInfo<?> mainField) {}
}