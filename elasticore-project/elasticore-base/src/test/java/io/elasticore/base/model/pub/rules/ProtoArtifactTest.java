package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A NON-JAVA artifact driven by the same rule engine: proto definitions.
 *
 * This is the architecture check for language neutrality - no imports, no
 * class annotations, proto comment syntax, and an output identity under
 * src/main/proto instead of a Java package path.
 *
 * The model is the one committed in elasticore-template
 * (src/main/proto/CarInfoPortService.proto). That golden is a stale V1.0
 * artifact whose DSL no longer exists in the blueprint - every domain is
 * mode: jpa today - so it serves as a SHAPE reference, not a byte gate.
 * Two 2.x traits are deliberately not reproduced:
 *   - message order came from HashSet iteration (hash-dependent)
 *   - a blank paragraph line picked up the slot indent (trailing spaces)
 */
public class ProtoArtifactTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /**
     * The rpc description of the committed golden. Written as escapes so the
     * source stays ASCII: elasticore-base must compile on MS949 Windows.
     */
    private static final String RPC_DESC =
            "\uBE0C\uB79C\uB4DC\uBCC4 \uCC28\uB7C9 \uC815\uBCF4 \uC870\uD68C";

    /** Same service the committed golden describes. */
    private ProtoServiceSpec carInfoService() {
        EntitySpec input = new EntitySpec("CarInfo2Input")
                .addField(loader.parse("brand", "string"))
                .addField(loader.parse("inputType", "CarInfo23Input"))
                .addField(loader.parse("boType", "BoardType"));

        EntitySpec nestedInput = new EntitySpec("CarInfo23Input")
                .addField(loader.parse("brand", "string"));

        EntitySpec output = new EntitySpec("CarInfo2Output")
                .addField(loader.parse("brand", "string"))
                .addField(loader.parse("description", "string"))
                .addField(loader.parse("modelId", "string"));

        return new ProtoServiceSpec("CarInfoPortService", "io.elasticore.blueprint.proto")
                .rpc("findByBrand", "CarInfo2Input", "CarInfo2Output", RPC_DESC)
                .message(input)
                .message(nestedInput)
                .message(output)
                .nest("CarInfo2Input", "CarInfo23Input")
                .enumeration("BoardType", "PUBLIC", "PRIVATE");
    }

    private String render(ProtoServiceSpec s, RuleEngine engine) {
        return engine.run(s, "proto").render();
    }

    private RuleEngine engine() {
        return RuleCatalog.standard()
                .engineFor("proto")
                .templateSet(SourceCode.TemplateSet.V3);
    }

    // ------------------------------------------------------------------

    @Test
    public void protoArtifact_rendersServiceMessagesAndEnums() {
        String out = render(carInfoService(), engine());

        String expected =
                "syntax = \"proto3\";\n"
              + "\n"
              + "option java_package = \"io.elasticore.blueprint.proto\";\n"
              + "option java_multiple_files = true;\n"
              + "\n"
              + "service CarInfoPortService {\n"
              + "  // " + RPC_DESC + "\n"
              + "  rpc findByBrand (CarInfo2Input) returns (CarInfo2Output);\n"
              + "}\n"
              + "\n"
              + "message CarInfo2Input {\n"
              + "  string brand = 1;\n"
              + "  CarInfo23Input inputType = 2;\n"
              + "  BoardType boType = 3;\n"
              + "\n"
              + "  message CarInfo23Input {\n"
              + "    string brand = 1;\n"
              + "  }\n"
              + "}\n"
              + "\n"
              + "message CarInfo2Output {\n"
              + "  string brand = 1;\n"
              + "  string description = 2;\n"
              + "  string modelId = 3;\n"
              + "}\n"
              + "\n"
              + "enum BoardType {\n"
              + "  PUBLIC = 0;\n"
              + "  PRIVATE = 1;\n"
              + "}\n";

        assertEquals(expected, out);
    }

    @Test
    public void noTrailingWhitespaceOnAnyLine() {
        String out = render(carInfoService(), engine());
        int lineNo = 0;
        for (String line : out.split("\n", -1)) {
            lineNo++;
            assertFalse(!line.isEmpty() && line.endsWith(" "),
                    "line " + lineNo + " has trailing whitespace: [" + line + "]");
        }
    }

    /** Deterministic ordering: the 2.x HashSet-based message order is fixed here. */
    @Test
    public void repeatedRuns_produceIdenticalOutput() {
        String first = render(carInfoService(), engine());
        for (int i = 0; i < 20; i++) {
            assertEquals(first, render(carInfoService(), engine()),
                    "proto output must be deterministic across runs");
        }
    }

    @Test
    public void suppressEnumRule_dropsEnumBlockOnly() {
        String out = render(carInfoService(), engine().suppress("v3-proto-enums"));

        assertFalse(out.contains("enum BoardType"), out);
        // the field referencing the enum type stays; only the block is gone
        assertTrue(out.contains("BoardType boType = 3;"), out);
        assertTrue(out.contains("message CarInfo2Output {"), out);
    }

    // ------------------------------------------------------------------
    // output identity: non-Java paths and extensions
    // ------------------------------------------------------------------

    @Test
    public void protoTarget_usesProtoRootAndExtension() {
        ArtifactTarget t = carInfoService().target();

        assertEquals("proto", t.scheme());
        assertEquals("proto", t.extension());
        // package is a proto option, not a directory
        assertEquals("src/main/proto/CarInfoPortService.proto", t.relativePath());
    }

    @Test
    public void javaTarget_keepsPackageDirectories() {
        ArtifactTarget t = ArtifactTarget.java("kr.co.acme.entity", "Order");
        assertEquals("src/main/java/kr/co/acme/entity/Order.java", t.relativePath());
    }

    @Test
    public void resourceTarget_supportsXmlAndOtherOutputs() {
        ArtifactTarget t = ArtifactTarget.resource(
                "src/main/resources", "xml", "mapper/shop", "OrderMapper");
        assertEquals("src/main/resources/mapper/shop/OrderMapper.xml", t.relativePath());
        assertEquals("xml", t.extension());
    }
}
