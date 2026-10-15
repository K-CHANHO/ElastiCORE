package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests DTO generation features including field presence, template DTOs,
 * search DTOs, and mapper integration.
 *
 * Generated and managed by ElastiCORE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DtoTestExecutor extends AbstractTestExecutor {

    @Override
    public TestCategory getCategory() {
        return TestCategory.DTO;
    }

    /**
     * Verify ProductDTO has expected fields using reflection
     */
    public TestResult testProductDTOFields() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = io.elasticore.blueprint.domain.shop.dto.ProductDTO.class;
            Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            String[] expectedFields = {
                    "productId", "name", "sku", "description", "price", "costPrice",
                    "stockQuantity", "weight", "rating", "active", "launchDate",
                    "lastRestocked", "tags", "createdBy", "lastModifiedBy",
                    "categoryName", "categoryCatId"
            };

            for (String expected : expectedFields) {
                if (!fieldNames.contains(expected)) {
                    return failure("testProductDTOFields",
                            "Missing field in ProductDTO: " + expected, startTime, null);
                }
            }

            return success("testProductDTOFields",
                    "ProductDTO has all " + expectedFields.length + " expected fields", startTime);
        } catch (Exception e) {
            log.error("testProductDTOFields failed", e);
            return failure("testProductDTOFields", "ProductDTO field check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify ProductInputDTO class exists (second DTO template)
     */
    public TestResult testProductInputDTOExists() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = Class.forName("io.elasticore.blueprint.domain.shop.dto.ProductInputDTO");
            if (clazz == null) {
                return failure("testProductInputDTOExists", "ProductInputDTO class not found", startTime, null);
            }

            // Verify key fields exist
            Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            if (!fieldNames.contains("productId") || !fieldNames.contains("name") || !fieldNames.contains("price")) {
                return failure("testProductInputDTOExists",
                        "ProductInputDTO missing core fields (productId, name, price)", startTime, null);
            }

            return success("testProductInputDTOExists",
                    "ProductInputDTO exists with " + fieldNames.size() + " fields", startTime);
        } catch (ClassNotFoundException e) {
            return failure("testProductInputDTOExists", "ProductInputDTO class not found on classpath", startTime, e);
        } catch (Exception e) {
            log.error("testProductInputDTOExists failed", e);
            return failure("testProductInputDTOExists", "ProductInputDTO check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify ProductKeyDTO exists with productId field
     */
    public TestResult testKeyDTOExists() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = Class.forName("io.elasticore.blueprint.domain.shop.dto.ProductKeyDTO");
            Field productIdField = clazz.getDeclaredField("productId");
            if (productIdField == null) {
                return failure("testKeyDTOExists", "ProductKeyDTO missing productId field", startTime, null);
            }

            if (!productIdField.getType().equals(String.class)) {
                return failure("testKeyDTOExists",
                        "ProductKeyDTO.productId is not String type, found: " + productIdField.getType().getName(), startTime, null);
            }

            return success("testKeyDTOExists", "ProductKeyDTO exists with String productId field", startTime);
        } catch (ClassNotFoundException e) {
            return failure("testKeyDTOExists", "ProductKeyDTO class not found on classpath", startTime, e);
        } catch (NoSuchFieldException e) {
            return failure("testKeyDTOExists", "ProductKeyDTO missing productId field", startTime, e);
        } catch (Exception e) {
            log.error("testKeyDTOExists failed", e);
            return failure("testKeyDTOExists", "ProductKeyDTO check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify ProductSrchDTO exists with search fields
     */
    public TestResult testSearchDTOExists() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = Class.forName("io.elasticore.blueprint.domain.shop.dto.ProductSrchDTO");
            Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            String[] expectedSearchFields = {"name", "sku", "priceFrom", "priceTo", "active"};
            for (String expected : expectedSearchFields) {
                if (!fieldNames.contains(expected)) {
                    return failure("testSearchDTOExists",
                            "ProductSrchDTO missing search field: " + expected, startTime, null);
                }
            }

            return success("testSearchDTOExists",
                    "ProductSrchDTO exists with expected search fields", startTime);
        } catch (ClassNotFoundException e) {
            return failure("testSearchDTOExists", "ProductSrchDTO class not found on classpath", startTime, e);
        } catch (Exception e) {
            log.error("testSearchDTOExists failed", e);
            return failure("testSearchDTOExists", "ProductSrchDTO check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify ProductDTO has categoryCatId and categoryName from @ref annotation
     */
    public TestResult testRefFields() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = io.elasticore.blueprint.domain.shop.dto.ProductDTO.class;
            Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            if (!fieldNames.contains("categoryCatId")) {
                return failure("testRefFields", "ProductDTO missing @ref field: categoryCatId", startTime, null);
            }
            if (!fieldNames.contains("categoryName")) {
                return failure("testRefFields", "ProductDTO missing @ref field: categoryName", startTime, null);
            }

            // Verify types
            Field catIdField = clazz.getDeclaredField("categoryCatId");
            if (!catIdField.getType().equals(Long.class)) {
                return failure("testRefFields",
                        "categoryCatId type mismatch: expected Long, got " + catIdField.getType().getName(), startTime, null);
            }

            Field catNameField = clazz.getDeclaredField("categoryName");
            if (!catNameField.getType().equals(String.class)) {
                return failure("testRefFields",
                        "categoryName type mismatch: expected String, got " + catNameField.getType().getName(), startTime, null);
            }

            return success("testRefFields",
                    "ProductDTO has @ref fields: categoryCatId (Long), categoryName (String)", startTime);
        } catch (NoSuchFieldException e) {
            return failure("testRefFields", "Missing @ref field: " + e.getMessage(), startTime, e);
        } catch (Exception e) {
            log.error("testRefFields failed", e);
            return failure("testRefFields", "@ref fields check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify OrderDTO has customer reference fields: customerFirstName, customerEmail
     */
    public TestResult testOrderDTORefFields() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = io.elasticore.blueprint.domain.shop.dto.OrderDTO.class;
            Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            if (!fieldNames.contains("customerFirstName")) {
                return failure("testOrderDTORefFields",
                        "OrderDTO missing @ref field: customerFirstName", startTime, null);
            }
            if (!fieldNames.contains("customerEmail")) {
                return failure("testOrderDTORefFields",
                        "OrderDTO missing @ref field: customerEmail", startTime, null);
            }

            return success("testOrderDTORefFields",
                    "OrderDTO has @ref fields: customerFirstName, customerEmail", startTime);
        } catch (Exception e) {
            log.error("testOrderDTORefFields failed", e);
            return failure("testOrderDTORefFields", "OrderDTO @ref check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify ProductSrchDTO has date range and price range fields
     */
    public TestResult testSearchDTODateRange() {
        long startTime = System.currentTimeMillis();
        try {
            Class<?> clazz = io.elasticore.blueprint.domain.shop.dto.ProductSrchDTO.class;
            Set<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            String[] rangeFields = {"priceFrom", "priceTo", "launchDateFrom", "launchDateTo"};
            for (String expected : rangeFields) {
                if (!fieldNames.contains(expected)) {
                    return failure("testSearchDTODateRange",
                            "ProductSrchDTO missing range field: " + expected, startTime, null);
                }
            }

            // Verify types
            Field priceFrom = clazz.getDeclaredField("priceFrom");
            if (!priceFrom.getType().getName().contains("BigDecimal")) {
                return failure("testSearchDTODateRange",
                        "priceFrom type mismatch: expected BigDecimal, got " + priceFrom.getType().getName(), startTime, null);
            }

            Field launchDateFrom = clazz.getDeclaredField("launchDateFrom");
            if (!launchDateFrom.getType().getName().contains("LocalDate")) {
                return failure("testSearchDTODateRange",
                        "launchDateFrom type mismatch: expected LocalDate, got " + launchDateFrom.getType().getName(), startTime, null);
            }

            return success("testSearchDTODateRange",
                    "ProductSrchDTO has range fields: priceFrom/priceTo (BigDecimal), launchDateFrom/launchDateTo (LocalDate)", startTime);
        } catch (NoSuchFieldException e) {
            return failure("testSearchDTODateRange", "Missing range field: " + e.getMessage(), startTime, e);
        } catch (Exception e) {
            log.error("testSearchDTODateRange failed", e);
            return failure("testSearchDTODateRange", "Search DTO range check failed: " + e.getMessage(), startTime, e);
        }
    }

    /**
     * Verify ShopMapper.toDTO(Product) works for entity-to-DTO conversion
     */
    public TestResult testMapperToDTO() {
        long startTime = System.currentTimeMillis();
        try {
            // Create a Product entity directly
            io.elasticore.blueprint.domain.shop.entity.Product entity =
                    new io.elasticore.blueprint.domain.shop.entity.Product();
            entity.setProductId("test-mapper-id");
            entity.setName("Mapper Test Product");
            entity.setSku("MAP-SKU-001");
            entity.setPrice(new java.math.BigDecimal("25.00"));
            entity.setStockQuantity(10);
            entity.setActive(true);

            io.elasticore.blueprint.domain.shop.dto.ProductDTO dto =
                    io.elasticore.blueprint.domain.shop.dto.ShopMapper.toDTO(entity);
            if (dto == null) {
                return failure("testMapperToDTO", "ShopMapper.toDTO returned null", startTime, null);
            }
            if (!"Mapper Test Product".equals(dto.getName())) {
                return failure("testMapperToDTO", "Mapper did not copy name field", startTime, null);
            }
            if (!"test-mapper-id".equals(dto.getProductId())) {
                return failure("testMapperToDTO", "Mapper did not copy productId field", startTime, null);
            }

            return success("testMapperToDTO", "ShopMapper.toDTO(Product) correctly converts entity to DTO", startTime);
        } catch (Exception e) {
            log.error("testMapperToDTO failed", e);
            return failure("testMapperToDTO", "ShopMapper.toDTO test failed: " + e.getMessage(), startTime, e);
        }
    }
}
