package io.elasticore.springboot3.http;

import java.util.Map;

/**
 * Wrapper class for multipart request data with per-field content types.
 * Used internally by HttpServiceProxyFactory and HttpApiClient to support
 * per-field Content-Type specification in multipart/form-data requests.
 *
 * <p>This class is created when a Port interface method has multiple parameters
 * with {@link io.elasticore.runtime.port.MultipartContentType} annotations.</p>
 *
 * @author ElastiCore Framework
 * @since 1.8
 */
public class MultipartRequest {

    private final Map<String, Object> parts;
    private final Map<String, String> contentTypes;

    /**
     * Constructs a MultipartRequest with the specified parts and content types.
     *
     * @param parts       map of part names to their values
     * @param contentTypes map of part names to their Content-Type values (may be null or partial)
     */
    public MultipartRequest(Map<String, Object> parts, Map<String, String> contentTypes) {
        this.parts = parts;
        this.contentTypes = contentTypes;
    }

    /**
     * Returns the map of all multipart parts.
     *
     * @return map of part names to their values
     */
    public Map<String, Object> getParts() {
        return parts;
    }

    /**
     * Returns the Content-Type for a specific part.
     *
     * @param partName the name of the part
     * @return the Content-Type string, or null if not specified
     */
    public String getContentType(String partName) {
        return contentTypes != null ? contentTypes.get(partName) : null;
    }

    /**
     * Checks if a specific part has a custom Content-Type specified.
     *
     * @param partName the name of the part
     * @return true if a Content-Type is specified for this part
     */
    public boolean hasContentType(String partName) {
        return contentTypes != null && contentTypes.containsKey(partName);
    }

    /**
     * Checks if this request has any parts.
     *
     * @return true if there are parts in this request
     */
    public boolean hasParts() {
        return parts != null && !parts.isEmpty();
    }
}
