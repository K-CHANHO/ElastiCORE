package io.elasticore.runtime.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The runtime API that generated code calls, checked for raw types.
 *
 * A raw parameter here is not a style question, because none of the callers
 * are ours: every call site is generated and lands in somebody's controller.
 * {@code getMetaInfo(Class)} was raw, so the generated line
 *
 * <pre>DTOUtils.getMetaInfo(CustomerDTO.class)</pre>
 *
 * was a raw-type use in their file. javac accepts passing a
 * {@code Class<CustomerDTO>} to a raw {@code Class}, so the build said
 * nothing and the first report of it was a screenshot of a red mark in an
 * IDE - which is the worst way for a generator to learn about its own output.
 *
 * The check is a source scan rather than a reflection walk because erasure
 * hides exactly what is being asked about: a raw {@code Class} and a
 * {@code Class<?>} are the same at runtime, and the difference only exists
 * where the caller reads it.
 */
public class RuntimeApiTest {

    /**
     * Types whose raw use in a signature is worth failing over.
     *
     * Not every generic type - only the ones that appear in this API and
     * would put a raw-type use into generated code.
     */
    private static final String[] PARAMETERISED = {"Class", "List", "Map", "Set", "Optional"};

    @Test
    @DisplayName("getMetaInfo binds the DTO type exactly, so no analyser has to infer one")
    public void metaInfoBindsItsArgument() throws NoSuchMethodException {
        Method m = DTOUtils.class.getMethod("getMetaInfo", Class.class);
        Type parameter = m.getGenericParameterTypes()[0];

        // Not raw: that made the generated line a raw-type use. And not a
        // wildcard either - an IDE still marked the call and offered to
        // change the parameter to Class<ProductDTO>, wanting an exact
        // binding. A type variable is that binding.
        assertTrue(parameter.getTypeName().startsWith("java.lang.Class<"),
                "a raw Class makes every generated call site a raw-type use: "
                        + parameter.getTypeName());
        assertEquals(1, m.getTypeParameters().length,
                "the DTO type is bound from the class literal, not inferred from a wildcard");

        // The erasure has to be what it always was, or every project compiled
        // against an earlier release breaks on this line.
        assertEquals(Class.class, m.getParameterTypes()[0],
                "generics are the caller's view; the descriptor must not move");
    }

    @Test
    @DisplayName("the metadata a DTO reports is keyed by its declared fields")
    public void metaInfoDescribesTheDto() {
        Map<String, Object> meta = DTOUtils.getMetaInfo(Sample.class);

        assertTrue(meta.containsKey("name"), meta.toString());
        assertTrue(meta.containsKey("count"), meta.toString());
    }

    /** A DTO shaped like a generated one, without dragging a generated tree in. */
    public static class Sample {
        private String name;
        private Integer count;
    }

    @Test
    @DisplayName("no public runtime signature names a raw generic type")
    public void theRuntimeApiIsNotRaw() throws IOException {
        List<String> offenders = new ArrayList<String>();

        for (File source : javaFilesUnder(runtimeSourceRoot())) {
            String text = new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8);
            int line = 0;
            for (String each : text.split("\n")) {
                line++;
                String trimmed = each.trim();
                if (!trimmed.startsWith("public ") || trimmed.indexOf('(') < 0) continue;

                for (String type : PARAMETERISED) {
                    if (rawUse(trimmed, type)) {
                        offenders.add(source.getName() + ":" + line + " raw " + type);
                    }
                }
            }
        }

        assertTrue(offenders.isEmpty(),
                "every caller of these is generated, so a raw type here becomes a raw type"
                        + " in somebody else's file: " + offenders);
    }

    /**
     * The type named in a parameter list without its arguments.
     *
     * Deliberately crude: it looks at the parameter list only, and a name
     * followed by '<' is parameterised. A check that tried to parse Java
     * would be a check nobody trusts the verdict of.
     */
    private boolean rawUse(String signature, String type) {
        int open = signature.indexOf('(');
        int close = signature.lastIndexOf(')');
        if (open < 0 || close < open) return false;

        String params = signature.substring(open + 1, close);
        int at = params.indexOf(type);
        while (at >= 0) {
            boolean startsWord = at == 0 || !Character.isJavaIdentifierPart(params.charAt(at - 1));
            int after = at + type.length();
            boolean parameterised = after < params.length() && params.charAt(after) == '<';

            if (startsWord && !parameterised) return true;
            at = params.indexOf(type, at + 1);
        }
        return false;
    }

    private File runtimeSourceRoot() {
        File dir = new File(RuntimeApiTest.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath().replace("%20", " "));
        while (dir != null && !new File(dir, "src/main/java").isDirectory()) {
            dir = dir.getParentFile();
        }
        assertTrue(dir != null, "could not find the module root from the test's own location");
        return new File(dir, "src/main/java/io/elasticore/runtime");
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
