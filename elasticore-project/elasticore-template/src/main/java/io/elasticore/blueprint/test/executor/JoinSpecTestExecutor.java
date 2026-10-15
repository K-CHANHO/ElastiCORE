package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.domain.shop.entity.Product;
import io.elasticore.blueprint.domain.shop.entity.Q;
import io.elasticore.blueprint.domain.shop.repository.ProductRepository;
import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;
import io.elasticore.springboot3.entity.JoinSpec;
import io.elasticore.springboot3.entity.Op;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the JoinSpec API via generated Q classes for the shop domain.
 * Each test method exercises a specific JoinSpec feature (where, orderBy,
 * distinct, join, fetchJoin) and verifies that the specification builds
 * and optionally executes without error.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JoinSpecTestExecutor extends AbstractTestExecutor {

    private final EntityManager entityManager;
    private final ProductRepository productRepository;

    @Override
    public TestCategory getCategory() {
        return TestCategory.JOIN_SPEC;
    }

    // ===== 1. JoinSpec creation =====
    public TestResult testJoinSpecCreation() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec();
            if (joinSpec == null) {
                return failure("testJoinSpecCreation",
                        "Q.Product.joinSpec() returned null", startTime, null);
            }
            return success("testJoinSpecCreation",
                    "JoinSpec created successfully", startTime);
        } catch (Exception e) {
            log.error("testJoinSpecCreation failed", e);
            return failure("testJoinSpecCreation", "JoinSpec creation failed", startTime, e);
        }
    }

    // ===== 2. WHERE with EQ operator =====
    public TestResult testWhereEq() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.name(Op.EQ, "test"));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testWhereEq",
                    "WHERE EQ executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testWhereEq failed", e);
            return failure("testWhereEq", "WHERE EQ failed", startTime, e);
        }
    }

    // ===== 3. WHERE with LIKE operator =====
    public TestResult testWhereLike() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.name(Op.LIKE, "%test%"));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testWhereLike",
                    "WHERE LIKE executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testWhereLike failed", e);
            return failure("testWhereLike", "WHERE LIKE failed", startTime, e);
        }
    }

    // ===== 4. WHERE with GT operator =====
    public TestResult testWhereGt() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.price(Op.GT, BigDecimal.ZERO));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testWhereGt",
                    "WHERE GT executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testWhereGt failed", e);
            return failure("testWhereGt", "WHERE GT failed", startTime, e);
        }
    }

    // ===== 5. WHERE with IN operator =====
    public TestResult testWhereIn() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.active(Op.IN, Arrays.asList(true)));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testWhereIn",
                    "WHERE IN executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testWhereIn failed", e);
            return failure("testWhereIn", "WHERE IN failed", startTime, e);
        }
    }

    // ===== 6. WHERE with IS_NULL operator =====
    public TestResult testWhereIsNull() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.description(Op.IS_NULL, null));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testWhereIsNull",
                    "WHERE IS_NULL executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testWhereIsNull failed", e);
            return failure("testWhereIsNull", "WHERE IS_NULL failed", startTime, e);
        }
    }

    // ===== 7. WHERE with BETWEEN operator =====
    public TestResult testWhereBetween() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.price(Op.BETWEEN,
                            Arrays.asList(BigDecimal.ZERO, new BigDecimal("99999"))));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testWhereBetween",
                    "WHERE BETWEEN executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testWhereBetween failed", e);
            return failure("testWhereBetween", "WHERE BETWEEN failed", startTime, e);
        }
    }

    // ===== 8. ORDER BY ascending =====
    public TestResult testOrderByAsc() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .orderByAsc(Q.Product.getName());

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testOrderByAsc",
                    "ORDER BY ASC executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testOrderByAsc failed", e);
            return failure("testOrderByAsc", "ORDER BY ASC failed", startTime, e);
        }
    }

    // ===== 9. ORDER BY descending =====
    public TestResult testOrderByDesc() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .orderByDesc(Q.Product.getName());

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testOrderByDesc",
                    "ORDER BY DESC executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testOrderByDesc failed", e);
            return failure("testOrderByDesc", "ORDER BY DESC failed", startTime, e);
        }
    }

    // ===== 10. DISTINCT =====
    public TestResult testDistinct() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .distinct();

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testDistinct",
                    "DISTINCT executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testDistinct failed", e);
            return failure("testDistinct", "DISTINCT failed", startTime, e);
        }
    }

    // ===== 11. AND / OR combinators =====
    public TestResult testAndOr() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .where(Q.Product.active(Op.EQ, true))
                    .and(Q.Product.price(Op.GT, BigDecimal.ZERO))
                    .or(Q.Product.name(Op.LIKE, "%special%"));

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testAndOr",
                    "AND/OR combinators executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testAndOr failed", e);
            return failure("testAndOr", "AND/OR combinators failed", startTime, e);
        }
    }

    // ===== 12. FETCH JOIN =====
    public TestResult testFetchJoin() {
        long startTime = System.currentTimeMillis();
        try {
            JoinSpec<Product> joinSpec = Q.Product.joinSpec()
                    .fetchJoin(Q.Product.getCategory());

            List<Product> results = productRepository.findAll(joinSpec);
            return success("testFetchJoin",
                    "FETCH JOIN executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testFetchJoin failed", e);
            return failure("testFetchJoin", "FETCH JOIN failed", startTime, e);
        }
    }
}
