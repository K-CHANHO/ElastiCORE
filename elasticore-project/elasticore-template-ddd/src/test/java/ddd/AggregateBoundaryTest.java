package ddd;

import io.elasticore.sample.ddd.shop.application.dto.OrderDTO;
import io.elasticore.sample.ddd.shop.application.dto.ShopMapper;
import io.elasticore.sample.ddd.shop.domain.model.Customer;
import io.elasticore.sample.ddd.shop.domain.model.Order;
import io.elasticore.sample.ddd.shop.domain.model.OrderItem;
import io.elasticore.sample.ddd.shop.infrastructure.persistence.CustomerRepository;
import io.elasticore.sample.ddd.shop.infrastructure.persistence.OrderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The aggregate boundary, checked against a database rather than a compiler.
 *
 * Every rule in the DDD bundle states something the generated source cannot
 * be asked about: that a member is written through its root, that another
 * aggregate is not loaded to be edited, that saving one thing does not save
 * two. Source that compiles can still be wrong about all three, so this boots
 * Hibernate over what was generated and drives it.
 */
@DataJpaTest
class AggregateBoundaryTest {

    @Autowired
    private OrderRepository orders;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("a member added through the root is persisted with it")
    void memberIsPersistedThroughItsRoot() {
        Order order = newOrder(persistedCustomer());
        order.addOrderItem(newItem(1, "9.99"));
        order.addOrderItem(newItem(2, "4.50"));

        orders.saveAndFlush(order);
        em.clear();

        Order reloaded = orders.findById(order.getOrderId()).orElseThrow();
        assertThat(reloaded.getItems())
                .as("the root was saved; its members had to be saved with it")
                .hasSize(2);
        assertThat(reloaded.getItems())
                .allSatisfy(item -> assertThat(item.getOrder().getOrderId())
                        .as("a member knows the root it belongs to")
                        .isEqualTo(order.getOrderId()));
    }

    @Test
    @DisplayName("a member removed through the root is deleted, not orphaned")
    void memberIsRemovedThroughItsRoot() {
        Order order = newOrder(persistedCustomer());
        order.addOrderItem(newItem(1, "9.99"));
        order.addOrderItem(newItem(2, "4.50"));
        orders.saveAndFlush(order);

        order.removeOrderItem(order.getItems().get(0));
        orders.saveAndFlush(order);
        em.clear();

        assertThat(orders.findById(order.getOrderId()).orElseThrow().getItems())
                .as("the root no longer holds it")
                .hasSize(1);
        // Not the same assertion: a member detached from its root disappears
        // from the collection whether the row was deleted or merely orphaned,
        // so the collection cannot say which happened. The table can.
        assertThat(em.createQuery("select count(i) from OrderItem i", Long.class).getSingleResult())
                .as("removed through the root means gone, not orphaned")
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("saving an order does not modify the customer it references")
    void savingAnOrderDoesNotWriteThroughToAnotherAggregate() {
        Customer customer = persistedCustomer();
        Order order = newValidOrder(customer);
        orders.saveAndFlush(order);
        em.clear();

        // A DTO carrying the other aggregate's field, changed. Before
        // ddd-mapper-ref the mapper assigned this back and the customer's row
        // changed with the order's.
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setNotes("shipped");
        dto.setCustomerFirstName("OVERWRITTEN");

        Order managed = orders.findById(order.getOrderId()).orElseThrow();
        ShopMapper.mapping(dto, managed, true);
        orders.saveAndFlush(managed);
        em.clear();

        assertThat(orders.findById(order.getOrderId()).orElseThrow().getNotes())
                .as("the order itself is still written")
                .isEqualTo("shipped");
        assertThat(customers.findById(customer.getCustomerId()).orElseThrow().getFirstName())
                .as("one transaction, one aggregate")
                .isEqualTo("Ada");
    }

    @Test
    @DisplayName("another aggregate is referenced by its identity and read through a read-only join")
    void anotherAggregateIsReferencedById() throws Exception {
        // The reference is an id; the object is there to read, and nothing
        // in this aggregate can set it.
        assertThat(io.elasticore.sample.ddd.shop.domain.model.OrderBase.class.getDeclaredField("customerId").getType())
                .isEqualTo(Long.class);
        assertThat(java.util.Arrays.stream(Order.class.getMethods()).map(java.lang.reflect.Method::getName))
                .contains("getCustomer", "getCustomerId")
                .doesNotContain("setCustomer");

        Customer customer = persistedCustomer();
        Order order = newValidOrder(customer);
        orders.saveAndFlush(order);
        em.clear();

        Order reloaded = orders.findById(order.getOrderId()).orElseThrow();
        assertThat(reloaded.getCustomerId()).isEqualTo(customer.getCustomerId());
        assertThat(reloaded.getCustomer().getFirstName())
                .as("the same column, joined read-only: queries and reads still see the customer")
                .isEqualTo("Ada");
        assertThat(em.createQuery("select o.customer.lastName from Order o where o.orderId = :id", String.class)
                .setParameter("id", order.getOrderId()).getSingleResult()).isEqualTo("Lovelace");
    }

    // ---- fixtures ---------------------------------------------------------

    private Customer persistedCustomer() {
        Customer c = new Customer();
        c.creation().setFirstName("Ada");
        c.creation().setLastName("Lovelace");
        c.creation().setEmail(String.format("ada%d@example.com", System.nanoTime()));
        return customers.saveAndFlush(c);
    }

    private Order newOrder(Customer customer) {
        Order order = new Order();
        order.creation().setOrderDate(LocalDateTime.now());
        order.creation().setTotalAmount(new BigDecimal("14.49"));
        order.creation().setCustomerId(customer.getCustomerId());
        return order;
    }

    /**
     * The same order, already valid.
     *
     * The aggregate promises it has at least one line, so a test that means
     * to save one has to give it one. Tests that add their own lines use
     * newOrder directly.
     */
    private Order newValidOrder(Customer customer) {
        Order order = newOrder(customer);
        order.addOrderItem(newItem(1, "14.49"));
        return order;
    }

    private OrderItem newItem(int seq, String unitPrice) {
        OrderItem item = new OrderItem();
        item.setItemSeq(seq);
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }
}
