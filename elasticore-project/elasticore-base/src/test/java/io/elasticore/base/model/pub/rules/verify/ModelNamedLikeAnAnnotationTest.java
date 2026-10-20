package io.elasticore.base.model.pub.rules.verify;

import io.elasticore.base.extract.ModelExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An entity named like an annotation the generated code uses - a board's
 * Comment and Hibernate's @Comment, which every field with a "-- label" gets.
 *
 * The rule engine imported org.hibernate.annotations.Comment into the entity
 * files, and that import shadowed the entity of the same package: PostBase's
 * List of Comment meant the annotation, and nothing compiled. The 2.x
 * templates import org.hibernate.annotations.*, which loses to the entity, so
 * there @Comment("...") named the entity and did not compile either. Found by
 * the first two boards anyone built with the agent kit.
 *
 * Each generator writes the annotation in full where the model has a type of
 * its name, and by its simple name as before where it does not.
 */
public class ModelNamedLikeAnAnnotationTest {

    private static final String BOARD =
            "entity:\n"
            + "  Post:\n"
            + "    meta: entity @root -- post\n"
            + "    fields:\n"
            + "      postId:    long @id @sequence\n"
            + "      title:     string(100)! -- title\n"
            + "  Comment:\n"
            + "    meta: entity @root -- comment\n"
            + "    fields:\n"
            + "      commentId: long @id @sequence\n"
            + "      content:   string(500)! -- comment text\n"
            + "      post:      Post! -- parent post\n";

    /** The same board with the comment called something no annotation is called. */
    private static final String CONTROL = BOARD.replace("Comment:", "Reply:").replace("commentId", "replyId");

    private static final Pattern SIMPLE_COMMENT = Pattern.compile("(?m)^\\s*@Comment\\(");
    private static final String COMMENT_IMPORT = "import org.hibernate.annotations.Comment;";

    @TempDir
    Path dir;

    @Test
    public void the2xGeneratorWritesHibernatesCommentInFull() throws Exception {
        Path project = project("v2", BOARD, env2x());
        generate(project, ModelExtractor.class.getName());
        String comment = read(project.resolve("src/main/java/io/board/entity/Comment.java"));
        assertTrue(comment.contains("@org.hibernate.annotations.Comment(\"comment text\")"), comment);
        assertNoSimpleComment(project);
    }

    @Test
    public void theRuleEngineWritesItInFullOnLayeredJava() throws Exception {
        Path project = project("layered", BOARD, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=layered",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"comment text\")"), tree(project));
        assertNoSimpleComment(project);
    }

    @Test
    public void theRuleEngineWritesItInFullOnHexagonal() throws Exception {
        Path project = project("hexagonal", BOARD, env3());
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"comment text\")"), tree(project));
        assertTrue(anyFileContains(project, "@org.hibernate.annotations.Comment(\"title\")"),
                "Post refers to the Comment entity too, so its own labels are written in full\n" + tree(project));
        assertNoSimpleComment(project);
    }

    @Test
    public void withoutSuchAnEntityTheAnnotationStaysShort() throws Exception {
        Path v2 = project("control-v2", CONTROL, env2x());
        generate(v2, ModelExtractor.class.getName());
        assertTrue(read(v2.resolve("src/main/java/io/board/entity/Reply.java")).contains("@Comment(\"comment text\")"));

        Path v3 = project("control-v3", CONTROL, env3());
        generate(v3, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");
        assertTrue(anyFileContains(v3, COMMENT_IMPORT), tree(v3));
        assertFalse(anyFileContains(v3, "@org.hibernate.annotations.Comment("), tree(v3));
    }

    // ------------------------------------------------------------------------------------------------

    private static String env2x() {
        return "elasticore:\n"
                + "  enable: true\n"
                + "  config:\n"
                + "    domainName: board\n"
                + "    framework: springboot\n"
                + "    mode: jpa\n"
                + "    j2ee: jakarta\n"
                + "  namespace:\n"
                + "    entity: io.board.entity\n"
                + "    dto: io.board.dto\n"
                + "    enumeration: io.board.enums\n"
                + "    repository: io.board.repository\n"
                + "    service: io.board.service\n"
                + "    control: io.board.controller\n";
    }

    private static String env3() {
        return "elasticore:\n"
                + "  enable: true\n"
                + "  config:\n"
                + "    domainName: board\n"
                + "    framework: springboot\n"
                + "    mode: jpa\n"
                + "    j2ee: jakarta\n"
                + "    apiPath: \"/api\"\n"
                + "  namespace:\n"
                + "    base: io.board\n";
    }

    private Path project(String name, String model, String env) throws IOException {
        Path project = dir.resolve(name);
        Path domain = project.resolve("src/main/resources/blueprint/board");
        Files.createDirectories(domain.resolve("model"));
        Files.write(domain.resolve("env.yml"), env.getBytes(StandardCharsets.UTF_8));
        Files.write(domain.resolve("model/entity.yml"), model.getBytes(StandardCharsets.UTF_8));
        return project;
    }

    /**
     * Generates in a JVM of its own, as the Gradle tasks do: both generators keep the loaded model in
     * static state that other tests here expect empty.
     */
    private static void generate(Path project, String mainClass, String... properties) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Xmx384m");
        command.add("-Dfile.encoding=UTF-8");
        command.add("-Delasticore.base.path=" + project);
        for (String p : properties) command.add(p);
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(mainClass);
        Path log = project.resolve("gen.log");
        Process p = new ProcessBuilder(command).directory(project.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(p.waitFor(180, TimeUnit.SECONDS), "generation did not finish");
        } finally {
            p.destroyForcibly();
        }
        assertEquals(0, p.exitValue(), read(log));
    }

    private static void assertNoSimpleComment(Path project) throws IOException {
        for (Path file : javaFiles(project)) {
            String src = read(file);
            assertFalse(src.contains(COMMENT_IMPORT), file + " imports Hibernate's Comment over the entity:\n" + src);
            assertFalse(SIMPLE_COMMENT.matcher(src).find(), file + " writes @Comment, which names the entity:\n" + src);
        }
    }

    private static boolean anyFileContains(Path project, String text) throws IOException {
        for (Path file : javaFiles(project)) {
            if (read(file).contains(text)) return true;
        }
        return false;
    }

    private static List<Path> javaFiles(Path project) throws IOException {
        Path java = project.resolve("src/main/java");
        if (!Files.isDirectory(java)) return new ArrayList<Path>();
        try (Stream<Path> files = Files.walk(java)) {
            return files.filter(f -> f.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
    }

    private static String tree(Path project) throws IOException {
        StringBuilder sb = new StringBuilder("generated:\n");
        for (Path f : javaFiles(project)) sb.append("  ").append(project.relativize(f)).append('\n');
        return sb.append(read(project.resolve("gen.log"))).toString();
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
