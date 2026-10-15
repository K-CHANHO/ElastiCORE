package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.domain.shop.entity.Product;
import io.elasticore.blueprint.domain.shop.entity.Q;
import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;
import io.elasticore.springboot3.entity.DeleteSpec;
import io.elasticore.springboot3.entity.Op;
import io.elasticore.springboot3.entity.UpdateSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

/**
 * Tests the UpdateSpec and DeleteSpec APIs via generated Q classes.
 * Each test method exercises a specific spec-builder feature
 * (creation, set, increment, where, count) and verifies that the
 * specification builds without error.
 *
 * Actual execute() calls are avoided to prevent unintended data mutation.
 * Only count() is safe to call as it performs a read-only query.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDeleteTestExecutor extends AbstractTestExecutor {

    private final EntityManager entityManager;

    @Override
    public TestCategory getCategory() {
        return TestCategory.UPDATE_DELETE_SPEC;
    }

    // ===== 1. UpdateSpec creation =====
    public TestResult testUpdateSpecCreation() {
        long startTime = System.currentTimeMillis();
        try {
            UpdateSpec<Product> updateSpec = Q.Product.updateSpec();
            if (updateSpec == null) {
                return failure("testUpdateSpecCreation",
                        "Q.Product.updateSpec() returned null", startTime, null);
            }
            return success("testUpdateSpecCreation",
                    "UpdateSpec created successfully", startTime);
        } catch (Exception e) {
            log.error("testUpdateSpecCreation failed", e);
            return failure("testUpdateSpecCreation", "UpdateSpec creation failed", startTime, e);
        }
    }

    // ===== 2. UpdateSpec with set and where =====
    public TestResult testUpdateSpecSet() {
        long startTime = System.currentTimeMillis();
        try {
            UpdateSpec<Product> updateSpec = Q.Product.updateSpec()
                    .set(Q.Product.getName(), "updated-test-name")
                    .where(Q.Product.active(Op.EQ, false));

            // Verify the spec builds without error; do not execute to avoid data changes
            if (updateSpec == null) {
                return failure("testUpdateSpecSet",
                        "UpdateSpec with set/where returned null", startTime, null);
            }
            return success("testUpdateSpecSet",
                    "UpdateSpec set + where built successfully", startTime);
        } catch (Exception e) {
            log.error("testUpdateSpecSet failed", e);
            return failure("testUpdateSpecSet", "UpdateSpec set + where failed", startTime, e);
        }
    }

    // ===== 3. UpdateSpec with increment =====
    public TestResult testUpdateSpecIncrement() {
        long startTime = System.currentTimeMillis();
        try {
            UpdateSpec<Product> updateSpec = Q.Product.updateSpec()
                    .increment(Q.Product.getStockQuantity(), 10)
                    .where(Q.Product.active(Op.EQ, true));

            if (updateSpec == null) {
                return failure("testUpdateSpecIncrement",
                        "UpdateSpec with increment returned null", startTime, null);
            }
            return success("testUpdateSpecIncrement",
                    "UpdateSpec increment built successfully", startTime);
        } catch (Exception e) {
            log.error("testUpdateSpecIncrement failed", e);
            return failure("testUpdateSpecIncrement", "UpdateSpec increment failed", startTime, e);
        }
    }

    // ===== 4. DeleteSpec creation =====
    public TestResult testDeleteSpecCreation() {
        long startTime = System.currentTimeMillis();
        try {
            DeleteSpec<Product> deleteSpec = Q.Product.deleteSpec();
            if (deleteSpec == null) {
                return failure("testDeleteSpecCreation",
                        "Q.Product.deleteSpec() returned null", startTime, null);
            }
            return success("testDeleteSpecCreation",
                    "DeleteSpec created successfully", startTime);
        } catch (Exception e) {
            log.error("testDeleteSpecCreation failed", e);
            return failure("testDeleteSpecCreation", "DeleteSpec creation failed", startTime, e);
        }
    }

    // ===== 5. DeleteSpec count (read-only, safe to execute) =====
    public TestResult testDeleteSpecCount() {
        long startTime = System.currentTimeMillis();
        try {
            DeleteSpec<Product> deleteSpec = Q.Product.deleteSpec()
                    .where(Q.Product.active(Op.EQ, false));

            long count = deleteSpec.count(entityManager);
            return success("testDeleteSpecCount",
                    "DeleteSpec count executed successfully, matching rows = " + count,
                    startTime);
        } catch (Exception e) {
            log.error("testDeleteSpecCount failed", e);
            return failure("testDeleteSpecCount", "DeleteSpec count failed", startTime, e);
        }
    }

    // ===== 6. DeleteSpec with where condition =====
    public TestResult testDeleteSpecWhere() {
        long startTime = System.currentTimeMillis();
        try {
            DeleteSpec<Product> deleteSpec = Q.Product.deleteSpec()
                    .where(Q.Product.name(Op.EQ, "TO_DELETE"));

            // Only count to verify the spec builds and queries correctly; do not execute
            long count = deleteSpec.count(entityManager);
            return success("testDeleteSpecWhere",
                    "DeleteSpec where built and counted successfully, matching rows = " + count,
                    startTime);
        } catch (Exception e) {
            log.error("testDeleteSpecWhere failed", e);
            return failure("testDeleteSpecWhere", "DeleteSpec where failed", startTime, e);
        }
    }
}
