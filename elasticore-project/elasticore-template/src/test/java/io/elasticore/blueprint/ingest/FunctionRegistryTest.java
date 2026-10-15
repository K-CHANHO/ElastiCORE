package io.elasticore.blueprint.ingest;

import io.elasticore.base.runtime.FunctionRegistry;
import io.elasticore.base.runtime.IngestFunction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies built-in ingest functions and the custom registration path.
 */
class FunctionRegistryTest {

    // --- toNum ---

    @Test
    void toNum_plainInteger() {
        assertThat(FunctionRegistry.apply("toNum", "12345"))
                .isEqualTo(new BigDecimal("12345"));
    }

    @Test
    void toNum_stripsCommas() {
        assertThat(FunctionRegistry.apply("toNum", "21,700"))
                .isEqualTo(new BigDecimal("21700"));
    }

    @Test
    void toNum_stripsWhitespaceAndCurrency() {
        assertThat(FunctionRegistry.apply("toNum", "  ₩ 3,456.78 "))
                .isEqualTo(new BigDecimal("3456.78"));
    }

    @Test
    void toNum_emptyStringReturnsNull() {
        assertThat(FunctionRegistry.apply("toNum", "")).isNull();
        assertThat(FunctionRegistry.apply("toNum", "   ")).isNull();
    }

    @Test
    void toNum_nullPassesThrough() {
        assertThat(FunctionRegistry.apply("toNum", null)).isNull();
    }

    @Test
    void toNum_unparseableReturnsNull() {
        assertThat(FunctionRegistry.apply("toNum", "not-a-number")).isNull();
    }

    @Test
    void toNum_numberBoxed() {
        assertThat(FunctionRegistry.apply("toNum", 42)).isEqualTo(new BigDecimal("42"));
    }

    @Test
    void toNumber_aliasSameAsToNum() {
        assertThat(FunctionRegistry.apply("toNumber", "1,234"))
                .isEqualTo(new BigDecimal("1234"));
    }

    // --- ymd ---

    @Test
    void ymd_convertsYyyymmdd() {
        assertThat(FunctionRegistry.apply("ymd", "20260307")).isEqualTo("2026-03-07");
    }

    @Test
    void ymd_stripsSeparatorsBeforeParse() {
        assertThat(FunctionRegistry.apply("ymd", "2026/03/07")).isEqualTo("2026-03-07");
        assertThat(FunctionRegistry.apply("ymd", "2026.03.07")).isEqualTo("2026-03-07");
    }

    @Test
    void ymd_passThroughForNon8Digit() {
        assertThat(FunctionRegistry.apply("ymd", "20260")).isEqualTo("20260");
    }

    @Test
    void ymd_nullReturnsNull() {
        assertThat(FunctionRegistry.apply("ymd", null)).isNull();
    }

    // --- trim / upper / lower ---

    @Test
    void trim_removesSurroundingWhitespace() {
        assertThat(FunctionRegistry.apply("trim", "  hello  ")).isEqualTo("hello");
    }

    @Test
    void upper_andLower() {
        assertThat(FunctionRegistry.apply("upper", "abc")).isEqualTo("ABC");
        assertThat(FunctionRegistry.apply("lower", "ABC")).isEqualTo("abc");
    }

    // --- yn ---

    @Test
    void yn_trueVariantsMapToY() {
        assertThat(FunctionRegistry.apply("yn", "1")).isEqualTo("Y");
        assertThat(FunctionRegistry.apply("yn", "y")).isEqualTo("Y");
        assertThat(FunctionRegistry.apply("yn", "Y")).isEqualTo("Y");
        assertThat(FunctionRegistry.apply("yn", "true")).isEqualTo("Y");
        assertThat(FunctionRegistry.apply("yn", Boolean.TRUE)).isEqualTo("Y");
    }

    @Test
    void yn_otherValuesMapToN() {
        assertThat(FunctionRegistry.apply("yn", "0")).isEqualTo("N");
        assertThat(FunctionRegistry.apply("yn", "no")).isEqualTo("N");
        assertThat(FunctionRegistry.apply("yn", Boolean.FALSE)).isEqualTo("N");
    }

    // --- registry behaviour ---

    @Test
    void caseInsensitiveLookup() {
        assertThat(FunctionRegistry.apply("TONUM", "10")).isEqualTo(new BigDecimal("10"));
        assertThat(FunctionRegistry.apply("Ymd", "20260101")).isEqualTo("2026-01-01");
    }

    @Test
    void unknownFunctionThrows() {
        assertThatThrownBy(() -> FunctionRegistry.apply("doesNotExist", "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("doesNotExist");
    }

    @Test
    void customRegistration() {
        String name = "doubler_test";
        try {
            FunctionRegistry.register(name, new IngestFunction() {
                public Object apply(Object value) {
                    return value == null ? null : (((Integer) value) * 2);
                }
            });
            assertThat(FunctionRegistry.apply(name, 21)).isEqualTo(42);
        } finally {
            FunctionRegistry.unregister(name);
        }
        assertThatThrownBy(() -> FunctionRegistry.apply(name, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
