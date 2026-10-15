package io.elasticore.runtime.port;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Interface for transforming HTTP responses and customizing the deserialization process.
 *
 * <p>This interface extends {@link HttpAuthProvider}, allowing implementations to handle
 * both authentication (request modification) and response transformation in a single component.
 * It is used by {@code HttpServiceProxyFactory} to customize HTTP call behavior.</p>
 *
 * <h3>Usage Flow:</h3>
 * <ol>
 *   <li>Before HTTP call: {@link HttpAuthProvider#process} modifies request headers/body</li>
 *   <li>HTTP call execution: Response is deserialized using type from {@link #getReturnType()}</li>
 *   <li>After HTTP call: {@link #transformResponse} converts the raw response to final result</li>
 * </ol>
 *
 * <h3>Registration:</h3>
 * <p>Instances are created via {@link HttpTransformerFactory} which should be registered
 * as a Spring bean with ID: {@code {externalServiceId}.httpTransformFactory}</p>
 *
 * @author ElastiCore Framework
 * @since 1.0
 * @see HttpTransformerFactory
 * @see HttpAuthProvider
 */
public interface HttpTransformer extends HttpAuthProvider {

    /**
     * Returns the type to be used for deserializing the HTTP response body.
     *
     * <p>This allows the transformer to specify a different deserialization type
     * than the method's declared return type. For example, the method may return
     * {@code UserDTO} but the actual HTTP response might be wrapped in a generic
     * {@code ApiResponse<UserDTO>} structure.</p>
     *
     * @return the {@link Type} to use for response deserialization;
     *         can be a Class, ParameterizedType, or other Type implementation
     */
    Type getReturnType();

    /**
     * Transforms the deserialized HTTP response into the final return value.
     *
     * <p>This method is called after the HTTP response has been deserialized
     * using the type returned by {@link #getReturnType()}. It allows for:</p>
     * <ul>
     *   <li>Unwrapping nested response structures (e.g., extracting data from wrapper objects)</li>
     *   <li>Error handling based on response content</li>
     *   <li>Type conversion or mapping to different DTOs</li>
     *   <li>Logging or metrics collection</li>
     * </ul>
     *
     * @param respObject the deserialized response object from the HTTP call
     * @param headerMapList the response headers (may be null if not captured)
     * @return the transformed response to be returned to the caller
     */
    Object transformResponse(Object respObject, List<Map<String, String>> headerMapList);
}
