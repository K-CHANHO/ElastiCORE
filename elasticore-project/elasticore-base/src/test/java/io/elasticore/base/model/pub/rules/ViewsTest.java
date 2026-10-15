package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.pub.rules.base.ViewsBaseRules;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Views class, compared against the one committed for the jsonview_demo
 * domain - the only domain whose env.yml turns views on.
 */
public class ViewsTest {

    private SourceCode run(DomainSpec d) {
        return new RuleEngine().register(new ViewsBaseRules()).run(d, "views");
    }

    private DomainSpec domain(String name) {
        return new DomainSpec(name, name)
                .pkg("dto", "io.elasticore.blueprint.domain." + name + ".dto");
    }

    @Test
    public void viewsClass_matchesGolden() throws IOException {
        DomainSpec d = domain("jsonview_demo").jsonViewConfig("auto");

        List<String> expected = readLines(golden());
        expected = expected.subList(1, expected.size());   // skip the //ecd: line

        String actual = run(d).render();
        if (actual.endsWith("\n")) actual = actual.substring(0, actual.length() - 1);
        assertEquals(join(expected), actual);
    }

    /** An env.yml of "true" turns the class on for the whole domain. */
    @Test
    public void envConfigTrue_producesTheClass() {
        assertTrue(run(domain("shop").jsonViewConfig("true")).isMake());
    }

    /** A single DTO asking for views is enough. */
    @Test
    public void oneDtoDeclaringJsonView_producesTheClass() {
        DomainSpec d = domain("shop").entity(new EntitySpec("Member").meta("jsonview", null));

        assertTrue(run(d).isMake());
    }

    /** A DTO that turns views off does not ask for the class. */
    @Test
    public void dtoDisablingJsonView_doesNotProduceTheClass() {
        DomainSpec d = domain("shop").entity(new EntitySpec("Member").meta("jsonview", "false"));

        assertFalse(run(d).isMake());
    }

    /** With views off everywhere the class is not written. */
    @Test
    public void domainWithoutJsonView_producesNoFile() {
        DomainSpec d = domain("shop").entity(new EntitySpec("Order"));

        assertFalse(run(d).isMake());
    }

    // ---- golden access ----------------------------------------------------

    private File golden() {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/jsonview_demo/dto/Views.java";
        String[] candidates = {"../" + rel, "elasticore-project/" + rel};
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) return f;
        }
        throw new IllegalStateException("Golden file not found: " + rel
                + " from cwd " + new File(".").getAbsolutePath());
    }

    private static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private List<String> readLines(File f) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
        } finally {
            r.close();
        }
        return lines;
    }
}
