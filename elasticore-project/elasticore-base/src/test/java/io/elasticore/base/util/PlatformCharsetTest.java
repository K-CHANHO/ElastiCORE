package io.elasticore.base.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * No file is written in whatever charset the machine happened to have.
 *
 * The rule is "never use FileWriter or FileReader": both take the platform
 * charset, so Korean written through them - a generated schema.yml, a user's
 * env.yml or model.yml - turns into mojibake on a machine whose charset is not
 * UTF-8.
 *
 * <h3>Why this is a source scan and not an output comparison</h3>
 *
 * Because an output comparison cannot see it. FileWriter uses the platform
 * default charset, which on a Korean Windows machine is MS949 and on a
 * Linux CI is UTF-8 - so on CI the wrong code produces exactly the right
 * bytes and every assertion about the file passes. A test that only fails on
 * some developers' laptops is not a gate.
 *
 * What is checkable everywhere is the instruction itself. This reads the
 * sources and asks whether any of them names a constructor that decides an
 * encoding by asking the operating system.
 */
public class PlatformCharsetTest {

    /**
     * The rule, as a pair of constructors nothing may call.
     *
     * Both take a charset from the platform and neither has an overload that
     * does not, which is what makes them worth banning outright rather than
     * reviewing case by case.
     */
    private static final String[] BANNED = {"new FileWriter(", "new FileReader("};

    @Test
    @DisplayName("no source names a constructor that lets the platform pick the encoding")
    public void noPlatformDefaultFileIO() throws IOException {
        File sourceRoot = sourceRoot();
        List<String> offenders = new ArrayList<String>();

        for (File source : javaFilesUnder(sourceRoot)) {
            String text = new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8);
            for (String banned : BANNED) {
                if (text.contains(banned)) {
                    offenders.add(source.getName() + " uses " + banned + ")");
                }
            }
        }

        assertTrue(offenders.isEmpty(),
                "Use new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)"
                        + " and the InputStreamReader equivalent. Offending sources: " + offenders);
    }

    /**
     * The module's own sources, found from the class output rather than from
     * a working directory, which differs between an IDE run and a Gradle one.
     */
    private File sourceRoot() {
        File dir = new File(PlatformCharsetTest.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath().replace("%20", " "));

        while (dir != null && !new File(dir, "src/main/java").isDirectory()) {
            dir = dir.getParentFile();
        }
        assertTrue(dir != null, "could not find the module root from the test's own location");
        return new File(dir, "src/main/java");
    }

    private List<File> javaFilesUnder(File dir) {
        List<File> out = new ArrayList<File>();
        File[] children = dir.listFiles();
        if (children == null) return out;

        for (File child : children) {
            if (child.isDirectory()) {
                out.addAll(javaFilesUnder(child));
            } else if (child.getName().endsWith(".java")) {
                out.add(child);
            }
        }
        return out;
    }
}
