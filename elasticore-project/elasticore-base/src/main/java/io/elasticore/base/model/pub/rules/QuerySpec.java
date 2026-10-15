package io.elasticore.base.model.pub.rules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One method of repository.yml, as a rule reads it.
 *
 *   repository:
 *     SalesOrder:
 *       methods:
 *         - id: summarizeByPartner          name()       "summarizeByPartner"
 *           params:                         params()     {status=OrderStatus}
 *             status: OrderStatus
 *           return: List&lt;PartnerSalesDTO&gt;  returnType() "List&lt;PartnerSalesDTO&gt;"
 *           query: "SELECT ... "            sql()        "SELECT ..."
 *
 * A method with no query is one Spring Data derives from its name; sql() is
 * null for it.
 */
public final class QuerySpec {

    private final String name;
    private final String sql;
    private final boolean nativeQuery;
    private final boolean select;
    private final String returnType;
    private final Map<String, String> params;

    QuerySpec(String name, String sql, boolean nativeQuery, boolean select,
              String returnType, Map<String, String> params) {
        this.name = name;
        this.sql = sql;
        this.nativeQuery = nativeQuery;
        this.select = select;
        this.returnType = returnType;
        this.params = Collections.unmodifiableMap(new LinkedHashMap<String, String>(params));
    }

    static QuerySpec of(io.elasticore.base.model.repo.Method method) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        io.elasticore.base.model.core.Items<io.elasticore.base.model.entity.Field> declared = method.getParams();
        if (declared != null) {
            for (io.elasticore.base.model.entity.Field f : declared.getItemList()) {
                params.put(f.getName(), f.getTypeInfo().getDefaultTypeName());
            }
        }
        boolean select = method.getQueryInfo() == null || method.getQueryInfo().isSelectQuery();
        return new QuerySpec(method.getName(), method.getQuery(), method.isNative(), select,
                method.getReturnType(), params);
    }

    /** The method's name in the repository interface. */
    public String name() {
        return name;
    }

    /** The query as written in repository.yml, or null when Spring Data derives it from the name. */
    public String sql() {
        return sql;
    }

    /** Whether the query is SQL for the database rather than JPQL. */
    public boolean isNative() {
        return nativeQuery;
    }

    /** Whether the query only reads. A method without a query always does. */
    public boolean isSelect() {
        return select;
    }

    /** The return type as written in repository.yml. */
    public String returnType() {
        return returnType;
    }

    /** Parameter names to their types, in declaration order. */
    public Map<String, String> params() {
        return params;
    }

    @Override
    public String toString() {
        return name;
    }
}
