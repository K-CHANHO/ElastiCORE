package io.elasticore.runtime.port;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define an external service.
 * This annotation is applied at the class level and specifies the service type
 * and a unique identifier for the service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalService {

    /**
     * Specifies the type of the external service protocol.
     *
     * @return the protocole
     */
    String protocol();

    /**
     * Specifies the unique identifier for this external service.
     *
     * @return the unique service identifier
     */
    String id();


    String url();


    String authKey() default "";

    String authHeaderName() default "";

    /**
     * Connection establishment timeout in milliseconds.
     * A value of {@code -1} means "not set in DSL"; the runtime then falls back
     * to a global property or the framework default.
     *
     * @return the connect timeout in ms, or {@code -1} when unset
     */
    int connectTimeout() default -1;

    /**
     * Read (response body) timeout in milliseconds.
     * A value of {@code -1} means "not set in DSL".
     *
     * @return the read timeout in ms, or {@code -1} when unset
     */
    int readTimeout() default -1;

    /**
     * Write (request body) timeout in milliseconds.
     * A value of {@code -1} means "not set in DSL".
     *
     * @return the write timeout in ms, or {@code -1} when unset
     */
    int writeTimeout() default -1;

    /**
     * Overall response timeout in milliseconds.
     * A value of {@code -1} means "not set in DSL".
     *
     * @return the response timeout in ms, or {@code -1} when unset
     */
    int responseTimeout() default -1;

}
