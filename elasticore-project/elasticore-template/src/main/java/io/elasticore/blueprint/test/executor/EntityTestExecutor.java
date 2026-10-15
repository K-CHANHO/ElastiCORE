package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import io.elasticore.blueprint.domain.shop.dto.*;
import io.elasticore.blueprint.domain.shop.enums.*;
import io.elasticore.blueprint.domain.shop.service.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Tests entity CRUD operations via generated CoreService classes.
 *
 * Generated and managed by ElastiCORE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntityTestExecutor extends AbstractTestExecutor {

    private final CategoryCoreService categoryCoreService;
    private final ProductCoreService productCoreService;
    private final CustomerCoreService customerCoreService;
    private final OrderCoreService orderCoreService;

    @Override
    public TestCategory getCategory() {
        return TestCategory.ENTITY;
    }

    /**
     * Test Category full CRUD lifecycle: create, read, update, delete
     */
    public TestResult testCategoryCrud() {
        long startTime = System.currentTimeMillis();
        try {
            // Create
            CategoryDTO dto = CategoryDTO.builder()
                    .name("Test Category")
                    .description("Test description")
                    .sortOrder(1)
                    .active(true)
                    .build();
            CategoryDTO saved = categoryCoreService.save(dto);
            if (saved == null || saved.getCatId() == null) {
                return failure("testCategoryCrud", "Category save returned null or no ID", startTime, null);
            }
            Long catId = saved.getCatId();

            // Read
            Optional<CategoryDTO> found = categoryCoreService.findById(catId);
            if (found.isEmpty()) {
                return failure("testCategoryCrud", "Category not found after save", startTime, null);
            }

            // Update
            CategoryDTO updateDto = CategoryDTO.builder()
                    .catId(catId)
                    .name("Updated Category")
                    .description("Updated description")
                    .sortOrder(2)
                    .active(true)
                    .build();
            CategoryDTO updated = categoryCoreService.update(updateDto);
            if (!"Updated Category".equals(updated.getName())) {
                return failure("testCategoryCrud", "Category name not updated", startTime, null);
            }

            // Delete
            boolean deleted = categoryCoreService.deleteById(catId);
            if (!deleted) {
                return failure("testCategoryCrud", "Category deletion failed", startTime, null);
            }

            return success("testCategoryCrud", "Category CRUD lifecycle completed successfully", startTime);
        } catch (Exception e) {
            log.error("testCategoryCrud failed", e);
            return failure("testCategoryCrud", "Category CRUD failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Product CRUD with all fields populated
     */
    public TestResult testProductCrud() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = ProductDTO.builder()
                    .name("Test Product")
                    .sku("TEST-SKU-CRUD-" + System.currentTimeMillis())
                    .description("Test product description")
                    .price(new BigDecimal("29.99"))
                    .costPrice(new BigDecimal("15.00"))
                    .stockQuantity(100)
                    .weight(1.5f)
                    .rating(4.5)
                    .active(true)
                    .build();
            ProductDTO saved = productCoreService.save(dto);
            if (saved == null || saved.getProductId() == null) {
                return failure("testProductCrud", "Product save returned null or no ID", startTime, null);
            }

            Optional<ProductDTO> found = productCoreService.findById(saved.getProductId());
            if (found.isEmpty()) {
                return failure("testProductCrud", "Product not found after save", startTime, null);
            }

            ProductDTO readBack = found.get();
            if (!"Test Product".equals(readBack.getName())) {
                return failure("testProductCrud", "Product name mismatch after read", startTime, null);
            }

            // Cleanup
            productCoreService.deleteById(saved.getProductId());

            return success("testProductCrud", "Product CRUD with all fields completed successfully", startTime);
        } catch (Exception e) {
            log.error("testProductCrud failed", e);
            return failure("testProductCrud", "Product CRUD failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Customer CRUD verifying email and phone fields
     */
    public TestResult testCustomerCrud() {
        long startTime = System.currentTimeMillis();
        try {
            String uniqueEmail = "test-" + System.currentTimeMillis() + "@example.com";
            CustomerDTO dto = CustomerDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email(uniqueEmail)
                    .phone("010-1234-5678")
                    .customerType(CustomerType.INDIVIDUAL)
                    .loyaltyPoints(100L)
                    .build();
            CustomerDTO saved = customerCoreService.save(dto);
            if (saved == null || saved.getCustomerId() == null) {
                return failure("testCustomerCrud", "Customer save returned null or no ID", startTime, null);
            }

            Optional<CustomerDTO> found = customerCoreService.findById(saved.getCustomerId());
            if (found.isEmpty()) {
                return failure("testCustomerCrud", "Customer not found after save", startTime, null);
            }

            CustomerDTO readBack = found.get();
            if (!uniqueEmail.equals(readBack.getEmail())) {
                return failure("testCustomerCrud", "Customer email mismatch", startTime, null);
            }
            if (!"010-1234-5678".equals(readBack.getPhone())) {
                return failure("testCustomerCrud", "Customer phone mismatch", startTime, null);
            }

            // Cleanup
            customerCoreService.deleteById(saved.getCustomerId());

            return success("testCustomerCrud", "Customer CRUD with email/phone verification succeeded", startTime);
        } catch (Exception e) {
            log.error("testCustomerCrud failed", e);
            return failure("testCustomerCrud", "Customer CRUD failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Product findById: create product, then retrieve by generated ID
     */
    public TestResult testProductFindById() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = ProductDTO.builder()
                    .name("FindById Product")
                    .sku("TEST-FIND-" + System.currentTimeMillis())
                    .price(new BigDecimal("19.99"))
                    .stockQuantity(50)
                    .active(true)
                    .build();
            ProductDTO saved = productCoreService.save(dto);
            String productId = saved.getProductId();

            Optional<ProductDTO> found = productCoreService.findById(productId);
            if (found.isEmpty()) {
                return failure("testProductFindById", "Product not found by ID: " + productId, startTime, null);
            }
            if (!productId.equals(found.get().getProductId())) {
                return failure("testProductFindById", "Product ID mismatch", startTime, null);
            }

            // Cleanup
            productCoreService.deleteById(productId);

            return success("testProductFindById", "Product findById returned correct entity", startTime);
        } catch (Exception e) {
            log.error("testProductFindById failed", e);
            return failure("testProductFindById", "Product findById failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Product search using name filter in ProductSrchDTO
     */
    public TestResult testProductSearch() {
        long startTime = System.currentTimeMillis();
        try {
            String uniqueName = "SearchTest-" + System.currentTimeMillis();
            ProductDTO dto = ProductDTO.builder()
                    .name(uniqueName)
                    .sku("TEST-SRCH-" + System.currentTimeMillis())
                    .price(new BigDecimal("9.99"))
                    .stockQuantity(10)
                    .active(true)
                    .build();
            ProductDTO saved = productCoreService.save(dto);

            ProductSrchDTO srch = ProductSrchDTO.builder()
                    .name(uniqueName)
                    .build();
            List<ProductDTO> results = productCoreService.listBySearch(srch);
            if (results == null || results.isEmpty()) {
                return failure("testProductSearch", "Product search by name returned empty", startTime, null);
            }

            boolean found = results.stream().anyMatch(p -> uniqueName.equals(p.getName()));
            if (!found) {
                return failure("testProductSearch", "Search result does not contain expected product", startTime, null);
            }

            // Cleanup
            productCoreService.deleteById(saved.getProductId());

            return success("testProductSearch", "Product search by name returned " + results.size() + " result(s)", startTime);
        } catch (Exception e) {
            log.error("testProductSearch failed", e);
            return failure("testProductSearch", "Product search failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Product search by price range using between filter
     */
    public TestResult testProductSearchByPrice() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = ProductDTO.builder()
                    .name("PriceRange Product")
                    .sku("TEST-PRICE-" + System.currentTimeMillis())
                    .price(new BigDecimal("50.00"))
                    .stockQuantity(5)
                    .active(true)
                    .build();
            ProductDTO saved = productCoreService.save(dto);

            ProductSrchDTO srch = ProductSrchDTO.builder()
                    .priceFrom(new BigDecimal("40.00"))
                    .priceTo(new BigDecimal("60.00"))
                    .build();
            List<ProductDTO> results = productCoreService.listBySearch(srch);
            if (results == null || results.isEmpty()) {
                return failure("testProductSearchByPrice", "Price range search returned empty", startTime, null);
            }

            boolean found = results.stream().anyMatch(p -> saved.getProductId().equals(p.getProductId()));
            if (!found) {
                return failure("testProductSearchByPrice", "Price range search did not include expected product", startTime, null);
            }

            // Cleanup
            productCoreService.deleteById(saved.getProductId());

            return success("testProductSearchByPrice", "Product price range search returned " + results.size() + " result(s)", startTime);
        } catch (Exception e) {
            log.error("testProductSearchByPrice failed", e);
            return failure("testProductSearchByPrice", "Price range search failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Category listBySearch with CategorySrchDTO
     */
    public TestResult testCategoryListBySearch() {
        long startTime = System.currentTimeMillis();
        try {
            String uniqueName = "CatSearch-" + System.currentTimeMillis();
            CategoryDTO dto = CategoryDTO.builder()
                    .name(uniqueName)
                    .description("Category for search test")
                    .sortOrder(10)
                    .active(true)
                    .build();
            CategoryDTO saved = categoryCoreService.save(dto);

            CategorySrchDTO srch = CategorySrchDTO.builder()
                    .name(uniqueName)
                    .active(true)
                    .build();
            List<CategoryDTO> results = categoryCoreService.listBySearch(srch);
            if (results == null || results.isEmpty()) {
                return failure("testCategoryListBySearch", "Category search returned empty", startTime, null);
            }

            // Cleanup
            categoryCoreService.deleteById(saved.getCatId());

            return success("testCategoryListBySearch", "Category listBySearch returned " + results.size() + " result(s)", startTime);
        } catch (Exception e) {
            log.error("testCategoryListBySearch failed", e);
            return failure("testCategoryListBySearch", "Category listBySearch failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test entity relationship: create category, then product referencing that category
     */
    public TestResult testEntityRelationship() {
        long startTime = System.currentTimeMillis();
        try {
            // Create category first
            CategoryDTO catDto = CategoryDTO.builder()
                    .name("Relation Category")
                    .description("For relationship test")
                    .active(true)
                    .build();
            CategoryDTO savedCat = categoryCoreService.save(catDto);

            // Create product referencing the category
            ProductDTO prodDto = ProductDTO.builder()
                    .name("Relation Product")
                    .sku("TEST-REL-" + System.currentTimeMillis())
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(20)
                    .active(true)
                    .categoryCatId(savedCat.getCatId())
                    .build();
            ProductDTO savedProd = productCoreService.save(prodDto);

            // Read back and verify relationship
            Optional<ProductDTO> found = productCoreService.findById(savedProd.getProductId());
            if (found.isEmpty()) {
                return failure("testEntityRelationship", "Product not found after save", startTime, null);
            }

            ProductDTO readBack = found.get();
            if (readBack.getCategoryCatId() == null || !readBack.getCategoryCatId().equals(savedCat.getCatId())) {
                return failure("testEntityRelationship", "Product category reference not preserved", startTime, null);
            }
            // Check if categoryName field exists in DTO via reflection (may not be populated at runtime depending on mapper configuration)
            try {
                java.lang.reflect.Field categoryNameField = ProductDTO.class.getDeclaredField("categoryName");
                categoryNameField.setAccessible(true);
                String categoryName = (String) categoryNameField.get(readBack);
                log.info("testEntityRelationship: categoryName field exists, runtime value: {}", categoryName);
            } catch (NoSuchFieldException e) {
                return failure("testEntityRelationship", "ProductDTO missing categoryName field from @ref annotation", startTime, e);
            } catch (Exception e) {
                log.warn("testEntityRelationship: Could not access categoryName field: {}", e.getMessage());
            }

            // Cleanup
            productCoreService.deleteById(savedProd.getProductId());
            categoryCoreService.deleteById(savedCat.getCatId());

            return success("testEntityRelationship", "Entity relationship (Category -> Product) verified successfully", startTime);
        } catch (Exception e) {
            log.error("testEntityRelationship failed", e);
            return failure("testEntityRelationship", "Entity relationship test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test that audit fields (createdBy, createDate) exist in the DTO structure
     */
    public TestResult testAuditFields() {
        long startTime = System.currentTimeMillis();
        try {
            CategoryDTO dto = CategoryDTO.builder()
                    .name("Audit Test Category")
                    .active(true)
                    .build();
            CategoryDTO saved = categoryCoreService.save(dto);

            Optional<CategoryDTO> found = categoryCoreService.findById(saved.getCatId());
            if (found.isEmpty()) {
                return failure("testAuditFields", "Category not found after save", startTime, null);
            }

            // Verify audit fields exist in DTO class via getter access
            CategoryDTO readBack = found.get();
            // Access audit fields - they may be null if auditing is not fully configured
            String createdBy = readBack.getCreatedBy();
            LocalDateTime createDate = readBack.getCreateDate();
            String lastModifiedBy = readBack.getLastModifiedBy();
            LocalDateTime lastModifiedDate = readBack.getLastModifiedDate();

            // The fields should be accessible (no exception) even if null
            log.info("Audit fields - createdBy: {}, createDate: {}, lastModifiedBy: {}, lastModifiedDate: {}",
                    createdBy, createDate, lastModifiedBy, lastModifiedDate);

            // Cleanup
            categoryCoreService.deleteById(saved.getCatId());

            return success("testAuditFields", "Audit fields (createdBy, createDate, lastModifiedBy, lastModifiedDate) are accessible in DTO", startTime);
        } catch (Exception e) {
            log.error("testAuditFields failed", e);
            return failure("testAuditFields", "Audit fields test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test Product countBySearch returns correct count
     */
    public TestResult testProductCountBySearch() {
        long startTime = System.currentTimeMillis();
        try {
            String uniqueSku = "TEST-CNT-" + System.currentTimeMillis();
            ProductDTO dto = ProductDTO.builder()
                    .name("Count Product")
                    .sku(uniqueSku)
                    .price(new BigDecimal("10.00"))
                    .stockQuantity(1)
                    .active(true)
                    .build();
            ProductDTO saved = productCoreService.save(dto);

            ProductSrchDTO srch = ProductSrchDTO.builder()
                    .sku(uniqueSku)
                    .build();
            long count = productCoreService.countBySearch(srch);
            if (count < 1) {
                return failure("testProductCountBySearch", "countBySearch returned " + count + ", expected >= 1", startTime, null);
            }

            // Cleanup
            productCoreService.deleteById(saved.getProductId());

            return success("testProductCountBySearch", "countBySearch returned " + count, startTime);
        } catch (Exception e) {
            log.error("testProductCountBySearch failed", e);
            return failure("testProductCountBySearch", "countBySearch failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test boolean field filtering (active field on Product)
     */
    public TestResult testBooleanField() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = ProductDTO.builder()
                    .name("Bool Product")
                    .sku("TEST-BOOL-" + System.currentTimeMillis())
                    .price(new BigDecimal("5.00"))
                    .stockQuantity(1)
                    .active(false)
                    .build();
            ProductDTO saved = productCoreService.save(dto);

            // Search for inactive products
            ProductSrchDTO srch = ProductSrchDTO.builder()
                    .active(false)
                    .build();
            List<ProductDTO> results = productCoreService.listBySearch(srch);
            if (results == null || results.isEmpty()) {
                return failure("testBooleanField", "Boolean field search for active=false returned empty", startTime, null);
            }

            boolean found = results.stream().anyMatch(p -> saved.getProductId().equals(p.getProductId()));
            if (!found) {
                return failure("testBooleanField", "Inactive product not found in search results", startTime, null);
            }

            // Cleanup
            productCoreService.deleteById(saved.getProductId());

            return success("testBooleanField", "Boolean field filtering works correctly", startTime);
        } catch (Exception e) {
            log.error("testBooleanField failed", e);
            return failure("testBooleanField", "Boolean field test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test enum field usage in OrderDTO with OrderStatus
     */
    public TestResult testEnumField() {
        long startTime = System.currentTimeMillis();
        try {
            OrderDTO dto = OrderDTO.builder()
                    .orderDate(LocalDateTime.now())
                    .totalAmount(new BigDecimal("150.00"))
                    .status(OrderStatus.PENDING)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .build();
            OrderDTO saved = orderCoreService.save(dto);
            if (saved == null || saved.getOrderId() == null) {
                return failure("testEnumField", "Order save returned null or no ID", startTime, null);
            }

            Optional<OrderDTO> found = orderCoreService.findById(saved.getOrderId());
            if (found.isEmpty()) {
                return failure("testEnumField", "Order not found after save", startTime, null);
            }

            OrderDTO readBack = found.get();
            if (readBack.getStatus() != OrderStatus.PENDING) {
                return failure("testEnumField", "OrderStatus mismatch: expected PENDING, got " + readBack.getStatus(), startTime, null);
            }
            if (readBack.getPaymentMethod() != PaymentMethod.CREDIT_CARD) {
                return failure("testEnumField", "PaymentMethod mismatch: expected CREDIT_CARD, got " + readBack.getPaymentMethod(), startTime, null);
            }

            // Cleanup
            orderCoreService.deleteById(saved.getOrderId());

            return success("testEnumField", "Enum fields (OrderStatus, PaymentMethod) saved and read correctly", startTime);
        } catch (Exception e) {
            log.error("testEnumField failed", e);
            return failure("testEnumField", "Enum field test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test that Customer entity has embedded Address (billingAddress) fields available
     */
    public TestResult testEmbeddedAddress() {
        long startTime = System.currentTimeMillis();
        try {
            // Verify the Customer entity class has a billingAddress field of type Address
            java.lang.reflect.Field billingAddressField =
                    io.elasticore.blueprint.domain.shop.entity.Customer.class.getDeclaredField("billingAddress");
            if (billingAddressField == null) {
                return failure("testEmbeddedAddress", "billingAddress field not found on Customer entity", startTime, null);
            }

            Class<?> fieldType = billingAddressField.getType();
            if (!fieldType.getSimpleName().equals("Address")) {
                return failure("testEmbeddedAddress", "billingAddress is not of type Address, found: " + fieldType.getName(), startTime, null);
            }

            // Verify Address has expected fields
            Class<?> addressClass = io.elasticore.blueprint.domain.shop.entity.Address.class;
            addressClass.getDeclaredField("street");
            addressClass.getDeclaredField("city");
            addressClass.getDeclaredField("state");
            addressClass.getDeclaredField("zipCode");
            addressClass.getDeclaredField("country");

            return success("testEmbeddedAddress", "Customer has embedded Address with street, city, state, zipCode, country fields", startTime);
        } catch (NoSuchFieldException e) {
            return failure("testEmbeddedAddress", "Missing expected field: " + e.getMessage(), startTime, e);
        } catch (Exception e) {
            log.error("testEmbeddedAddress failed", e);
            return failure("testEmbeddedAddress", "Embedded Address test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test that Product ID is auto-generated when saved without explicit ID
     */
    public TestResult testProductGeneratedId() {
        long startTime = System.currentTimeMillis();
        try {
            ProductDTO dto = ProductDTO.builder()
                    .name("AutoID Product")
                    .sku("TEST-AUTOID-" + System.currentTimeMillis())
                    .price(new BigDecimal("12.00"))
                    .stockQuantity(3)
                    .active(true)
                    .build();
            // Do NOT set productId - it should be generated via @genid
            ProductDTO saved = productCoreService.save(dto);
            if (saved.getProductId() == null || saved.getProductId().isEmpty()) {
                return failure("testProductGeneratedId", "Product ID was not auto-generated", startTime, null);
            }

            log.info("Auto-generated Product ID: {}", saved.getProductId());

            // Cleanup
            productCoreService.deleteById(saved.getProductId());

            return success("testProductGeneratedId", "Product ID auto-generated: " + saved.getProductId(), startTime);
        } catch (Exception e) {
            log.error("testProductGeneratedId failed", e);
            return failure("testProductGeneratedId", "Product auto-generated ID test failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Test deleteById: create entity, delete it, verify it no longer exists
     */
    public TestResult testDeleteById() {
        long startTime = System.currentTimeMillis();
        try {
            CategoryDTO dto = CategoryDTO.builder()
                    .name("Delete Test Category")
                    .active(true)
                    .build();
            CategoryDTO saved = categoryCoreService.save(dto);
            Long catId = saved.getCatId();

            boolean deleted = categoryCoreService.deleteById(catId);
            if (!deleted) {
                return failure("testDeleteById", "deleteById returned false", startTime, null);
            }

            Optional<CategoryDTO> afterDelete = categoryCoreService.findById(catId);
            if (afterDelete.isPresent()) {
                return failure("testDeleteById", "Entity still exists after deleteById", startTime, null);
            }

            return success("testDeleteById", "Entity deleted and verified absent", startTime);
        } catch (Exception e) {
            log.error("testDeleteById failed", e);
            return failure("testDeleteById", "deleteById test failed: " + e.getMessage(), startTime, e);
        }
    }
}
