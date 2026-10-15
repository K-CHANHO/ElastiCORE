package io.elasticore.springboot3.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

import java.nio.charset.StandardCharsets;

/**
 * The JSON and XML codecs a port's WebClient reads and writes with, on
 * Jackson 2 (Spring Boot 3). elasticore-springboot4 has the same class on
 * Jackson 3: the client calls one name and each runtime brings its own.
 */
final class HttpCodecs {

    private static final ObjectMapper JSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final XmlMapper XML = new XmlMapper();

    private HttpCodecs() {
    }

    static void configure(ClientCodecConfigurer configurer) {
        configurer.defaultCodecs().jackson2JsonDecoder(
                new Jackson2JsonDecoder(JSON,
                        MediaType.APPLICATION_JSON,
                        MediaType.TEXT_HTML,
                        new MediaType("text", "html", StandardCharsets.UTF_8),
                        MediaType.TEXT_PLAIN,
                        new MediaType("text", "plain", StandardCharsets.UTF_8)
                ));
        configurer.defaultCodecs().jackson2JsonEncoder(
                new Jackson2JsonEncoder(JSON, MediaType.APPLICATION_JSON));
        configurer.customCodecs().register(
                new Jackson2JsonDecoder(XML,
                        MediaType.APPLICATION_XML,
                        MediaType.TEXT_XML
                ));
        configurer.customCodecs().register(
                new Jackson2JsonEncoder(XML,
                        MediaType.APPLICATION_XML,
                        MediaType.TEXT_XML
                ));
    }
}
