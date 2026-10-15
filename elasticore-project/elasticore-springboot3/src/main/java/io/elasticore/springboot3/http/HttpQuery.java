package io.elasticore.springboot3.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.Map;

/**
 * Turns the objects a port method sends as a query - a search DTO on a GET -
 * into query parameters, the way the ElastiCORE port runtime does: one
 * parameter per non-null property, a collection as repeated values.
 *
 * Used by ports generated as Spring HTTP service clients (httpPort: spring),
 * whose interface keeps the DTO in its signature and passes this map on.
 */
public final class HttpQuery {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private HttpQuery() {
    }

    public static MultiValueMap<String, String> of(Object... sources) {
        MultiValueMap<String, String> query = new LinkedMultiValueMap<String, String>();
        if (sources == null) return query;
        for (Object source : sources) {
            if (source == null) continue;
            Map<String, Object> values = MAPPER.convertValue(source, new TypeReference<Map<String, Object>>() {
            });
            for (Map.Entry<String, Object> en : values.entrySet()) {
                Object v = en.getValue();
                if (v == null) continue;
                if (v instanceof Collection) {
                    for (Object item : (Collection<?>) v) {
                        if (item != null) query.add(en.getKey(), String.valueOf(item));
                    }
                } else if (v instanceof Map) {
                    // A nested object has no query form the runtime ever gave it.
                    continue;
                } else {
                    query.add(en.getKey(), String.valueOf(v));
                }
            }
        }
        return query;
    }
}
