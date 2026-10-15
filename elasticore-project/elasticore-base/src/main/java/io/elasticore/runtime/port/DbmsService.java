package io.elasticore.runtime.port;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a Database Management System (DBMS) service.
 * This annotation is applied at the class level and specifies the data source
 * and an identifier for the service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmsService {

    /**
     * Specifies the name of the data source to be used by this service.
     *
     * @return the name of the data source
     */
    String datasource() default "primary";

    /**
     * Specifies the unique identifier for this DBMS service.
     *
     * @return the unique service identifier
     */
    String id();

    /**
     * Optional SQL source.
     * If not provided, the default value is an empty string ("").
     *
     * @return the SQL source or default empty string
     */
    String sqlSource();

}
