package io.elasticore.runtime.port;

import java.lang.reflect.Method;

/**
 * Factory interface for creating {@link HttpTransformer} instances for HTTP service proxy calls.
 *
 * <p>Implementations of this interface are responsible for creating appropriate
 * {@link HttpTransformer} instances based on the context of each HTTP method invocation.
 * This enables per-method customization of request/response handling.</p>
 *
 * <h3>Registration:</h3>
 * <p>Register as a Spring bean with ID: {@code {externalServiceId}.httpTransformFactory}</p>
 * <pre>{@code
 * @Bean("myApi.httpTransformFactory")
 * public HttpTransformerFactory myApiTransformerFactory() {
 *     return (proxy, method, args, authProvider) -> {
 *         // Create and return appropriate HttpTransformer
 *         return new MyHttpTransformer(method.getReturnType());
 *     };
 * }
 * }</pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Unwrapping API responses from generic wrapper objects</li>
 *   <li>Method-specific authentication token handling</li>
 *   <li>Response type resolution for generic method signatures</li>
 *   <li>Centralized error handling and response validation</li>
 * </ul>
 *
 * @author ElastiCore Framework
 * @since 1.0
 * @see HttpTransformer
 * @see ExternalService
 */
public interface HttpTransformerFactory {

    /**
     * Creates an {@link HttpTransformer} for the given HTTP service method invocation.
     *
     * <p>This method is called by {@code HttpServiceProxyFactory} before each HTTP request.
     * The returned transformer will be used for:</p>
     * <ol>
     *   <li>Determining the response deserialization type via {@link HttpTransformer#getReturnType()}</li>
     *   <li>Processing request headers/body via {@link HttpAuthProvider#process}</li>
     *   <li>Transforming the response via {@link HttpTransformer#transformResponse}</li>
     * </ol>
     *
     * @param proxy the proxy instance that the method was invoked on
     * @param method the {@link Method} being invoked on the service interface
     * @param args the arguments passed to the method invocation
     * @param httpAuthProvider the existing {@link HttpAuthProvider} if any (may be null);
     *                         can be used to chain or delegate authentication
     * @return an {@link HttpTransformer} instance to handle this specific method call;
     *         returning null will skip transformation and use default behavior
     */
    HttpTransformer getHttpTransformer(Object proxy, Method method, Object[] args, HttpAuthProvider httpAuthProvider);
}
