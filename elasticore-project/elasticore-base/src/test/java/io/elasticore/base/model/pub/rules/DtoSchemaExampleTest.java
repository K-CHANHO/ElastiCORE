package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The example a DTO field documents itself with, and the pattern it formats
 * with. Both were derived from the datetime type alone, so a plain date and
 * an enum went undocumented.
 *
 * Expected text is taken from the committed shop DTOs.
 */
public class DtoSchemaExampleTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String render(EntitySpec e) {
        return new RuleEngine().register(new DtoBaseRules())
                .run(e, "dto").renderFieldBlock(1);
    }

    /**
     * ProductDTO.launchDate, from "launchDate: date", is exactly:
     *   @Schema(description = "launchDate"  , example="yyyy-MM-dd")
     *   @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
     *   @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
     */
    @Test
    public void dateField_documentsAndFormatsWithoutATimePart() {
        EntitySpec e = new EntitySpec("Product")
                .addField(loader.parse("launchDate", "date @search(between)"));

        String out = render(e);
        assertTrue(out.contains("@Schema(description = \"launchDate\"  , example=\"yyyy-MM-dd\")"), out);
        assertTrue(out.contains(
                "@org.springframework.format.annotation.DateTimeFormat(pattern = \"yyyy-MM-dd\")"), out);
        assertTrue(out.contains(
                "@com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd\")"), out);
    }

    /** ProductDTO.lastRestocked, from "lastRestocked: datetime", keeps the time part. */
    @Test
    public void dateTimeField_keepsTheTimePart() {
        EntitySpec e = new EntitySpec("Product")
                .addField(loader.parse("lastRestocked", "datetime"));

        assertTrue(render(e).contains(
                "@Schema(description = \"lastRestocked\"  , example=\"yyyy-MM-dd HH:mm:ss\")"), render(e));
    }

    /** An explicit @format wins over the type's default pattern. */
    @Test
    public void explicitFormat_overridesTheDefaultPattern() {
        EntitySpec e = new EntitySpec("Product")
                .addField(loader.parse("launchDate", "date @format(yyyyMMdd)"));

        String out = render(e);
        assertTrue(out.contains("example=\"yyyyMMdd\""), out);
        assertTrue(out.contains(
                "@org.springframework.format.annotation.DateTimeFormat(pattern = \"yyyyMMdd\")"), out);
    }

    /**
     * CustomerDTO.customerType, whose enum names "code" for @json, is exactly:
     *   @Schema(description = "customerType"  , example="I: Individual | B: Business | W: Wholesale")
     */
    @Test
    public void enumField_documentsItsConstants() {
        EnumSpec customerType = new EnumSpec("CustomerType")
                .addField(loader.parse("code", "string(1) @code"))
                .addField(loader.parse("description", "string(20)"))
                .constant("INDIVIDUAL", "I", "Individual")
                .constant("BUSINESS", "B", "Business")
                .constant("WHOLESALE", "W", "Wholesale")
                .jsonField("code");

        EntitySpec e = new EntitySpec("Customer")
                .knownEnum(customerType, "1", true)
                .addField(loader.parse("customerType", "CustomerType @search(eq)"));

        assertTrue(render(e).contains("@Schema(description = \"customerType\"  ,"
                + " example=\"I: Individual | B: Business | W: Wholesale\")"), render(e));
    }

    /** An enum without @json lists its constant names instead. */
    @Test
    public void enumWithoutJsonField_listsItsConstantNames() {
        EnumSpec status = new EnumSpec("Status")
                .addField(loader.parse("label", "string(20)"))
                .constant("OPEN", "Open")
                .constant("CLOSED", "Closed");

        EntitySpec e = new EntitySpec("Ticket")
                .knownEnum(status, "", false)
                .addField(loader.parse("status", "Status"));

        assertTrue(render(e).contains("example=\"OPEN: Open | CLOSED: Closed\""), render(e));
    }

    /** An explicit @example wins over everything derived. */
    @Test
    public void explicitExample_winsOverTheDerivedOne() {
        EntitySpec e = new EntitySpec("Product")
                .addField(loader.parse("launchDate", "date @example(2026-01-01)"));

        assertTrue(render(e).contains("example=\"2026-01-01\""), render(e));
    }

    /** A field with nothing to document carries no example. */
    @Test
    public void plainField_carriesNoExample() {
        EntitySpec e = new EntitySpec("Product")
                .addField(loader.parse("name", "string(200)!"));

        String out = render(e);
        assertTrue(out.contains("@Schema(description = \"name\" ,"
                + " requiredMode=Schema.RequiredMode.REQUIRED )"), out);
    }
}
