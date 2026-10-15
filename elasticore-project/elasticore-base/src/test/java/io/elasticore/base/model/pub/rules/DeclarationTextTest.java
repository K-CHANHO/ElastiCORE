package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Where a project rule's method and class annotations land in written text. */
public class DeclarationTextTest {

    private static Map<String, List<AnnoCode>> on(String method, AnnoCode anno) {
        Map<String, List<AnnoCode>> m = new LinkedHashMap<String, List<AnnoCode>>();
        List<AnnoCode> list = new ArrayList<AnnoCode>();
        list.add(anno);
        m.put(method, list);
        return m;
    }

    private static String annotate(String text, Syntax syntax, String method, String fqcn) {
        return new DeclarationText(text, syntax, "repository")
                .annotateMethods(on(method, AnnoCode.of(fqcn))).text();
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) n++;
        return n;
    }

    @Test
    public void aFunctionCalledInsideAQueryStringIsNotTheMethod() {
        String text = "package a;\n\npublic interface R {\n"
                + "    @Query(\"SELECT DISTINCT count(o) FROM Order o\")\n"
                + "    long total();\n"
                + "    long count();\n}\n";
        String out = annotate(text, Syntax.JAVA, "count", "io.micrometer.core.annotation.Timed");
        assertEquals(1, count(out, "@Timed"), out);
        assertTrue(out.contains("    @Timed\n    long count();"), out);
    }

    @Test
    public void anAnnotationOnTheDeclarationLineIsNoObstacle() {
        String text = "package a;\n\npublic class S {\n"
                + "    @Transactional(readOnly = true) public Foo find(Long id) {\n        return null;\n    }\n}\n";
        String out = annotate(text, Syntax.JAVA, "find", "io.micrometer.core.annotation.Timed");
        assertTrue(out.contains("    @Timed\n    @Transactional(readOnly = true) public Foo find"), out);
    }

    @Test
    public void anotherTypeOfTheSameNameIsNotTheSameAnnotation() {
        String text = "package a;\n\nimport other.Timed;\n\npublic interface R {\n    @Timed\n    long total();\n}\n";
        String out = annotate(text, Syntax.JAVA, "total", "io.micrometer.core.annotation.Timed");
        assertTrue(out.contains("@io.micrometer.core.annotation.Timed"), out);
        assertFalse(out.contains("import io.micrometer.core.annotation.Timed;"), out);
    }

    @Test
    public void aTypeTheFileDeclaresIsNotImportedOver() {
        String text = "package a;\n\npublic class Timed {\n    public long total() { return 0; }\n}\n";
        String out = annotate(text, Syntax.JAVA, "total", "io.micrometer.core.annotation.Timed");
        assertTrue(out.contains("@io.micrometer.core.annotation.Timed"), out);
        assertFalse(out.contains("import io.micrometer"), out);
    }

    @Test
    public void aSubpackageOfJavaLangIsImported() {
        String text = "package a;\n\npublic interface R {\n    long total();\n}\n";
        String out = annotate(text, Syntax.JAVA, "total", "java.lang.annotation.Documented");
        assertTrue(out.contains("import java.lang.annotation.Documented;"), out);
        String plain = annotate(text, Syntax.JAVA, "total", "java.lang.Deprecated");
        assertFalse(plain.contains("import java.lang.Deprecated;"), plain);
    }

    @Test
    public void mixedLineEndingsStillFindTheMethod() {
        String text = "package a;\r\n\npublic interface R {\n    long total();\n}\n";
        String out = annotate(text, Syntax.JAVA, "total", "io.micrometer.core.annotation.Timed");
        assertTrue(out.contains("@Timed"), out);
    }

    @Test
    public void kotlinFormsAreFound() {
        String text = "package a\n\nclass S {\n"
                + "    @Transactional fun find(id: Long): Foo? = null\n"
                + "    tailrec fun loop(n: Int): Int = n\n"
                + "    fun String.shout(): String = this\n}\n";
        DeclarationText d = new DeclarationText(text, Syntax.KOTLIN, "service");
        Map<String, List<AnnoCode>> m = on("find", AnnoCode.of("io.micrometer.core.annotation.Timed"));
        m.putAll(on("loop", AnnoCode.of("io.micrometer.core.annotation.Timed")));
        m.putAll(on("shout", AnnoCode.of("io.micrometer.core.annotation.Timed")));
        String out = d.annotateMethods(m).text();
        assertEquals(3, count(out, "@Timed\n"), out);
        assertTrue(out.contains("import io.micrometer.core.annotation.Timed"), out);
    }

    @Test
    public void aClassDeclaredOnOneLineWithItsAnnotationGetsTheNewOne() {
        String text = "package a;\n\n@Entity public class X {\n}\n";
        String out = new DeclarationText(text, Syntax.JAVA, "entity")
                .annotateType(Collections.singletonList(AnnoCode.of("com.acme.Marker"))).text();
        assertTrue(out.contains("@Marker\n@Entity public class X"), out);
        assertTrue(out.contains("import com.acme.Marker;"), out);
    }
}
