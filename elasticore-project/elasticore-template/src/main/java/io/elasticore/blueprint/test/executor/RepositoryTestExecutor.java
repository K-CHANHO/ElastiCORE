package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import io.elasticore.blueprint.domain.shop.entity.Product;
import io.elasticore.blueprint.domain.shop.repository.ProductRepository;
import io.elasticore.blueprint.domain.shop.repository.ShopRepositoryHelper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Test executor for Repository category.
 * Validates generated JPA repository methods including standard CRUD,
 * specification-based queries, and custom @query methods.
 */
@Slf4j
@Component
public class RepositoryTestExecutor extends AbstractTestExecutor {

    @Autowired(required = false)
    private ShopRepositoryHelper helper;

    @Override
    public TestCategory getCategory() {
        return TestCategory.REPOSITORY;
    }

    /**
     * Tests the findAll() standard JPA repository method on ProductRepository.
     */
    public TestResult testRepositoryFindAll() {
        long startTime = System.currentTimeMillis();
        try {
            if (helper == null) {
                return failure("testRepositoryFindAll",
                        "ShopRepositoryHelper bean is not available", startTime, null);
            }
            ProductRepository repo = helper.getProduct();
            if (repo == null) {
                return failure("testRepositoryFindAll",
                        "ProductRepository is null from helper", startTime, null);
            }

            List<Product> products = repo.findAll();
            log.info("testRepositoryFindAll: found {} products", products.size());
            return success("testRepositoryFindAll",
                    "findAll() returned " + products.size() + " products", startTime);
        } catch (Exception e) {
            log.error("testRepositoryFindAll failed", e);
            return failure("testRepositoryFindAll",
                    "Exception during findAll(): " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Tests the findById() standard JPA repository method on ProductRepository.
     */
    public TestResult testRepositoryFindById() {
        long startTime = System.currentTimeMillis();
        try {
            if (helper == null) {
                return failure("testRepositoryFindById",
                        "ShopRepositoryHelper bean is not available", startTime, null);
            }
            ProductRepository repo = helper.getProduct();
            if (repo == null) {
                return failure("testRepositoryFindById",
                        "ProductRepository is null from helper", startTime, null);
            }

            // Attempt findById with a non-existent id; should return empty Optional without error
            Optional<Product> result = repo.findById("test-nonexistent-id");
            log.info("testRepositoryFindById: present={}", result.isPresent());
            return success("testRepositoryFindById",
                    "findById() executed successfully, present=" + result.isPresent(), startTime);
        } catch (Exception e) {
            log.error("testRepositoryFindById failed", e);
            return failure("testRepositoryFindById",
                    "Exception during findById(): " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Tests the count() standard JPA repository method on ProductRepository.
     */
    public TestResult testRepositoryCount() {
        long startTime = System.currentTimeMillis();
        try {
            if (helper == null) {
                return failure("testRepositoryCount",
                        "ShopRepositoryHelper bean is not available", startTime, null);
            }
            ProductRepository repo = helper.getProduct();
            if (repo == null) {
                return failure("testRepositoryCount",
                        "ProductRepository is null from helper", startTime, null);
            }

            long count = repo.count();
            log.info("testRepositoryCount: count={}", count);
            return success("testRepositoryCount",
                    "count() returned " + count, startTime);
        } catch (Exception e) {
            log.error("testRepositoryCount failed", e);
            return failure("testRepositoryCount",
                    "Exception during count(): " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Tests findAll with a JPA Specification on ProductRepository.
     * Uses JpaSpecificationExecutor to verify specification-based queries work.
     */
    public TestResult testRepositoryWithSpecification() {
        long startTime = System.currentTimeMillis();
        try {
            if (helper == null) {
                return failure("testRepositoryWithSpecification",
                        "ShopRepositoryHelper bean is not available", startTime, null);
            }
            ProductRepository repo = helper.getProduct();
            if (repo == null) {
                return failure("testRepositoryWithSpecification",
                        "ProductRepository is null from helper", startTime, null);
            }

            // Build a simple specification that matches active products
            Specification<Product> spec = (root, query, cb) ->
                    cb.equal(root.get("active"), true);

            List<Product> results = repo.findAll(spec);
            log.info("testRepositoryWithSpecification: found {} active products", results.size());
            return success("testRepositoryWithSpecification",
                    "Specification query returned " + results.size() + " active products", startTime);
        } catch (Exception e) {
            log.error("testRepositoryWithSpecification failed", e);
            return failure("testRepositoryWithSpecification",
                    "Exception during specification query: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Tests that the ProductRepository interface has the expected structure.
     * Verifies JpaRepository and JpaSpecificationExecutor are extended.
     * Also checks for any custom @query methods via reflection.
     */
    public TestResult testCustomQueryMethod() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> repoClass = ProductRepository.class;

            // Verify it extends JpaRepository
            boolean extendsJpa = org.springframework.data.jpa.repository.JpaRepository.class
                    .isAssignableFrom(repoClass);
            // Verify it extends JpaSpecificationExecutor
            boolean extendsSpec = org.springframework.data.jpa.repository.JpaSpecificationExecutor.class
                    .isAssignableFrom(repoClass);

            if (!extendsJpa) {
                return failure("testCustomQueryMethod",
                        "ProductRepository does not extend JpaRepository", startTime, null);
            }
            if (!extendsSpec) {
                return failure("testCustomQueryMethod",
                        "ProductRepository does not extend JpaSpecificationExecutor", startTime, null);
            }

            // Check for custom query methods defined on the repository interface
            java.lang.reflect.Method[] methods = repoClass.getDeclaredMethods();
            StringBuilder customMethods = new StringBuilder();
            for (java.lang.reflect.Method m : methods) {
                if (m.isAnnotationPresent(org.springframework.data.jpa.repository.Query.class)) {
                    if (customMethods.length() > 0) customMethods.append(", ");
                    customMethods.append(m.getName());
                }
            }

            String msg = "ProductRepository structure verified: extendsJpa=true, extendsSpec=true";
            if (customMethods.length() > 0) {
                msg += ", customQueryMethods=[" + customMethods + "]";
            } else {
                msg += ", no custom @Query methods declared on interface";
            }

            log.info("testCustomQueryMethod: {}", msg);
            return success("testCustomQueryMethod", msg, startTime);
        } catch (Exception e) {
            log.error("testCustomQueryMethod failed", e);
            return failure("testCustomQueryMethod",
                    "Exception during repository structure check: " + e.getMessage(), startTime, e);
        }
    }
}
