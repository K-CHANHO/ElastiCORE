package io.elasticore.springboot3.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link HttpTimeoutConfig} — verifies the timeout value resolution
 * precedence (per-port property &gt; DSL annotation &gt; global property &gt; default)
 * and the constructor's default-fallback behavior.
 */
public class HttpTimeoutConfigTest {

    // ---- resolveTimeoutValue: precedence ordering ----

    @Test
    public void perPortProperty_winsOverEverything() {
        // per-port=1000, dsl=2000, global=3000, default=10000 -> per-port
        assertEquals(1000, HttpTimeoutConfig.resolveTimeoutValue(1000, 2000, 3000, 10000));
    }

    @Test
    public void dslValue_winsWhenNoPerPortProperty() {
        // per-port unset, dsl=2000, global=3000 -> dsl
        assertEquals(2000, HttpTimeoutConfig.resolveTimeoutValue(null, 2000, 3000, 10000));
    }

    @Test
    public void globalProperty_winsWhenNoPerPortAndNoDsl() {
        // per-port unset, dsl unset(-1), global=3000 -> global
        assertEquals(3000, HttpTimeoutConfig.resolveTimeoutValue(null, -1, 3000, 10000));
    }

    @Test
    public void hardDefault_usedWhenNothingSet() {
        // everything unset -> framework default
        assertEquals(10000, HttpTimeoutConfig.resolveTimeoutValue(null, -1, null, 10000));
    }

    // ---- resolveTimeoutValue: negative values treated as "unset" ----

    @Test
    public void negativePerPort_isSkipped_fallsThroughToDsl() {
        assertEquals(2000, HttpTimeoutConfig.resolveTimeoutValue(-5, 2000, 3000, 10000));
    }

    @Test
    public void negativeDsl_isSkipped_fallsThroughToGlobal() {
        assertEquals(3000, HttpTimeoutConfig.resolveTimeoutValue(null, -1, 3000, 10000));
    }

    @Test
    public void negativeGlobal_isSkipped_fallsThroughToDefault() {
        assertEquals(10000, HttpTimeoutConfig.resolveTimeoutValue(null, -1, -7, 10000));
    }

    @Test
    public void zeroIsAValidExplicitValue_notTreatedAsUnset() {
        // 0 >= 0, so a per-port value of 0 is honored (not skipped)
        assertEquals(0, HttpTimeoutConfig.resolveTimeoutValue(0, 2000, 3000, 10000));
    }

    // ---- constructor + defaults ----

    @Test
    public void constructor_negativeArgsFallBackToFrameworkDefaults() {
        HttpTimeoutConfig c = new HttpTimeoutConfig(-1, -1, -1, -1);
        assertEquals(HttpTimeoutConfig.DEFAULT_CONNECT_TIMEOUT, c.getConnectTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_READ_TIMEOUT, c.getReadTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_WRITE_TIMEOUT, c.getWriteTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_RESPONSE_TIMEOUT, c.getResponseTimeout());
    }

    @Test
    public void constructor_explicitValuesAreHonored() {
        HttpTimeoutConfig c = new HttpTimeoutConfig(1000, 2000, 3000, 4000);
        assertEquals(1000, c.getConnectTimeout());
        assertEquals(2000, c.getReadTimeout());
        assertEquals(3000, c.getWriteTimeout());
        assertEquals(4000, c.getResponseTimeout());
    }

    @Test
    public void constructor_mixedValues_onlyNegativesFallBack() {
        // connect explicit, read unset, write explicit, response unset
        HttpTimeoutConfig c = new HttpTimeoutConfig(5000, -1, 6000, -1);
        assertEquals(5000, c.getConnectTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_READ_TIMEOUT, c.getReadTimeout());
        assertEquals(6000, c.getWriteTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_RESPONSE_TIMEOUT, c.getResponseTimeout());
    }

    @Test
    public void defaults_returnsAllFrameworkDefaults() {
        HttpTimeoutConfig c = HttpTimeoutConfig.defaults();
        assertEquals(HttpTimeoutConfig.DEFAULT_CONNECT_TIMEOUT, c.getConnectTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_READ_TIMEOUT, c.getReadTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_WRITE_TIMEOUT, c.getWriteTimeout());
        assertEquals(HttpTimeoutConfig.DEFAULT_RESPONSE_TIMEOUT, c.getResponseTimeout());
    }
}
