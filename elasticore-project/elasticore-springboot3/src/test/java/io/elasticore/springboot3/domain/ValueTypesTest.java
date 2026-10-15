package io.elasticore.springboot3.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elasticore.springboot3.entity.FixedAtCreation;
import io.elasticore.springboot3.validation.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Money and Quantity: values with the arithmetic and the refusals a bare
 * BigDecimal or int leaves to whoever remembers them.
 */
class ValueTypesTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void moneyIsAValueWhateverItsScale() {
        assertThat(Money.of("10")).isEqualTo(Money.of("10.00"));
        assertThat(Money.of("10").hashCode()).isEqualTo(Money.of("10.00").hashCode());
        assertThat(Money.of("100").times(Quantity.of(3)).plus(Money.of("0.50"))).isEqualTo(Money.of("300.5"));
        assertThat(Money.of("1").minus(Money.of("2")).isNegative()).isTrue();
        assertThat(Money.of("2.345").rounded(2)).isEqualTo(Money.of("2.35"));
    }

    @Test
    void moneyTravelsAsItsNumber() throws Exception {
        assertThat(json.writeValueAsString(Money.of("14.49"))).isEqualTo("14.49");
        assertThat(json.readValue("14.49", Money.class)).isEqualTo(Money.of("14.49"));
        assertThat(new Money.JpaConverter().convertToDatabaseColumn(Money.of("3.10")))
                .isEqualByComparingTo(new BigDecimal("3.1"));
        assertThat(new Money.JpaConverter().convertToEntityAttribute(null)).isNull();
    }

    @Test
    void aQuantityIsNeverNegative() throws Exception {
        assertThat(Quantity.of(5).minus(Quantity.of(2))).isEqualTo(Quantity.of(3));
        assertThatThrownBy(() -> Quantity.of(2).minus(Quantity.of(3)))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("not enough");
        assertThatThrownBy(() -> Quantity.of(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(json.writeValueAsString(Quantity.of(7))).isEqualTo("7");
        assertThat(json.readValue("7", Quantity.class)).isEqualTo(Quantity.of(7));
        assertThat(new Quantity.JpaConverter().convertToEntityAttribute(4)).isEqualTo(Quantity.of(4));
    }

    /** A value class with no annotation and no equals of its own (mapping: xml). */
    static final class Address {
        String city;

        Address(String city) {
            this.city = city;
        }
    }

    @Test
    void aValueWithoutEqualsIsComparedByItsParts() {
        FixedAtCreation.check(true, "Partner", "address", new Address("Seoul"), new Address("Seoul"));
        assertThatThrownBy(() -> FixedAtCreation.check(true, "Partner", "address", new Address("Seoul"), new Address("Busan")))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void aSettledValueIsTheSameValueWhateverItsScale() {
        FixedAtCreation.check(true, "Item", "unitPrice", new BigDecimal("10.00"), new BigDecimal("10"));
        FixedAtCreation.check(true, "Item", "unitPrice", Money.of("10.00"), Money.of("10"));
        assertThatThrownBy(() -> FixedAtCreation.check(true, "Item", "unitPrice", Money.of("10"), Money.of("11")))
                .isInstanceOf(InvariantViolationException.class)
                .hasMessageContaining("Item.unitPrice");
        FixedAtCreation.check(false, "Item", "unitPrice", Money.of("10"), Money.of("11"));
    }
}
