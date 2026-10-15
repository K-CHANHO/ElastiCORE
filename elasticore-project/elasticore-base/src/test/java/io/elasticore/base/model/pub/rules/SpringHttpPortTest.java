package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.base.PortBaseRules;
import io.elasticore.base.model.pub.rules.base.SpringHttpPorts;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP ports written as Spring HTTP service clients (httpPort: spring): the
 * request each method makes is the one the ElastiCORE port runtime made.
 * The project-level switch and the Boot 4 check are SpecFactory's; here the
 * spec says it directly.
 */
public class SpringHttpPortTest {

    private static PortSpec port(boolean spring) {
        PortSpec p = new PortSpec("CreditCheckPort", "erp").protocol("http").url("https://credit.example.com/api")
                .authKey("Bearer k").timeout("connectTimeout", 2000).timeout("writeTimeout", 9000)
                .springHttp(spring).domainClassName("Erp")
                .pkg("port", "com.acme.erp.application.port.out").pkg("dto", "com.acme.erp.application.dto")
                .pkg("entity", "com.acme.erp.domain.model");
        p.operation("checkCredit", "CreditResultDTO").param("String", "bizNo")
                .endpoint("/partners/{bizNo}/credit", "GET", null);
        p.operation("search", "List<CreditResultDTO>").param("String", "grade").param("SearchDTO", "cond")
                .endpoint("/credits", "GET", null);
        p.operation("review", "CreditResultDTO").param("String", "bizNo").param("CreditResultDTO", "body")
                .endpoint("/partners/{bizNo}/review", "POST", null);
        p.operation("upload", "CreditResultDTO").param("@MultipartContentType(\"application/json\") Meta", "meta", "Meta")
                .param("org.springframework.core.io.Resource", "file")
                .endpoint("/upload", "POST", "multipart/form-data");
        return p;
    }

    private static String render(PortSpec p, SourceCode.TemplateSet set) {
        return new RuleEngine().register(new PortBaseRules()).templateSet(set).run(p, "port").render();
    }

    @Test
    public void eachParameterGoesWhereTheRuntimeSentIt() {
        String out = render(port(true), SourceCode.TemplateSet.V3);
        assertTrue(out.contains("@HttpExchange\npublic interface CreditCheckPort")
                || out.contains("@HttpExchange\r\npublic interface CreditCheckPort"), out);
        assertTrue(out.contains("CreditResultDTO checkCredit(@PathVariable(\"bizNo\") String bizNo);"), out);
        // An object on a GET: the caller's method flattens it, the exchange takes the query.
        assertTrue(out.contains("default List<CreditResultDTO> search(String grade, SearchDTO cond) {"), out);
        assertTrue(out.contains("return searchExchange(grade, HttpQuery.of(cond));"), out);
        assertTrue(out.contains("@RequestParam(name = \"grade\", required = false) String grade, @RequestParam MultiValueMap<String, String> ecQuery"), out);
        assertTrue(out.contains("@PostExchange(url = \"/partners/{bizNo}/review\")"), out);
        assertTrue(out.contains("@RequestBody(required = false) CreditResultDTO body"), out);
        assertTrue(out.contains("@PostExchange(url = \"/upload\", contentType = \"multipart/form-data\")"), out);
        assertTrue(out.contains("@RequestPart(name = \"meta\", required = false) Meta meta"), out);
        assertFalse(out.contains("@ExternalService") || out.contains("@HttpEndpoint"), out);
    }

    @Test
    public void withoutTheSwitchThePortIsWhatItWas() {
        String out = render(port(false), SourceCode.TemplateSet.V3);
        assertTrue(out.contains("@ExternalService"), out);
        assertFalse(out.contains("@HttpExchange"), out);
    }

    @Test
    public void theGroupCarriesTheDslDefaultsUnderTheRuntimesKeys() {
        String props = SpringHttpPorts.properties("erp", Collections.singletonList(port(true)));
        assertTrue(props.contains("spring.http.serviceclient.erp--credit-check-port.base-url=${erp.CreditCheckPort.url:https://credit.example.com/api}"), props);
        assertTrue(props.contains("connect-timeout=${erp.CreditCheckPort.connectTimeout:2000}ms"), props);
        assertTrue(props.contains("read-timeout=${erp.CreditCheckPort.readTimeout:${elasticore.http.readTimeout:90000}}ms"), props);
        assertTrue(SpringHttpPorts.unsupported(port(true)).contains("@timeout(write=...)"));

        String config = SpringHttpPorts.configuration(Syntax.JAVA, "com.acme.erp.application.port.out",
                "ErpHttpServices", "erp-http-services", Collections.singletonList(port(true)));
        assertTrue(config.contains("@ImportHttpServices(group = \"erp--credit-check-port\", types = CreditCheckPort.class)"), config);
        assertTrue(config.contains("PortAuth.interceptor(env, context, \"erp.CreditCheckPort\", \"Bearer k\")"), config);
    }

    /** What a Spring client cannot send as the runtime does keeps the port on the runtime. */
    @Test
    public void aPortTheSpringClientCannotMatchStaysOnTheRuntime() {
        PortSpec form = new PortSpec("TokenPort", "erp").protocol("http");
        form.operation("token", "TokenDTO").param("TokenRequest", "req").endpoint("/token", "POST",
                "application/x-www-form-urlencoded");
        assertTrue(SpringHttpPorts.runtimeOnlyReason(form).contains("form"));

        PortSpec wrapper = new PortSpec("UploadPort", "erp").protocol("http");
        wrapper.operation("upload", "R").param("UploadForm", "form").endpoint("/u", "POST", "multipart/form-data");
        assertTrue(SpringHttpPorts.runtimeOnlyReason(wrapper).contains("multipart"));

        PortSpec two = new PortSpec("TwoPort", "erp").protocol("http");
        two.operation("save", "R").param("A", "a").param("B", "b").endpoint("/s", "POST", null);
        assertTrue(SpringHttpPorts.runtimeOnlyReason(two).contains("2 objects"));

        PortSpec array = new PortSpec("ArrayPort", "erp").protocol("http");
        array.operation("find", "R").param("Long[]", "ids").endpoint("/f", "GET", null);
        assertTrue(SpringHttpPorts.runtimeOnlyReason(array).contains("array"));

        PortSpec unbound = new PortSpec("UnboundPort", "erp").protocol("http");
        unbound.operation("get", "R").endpoint("/a/{id}", "GET", null);
        assertTrue(SpringHttpPorts.runtimeOnlyReason(unbound).contains("{id}"));

        org.junit.jupiter.api.Assertions.assertNull(SpringHttpPorts.runtimeOnlyReason(port(true)));
    }

    @Test
    public void groupsOfDifferentDomainsDoNotMeet() {
        org.junit.jupiter.api.Assertions.assertNotEquals(
                SpringHttpPorts.group(new PortSpec("CreditPort", "erp-core")),
                SpringHttpPorts.group(new PortSpec("CoreCreditPort", "erp")));
    }
}
