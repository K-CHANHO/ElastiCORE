package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the three-level rule configuration:
 * env.yml (domain) over gradle system property (project) over built-in default.
 */
public class RulesConfigTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    @AfterEach
    public void clearProps() {
        System.clearProperty(RulesConfig.PROP_RULES);
        System.clearProperty(RulesConfig.PROP_SUPPRESS);
    }

    private EntitySpec order() {
        return new EntitySpec("Order")
                .addField(loader.parse("orderId", "long @id @sequence"))
                .addField(loader.parse("orderNo", "string(30)"));
    }

    @Test
    public void noConfig_fallsBackToDefaultBundle() {
        assertArrayEquals(new String[]{"jpa-springboot"}, RulesConfig.resolveRules(null, RulesConfig.DEFAULT_BUNDLE));
    }

    @Test
    public void projectProperty_appliesWhenDomainSilent() {
        System.setProperty(RulesConfig.PROP_RULES, "jpa-entity, jpa-dto");
        assertArrayEquals(new String[]{"jpa-entity", "jpa-dto"}, RulesConfig.resolveRules(null, RulesConfig.DEFAULT_BUNDLE));
        assertArrayEquals(new String[]{"jpa-entity", "jpa-dto"},
                RulesConfig.resolveRules(Collections.<String>emptyList(), RulesConfig.DEFAULT_BUNDLE));
    }

    @Test
    public void domainRules_overrideProjectProperty() {
        System.setProperty(RulesConfig.PROP_RULES, "jpa-springboot");
        List<String> domain = Arrays.asList("jpa-entity");
        assertArrayEquals(new String[]{"jpa-entity"}, RulesConfig.resolveRules(domain, RulesConfig.DEFAULT_BUNDLE));
    }

    @Test
    public void suppress_accumulatesProjectAndDomain() {
        System.setProperty(RulesConfig.PROP_SUPPRESS, "jpa-column");
        List<String> merged = RulesConfig.resolveSuppress(Arrays.asList("jpa-id"));
        assertEquals(Arrays.asList("jpa-column", "jpa-id"), merged);
    }

    @Test
    public void endToEnd_projectPropertyDrivesGeneration() {
        // Project-wide config as build.gradle would set it: entity rules only,
        // with the column rule suppressed for the whole project.
        System.setProperty(RulesConfig.PROP_RULES, "jpa-entity");
        System.setProperty(RulesConfig.PROP_SUPPRESS, "jpa-column");

        SourceCode code = RulesConfig
                .engineFor(RuleCatalog.standard(), null, null)
                .run(order());

        String out = code.renderFieldBlock();
        assertTrue(out.contains("@Id"), out);
        assertFalse(out.contains("@Column"), "project-level suppress must apply:\n" + out);
    }

    // ---- what env.yml hands over ------------------------------------------

    /**
     * The config map stringifies whatever YAML parsed, so a list arrives with
     * its brackets. Accepting both that and a plain value keeps a project from
     * having to know which form this reader wanted.
     */
    @Test
    public void yamlList_isReadWithItsBrackets() {
        assertEquals(java.util.Arrays.asList("jpa-springboot-kt"),
                RulesConfig.configuredList("[jpa-springboot-kt]"));
        assertEquals(java.util.Arrays.asList("jpa-entity", "jpa-dto"),
                RulesConfig.configuredList("[jpa-entity, jpa-dto]"));
    }

    @Test
    public void plainValueAndCsv_areReadToo() {
        assertEquals(java.util.Arrays.asList("jpa-springboot-kt"),
                RulesConfig.configuredList("jpa-springboot-kt"));
        assertEquals(java.util.Arrays.asList("jpa-entity", "jpa-dto"),
                RulesConfig.configuredList("jpa-entity, jpa-dto"));
    }

    /**
     * Nothing configured has to read as nothing, not as an empty selection -
     * that is what lets the resolution fall through to the project property
     * and then the default bundle.
     */
    @Test
    public void absentOrBlank_fallsThrough() {
        assertNull(RulesConfig.configuredList(null));
        assertNull(RulesConfig.configuredList(""));
        assertNull(RulesConfig.configuredList("   "));
        assertNull(RulesConfig.configuredList("[]"));

        assertArrayEquals(new String[]{RulesConfig.DEFAULT_BUNDLE},
                RulesConfig.resolveRules(RulesConfig.configuredList(null), RulesConfig.DEFAULT_BUNDLE));
    }

    /** A domain naming the Kotlin bundle in env.yml selects it. */
    @Test
    public void domainCanSelectTheKotlinBundle() {
        assertArrayEquals(new String[]{"jpa-springboot-kt"},
                RulesConfig.resolveRules(
                        RulesConfig.configuredList("[jpa-springboot-kt]"), RulesConfig.DEFAULT_BUNDLE));
    }
}
