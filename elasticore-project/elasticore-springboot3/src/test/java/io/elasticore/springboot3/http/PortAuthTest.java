package io.elasticore.springboot3.http;

import io.elasticore.runtime.port.HttpAuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The port runtime's authentication rules, as a Spring client interceptor applies them. */
public class PortAuthTest {

    private static final String ID = "erp.CreditCheckPort";

    private static MockClientHttpRequest send(ClientHttpRequestInterceptor interceptor) throws IOException {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://x/y"));
        interceptor.intercept(request, new byte[0], (r, b) -> new MockClientHttpResponse(new byte[0], 200));
        return request;
    }

    @Test
    public void theDeclaredKeyIsTheDefault() throws IOException {
        MockClientHttpRequest r = send(PortAuth.interceptor(new MockEnvironment(), null, ID, "Bearer dsl"));
        assertEquals("Bearer dsl", r.getHeaders().getFirst("Authorization"));
    }

    @Test
    public void authKeyOverridesItAndApiKeyWins() throws IOException {
        MockEnvironment env = new MockEnvironment().withProperty(ID + ".authKey", "Basic abc");
        assertEquals("Basic abc", send(PortAuth.interceptor(env, null, ID, "Bearer dsl")).getHeaders().getFirst("Authorization"));

        env.setProperty(ID + ".apiKey", "k-1");
        assertEquals("Bearer k-1", send(PortAuth.interceptor(env, null, ID, "Bearer dsl")).getHeaders().getFirst("Authorization"));
    }

    @Test
    public void noKeyNoHeader() throws IOException {
        assertNull(send(PortAuth.interceptor(new MockEnvironment(), null, ID, null)).getHeaders().getFirst("Authorization"));
    }

    /** As the runtime gave it: one map holding the Authorization the keys produced, to edit. */
    @Test
    public void theProviderBeanEditsTheHeaderList() throws IOException {
        StaticApplicationContext context = new StaticApplicationContext();
        HttpAuthProvider provider = (input, headers) -> {
            assertNull(input);
            assertEquals("Bearer dsl", headers.get(0).get("Authorization"));
            headers.get(0).put("Authorization", "Bearer dynamic");
            Map<String, String> more = new HashMap<String, String>();
            more.put("X-Tenant", "t1");
            headers.add(more);
            return null;
        };
        context.getBeanFactory().registerSingleton(ID + ".httpAuthProvider", provider);
        MockClientHttpRequest r = send(PortAuth.interceptor(new MockEnvironment(), context, ID, "Bearer dsl"));
        assertEquals(Collections.singletonList("Bearer dynamic"), r.getHeaders().get("Authorization"));
        assertEquals("t1", r.getHeaders().getFirst("X-Tenant"));
    }

    /** A search object, as a port sends it on a GET. */
    public static class Cond {
        public String code = "C1";
        public Integer qty = null;
        public java.util.List<String> tags = Arrays.asList("a", "b");
    }

    @Test
    public void anObjectBecomesQueryParameters() {
        MultiValueMap<String, String> q = HttpQuery.of(new Cond(), null);
        assertEquals(Collections.singletonList("C1"), q.get("code"));
        assertEquals(Arrays.asList("a", "b"), q.get("tags"));
        assertNull(q.get("qty"));
    }
}
