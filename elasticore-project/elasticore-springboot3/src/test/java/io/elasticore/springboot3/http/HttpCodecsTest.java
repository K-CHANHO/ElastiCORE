package io.elasticore.springboot3.http;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The codecs a port's WebClient reads and writes with - Jackson 2 in
 * elasticore-springboot3, Jackson 3 in elasticore-springboot4, which runs
 * this same test - against a real HTTP exchange.
 */
public class HttpCodecsTest {

    /** A response body, as a port's DTO would declare it. */
    public static class Reply {
        private String name;
        private int count;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    private MockWebServer server;
    private WebClient client;

    @BeforeEach
    public void start() throws IOException {
        server = new MockWebServer();
        server.start();
        client = HttpApiClient.createWebClient(server.url("/").toString());
    }

    @AfterEach
    public void stop() throws IOException {
        server.shutdown();
    }

    @Test
    public void aJsonReplyIsReadAndUnknownFieldsAreIgnored() {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"name\":\"ace\",\"count\":3,\"extra\":true}"));
        Reply reply = client.get().uri("/r").retrieve().bodyToMono(Reply.class).block(Duration.ofSeconds(10));
        assertEquals("ace", reply.getName());
        assertEquals(3, reply.getCount());
    }

    @Test
    public void aJsonReplySentAsTextIsReadToo() {
        server.enqueue(new MockResponse().setHeader("Content-Type", "text/plain;charset=UTF-8")
                .setBody("{\"name\":\"plain\",\"count\":1}"));
        Reply reply = client.get().uri("/r").retrieve().bodyToMono(Reply.class).block(Duration.ofSeconds(10));
        assertEquals("plain", reply.getName());
    }

    @Test
    public void aRequestBodyIsWrittenAsJson() throws InterruptedException {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{}"));
        Reply body = new Reply();
        body.setName("sent");
        body.setCount(7);
        client.post().uri("/w").contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                .retrieve().bodyToMono(String.class).block(Duration.ofSeconds(10));
        RecordedRequest request = server.takeRequest();
        String sent = request.getBody().readUtf8();
        assertTrue(sent.contains("\"name\":\"sent\"") && sent.contains("\"count\":7"), sent);
    }

    @Test
    public void anXmlReplyIsRead() {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/xml")
                .setBody("<Reply><name>xml</name><count>2</count></Reply>"));
        Reply reply = client.get().uri("/x").accept(MediaType.APPLICATION_XML).retrieve()
                .bodyToMono(Reply.class).block(Duration.ofSeconds(10));
        assertEquals("xml", reply.getName());
        assertEquals(2, reply.getCount());
    }
}
