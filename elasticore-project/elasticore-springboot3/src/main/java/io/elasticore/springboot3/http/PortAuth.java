package io.elasticore.springboot3.http;

import io.elasticore.runtime.port.HttpAuthProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A port's request settings for a port generated as a Spring HTTP service
 * client (httpPort: spring), by the rules the ElastiCORE port runtime has
 * always applied - so a project changing how its ports are built keeps its
 * configuration and the answers its callers get:
 *
 *   {id}.apiKey              Authorization: Bearer {apiKey}   (wins)
 *   {id}.authKey             Authorization: {authKey}, default @auth(...)
 *   bean {id}.httpAuthProvider  edits the header list, as with the runtime
 *
 * The provider gets the list the runtime gave it - one map, holding the
 * Authorization the keys produced - and a null body: rewriting the body, which
 * the runtime also allowed, has no place in a client interceptor.
 */
public final class PortAuth {

    private PortAuth() {
    }

    /**
     * @param id          the port id, domain.PortName, as the property keys use it
     * @param declaredKey the @auth value of the DSL, or null
     */
    public static ClientHttpRequestInterceptor interceptor(Environment env, ApplicationContext context,
                                                           String id, String declaredKey) {
        return (request, body, execution) -> {
            Map<String, String> header = new HashMap<String, String>();
            String apiKey = env.getProperty(id + ".apiKey");
            String authKey = env.getProperty(id + ".authKey", declaredKey == null ? "" : declaredKey);
            if (apiKey != null && !apiKey.isEmpty()) {
                header.put("Authorization", "Bearer " + apiKey);
            } else if (authKey != null && !authKey.isEmpty()) {
                header.put("Authorization", authKey);
            }
            List<Map<String, String>> headers = new ArrayList<Map<String, String>>();
            headers.add(header);

            HttpAuthProvider provider = provider(context, id);
            if (provider != null) provider.process(null, headers);

            // Added, as the runtime adds them, after the provider has had its say.
            for (Map<String, String> map : headers) {
                if (map == null) continue;
                for (Map.Entry<String, String> h : map.entrySet()) {
                    if (h.getValue() != null) request.getHeaders().add(h.getKey(), h.getValue());
                }
            }
            return execution.execute(request, body);
        };
    }

    /**
     * Reads an error response's body as the method's return value instead of
     * throwing - what the port runtime did, so a caller that checks the reply
     * of a 404 keeps getting one.
     */
    public static void passErrorBodies(RestClient.Builder builder) {
        builder.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
        });
    }

    private static HttpAuthProvider provider(ApplicationContext context, String id) {
        if (context == null) return null;
        String name = id + ".httpAuthProvider";
        if (!context.containsBean(name)) return null;
        try {
            Object bean = context.getBean(name);
            return bean instanceof HttpAuthProvider ? (HttpAuthProvider) bean : null;
        } catch (BeansException e) {
            return null;
        }
    }
}
