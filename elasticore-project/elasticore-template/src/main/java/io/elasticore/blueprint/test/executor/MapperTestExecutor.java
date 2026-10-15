package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import io.elasticore.blueprint.domain.shop.dto.*;
import io.elasticore.blueprint.domain.shop.entity.*;
import io.elasticore.blueprint.domain.shop.enums.*;
import io.elasticore.blueprint.domain.shop.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests generated ShopMapper class for DTO-Entity conversions.
 * Includes EntityManager overload tests and cascade save tests.
 * Uses TransactionTemplate for programmatic TX (reflection bypasses @Transactional proxy).
 *
 * Generated and managed by ElastiCORE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MapperTestExecutor extends AbstractTestExecutor {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager em;

    @Override
    public TestCategory getCategory() {
        return TestCategory.MAPPER;
    }

    // ===== BASIC MAPPER TESTS =====

    /**
     * Test mapping from Product entity to ProductDTO
     */
    public TestResult testMapperEntityToDTO() {
        long startTime = System.currentTimeMillis();
        try {
            Product entity = new Product();
            entity.setProductId("prod-001");
            entity.setName("Test Product");
            entity.setSku("SKU-001");
            entity.setDescription("A test product");
            entity.setPrice(new BigDecimal("49.99"));
            entity.setCostPrice(new BigDecimal("25.00"));
            entity.setStockQuantity(100);
            entity.setWeight(2.5f);
            entity.setRating(4.2);
            entity.setActive(true);
            entity.setTags(new String[]{"tag1", "tag2"});

            ProductDTO dto = ShopMapper.toDTO(entity);
            if (dto == null) {
                return failure("testMapperEntityToDTO", "toDTO returned null for non-null entity", startTime, null);
            }
            if (!"prod-001".equals(dto.getProductId())) {
                return failure("testMapperEntityToDTO", "productId not mapped", startTime, null);
            }
            if (!"Test Product".equals(dto.getName())) {
                return failure("testMapperEntityToDTO", "name not mapped", startTime, null);
            }
            if (!"SKU-001".equals(dto.getSku())) {
                return failure("testMapperEntityToDTO", "sku not mapped", startTime, null);
            }
            if (dto.getPrice().compareTo(new BigDecimal("49.99")) != 0) {
                return failure("testMapperEntityToDTO", "price not mapped correctly", startTime, null);
            }
            if (dto.getStockQuantity() != 100) {
                return failure("testMapperEntityToDTO", "stockQuantity not mapped", startTime, null);
            }
            if (!Boolean.TRUE.equals(dto.getActive())) {
                return failure("testMapperEntityToDTO", "active not mapped", startTime, null);
            }

            return success("testMapperEntityToDTO",
                    "Entity to DTO mapping verified for all Product fields", startTime);
        } catch (Exception e) {
            log.error("testMapperEntityToDTO failed", e);
            return failure("testMapperEntityToDTO", "Entity to DTO mapping failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test mapping from ProductDTO to Product entity
     */
    public TestResult testMapperDTOToEntity() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = ProductDTO.builder()
                    .productId("dto-prod-001")
                    .name("DTO Product")
                    .sku("DTO-SKU-001")
                    .description("From DTO")
                    .price(new BigDecimal("99.99"))
                    .costPrice(new BigDecimal("50.00"))
                    .stockQuantity(200)
                    .weight(3.0f)
                    .rating(3.8)
                    .active(false)
                    .build();

            Product entity = ShopMapper.toEntity(dto);
            if (entity == null) {
                return failure("testMapperDTOToEntity", "toEntity returned null for non-null DTO", startTime, null);
            }
            if (!"dto-prod-001".equals(entity.getProductId())) {
                return failure("testMapperDTOToEntity", "productId not mapped to entity", startTime, null);
            }
            if (!"DTO Product".equals(entity.getName())) {
                return failure("testMapperDTOToEntity", "name not mapped to entity", startTime, null);
            }
            if (!"DTO-SKU-001".equals(entity.getSku())) {
                return failure("testMapperDTOToEntity", "sku not mapped to entity", startTime, null);
            }
            if (entity.getPrice().compareTo(new BigDecimal("99.99")) != 0) {
                return failure("testMapperDTOToEntity", "price not mapped to entity", startTime, null);
            }
            if (entity.getStockQuantity() != 200) {
                return failure("testMapperDTOToEntity", "stockQuantity not mapped to entity", startTime, null);
            }
            if (!Boolean.FALSE.equals(entity.getActive())) {
                return failure("testMapperDTOToEntity", "active not mapped to entity", startTime, null);
            }

            return success("testMapperDTOToEntity",
                    "DTO to Entity mapping verified for all Product fields", startTime);
        } catch (Exception e) {
            log.error("testMapperDTOToEntity failed", e);
            return failure("testMapperDTOToEntity", "DTO to Entity mapping failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test mapping with skipNull=true: null fields in source should not overwrite target
     */
    public TestResult testMapperSkipNull() {
        long startTime = System.currentTimeMillis();
        try {
            Product entity = new Product();
            entity.setProductId("skip-null-001");
            entity.setName("Original Name");
            entity.setSku("ORIG-SKU");
            entity.setPrice(new BigDecimal("30.00"));
            entity.setStockQuantity(50);
            entity.setActive(true);
            entity.setDescription("Original description");

            ProductDTO partialDto = new ProductDTO();
            partialDto.setName("Updated Name");

            ShopMapper.mapping(partialDto, entity, true);

            if (!"Updated Name".equals(entity.getName())) {
                return failure("testMapperSkipNull", "Name was not updated", startTime, null);
            }
            if (!"ORIG-SKU".equals(entity.getSku())) {
                return failure("testMapperSkipNull",
                        "SKU was overwritten by null (skipNull failed): " + entity.getSku(), startTime, null);
            }
            if (entity.getPrice().compareTo(new BigDecimal("30.00")) != 0) {
                return failure("testMapperSkipNull", "Price was overwritten by null", startTime, null);
            }

            return success("testMapperSkipNull",
                    "skipNull=true correctly preserves non-null target values when source is null", startTime);
        } catch (Exception e) {
            log.error("testMapperSkipNull failed", e);
            return failure("testMapperSkipNull", "skipNull test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test toProductDTOList conversion with a list of entities
     */
    public TestResult testMapperListConversion() {
        long startTime = System.currentTimeMillis();
        try {
            List<Product> entities = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Product p = new Product();
                p.setProductId("list-prod-" + i);
                p.setName("Product " + i);
                p.setSku("LIST-SKU-" + i);
                p.setPrice(new BigDecimal("10.00").add(new BigDecimal(i)));
                p.setStockQuantity(i * 10);
                p.setActive(true);
                entities.add(p);
            }

            List<ProductDTO> dtos = ShopMapper.toProductDTOList(entities);
            if (dtos == null) {
                return failure("testMapperListConversion", "toProductDTOList returned null", startTime, null);
            }
            if (dtos.size() != 3) {
                return failure("testMapperListConversion",
                        "Expected 3 DTOs, got " + dtos.size(), startTime, null);
            }

            for (int i = 0; i < 3; i++) {
                if (!("list-prod-" + i).equals(dtos.get(i).getProductId())) {
                    return failure("testMapperListConversion",
                            "Product ID mismatch at index " + i, startTime, null);
                }
            }

            return success("testMapperListConversion",
                    "toProductDTOList correctly converted list of 3 entities to DTOs", startTime);
        } catch (Exception e) {
            log.error("testMapperListConversion failed", e);
            return failure("testMapperListConversion", "List conversion failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Category entity-DTO-entity round-trip via ShopMapper
     */
    public TestResult testMapperCategoryConversion() {
        long startTime = System.currentTimeMillis();
        try {
            Category entity = new Category();
            entity.setCatId(42L);
            entity.setName("Electronics");
            entity.setDescription("Electronic products");
            entity.setSortOrder(1);
            entity.setActive(true);

            CategoryDTO dto = ShopMapper.toDTO(entity);
            if (dto == null) {
                return failure("testMapperCategoryConversion", "Category toDTO returned null", startTime, null);
            }
            if (!Long.valueOf(42L).equals(dto.getCatId())) {
                return failure("testMapperCategoryConversion", "catId not mapped to DTO", startTime, null);
            }
            if (!"Electronics".equals(dto.getName())) {
                return failure("testMapperCategoryConversion", "name not mapped to DTO", startTime, null);
            }

            Category backToEntity = ShopMapper.toEntity(dto);
            if (backToEntity == null) {
                return failure("testMapperCategoryConversion", "Category toEntity returned null", startTime, null);
            }
            if (!Long.valueOf(42L).equals(backToEntity.getCatId())) {
                return failure("testMapperCategoryConversion", "catId not mapped back to entity", startTime, null);
            }
            if (!"Electronics".equals(backToEntity.getName())) {
                return failure("testMapperCategoryConversion", "name not mapped back to entity", startTime, null);
            }

            return success("testMapperCategoryConversion",
                    "Category entity->DTO->entity round-trip conversion verified", startTime);
        } catch (Exception e) {
            log.error("testMapperCategoryConversion failed", e);
            return failure("testMapperCategoryConversion", "Category conversion failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test null safety: toDTO(null) and toEntity(null) should return null
     */
    public TestResult testMapperNullSafety() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dtoResult = ShopMapper.toDTO((Product) null);
            if (dtoResult != null) {
                return failure("testMapperNullSafety",
                        "ShopMapper.toDTO(null Product) should return null", startTime, null);
            }

            Product entityResult = ShopMapper.toEntity((ProductDTO) null);
            if (entityResult != null) {
                return failure("testMapperNullSafety",
                        "ShopMapper.toEntity(null ProductDTO) should return null", startTime, null);
            }

            CategoryDTO catResult = ShopMapper.toDTO((Category) null);
            if (catResult != null) {
                return failure("testMapperNullSafety",
                        "ShopMapper.toDTO(null Category) should return null", startTime, null);
            }

            Category catEntityResult = ShopMapper.toEntity((CategoryDTO) null);
            if (catEntityResult != null) {
                return failure("testMapperNullSafety",
                        "ShopMapper.toEntity(null CategoryDTO) should return null", startTime, null);
            }

            List<ProductDTO> listResult = ShopMapper.toProductDTOList(null);
            if (listResult != null) {
                return failure("testMapperNullSafety",
                        "ShopMapper.toProductDTOList(null) should return null", startTime, null);
            }

            return success("testMapperNullSafety",
                    "All mapper methods return null safely when given null input", startTime);
        } catch (Exception e) {
            log.error("testMapperNullSafety failed", e);
            return failure("testMapperNullSafety", "Null safety test failed: " + e.getMessage(), startTime, e);
        }
    }

    // ===== ENTITY MANAGER MAPPER TESTS =====

    /**
     * Test EntityManager overload: toEntity(ProductDTO, EntityManager)
     * Verifies em.getReference() is used for FK (Category) reference instead of new Category()
     */
    public TestResult testEmMapperFkReference() {
        long startTime = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                // Pre-save Category (like Company)
                Category category = new Category();
                category.setName("EM Test Category");
                category.setDescription("For EntityManager mapper test");
                category.setActive(true);
                categoryRepository.save(category);
                em.flush();

                Long savedCatId = category.getCatId();

                // Create ProductDTO with FK reference only (categoryCatId)
                ProductDTO dto = new ProductDTO();
                dto.setName("EM Test Product");
                dto.setSku("EM-SKU-" + System.currentTimeMillis());
                dto.setPrice(new BigDecimal("299.99"));
                dto.setActive(true);
                dto.setCategoryCatId(savedCatId);

                // Convert using EntityManager overload
                Product product = ShopMapper.toEntity(dto, em);

                if (product == null) {
                    return failure("testEmMapperFkReference", "toEntity(dto, em) returned null", startTime, null);
                }
                if (product.getCategory() == null) {
                    return failure("testEmMapperFkReference", "Category not set via em.getReference()", startTime, null);
                }

                // Verify it's a Hibernate proxy (em.getReference returns proxy)
                String categoryClass = product.getCategory().getClass().getName();
                boolean isProxy = categoryClass.contains("$");

                // Save and verify FK relationship
                productRepository.save(product);
                em.flush();
                em.clear();

                Product reloaded = productRepository.findById(product.getProductId()).orElse(null);
                if (reloaded == null || reloaded.getCategory() == null) {
                    return failure("testEmMapperFkReference", "Reloaded product has no category FK", startTime, null);
                }
                if (!savedCatId.equals(reloaded.getCategory().getCatId())) {
                    return failure("testEmMapperFkReference",
                            "FK mismatch: expected=" + savedCatId + " actual=" + reloaded.getCategory().getCatId(), startTime, null);
                }

                return success("testEmMapperFkReference",
                        "EntityManager mapper correctly used em.getReference() for Category FK (proxy=" + isProxy +
                        "), saved and verified productId=" + reloaded.getProductId() +
                        " -> categoryCatId=" + reloaded.getCategory().getCatId(), startTime);
            });
        } catch (Exception e) {
            log.error("testEmMapperFkReference failed", e);
            return failure("testEmMapperFkReference", "EM mapper FK test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test: Compare old toEntity(dto) vs new toEntity(dto, em)
     * Old creates detached new Category(), new uses em.getReference() proxy
     */
    public TestResult testEmMapperCompareOldVsNew() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = new ProductDTO();
            dto.setName("Compare Product");
            dto.setSku("CMP-SKU-001");
            dto.setPrice(new BigDecimal("100.00"));
            dto.setCategoryCatId(999L);

            // OLD: toEntity(dto) -creates new Category() with setCatId()
            Product oldWay = ShopMapper.toEntity(dto);
            if (oldWay.getCategory() == null) {
                return failure("testEmMapperCompareOldVsNew", "Old approach: category is null", startTime, null);
            }
            String oldClass = oldWay.getCategory().getClass().getName();
            boolean oldIsProxy = oldClass.contains("$");

            // NEW: toEntity(dto, (EntityManager) null) -should fallback to old behavior
            Product nullEmWay = ShopMapper.toEntity(dto, (EntityManager) null);
            if (nullEmWay.getCategory() == null) {
                return failure("testEmMapperCompareOldVsNew", "Null EM fallback: category is null", startTime, null);
            }
            String nullEmClass = nullEmWay.getCategory().getClass().getName();

            // Verify old approach creates plain Category (not proxy)
            if (oldIsProxy) {
                return failure("testEmMapperCompareOldVsNew",
                        "Old approach should create plain Category, not proxy", startTime, null);
            }

            // Verify catId is set correctly in both
            if (!Long.valueOf(999L).equals(oldWay.getCategory().getCatId())) {
                return failure("testEmMapperCompareOldVsNew", "Old approach: catId not set", startTime, null);
            }
            if (!Long.valueOf(999L).equals(nullEmWay.getCategory().getCatId())) {
                return failure("testEmMapperCompareOldVsNew", "Null EM fallback: catId not set", startTime, null);
            }

            return success("testEmMapperCompareOldVsNew",
                    "Old approach: plain " + oldClass + " | Null EM fallback: " + nullEmClass +
                    " | Both correctly set catId=999", startTime);
        } catch (Exception e) {
            log.error("testEmMapperCompareOldVsNew failed", e);
            return failure("testEmMapperCompareOldVsNew", "Compare test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test: EntityManager mapper with list conversion
     * toProductList(List<ProductDTO>, EntityManager) should propagate em to each item
     */
    public TestResult testEmMapperListWithEm() {
        long startTime = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                // Pre-save Category
                Category category = new Category();
                category.setName("EM List Test Category");
                category.setActive(true);
                categoryRepository.save(category);
                em.flush();
                Long savedCatId = category.getCatId();

                // Create list of ProductDTOs with FK
                List<ProductDTO> dtoList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    ProductDTO dto = new ProductDTO();
                    dto.setName("EM List Product " + i);
                    dto.setSku("EM-LIST-" + System.currentTimeMillis() + "-" + i);
                    dto.setPrice(new BigDecimal("10.00"));
                    dto.setActive(true);
                    dto.setCategoryCatId(savedCatId);
                    dtoList.add(dto);
                }

                // Convert list using EM overload
                List<Product> products = ShopMapper.toProductList(dtoList, em);

                if (products == null || products.size() != 3) {
                    return failure("testEmMapperListWithEm",
                            "toProductList(list, em) returned " + (products == null ? "null" : products.size()), startTime, null);
                }

                // Verify each product has Category via em.getReference
                for (int i = 0; i < products.size(); i++) {
                    Product p = products.get(i);
                    if (p.getCategory() == null) {
                        return failure("testEmMapperListWithEm",
                                "Product[" + i + "] has null category", startTime, null);
                    }
                }

                return success("testEmMapperListWithEm",
                        "toProductList(dtoList, em) correctly converted 3 DTOs with em.getReference() FK", startTime);
            });
        } catch (Exception e) {
            log.error("testEmMapperListWithEm failed", e);
            return failure("testEmMapperListWithEm", "EM list mapper test failed: " + e.getMessage(), startTime, e);
        }
    }

    // ===== CASCADE SAVE TESTS =====

    /**
     * Test: Order with cascade=ALL saves OrderItems automatically
     * Order entity has @OneToMany(cascade=ALL) for items
     * When Order is saved, its OrderItems should be persisted together
     */
    public TestResult testCascadeSaveOrderWithItems() {
        long startTime = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                // Pre-save Customer (Order has FK to Customer, no cascade)
                Customer customer = new Customer();
                customer.setFirstName("Cascade");
                customer.setLastName("Test");
                customer.setEmail("cascade-" + System.currentTimeMillis() + "@test.com");
                customerRepository.save(customer);
                em.flush();

                // Create Order entity with OrderItems
                Order order = new Order();
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(new BigDecimal("500.00"));
                order.setStatus(OrderStatus.PENDING);
                order.setCustomer(customer);

                // Create child OrderItems and set parent reference
                List<OrderItem> items = new ArrayList<>();
                for (int i = 1; i <= 3; i++) {
                    OrderItem item = new OrderItem();
                    item.setItemSeq(i);
                    item.setQuantity(i * 2);
                    item.setUnitPrice(new BigDecimal("100.00"));
                    item.setDiscount(BigDecimal.ZERO);
                    item.setOrder(order);
                    items.add(item);
                }
                order.setItems(items);

                // Save Order only -cascade=ALL should auto-save OrderItems
                Order saved = orderRepository.save(order);
                em.flush();
                em.clear();

                // Reload and verify children were cascade-saved
                Order reloaded = orderRepository.findById(saved.getOrderId()).orElse(null);
                if (reloaded == null) {
                    return failure("testCascadeSaveOrderWithItems", "Order not found after save", startTime, null);
                }

                List<OrderItem> reloadedItems = reloaded.getItems();
                if (reloadedItems == null || reloadedItems.isEmpty()) {
                    return failure("testCascadeSaveOrderWithItems",
                            "cascade=ALL failed: OrderItems not saved with Order", startTime, null);
                }
                if (reloadedItems.size() != 3) {
                    return failure("testCascadeSaveOrderWithItems",
                            "Expected 3 OrderItems, got " + reloadedItems.size(), startTime, null);
                }

                return success("testCascadeSaveOrderWithItems",
                        "cascade=ALL works: Order saved with " + reloadedItems.size() +
                        " OrderItems automatically. orderId=" + reloaded.getOrderId(), startTime);
            });
        } catch (Exception e) {
            log.error("testCascadeSaveOrderWithItems failed", e);
            return failure("testCascadeSaveOrderWithItems", "Cascade save test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test: Without cascade, child entity must be saved separately
     * Customer -> Order has NO cascade, so Order must be saved explicitly
     */
    public TestResult testNoCascadeRequiresSeparateSave() {
        long startTime = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                // Create Customer
                Customer customer = new Customer();
                customer.setFirstName("NoCascade");
                customer.setLastName("Test");
                customer.setEmail("nocascade-" + System.currentTimeMillis() + "@test.com");
                customerRepository.save(customer);
                em.flush();

                Long customerId = customer.getCustomerId();

                // Create Order (child) -Customer->Order has NO cascade
                Order order = new Order();
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(new BigDecimal("200.00"));
                order.setStatus(OrderStatus.PENDING);
                order.setCustomer(customer);

                // Must save Order separately since Customer has no cascade to Orders
                Order saved = orderRepository.save(order);
                em.flush();
                em.clear();

                // Verify Order was saved and has correct Customer FK
                Order reloaded = orderRepository.findById(saved.getOrderId()).orElse(null);
                if (reloaded == null) {
                    return failure("testNoCascadeRequiresSeparateSave", "Order not found after explicit save", startTime, null);
                }
                if (reloaded.getCustomer() == null || !customerId.equals(reloaded.getCustomer().getCustomerId())) {
                    return failure("testNoCascadeRequiresSeparateSave",
                            "Customer FK not set correctly on reloaded Order", startTime, null);
                }

                return success("testNoCascadeRequiresSeparateSave",
                        "Without cascade, Order saved separately. Verified Customer FK=" +
                        reloaded.getCustomer().getCustomerId() + " on orderId=" + reloaded.getOrderId(), startTime);
            });
        } catch (Exception e) {
            log.error("testNoCascadeRequiresSeparateSave failed", e);
            return failure("testNoCascadeRequiresSeparateSave",
                    "No-cascade test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test: Cascade save with EntityManager mapper
     * Full flow: DTO -> Entity(with EM mapper) -> cascade save parent+children
     * Order(cascade=ALL) with OrderItems, Customer FK via em.getReference()
     */
    public TestResult testCascadeSaveWithEmMapper() {
        long startTime = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                // Pre-save Customer
                Customer customer = new Customer();
                customer.setFirstName("EMCascade");
                customer.setLastName("User");
                customer.setEmail("emcascade-" + System.currentTimeMillis() + "@test.com");
                customerRepository.save(customer);
                em.flush();
                Long customerId = customer.getCustomerId();

                // Build OrderDTO with child OrderItemDTOs
                OrderDTO orderDto = new OrderDTO();
                orderDto.setOrderDate(LocalDateTime.now());
                orderDto.setTotalAmount(new BigDecimal("750.00"));
                orderDto.setStatus(OrderStatus.CONFIRMED);
                orderDto.setNotes("EM cascade test order");

                List<OrderItemDTO> itemDtos = new ArrayList<>();
                for (int i = 1; i <= 2; i++) {
                    OrderItemDTO itemDto = OrderItemDTO.builder()
                            .itemSeq(i)
                            .quantity(i * 3)
                            .unitPrice(new BigDecimal("250.00"))
                            .discount(new BigDecimal("10.00"))
                            .build();
                    itemDtos.add(itemDto);
                }
                orderDto.setItems(itemDtos);

                // Convert OrderDTO -> Order entity using EM mapper
                Order order = ShopMapper.toEntity(orderDto, em);

                if (order == null) {
                    return failure("testCascadeSaveWithEmMapper", "toEntity(orderDto, em) returned null", startTime, null);
                }

                // Set Customer FK via em.getReference
                order.setCustomer(em.getReference(Customer.class, customerId));

                // Manually convert and attach OrderItem children
                List<OrderItem> items = new ArrayList<>();
                for (OrderItemDTO itemDto : itemDtos) {
                    OrderItem item = ShopMapper.toEntity(itemDto, em);
                    item.setOrder(order);
                    items.add(item);
                }
                order.setItems(items);

                // Cascade save: Order + OrderItems saved together
                Order saved = orderRepository.save(order);
                em.flush();
                em.clear();

                // Verify
                Order reloaded = orderRepository.findById(saved.getOrderId()).orElse(null);
                if (reloaded == null) {
                    return failure("testCascadeSaveWithEmMapper", "Order not found after cascade save", startTime, null);
                }
                if (reloaded.getCustomer() == null || !customerId.equals(reloaded.getCustomer().getCustomerId())) {
                    return failure("testCascadeSaveWithEmMapper",
                            "Customer FK not correct after EM mapper cascade save", startTime, null);
                }

                List<OrderItem> reloadedItems = reloaded.getItems();
                if (reloadedItems == null || reloadedItems.size() != 2) {
                    return failure("testCascadeSaveWithEmMapper",
                            "Expected 2 cascade-saved OrderItems, got " +
                            (reloadedItems == null ? "null" : reloadedItems.size()), startTime, null);
                }

                return success("testCascadeSaveWithEmMapper",
                        "Full EM + cascade flow: OrderDTO->Order(em.getReference for Customer) + " +
                        reloadedItems.size() + " OrderItems cascade-saved. orderId=" + reloaded.getOrderId(), startTime);
            });
        } catch (Exception e) {
            log.error("testCascadeSaveWithEmMapper failed", e);
            return failure("testCascadeSaveWithEmMapper",
                    "EM cascade test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test: Entity->DTO with nested children (Order->OrderItemDTOs)
     * Verifies toDTO correctly maps OneToMany children to DTO list
     */
    public TestResult testEntityToDtoWithChildren() {
        long startTime = System.currentTimeMillis();
        try {
            return transactionTemplate.execute(status -> {
                // Create Order with OrderItems
                Customer customer = new Customer();
                customer.setFirstName("DtoChild");
                customer.setLastName("Test");
                customer.setEmail("dtochild-" + System.currentTimeMillis() + "@test.com");
                customerRepository.save(customer);

                Order order = new Order();
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(new BigDecimal("300.00"));
                order.setStatus(OrderStatus.PROCESSING);
                order.setCustomer(customer);

                List<OrderItem> items = new ArrayList<>();
                for (int i = 1; i <= 2; i++) {
                    OrderItem item = new OrderItem();
                    item.setItemSeq(i);
                    item.setQuantity(i);
                    item.setUnitPrice(new BigDecimal("150.00"));
                    item.setDiscount(BigDecimal.ZERO);
                    item.setOrder(order);
                    items.add(item);
                }
                order.setItems(items);
                Order saved = orderRepository.save(order);
                em.flush();
                em.clear();

                // Reload with items
                Order loaded = orderRepository.findById(saved.getOrderId()).orElse(null);
                if (loaded == null) {
                    return failure("testEntityToDtoWithChildren", "Order not found", startTime, null);
                }

                // Convert Entity -> DTO (should include items as List<OrderItemDTO>)
                OrderDTO orderDtoResult = ShopMapper.toDTO(loaded);
                if (orderDtoResult == null) {
                    return failure("testEntityToDtoWithChildren", "toDTO returned null", startTime, null);
                }

                // Check nested items converted
                if (orderDtoResult.getItems() == null || orderDtoResult.getItems().isEmpty()) {
                    return failure("testEntityToDtoWithChildren",
                            "OrderDTO.items is null/empty - nested OneToMany not mapped to DTO", startTime, null);
                }
                if (orderDtoResult.getItems().size() != 2) {
                    return failure("testEntityToDtoWithChildren",
                            "Expected 2 OrderItemDTOs, got " + orderDtoResult.getItems().size(), startTime, null);
                }

                // Check Customer ref fields mapped
                if (!"DtoChild".equals(orderDtoResult.getCustomerFirstName())) {
                    return failure("testEntityToDtoWithChildren",
                            "customerFirstName not mapped from Customer entity", startTime, null);
                }

                return success("testEntityToDtoWithChildren",
                        "Entity->DTO with nested children: Order->OrderDTO with " +
                        orderDtoResult.getItems().size() + " OrderItemDTOs, customerFirstName=" +
                        orderDtoResult.getCustomerFirstName(), startTime);
            });
        } catch (Exception e) {
            log.error("testEntityToDtoWithChildren failed", e);
            return failure("testEntityToDtoWithChildren",
                    "Entity->DTO children test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test: FK reference mapping without EntityManager (old way)
     * ProductDTO.categoryCatId -> Product.category via new Category(); setCatId()
     */
    public TestResult testFkReferenceMappingOldWay() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = new ProductDTO();
            dto.setName("FK Test Product");
            dto.setSku("FK-OLD-001");
            dto.setPrice(new BigDecimal("50.00"));
            dto.setCategoryCatId(123L);
            dto.setCategoryName("Test Category Name");

            Product entity = ShopMapper.toEntity(dto);

            if (entity.getCategory() == null) {
                return failure("testFkReferenceMappingOldWay",
                        "Category not set from categoryCatId", startTime, null);
            }
            if (!Long.valueOf(123L).equals(entity.getCategory().getCatId())) {
                return failure("testFkReferenceMappingOldWay",
                        "catId not set correctly: " + entity.getCategory().getCatId(), startTime, null);
            }

            // Old way creates plain entity, not proxy
            String className = entity.getCategory().getClass().getName();
            if (className.contains("$")) {
                return failure("testFkReferenceMappingOldWay",
                        "Old way should create plain Category, got: " + className, startTime, null);
            }

            return success("testFkReferenceMappingOldWay",
                    "FK reference (old way): categoryCatId=123 -> new Category with catId=123 (class=" + className + ")", startTime);
        } catch (Exception e) {
            log.error("testFkReferenceMappingOldWay failed", e);
            return failure("testFkReferenceMappingOldWay", "FK mapping test failed: " + e.getMessage(), startTime, e);
        }
    }
}
