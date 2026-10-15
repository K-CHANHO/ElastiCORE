package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.base.PortBaseRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Transaction ports, compared against the two committed for the integration
 * domain: MockApiPortService reaches an HTTP service, TestDbmsService a
 * database.
 */
public class PortArtifactTest {

    private static final String BASE = "io.elasticore.blueprint.domain.integration";

    private PortSpec port(String name) {
        return new PortSpec(name, "integration")
                .pkg("port", BASE + ".port")
                .pkg("entity", BASE + ".entity")
                .pkg("dto", BASE + ".dto")
                .pkg("enumeration", BASE + ".enums");
    }

    /** blueprint/integration/port/http_port.yml MockApiPortService. */
    private PortSpec mockApi() {
        PortSpec p = port("MockApiPortService")
                .protocol("http")
                .url("http://localhost:8080/api/mock")
                .timeout("connectTimeout", 3000)
                .timeout("readTimeout", 15000);

        p.operation("getEcho", "HttpTestResponse")
                .param("String", "message")
                .endpoint("/echo/{message}", "GET", null);
        p.operation("postData", "HttpTestResponse")
                .param("HttpTestRequest", "body")
                .endpoint("/data", "POST", null);
        p.operation("updateUser", "HttpUserResponse")
                .param("Long", "userId").param("HttpUserRequest", "body")
                .endpoint("/users/{userId}", "PUT", null);
        p.operation("deleteUser", "HttpTestResponse")
                .param("Long", "userId")
                .endpoint("/users/{userId}", "DELETE", null);
        p.operation("patchUser", "HttpUserResponse")
                .param("Long", "userId").param("HttpTestRequest", "body")
                .endpoint("/users/{userId}/status", "PATCH", null);
        return p;
    }

    private String render(PortSpec p) {
        return new RuleEngine().register(new PortBaseRules()).run(p, "port").render();
    }

    // ---- HTTP ------------------------------------------------------------

    /** The head of the file, down to the interface declaration. */
    @Test
    public void httpPortHead_matchesGolden() throws IOException {
        List<String> golden = goldenLines("MockApiPortService.java");
        List<String> actual = toLines(render(mockApi()));

        String declaration = "public interface MockApiPortService   {";
        int end = golden.indexOf(declaration);
        assertTrue(end > 0, "interface declaration not found in golden");
        assertEquals(join(golden.subList(0, end + 1)), join(actual.subList(0, end + 1)));
    }

    /** Each operation declares the endpoint it calls, exactly as committed. */
    @Test
    public void httpOperations_matchGolden() {
        String out = render(mockApi());

        assertTrue(out.contains("    @HttpEndpoint(url=\"/echo/{message}\", method=\"GET\","
                + " contentType=\"application/json\", paramNames=\"message\")\n"
                + "    HttpTestResponse getEcho(String message);"), out);
        assertTrue(out.contains("    @HttpEndpoint(url=\"/users/{userId}\", method=\"PUT\","
                + " contentType=\"application/json\", paramNames=\"userId,body\")\n"
                + "    HttpUserResponse updateUser(Long userId, HttpUserRequest body);"), out);
        assertTrue(out.contains("method=\"PATCH\""), out);
    }

    /** Each operation is introduced by its own comment. */
    @Test
    public void operations_carryTheirComment() {
        String out = render(mockApi());

        assertTrue(out.contains("    /*\n     * getEcho\n    */"), out);
    }

    /** A declared timeout appears on the service annotation, an absent one does not. */
    @Test
    public void declaredTimeouts_appearOnTheAnnotation() {
        String out = render(mockApi());

        assertTrue(out.contains("connectTimeout=3000, readTimeout=15000)"), out);
        assertFalse(out.contains("writeTimeout"), out);
    }

    /** An authorization key is named when the port declares one. */
    @Test
    public void authKey_isNamedWhenDeclared() {
        PortSpec p = mockApi().authKey("X-Api-Key");

        assertTrue(render(p).contains(", authKey=\"X-Api-Key\""), render(p));
    }

    /** An operation with no declared contentType takes the default. */
    @Test
    public void explicitContentType_overridesTheDefault() {
        PortSpec p = port("Api").protocol("http").url("http://h");
        p.operation("send", "String").param("String", "x")
                .endpoint("/send", "POST", "text/plain");

        assertTrue(render(p).contains("contentType=\"text/plain\""), render(p));
    }

    // ---- database ---------------------------------------------------------

    /** The head of the database port, down to its declaration. */
    @Test
    public void dbmsPortHead_matchesGolden() throws IOException {
        PortSpec p = port("TestDbmsService")
                .protocol("dbms")
                .sqlSource("blueprint/integration/port/dbms_port.yml");
        p.operation("countAllLogs", "Integer");

        List<String> golden = goldenLines("TestDbmsService.java");
        List<String> actual = toLines(render(p));

        String declaration = "public interface TestDbmsService   {";
        int end = golden.indexOf(declaration);
        assertTrue(end > 0, "interface declaration not found in golden");
        assertEquals(join(golden.subList(0, end + 1)), join(actual.subList(0, end + 1)));
    }

    /** A database operation declares no endpoint. */
    @Test
    public void dbmsOperations_declareNoEndpoint() {
        PortSpec p = port("TestDbmsService").protocol("dbms")
                .sqlSource("blueprint/integration/port/dbms_port.yml");
        p.operation("getLogById", "DbmsResultDTO").param("Long", "logId");

        String out = render(p);
        assertTrue(out.contains("    DbmsResultDTO getLogById(Long logId);"), out);
        assertFalse(out.contains("HttpEndpoint"), out);
    }

    /** A chosen datasource is named alongside the sql source. */
    @Test
    public void datasource_isNamedWhenChosen() {
        PortSpec p = port("TestDbmsService").protocol("dbms")
                .datasource("secondary").sqlSource("blueprint/x.yml");
        p.operation("count", "Integer");

        assertTrue(render(p).contains(
                "@DbmsService(id=\"integration.TestDbmsService\" ,datasource=\"secondary\""
                        + " ,sqlSource=\"blueprint/x.yml\")"), render(p));
    }

    /** A proto port is a different artifact and is not produced here. */
    @Test
    public void protoPort_producesNoFile() {
        SourceCode code = new RuleEngine().register(new PortBaseRules())
                .run(port("GrpcPort").protocol("proto"), "port");

        assertFalse(code.isMake());
    }

    // ---- golden access ----------------------------------------------------

    private List<String> goldenLines(String fileName) throws IOException {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/integration/port/" + fileName;
        String[] candidates = {"../" + rel, "elasticore-project/" + rel};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) {
                List<String> lines = readLines(f);
                return lines.subList(1, lines.size());   // skip the //ecd: line
            }
        }
        throw new IllegalStateException("Golden file not found: " + rel);
    }

    private static List<String> toLines(String text) {
        List<String> lines = new ArrayList<String>();
        for (String l : text.split("\n", -1)) lines.add(l);
        return lines;
    }

    private static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private List<String> readLines(File f) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
        } finally {
            r.close();
        }
        return lines;
    }
}
