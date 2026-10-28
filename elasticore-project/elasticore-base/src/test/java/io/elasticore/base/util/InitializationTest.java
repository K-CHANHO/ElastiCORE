package io.elasticore.base.util;

import io.elasticore.base.model.pub.rules.RuleCatalog;
import io.elasticore.base.model.pub.rules.RulesConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The first file a new project ever has, checked against the engine that reads it.
 *
 * The wizard writes somebody's env.yml before they have read anything, so it
 * is the only teacher a new project starts with - and until 3.0.260829 it
 * taught 2.x. It named no bundle, so the rule engine was reachable only by
 * someone who had already found the guide, which is backwards: the generated
 * file is what should be sending them there.
 *
 * What is checked here is what the wizard cannot check for itself. A bundle
 * name it emits has to be one the catalog has, or the project fails on its
 * first build with a name nobody typed. And a bundle that places artifacts by
 * a namespace key the wizard did not write does not fail at all - the artifact
 * lands in a fallback package, and the layering the user chose silently
 * doesn't happen. That second one is the reason this file exists.
 */
public class InitializationTest {

    /** Namespace keys the domain-model bundles place artifacts by. */
    private static final String[] DOMAIN_MODEL_KEYS = {
            "entity", "enumeration", "repository", "domainRepository", "persistence",
            "event", "domainService", "service", "dto", "control", "port"};

    @Test
    @DisplayName("every bundle the wizard can write is one the catalog has")
    public void theBundlesAreReal() throws Exception {
        Set<String> known = new LinkedHashSet<String>(RuleCatalog.standard().bundleNames());
        List<String> unknown = new ArrayList<String>();

        for (String[] byLanguage : bundles()) {
            for (String bundle : byLanguage) {
                if (!known.contains(bundle)) unknown.add(bundle);
            }
        }

        // A bundle name that does not resolve is a project that fails on its
        // first build citing a name its author never typed.
        assertTrue(unknown.isEmpty(), "the wizard offers " + unknown + "; the catalog has " + known);
    }

    @Test
    @DisplayName("a layered project gets the layout every existing project has")
    public void theLayeredShapeIsUnchanged() throws Exception {
        String env = envFor(0, false);

        assertTrue(env.contains("rules: [jpa-springboot]"), env);
        for (String key : new String[]{
                "entity", "dto", "enumeration", "repository", "service", "control", "port"}) {
            assertTrue(env.contains(nl() + "    " + key + ": "), "missing namespace " + key + ":" + nl() + env);
        }
        // The default has to stay the default: a new project that says nothing
        // about architecture must generate what 2.x generated.
        assertTrue(env.contains("com.example.domain.demo.entity"), env);
    }

    @Test
    @DisplayName("choosing a domain model places every artifact that bundle produces")
    public void theDomainModelShapeNamesEveryLayer() throws Exception {
        for (int architecture : new int[]{1, 2}) {
            String env = envFor(architecture, false);
            List<String> missing = new ArrayList<String>();

            for (String key : DOMAIN_MODEL_KEYS) {
                if (!env.contains(nl() + "    " + key + ": ")) missing.add(key);
            }

            // This is the failure the wizard could not see: an unnamed key
            // does not error, it falls back - so the domain model the user
            // asked for arrives in flat packages and nothing says why.
            assertTrue(missing.isEmpty(), "architecture " + architecture + " names no package for " + missing);

            assertTrue(env.contains(".domain.model" + nl()), "the entity is not in the domain layer:" + nl() + env);
            assertTrue(env.contains(".infrastructure.persistence" + nl()),
                    "Spring Data is not in infrastructure:" + nl() + env);
        }
    }

    @Test
    @DisplayName("only the hexagonal choice names an inbound port")
    public void theInboundPortBelongsToTheHexagon() throws Exception {
        assertTrue(envFor(2, false).contains("    inboundPort: "), "hexagonal names no inbound port");

        // The inbound port is what hexagonal adds over the domain model. If
        // both wrote it, the choice between them would be cosmetic.
        assertTrue(!envFor(1, false).contains("    inboundPort: "),
                "the domain-model bundle generates no inbound port, so naming a package for one misleads");
        assertTrue(!envFor(0, false).contains("    inboundPort: "), "layered names an inbound port");
    }

    @Test
    @DisplayName("choosing Kotlin selects the Kotlin bundle of the same architecture")
    public void kotlinKeepsTheArchitecture() throws Exception {
        assertTrue(envFor(0, true).contains("rules: [jpa-springboot-kt]"), envFor(0, true));
        assertTrue(envFor(1, true).contains("rules: [ddd-springboot-kt]"), envFor(1, true));
        assertTrue(envFor(2, true).contains("rules: [hexagonal-springboot-kt]"), envFor(2, true));

        // A Java bundle in a Kotlin project writes .java files into a Kotlin
        // source set - a failure this codebase has already had once.
        assertTrue(!envFor(2, true).contains("rules: [hexagonal-springboot]"), "a Java bundle leaked into Kotlin");
    }

    @Test
    @DisplayName("the file offers the architectures it did not write")
    public void theAlternativesAreListed() throws Exception {
        String env = envFor(0, false);

        // The wizard asks once. Everything after that is somebody reading
        // their own env.yml, so the choice has to still be visible in it.
        // The other two architectures, and since 3.0.261028, when layered Java
        // became the v3 bundle, the code 2.x generates.
        assertEquals(3, countOf(env, "      #rules: ["),
                "the other two architectures and 2.x's code are not offered in the generated file:" + nl() + env);
        assertTrue(env.contains("      rules: [jpa-springboot-v3]"), env);
        assertTrue(env.contains("      #rules: [jpa-springboot]   # the code 2.x generates"), env);
        assertTrue(env.contains("elasticore-rule-guide.md"), "the file does not say where the bundles are explained");
    }

    @Test
    @DisplayName("the setup it prints matches the language it was asked for")
    public void theSetupNamesTheLanguageSwitch() throws Exception {
        // The bundle chooses the rules; a separate system property chooses the
        // tree and the extension. Printing the task without the second one
        // hands a Kotlin project a command that writes .java - which the rule
        // guide warns about, so the tool making that mistake itself is worse
        // than a user making it.
        assertTrue(setupFor(true).contains("language     = 'kotlin'"),
                "a Kotlin project is told to configure Java:" + nl() + setupFor(true));
        assertTrue(setupFor(false).contains("language     = 'java'"), setupFor(false));

        // The setup prints the plugin, not a hand-written task. It printed a
        // JavaExec block for one release - the wizard teaching every new user
        // the approach the same release had replaced - and an audit caught it.
        assertTrue(setupFor(false).contains("id 'io.elasticore'"),
                "the setup does not apply the plugin:" + nl() + setupFor(false));
        assertTrue(!setupFor(false).contains("JavaExec"),
                "the setup still hand-writes the task the plugin replaced");

        // And it has to say the two entry points are exclusive, because
        // nothing in a build will say it for them.
        assertTrue(setupFor(false).contains("@ElastiCore"),
                "the setup does not mention the annotation it replaces");
    }

    /** What the wizard prints when a bundle was chosen. */
    @Test
    @DisplayName("a layered project is told to run the rule engine its env.yml now names")
    public void theLayeredSetupRunsTheRuleEngine() throws Exception {
        // Since 3.0.261028 the layered env.yml names jpa-springboot-v3. The
        // setup used to say "run ./gradlew elcore again" - the 2.x generator,
        // which reads no bundle - so the project got 2.x's code under an
        // env.yml that said otherwise.
        String setup = setupFor(false, 0);
        assertTrue(setup.contains("architecture = 'layered'"), setup);
        assertTrue(setup.contains("./gradlew elcore3"), setup);
        assertTrue(setup.contains("output = 'v2'"), setup);
        assertTrue(envFor(0, false).contains("rule engine"), envFor(0, false));
    }

    private String setupFor(boolean kotlin) throws Exception {
        return setupFor(kotlin, 1);
    }

    private String setupFor(boolean kotlin, int architecture) throws Exception {
        java.io.PrintStream real = System.out;
        java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
        try {
            System.setOut(new java.io.PrintStream(captured, true, "UTF-8"));

            Method m = InitializationHelper.class.getDeclaredMethod("printRuleEngineSetup",
                    boolean.class, int.class);
            m.setAccessible(true);
            m.invoke(new InitializationHelper(), kotlin, architecture);
        } finally {
            System.setOut(real);
        }
        return new String(captured.toByteArray(), StandardCharsets.UTF_8);
    }

    /** The env.yml the wizard would write, without running the prompts. */
    private String envFor(int architecture, boolean kotlin) throws Exception {
        File dir = Files.createTempDirectory("elcore-init").toFile();
        File env = new File(dir, "env.yml");
        try {
            Method m = InitializationHelper.class.getDeclaredMethod("generateEnvFile",
                    File.class, String.class, String.class, int.class, boolean.class);
            m.setAccessible(true);
            m.invoke(new InitializationHelper(), env, "demo", "com.example", architecture, kotlin);

            return new String(Files.readAllBytes(env.toPath()), StandardCharsets.UTF_8);
        } finally {
            env.delete();
            dir.delete();
        }
    }

    /**
     * Every bundle the wizard can reach, composed the way it composes them.
     *
     * Read from RulesConfig rather than from a field of the wizard, because
     * the table moved there: it had been written twice, once here and once as
     * the runner's defaults, which is how a wizard and a build come to
     * disagree about what a project is.
     */
    private String[][] bundles() {
        RulesConfig.Architecture[] architectures = RulesConfig.Architecture.values();
        String[][] out = new String[architectures.length][2];

        for (int i = 0; i < architectures.length; i++) {
            out[i][0] = RulesConfig.bundleFor(architectures[i], RulesConfig.Language.JAVA);
            out[i][1] = RulesConfig.bundleFor(architectures[i], RulesConfig.Language.KOTLIN);
        }
        return out;
    }

    @Test
    @DisplayName("the TypeScript bundle takes no architecture, and says so by being one")
    public void typescriptHasOneBundle() {
        // An aggregate boundary is a statement about writes on a server. A
        // browser model has no writes to bound, so three TypeScript bundles
        // would be three names for the same file set.
        String layered = RulesConfig.bundleFor(
                RulesConfig.Architecture.LAYERED, RulesConfig.Language.TYPESCRIPT);
        for (RulesConfig.Architecture a : RulesConfig.Architecture.values()) {
            assertEquals(layered, RulesConfig.bundleFor(a, RulesConfig.Language.TYPESCRIPT),
                    "TypeScript should not vary by architecture");
        }
        assertTrue(new LinkedHashSet<String>(RuleCatalog.standard().bundleNames()).contains(layered),
                "the TypeScript bundle is not one the catalog has: " + layered);
    }

    private int countOf(String text, String needle) {
        int n = 0, at = text.indexOf(needle);
        while (at >= 0) {
            n++;
            at = text.indexOf(needle, at + 1);
        }
        return n;
    }

    /** The wizard writes \n; the assertions have to look for the same thing. */
    private String nl() {
        return "\n";
    }
}
