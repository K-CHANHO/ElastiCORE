package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Field-level @readonly generated the way a project generates it: a
 * hexagonal library in Java and in Kotlin. The response carries the field,
 * the mapper never writes it back - on a split root, an @editable root and a
 * DTO that marks a template field itself - and the DTO neither requires it
 * nor documents it as writable.
 */
public class ServerSetFieldGenerationTest {

    private static File java;
    private static File kotlin;

    private static final String ENV = String.join("\n",
            "elasticore:",
            "  enable: true",
            "  config:",
            "    domainName: library",
            "    framework: springboot",
            "    mode: jpa",
            "    j2ee: jakarta",
            "  namespace:",
            "    base: com.acme.library",
            "");

    private static final String MODEL = String.join("\n",
            "entity:",
            "  Loan:",
            "    meta: entity @root @expose(20)",
            "    fields:",
            "      loanId: long @id @sequence",
            "      bookTitle: string(100)!",
            "      returnedDate: date @readonly",
            "      status: string(3) @behavior",
            "  Member:",
            "    meta: entity @root @editable @expose(20)",
            "    fields:",
            "      memberId: long @id @sequence",
            "      name: string(50)!",
            "      joinedAt: date @readonly",
            "      grade: string(10)! @noinput",
            "dto:",
            "  MemberCardDTO:",
            "    meta: dto @template(Member)",
            "    fields:",
            "      name: string(50) @readonly",
            "");

    private static File generate(String prefix, String language) throws Exception {
        File project = Files.createTempDirectory(prefix).toFile();
        File dir = new File(project, "src/main/resources/blueprint/library");
        assertTrue(new File(dir, "model").mkdirs());
        Files.write(new File(dir, "env.yml").toPath(), ENV.getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "model/model.yml").toPath(), MODEL.getBytes(StandardCharsets.UTF_8));

        String[] keys = {"elasticore.base.path", "elasticore.architecture", "elasticore.language", "elasticore.rules.force"};
        String[] saved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) saved[i] = System.getProperty(keys[i]);
        try {
            System.setProperty("elasticore.base.path", project.getAbsolutePath());
            System.setProperty("elasticore.architecture", "hexagonal");
            System.setProperty("elasticore.language", language);
            System.setProperty("elasticore.rules.force", "true");
            RulesGenRunner.main(new String[0]);
        } finally {
            for (int i = 0; i < keys.length; i++) {
                if (saved[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], saved[i]);
            }
            resetStatics();
        }
        return project;
    }

    private static void resetStatics() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        SpecFactory.valueObjectsInDto(false);
        SpecFactory.rootsSplitByDefault(false);
        SpecFactory.commandOnlyRoots(false);
    }

    @BeforeAll
    static void generateBoth() throws Exception {
        java = generate("elcore-ssf-java", "java");
        kotlin = generate("elcore-ssf-kt", "kotlin");
    }

    @AfterAll
    static void clean() throws Exception {
        delete(java);
        delete(kotlin);
    }

    private static void delete(File project) throws Exception {
        if (project == null || !project.exists()) return;
        Files.walkFileTree(project.toPath(), new SimpleFileVisitor<Path>() {
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

    /** The generated file of that name, wherever its package put it. */
    private static String read(File project, String fileName) throws Exception {
        final Path[] found = new Path[1];
        Files.walkFileTree(new File(project, "src/main").toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals(fileName)) {
                    found[0] = file;
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        assertNotNull(found[0], fileName + " was not generated under " + project);
        return new String(Files.readAllBytes(found[0]), StandardCharsets.UTF_8);
    }

    /** A method's body: from its signature to the brace that closes it. */
    private static String body(String source, String signature) {
        int at = source.indexOf(signature);
        assertTrue(at >= 0, "no " + signature + " in:\n" + source);
        int end = source.indexOf("\n    }", at);
        return source.substring(at, end);
    }

    /** The field's own declaration, from its annotations to its name. */
    private static String declarationOf(String source, String field) {
        int at = source.indexOf(" " + field + ";");
        if (at < 0) at = source.indexOf(" " + field + ":");
        assertTrue(at >= 0, field + " not declared:\n" + source);
        int from = Math.max(source.lastIndexOf(";", at),
                Math.max(source.lastIndexOf("}", at), source.lastIndexOf("= null", at)));
        return source.substring(from + 1, at);
    }

    // ---- Java ----------------------------------------------------------

    @Test
    public void theJavaMapperReadsItOutAndNeverWritesItBack() throws Exception {
        String mapper = read(java, "LibraryMapper.java");
        String forward = body(mapper, "public static void mapping(Member from, MemberDTO to, boolean isSkipNull, MappingContext c)");
        assertTrue(forward.contains("setVal(from.getJoinedAt(), to::setJoinedAt, isSkipNull);"), forward);
        assertTrue(forward.contains("setVal(from.getGrade(), to::setGrade, isSkipNull);"), forward);

        for (String sig : new String[]{
                "public static void mapping(MemberDTO from, Member to, boolean isSkipNull, MappingContext c)",
                "public static void mapping(MemberDTO from, Member to, boolean isSkipNull, MappingContext c, EntityManager em)"}) {
            String reverse = body(mapper, sig);
            assertFalse(reverse.contains("JoinedAt"), reverse);
            assertFalse(reverse.contains("Grade"), reverse);
            assertTrue(reverse.contains("setVal(from.getName(), to::setName, isSkipNull);"), reverse);
        }
    }

    @Test
    public void aSplitRootKeepsTheCreationWriterButTheMapperLeavesTheField() throws Exception {
        String mapper = read(java, "LibraryMapper.java");
        String reverse = body(mapper, "public static void mapping(LoanDTO from, Loan to, boolean isSkipNull, MappingContext c)");
        assertTrue(reverse.contains("setVal(from.getBookTitle(), to.creation()::setBookTitle, isSkipNull);"), reverse);
        assertFalse(reverse.contains("ReturnedDate"), "@readonly: " + reverse);
        assertFalse(reverse.contains("Status"), "@behavior: " + reverse);

        String base = read(java, "LoanBase.java");
        assertTrue(declarationOf(base, "returnedDate").contains("@Setter(AccessLevel.PROTECTED)"), base);
        assertTrue(base.contains("public void setReturnedDate(java.time.LocalDate value) {"), base);
    }

    @Test
    public void aDtoThatMarksATemplateFieldReadonlyDoesNotWriteIt() throws Exception {
        String mapper = read(java, "LibraryMapper.java");
        String reverse = body(mapper, "public static void mapping(MemberCardDTO from, Member to, boolean isSkipNull, MappingContext c)");
        assertFalse(reverse.contains("setName"), reverse);
        assertFalse(reverse.contains("JoinedAt"), reverse);
        // The entity's own DTO still takes it.
        assertTrue(body(mapper, "public static void mapping(MemberDTO from, Member to, boolean isSkipNull, MappingContext c)")
                .contains("setVal(from.getName(), to::setName, isSkipNull);"));
    }

    @Test
    public void theJavaDtoShowsItReadOnlyAndDoesNotRequireIt() throws Exception {
        String dto = read(java, "MemberDTO.java");
        String grade = declarationOf(dto, "grade");
        assertTrue(grade.contains("accessMode = Schema.AccessMode.READ_ONLY"), grade);
        assertTrue(grade.contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), grade);
        assertFalse(grade.contains("NotNull"), grade);
        assertTrue(declarationOf(dto, "name").contains("NotNull"), dto);
        String loan = read(java, "LoanDTO.java");
        assertTrue(declarationOf(loan, "returnedDate").contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), loan);
    }

    // ---- Kotlin --------------------------------------------------------

    @Test
    public void theKotlinMapperNeverWritesItBack() throws Exception {
        String mapper = read(kotlin, "LibraryMapper.kt");
        String forward = body(mapper, "fun mapping(from: Member?, to: MemberDTO?, isSkipNull: Boolean, c: MappingContext?)");
        assertTrue(forward.contains("joinedAt"), forward);
        String reverse = body(mapper, "fun mapping(from: MemberDTO?, to: Member?, isSkipNull: Boolean, c: MappingContext?)");
        assertFalse(reverse.contains("joinedAt"), reverse);
        assertFalse(reverse.contains("grade"), reverse);
        assertTrue(reverse.contains("to.name = v"), reverse);
        String loan = body(mapper, "fun mapping(from: LoanDTO?, to: Loan?, isSkipNull: Boolean, c: MappingContext?)");
        assertFalse(loan.contains("returnedDate"), loan);
        assertTrue(loan.contains("bookTitle"), loan);
    }
}
