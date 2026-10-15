//ecd:<hash>_V3.1
package io.elasticore.sample.ddd.shop.domain.model;

// Generated from `state: Order`. The whole transition table, both ways:
// every declared (from, transition) succeeds, every undeclared one is refused.
// 7 states x 6 transitions = 42 cases; 9 allowed, 33 refused.

class OrderTransitionsTest {

    @ParameterizedTest
    @CsvSource({
        "PENDING,    confirm, CONFIRMED",
        "CONFIRMED,  process, PROCESSING",
        "PROCESSING, ship,    SHIPPED",
        "SHIPPED,    deliver, DELIVERED",
        "PENDING,    cancel,  CANCELLED",
        "CONFIRMED,  cancel,  CANCELLED",
        "PROCESSING, cancel,  CANCELLED",
        "DELIVERED,  refund,  REFUNDED",
        "CANCELLED,  refund,  REFUNDED",
    })
    void allowed(OrderStatus from, String transition, OrderStatus to) {
        Order o = Fixtures.orderSatisfyingGuards(from);   // items, active customer, trackingNumber
        Transitions.apply(o, transition);
        assertEquals(to, o.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
        "SHIPPED,    confirm",
        "DELIVERED,  cancel",
        "REFUNDED,   ship",
        "PENDING,    ship",
        // ... the remaining 28 undeclared pairs
    })
    void refused(OrderStatus from, String transition) {
        Order o = Fixtures.orderSatisfyingGuards(from);
        assertThrows(IllegalStateException.class, () -> Transitions.apply(o, transition));
        assertEquals(from, o.getStatus(), "a refused transition leaves the state alone");
    }

    @Test
    void confirmRequiresItsGuard() {
        Order o = Fixtures.orderSatisfyingGuards(OrderStatus.PENDING);
        o.getItems().clear();
        assertFalse(o.canConfirm());
        assertThrows(IllegalStateException.class, o::confirm);
    }

    @Test
    void confirmRaisesPlaced() {
        Order o = Fixtures.orderSatisfyingGuards(OrderStatus.PENDING);
        o.confirm();
        assertTrue(o.domainEvents().stream().anyMatch(e -> e instanceof OrderPlacedEvent));
    }

    @Test
    void finalStatesHaveNoExit() {
        for (OrderStatus s : List.of(OrderStatus.DELIVERED, OrderStatus.REFUNDED)) {
            assertTrue(Fixtures.orderSatisfyingGuards(s).isFinal());
        }
    }
}
