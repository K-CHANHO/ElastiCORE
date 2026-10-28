package ddd;

import io.elasticore.sample.ddd.shop.application.OrderService;
import io.elasticore.sample.ddd.shop.application.dto.OrderDTO;
import io.elasticore.sample.ddd.shop.application.dto.OrderSrchDTO;
import io.elasticore.sample.ddd.shop.domain.model.Customer;
import io.elasticore.sample.ddd.shop.domain.model.Order;
import io.elasticore.sample.ddd.shop.domain.model.OrderItem;
import io.elasticore.sample.ddd.shop.infrastructure.persistence.CustomerRepository;
import io.elasticore.sample.ddd.shop.infrastructure.persistence.OrderRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A relation marked @fetchjoin is read with its root in the paged search.
 *
 * The v3 CoreService had no JOIN FETCH: an order's customer, which the
 * search result maps, was loaded with a query per row - five orders, five
 * more queries. Hibernate's fetch count for Customer is what shows it; the
 * result is the same either way.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class JoinFetchTest {

    private static final int ORDERS = 5;

    @Autowired
    private OrderService service;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private EntityManagerFactory emf;

    private final List<Order> savedOrders = new ArrayList<Order>();
    private final List<Customer> savedCustomers = new ArrayList<Customer>();

    @AfterEach
    void cleanUp() {
        for (Order o : savedOrders) orders.deleteById(o.getOrderId());
        for (Customer c : savedCustomers) customers.deleteById(c.getCustomerId());
    }

    @Test
    @DisplayName("the paged search reads each order's customer in the same query")
    void theCustomerIsFetchedWithTheOrder() {
        String tag = "join-fetch-" + System.nanoTime();
        for (int i = 0; i < ORDERS; i++) {
            Customer c = new Customer();
            c.creation().setFirstName("Customer" + i);
            c.creation().setLastName("Fetch");
            c.creation().setEmail(String.format("fetch%d-%d@example.com", i, System.nanoTime()));
            savedCustomers.add(customers.saveAndFlush(c));

            Order order = new Order();
            order.creation().setOrderDate(LocalDateTime.now());
            order.creation().setTotalAmount(new BigDecimal("14.49"));
            order.creation().setCustomerId(c.getCustomerId());
            order.setNotes(tag);
            OrderItem item = new OrderItem();
            item.setItemSeq(1);
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("14.49"));
            order.addOrderItem(item);
            savedOrders.add(orders.saveAndFlush(order));
        }

        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        OrderSrchDTO search = new OrderSrchDTO();
        search.setPageSize(50);
        Page<OrderDTO> page = service.findBySearch(search);
        // A fetch, in Hibernate's statistics, is an entity read by a select of
        // its own - a lazy relation initialised row by row. The order's lines
        // are such a fetch either way (they are not @fetchjoin); its customer
        // must not be.
        long customerFetches = stats.getEntityStatistics(Customer.class.getName()).getFetchCount();

        List<OrderDTO> mine = new ArrayList<OrderDTO>();
        for (OrderDTO dto : page.getContent()) {
            if (tag.equals(dto.getNotes())) mine.add(dto);
        }
        assertThat(mine).hasSize(ORDERS);
        assertThat(mine).allSatisfy(dto -> assertThat(dto.getCustomerFirstName()).startsWith("Customer"));
        // Without the JOIN FETCH, each of the five customers is a select of its own.
        assertThat(customerFetches)
                .as("customers read by a select of their own in one paged search of %d orders", ORDERS)
                .isZero();
    }
}
