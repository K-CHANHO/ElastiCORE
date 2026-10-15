package io.elasticore.runtime.port;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the Content-Type for a parameter in multipart/form-data requests.
 * Used on Port interface method parameters to indicate per-field content type.
 *
 * <p>Example usage in Port YAML DSL:</p>
 * <pre>
 * params:
 *   data: RentClaimRequest @contentType(application/json)
 *   file: FileSystemResource
 * </pre>
 *
 * <p>Generates:</p>
 * <pre>
 * void method(@MultipartContentType("application/json") RentClaimRequest data, FileSystemResource file);
 * </pre>
 *
 * @author ElastiCore Framework
 * @since 1.8
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipartContentType {
    /**
     * The Content-Type value (e.g., "application/json", "text/plain").
     *
     * @return the media type string
     */
    String value();
}
