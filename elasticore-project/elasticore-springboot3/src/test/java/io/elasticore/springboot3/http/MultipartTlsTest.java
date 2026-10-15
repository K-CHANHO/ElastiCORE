package io.elasticore.springboot3.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A multipart port call with ignoreSSL against a real HTTPS server whose
 * certificate nobody trusts.
 *
 * The call used to switch certificate checks off for the whole JVM - the trust
 * went in as the HttpsURLConnection default - so after one such call every other
 * HTTPS connection in the application accepted any certificate. It also logged
 * its headers, Authorization included, at INFO.
 */
public class MultipartTlsTest {

    private static final String SECRET = "Bearer s3cr3t-port-key";

    private MockWebServer server;

    @BeforeEach
    public void start() throws IOException {
        HeldCertificate selfSigned = new HeldCertificate.Builder().addSubjectAlternativeName("localhost").build();
        HandshakeCertificates certificates = new HandshakeCertificates.Builder().heldCertificate(selfSigned).build();
        server = new MockWebServer();
        server.useHttps(certificates.sslSocketFactory(), false);
        server.start();
    }

    @AfterEach
    public void stop() throws IOException {
        server.shutdown();
    }

    @Test
    public void ignoreSSLTrustsTheServerForThatCallOnly() {
        SSLSocketFactory defaultFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        HostnameVerifier defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        server.enqueue(new MockResponse().setBody("stored"));

        assertEquals("stored", send(true));

        assertSame(defaultFactory, HttpsURLConnection.getDefaultSSLSocketFactory(),
                "the JVM-wide socket factory must stay as it was");
        assertSame(defaultVerifier, HttpsURLConnection.getDefaultHostnameVerifier(),
                "the JVM-wide host name check must stay as it was");

        // Any other HTTPS connection still refuses the untrusted certificate.
        assertThrows(SSLException.class, () -> {
            HttpsURLConnection other = (HttpsURLConnection) new URL(server.url("/other").toString()).openConnection();
            other.getResponseCode();
        });
    }

    @Test
    public void withoutIgnoreSSLTheCertificateIsChecked() {
        assertThrows(ResourceAccessException.class, () -> send(false));
    }

    @Test
    public void theRequestIsLoggedAtDebugWithoutItsCredentials() throws InterruptedException {
        Logger logger = (Logger) LoggerFactory.getLogger(HttpApiClient.class);
        Level level = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        try {
            server.enqueue(new MockResponse().setBody("stored"));
            send(true);
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(level);
        }

        assertEquals(SECRET, server.takeRequest().getHeader("Authorization"), "the server still gets the credential");
        boolean logged = false;
        for (ILoggingEvent event : appender.list) {
            String line = event.getFormattedMessage();
            assertFalse(line.contains("s3cr3t"), "a log line shows the credential: " + line);
            assertTrue(event.getLevel().toInt() <= Level.DEBUG.toInt(), "logged above DEBUG: " + line);
            logged |= line.startsWith("Multipart request") && line.contains("****");
        }
        assertTrue(logged, "the request line, with its masked Authorization, is logged at DEBUG");
    }

    @Test
    public void credentialHeadersAreMaskedAndTheRestAreShown() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", SECRET);
        headers.add("X-Api-Key", "k");
        headers.add("Cookie", "SESSION=c");
        headers.add("X-Auth-Token", "t");
        headers.add("Accept", "application/json");

        Map<String, List<String>> shown = HttpApiClient.withCredentialsMasked(headers);

        assertEquals(Collections.singletonList("****"), shown.get("Authorization"));
        assertEquals(Collections.singletonList("****"), shown.get("X-Api-Key"));
        assertEquals(Collections.singletonList("****"), shown.get("Cookie"));
        assertEquals(Collections.singletonList("****"), shown.get("X-Auth-Token"));
        assertEquals(Collections.singletonList("application/json"), shown.get("Accept"));
    }

    private String send(boolean ignoreSSL) {
        Map<String, Object> parts = new LinkedHashMap<>();
        parts.put("title", "report");
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", SECRET);
        return HttpApiClient.sendMultipartWithRestTemplate(HttpMethod.POST, server.url("/upload").toString(),
                new MultipartRequest(parts, null), Collections.singletonList(headers), String.class, ignoreSSL);
    }
}
