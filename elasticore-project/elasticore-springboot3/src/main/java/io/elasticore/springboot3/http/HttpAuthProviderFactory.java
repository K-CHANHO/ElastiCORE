package io.elasticore.springboot3.http;

import io.elasticore.runtime.port.HttpAuthProvider;

import java.util.Map;

/**
 * Factory class for creating instances of HttpAuthProvider.
 * This factory provides methods to generate authentication providers
 * that add authorization headers to HTTP requests.
 */
public class HttpAuthProviderFactory {

    /**
     * Creates an HttpAuthProvider that adds a Bearer token to the request headers.
     *
     * @param token The Bearer token to be included in the Authorization header.
     * @return An HttpAuthProvider instance that adds the Authorization header.
     */
    public static HttpAuthProvider createBearerToken(String token) {
        return create("Authorization", "Bearer", token);
    }

    /**
     * Creates an HttpAuthProvider that adds a custom authorization header.
     *
     * @param headerKey The name of the HTTP header to be added (e.g., "Authorization").
     * @param type      The authentication type (e.g., "Bearer", "Basic").
     * @param token     The authentication token or credential.
     * @return An HttpAuthProvider instance that adds the specified authentication header.
     */
    public static HttpAuthProvider create(String headerKey, String type, String token) {
        HttpAuthProvider authProvider = (inputObj, headers) -> {
            // Adds the authentication header to the request headers
            headers.add(Map.of(headerKey, type + " " + token));
            return null;
        };
        return authProvider;
    }

    /**
     * Creates an HttpAuthProvider that adds a custom header with the specified value.
     * This method is useful for adding custom authentication headers without a type prefix.
     * 
     * @param headerKey the name of the HTTP header to be added
     * @param value the value to be set for the header
     * @return an HttpAuthProvider instance that adds the specified header
     */
    public static HttpAuthProvider create(String headerKey, String value) {
        HttpAuthProvider authProvider = (inputObj, headers) -> {
            headers.add(Map.of(headerKey, value));
            return null;
        };
        return authProvider;
    }
}
