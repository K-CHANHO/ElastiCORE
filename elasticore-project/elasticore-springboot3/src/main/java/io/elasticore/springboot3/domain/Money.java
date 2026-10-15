package io.elasticore.springboot3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An amount of money: a value, not a number.
 *
 * Written in the DSL as {@code money} or {@code money(14.2)}; the column is
 * a plain decimal of that precision and scale, and the JSON is a plain
 * number, so neither the table nor the API changes shape. What changes is
 * the code in between: an amount is added to an amount, multiplied by a
 * quantity, compared with another amount - and cannot be mistaken for a
 * count or a rate, which a bare BigDecimal can.
 *
 * Immutable. Equal when the amounts are equal, whatever their scale: 10 and
 * 10.00 are the same money.
 */
public final class Money implements Comparable<Money>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    /** The amount given, or null for null - a column or a request that has none. */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Money of(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }

    public static Money of(String amount) {
        return amount == null ? null : new Money(new BigDecimal(amount.trim()));
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    /** The amount, as the column and the JSON carry it. */
    @JsonValue
    public BigDecimal toBigDecimal() {
        return amount;
    }

    public Money plus(Money other) {
        return other == null ? this : new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return other == null ? this : new Money(amount.subtract(other.amount));
    }

    public Money times(long factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)));
    }

    public Money times(Quantity quantity) {
        return quantity == null ? this : times(quantity.value());
    }

    /** Multiplied by a rate, exactly. Round with {@link #rounded(int)} where the domain says so. */
    public Money times(BigDecimal rate) {
        return rate == null ? this : new Money(amount.multiply(rate));
    }

    /** Rounded half-up to the given number of decimal places. */
    public Money rounded(int scale) {
        return new Money(amount.setScale(scale, RoundingMode.HALF_UP));
    }

    public Money negate() {
        return new Money(amount.negate());
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money && amount.compareTo(((Money) o).amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.signum() == 0 ? 0 : amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    /** The column: a decimal. Named by the generated {@code @Convert}. */
    public static final class JpaConverter implements AttributeConverter<Money, BigDecimal> {

        @Override
        public BigDecimal convertToDatabaseColumn(Money money) {
            return money == null ? null : money.amount;
        }

        @Override
        public Money convertToEntityAttribute(BigDecimal column) {
            return Money.of(column);
        }
    }
}
