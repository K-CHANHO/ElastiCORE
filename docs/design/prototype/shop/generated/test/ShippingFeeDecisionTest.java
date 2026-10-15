//ecd:<hash>_V3.1
package io.elasticore.sample.ddd.shop.domain.model;

// Generated from `decision: ShippingFee`. The table, row by row, plus the
// boundary of every numeric comparison the table makes.

class ShippingFeeDecisionTest {

    @ParameterizedTest
    @CsvSource({
        // country, weight, customerType, expected      <- row
        "US, 3.0,  WHOLESALE,  0",                   // row 1
        "DE, 50.0, WHOLESALE,  0",                   // row 1 wins over row 4/else
        "US, 4.99, INDIVIDUAL, 5.00",                // row 2
        "US, 5.0,  INDIVIDUAL, 12.00",               // row 3: boundary of weight < 5.0
        "US, 9.0,  BUSINESS,   12.00",               // row 3 wins over row 4 (first hit)
        "DE, 1.0,  BUSINESS,   15.00",               // row 4
        "DE, 1.0,  INDIVIDUAL, 20.00",               // else
    })
    void decides(String country, float weight, CustomerType type, BigDecimal expected) {
        assertEquals(0, expected.compareTo(ShippingFeeDecision.decide(country, weight, type)));
    }

    // Completeness was proved at load time (an else row exists). If the else
    // row is removed, the build warns:
    //   DSL_WARN decision.ShippingFee: incomplete - no rule matches
    //            {country != "US", customerType in [INDIVIDUAL]}
    // That message is the point of the table: the gap is named, not found in production.
}
