package io.elasticore.base.model.pub.rules.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.elasticore.base.model.pub.rules.schema.DdlConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * {@code @signin(refresh=request)}: the entity that signs in gets a filter that reads
 * the signed-in row again on each request (issue 181, item 1). What the
 * filter does to a session is SessionRefreshFilterTest's (the runtime); what
 * a request sees is the pet store's ShopFlowTest.
 */
public class SignInRefreshTest {

    private static final String MODEL = String.join(
            "\n",
            "enumeration:",
            "  RfRole:",
            "    meta: enum @db(code) @json(code)",
            "    fields:",
            "      code: string(1) @code",
            "    enum:",
            "      USER:  U",
            "      ADMIN: A",
            "",
            "entity:",
            "  RfMember:",
            "    meta: entity @root @editable @expose(20) @signin(refresh=request) @owner(memberId)"
                    + " @access(read=owner, create=public, update=owner)",
            "    fields:",
            "      memberId: long @id @sequence",
            "      email:    string(100)! @login @unique",
            "      secret:   string(255)! @password",
            "      role:     RfRole @default(RfRole.USER) @access(admin)",
            "      leftAt:   timestamp @deleted",
            "",
            "  RfNote:",
            "    meta: entity @root @expose(20) @owner(memberId) @access(read=owner, create=user)",
            "    fields:",
            "      noteId:   long @id @sequence",
            "      memberId: long",
            "      text:     string(200)",
            "");

    private static final String FILTER = "SessionRefreshFilter";

    private static final List<File> projects = new ArrayList<File>();

    @AfterAll
    static void cleanUp() throws Exception {
        java.lang.reflect.Field tmp =
                io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        for (File project : projects) delete(project);
    }

    @Test
    public void aHexagonalProjectReadsTheRowThroughTheInboundPort() throws Exception {
        String[] log = new String[1];
        File project = run("elcore-refresh-hex", "hexagonal", "java", "jpa", MODEL, false, 0, log);
        String filter = found(project, "RfMemberSessionRefresh.java");
        assertTrue(filter.contains("package io.acme.rfshop.adapter.in.web;"), filter);
        assertTrue(filter.contains("import io.acme.rfshop.application.port.in.RfMemberUseCase;"), filter);
        assertTrue(
                filter.contains("@Component\npublic class RfMemberSessionRefresh extends SessionRefreshFilter {"),
                filter);
        assertTrue(filter.contains("private final RfMemberUseCase service;"), filter);
        // The role as the sign-in puts it in the session.
        assertTrue(
                filter.contains("return service.me().map(signedIn -> SessionRefreshFilter.roles("
                        + "signedIn.getRole() == null ? null : signedIn.getRole().name()));"),
                filter);
        String control = found(project, "RfMemberController.java");
        assertTrue(
                control.contains("SessionCurrentUser.signIn(request, signedIn.getMemberId(), "
                        + "signedIn.getRole() == null ? null : signedIn.getRole().name());"),
                control);
        // Regenerated with the model: not a file the developer takes over.
        assertFalse(filter.contains("Initially generated"), filter);
        // The other root signs nobody in.
        assertEquals(0, count(project, "RfNoteSessionRefresh.java"));
        assertFalse(log[0].contains("@signin("), log[0]);
    }

    /**
     * What makes a row kept under @deleted count as gone: me() reads it with
     * findById, which the repository answers without the deleted rows.
     */
    @Test
    public void aDeletedRowIsGoneToMe() throws Exception {
        File project = run("elcore-refresh-deleted", "hexagonal", "java", "jpa", MODEL, false, 0, new String[1]);
        String core = found(project, "RfMemberCoreService.java");
        assertTrue(core.contains("public Optional<RfMemberDTO> me() {"), core);
        assertTrue(core.contains("return repository.findById("), core);
        String repository = found(project, "RfMemberRepository.java");
        assertTrue(
                repository.contains("@Query(\"SELECT e FROM RfMember e WHERE e.memberId = :id AND e.leftAt IS NULL\")\n"
                        + "    java.util.Optional<RfMember> findById(@Param(\"id\") Long id);"),
                repository);
        String adapter = found(project, "RfMemberRepositoryAdapter.java");
        assertTrue(adapter.contains("return repository.findById(memberId);"), adapter);
    }

    @Test
    public void kotlinReadsItTheSameWay() throws Exception {
        File project = run("elcore-refresh-kt", "hexagonal", "kotlin", "jpa", MODEL, false, 0, new String[1]);
        String filter = found(project, "RfMemberSessionRefresh.kt");
        assertTrue(filter.contains("package io.acme.rfshop.adapter.`in`.web\n"), filter);
        // A Kotlin keyword in a package is quoted, in the import too.
        assertTrue(filter.contains("import io.acme.rfshop.application.port.`in`.RfMemberUseCase\n"), filter);
        assertTrue(
                filter.contains("@Component\nclass RfMemberSessionRefresh(private val service: RfMemberUseCase)"
                        + " : SessionRefreshFilter() {"),
                filter);
        assertTrue(
                filter.contains("override fun signedInRoles(): Optional<out Collection<String>> =\n"
                        + "        service.me().map { signedIn -> SessionRefreshFilter.roles(signedIn.role?.name) }"),
                filter);
    }

    @Test
    public void aLayeredProjectReadsTheRowThroughTheService() throws Exception {
        File java = run("elcore-refresh-layered", "layered", "java", "jpa", MODEL, false, 0, new String[1]);
        String filter = found(java, "RfMemberSessionRefresh.java");
        assertTrue(filter.contains("package io.acme.rfshop.controller;"), filter);
        assertTrue(filter.contains("import io.acme.rfshop.service.RfMemberService;"), filter);
        assertTrue(filter.contains("private final RfMemberService service;"), filter);

        File kt = run("elcore-refresh-layered-kt", "layered", "kotlin", "jpa", MODEL, false, 0, new String[1]);
        String ktFilter = found(kt, "RfMemberSessionRefresh.kt");
        assertTrue(ktFilter.contains("class RfMemberSessionRefresh(private val service: RfMemberService)"), ktFilter);
    }

    @Test
    public void myBatisReadsItThroughTheSameMe() throws Exception {
        File project = run("elcore-refresh-mybatis", "layered", "java", "mybatis", MODEL, false, 0, new String[1]);
        String filter = found(project, "RfMemberSessionRefresh.java");
        assertTrue(filter.contains("private final RfMemberService service;"), filter);
        assertTrue(found(project, "RfMemberCoreService.java").contains("public Optional<RfMemberDTO> me() {"));
    }

    @Test
    public void withoutRefreshThereIsNoFilter() throws Exception {
        File project = run(
                "elcore-refresh-off",
                "hexagonal",
                "java",
                "jpa",
                MODEL.replace("@signin(refresh=request)", "@signin"),
                false,
                0,
                new String[1]);
        assertTrue(found(project, "RfMemberController.java").contains("@PostMapping(\"/signin\")"));
        assertEquals(0, count(project, "RfMemberSessionRefresh.java"));
    }

    /** No role field the session could hold: only whether the row is still there is asked. */
    @Test
    public void aRowWithoutARoleAsksOnlyWhetherItIsThere() throws Exception {
        String model = MODEL.replace("      role:     RfRole @default(RfRole.USER) @access(admin)\n", "");
        File project = run("elcore-refresh-norole", "hexagonal", "java", "jpa", model, false, 0, new String[1]);
        String filter = found(project, "RfMemberSessionRefresh.java");
        assertTrue(filter.contains("return service.me().map(signedIn -> SessionRefreshFilter.roles());"), filter);
    }

    @Test
    public void whatSigninDoesNotTakeIsSaid() throws Exception {
        String[] log = new String[1];
        File project = run(
                "elcore-refresh-unknown",
                "hexagonal",
                "java",
                "jpa",
                MODEL.replace("@signin(refresh=request)", "@signin(refresh=command, remember=true)"),
                false,
                0,
                log);
        assertTrue(log[0].contains("RfMember: @signin(refresh=command): refresh takes request"), log[0]);
        assertTrue(
                log[0].contains("RfMember: @signin(remember=...): @signin takes refresh=request; remember is ignored."),
                log[0]);
        assertEquals(0, count(project, "RfMemberSessionRefresh.java"));

        File bare = run(
                "elcore-refresh-bare",
                "hexagonal",
                "java",
                "jpa",
                MODEL.replace("@signin(refresh=request)", "@signin(request)"),
                false,
                0,
                log);
        assertTrue(
                log[0].contains("RfMember: @signin(request): @signin takes refresh=request; request is ignored."),
                log[0]);
        assertEquals(0, count(bare, "RfMemberSessionRefresh.java"));
    }

    /** An entity whose sign-in cannot be met gets no filter either; why is said as before. */
    @Test
    public void noSignInNoFilter() throws Exception {
        String[] log = new String[1];
        File project = run(
                "elcore-refresh-unexposed",
                "hexagonal",
                "java",
                "jpa",
                MODEL.replace("@root @editable @expose(20) @signin", "@root @editable @signin"),
                false,
                0,
                log);
        assertTrue(log[0].contains("@signin generates no sign-in: it is not @expose"), log[0]);
        assertEquals(0, count(project, "RfMemberSessionRefresh.java"));
    }

    /** The 2.x-compatible output has no generated sign-in, so no filter. */
    @Test
    public void theCompatibleOutputHasNone() throws Exception {
        File project = run("elcore-refresh-v2", "layered", "java", "jpa", MODEL, true, null, new String[1]);
        assertEquals(0, count(project, "RfMemberSessionRefresh.java"));
    }

    private static File run(
            String name,
            String architecture,
            String language,
            String mode,
            String model,
            boolean v2,
            Integer exit,
            String[] log)
            throws Exception {
        File project = Files.createTempDirectory(name).toFile();
        projects.add(project);
        File dir = new File(project, "src/main/resources/blueprint/rfshop");
        assertTrue(new File(dir, "model").mkdirs());
        String namespaces = "hexagonal".equals(architecture)
                ? "    base: io.acme.rfshop\n"
                : String.join(
                        "\n",
                        "    base: io.acme.rfshop",
                        "    entity: io.acme.rfshop.entity",
                        "    dto: io.acme.rfshop.dto",
                        "    repository: io.acme.rfshop.repository",
                        "    service: io.acme.rfshop.service",
                        "    control: io.acme.rfshop.controller",
                        "");
        Files.write(
                new File(dir, "env.yml").toPath(),
                String.join(
                                "\n",
                                "elasticore:",
                                "  enable: true",
                                "  config:",
                                "    domainName: rfshop",
                                "    framework: springboot",
                                "    mode: " + mode,
                                "    j2ee: jakarta",
                                "  namespace:",
                                namespaces)
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), model.getBytes(StandardCharsets.UTF_8));

        String[] keys = {
            "elasticore.base.path",
            "elasticore.architecture",
            "elasticore.language",
            "elasticore.rules.force",
            "elasticore.rules.v3",
            "elasticore.rules.v2",
            "elasticore.rules.ts",
            DdlConfig.DATABASES
        };
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        PrintStream out = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(bytes, true, "UTF-8"));
            for (String k : keys) System.clearProperty(k);
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", architecture);
            System.setProperty("elasticore.language", language);
            System.setProperty("elasticore.rules.force", "true");
            if (v2) System.setProperty("elasticore.rules.v2", "true");
            else if ("layered".equals(architecture)) System.setProperty("elasticore.rules.v3", "true");
            System.setProperty(DdlConfig.DATABASES, "postgresql");
            int code = RulesGenRunner.run(new String[0]);
            if (exit != null) assertEquals(exit.intValue(), code, "the run's exit: " + project);
        } finally {
            System.setOut(out);
            for (int i = 0; i < keys.length; i++) {
                if (saved[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], saved[i]);
            }
            io.elasticore.base.model.pub.rules.ModelCheck.reset();
        }
        log[0] = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        out.print(log[0]);
        return project;
    }

    private static String read(File file) throws IOException {
        assertTrue(file.isFile(), file + " was not written");
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String found(File project, String fileName) throws IOException {
        List<File> hits = new ArrayList<File>();
        collect(project, fileName, hits);
        assertEquals(1, hits.size(), fileName + " in " + project + ": " + hits);
        return read(hits.get(0));
    }

    private static int count(File project, String fileName) {
        List<File> hits = new ArrayList<File>();
        collect(project, fileName, hits);
        return hits.size();
    }

    private static void collect(File dir, String fileName, List<File> hits) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) collect(f, fileName, hits);
            else if (f.getName().equals(fileName)) hits.add(f);
        }
    }

    private static void delete(File root) throws IOException {
        if (!root.exists()) return;
        Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
