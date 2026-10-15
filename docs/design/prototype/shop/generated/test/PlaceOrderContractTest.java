//ecd:<hash>_V3.1
package io.elasticore.sample.ddd.shop.application;

// Generated from `usecase: PlaceOrder`. Regenerated with the declaration;
// never edited. Three kinds of test come from three parts of the block:
//   requires -> one test per precondition, expecting its error code
//   examples -> one test per example, given/when/then verbatim
//   ensures  -> the contract test: a deliberately wrong custom step must be
//               refused, which is what protects you from an LLM-written body

@SpringBootTest
@Transactional
class PlaceOrderContractTest {

    @Autowired OrderUseCase orders;
    @Autowired Fixtures fixtures;   // generated: builds roots and to-one references from `given`

    // ---- requires ----------------------------------------------------

    @Test
    void refusesWhenNotPending() {
        Order o = fixtures.order(Map.of("status", "CONFIRMED", "customer", fixtures.activeCustomer()));
        PlaceOrderException e = assertThrows(PlaceOrderException.class,
                () -> orders.placeOrder(new PlaceOrderInput(o.getOrderId(), "US", 1.0f)));
        assertEquals(PlaceOrderException.Code.NOT_PENDING, e.getCode());
    }

    @Test
    void refusesAnInactiveCustomer() { /* CUSTOMER_INACTIVE, same shape */ }

    @Test
    void refusesAnEmptyOrder() { /* EMPTY_ORDER */ }

    @Test
    void refusesABadQuantity() { /* BAD_QUANTITY */ }

    // ---- examples ----------------------------------------------------

    @Test
    void wholesalePaysNoShipping() {
        fixtures.customer(Map.of("customerId", 7, "active", true, "customerType", "WHOLESALE"));
        fixtures.order(Map.of("orderId", "o-1", "status", "PENDING", "customer", 7,
                "items", List.of(Map.of("product", "p-1", "quantity", 2, "unitPrice", "10.00"))));

        OrderDTO result = orders.placeOrder(new PlaceOrderInput("o-1", "US", 3.0f));

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(0, result.getShippingFee().compareTo(BigDecimal.ZERO));
    }

    @Test
    void anEmptyOrderIsRefused() {
        fixtures.customer(Map.of("customerId", 7, "active", true, "customerType", "INDIVIDUAL"));
        fixtures.order(Map.of("orderId", "o-2", "status", "PENDING", "customer", 7, "items", List.of()));

        PlaceOrderException e = assertThrows(PlaceOrderException.class,
                () -> orders.placeOrder(new PlaceOrderInput("o-2", "US", 0.0f)));
        assertEquals(PlaceOrderException.Code.EMPTY_ORDER, e.getCode());
    }

    // ---- ensures: the contract ----------------------------------------

    @Test
    void aCustomStepThatBreaksThePromiseIsRefused() {
        // A subclass whose computeTotal sets a negative total. The use case
        // must refuse it at the `ensures` line, not persist it.
        OrderUseCase broken = fixtures.orderUseCaseWith(
                (order, input) -> order.setTotalAmount(new BigDecimal("-1")));
        fixtures.customer(Map.of("customerId", 7, "active", true, "customerType", "INDIVIDUAL"));
        fixtures.order(Map.of("orderId", "o-3", "status", "PENDING", "customer", 7,
                "items", List.of(Map.of("product", "p-1", "quantity", 1, "unitPrice", "1.00"))));

        assertThrows(IllegalStateException.class,
                () -> broken.placeOrder(new PlaceOrderInput("o-3", "US", 1.0f)));
        assertEquals(OrderStatus.PENDING, fixtures.reload("o-3").getStatus(), "nothing was persisted");
    }

    // ---- effects: enforced by the compiler, not by a test -------------
    // OrderCoreService's constructor takes OrderRepository, CustomerRepository,
    // ProductRepository - the `effects` list. A custom step that needs
    // CategoryRepository does not compile until `reads: [..., Category]` is
    // declared. There is nothing to test because there is nothing to bypass.
}
