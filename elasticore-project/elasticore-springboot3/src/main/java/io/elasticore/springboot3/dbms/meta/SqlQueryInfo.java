package io.elasticore.springboot3.dbms.meta;

/**
 * Data class that holds metadata and configuration for SQL queries.
 * This class encapsulates information about SQL queries including the query text,
 * execution type, source file information, and caching metadata.
 * 
 * <p>SqlQueryInfo is used by the DBMS execution framework to:</p>
 * <ul>
 *   <li>Store parsed SQL query definitions from YAML configuration files</li>
 *   <li>Track file modification times for hot-reloading</li>
 *   <li>Distinguish between native SQL and JPQL queries</li>
 *   <li>Support query references and inheritance</li>
 * </ul>
 * 
 * @author ElastiCore Framework
 * @since 1.0
 */
public class SqlQueryInfo {

    private String sql;

    private String nativeQuery;

    private String id;

    private String sqlSourceFilePath;

    private long lastModifiedTime;

    private String ref;

    /**
     * Returns the SQL query text.
     * 
     * @return the SQL query string
     */
    public String getSql() {
        return sql;
    }

    /**
     * Sets the SQL query text.
     * 
     * @param sql the SQL query string
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * Returns the unique identifier for this query.
     * 
     * @return the query ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this query.
     * 
     * @param id the query ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the file path where this SQL query is defined.
     * This is typically a YAML configuration file path that contains
     * the query definition and metadata.
     * 
     * @return the source file path, or null if not set
     */
    public String getSqlSourceFilePath() {
        return sqlSourceFilePath;
    }

    /**
     * Sets the file path where this SQL query is defined.
     * This is used by the framework to track the source location of queries
     * for hot-reloading and debugging purposes.
     * 
     * @param sqlSourceFilePath the source file path
     */
    public void setSqlSourceFilePath(String sqlSourceFilePath) {
        this.sqlSourceFilePath = sqlSourceFilePath;
    }

    /**
     * Returns the last modification time of the source file in milliseconds since epoch.
     * This timestamp is used by the framework to determine if the query definition
     * needs to be reloaded from the source file.
     * 
     * @return the last modification time in milliseconds, or 0 if not set
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the last modification time of the source file.
     * This is used by the framework's hot-reloading mechanism to track
     * when query definitions have been updated.
     * 
     * @param lastModifiedTime the last modification time in milliseconds since epoch
     */
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Returns whether this is a native SQL query or JPQL.
     * 
     * @return true if this is a native SQL query, false for JPQL
     */
    public boolean getNativeQuery() {
        if("false".equals(nativeQuery)) {
            return false;
        }
        return true;
    }

    /**
     * Sets whether this is a native SQL query.
     * 
     * @param nativeQuery "true" for native SQL, "false" for JPQL
     */
    public void setNativeQuery(String nativeQuery) {
        this.nativeQuery = nativeQuery;
    }

    /**
     * Returns the reference to another query (for query inheritance).
     * 
     * @return the reference query ID, or null if no reference
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the reference to another query for inheritance.
     * 
     * @param ref the reference query ID
     */
    public void setRef(String ref) {
        this.ref = ref;
    }
}
