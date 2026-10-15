package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import io.elasticore.blueprint.domain.shop.enums.OrderStatus;
import io.elasticore.blueprint.domain.shop.enums.PaymentMethod;
import io.elasticore.blueprint.domain.shop.enums.CustomerType;
import io.elasticore.blueprint.domain.hr.enums.DeptStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests enum generation features including value presence and valueOf behavior.
 *
 * Generated and managed by ElastiCORE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnumTestExecutor extends AbstractTestExecutor {

    @Override
    public TestCategory getCategory() {
        return TestCategory.ENUM;
    }

    /**
     * Verify OrderStatus has all 7 expected values
     */
    public TestResult testOrderStatusValues() {
        long startTime = System.currentTimeMillis();
        try {
            OrderStatus[] values = OrderStatus.values();
            if (values.length != 7) {
                return failure("testOrderStatusValues",
                        "Expected 7 OrderStatus values, found " + values.length, startTime, null);
            }

            Set<String> names = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());
            String[] expected = {"PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"};
            for (String exp : expected) {
                if (!names.contains(exp)) {
                    return failure("testOrderStatusValues",
                            "Missing OrderStatus value: " + exp, startTime, null);
                }
            }

            // Verify code properties
            if (!"PEN".equals(OrderStatus.PENDING.getCode())) {
                return failure("testOrderStatusValues",
                        "PENDING code mismatch: expected PEN, got " + OrderStatus.PENDING.getCode(), startTime, null);
            }
            if (!"CAN".equals(OrderStatus.CANCELLED.getCode())) {
                return failure("testOrderStatusValues",
                        "CANCELLED code mismatch: expected CAN, got " + OrderStatus.CANCELLED.getCode(), startTime, null);
            }

            return success("testOrderStatusValues",
                    "OrderStatus has all 7 values with correct codes", startTime);
        } catch (Exception e) {
            log.error("testOrderStatusValues failed", e);
            return failure("testOrderStatusValues", "OrderStatus check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify PaymentMethod has all 5 expected values
     */
    public TestResult testPaymentMethodValues() {
        long startTime = System.currentTimeMillis();
        try {
            PaymentMethod[] values = PaymentMethod.values();
            if (values.length != 5) {
                return failure("testPaymentMethodValues",
                        "Expected 5 PaymentMethod values, found " + values.length, startTime, null);
            }

            Set<String> names = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());
            String[] expected = {"CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "PAYPAL", "CASH"};
            for (String exp : expected) {
                if (!names.contains(exp)) {
                    return failure("testPaymentMethodValues",
                            "Missing PaymentMethod value: " + exp, startTime, null);
                }
            }

            // Verify code properties
            if (!"CC".equals(PaymentMethod.CREDIT_CARD.getCode())) {
                return failure("testPaymentMethodValues",
                        "CREDIT_CARD code mismatch: expected CC, got " + PaymentMethod.CREDIT_CARD.getCode(), startTime, null);
            }

            return success("testPaymentMethodValues",
                    "PaymentMethod has all 5 values with correct codes", startTime);
        } catch (Exception e) {
            log.error("testPaymentMethodValues failed", e);
            return failure("testPaymentMethodValues", "PaymentMethod check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify CustomerType has 3 expected values: INDIVIDUAL, BUSINESS, WHOLESALE
     */
    public TestResult testCustomerTypeValues() {
        long startTime = System.currentTimeMillis();
        try {
            CustomerType[] values = CustomerType.values();
            if (values.length != 3) {
                return failure("testCustomerTypeValues",
                        "Expected 3 CustomerType values, found " + values.length, startTime, null);
            }

            Set<String> names = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());
            String[] expected = {"INDIVIDUAL", "BUSINESS", "WHOLESALE"};
            for (String exp : expected) {
                if (!names.contains(exp)) {
                    return failure("testCustomerTypeValues",
                            "Missing CustomerType value: " + exp, startTime, null);
                }
            }

            // Verify code/description properties
            if (!"I".equals(CustomerType.INDIVIDUAL.getCode())) {
                return failure("testCustomerTypeValues",
                        "INDIVIDUAL code mismatch: expected I, got " + CustomerType.INDIVIDUAL.getCode(), startTime, null);
            }
            if (!"Individual".equals(CustomerType.INDIVIDUAL.getDescription())) {
                return failure("testCustomerTypeValues",
                        "INDIVIDUAL description mismatch", startTime, null);
            }

            return success("testCustomerTypeValues",
                    "CustomerType has all 3 values (INDIVIDUAL, BUSINESS, WHOLESALE) with correct codes", startTime);
        } catch (Exception e) {
            log.error("testCustomerTypeValues failed", e);
            return failure("testCustomerTypeValues", "CustomerType check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify DeptStatus has ACTIVE, INACTIVE, MERGED with codes A, I, M
     */
    public TestResult testDeptStatusValues() {
        long startTime = System.currentTimeMillis();
        try {
            DeptStatus[] values = DeptStatus.values();
            if (values.length != 3) {
                return failure("testDeptStatusValues",
                        "Expected 3 DeptStatus values, found " + values.length, startTime, null);
            }

            if (!"A".equals(DeptStatus.ACTIVE.getCode())) {
                return failure("testDeptStatusValues",
                        "ACTIVE code mismatch: expected A, got " + DeptStatus.ACTIVE.getCode(), startTime, null);
            }
            if (!"I".equals(DeptStatus.INACTIVE.getCode())) {
                return failure("testDeptStatusValues",
                        "INACTIVE code mismatch: expected I, got " + DeptStatus.INACTIVE.getCode(), startTime, null);
            }
            if (!"M".equals(DeptStatus.MERGED.getCode())) {
                return failure("testDeptStatusValues",
                        "MERGED code mismatch: expected M, got " + DeptStatus.MERGED.getCode(), startTime, null);
            }

            return success("testDeptStatusValues",
                    "DeptStatus has ACTIVE(A), INACTIVE(I), MERGED(M)", startTime);
        } catch (Exception e) {
            log.error("testDeptStatusValues failed", e);
            return failure("testDeptStatusValues", "DeptStatus check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test enum valueOf and fromCode methods work correctly
     */
    public TestResult testEnumValueOf() {
        long startTime = System.currentTimeMillis();
        try {
            // Test valueOf (standard Java enum)
            OrderStatus pending = OrderStatus.valueOf("PENDING");
            if (pending != OrderStatus.PENDING) {
                return failure("testEnumValueOf",
                        "OrderStatus.valueOf('PENDING') did not return PENDING", startTime, null);
            }

            // Test fromCode (ElastiCORE generated)
            OrderStatus fromCode = OrderStatus.fromCode("PEN");
            if (fromCode != OrderStatus.PENDING) {
                return failure("testEnumValueOf",
                        "OrderStatus.fromCode('PEN') did not return PENDING", startTime, null);
            }

            // Test invalid valueOf throws exception
            try {
                OrderStatus.valueOf("INVALID_VALUE");
                return failure("testEnumValueOf",
                        "OrderStatus.valueOf('INVALID_VALUE') should throw IllegalArgumentException", startTime, null);
            } catch (IllegalArgumentException ignored) {
                // Expected behavior
            }

            // Test fromCode with null returns null
            OrderStatus nullResult = OrderStatus.fromCode(null);
            if (nullResult != null) {
                return failure("testEnumValueOf",
                        "OrderStatus.fromCode(null) should return null", startTime, null);
            }

            return success("testEnumValueOf",
                    "valueOf and fromCode methods work correctly for OrderStatus", startTime);
        } catch (Exception e) {
            log.error("testEnumValueOf failed", e);
            return failure("testEnumValueOf", "Enum valueOf test failed: " + e.getMessage(), startTime, e);
        }
    }
}
