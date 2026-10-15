package io.elasticore.springboot3.dbms;

import io.elasticore.runtime.port.DbmsService;
import io.elasticore.springboot3.dbms.meta.SqlQueryInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Interface for executing SQL queries and operations against database systems.
 * This interface provides a unified API for database operations including
 * queries, updates, and paginated results.
 * 
 * <p>The executor supports various return types including single objects,
 * lists, and paginated results. It handles parameter mapping and result
 * type conversion automatically.</p>
 * 
 * @author ElastiCore Framework
 * @since 1.0
 */
public interface DbmsSqlExecutor {




    /**
     * Executes a query and returns the results as a list.
     * 
     * @param <I> the input parameter type
     * @param <O> the output result type
     * @param dbmsSvcMeta the DBMS service metadata configuration
     * @param methodName the name of the method/query to execute
     * @param input the input parameters for the query
     * @param outputType the class of the expected output type
     * @return a list of results mapped to the specified output type
     * @throws IOException if an I/O error occurs during execution
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     * @throws IllegalAccessException if the method cannot be accessed
     */
    <I, O> List<O> inqueryList(DbmsService dbmsSvcMeta, String methodName, I input, Class<O> outputType) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException;

    /**
     * Executes a paginated query and returns the results as a Page.
     * 
     * @param <I> the input parameter type
     * @param <O> the output result type
     * @param dbmsSvcMeta the DBMS service metadata configuration
     * @param methodName the name of the method/query to execute
     * @param input the input parameters for the query
     * @param outputType the class of the expected output type
     * @param pageable the pagination information
     * @return a Page containing the results and pagination metadata
     * @throws IOException if an I/O error occurs during execution
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     * @throws IllegalAccessException if the method cannot be accessed
     */
    <I, O> Page<O> inqueryPage(DbmsService dbmsSvcMeta, String methodName, I input, Class<O> outputType, Pageable pageable) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException;

    /**
     * Executes a query and returns the first result or null if no results found.
     * 
     * @param <I> the input parameter type
     * @param <O> the output result type
     * @param dbmsSvcMeta the DBMS service metadata configuration
     * @param methodName the name of the method/query to execute
     * @param input the input parameters for the query
     * @param outputType the class of the expected output type
     * @return the first result mapped to the specified output type, or null if no results
     * @throws IOException if an I/O error occurs during execution
     * @throws NoSuchMethodException if the specified method cannot be found
     * @throws IllegalAccessException if the method cannot be accessed
     * @throws InvocationTargetException if the underlying method throws an exception
     */
    <I, O> O inqueryFirst(DbmsService dbmsSvcMeta, String methodName, I input, Class<O> outputType) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    /**
     * Executes an update, insert, or delete operation.
     * 
     * @param dbmsSvcMeta the DBMS service metadata configuration
     * @param methodName the name of the method/operation to execute
     * @param input the input parameters for the operation
     * @return the number of rows affected by the operation
     * @throws IOException if an I/O error occurs during execution
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     */
    int executeUpdate(DbmsService dbmsSvcMeta, String methodName, Object input) throws IOException, InvocationTargetException, NoSuchMethodException;

    /**
     * Executes an update, insert, or delete operation once per element of the input list.
     * Each element is used as the parameter source for a single execution of the SQL,
     * and the affected row counts of all executions are summed.
     *
     * <p>The default implementation iterates over the list and delegates each element
     * to {@link #executeUpdate(DbmsService, String, Object)}. Implementations may
     * override this method to execute all elements within a single transaction.</p>
     *
     * @param dbmsSvcMeta the DBMS service metadata configuration
     * @param methodName the name of the method/operation to execute
     * @param inputList the list of parameter source objects, one per execution
     * @return the total number of rows affected by all executions
     * @throws IOException if an I/O error occurs during execution
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     */
    default int executeBatchUpdate(DbmsService dbmsSvcMeta, String methodName, List<?> inputList) throws IOException, InvocationTargetException, NoSuchMethodException {
        if (inputList == null || inputList.isEmpty())
            return 0;

        int total = 0;
        for (Object item : inputList) {
            if (item == null)
                continue;
            total += executeUpdate(dbmsSvcMeta, methodName, item);
        }
        return total;
    }

    /**
     * Executes a query using SqlQueryInfo metadata and returns results as a list.
     * 
     * @param <I> the input parameter type
     * @param <O> the output result type
     * @param entry the SQL query information containing the query definition
     * @param input the input parameters for the query
     * @param outputType the class of the expected output type
     * @param dataSource the name of the datasource to use
     * @return a list of results mapped to the specified output type
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     */
    <I, O> List<O> executeQuery(SqlQueryInfo entry, I input, Class<O> outputType, String dataSource) throws InvocationTargetException, NoSuchMethodException;

    /**
     * Executes a paginated query using SqlQueryInfo metadata and returns results as a Page.
     * 
     * @param <I> the input parameter type
     * @param <O> the output result type
     * @param entry the SQL query information containing the query definition
     * @param input the input parameters for the query
     * @param outputType the class of the expected output type
     * @param dataSource the name of the datasource to use
     * @param pageable the pagination information
     * @param checkTotalCount whether to calculate the total count for pagination
     * @return a Page containing the results and pagination metadata
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws NoSuchMethodException if the specified method cannot be found
     */
    <I, O> Page<O> executePageQuery(SqlQueryInfo entry, I input, Class<O> outputType, String dataSource,  Pageable pageable, boolean checkTotalCount) throws InvocationTargetException, NoSuchMethodException;
}
