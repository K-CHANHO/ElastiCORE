package io.elasticore.base.util;

import io.elasticore.base.extract.ModelExtractor;
import io.elasticore.base.model.pub.rules.verify.RulesGenRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wizard's output, generated the way the user will generate it.
 *
 * Every other check on the wizard reads the file it wrote. That catches a
 * misspelled bundle and a missing namespace, and it would still pass if the
 * env.yml were unreadable to the loader, or readable and produced nothing.
 * The only way to know a starting configuration works is to start from it.
 *
 * It is also the check that would have caught the generated tree escaping to
 * the repository root: that bug survived every gate for a week because the
 * golden comparison reads file contents and never asks where a file is. Here
 * the whole assertion is where the files are.
 */
public class InitializedProjectTest {

    @Test
    @DisplayName("a layered project generates the layout it was promised")
    public void aLayeredProjectGenerates() throws Exception {
        List<String> generated = generateFrom(0, false);

        assertTrue(generated.contains("com/example/domain/demo/entity/User.java"), generated.toString());
        assertTrue(generated.contains("com/example/domain/demo/dto/UserDTO.java"), generated.toString());
        assertTrue(generated.contains("com/example/domain/demo/repository/UserRepository.java"), generated.toString());
        assertTrue(generated.contains("com/example/domain/demo/enums/UserStatus.java"), generated.toString());
    }

    @Test
    @DisplayName("a hexagonal project lays its artifacts out in layers")
    public void aHexagonalProjectIsLayered() throws Exception {
        List<String> generated = generateFrom(2, false);

        // Each of these is a namespace key the wizard had to know to write.
        // An unnamed key does not fail - it falls back - so the layering the
        // user chose silently does not happen and nothing says why.
        assertTrue(generated.contains("com/example/demo/domain/model/User.java"), generated.toString());
        assertTrue(generated.contains("com/example/demo/application/dto/UserDTO.java"), generated.toString());
        assertTrue(generated.contains("com/example/demo/interfaces/rest/UserController.java"), generated.toString());

        // Spring Data belongs to infrastructure. If this landed in the domain,
        // the innermost layer would name a persistence type and the inversion
        // the bundle exists for would not have happened.
        assertTrue(generated.contains("com/example/demo/infrastructure/persistence/UserRepository.java"),
                generated.toString());
    }

    @Test
    @DisplayName("the rule engine reads the wizard's bundle and produces what it names")
    public void theBundleTheWizardWroteIsHonoured() throws Exception {
        List<String> generated = generateFrom(2, true);

        // publisher.rules is read by the rule engine, and the engine is a
        // separate entry point from the annotation processor - so a bundle
        // the wizard writes is inert until the project runs it. This is the
        // check that the configuration itself is right: given the engine, the
        // bundle resolves and its artifacts land in the packages the wizard
        // named. Whether a new project can reach the engine at all is a
        // different question, and the wizard now answers it in prose.
        assertTrue(anyUnder(generated, "com/example/demo/application/port/in/"),
                "the hexagonal bundle produced no inbound port: " + generated);
        assertTrue(anyUnder(generated, "com/example/demo/domain/repository/"),
                "the domain owns no repository port: " + generated);
    }

    @Test
    @DisplayName("the rule engine carries the extractor's side-work: schema and validation")
    public void theRuleEngineIsNotADowngrade() throws Exception {
        List<String> everything = generateFrom(2, true, false);

        // Choosing a bundle used to silently cost the project its schema.yml
        // and its global type validation - the extractor ran both and the rule
        // engine ran neither, so opting into v3 was a downgrade nobody had
        // announced. The evidence was in this repository's own samples: both
        // rule-engine modules had no schema.yml at all.
        assertTrue(everything.contains("src/main/resources/blueprint/schema.yml"),
                "the rule engine wrote no schema.yml: " + everything);
    }

    private boolean anyUnder(List<String> paths, String prefix) {
        for (String path : paths) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    @Test
    @DisplayName("nothing is written outside the project the wizard created")
    public void generationStaysInsideTheProject() throws Exception {
        File project = Files.createTempDirectory("elcore-project").toFile();
        try {
            List<String> everything = generateInto(project, 2, false);

            List<String> stray = new ArrayList<String>();
            for (String path : everything) {
                boolean expected = path.startsWith("src/main/java/")
                        || path.startsWith("src/main/resources/blueprint/");
                if (!expected) stray.add(path);
            }

            // The generated tree once appeared at the repository root of the
            // project being built, 227 files of it, because the writer was
            // handed the wrong root. Nothing noticed, because nothing looked.
            assertTrue(stray.isEmpty(), "generation wrote outside src/: " + stray);
        } finally {
            deleteTree(project);
        }
    }

    /** Java sources the wizard's own configuration produces, relative to src/main/java. */
    private List<String> generateFrom(int architecture, boolean throughTheRuleEngine) throws Exception {
        return generateFrom(architecture, throughTheRuleEngine, true);
    }

    private List<String> generateFrom(int architecture, boolean throughTheRuleEngine,
                                      boolean sourcesOnly) throws Exception {
        File project = Files.createTempDirectory("elcore-project").toFile();
        try {
            List<String> all = generateInto(project, architecture, throughTheRuleEngine);
            if (!sourcesOnly) return all;

            List<String> sources = new ArrayList<String>();
            for (String path : all) {
                if (path.startsWith("src/main/java/") && path.endsWith(".java")) {
                    sources.add(path.substring("src/main/java/".length()));
                }
            }
            assertTrue(!sources.isEmpty(), "the wizard's configuration generated nothing at all");
            return sources;
        } finally {
            deleteTree(project);
        }
    }

    /**
     * Runs the wizard and then the generator, in one directory.
     *
     * The wizard's own createDomainEnvironment resolves against the process
     * working directory, which a test cannot change, so the two files it
     * writes are produced directly. Everything downstream of them - the
     * loading, the placement, the writing - is the real path.
     */
    private List<String> generateInto(File project, int architecture, boolean throughTheRuleEngine)
            throws Exception {
        File blueprint = new File(project, "src/main/resources/blueprint/demo");
        File model = new File(blueprint, "model");
        assertTrue(model.mkdirs(), "could not lay out the temporary project");

        InitializationHelper wizard = new InitializationHelper();

        Method env = InitializationHelper.class.getDeclaredMethod("generateEnvFile",
                File.class, String.class, String.class, int.class, boolean.class);
        env.setAccessible(true);
        env.invoke(wizard, new File(blueprint, "env.yml"), "demo", "com.example", architecture, false);

        Method sample = InitializationHelper.class.getDeclaredMethod("generateSampleModelFile",
                File.class, int.class);
        sample.setAccessible(true);
        sample.invoke(wizard, new File(model, "model.yml"), architecture);

        if (throughTheRuleEngine) {
            runTheRuleEngine(project);
        } else {
            // null is what the CLI passes: the publishers are handed a factory
            // built from the project root either way, so this is the path a
            // user takes rather than one arranged for the test.
            new ModelExtractor(new File(project, "src/main/resources/blueprint").getAbsolutePath())
                    .extract(null);
        }

        return filesUnder(project);
    }

    /**
     * The engine that reads publisher.rules, driven the way the Gradle task drives it.
     *
     * Its configuration is system properties, which are global, so they are
     * put back afterwards - a leaked elasticore.base.path would point every
     * later test at a directory this one deleted.
     */
    private void runTheRuleEngine(File project) {
        String basePath = System.getProperty("elasticore.base.path");
        String v3 = System.getProperty("elasticore.rules.v3");
        String force = System.getProperty("elasticore.rules.force");
        try {
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.rules.v3", "true");
            System.setProperty("elasticore.rules.force", "true");

            RulesGenRunner.main(new String[0]);
        } finally {
            restore("elasticore.base.path", basePath);
            restore("elasticore.rules.v3", v3);
            restore("elasticore.rules.force", force);
        }
    }

    private void restore(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    private List<String> filesUnder(File root) throws Exception {
        final Path base = root.toPath();
        final List<String> out = new ArrayList<String>();

        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                out.add(base.relativize(file).toString().replace('\\', '/'));
                return FileVisitResult.CONTINUE;
            }
        });
        return out;
    }

    private void deleteTree(File root) throws Exception {
        if (!root.exists()) return;
        Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws java.io.IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, java.io.IOException e) throws java.io.IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
