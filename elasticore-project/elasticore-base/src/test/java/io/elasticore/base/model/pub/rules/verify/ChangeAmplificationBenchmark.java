package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How much generated code one line of DSL moves, and whether generating twice
 * gives the same code - measured on the pet store sample, as the plugin
 * generates it (hexagonal, Java, Spring Boot 4, Ossie export, TypeScript).
 *
 * Each scenario is a business change a developer (or an AI) would make. It is
 * applied to a fresh copy of the pet store's DSL, generated into a fresh
 * directory, and compared with the unchanged model generated the same way.
 * Lines are counted by a longest-common-subsequence diff; the first line of a
 * generated file (//ecd: content hash and time) is left out of every count.
 *
 * Not part of the build - it generates the sample many times. Run with
 *   ELASTICORE_BENCH=1 ./gradlew :elasticore-project:elasticore-base:test --tests "*ChangeAmplificationBenchmark"
 * and read build/bench/change-amplification.md.
 */
@EnabledIfEnvironmentVariable(named = "ELASTICORE_BENCH", matches = "1|true")
public class ChangeAmplificationBenchmark {

    /** One business change: what it is, and the DSL edits that make it (file -> old -> new). */
    private static final class Scenario {
        final String name;
        final List<String[]> edits = new ArrayList<String[]>();

        Scenario(String name) {
            this.name = name;
        }

        Scenario edit(String file, String before, String after) {
            edits.add(new String[]{file, before, after});
            return this;
        }
    }

    private static List<Scenario> scenarios() {
        List<Scenario> out = new ArrayList<Scenario>();
        out.add(new Scenario("Add a searchable field (Customer.nickname)")
                .edit("model/entity.yml",
                        "      firstName:   string(50)! @search(like)\n",
                        "      firstName:   string(50)! @search(like)\n      nickname:    string(30) @search(like)\n"));
        out.add(new Scenario("Add a lifecycle step (deliver: SHIPPED -> DELIVERED)")
                .edit("model/enum.yml",
                        "      CANCELLED: CAN, Cancelled\n",
                        "      CANCELLED: CAN, Cancelled\n      DELIVERED: DLV, Delivered\n")
                .edit("model/entity.yml",
                        "        cancel:  PLACED -> CANCELLED\n",
                        "        cancel:  PLACED -> CANCELLED\n        deliver: SHIPPED -> DELIVERED\n"));
        out.add(new Scenario("Change who may do something (cancel: owner -> admin)")
                .edit("model/entity.yml", "cancel=owner)", "cancel=admin)"));
        out.add(new Scenario("Add a business rule (a price ceiling)")
                .edit("model/entity.yml",
                        "@min(0.01, 'price must be positive')",
                        "@min(0.01, 'price must be positive') @max(10000, 'price is too high')"));
        out.add(new Scenario("Order a search by default (Category @sort)")
                .edit("model/entity.yml",
                        "@access(read=public) -- 분류",
                        "@access(read=public) @sort(name+) -- 분류"));
        return out;
    }

    @Test
    public void measure() throws Exception {
        File blueprint = new File(repositoryRoot(),
                "elasticore-project/elasticore-sample-petstore/src/main/resources/blueprint/petstore");
        assertTrue(blueprint.isDirectory(), blueprint.getPath());

        Map<String, List<String>> base = generate(blueprint, null);
        Map<String, List<String>> again = generate(blueprint, null);
        int differingOnRepeat = 0;
        for (String path : union(base, again)) {
            if (!same(base.get(path), again.get(path))) differingOnRepeat++;
        }

        StringBuilder detail = new StringBuilder("\n## Files each change touched\n");
        StringBuilder md = new StringBuilder();
        md.append("| Business change | DSL lines | Generated files | Generated lines | Java | TypeScript | Other | Per DSL line |\n");
        md.append("|---|---:|---:|---:|---:|---:|---:|---:|\n");
        for (Scenario s : scenarios()) {
            Map<String, List<String>> changed = generate(blueprint, s);
            int dsl = dslLinesChanged(blueprint, s);
            int files = 0, java = 0, ts = 0, other = 0;
            detail.append("\n").append(s.name).append(":\n");
            for (String path : union(base, changed)) {
                List<String> a = base.get(path), b = changed.get(path);
                if (same(a, b)) continue;
                files++;
                int n = changedLines(a, b);
                detail.append(String.format("- %s (%d)%n", path.substring(path.lastIndexOf('/') + 1), n));
                if (path.endsWith(".java")) java += n;
                else if (path.endsWith(".ts")) ts += n;
                else other += n;
            }
            int total = java + ts + other;
            md.append(String.format("| %s | %d | %d | %d | %d | %d | %d | %.0f |%n",
                    s.name, dsl, files, total, java, ts, other, dsl == 0 ? 0.0 : (double) total / dsl));
        }

        int dslTotal = dslSize(blueprint);
        int genTotal = 0, genFiles = base.size();
        for (List<String> lines : base.values()) genTotal += nonBlank(lines);

        StringBuilder head = new StringBuilder();
        head.append(String.format("Model: %d non-blank DSL lines -> %d generated files, %d non-blank generated lines.%n%n",
                dslTotal, genFiles, genTotal));
        head.append(String.format("Determinism: generated twice from the same DSL, %d of %d files differ.%n%n",
                differingOnRepeat, genFiles));

        File out = new File("build/bench/change-amplification.md");
        out.getParentFile().mkdirs();
        Files.write(out.toPath(), (head + md.toString() + detail).getBytes(StandardCharsets.UTF_8));
        System.out.println(head + md.toString() + detail);

        assertEquals(0, differingOnRepeat, "the same DSL must generate the same code");
    }

    // ---- generation ---------------------------------------------------------

    /** Generates the pet store (server, then TypeScript) from a copy of its DSL, a scenario applied. */
    private Map<String, List<String>> generate(File blueprint, Scenario scenario) throws Exception {
        File project = Files.createTempDirectory("elcore-bench").toFile();
        try {
            File copy = new File(project, "src/main/resources/blueprint/petstore");
            copyTree(blueprint.toPath(), copy.toPath());
            if (scenario != null) apply(copy, scenario);
            run(project, false);
            run(project, true);
            return snapshot(project);
        } finally {
            delete(project);
            resetContext();
        }
    }

    private static void run(File project, boolean typescript) {
        String[][] props = {
                {"elasticore.base.path", project.getAbsolutePath()},
                {"elasticore.architecture", "hexagonal"},
                {"elasticore.language", "java"},
                {"elasticore.springBoot", "4"},
                {"elasticore.publisher.rules.add", "ossie-export"},
                {"elasticore.rules.ts", typescript ? "true" : null},
        };
        String[] saved = new String[props.length];
        for (int i = 0; i < props.length; i++) {
            saved[i] = System.getProperty(props[i][0]);
            if (props[i][1] == null) System.clearProperty(props[i][0]);
            else System.setProperty(props[i][0], props[i][1]);
        }
        java.io.PrintStream out = System.out;
        System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        try {
            RulesGenRunner.main(new String[0]);
        } finally {
            System.setOut(out);
            for (int i = 0; i < props.length; i++) {
                if (saved[i] == null) System.clearProperty(props[i][0]);
                else System.setProperty(props[i][0], saved[i]);
            }
        }
    }

    /** Every generated file - the copied guides are documents, not output, and stay out. */
    private static Map<String, List<String>> snapshot(File project) throws IOException {
        final Map<String, List<String>> files = new TreeMap<String, List<String>>();
        final Path root = project.toPath();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String rel = root.relativize(file).toString().replace('\\', '/');
                boolean dsl = rel.startsWith("src/main/resources/blueprint/petstore/");
                boolean guide = rel.startsWith("src/main/resources/blueprint/elasticore-") && rel.endsWith(".md");
                if (!dsl && !guide) files.put(rel, contentLines(file));
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    private static List<String> contentLines(Path file) throws IOException {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).replace("\r\n", "\n");
        List<String> lines = new ArrayList<String>(Arrays.asList(text.split("\n", -1)));
        // The content hash and generation time: different on every run by design.
        if (!lines.isEmpty() && lines.get(0).startsWith("//ecd:")) lines.remove(0);
        return lines;
    }

    private static void apply(File copy, Scenario s) throws IOException {
        for (String[] e : s.edits) {
            File f = new File(copy, e[0]);
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            String nl = text.contains("\r\n") ? "\r\n" : "\n";
            String before = e[1].replace("\n", nl), after = e[2].replace("\n", nl);
            assertTrue(text.contains(before), s.name + ": " + e[0] + " has no '" + e[1].trim() + "'");
            Files.write(f.toPath(), text.replace(before, after).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static int dslLinesChanged(File blueprint, Scenario s) throws IOException {
        Map<String, String> after = new LinkedHashMap<String, String>();
        for (String[] e : s.edits) {
            String current = after.containsKey(e[0]) ? after.get(e[0])
                    : new String(Files.readAllBytes(new File(blueprint, e[0]).toPath()), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
            after.put(e[0], current.replace(e[1], e[2]));
        }
        int n = 0;
        for (Map.Entry<String, String> en : after.entrySet()) {
            String before = new String(Files.readAllBytes(new File(blueprint, en.getKey()).toPath()), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
            n += changedLines(Arrays.asList(before.split("\n", -1)), Arrays.asList(en.getValue().split("\n", -1)));
        }
        return n;
    }

    private static int dslSize(File blueprint) throws IOException {
        final int[] n = {0};
        Files.walkFileTree(blueprint.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toString().endsWith(".yml")) return FileVisitResult.CONTINUE;
                for (String line : contentLines(file)) {
                    String t = line.trim();
                    if (!t.isEmpty() && !t.startsWith("#")) n[0]++;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return n[0];
    }

    // ---- counting -----------------------------------------------------------

    /** Lines removed plus lines added: what a reviewer reads in a diff of the two. */
    static int changedLines(List<String> a, List<String> b) {
        if (a == null) return nonBlank(b);
        if (b == null) return nonBlank(a);
        int n = a.size(), m = b.size();
        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = a.get(i).equals(b.get(j)) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        return (n - lcs[0][0]) + (m - lcs[0][0]);
    }

    private static int nonBlank(List<String> lines) {
        int n = 0;
        for (String l : lines) if (!l.trim().isEmpty()) n++;
        return n;
    }

    private static boolean same(List<String> a, List<String> b) {
        return a == null ? b == null : a.equals(b);
    }

    private static List<String> union(Map<String, ?> a, Map<String, ?> b) {
        TreeMap<String, Boolean> all = new TreeMap<String, Boolean>();
        for (String k : a.keySet()) all.put(k, true);
        for (String k : b.keySet()) all.put(k, true);
        return new ArrayList<String>(all.keySet());
    }

    // ---- files --------------------------------------------------------------

    private static void copyTree(final Path from, final Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(to.resolve(from.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** Removes a directory this test created under the system temp directory - nothing else. */
    private static void delete(File dir) throws IOException {
        if (dir == null || !dir.exists()) return;
        String temp = new File(System.getProperty("java.io.tmpdir")).getCanonicalPath();
        if (!dir.getCanonicalPath().startsWith(temp) || !dir.getName().startsWith("elcore-bench")) {
            throw new IllegalStateException("refusing to delete " + dir);
        }
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void resetContext() throws Exception {
        java.lang.reflect.Field tmp = io.elasticore.base.model.core.BaseECoreModelContext.class.getDeclaredField("tmpCtx");
        tmp.setAccessible(true);
        tmp.set(null, null);
        SpecFactory.valueObjectsInDto(false);
        SpecFactory.rootsSplitByDefault(false);
        SpecFactory.commandOnlyRoots(false);
    }

    private static File repositoryRoot() {
        File dir = new File("").getAbsoluteFile();
        while (dir != null && !new File(dir, "settings.gradle").isFile()) dir = dir.getParentFile();
        return dir;
    }
}
