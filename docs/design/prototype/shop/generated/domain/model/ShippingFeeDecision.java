//ecd:<hash>_V3.1
package io.elasticore.sample.ddd.shop.domain.model;

import java.math.BigDecimal;

/**
 * What delivery costs, by destination, weight and customer standing.
 *
 * Generated from `decision: ShippingFee`. A pure function: no repository, no
 * Spring, no state - which is why the same table can also be emitted for
 * TypeScript and why its test is the table itself.
 *
 * Hit policy: first. The load-time check proved the table complete (an
 * `else` row exists) so no input reaches the end of the method.
 */
public final class ShippingFeeDecision {

    private ShippingFeeDecision() {}

    public static BigDecimal decide(String country, float weight, CustomerType customerType) {
        if (customerType == CustomerType.WHOLESALE)                 return new BigDecimal("0");
        if ("US".equals(country) && weight < 5.0f)                  return new BigDecimal("5.00");
        if ("US".equals(country))                                   return new BigDecimal("12.00");
        if (customerType == CustomerType.BUSINESS)                  return new BigDecimal("15.00");
        return new BigDecimal("20.00");
    }
}
