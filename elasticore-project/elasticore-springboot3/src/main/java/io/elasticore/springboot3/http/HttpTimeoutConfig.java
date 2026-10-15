package io.elasticore.springboot3.http;

/**
 * Immutable holder for HTTP client timeout settings (all values in milliseconds).
 *
 * <p>Timeout values are resolved by {@code HttpServiceProxyFactory} using the
 * following precedence (highest priority first):
 * <ol>
 *   <li>per-port runtime property   - {@code {serviceId}.connectTimeout}, ...</li>
 *   <li>port DSL annotation value   - {@code @timeout(connect=...)} baked into {@code @ExternalService}</li>
 *   <li>global runtime property      - {@code elasticore.http.connectTimeout}, ...</li>
 *   <li>framework hardcoded default  - {@code DEFAULT_*}</li>
 * </ol>
 *
 * <p>An individual constructor argument of {@code -1} (or any negative value) means
 * "not set", in which case the corresponding {@code DEFAULT_*} value is used.
 */
public final class HttpTimeoutConfig {

    /** Default connection establishment timeout in milliseconds. */
    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;

    /** Default read (response body) timeout in milliseconds. */
    public static final int DEFAULT_READ_TIMEOUT = 90000;

    /** Default write (request body) timeout in milliseconds. */
    public static final int DEFAULT_WRITE_TIMEOUT = 90000;

    /** Default overall response timeout in milliseconds. */
    public static final int DEFAULT_RESPONSE_TIMEOUT = 60000;

    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    private final int responseTimeout;

    /**
     * Creates a timeout configuration. Any negative value falls back to the
     * matching framework default.
     *
     * @param connectTimeout  connection timeout in ms ({@code -1} for default)
     * @param readTimeout     read timeout in ms ({@code -1} for default)
     * @param writeTimeout    write timeout in ms ({@code -1} for default)
     * @param responseTimeout response timeout in ms ({@code -1} for default)
     */
    public HttpTimeoutConfig(int connectTimeout, int readTimeout, int writeTimeout, int responseTimeout) {
        this.connectTimeout = connectTimeout >= 0 ? connectTimeout : DEFAULT_CONNECT_TIMEOUT;
        this.readTimeout = readTimeout >= 0 ? readTimeout : DEFAULT_READ_TIMEOUT;
        this.writeTimeout = writeTimeout >= 0 ? writeTimeout : DEFAULT_WRITE_TIMEOUT;
        this.responseTimeout = responseTimeout >= 0 ? responseTimeout : DEFAULT_RESPONSE_TIMEOUT;
    }

    /**
     * Returns a configuration using all framework default timeouts.
     *
     * @return a default timeout configuration
     */
    public static HttpTimeoutConfig defaults() {
        return new HttpTimeoutConfig(-1, -1, -1, -1);
    }

    /**
     * Resolves a single timeout value (ms) by precedence (highest first):
     * <ol>
     *   <li>per-port runtime property value</li>
     *   <li>DSL annotation value (when {@code >= 0})</li>
     *   <li>global runtime property value</li>
     *   <li>framework hardcoded default</li>
     * </ol>
     * A {@code null} or negative property value is treated as "not set" and skipped.
     *
     * @param perPortValue the parsed per-port property value, or {@code null} when unset
     * @param dslValue     the DSL {@code @timeout} value ({@code -1}/negative when unset)
     * @param globalValue  the parsed global property value, or {@code null} when unset
     * @param hardDefault  the framework default applied when nothing else is set
     * @return the resolved timeout value in milliseconds
     */
    public static int resolveTimeoutValue(Integer perPortValue, int dslValue, Integer globalValue, int hardDefault) {
        if (perPortValue != null && perPortValue >= 0) {
            return perPortValue;
        }
        if (dslValue >= 0) {
            return dslValue;
        }
        if (globalValue != null && globalValue >= 0) {
            return globalValue;
        }
        return hardDefault;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    @Override
    public String toString() {
        return "HttpTimeoutConfig{connect=" + connectTimeout + "ms, read=" + readTimeout
                + "ms, write=" + writeTimeout + "ms, response=" + responseTimeout + "ms}";
    }
}
