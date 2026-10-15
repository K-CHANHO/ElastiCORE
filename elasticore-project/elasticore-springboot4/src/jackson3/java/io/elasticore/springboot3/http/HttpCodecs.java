package io.elasticore.springboot3.http;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.http.codec.xml.JacksonXmlDecoder;
import org.springframework.http.codec.xml.JacksonXmlEncoder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

import java.nio.charset.StandardCharsets;

/**
 * The JSON and XML codecs a port's WebClient reads and writes with, on
 * Jackson 3 (Spring Boot 4). Spring Framework 7 deprecates the Jackson 2
 * codecs for removal; the media types each one accepts are the ones the
 * Jackson 2 version accepts, so a port reads the same responses as before.
 */
final class HttpCodecs {

    private static final JsonMapper JSON = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final XmlMapper XML = XmlMapper.builder().build();

    private HttpCodecs() {
    }

    static void configure(ClientCodecConfigurer configurer) {
        configurer.defaultCodecs().jacksonJsonDecoder(
                new JacksonJsonDecoder(JSON,
                        MediaType.APPLICATION_JSON,
                        MediaType.TEXT_HTML,
                        new MediaType("text", "html", StandardCharsets.UTF_8),
                        MediaType.TEXT_PLAIN,
                        new MediaType("text", "plain", StandardCharsets.UTF_8)
                ));
        configurer.defaultCodecs().jacksonJsonEncoder(
                new JacksonJsonEncoder(JSON, MediaType.APPLICATION_JSON));
        configurer.defaultCodecs().jacksonXmlDecoder(
                new JacksonXmlDecoder(XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
        configurer.defaultCodecs().jacksonXmlEncoder(
                new JacksonXmlEncoder(XML, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
    }
}
