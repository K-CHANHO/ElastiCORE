package io.elasticore.base.model.loader;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/** namespace: { base: com.acme.erp } lays out the hexagon under one package. */
public class NamespaceBaseTest {

    @Test
    public void oneLineFillsTheLayoutAndWrittenKeysWin() {
        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("base", "com.acme.erp");
        ns.put("control", "com.acme.erp.web");
        Map<String, String> out = FileBasedModelLoader.withBaseLayout(ns);
        assertEquals("com.acme.erp.domain.model", out.get("entity"));
        assertEquals("com.acme.erp.domain.repository", out.get("domainRepository"));
        assertEquals("com.acme.erp.application.port.in", out.get("inboundPort"));
        assertEquals("com.acme.erp.infrastructure.persistence", out.get("repository"));
        assertEquals("com.acme.erp.web", out.get("control"));
        assertFalse(out.containsKey("base"));
    }

    @Test
    public void withoutBaseNothingChanges() {
        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("entity", "a.b");
        assertSame(ns, FileBasedModelLoader.withBaseLayout(ns));
    }
}
