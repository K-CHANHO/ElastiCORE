package io.elasticore.runtime.port;

import java.util.List;
import java.util.Map;

/**
 * An interface for handling HTTP authentication processing.
 * This interface defines a method for performing authentication or authorization
 * operations based on the given input object and HTTP headers.
 */
public interface HttpAuthProvider {

    /**
     * Processes authentication or authorization logic using the provided input object
     * and a list of HTTP header maps.
     *
     * @param inputObject The input data for the HTTP request body.
     * @param headerMapList A list of maps containing HTTP headers as key-value pairs.
     * @return The modified input data if changes are required; otherwise, returns null.
     */
    Object process(Object inputObject, List<Map<String, String>> headerMapList);

}
