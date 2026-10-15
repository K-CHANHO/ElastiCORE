package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.model.pub.rules.base.RepositoryBaseRules;
import io.elasticore.base.model.repo.Method;
import io.elasticore.base.model.repo.SqlQueryInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository query methods, ported from RepositoryFilePublisher.getMethodInfo.
 *
 * There is no golden for these: the repository DSL of the blueprints declares
 * its methods directly under the repository name, while RepositoryModelLoader
 * reads them from a "methods" entry, so no declared method currently reaches
 * a publisher and every committed repository is empty. The expectations here
 * come from the 2.x emission code itself.
 */
public class RepositoryMethodTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec repo() {
        String base = "kr.co.acme.domain";
        return new EntitySpec("Product")
                .domain("shop", "Shop")
                .pkg("repository", base + ".repository")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                .pkg("enumeration", base + ".enums")
                .addField(loader.parse("productId", "string(36) @id @genid"));
    }

    private String render(EntitySpec e) {
        return new RuleEngine().register(new RepositoryBaseRules())
                .run(e, "repository").render();
    }

    /** Builds a method as RepositoryModelLoader would, had its DSL matched. */
    private Method method(String name, String query, boolean isNative, boolean pageable,
                          String returnType, String... params) {
        Items<Field> items = Items.create(Field.class);
        for (int i = 0; i + 1 < params.length; i += 2) {
            items.addItem(loader.parse(params[i], params[i + 1]));
        }
        SqlQueryInfo info = SqlQueryInfo.creat("shop", query, isNative, pageable, null, returnType, true);
        return Method.builder()
                .name(name).query(query).isNative(isNative).pageable(pageable)
                .returnType(returnType).params(items).queryInfo(info)
                .build();
    }

    // ---- the declared query -------------------------------------------

    /** A select query is declared with @Query and binds its parameters by name. */
    @Test
    public void selectQuery_declaresQueryAndNamesItsParameters() {
        EntitySpec e = repo().addQueryMethod(method("findByCategory",
                "SELECT p FROM Product p WHERE p.category.catId = :categoryId",
                false, false, "List<Product>", "categoryId", "long"));

        String out = render(e);
        assertTrue(out.contains("@Query(nativeQuery=false, value="), out);
        assertTrue(out.contains("List<Product> findByCategory(@Param(\"categoryId\") Long categoryId);"), out);
    }

    /** Several parameters are separated the way 2.x writes them. */
    @Test
    public void severalParameters_areSeparatedWithASpaceBeforeTheComma() {
        EntitySpec e = repo().addQueryMethod(method("findByPriceRange",
                "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice",
                false, false, "List<Product>",
                "minPrice", "bigdecimal", "maxPrice", "bigdecimal"));

        // Parameter types render exactly as the model spells them, which for
        // a decimal is the qualified name.
        assertTrue(render(e).contains("findByPriceRange(@Param(\"minPrice\") java.math.BigDecimal minPrice"
                        + " ,@Param(\"maxPrice\") java.math.BigDecimal maxPrice);"),
                render(e));
    }

    /** A native query says so in the annotation. */
    @Test
    public void nativeQuery_isMarkedNative() {
        EntitySpec e = repo().addQueryMethod(method("countRows",
                "SELECT count(*) FROM shop_products", true, false, "List<Product>"));

        assertTrue(render(e).contains("@Query(nativeQuery=true, value="), render(e));
    }

    // ---- writing queries -----------------------------------------------

    /** A query that writes is marked modifying and transactional. */
    @Test
    public void writingQuery_isModifyingAndTransactional() {
        EntitySpec e = repo().addQueryMethod(method("deactivateAll",
                "UPDATE Product p SET p.active = false WHERE p.stockQuantity = 0",
                false, false, "int"));

        String out = render(e);
        assertTrue(out.contains("@org.springframework.data.jpa.repository.Modifying"), out);
        assertTrue(out.contains("@jakarta.transaction.Transactional"), out);
        assertTrue(out.indexOf("Modifying") < out.indexOf("Transactional"), out);
    }

    /** A select query gets neither marker. */
    @Test
    public void selectQuery_isNeitherModifyingNorTransactional() {
        EntitySpec e = repo().addQueryMethod(method("findActive",
                "SELECT p FROM Product p WHERE p.active = true", false, false, "List<Product>"));

        String out = render(e);
        assertFalse(out.contains("@org.springframework.data.jpa.repository.Modifying"), out);
        assertFalse(out.contains("Transactional"), out);
    }

    // ---- paging ---------------------------------------------------------

    /** A pageable JPQL query takes the Pageable as a trailing parameter. */
    @Test
    public void pageableQuery_takesATrailingPageable() {
        EntitySpec e = repo().addQueryMethod(method("findPaged",
                "SELECT p FROM Product p WHERE p.active = :active",
                false, true, "Page<Product>", "active", "boolean"));

        assertTrue(render(e).contains(
                "findPaged(@Param(\"active\") Boolean active, Pageable pageable);"), render(e));
    }

    /** A native query pages itself, so no Pageable is appended. */
    @Test
    public void pageableNativeQuery_takesNoPageable() {
        EntitySpec e = repo().addQueryMethod(method("findPagedNative",
                "SELECT * FROM shop_products", true, true, "List<Product>"));

        assertFalse(render(e).contains("Pageable pageable"), render(e));
    }

    // ---- projection ------------------------------------------------------

    /**
     * A query projecting onto a DTO cannot be typed as that DTO, because JPA
     * hands back raw tuples.
     */
    @Test
    public void dtoProjection_isDeclaredAsRawTuples() {
        EntitySpec e = repo().knownDto("ProductSummaryDTO", "Product")
                .addQueryMethod(method("summaries",
                        "SELECT p.name, p.price FROM Product p", false, false,
                        "List<ProductSummaryDTO>"));

        String out = render(e);
        assertTrue(out.contains("List<Object[]> summaries();"), out);
    }

    /** A query returning entities keeps its declared type. */
    @Test
    public void entityProjection_keepsItsDeclaredType() {
        EntitySpec e = repo().knownDto("ProductSummaryDTO", "Product")
                .addQueryMethod(method("all",
                        "SELECT p FROM Product p", false, false, "List<Product>"));

        assertTrue(render(e).contains("List<Product> all();"), render(e));
    }

    // ---- conditional queries ---------------------------------------------

    /** A query assembled at runtime is not declared on the interface. */
    @Test
    public void conditionalQuery_isNotDeclared() {
        EntitySpec e = repo().addQueryMethod(method("findDynamic",
                "SELECT p FROM Product p WHERE 1=1 /* if: name */ AND p.name = :name",
                false, false, "List<Product>", "name", "string"));

        assertFalse(render(e).contains("findDynamic"), render(e));
    }

    // ---- imports ----------------------------------------------------------

    /** The DTO and enum imports appear only once methods are declared. */
    @Test
    public void declaredMethods_bringInTheDtoAndEnumImports() {
        EntitySpec e = repo().knownDto("ProductDTO", "Product")
                .addQueryMethod(method("findActive",
                        "SELECT p FROM Product p WHERE p.active = true", false, false, "List<Product>"));

        String out = render(e);
        assertTrue(out.contains("import kr.co.acme.domain.dto.*;"), out);
        assertTrue(out.contains("import kr.co.acme.domain.enums.*;"), out);
    }

    /** A repository without methods keeps the minimal import list. */
    @Test
    public void repositoryWithoutMethods_keepsTheMinimalImports() {
        String out = render(repo());
        assertFalse(out.contains("import kr.co.acme.domain.dto.*;"), out);
        assertFalse(out.contains("import kr.co.acme.domain.enums.*;"), out);
        assertTrue(out.contains("import kr.co.acme.domain.entity.*;"), out);
    }

    // ---- layout ------------------------------------------------------------

    /**
     * Every emitted line inherits the slot's indentation, including the blank
     * separator that follows each method - that is what a 2.x Paragraph does.
     */
    @Test
    public void everyEmittedLine_inheritsTheSlotIndentation() {
        EntitySpec e = repo().addQueryMethod(method("findActive",
                "SELECT p FROM Product p WHERE p.active = true", false, false, "List<Product>"));

        String out = render(e);
        assertTrue(out.contains("\n    @Query(nativeQuery=false, value="), out);
        assertTrue(out.contains("\n    List<Product> findActive();\n    \n"), out);
    }

    // ---- the same methods in Kotlin -----------------------------------

    private String renderKotlin(EntitySpec e) {
        return new RuleEngine().register(new RepositoryBaseRules())
                .templateSet(SourceCode.TemplateSet.KOTLIN)
                .run(e, "repository").render();
    }

    /**
     * No blueprint repository declares a query, so the compile check in
     * elasticore-template-kt never sees one of these. These state the Kotlin
     * signature form directly.
     */
    @Test
    public void kotlinSignature_namesTheFunctionAndTrailsTheReturnType() {
        EntitySpec e = repo().addQueryMethod(method("findByCategory",
                "SELECT p FROM Product p WHERE p.category.catId = :categoryId",
                false, false, "List<Product>", "categoryId", "long"));

        String out = renderKotlin(e);
        assertTrue(out.contains(
                "fun findByCategory(@Param(\"categoryId\") categoryId: Long): List<Product>"), out);
        assertFalse(out.contains("List<Product> findByCategory"), out);
    }

    /** A Kotlin declaration is not terminated. */
    @Test
    public void kotlinSignature_carriesNoSemicolon() {
        EntitySpec e = repo().addQueryMethod(method("countRows",
                "SELECT count(*) FROM shop_products", true, false, "List<Product>"));

        assertFalse(renderKotlin(e).contains("countRows();"), renderKotlin(e));
    }

    /** The key type is a model type name, so it goes through the seam too. */
    @Test
    public void kotlinKeyType_isKotlins() {
        EntitySpec e = new EntitySpec("Counter")
                .domain("shop", "Shop")
                .pkg("repository", "kr.co.acme.domain.repository")
                .pkg("entity", "kr.co.acme.domain.entity")
                .addField(loader.parse("seq", "int @id"));

        assertTrue(renderKotlin(e).contains("JpaRepository<Counter, Int>"), renderKotlin(e));
    }
}
