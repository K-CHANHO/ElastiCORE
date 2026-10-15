package io.elasticore.springboot3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.elasticore.springboot3.validation.InvariantViolationException;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;

/**
 * A count of something: never negative.
 *
 * Written in the DSL as {@code quantity}; the column is an integer and the
 * JSON a plain number. Taking more than there is does not produce a negative
 * quantity - it is refused where it happens, as a broken rule (422), which
 * is the check a bare int leaves to whoever remembers it.
 *
 * A signed change - a stock movement that goes out as well as in - is not a
 * quantity; keep that one an int.
 */
public final class Quantity implements Comparable<Quantity>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final Quantity ZERO = new Quantity(0);

    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    /** A quantity of the given count. A negative count is not one. */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Quantity of(int value) {
        if (value < 0) throw new IllegalArgumentException("a quantity cannot be negative: " + value);
        return value == 0 ? ZERO : new Quantity(value);
    }

    /** The column's integer, or null. */
    public static Quantity ofNullable(Integer value) {
        return value == null ? null : of(value);
    }

    @JsonValue
    public int value() {
        return value;
    }

    public Quantity plus(Quantity other) {
        return other == null ? this : of(Math.addExact(value, other.value));
    }

    public Quantity plus(int more) {
        return of(Math.addExact(value, more));
    }

    /** Less by the other; refused (422) when there is not that much. */
    public Quantity minus(Quantity other) {
        return other == null ? this : minus(other.value);
    }

    public Quantity minus(int less) {
        if (less > value) {
            throw new InvariantViolationException(String.format(
                    "not enough: %d requested, %d available", less, value));
        }
        return of(value - less);
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean covers(Quantity other) {
        return other == null || value >= other.value;
    }

    @Override
    public int compareTo(Quantity other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quantity && ((Quantity) o).value == value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /** The column: an integer. Named by the generated {@code @Convert}. */
    public static final class JpaConverter implements AttributeConverter<Quantity, Integer> {

        @Override
        public Integer convertToDatabaseColumn(Quantity quantity) {
            return quantity == null ? null : quantity.value;
        }

        @Override
        public Quantity convertToEntityAttribute(Integer column) {
            return ofNullable(column);
        }
    }
}
