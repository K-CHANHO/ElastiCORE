package io.elasticore.runtime.port;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define an HTTP endpoint.
 * This annotation is applied at the method level to specify the URL,
 * HTTP method, and content type for an API endpoint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpEndpoint {

    /**
     * Specifies the URL of the HTTP endpoint.
     *
     * @return the endpoint URL
     */
    String url();

    /**
     * Specifies the HTTP method (e.g., GET, POST, PUT, DELETE).
     * The default method is GET.
     *
     * @return the HTTP method type
     */
    String method() default "GET";

    /**
     * Specifies the content type of the request or response.
     * The default content type is "application/json".
     *
     * @return the content type
     */
    String contentType() default "application/json";


    String paramNames() default "";


    /**
     * Content-Type of the request body (input).
     * If empty, the value of {@link #contentType()} is used.
     */
    String consumes() default "";

    /**
     * Content-Type of the response body (output).
     * If empty, the value of {@link #contentType()} is used.
     */
    String produces() default "";
}
