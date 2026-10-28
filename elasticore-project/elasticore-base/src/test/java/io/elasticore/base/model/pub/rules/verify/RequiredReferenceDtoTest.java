package io.elasticore.base.model.pub.rules.verify;

import io.elasticore.base.extract.ModelExtractor;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.entity.EntityAnnotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import org.junit.jupiter.api.AfterEach;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A required reference, "post: Post!", in the DTO an @expose entity gets.
 *
 * The request sets the relation through the flattened key, postPostId, and the
 * nested PostDTO is the response's (READ_ONLY in the schema). 2.x requires the
 * nested object and not the key, so a request that sent only the key got a 400.
 * A v3, Kotlin or TypeScript run requires the key and leaves the nested object
 * optional; 2.x and the compatible mode stay as they were.
 */
public class RequiredReferenceDtoTest {

    private static final String BOARD =
            "entity:\n"
            + "  Post:\n"
            + "    meta: entity @root @expose(20) -- post\n"
            + "    fields:\n"
            + "      postId:    long @id @sequence\n"
            + "      title:     string(100)! -- title\n"
            + "  Comment:\n"
            + "    meta: entity @root @expose(20) -- comment\n"
            + "    fields:\n"
            + "      commentId: long @id @sequence\n"
            + "      content:   string(500)! -- comment text\n"
            + "      post:      Post! -- parent post\n"
            + "  Topic:\n"
            + "    meta: entity @root @expose(20) -- topic\n"
            + "    fields:\n"
            + "      id:        long @id @sequence\n"
            + "      name:      string(50)! -- topic name\n"
            + "  Reply:\n"
            + "    meta: entity @root @expose(20) -- reply\n"
            + "    fields:\n"
            + "      replyId:   long @id @sequence\n"
            + "      topic:     Topic! -- topic\n"
            + "      post:      Post -- quoted post\n"
            + "dto:\n"
            + "  CommentView:\n"
            + "    meta: dto @template(Comment)\n"
            + "    fields:\n"
            + "      postId:    long! @ref(post.postId)\n"
            + "      postTitle: string @ref(post.title)\n";

    /** Another aggregate by its id (hexagonal): a key named after the field, and one that is not. */
    private static final String LIBRARY =
            "entity:\n"
            + "  Member:\n"
            + "    meta: entity @root @expose(20) -- member\n"
            + "    fields:\n"
            + "      memberId:  long @id @sequence\n"
            + "      name:      string(50)! -- member name\n"
            + "  Book:\n"
            + "    meta: entity @root @expose(20) -- book\n"
            + "    fields:\n"
            + "      isbn:      string(13) @id -- ISBN\n"
            + "      title:     string(100)! -- title\n"
            + "  Loan:\n"
            + "    meta: entity @root @expose(20) -- loan\n"
            + "    fields:\n"
            + "      loanId:    long @id @sequence\n"
            + "      member:    Member! -- borrower\n"
            + "      book:      Book! -- book\n";

    @TempDir
    Path dir;

    @AfterEach
    void reset() throws Exception {
        SpecFactory.referenceRequiredByKey(false);
        // An in-process load leaves the model process-wide; other tests expect none.
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
    }

    @Test
    public void v3LayeredRequiresTheKeyAndNotTheNestedObject() throws Exception {
        Path project = project("v3", BOARD);
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.rules.v3=true",
                "-Delasticore.architecture=layered", "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");

        String comment = read(dto(project, "CommentDTO"));
        String post = block(comment, "private PostDTO post;");
        assertTrue(post.contains("@Schema(description = \"parent post\" , accessMode = Schema.AccessMode.READ_ONLY )"), post);
        assertFalse(post.contains("@NotNull"), post);
        assertFalse(post.contains("RequiredMode.REQUIRED"), post);
        String key = block(comment, "private Long postPostId;");
        assertTrue(key.contains("@Schema(description = \"postPostId\" , requiredMode=Schema.RequiredMode.REQUIRED )"), key);
        assertTrue(key.contains("@NotNull"), key);

        // A key named id flattens to topicId, and is required the same way;
        // an optional reference beside it stays optional.
        String reply = read(dto(project, "ReplyDTO"));
        String topicKey = block(reply, "private Long topicId;");
        assertTrue(topicKey.contains("@NotNull"), topicKey);
        assertTrue(topicKey.contains("requiredMode=Schema.RequiredMode.REQUIRED"), topicKey);
        assertFalse(block(reply, "private TopicDTO topic;").contains("@NotNull"), reply);
        String quoted = block(reply, "private Long postPostId;");
        assertFalse(quoted.contains("@NotNull"), quoted);
        assertFalse(quoted.contains("RequiredMode.REQUIRED"), quoted);

        // The search DTO shares the key field and searches by it, required or not.
        String search = read(dto(project, "CommentSrchDTO"));
        assertFalse(search.contains("PostDTO post"), search);
        String searchKey = block(search, "private Long postPostId;");
        assertFalse(searchKey.contains("@NotNull"), searchKey);
        assertFalse(searchKey.contains("RequiredMode.REQUIRED"), searchKey);

        // A hand-declared DTO decides for itself: its own "!" on the key, and a
        // @ref to the title is no key anyway.
        String view = read(dto(project, "CommentView"));
        assertTrue(block(view, "private Long postId;").contains("@NotNull"), view);
        assertFalse(block(view, "private String postTitle;").contains("@NotNull"), view);

        // The entity is unchanged: the column is NOT NULL.
        assertTrue(read(entity(project, "Comment")).contains("nullable = false"), read(entity(project, "Comment")));
    }

    @Test
    public void hexagonalRequiresTheIdItReferencesBy() throws Exception {
        Path project = project("hexagonal", LIBRARY);
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=hexagonal",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");

        String loan = read(dto(project, "LoanDTO"));
        String memberId = block(loan, "private Long memberId;");
        assertTrue(memberId.contains("@Schema(description = \"memberId\" , requiredMode=Schema.RequiredMode.REQUIRED )"), memberId);
        assertTrue(memberId.contains("@NotNull"), memberId);
        String bookIsbn = block(loan, "private String bookIsbn;");
        assertTrue(bookIsbn.contains("@Schema(description = \"bookIsbn\" , requiredMode=Schema.RequiredMode.REQUIRED )"), bookIsbn);
        assertTrue(bookIsbn.contains("@NotNull"), bookIsbn);

        String member = block(loan, "private MemberDTO member;");
        assertTrue(member.contains("@Schema(description = \"borrower\" , accessMode = Schema.AccessMode.READ_ONLY )"), member);
        assertFalse(member.contains("@NotNull"), member);
        assertFalse(member.contains("RequiredMode.REQUIRED"), member);
        String book = block(loan, "private BookDTO book;");
        assertFalse(book.contains("@NotNull"), book);
        assertFalse(book.contains("RequiredMode.REQUIRED"), book);

        // The key once: the identity, not the flattened copy of it.
        assertFalse(loan.contains("memberMemberId"), loan);
    }

    @Test
    public void theCompatibleModeAnd2xKeepRequiringTheNestedObject() throws Exception {
        Path compatible = project("compatible", BOARD, env2x());
        // Layered Java is the v3 code by default since 3.0.261028-rc3; the
        // 2.x-compatible code is asked for, as output = 'v2' asks.
        generate(compatible, RulesGenRunner.class.getName(), "-Delasticore.architecture=layered",
                "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8", "-Delasticore.rules.v2=true");
        Path v2 = project("v2", BOARD, env2x());
        generate(v2, ModelExtractor.class.getName());

        for (Path project : new Path[]{compatible, v2}) {
            String comment = read(dto(project, "CommentDTO"));
            String post = block(comment, "private PostDTO post;");
            assertTrue(post.contains("requiredMode=Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY"), post);
            assertTrue(post.contains("@NotNull"), post);
            String key = block(comment, "private Long postPostId;");
            assertTrue(key.contains("@Schema(description = \"postPostId\"  )"), key);
            assertFalse(key.contains("@NotNull"), key);
        }
        // The compatible mode is the 2.x output, but for the generation stamp.
        for (String name : new String[]{"CommentDTO", "CommentSrchDTO", "ReplyDTO", "CommentView"}) {
            assertEquals(unstamped(read(dto(v2, name))), unstamped(read(dto(compatible, name))), name);
        }
    }

    /** The source without its "//ecd:" line, which carries the time it was written. */
    private static String unstamped(String src) {
        return src.replaceAll("(?m)^//ecd:.*\n", "");
    }

    @Test
    public void kotlinPutsTheConstraintOnTheKey() throws Exception {
        Path project = project("kotlin", BOARD);
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.architecture=layered",
                "-Delasticore.language=kotlin", "-Delasticore.springBoot=4.0.8");

        String comment = read(kotlinDto(project, "CommentDTO"));
        // The types stay nullable: the constraint moves, the declaration does not.
        String post = block(comment, "var post: PostDTO? = null");
        assertTrue(post.contains("@Schema(description = \"parent post\" , accessMode = Schema.AccessMode.READ_ONLY )"), post);
        assertFalse(post.contains("NotNull"), post);
        String key = block(comment, "var postPostId: Long? = null");
        assertTrue(key.contains("@NotNull"), key);
        assertTrue(key.contains("@Schema(description = \"postPostId\" , requiredMode=Schema.RequiredMode.REQUIRED )"), key);
    }

    @Test
    public void typeScriptDeclaresTheKeyAndNotTheNestedObjectRequired() throws Exception {
        Path project = project("typescript", BOARD);
        generate(project, RulesGenRunner.class.getName(), "-Delasticore.rules.ts=true",
                "-Delasticore.architecture=layered", "-Delasticore.language=java", "-Delasticore.springBoot=4.0.8");

        String comment = read(tsFile(project, "CommentDTO"));
        assertTrue(comment.contains("    post?: PostDTO;\n"), comment);
        assertTrue(comment.contains("    postPostId: number;\n"), comment);
        // The client's check no longer asks for the nested object.
        assertTrue(comment.contains("    post: { label: \"parent post\" },\n"), comment);
        assertFalse(comment.contains("errors.post ="), comment);
    }

    /**
     * The key field is one object in the DTO and the search DTO of an entity
     * (they share their field list), so the main DTO requires a copy of it:
     * the search DTO's stays optional whichever of the two is built first.
     */
    @Test
    public void theSearchDtosKeyStaysOptionalWhicheverIsBuiltFirst() throws Exception {
        for (boolean searchFirst : new boolean[]{false, true}) {
            Path project = project(searchFirst ? "search-first" : "main-first", BOARD);
            BlueprintLoader loader = BlueprintLoader.load(project.resolve("src/main/resources/blueprint").toString());
            ECoreModel model = loader.allModels().get(0);
            DataTransfer main = model.getDataTransferModels().findByName("CommentDTO");
            DataTransfer search = model.getDataTransferModels().findByName("CommentSrchDTO");
            assertNotNull(main);
            assertNotNull(search);

            SpecFactory.referenceRequiredByKey(true);
            EntitySpec searchSpec;
            EntitySpec mainSpec;
            if (searchFirst) {
                searchSpec = SpecFactory.fromDto(search, model, "board");
                mainSpec = SpecFactory.fromDto(main, model, "board");
            } else {
                mainSpec = SpecFactory.fromDto(main, model, "board");
                searchSpec = SpecFactory.fromDto(search, model, "board");
            }
            EntitySpec searchAgain = SpecFactory.fromDto(search, model, "board");

            String order = searchFirst ? "search DTO first" : "main DTO first";
            assertTrue(field(mainSpec, "postPostId").required(), order);
            assertFalse(field(mainSpec, "post").required(), order);
            assertFalse(field(searchSpec, "postPostId").required(), order);
            assertFalse(field(searchAgain, "postPostId").required(), order);
            io.elasticore.base.model.core.ListMap<String, Field> searchFields = search.getAllFieldListMap();
            Field shared = searchFields.get("postPostId");
            assertFalse(shared.hasAnnotation(EntityAnnotation.NOT_NULL), order);
            reset();
        }
    }

    // ------------------------------------------------------------------------------------------------

    private static FieldSpec field(EntitySpec spec, String name) {
        for (FieldSpec f : spec.fields()) {
            if (f.name().equals(name)) return f;
        }
        throw new AssertionError(name + " is not a field of " + spec.fields().stream()
                .map(FieldSpec::name).collect(Collectors.toList()));
    }

    /** A declaration's line with the annotation and comment lines right above it. */
    private static String block(String src, String declaration) {
        String[] lines = src.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].contains(declaration)) continue;
            int start = i;
            while (start > 0) {
                String above = lines[start - 1].trim();
                if (above.startsWith("@") || above.startsWith("/*") || above.startsWith("*")
                        || above.startsWith("//")) {
                    start--;
                } else {
                    break;
                }
            }
            StringBuilder out = new StringBuilder();
            for (int j = start; j <= i; j++) out.append(lines[j]).append('\n');
            return out.toString();
        }
        throw new AssertionError("no \"" + declaration + "\" in:\n" + src);
    }

    /** The rule engine's own runs: one base package. */
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

    /** A 2.x project, which the compatible mode generates the same way. */
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

    private Path project(String name, String model) throws IOException {
        return project(name, model, env3());
    }

    private Path project(String name, String model, String env) throws IOException {
        Path project = dir.resolve(name);
        Path domain = project.resolve("src/main/resources/blueprint/board");
        Files.createDirectories(domain.resolve("model"));
        Files.write(domain.resolve("env.yml"), env.getBytes(StandardCharsets.UTF_8));
        Files.write(domain.resolve("model/entity.yml"), model.getBytes(StandardCharsets.UTF_8));
        return project;
    }

    /** The generated DTO, wherever the architecture put it. */
    private static Path dto(Path project, String name) throws IOException {
        return find(project, "src/main/java", name + ".java", "/dto/");
    }

    private static Path entity(Path project, String name) throws IOException {
        return find(project, "src/main/java", name + ".java", "/entity/");
    }

    private static Path kotlinDto(Path project, String name) throws IOException {
        return find(project, "src/main/kotlin", name + ".kt", "/dto/");
    }

    private static Path tsFile(Path project, String name) throws IOException {
        Path ts = project.resolve("src/main/ts");
        try (Stream<Path> files = Files.walk(ts)) {
            List<Path> all = files.filter(Files::isRegularFile).sorted().collect(Collectors.toList());
            for (Path f : all) {
                if (read(f).contains("export interface " + name + " ")) return f;
            }
            throw new AssertionError("no interface " + name + " under " + ts + ": " + all);
        }
    }

    private static Path find(Path project, String root, String fileName, String folder) throws IOException {
        Path base = project.resolve(root);
        try (Stream<Path> files = Files.walk(base)) {
            List<Path> all = files.filter(Files::isRegularFile).sorted().collect(Collectors.toList());
            for (Path f : all) {
                String p = f.toString().replace('\\', '/');
                if (f.getFileName().toString().equals(fileName) && p.contains(folder)) return f;
            }
            for (Path f : all) {
                if (f.getFileName().toString().equals(fileName)) return f;
            }
            throw new AssertionError("no " + fileName + " under " + base + ":\n" + all.stream()
                    .map(x -> base.relativize(x).toString()).collect(Collectors.joining("\n")));
        }
    }

    /** Generates in a JVM of its own, as the Gradle tasks do: both generators keep static state. */
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

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
