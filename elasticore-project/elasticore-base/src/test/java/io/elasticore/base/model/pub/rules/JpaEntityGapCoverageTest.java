package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.JpaEntityBaseRules;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coverage for the EntitySrcFilePublisher emission points that the
 * elasticore-template project does not exercise, so elcoreRulesCheck cannot
 * catch a regression in them. Each case states the 2.x behaviour it mirrors.
 *
 * Emission order follows loadFieldInfo: description block, @Id family,
 * @Comment, relation, @Column/@Formula, @JdbcTypeCode, element collection,
 * @NotAudited, @JoinColumn from @join, native annotations, enum converter,
 * then the initializer.
 */
public class JpaEntityGapCoverageTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private String render(EntitySpec e) {
        return new RuleEngine().register(new JpaEntityBaseRules()).run(e).renderFieldBlock();
    }

    private EntitySpec entity(String name) {
        return new EntitySpec(name);
    }

    // ---- setFieldDesc -------------------------------------------------

    /** @label produces the description block AND the @Comment annotation. */
    @Test
    public void label_producesDescriptionBlockAndCommentAnnotation() {
        EntitySpec e = entity("Order")
                .addField(loader.parse("orderNo", "string(30) @label(order number)"));

        String out = render(e);

        assertTrue(out.contains("/*\n  order number\n*/"), out);
        assertTrue(out.contains("@Comment(\"order number\")"), out);
        // the description block precedes the annotations
        assertTrue(out.indexOf("/*") < out.indexOf("@Comment"), out);
    }

    /**
     * The 2.x "// calcRequired:" marker cannot fire: its annotation constant is
     * {"calcRequired"} while Field.getAnnotation looks the name up verbatim in a
     * map whose keys are lowercase. The rules reproduce that faithfully rather
     * than adding a line the compatibility mode would not otherwise contain.
     */
    @Test
    public void calcRequired_isUnreachableIn2x() {
        EntitySpec e = entity("Order")
                .addField(loader.parse("amount", "long @calcRequired(true)"));

        String out = render(e);
        assertFalse(out.contains("calcRequired"),
                "2.x cannot emit this marker; v3 must not either:\n" + out);
        // the field itself is still emitted normally
        assertTrue(out.contains("private Long amount;"), out);
    }

    // ---- setFieldPkInfo ------------------------------------------------

    /** @sequence on a NON-pk field uses SequenceStyleGenerator, not IDENTITY. */
    @Test
    public void sequenceOnNonPkField_emitsGenericGenerator() {
        EntitySpec e = entity("Order")
                .addField(loader.parse("orderId", "long @id"))
                .addField(loader.parse("serialNo", "long @sequence"));

        String out = render(e);

        assertTrue(out.contains(
                "@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"seq_gen\")"), out);
        assertTrue(out.contains("@GenericGenerator("), out);
        assertTrue(out.contains("name = \"seq_gen\","), out);
        assertTrue(out.contains("strategy = \"org.hibernate.id.enhanced.SequenceStyleGenerator\","), out);
        assertTrue(out.contains("@Parameter(name = \"sequence_name\", value = \"Order_serialNo_seq\"),"), out);
        assertTrue(out.contains("@Parameter(name = \"initial_value\", value = \"1\"),"), out);
        assertTrue(out.contains("@Parameter(name = \"increment_size\", value = \"1\")"), out);
        // the pk keeps the plain IDENTITY form
        assertTrue(out.contains("@Id"), out);
    }

    // ---- setRelationInfo: @Embedded and @AttributeOverrides ------------

    @Test
    public void embeddedWithPrefix_emitsAttributeOverrides() {
        EntitySpec e = entity("Customer")
                .knownEntity("Address")
                .knownEmbeddable("Address", Arrays.asList("zipCode", "street"))
                .addField(loader.parse("homeAddress", "Address @embedded(home)"));

        String out = render(e);

        assertTrue(out.contains("@Embedded"), out);
        assertTrue(out.contains("@AttributeOverrides({"), out);
        assertTrue(out.contains("  @AttributeOverride(name = \"zipCode\", column = @Column(name = \"home_zip_code\"))"), out);
        assertTrue(out.contains(" , @AttributeOverride(name = \"street\", column = @Column(name = \"home_street\"))"), out);
        assertTrue(out.contains("})"), out);
        // an embedded field gets no relation annotation
        assertFalse(out.contains("@ManyToOne"), out);
    }

    /** An @embeddable target is detected even without an explicit @embedded. */
    @Test
    public void embeddableTargetType_impliesEmbedded() {
        EntitySpec e = entity("Order")
                .knownEntity("Address")
                .knownEmbeddable("Address", Arrays.asList("zipCode"))
                .addField(loader.parse("shippingAddress", "Address"));

        String out = render(e);
        assertTrue(out.contains("@Embedded"), out);
        assertFalse(out.contains("@ManyToOne"), out);
    }

    // ---- @joinBy -------------------------------------------------------

    /** @joinBy reuses another field's column read-only. */
    @Test
    public void joinBy_emitsReadOnlyJoinColumn() {
        EntitySpec e = entity("Comment")
                .knownEntity("Article")
                .addField(loader.parse("articleId", "string(36)"))
                .addField(loader.parse("article", "Article @ManyToOne @joinby(articleId)"));

        String out = render(e);

        assertTrue(out.contains("@JoinColumn(name = \"article_id\" , insertable = false, updatable = false)"), out);
    }

    // ---- setFieldColumnAnnotation: formula, unique, discriminator ------

    /** A formula carries @Formula alone: Hibernate ignores @Formula beside a @Column. */
    @Test
    public void formula_suppressesColumnNameAndEmitsFormula() {
        EntitySpec e = entity("Order")
                .addField(loader.parse("total", "long @formula(price * qty)"));

        String out = render(e);

        assertTrue(out.contains("@Formula("), out);
        assertFalse(out.contains("@Column"), "a formula is not a column:\n" + out);
    }

    @Test
    public void unique_emitsUniqueAttribute() {
        EntitySpec e = entity("Order")
                .addField(loader.parse("orderNo", "string(30) @unique"));

        assertTrue(render(e).contains("unique = true"), render(e));
    }

    @Test
    public void discriminator_emitsInsertableUpdatableFalse() {
        EntitySpec e = entity("Order")
                .addField(loader.parse("kind", "string(10) @discriminator"));

        String out = render(e);
        assertTrue(out.contains("insertable = false"), out);
        assertTrue(out.contains("updatable = false"), out);
    }

    // ---- setEnumListAnnotation ----------------------------------------

    /** List<Enum> becomes an element collection, not a plain column. */
    @Test
    public void enumList_emitsElementCollection() {
        EntitySpec e = entity("Article")
                .knownEnum("ArticleStatus", "3", true)
                .addField(loader.parse("statuses", "List<ArticleStatus>"));

        String out = render(e);

        assertTrue(out.contains("@ElementCollection( fetch = FetchType.EAGER)"), out);
        assertTrue(out.contains(
                "@CollectionTable(name = \"article_statuses\", joinColumns = @JoinColumn(name = \"map_seq\"))"), out);
        assertTrue(out.contains("@Column(name = \"code\")"), out);
        // the element-collection column wins; no field-named column is emitted
        assertFalse(out.contains("@Column(name = \"statuses\""), out);
    }

    // ---- setJoinColumnAnnotation --------------------------------------

    @Test
    public void joinAnnotation_emitsColumnDefinitionJoinColumn() {
        EntitySpec e = entity("Order")
                .knownEntity("Customer")
                .addField(loader.parse("customer", "Customer @ManyToOne @join(cust_ref)"));

        assertTrue(render(e).contains("@JoinColumn(columnDefinition=\"cust_ref\")"), render(e));
    }

    // ---- emission order ------------------------------------------------

    /** The whole chain lands in loadFieldInfo order for one heavily annotated field. */
    @Test
    public void emissionOrder_followsPublisherCallOrder() {
        EntitySpec e = entity("Article")
                .knownEnum("ArticleStatus", "3", true)
                .addField(loader.parse("status",
                        "ArticleStatus @label(status) @notaudited @default(DRAFT)"));

        String out = render(e);

        int desc = out.indexOf("/*");
        int comment = out.indexOf("@Comment");
        int column = out.indexOf("@Column");
        int notAudited = out.indexOf("@NotAudited");
        int convert = out.indexOf("@Convert");
        int decl = out.indexOf("private ");

        assertTrue(desc >= 0 && comment > desc, out);
        assertTrue(column > comment, out);
        assertTrue(notAudited > column, out);
        assertTrue(convert > notAudited, out);
        assertTrue(decl > convert, out);
        // enum default gets its type prefix
        assertTrue(out.contains("= ArticleStatus.DRAFT;"), out);
    }
}
