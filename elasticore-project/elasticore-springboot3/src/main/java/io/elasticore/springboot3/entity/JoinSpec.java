package io.elasticore.springboot3.entity;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import io.elasticore.springboot3.mapper.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

/**
 * Fluent builder for JPA criteria queries with join, subquery, and projection support.
 *
 * @param <T> the root entity type for the query
 */
public class JoinSpec<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    /** Root entity class for the query. */
    private final Class<T> rootClass;
    /** Regular (non-fetch) join definitions. */
    private final List<JoinDef> joins = new ArrayList<>();
    /** Paths for which fetch joins should be applied. */
    private final Set<String> fetchJoinPaths = new HashSet<>();
    /** WHERE conditions accumulated for the query. */
    private final List<Specification<T>> conditions = new ArrayList<>();
    /** Cross-join definitions (Cartesian product joins). */
    private final List<CrossJoinDef> crossJoins = new ArrayList<>();

    /** Projection field definitions for SELECT clauses. */
    private final List<SelectDef> selectDefs = new ArrayList<>();

    /** Subquery conditions added via subquery(). */
    private final List<SubQueryDef> subQueryConditions = new ArrayList<>();
    /** EXISTS/NOT EXISTS conditions. */
    private final List<ExistsDef> existsConditions = new ArrayList<>();
    /** Scalar subquery projections added via selectSubquery(). */
    private final List<ScalarSubQueryDef> scalarSubQueries = new ArrayList<>();

    /** The current cross-join being built (used by link()). */
    private CrossJoinDef currentCrossJoin;

    /**
     * Constructs a JoinSpec for the given root entity class.
     *
     * @param rootClass the root entity class for the query
     */
    public JoinSpec(Class<T> rootClass) {
        this.rootClass = rootClass;
    }


    /** Whether to apply DISTINCT to the query. */
    private boolean distinct = false;
    /** ORDER BY clauses accumulated for the query. */
    private final List<OrderDef> orders = new ArrayList<>();
    /** Simple field projections (not aliased). */
    private final List<FieldInfo<?>> selectFields = new ArrayList<>();

    // ===== distinct =====
    /**
     * Enables DISTINCT on the query results.
     *
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> distinct() {
        this.distinct = true;
        return this;
    }

    // ===== orderBy =====
    /**
     * Adds an ORDER BY clause for the given field.
     *
     * @param field     the field to order by
     * @param ascending true for ascending order, false for descending
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> orderBy(FieldInfo<?> field, boolean ascending) {
        orders.add(new OrderDef(field, ascending));
        return this;
    }

    /**
     * Adds an ascending ORDER BY clause for the given field.
     *
     * @param field the field to order by ascending
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> orderByAsc(FieldInfo<?> field) {
        return orderBy(field, true);
    }

    /**
     * Adds a descending ORDER BY clause for the given field.
     *
     * @param field the field to order by descending
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> orderByDesc(FieldInfo<?> field) {
        return orderBy(field, false);
    }

    // ===== select (Projection) =====
    /**
     * Adds fields to the SELECT projection.
     *
     * @param fields one or more fields to include in the projection
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> select(FieldInfo<?>... fields) {
        for (FieldInfo<?> field : fields) {
            selectDefs.add(new SelectDef(field, field.getName()));
        }
        return this;
    }

    /**
     * Adds a field to the SELECT projection with a custom alias.
     *
     * @param field the field to project
     * @param alias the alias for the field in query results
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> select(FieldInfo<?> field, String alias) {
        selectDefs.add(new SelectDef(field, alias));
        return this;
    }


    // ===== Subquery =====

    /**
     * Adds a WHERE subquery condition.
     *
     * @param field    the main query field to compare
     * @param op       the comparison operator
     * @param subQuery the subquery to compare against
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> subquery(FieldInfo<?> field, SubQuery.Op op, SubQuery<?> subQuery) {
        this.subQueryConditions.add(new SubQueryDef(field, op, subQuery));
        return this;
    }

    /**
     * Adds an EXISTS subquery condition.
     *
     * @param subQuery the subquery to check existence for
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> exists(SubQuery<?> subQuery) {
        this.existsConditions.add(new ExistsDef(subQuery, false));
        return this;
    }

    /**
     * Adds a NOT EXISTS subquery condition.
     *
     * @param subQuery the subquery to check non-existence for
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> notExists(SubQuery<?> subQuery) {
        this.existsConditions.add(new ExistsDef(subQuery, true));
        return this;
    }

    /**
     * Adds a scalar subquery to the SELECT projection with the given alias.
     *
     * @param alias    the alias for the subquery result column
     * @param subQuery the subquery to evaluate as a scalar value
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> selectSubquery(String alias, SubQuery<?> subQuery) {
        this.scalarSubQueries.add(new ScalarSubQueryDef(alias, subQuery));
        return this;
    }


    // ===== exists =====
    /**
     * Adds an EXISTS condition using a correlated subquery built from the given entity and condition.
     *
     * @param subEntity the entity class for the subquery FROM clause
     * @param condition the condition linking the subquery to the main query
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> exists(Class<?> subEntity, SubQueryCondition condition) {
        conditions.add((root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<?> subRoot = subquery.from(subEntity);
            subquery.select(cb.literal(1));  // SELECT 1
            subquery.where(condition.toPredicate(subRoot, root, cb));
            return cb.exists(subquery);
        });
        return this;
    }

    /**
     * Adds a NOT EXISTS condition using a correlated subquery built from the given entity and condition.
     *
     * @param subEntity the entity class for the subquery FROM clause
     * @param condition the condition linking the subquery to the main query
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> notExists(Class<?> subEntity, SubQueryCondition condition) {
        conditions.add((root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<?> subRoot = subquery.from(subEntity);
            subquery.select(cb.literal(1));  // SELECT 1
            subquery.where(condition.toPredicate(subRoot, root, cb));
            return cb.not(cb.exists(subquery));
        });
        return this;
    }






    // ===== JOIN =====

    /**
     * Adds a LEFT JOIN for the given field.
     *
     * @param field the association field to left-join
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> leftJoin(FieldInfo<?> field) {
        joins.add(new JoinDef(field, JoinType.LEFT));
        return this;
    }

    /**
     * Adds an INNER JOIN for the given field.
     *
     * @param field the association field to inner-join
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> join(FieldInfo<?> field) {
        joins.add(new JoinDef(field, JoinType.INNER));
        return this;
    }

    /**
     * Adds a fetch join for the given field, eagerly loading the association.
     *
     * @param field the association field to fetch-join
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> fetchJoin(FieldInfo<?> field) {
        String path = resolvePath(field);
        fetchJoinPaths.add(path);
        if (joins.stream().noneMatch(j -> resolvePath(j.field).equals(path))) {
            joins.add(new JoinDef(field, JoinType.LEFT));
        }
        return this;
    }

    // ===== CROSS JOIN =====

    /**
     * Adds a CROSS JOIN for the given entity class with an explicit alias.
     *
     * @param <X>         the cross-joined entity type
     * @param entityClass the entity class to cross-join
     * @param alias       the alias to reference this cross-joined root
     * @return this JoinSpec for chaining
     */
    public <X> JoinSpec<T> crossJoin(Class<X> entityClass, String alias) {
        currentCrossJoin = new CrossJoinDef(entityClass, alias);
        crossJoins.add(currentCrossJoin);
        return this;
    }

    /**
     * Adds a CROSS JOIN for the given entity class using the simple class name as alias.
     *
     * @param <X>         the cross-joined entity type
     * @param entityClass the entity class to cross-join
     * @return this JoinSpec for chaining
     */
    public <X> JoinSpec<T> crossJoin(Class<X> entityClass) {
        return crossJoin(entityClass, entityClass.getSimpleName());
    }

    /**
     * Creates a link between a main query field and a cross-joined entity field.
     *
     * @param sourceField the field from the main query root
     * @param targetField the field from the cross-joined entity
     * @return a LinkBuilder for configuring the join condition
     */
    public LinkBuilder link(FieldInfo<?> sourceField, FieldInfo<?> targetField) {
        // Find matching cross join by targetField's entity type
        Class<?> targetEntityType = targetField.getEntityType();
        CrossJoinDef matchingCrossJoin = null;

        for (CrossJoinDef cj : crossJoins) {
            if (cj.entityClass.equals(targetEntityType)) {
                matchingCrossJoin = cj;
                break;
            }
        }

        if (matchingCrossJoin == null) {
            throw new IllegalStateException(
                    "No crossJoin found for entity: " + targetEntityType.getSimpleName() +
                            ". Call crossJoin(" + targetEntityType.getSimpleName() + ".class) first."
            );
        }

        return new LinkBuilder(sourceField, targetField, matchingCrossJoin);
    }

    // ===== WHERE =====

    /**
     * Adds one or more WHERE conditions to the query.
     *
     * @param specs one or more Specification conditions to apply
     * @return this JoinSpec for chaining
     */
    @SafeVarargs
    public final JoinSpec<T> where(Specification<T>... specs) {
        for (Specification<T> spec : specs) {
            if (spec != null) conditions.add(spec);
        }
        return this;
    }

    /**
     * Adds an AND condition to the query.
     *
     * @param spec the condition to AND with existing conditions
     * @return this JoinSpec for chaining
     */
    @Override
    public JoinSpec<T> and(Specification<T> spec) {
        if (spec != null) conditions.add(spec);
        return this;
    }

    /**
     * ORs the given condition with the last added condition.
     *
     * @param spec the condition to OR with the last condition
     * @return this JoinSpec for chaining
     */
    public JoinSpec<T> or(Specification<T> spec) {
        if (spec != null && !conditions.isEmpty()) {
            Specification<T> last = conditions.remove(conditions.size() - 1);
            conditions.add(last.or(spec));
        }
        return this;
    }

    /**
     * Returns the list of subquery conditions added to this spec.
     *
     * @return the subquery condition list
     */
    public List<SubQueryDef> getSubQueryConditions() {
        return this.subQueryConditions;
    }

    // ===== Build Predicate =====

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // Two maps: by path and by entity type
        Map<String, Join<?, ?>> joinByPath = new LinkedHashMap<>();
        Map<Class<?>, Join<?, ?>> joinByType = new HashMap<>();
        Map<String, Root<?>> rootMap = new HashMap<>();
        Map<Class<?>, Root<?>> crossRootByType = new HashMap<>();

        // 1. Create joins (order matters)
        for (JoinDef jd : joins) {
            createJoin(root, jd, joinByPath, joinByType);
        }

        // 2. Create cross joins and link conditions
        List<Predicate> linkPredicates = new ArrayList<>();
        for (CrossJoinDef cj : crossJoins) {
            Root<?> crossRoot = query.from(cj.entityClass);
            rootMap.put(cj.alias, crossRoot);
            crossRootByType.put(cj.entityClass, crossRoot);

            for (LinkDef link : cj.links) {
                Path<?> sourcePath = resolveFieldPath(root, joinByPath, joinByType, crossRootByType, link.sourceField);
                Path<?> targetPath = crossRoot.get(link.targetField.getName());

                Predicate linkPred;
                if (link.allowNull) {
                    linkPred = cb.or(cb.isNull(sourcePath), cb.equal(sourcePath, targetPath));
                } else {
                    linkPred = cb.equal(sourcePath, targetPath);
                }
                linkPredicates.add(linkPred);
            }
        }

        // 3. Apply fetch joins
        applyFetchJoins(root, query);

        // 4. Combine all predicates
        Predicate result = cb.conjunction();

        for (Predicate p : linkPredicates) {
            result = cb.and(result, p);
        }

        for (Specification<T> spec : conditions) {
            Predicate p = spec.toPredicate(root, query, cb);
            if (p != null) result = cb.and(result, p);
        }

        // ===== ADD: Subquery conditions 처리 =====
        for (SubQueryDef sqDef : subQueryConditions) {
            Predicate p = buildSubQueryPredicate(sqDef, root, query, cb, joinByPath, joinByType);
            if (p != null) result = cb.and(result, p);
        }

        // ===== ADD: EXISTS conditions 처리 =====
        for (ExistsDef existsDef : existsConditions) {
            Predicate p = buildExistsPredicate(existsDef, root, query, cb, joinByPath, joinByType);
            if (p != null) result = cb.and(result, p);
        }

        // Apply distinct
        if (distinct) {
            query.distinct(true);
        }

        // Apply orderBy
        if (!orders.isEmpty() && query.getResultType() != Long.class) {
            List<Order> jpaOrders = new ArrayList<>();
            for (OrderDef od : orders) {
                Path<?> path = resolveFieldPath(root, joinByPath, joinByType, crossRootByType, od.field);
                jpaOrders.add(od.ascending ? cb.asc(path) : cb.desc(path));
            }
            query.orderBy(jpaOrders);
        }
        return result;
    }

    private void createJoin(From<?, ?> root, JoinDef jd,
                            Map<String, Join<?, ?>> joinByPath,
                            Map<Class<?>, Join<?, ?>> joinByType) {
        String path = resolvePath(jd.field);
        if (joinByPath.containsKey(path)) return;

        From<?, ?> parent = root;
        FieldInfo<?> parentField = jd.field.getParentFieldInfo();

        if (parentField != null) {
            // Case 1: Has explicit parent field
            String parentPath = resolvePath(parentField);
            if (!joinByPath.containsKey(parentPath)) {
                createJoin(root, new JoinDef(parentField, jd.joinType), joinByPath, joinByType);
            }
            parent = joinByPath.get(parentPath);
        } else {
            // Case 2: No parent field - find existing join by entity type
            Class<?> declaringClass = jd.field.getEntityType();
            if (!declaringClass.equals(rootClass)) {
                Join<?, ?> existingJoin = joinByType.get(declaringClass);
                if (existingJoin != null) {
                    parent = existingJoin;
                } else {
                    throw new IllegalStateException(
                            "No join found for entity: " + declaringClass.getSimpleName() +
                                    ". Add join for " + declaringClass.getSimpleName() +
                                    " before joining " + jd.field.getName()
                    );
                }
            }
        }

        Join<?, ?> join = parent.join(jd.field.getName(), jd.joinType);
        joinByPath.put(path, join);

        // Store by target entity type (type field contains the target entity class)
        Class<?> targetType = jd.field.getType();
        if (targetType != null) {
            joinByType.put(targetType, join);
        }
    }

    private Path<?> resolveFieldPath(Root<T> root,
                                     Map<String, Join<?, ?>> joinByPath,
                                     Map<Class<?>, Join<?, ?>> joinByType,
                                     FieldInfo<?> field) {
        return resolveFieldPath(root, joinByPath, joinByType, null, field);
    }

    private Path<?> resolveFieldPath(Root<T> root,
                                     Map<String, Join<?, ?>> joinByPath,
                                     Map<Class<?>, Join<?, ?>> joinByType,
                                     Map<Class<?>, Root<?>> crossRootByType,
                                     FieldInfo<?> field) {
        FieldInfo<?> parentField = field.getParentFieldInfo();

        if (parentField == null) {
            // No parent - check if field's declaring entity has a join
            Class<?> declaringClass = field.getEntityType();
            if (!declaringClass.equals(rootClass)) {
                // First check joinByType (for regular joins)
                Join<?, ?> existingJoin = joinByType.get(declaringClass);
                if (existingJoin != null) {
                    return existingJoin.get(field.getName());
                }
                // Then check crossRootByType (for cross joins)
                if (crossRootByType != null) {
                    Root<?> crossRoot = crossRootByType.get(declaringClass);
                    if (crossRoot != null) {
                        return crossRoot.get(field.getName());
                    }
                }
                throw new IllegalStateException(
                        "No join found for entity: " + declaringClass.getSimpleName() +
                                ". Cannot resolve field: " + field.getName()
                );
            }
            return root.get(field.getName());
        }

        // Has parent field
        String parentPath = resolvePath(parentField);
        Join<?, ?> parentJoin = joinByPath.get(parentPath);
        if (parentJoin != null) {
            return parentJoin.get(field.getName());
        }

        // Try by entity type
        Class<?> parentEntityType = parentField.getType();
        Join<?, ?> joinByEntityType = joinByType.get(parentEntityType);
        if (joinByEntityType != null) {
            return joinByEntityType.get(field.getName());
        }

        // Try cross root by entity type
        if (crossRootByType != null) {
            Root<?> crossRoot = crossRootByType.get(parentEntityType);
            if (crossRoot != null) {
                return crossRoot.get(field.getName());
            }
        }

        return root.get(parentPath).get(field.getName());
    }

    private void applyFetchJoins(Root<T> root, CriteriaQuery<?> query) {
        if (query.getResultType() == Long.class || query.getResultType() == long.class) {
            return;
        }
        for (String path : fetchJoinPaths) {
            String[] parts = path.split("\\.");
            FetchParent<?, ?> fetch = root;
            for (String part : parts) {
                fetch = fetch.fetch(part, JoinType.LEFT);
            }
        }
    }

    private String resolvePath(FieldInfo<?> field) {
        if (field.getParentFieldInfo() == null) {
            return field.getName();
        }
        return resolvePath(field.getParentFieldInfo()) + "." + field.getName();
    }

    // ===== Inner Classes =====

    private record JoinDef(FieldInfo<?> field, JoinType joinType) {}

    private static class CrossJoinDef {
        final Class<?> entityClass;
        final String alias;
        final List<LinkDef> links = new ArrayList<>();

        CrossJoinDef(Class<?> entityClass, String alias) {
            this.entityClass = entityClass;
            this.alias = alias;
        }
    }

    // ===== Inner Classes =====
    private record OrderDef(FieldInfo<?> field, boolean ascending) {}

    /**
     * Functional interface for building a correlated subquery predicate.
     */
    @FunctionalInterface
    public interface SubQueryCondition {
        /**
         * Builds a predicate correlating the subquery root to the main query root.
         *
         * @param subRoot  the subquery root
         * @param mainRoot the main query root
         * @param cb       the CriteriaBuilder
         * @return the correlated predicate
         */
        Predicate toPredicate(Root<?> subRoot, Root<?> mainRoot, CriteriaBuilder cb);
    }

    private record LinkDef(FieldInfo<?> sourceField, FieldInfo<?> targetField, boolean allowNull) {}

    /**
     * Builder for configuring the join condition between the main query and a cross-joined entity.
     */
    public class LinkBuilder {
        private final FieldInfo<?> sourceField;
        private final FieldInfo<?> targetField;
        private final CrossJoinDef crossJoin;

        LinkBuilder(FieldInfo<?> sourceField, FieldInfo<?> targetField, CrossJoinDef crossJoin) {
            this.sourceField = sourceField;
            this.targetField = targetField;
            this.crossJoin = crossJoin;
            crossJoin.links.add(new LinkDef(sourceField, targetField, false));
        }

        /**
         * Configures the link to allow null values on the source side (LEFT JOIN semantics).
         *
         * @return the parent JoinSpec for chaining
         */
        public JoinSpec<T> allowNull() {
            crossJoin.links.remove(crossJoin.links.size() - 1);
            crossJoin.links.add(new LinkDef(sourceField, targetField, true));
            return JoinSpec.this;
        }

        /**
         * Configures the link to require exact equality (INNER JOIN semantics).
         *
         * @return the parent JoinSpec for chaining
         */
        public JoinSpec<T> strict() {
            return JoinSpec.this;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Subquery<?> buildScalarSubquery(SubQuery<?> sq, Root<T> root, CriteriaQuery<?> query,
                                            CriteriaBuilder cb, Map<String, Join<?, ?>> joinByPath,
                                            Map<Class<?>, Join<?, ?>> joinByType) {
        Class<?> subEntity = sq.getFromEntity();
        Subquery subquery;
        Root<?> subRoot;
        Expression<?> selectExpr;

        if (sq.getAggregateType() != null) {
            subquery = query.subquery(Number.class);
            subRoot = subquery.from(subEntity);
            selectExpr = buildAggregateExpression(sq, subRoot, cb);
        } else {
            // Use field's actual type for Hibernate 6.x compatibility
            Class<?> selectFieldType = sq.getSelectField().getType();
            subquery = query.subquery(selectFieldType);
            subRoot = subquery.from(subEntity);
            selectExpr = subRoot.get(sq.getSelectField().getName());
        }

        subquery.select(selectExpr);

        // Build WHERE conditions
        List<Predicate> subPredicates = new ArrayList<>();

        // Correlate conditions
        for (SubQuery.Correlation corr : sq.getCorrelations()) {
            Path<?> mainPath = resolveFieldPath(root, joinByPath, joinByType, corr.mainField());
            subPredicates.add(cb.equal(subRoot.get(corr.subField().getName()), mainPath));
        }

        // Where conditions
        for (Specification<?> spec : sq.getConditions()) {
            Predicate p = ((Specification<Object>) spec).toPredicate((Root<Object>) subRoot, query, cb);
            if (p != null) subPredicates.add(p);
        }

        if (!subPredicates.isEmpty()) {
            subquery.where(cb.and(subPredicates.toArray(new Predicate[0])));
        }

        return subquery;
    }

    // ===== Case 1: findAllAsMap =====
    /**
     * Executes the query and returns results as a list of alias-to-value maps.
     *
     * @param em the EntityManager to use for query execution
     * @return list of maps where keys are field aliases and values are field values
     */
    public List<Map<String, Object>> findAllAsMap(EntityManager em) {
        if (selectDefs.isEmpty() && scalarSubQueries.isEmpty()) {
            throw new IllegalStateException("No fields selected. Call select() or selectSubquery() before findAllAsMap()");
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(rootClass);

        // Build joins
        Map<String, Join<?, ?>> joinByPath = new LinkedHashMap<>();
        Map<Class<?>, Join<?, ?>> joinByType = new HashMap<>();
        for (JoinDef jd : joins) {
            createJoin(root, jd, joinByPath, joinByType);
        }

        // Apply cross joins and get crossRootByType
        Map<Class<?>, Root<?>> crossRootByType = applyCrossJoins(root, query, cb, joinByPath, joinByType);

        // Build selections
        List<Selection<?>> selections = new ArrayList<>();
        for (SelectDef sd : selectDefs) {
            Path<?> path = resolveFieldPath(root, joinByPath, joinByType, crossRootByType, sd.field());
            selections.add(path.alias(sd.alias()));
        }

        // ===== Append: Scalar subquery selections =====
        for (ScalarSubQueryDef ssd : scalarSubQueries) {
            Subquery<?> subquery = buildScalarSubquery(ssd.subQuery(), root, query, cb, joinByPath, joinByType);
            selections.add(subquery.alias(ssd.alias()));
        }

        query.multiselect(selections);

        // Apply where
        Predicate predicate = buildPredicate(root, query, cb, joinByPath, joinByType, crossRootByType);
        if (predicate != null) {
            query.where(predicate);
        }

        // Apply distinct
        if (distinct) {
            query.distinct(true);
        }

        // Apply orderBy
        applyOrderBy(root, query, cb, joinByPath, joinByType, crossRootByType);

        // Execute query
        List<Tuple> tuples = em.createQuery(query).getResultList();

        // Convert to Map
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> allAliases = new ArrayList<>();
        for (SelectDef sd : selectDefs) {
            allAliases.add(sd.alias());
        }
        for (ScalarSubQueryDef ssd : scalarSubQueries) {
            allAliases.add(ssd.alias());
        }

        for (Tuple tuple : tuples) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String alias : allAliases) {
                map.put(alias, tuple.get(alias));
            }
            result.add(map);
        }
        return result;
    }

    // ===== Case 2: findAll with DTO =====
    /**
     * Executes the query and maps results to the given DTO class.
     *
     * @param <D>      the DTO type
     * @param em       the EntityManager to use for query execution
     * @param dtoClass the DTO class to map results into
     * @return list of DTO instances populated from the query results
     */
    public <D> List<D> findAll(EntityManager em, Class<D> dtoClass) {
        List<Map<String, Object>> maps = findAllAsMap(em);
        return Mapper.of(maps, dtoClass).toList();
    }

    // ===== Case 3: findAll with DTO and Pageable =====
    /**
     * Executes the query with pagination and maps results to the given DTO class.
     *
     * @param <D>      the DTO type
     * @param em       the EntityManager to use for query execution
     * @param dtoClass the DTO class to map results into
     * @param pageable the pagination and sorting parameters
     * @return a Page of DTO instances
     */
    public <D> Page<D> findAll(EntityManager em, Class<D> dtoClass, Pageable pageable) {
        // Count query
        long total = count(em);

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Data query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(rootClass);

        // Build joins
        Map<String, Join<?, ?>> joinByPath = new LinkedHashMap<>();
        Map<Class<?>, Join<?, ?>> joinByType = new HashMap<>();
        for (JoinDef jd : joins) {
            createJoin(root, jd, joinByPath, joinByType);
        }

        // Apply cross joins and get crossRootByType
        Map<Class<?>, Root<?>> crossRootByType = applyCrossJoins(root, query, cb, joinByPath, joinByType);

        // Build selections
        List<Selection<?>> selections = new ArrayList<>();
        for (SelectDef sd : selectDefs) {
            Path<?> path = resolveFieldPath(root, joinByPath, joinByType, crossRootByType, sd.field());
            selections.add(path.alias(sd.alias()));
        }
        query.multiselect(selections);

        // Apply where
        Predicate predicate = buildPredicate(root, query, cb, joinByPath, joinByType, crossRootByType);
        if (predicate != null) {
            query.where(predicate);
        }

        // Apply distinct
        if (distinct) {
            query.distinct(true);
        }

        // Apply orderBy
        applyOrderBy(root, query, cb, joinByPath, joinByType, crossRootByType);

        // Apply paging orderBy if not already ordered
        if (orders.isEmpty() && pageable.getSort().isSorted()) {
            List<Order> jpaOrders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                Path<?> path = root.get(order.getProperty());
                jpaOrders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
            });
            query.orderBy(jpaOrders);
        }

        // Execute with paging
        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Tuple> tuples = typedQuery.getResultList();

        // Convert to Map then DTO
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (SelectDef sd : selectDefs) {
                map.put(sd.alias(), tuple.get(sd.alias()));
            }
            maps.add(map);
        }

        List<D> content = Mapper.of(maps, dtoClass).toList();
        return new PageImpl<>(content, pageable, total);
    }

    // ===== count =====
    /**
     * Executes a COUNT query using the current conditions and joins.
     *
     * @param em the EntityManager to use for query execution
     * @return the number of matching rows
     */
    public long count(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(rootClass);

        // Build joins
        Map<String, Join<?, ?>> joinByPath = new LinkedHashMap<>();
        Map<Class<?>, Join<?, ?>> joinByType = new HashMap<>();
        for (JoinDef jd : joins) {
            createJoin(root, jd, joinByPath, joinByType);
        }

        // Apply cross joins and get crossRootByType
        Map<Class<?>, Root<?>> crossRootByType = applyCrossJoins(root, query, cb, joinByPath, joinByType);

        // Select count
        if (distinct) {
            query.select(cb.countDistinct(root));
        } else {
            query.select(cb.count(root));
        }

        // Apply where
        Predicate predicate = buildPredicate(root, query, cb, joinByPath, joinByType, crossRootByType);
        if (predicate != null) {
            query.where(predicate);
        }

        return em.createQuery(query).getSingleResult();
    }

// ===== Helper Methods =====

    private Map<Class<?>, Root<?>> applyCrossJoins(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                 Map<String, Join<?, ?>> joinByPath, Map<Class<?>, Join<?, ?>> joinByType) {
        Map<Class<?>, Root<?>> crossRootByType = new HashMap<>();
        for (CrossJoinDef cj : crossJoins) {
            Root<?> crossRoot = query.from(cj.entityClass);
            crossRootByType.put(cj.entityClass, crossRoot);
        }
        return crossRootByType;
    }

    private Predicate buildPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                     Map<String, Join<?, ?>> joinByPath, Map<Class<?>, Join<?, ?>> joinByType,
                                     Map<Class<?>, Root<?>> crossRootByType) {
        List<Predicate> predicates = new ArrayList<>();

        // Cross join link conditions
        int crossJoinIndex = 0;
        Set<Root<?>> roots = query.getRoots();
        List<Root<?>> crossRoots = new ArrayList<>();
        for (Root<?> r : roots) {
            if (!r.getJavaType().equals(rootClass)) {
                crossRoots.add(r);
            }
        }

        for (CrossJoinDef cj : crossJoins) {
            Root<?> crossRoot = crossRoots.size() > crossJoinIndex ? crossRoots.get(crossJoinIndex) : null;
            if (crossRoot != null) {
                for (LinkDef link : cj.links) {
                    Path<?> sourcePath = resolveFieldPath(root, joinByPath, joinByType, crossRootByType, link.sourceField());
                    Path<?> targetPath = crossRoot.get(link.targetField().getName());

                    Predicate linkPred;
                    if (link.allowNull()) {
                        linkPred = cb.or(cb.isNull(sourcePath), cb.equal(sourcePath, targetPath));
                    } else {
                        linkPred = cb.equal(sourcePath, targetPath);
                    }
                    predicates.add(linkPred);
                }
            }
            crossJoinIndex++;
        }

        // Where conditions
        for (Specification<T> spec : conditions) {
            Predicate p = spec.toPredicate(root, query, cb);
            if (p != null) predicates.add(p);
        }

        // ===== : Subquery conditions =====
        for (SubQueryDef sqDef : subQueryConditions) {
            Predicate p = buildSubQueryPredicate(sqDef, root, query, cb, joinByPath, joinByType);
            if (p != null) predicates.add(p);
        }

        // ===== : EXISTS conditions =====
        for (ExistsDef existsDef : existsConditions) {
            Predicate p = buildExistsPredicate(existsDef, root, query, cb, joinByPath, joinByType);
            if (p != null) predicates.add(p);
        }

        if (predicates.isEmpty()) {
            return null;
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }


    // ===== Subquery Predicate Builders =====

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildSubQueryPredicate(SubQueryDef sqDef, Root<T> root, CriteriaQuery<?> query,
                                             CriteriaBuilder cb, Map<String, Join<?, ?>> joinByPath,
                                             Map<Class<?>, Join<?, ?>> joinByType) {
        SubQuery<?> sq = sqDef.subQuery();
        Class<?> subEntity = sq.getFromEntity();

        // Create subquery
        Subquery<?> subquery;
        Root<?> subRoot;
        Expression<?> selectExpr;

        if (sq.getAggregateType() != null) {
            // Aggregate function
            subquery = query.subquery(Number.class);
            subRoot = subquery.from(subEntity);
            selectExpr = buildAggregateExpression(sq, subRoot, cb);
        } else {
            // Normal SELECT - use field's actual type for Hibernate 6.x compatibility
            Class<?> selectFieldType = sq.getSelectField().getType();
            subquery = query.subquery(selectFieldType);
            subRoot = subquery.from(subEntity);
            selectExpr = subRoot.get(sq.getSelectField().getName());
        }

        ((Subquery) subquery).select(selectExpr);

        // Build WHERE conditions
        List<Predicate> subPredicates = new ArrayList<>();

        // Correlate conditions
        for (SubQuery.Correlation corr : sq.getCorrelations()) {
            Path<?> mainPath = resolveFieldPath(root, joinByPath, joinByType, corr.mainField());
            subPredicates.add(cb.equal(subRoot.get(corr.subField().getName()), mainPath));
        }

        // Where conditions
        for (Specification<?> spec : sq.getConditions()) {
            Predicate p = ((Specification<Object>) spec).toPredicate((Root<Object>) subRoot, query, cb);
            if (p != null) subPredicates.add(p);
        }

        if (!subPredicates.isEmpty()) {
            subquery.where(cb.and(subPredicates.toArray(new Predicate[0])));
        }

        // Build comparison predicate
        Path<?> mainField = resolveFieldPath(root, joinByPath, joinByType, sqDef.field());

        return switch (sqDef.op()) {
            case IN -> mainField.in(subquery);
            case NOT_IN -> cb.not(mainField.in(subquery));
            case EQ -> cb.equal(mainField, subquery);
            case NE -> cb.notEqual(mainField, subquery);
            case GT -> cb.gt((Expression<Number>) mainField, (Subquery<Number>) subquery);
            case GTE -> cb.ge((Expression<Number>) mainField, (Subquery<Number>) subquery);
            case LT -> cb.lt((Expression<Number>) mainField, (Subquery<Number>) subquery);
            case LTE -> cb.le((Expression<Number>) mainField, (Subquery<Number>) subquery);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildExistsPredicate(ExistsDef existsDef, Root<T> root, CriteriaQuery<?> query,
                                           CriteriaBuilder cb, Map<String, Join<?, ?>> joinByPath,
                                           Map<Class<?>, Join<?, ?>> joinByType) {
        SubQuery<?> sq = existsDef.subQuery();

        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<?> subRoot = subquery.from(sq.getFromEntity());
        subquery.select(cb.literal(1));

        // Build WHERE conditions
        List<Predicate> subPredicates = new ArrayList<>();

        // Correlate conditions
        for (SubQuery.Correlation corr : sq.getCorrelations()) {
            Path<?> mainPath = resolveFieldPath(root, joinByPath, joinByType, corr.mainField());
            subPredicates.add(cb.equal(subRoot.get(corr.subField().getName()), mainPath));
        }

        // Where conditions
        for (Specification<?> spec : sq.getConditions()) {
            Predicate p = ((Specification<Object>) spec).toPredicate((Root<Object>) subRoot, query, cb);
            if (p != null) subPredicates.add(p);
        }

        if (!subPredicates.isEmpty()) {
            subquery.where(cb.and(subPredicates.toArray(new Predicate[0])));
        }

        return existsDef.negated() ? cb.not(cb.exists(subquery)) : cb.exists(subquery);
    }

    private Expression<?> buildAggregateExpression(SubQuery<?> sq, Root<?> subRoot, CriteriaBuilder cb) {
        return switch (sq.getAggregateType()) {
            case COUNT -> cb.count(subRoot);
            case SUM -> cb.sum(subRoot.get(sq.getSelectField().getName()));
            case AVG -> cb.avg(subRoot.get(sq.getSelectField().getName()));
            case MAX -> cb.max(subRoot.get(sq.getSelectField().getName()));
            case MIN -> cb.min(subRoot.get(sq.getSelectField().getName()));
        };
    }

    private void applyOrderBy(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                              Map<String, Join<?, ?>> joinByPath, Map<Class<?>, Join<?, ?>> joinByType,
                              Map<Class<?>, Root<?>> crossRootByType) {
        if (!orders.isEmpty()) {
            List<Order> jpaOrders = new ArrayList<>();
            for (OrderDef od : orders) {
                Path<?> path = resolveFieldPath(root, joinByPath, joinByType, crossRootByType, od.field());
                jpaOrders.add(od.ascending() ? cb.asc(path) : cb.desc(path));
            }
            query.orderBy(jpaOrders);
        }
    }

    // ===== Inner Class =====
    /**
     * Represents a projected field and its alias in a SELECT clause.
     *
     * @param field the field to project
     * @param alias the alias for the projected column
     */
    public record SelectDef(FieldInfo<?> field, String alias) {}

    // ===== Subquery Inner Classes =====

    private record SubQueryDef(FieldInfo<?> field, SubQuery.Op op, SubQuery<?> subQuery) {}

    private record ExistsDef(SubQuery<?> subQuery, boolean negated) {}

    private record ScalarSubQueryDef(String alias, SubQuery<?> subQuery) {}
}