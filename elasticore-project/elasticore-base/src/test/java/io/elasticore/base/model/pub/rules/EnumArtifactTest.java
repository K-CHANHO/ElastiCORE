package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.EnumBaseRules;
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
 * The enumeration artifact, compared against the committed shop enums.
 *
 * CustomerType names the same field for @db and @json, so it carries both the
 * Jackson pair and the JPA converter off a single lookup - the case that
 * exercises every branch of the port.
 */
public class EnumArtifactTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String render(EnumSpec e) {
        String out = new RuleEngine().register(new EnumBaseRules()).run(e, "enum").render();
        return out.endsWith("\n") ? out.substring(0, out.length() - 1) : out;
    }

    /** blueprint/shop/model/enum.yml CustomerType. */
    private EnumSpec customerType() {
        return new EnumSpec("CustomerType")
                .pkg("enumeration", "io.elasticore.blueprint.domain.shop.enums")
                .addField(loader.parse("code", "string(1) @code"))
                .addField(loader.parse("description", "string(20)"))
                .constant("INDIVIDUAL", "I", "Individual")
                .constant("BUSINESS", "B", "Business")
                .constant("WHOLESALE", "W", "Wholesale")
                .jsonField("code")
                .dbField("code");
    }

    @Test
    public void customerType_matchesGolden() throws IOException {
        List<String> expected = readLines(golden("CustomerType.java"));
        expected = expected.subList(1, expected.size());   // skip the //ecd: line

        assertEquals(join(expected), render(customerType()));
    }

    // ---- the individual decisions, so a failure names itself ---------------

    /** A value declared for a string field is quoted. */
    @Test
    public void stringValues_areQuoted() {
        assertTrue(render(customerType()).contains("INDIVIDUAL(\"I\",\"Individual\")"),
                render(customerType()));
    }

    /** A numeric field takes its value unquoted. */
    @Test
    public void numericValues_areNotQuoted() {
        EnumSpec e = new EnumSpec("Level")
                .pkg("enumeration", "p.enums")
                .addField(loader.parse("rank", "int"))
                .constant("LOW", "1")
                .constant("HIGH", "9");

        String out = render(e);
        assertTrue(out.contains("LOW(1)"), out);
        assertTrue(out.contains("HIGH(9)"), out);
    }

    /** @json brings the serializer pair and the annotations that name it. */
    @Test
    public void jsonField_bringsTheSerializerPair() {
        EnumSpec e = new EnumSpec("Status")
                .pkg("enumeration", "p.enums")
                .addField(loader.parse("code", "string(1)"))
                .constant("OPEN", "O")
                .jsonField("code");

        String out = render(e);
        assertTrue(out.contains("@JsonSerialize(using = Status.CustomSerializer.class)"), out);
        assertTrue(out.contains("public static class CustomDeserializer extends JsonDeserializer<Status>"), out);
        assertTrue(out.contains("public static Status fromCode(String code)"), out);
        // no @db, so no converter
        assertFalse(out.contains("AttributeConverter"), out);
    }

    /** @db brings the JPA converter and no Jackson members. */
    @Test
    public void dbField_bringsTheJpaConverter() {
        EnumSpec e = new EnumSpec("Status")
                .pkg("enumeration", "p.enums")
                .addField(loader.parse("code", "string(1)"))
                .constant("OPEN", "O")
                .dbField("code");

        String out = render(e);
        assertTrue(out.contains("@Converter(autoApply = true)"), out);
        assertTrue(out.contains("implements AttributeConverter<Status, String>"), out);
        assertFalse(out.contains("JsonSerialize"), out);
    }

    /** Naming different fields produces a lookup for each. */
    @Test
    public void separateJsonAndDbFields_produceALookupEach() {
        EnumSpec e = new EnumSpec("Status")
                .pkg("enumeration", "p.enums")
                .addField(loader.parse("code", "string(1)"))
                .addField(loader.parse("label", "string(20)"))
                .constant("OPEN", "O", "Open")
                .jsonField("label")
                .dbField("code");

        String out = render(e);
        assertTrue(out.contains("public static Status fromLabel(String label)"), out);
        assertTrue(out.contains("public static Status fromCode(String code)"), out);
    }

    /** An enum declaring neither gets the listing only. */
    @Test
    public void plainEnum_getsTheListingOnly() {
        EnumSpec e = new EnumSpec("Status")
                .pkg("enumeration", "p.enums")
                .addField(loader.parse("code", "string(1)"))
                .constant("OPEN", "O");

        String out = render(e);
        assertTrue(out.contains("public static List<Map> getAllEnumInfo()"), out);
        assertFalse(out.contains("fromCode"), out);
        assertFalse(out.contains("JsonSerialize"), out);
        assertFalse(out.contains("AttributeConverter"), out);
    }

    /** The listing exposes every declared field by its getter. */
    @Test
    public void listing_exposesEveryField() {
        String out = render(customerType());
        assertTrue(out.contains("map.put(\"code\", i.getCode());"), out);
        assertTrue(out.contains("map.put(\"description\", i.getDescription());"), out);
    }

    // ---- golden access ----------------------------------------------------

    private File golden(String fileName) {
        String rel = "elasticore-template/src/main/java/io/elasticore/blueprint/"
                + "domain/shop/enums/" + fileName;
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
