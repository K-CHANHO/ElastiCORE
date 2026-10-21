package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.DtoBaseRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coverage for the SrcFilePublisher DTO emission points that the DTO golden
 * of the artifact-chain test does not reach: validation beyond @NotNull and a
 * bare @Size, the whole @JsonProperty family, and the typed default literal.
 *
 * Where a case exists in the committed template sources the expected text is
 * taken from there, so these are golden-backed rather than invented.
 */
public class DtoGapCoverageTest {

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

    private EntitySpec dto(String name) {
        return new EntitySpec(name);
    }

    // ---- validation ----------------------------------------------------

    /**
     * AttachmentDTO.fileSize in elasticore-template, generated from
     * "fileSize: long @min(0)", is exactly:
     *   @Schema(description = "fileSize"  )
     *   @jakarta.validation.constraints.Min(0)
     */
    @Test
    public void minValue_matchesCommittedAttachmentDto() {
        EntitySpec e = dto("Attachment")
                .addField(loader.parse("fileSize", "long @min(0)"));

        String out = render(e);

        assertTrue(out.contains("@Schema(description = \"fileSize\"  )"), out);
        assertTrue(out.contains("@jakarta.validation.constraints.Min(0)"), out);
        assertTrue(out.contains("private Long fileSize;"), out);
    }

    /**
     * ArticleDTO.viewCount, from "viewCount: long @default(0) @min(0)", shows
     * that the DTO applies the typed default literal too: "= 0L", not "= 0".
     */
    @Test
    public void longDefault_getsTypedLiteralAsInCommittedArticleDto() {
        EntitySpec e = dto("Article")
                .addField(loader.parse("viewCount", "long @default(0) @min(0)"));

        String out = render(e);

        assertTrue(out.contains("@jakarta.validation.constraints.Min(0)"), out);
        assertTrue(out.contains("@Builder.Default"), out);
        assertTrue(out.contains("private Long viewCount = 0L;"),
                "long default must carry the L suffix:\n" + out);
    }

    @Test
    public void maxValue_emitsMaxAnnotation() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("qty", "int @max(999)"));

        assertTrue(render(e).contains("@jakarta.validation.constraints.Max(999)"), render(e));
    }

    /** @length(min,max) produces one @Size carrying both, separated by " , ". */
    @Test
    public void sizeCarriesMinAndMaxTogether() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("memo", "string @length(min=1,max=200)"));

        assertTrue(render(e).contains("@Size(min=1 , max=200)"), render(e));
    }

    /** A plain sized string keeps the max-only form seen in the DTO goldens. */
    @Test
    public void sizeWithoutMin_keepsMaxOnlyForm() {
        EntitySpec e = dto("Comment")
                .addField(loader.parse("authorEmail", "string(100)"));

        assertTrue(render(e).contains("@Size(max=100)"), render(e));
    }

    /** A minimum written next to a type length used to be dropped (only max=255 came out). */
    @Test
    public void lengthMinNextToATypeLength_reachesTheSize() {
        EntitySpec e = dto("Customer")
                .addField(loader.parse("password", "string(255)! @length(min=8, max=255)"));

        assertTrue(render(e).contains("@Size(min=8 , max=255)"), render(e));
    }

    @Test
    public void legacyMinSize_fillsTheMinPart() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("code", "string(20) @minsize(3)"));

        assertTrue(render(e).contains("@Size(min=3 , max=20)"), render(e));
    }

    @Test
    public void notBlank_emitsAnnotation() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("orderNo", "string(30) @notblank"));

        assertTrue(render(e).contains("@NotBlank"), render(e));
    }

    @Test
    public void dateConstraints_emitWithTheir2xMessages() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("email", "string(100) @email"))
                .addField(loader.parse("birthDate", "date @past"))
                .addField(loader.parse("dueDate", "date @future"))
                .addField(loader.parse("regDate", "date @pastorpresent"))
                .addField(loader.parse("planDate", "date @futureorpresent"));

        String out = render(e);

        assertTrue(out.contains("@jakarta.validation.constraints.Email(message = \"Email format invalid\")"), out);
        assertTrue(out.contains("@jakarta.validation.constraints.Past(message = \"Date must be past\")"), out);
        assertTrue(out.contains("@jakarta.validation.constraints.Future(message = \"Date must be future\")"), out);
        assertTrue(out.contains("@jakarta.validation.constraints.PastOrPresent(message = \"Date must be past or present\")"), out);
        assertTrue(out.contains("@jakarta.validation.constraints.FutureOrPresent(message = \"Date must be future or present\")"), out);
    }

    @Test
    public void pattern_emitsMultiLineAnnotation() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("code", "string(10) @pattern(regexp=abc,message=bad)"));

        String out = render(e);

        assertTrue(out.contains("@jakarta.validation.constraints.Pattern("), out);
        assertTrue(out.contains(" regexp=\"abc\","), out);
        assertTrue(out.contains(" msg=\"bad\""), out);
    }

    /**
     * The golden above is the compatible output, which stays as 2.x wrote it.
     * The clean output writes Jakarta's attribute, message, on one line.
     */
    @Test
    public void pattern_cleanMode_usesMessage() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("code", "string(10) @pattern(regexp=abc,message=bad)"));

        String out = new RuleEngine().register(new DtoBaseRules())
                .templateSet(SourceCode.TemplateSet.V3)
                .run(e, "dto").renderFieldBlock(1);

        assertTrue(out.contains("@Pattern(regexp = \"abc\", message = \"bad\")"), out);
        assertFalse(out.contains("msg="), out);
    }

    // ---- json ----------------------------------------------------------

    @Test
    public void jsonName_emitsJsonProperty() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("orderNo", "string(30) @json(order_no)"));

        assertTrue(render(e).contains("@JsonProperty(\"order_no\")"), render(e));
    }

    @Test
    public void readOnly_emitsAccessAttribute() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("createdAt", "string(30) @readonly"));

        assertTrue(render(e).contains("@JsonProperty(access=JsonProperty.Access.READ_ONLY)"), render(e));
    }

    @Test
    public void writeOnly_emitsAccessAttribute() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("password", "string(60) @writeonly"));

        assertTrue(render(e).contains("@JsonProperty(access=JsonProperty.Access.WRITE_ONLY)"), render(e));
    }

    /** A name combined with an access mode produces value= plus access=. */
    @Test
    public void jsonNameWithReadOnly_combinesValueAndAccess() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("createdAt", "string(30) @json(created_at) @readonly"));

        assertTrue(render(e).contains(
                "@JsonProperty(value=\"created_at\", access=JsonProperty.Access.READ_ONLY)"), render(e));
    }

    @Test
    public void jsonIgnore_emitsAnnotation() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("internal", "string(30) @jsonignore"));

        assertTrue(render(e).contains("@JsonIgnore"), render(e));

        EntitySpec alias = dto("Order")
                .addField(loader.parse("internal", "string(30) @ignore"));
        assertTrue(render(alias).contains("@JsonIgnore"), render(alias));
    }

    /**
     * The 2.x @JsonSerialize emission cannot fire: JSON_SERIALIZE_USE is
     * {"jsonSerialize.using"} while annotation keys are stored lowercase
     * ("jsonserialize"), so Field.getAnnotation never matches. Verified
     * directly below, and reproduced faithfully so the compatibility mode
     * emits exactly what 2.x emits.
     */
    @Test
    public void jsonSerialize_isUnreachableIn2x() {
        Field f = loader.parse("amount", "long @jsonSerialize(using=MoneySerializer.class)");

        // the annotation IS present, only under its lowercase key
        assertTrue(f.getAnnotationMap().containsKey("jsonserialize"),
                String.valueOf(f.getAnnotationMap().keySet()));
        // the constant 2.x uses misses it
        assertTrue(f.getAnnotationValue("jsonSerialize.using") == null,
                "if this now resolves, 2.x behaviour changed and the rule must be revisited");

        EntitySpec e = dto("Order").addField(f);
        assertFalse(render(e).contains("JsonSerialize"), render(e));
    }

    // ---- ordering -------------------------------------------------------

    /** Schema, validation, json and the initializer land in publisher order. */
    @Test
    public void emissionOrder_followsPublisherCallOrder() {
        EntitySpec e = dto("Order")
                .addField(loader.parse("orderNo",
                        "string(30)! @json(order_no) @default(NEW)"));

        String out = render(e);

        int schema = out.indexOf("@Schema");
        int notNull = out.indexOf("@NotNull");
        int size = out.indexOf("@Size");
        int json = out.indexOf("@JsonProperty");
        int builder = out.indexOf("@Builder.Default");
        int decl = out.indexOf("private String orderNo");

        assertTrue(schema >= 0 && notNull > schema, out);
        assertTrue(size > notNull, out);
        assertTrue(json > size, out);
        assertTrue(builder > json, out);
        assertTrue(decl > builder, out);
        // string default is quoted
        assertTrue(out.contains("= \"NEW\";"), out);
        assertFalse(out.contains("= NEW;"), out);
    }
}
