package io.elasticore.base.model.pub.schema;

import io.elasticore.base.model.ConstantParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The schema file: where it lands, and in what encoding.
 *
 * Both were wrong at once and neither was caught. It was written against the
 * root directory, so in a multi-module build one module's schema went to the
 * repository root while the module's own copy went stale - the same
 * root-versus-module confusion the generated sources had. And it was written
 * through a FileWriter, which is the platform default encoding: every Korean
 * label the DSL carries came back as mojibake, in a file that had been
 * committed that way for months.
 *
 * Neither shows up in a content comparison of generated Java, which is what
 * every other gate here does. So they are asserted directly.
 */
public class SchemaFileTest {

    @TempDir
    Path tempDir;

    /**
     * A publisher holding one entity with a Korean label.
     *
     * Reached by reflection because collecting entities needs a whole loaded
     * model, and what is under test is the writing rather than the
     * collecting. The alternative - standing up a model to test a file
     * writer - is the kind of setup that makes a test nobody runs.
     */
    private SchemaCodePublisher publisherWithKoreanLabel() throws Exception {
        SchemaCodePublisher publisher = new SchemaCodePublisher();

        Field entitiesField = SchemaCodePublisher.class.getDeclaredField("allEntities");
        entitiesField.setAccessible(true);
        List<Object> entities = (List<Object>) entitiesField.get(publisher);

        Class<?> infoType = Class.forName(
                "io.elasticore.base.model.pub.schema.SchemaCodePublisher$EntitySchemaInfo");
        java.lang.reflect.Constructor<?> ctor = infoType.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object info = ctor.newInstance();

        set(info, "name", "Order");
        set(info, "domainName", "shop");
        set(info, "tableName", "shop_order");
        set(info, "javaClassName", "io.acme.shop.Order");
        set(info, "metaAnnotations", "entity");
        set(info, "label", "주문");
        set(info, "description", "주문을 담는 애그리거트");
        set(info, "fields", new java.util.ArrayList<Object>());
        entities.add(info);

        return publisher;
    }

    private void set(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("the schema is written beside the blueprint it describes")
    public void writtenBesideTheBlueprint() throws Exception {
        File blueprint = tempDir.resolve("app-shop")
                .resolve(ConstantParam.PROPERTY_ELCORE_HOME).toFile();
        assertTrue(blueprint.mkdirs());

        publisherWithKoreanLabel().generateSchemaFileInBlueprintDir(blueprint.getPath());

        File written = new File(blueprint, "schema.yml");
        assertTrue(written.isFile(),
                "the module's own blueprint directory, not a root computed from a system property");
        assertEquals(blueprint.getPath(), written.getParentFile().getPath());
    }

    @Test
    @DisplayName("nothing is written above the module")
    public void nothingLandsAtTheRoot() throws Exception {
        File module = tempDir.resolve("app-shop").toFile();
        File blueprint = new File(module, ConstantParam.PROPERTY_ELCORE_HOME);
        assertTrue(blueprint.mkdirs());

        publisherWithKoreanLabel().generateSchemaFileInBlueprintDir(blueprint.getPath());

        // The regression, stated as what must not reappear: a second schema.yml
        // under the directory the modules sit in.
        assertTrue(!new File(tempDir.toFile(), "src/main/resources/blueprint/schema.yml").exists(),
                "the repository root is not a project and nothing may be generated into it");
    }

    @Test
    @DisplayName("Korean survives the round trip, because the writer names UTF-8")
    public void koreanIsWrittenAsUtf8() throws Exception {
        File blueprint = tempDir.resolve("app-shop")
                .resolve(ConstantParam.PROPERTY_ELCORE_HOME).toFile();
        assertTrue(blueprint.mkdirs());

        publisherWithKoreanLabel().generateSchemaFileInBlueprintDir(blueprint.getPath());

        String asUtf8 = read(new File(blueprint, "schema.yml"));
        assertTrue(asUtf8.contains("주문"),
                "a platform-default writer stores this in the console codepage and it comes"
                        + " back as mojibake - which is how it was committed for months");
    }

    private String read(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
