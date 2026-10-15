package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.lang.kotlin.KotlinKeywords;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hexagon names its driving side adapter.in.web, and "in" is a Kotlin
 * keyword: found by a Kotlin ERP whose whole web layer failed to parse.
 */
public class KotlinKeywordsTest {

    @Test
    public void aKeywordSegmentIsQuotedWhereverTheNameIsCode() {
        assertEquals("package com.acme.adapter.`in`.web",
                KotlinKeywords.escapeQualifiedNames("package com.acme.adapter.in.web"));
        assertEquals("import com.acme.application.port.`in`.OrderUseCase",
                KotlinKeywords.escapeQualifiedNames("import com.acme.application.port.in.OrderUseCase"));
        assertEquals("import com.acme.port.`in`.*",
                KotlinKeywords.escapeQualifiedNames("import com.acme.port.in.*"));
        assertEquals("package com.acme.port.`in`",
                KotlinKeywords.escapeQualifiedNames("package com.acme.port.in"));
        assertEquals("    private val useCase: com.acme.port.`in`.OrderUseCase",
                KotlinKeywords.escapeQualifiedNames("    private val useCase: com.acme.port.in.OrderUseCase"));
    }

    @Test
    public void ordinaryCodeAndStringsAreLeftAlone() {
        String code = "for (x in list) if (a is B) return x as C // see port.in.web\n"
                + "@ComponentScan(\"com.acme.adapter.in.web\")";
        String out = KotlinKeywords.escapeQualifiedNames(code);
        assertTrue(out.startsWith("for (x in list) if (a is B) return x as C"), out);
        assertTrue(out.contains("\"com.acme.adapter.in.web\""), "a string is not Kotlin syntax: " + out);
        assertEquals("already.`in`.quoted", KotlinKeywords.escapeQualifiedNames("already.`in`.quoted"));
    }

    @Test
    public void aKotlinControllerCanLoseAnEndpoint() {
        String kt = "package a\n\n@RestController\nclass OrderController(\n    private val s: OrderService\n) {\n\n"
                + "    @Operation(summary = \"Create\")\n    @PostMapping\n    fun create(@RequestBody d: OrderDTO) = s.save(d)\n\n"
                + "    @Operation(summary = \"Read\")\n    @GetMapping(\"/{id}\")\n    fun read(@PathVariable id: String) = s.findById(id)\n}\n";
        Set<String> excluded = new LinkedHashSet<String>();
        excluded.add("create");
        String out = MethodSections.apply(kt, excluded, Collections.<String>emptyList());
        assertFalse(out.contains("fun create"), "@readonly drops the write endpoints in Kotlin too:\n" + out);
        assertTrue(out.contains("fun read"), out);
    }
}
