package io.elasticore.springboot3.domain;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * The standard bound constraints, taught to the value types.
 *
 * A DSL bound - {@code unitPrice: money(12.2)! @min(0.01, '...')} - is
 * written on the DTO as the same {@code @DecimalMin} a BigDecimal gets, so
 * the message and the 400 are the same. Bean Validation finds these through
 * the service loader (META-INF/services/jakarta.validation.ConstraintValidator);
 * nothing is registered by hand. Null is valid, as for every bound: required
 * is {@code !}'s business.
 */
public final class ValueBoundValidators {

    private ValueBoundValidators() {
    }

    public static final class MoneyDecimalMin implements ConstraintValidator<DecimalMin, Money> {
        private BigDecimal bound;
        private boolean inclusive;

        @Override
        public void initialize(DecimalMin a) {
            bound = new BigDecimal(a.value());
            inclusive = a.inclusive();
        }

        @Override
        public boolean isValid(Money value, ConstraintValidatorContext ctx) {
            if (value == null) return true;
            int c = value.toBigDecimal().compareTo(bound);
            return inclusive ? c >= 0 : c > 0;
        }
    }

    public static final class MoneyDecimalMax implements ConstraintValidator<DecimalMax, Money> {
        private BigDecimal bound;
        private boolean inclusive;

        @Override
        public void initialize(DecimalMax a) {
            bound = new BigDecimal(a.value());
            inclusive = a.inclusive();
        }

        @Override
        public boolean isValid(Money value, ConstraintValidatorContext ctx) {
            if (value == null) return true;
            int c = value.toBigDecimal().compareTo(bound);
            return inclusive ? c <= 0 : c < 0;
        }
    }

    public static final class QuantityMin implements ConstraintValidator<Min, Quantity> {
        private long bound;

        @Override
        public void initialize(Min a) {
            bound = a.value();
        }

        @Override
        public boolean isValid(Quantity value, ConstraintValidatorContext ctx) {
            return value == null || value.value() >= bound;
        }
    }

    public static final class QuantityMax implements ConstraintValidator<Max, Quantity> {
        private long bound;

        @Override
        public void initialize(Max a) {
            bound = a.value();
        }

        @Override
        public boolean isValid(Quantity value, ConstraintValidatorContext ctx) {
            return value == null || value.value() <= bound;
        }
    }
}
