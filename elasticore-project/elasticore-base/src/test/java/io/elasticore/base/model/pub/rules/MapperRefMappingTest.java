package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.MapperBaseRules;
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
 * The mapper cases the Comment chain does not reach, compared against the
 * committed ShopMapper: a relation converted through another DTO's mapper,
 * a @ref onto the relation's primary key, a relation list with no DTO of its
 * own, and template fields dropped from the DTO with "--".
 *
 * Product is the entity that carries all of them at once.
 */
public class MapperRefMappingTest {

    private static final String GEN_ROOT =
            "elasticore-template/src/main/java/io/elasticore/blueprint/domain/shop/";

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    /**
     * blueprint/shop/model/entity.yml Product, plus the AuditEntity fields it
     * inherits, and blueprint/shop/model/dto.yml ProductDTO, which adds two
     * @ref fields at the front and drops four template fields.
     */
    private EntitySpec productMapper() {
        EntitySpec e = new EntitySpec("Product")
                .knownEntity("Category").knownEntity("OrderItem")
                .knownDto("CategoryDTO", "Category")
                .knownEntityPk("Category", "catId")
                .parent("AuditEntity")
                .domain("shop", "Shop");

        // Entity side, in declaration order.
        for (String[] f : PRODUCT_FIELDS) e.addField(loader.parse(f[0], f[1]));
        e.addParentField(loader.parse("createdBy", "string(50) @updatable(false)"));
        e.addParentField(loader.parse("createDate", "datetime @updatable(false)"));
        e.addParentField(loader.parse("lastModifiedBy", "string(50)"));
        e.addParentField(loader.parse("lastModifiedDate", "datetime"));

        // DTO side: the @ref fields lead, then the template fields that
        // survived, in the same order.
        e.addDtoField(loader.parse("categoryName", "string @ref(category.name)"));
        e.addDtoField(loader.parse("categoryCatId", "long @ref(category.catId)"));
        for (String[] f : PRODUCT_FIELDS) {
            if (DROPPED_BY_DTO.contains(f[0])) continue;
            e.addDtoField(loader.parse(f[0], f[1]));
        }
        e.addParentDtoField(loader.parse("createdBy", "string(50) @updatable(false)"));
        e.addParentDtoField(loader.parse("lastModifiedBy", "string(50)"));
        return e;
    }

    private static final List<String> DROPPED_BY_DTO =
            java.util.Arrays.asList("thumbnail", "manualContent");

    private static final String[][] PRODUCT_FIELDS = {
            {"productId", "string(36) @id @genid"},
            {"name", "string(200)! @search(like)"},
            {"sku", "string(50)! @search(eq)"},
            {"description", "text"},
            {"price", "bigdecimal(12.2)! @min(0) @search(between)"},
            {"costPrice", "bigdecimal(12.2) @min(0)"},
            {"stockQuantity", "int @default(0) @min(0) @search(>=)"},
            {"weight", "float"},
            {"rating", "double @default(0.0) @min(0) @max(5)"},
            {"active", "boolean @default(true) @search(eq)"},
            {"launchDate", "date @search(between)"},
            {"lastRestocked", "datetime"},
            {"uuid", "string(36)"},
            {"metadata", "text"},
            {"thumbnail", "string(500)"},
            {"manualContent", "text"},
            {"metadataRaw", "string @columnDef('jsonb')"},
            {"oldPayload", "string @columnDef('json')"},
            {"customXml", "string @columnDef('xml')"},
            {"forceLong", "string @columnDef('jsonb') @jdbcType('LONG32VARCHAR')"},
            {"tags", "string[] @env:stringarray"},
            {"category", "Category @dtype(CategoryDTO) @ManyToOne @fetchjoin @notaudited"},
            {"orderItems", "List<OrderItem> @OneToMany(mappedBy=\"product\") @notaudited"},
    };

    private SourceCode render() {
        return new RuleEngine().register(new MapperBaseRules())
                .register(new io.elasticore.base.model.pub.rules.base.MapperAssembleRules())
                .run(productMapper(), "mapper");
    }

    // ---- full sections ---------------------------------------------------

    @Test
    public void forwardSection_matchesGolden() throws IOException {
        String expected = section(
                "    public static void mapping(Product from, ProductDTO to, boolean isSkipNull){",
                "BiFunction<Product, ProductDTO");
        assertEquals(expected, render().renderBlock("forward"));
    }

    @Test
    public void reverseSection_matchesGolden() throws IOException {
        String expected = section(
                "    public static void mapping(ProductDTO from, Product to, boolean isSkipNull){",
                "BiFunction<ProductDTO, Product");
        assertEquals(expected, render().renderBlock("reverse"));
    }

    // ---- the individual cases, so a failure names itself -------------------

    /** A relation with a DTO of its own converts through that DTO's mapper. */
    @Test
    public void relationField_convertsThroughItsMapper() {
        String out = render().renderBlock("forward");
        assertTrue(out.contains(
                "setVal(from.getCategory(), to::setCategory, isSkipNull, e->toDTO(e,c));"), out);
    }

    /** The reverse direction converts the same relation back. */
    @Test
    public void relationField_convertsBackWithoutEntityManager() {
        String out = render().renderBlock("reverse");
        assertTrue(out.contains(
                "setVal(from.getCategory(), to::setCategory, isSkipNull, e->toEntity(e,c));"), out);
    }

    /** With an EntityManager in scope the conversion passes it along. */
    @Test
    public void relationField_convertsBackThroughTheEntityManager() {
        String out = render().renderBlock("reverse");
        assertTrue(out.contains(
                "setVal(from.getCategory(), to::setCategory, isSkipNull, e->toEntity(e,c,em));"), out);
    }

    /**
     * A relation list whose element type has no DTO cannot be converted, so
     * the field drops out of both directions.
     */
    @Test
    public void relationList_withoutDto_isSkipped() {
        SourceCode code = render();
        assertTrue(!code.renderBlock("forward").contains("OrderItem"), "forward kept orderItems");
        assertTrue(!code.renderBlock("reverse").contains("OrderItem"), "reverse kept orderItems");
    }

    /** A @ref onto a plain column writes through the relation object. */
    @Test
    public void refOnPlainColumn_writesThroughTheRelation() {
        String out = render().renderBlock("reverse");
        assertTrue(out.contains("        if(hasValue(from.getCategoryName()) && hasValue(to.getCategory()))\n"
                + "            to.getCategory().setName(from.getCategoryName());"), out);
    }

    /** A @ref onto the relation's primary key attaches a new instance instead. */
    @Test
    public void refOnPrimaryKey_attachesAnInstance() {
        String out = render().renderBlock("reverse");
        assertTrue(out.contains("        if(hasValue(from.getCategoryCatId())){\n"
                + "            Category t = new Category();\n"
                + "            t.setCatId(from.getCategoryCatId());\n"
                + "            to.setCategory(t);\n"
                + "        }"), out);
    }

    /** With an EntityManager the same @ref attaches a managed reference. */
    @Test
    public void refOnPrimaryKey_attachesAManagedReference() {
        String out = render().renderBlock("reverse");
        assertTrue(out.contains("            if(em != null){\n"
                + "                to.setCategory(em.getReference(Category.class, from.getCategoryCatId()));\n"
                + "            }"), out);
    }

    /**
     * A template field the DTO dropped with "--" has nothing to read from, so
     * the reverse direction leaves it out.
     */
    @Test
    public void fieldDroppedByTheDto_isAbsentFromTheReverseBody() {
        String out = render().renderBlock("reverse");
        assertTrue(!out.contains("setThumbnail"), out);
        assertTrue(!out.contains("setManualContent"), out);
        assertTrue(!out.contains("setCreateDate"), out);
        assertTrue(!out.contains("setLastModifiedDate"), out);
    }

    // ---- @forceupdate ------------------------------------------------------

    /**
     * A @forceupdate field is assigned straight through, so the value is
     * written even when it is null.
     */
    @Test
    public void forceUpdateBaseField_isAssignedStraightThrough() {
        EntitySpec e = new EntitySpec("Product")
                .addField(loader.parse("name", "string(200) @forceupdate"))
                .addDtoField(loader.parse("name", "string(200) @forceupdate"));

        String out = new RuleEngine().register(new MapperBaseRules())
                .register(new io.elasticore.base.model.pub.rules.base.MapperAssembleRules())
                .run(e, "mapper").renderBlock("forward");

        assertTrue(out.contains("        to.setName(from.getName());"), out);
        assertFalse(out.contains("setVal(from.getName()"), out);
    }

    /**
     * A converted @forceupdate field goes through its mapper method, and the
     * call is closed.
     *
     * 2.x opened that call and never closed it, so a converted field emitted
     * code that does not compile. Only base types, which need no call at all,
     * reached it in practice.
     */
    @Test
    public void forceUpdateRelationField_closesTheMapperCall() {
        EntitySpec e = new EntitySpec("Product")
                .knownEntity("Category").knownDto("CategoryDTO", "Category")
                .addField(loader.parse("category", "Category @dtype(CategoryDTO) @forceupdate"))
                .addDtoField(loader.parse("category", "Category @dtype(CategoryDTO) @forceupdate"));

        SourceCode code = new RuleEngine().register(new MapperBaseRules())
                .register(new io.elasticore.base.model.pub.rules.base.MapperAssembleRules()).run(e, "mapper");

        assertTrue(code.renderBlock("forward").contains(
                "        to.setCategory(toDTO(from.getCategory()));"), code.renderBlock("forward"));
        assertTrue(code.renderBlock("reverse").contains(
                "        to.setCategory(toEntity(from.getCategory()));"), code.renderBlock("reverse"));
    }

    /** Only the forward direction marks where the abstract parent begins. */
    @Test
    public void parentBoundary_isForwardOnly() {
        SourceCode code = render();
        assertTrue(code.renderBlock("forward").contains("if(isSkip(\"Product\",\"AuditEntity\")) return;"),
                code.renderBlock("forward"));
        assertTrue(!code.renderBlock("reverse").contains("isSkip("), code.renderBlock("reverse"));
    }

    // ---- golden access ----------------------------------------------------

    /** Extracts a mapper section, ending at the trailing BiFunction method. */
    private String section(String startLine, String biFunctionMark) throws IOException {
        List<String> lines = readLines(golden("dto/ShopMapper.java"));
        int s = lines.indexOf(startLine);
        assertTrue(s >= 0, "section start not found: " + startLine);

        int end = -1;
        boolean inBiFunction = false;
        for (int i = s; i < lines.size(); i++) {
            if (lines.get(i).contains(biFunctionMark)) inBiFunction = true;
            if (inBiFunction && lines.get(i).equals("    }")) { end = i; break; }
        }
        assertTrue(end > s, "section end not found after " + biFunctionMark);

        StringBuilder expected = new StringBuilder();
        for (int i = s; i <= end; i++) expected.append(lines.get(i)).append('\n');
        return expected.toString();
    }

    private File golden(String relative) {
        String[] candidates = {
                "../" + GEN_ROOT + relative,
                "elasticore-project/" + GEN_ROOT + relative,
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.isFile()) return f;
        }
        throw new IllegalStateException("Golden file not found: " + relative
                + " from cwd " + new File(".").getAbsolutePath());
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
