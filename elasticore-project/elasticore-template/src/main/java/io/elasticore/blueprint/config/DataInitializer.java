package io.elasticore.blueprint.config;

import io.elasticore.blueprint.domain.content.dto.*;
import io.elasticore.blueprint.domain.content.enums.ArticleStatus;
import io.elasticore.blueprint.domain.content.service.*;
import io.elasticore.blueprint.domain.hr.dto.*;
import io.elasticore.blueprint.domain.hr.enums.DeptStatus;
import io.elasticore.blueprint.domain.hr.enums.ProjectStatus;
import io.elasticore.blueprint.domain.hr.service.*;
import io.elasticore.blueprint.domain.integration.dto.*;
import io.elasticore.blueprint.domain.integration.enums.SyncStatus;
import io.elasticore.blueprint.domain.integration.service.*;
import io.elasticore.blueprint.domain.shop.dto.*;
import io.elasticore.blueprint.domain.shop.enums.CustomerType;
import io.elasticore.blueprint.domain.shop.enums.OrderStatus;
import io.elasticore.blueprint.domain.shop.enums.PaymentMethod;
import io.elasticore.blueprint.domain.shop.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Spring Boot CommandLineRunner that seeds test data into PostgreSQL
 * using generated ElastiCORE services.
 *
 * This initializer is IDEMPOTENT: it checks if data exists before inserting.
 * Data is only created when the relevant tables are empty.
 *
 * Generated for ElastiCORE blueprint template project.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // Shop domain services
    private final CategoryCoreService categoryCoreService;
    private final ProductCoreService productCoreService;
    private final CustomerCoreService customerCoreService;
    private final OrderCoreService orderCoreService;
    private final VipProfileCoreService vipProfileCoreService;

    // HR domain services
    private final DepartmentCoreService departmentCoreService;
    private final EmployeeCoreService employeeCoreService;
    private final SkillCoreService skillCoreService;
    private final ProjectCoreService projectCoreService;
    private final ProjectAssignmentCoreService projectAssignmentCoreService;

    // Integration domain services
    private final ApiLogCoreService apiLogCoreService;
    private final SyncRecordCoreService syncRecordCoreService;

    // Content domain services
    private final ContentCategoryCoreService contentCategoryCoreService;
    private final ArticleCoreService articleCoreService;
    private final TagCoreService tagCoreService;
    private final CommentCoreService commentCoreService;

    @Override
    public void run(String... args) {
        log.info("=== DataInitializer: Starting test data seeding ===");
        try {
            initShopData();
            initHrData();
            initIntegrationData();
            initContentData();
            log.info("=== DataInitializer: Test data seeding completed ===");
        } catch (Exception e) {
            log.error("DataInitializer: Failed to seed test data. Application will continue.", e);
        }
    }

    // ========================================================================
    // SHOP DOMAIN
    // ========================================================================

    private void initShopData() {
        try {
            initCategories();
            initProducts();
            initCustomers();
            initOrders();
        } catch (Exception e) {
            log.error("Failed to initialize shop data", e);
        }
    }

    private void initCategories() {
        if (!categoryCoreService.findAll().isEmpty()) {
            log.info("Shop categories already exist, skipping.");
            return;
        }
        log.info("Seeding shop categories...");

        // Parent categories
        CategoryDTO electronics = categoryCoreService.save(CategoryDTO.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .sortOrder(1)
                .active(true)
                .build());

        CategoryDTO clothing = categoryCoreService.save(CategoryDTO.builder()
                .name("Clothing")
                .description("Apparel and fashion items")
                .sortOrder(2)
                .active(true)
                .build());

        CategoryDTO home = categoryCoreService.save(CategoryDTO.builder()
                .name("Home & Garden")
                .description("Home improvement and garden supplies")
                .sortOrder(3)
                .active(true)
                .build());

        // Child categories (sub-categories stored as separate categories)
        categoryCoreService.save(CategoryDTO.builder()
                .name("Smartphones")
                .description("Mobile phones and accessories")
                .sortOrder(1)
                .active(true)
                .build());

        categoryCoreService.save(CategoryDTO.builder()
                .name("Laptops")
                .description("Notebook computers and tablets")
                .sortOrder(2)
                .active(true)
                .build());

        log.info("Seeded 5 shop categories.");
    }

    private void initProducts() {
        if (!productCoreService.findAll().isEmpty()) {
            log.info("Products already exist, skipping.");
            return;
        }
        log.info("Seeding products...");

        // Get first category ID for FK references
        List<CategoryDTO> categories = categoryCoreService.findAll();
        Long electronicsId = categories.isEmpty() ? null : categories.get(0).getCatId();
        Long clothingId = categories.size() > 1 ? categories.get(1).getCatId() : electronicsId;
        Long homeId = categories.size() > 2 ? categories.get(2).getCatId() : electronicsId;

        String[][] products = {
                {"iPhone 15 Pro", "SKU-IP15P", "Latest Apple smartphone", "1199.99", "899.99"},
                {"Samsung Galaxy S24", "SKU-SGS24", "Samsung flagship phone", "999.99", "749.99"},
                {"MacBook Pro 16", "SKU-MBP16", "Apple laptop with M3 chip", "2499.99", "1899.99"},
                {"Sony WH-1000XM5", "SKU-SNWH5", "Noise cancelling headphones", "349.99", "249.99"},
                {"iPad Air", "SKU-IPDA5", "Apple tablet 10.9 inch", "599.99", "449.99"},
                {"Nike Air Max", "SKU-NAM90", "Classic running shoes", "129.99", "64.99"},
                {"Levi's 501 Jeans", "SKU-LV501", "Original fit jeans", "79.99", "39.99"},
                {"Dyson V15", "SKU-DYV15", "Cordless vacuum cleaner", "749.99", "549.99"},
                {"KitchenAid Mixer", "SKU-KAMIX", "Stand mixer 5-quart", "449.99", "299.99"},
                {"Bose SoundLink", "SKU-BSSL3", "Portable Bluetooth speaker", "149.99", "99.99"},
        };

        Long[] catIds = {electronicsId, electronicsId, electronicsId, electronicsId, electronicsId,
                clothingId, clothingId, homeId, homeId, electronicsId};
        int[] stocks = {150, 200, 75, 300, 120, 500, 400, 60, 90, 250};
        float[] weights = {0.2f, 0.19f, 2.1f, 0.25f, 0.46f, 0.35f, 0.8f, 3.1f, 11.5f, 0.68f};
        double[] ratings = {4.8, 4.6, 4.9, 4.7, 4.5, 4.3, 4.4, 4.6, 4.8, 4.5};

        for (int i = 0; i < products.length; i++) {
            productCoreService.save(ProductDTO.builder()
                    .productId(UUID.randomUUID().toString())
                    .name(products[i][0])
                    .sku(products[i][1])
                    .description(products[i][2])
                    .price(new BigDecimal(products[i][3]))
                    .costPrice(new BigDecimal(products[i][4]))
                    .stockQuantity(stocks[i])
                    .weight(weights[i])
                    .rating(ratings[i])
                    .active(true)
                    .launchDate(LocalDate.of(2024, (i % 12) + 1, 15))
                    .lastRestocked(LocalDateTime.now().minusDays(i * 3L))
                    .tags(new String[]{"bestseller", "featured"})
                    .categoryCatId(catIds[i])
                    .build());
        }

        log.info("Seeded 10 products.");
    }

    private void initCustomers() {
        if (!customerCoreService.findAll().isEmpty()) {
            log.info("Customers already exist, skipping.");
            return;
        }
        log.info("Seeding customers...");

        String[][] customerData = {
                {"John", "Doe", "john.doe@example.com", "010-1234-5678"},
                {"Jane", "Smith", "jane.smith@example.com", "010-2345-6789"},
                {"Robert", "Johnson", "robert.j@example.com", "010-3456-7890"},
                {"Emily", "Williams", "emily.w@example.com", "010-4567-8901"},
                {"Michael", "Brown", "michael.b@example.com", "010-5678-9012"},
        };
        CustomerType[] types = {CustomerType.INDIVIDUAL, CustomerType.BUSINESS, CustomerType.WHOLESALE,
                CustomerType.INDIVIDUAL, CustomerType.BUSINESS};
        long[] loyaltyPoints = {1500L, 5200L, 12000L, 800L, 3400L};

        List<CustomerDTO> savedCustomers = new ArrayList<>();
        for (int i = 0; i < customerData.length; i++) {
            CustomerDTO saved = customerCoreService.save(CustomerDTO.builder()
                    .firstName(customerData[i][0])
                    .lastName(customerData[i][1])
                    .email(customerData[i][2])
                    .phone(customerData[i][3])
                    .dateOfBirth(LocalDate.of(1985 + i, (i % 12) + 1, 10 + i))
                    .loyaltyPoints(loyaltyPoints[i])
                    .customerType(types[i])
                    .build());
            savedCustomers.add(saved);
        }

        // Create VIP profiles for VIP/PREMIUM customers
        initVipProfiles(savedCustomers);

        log.info("Seeded 5 customers.");
    }

    private void initVipProfiles(List<CustomerDTO> customers) {
        if (!vipProfileCoreService.findAll().isEmpty()) {
            log.info("VIP profiles already exist, skipping.");
            return;
        }
        log.info("Seeding VIP profiles...");

        if (customers.size() >= 2) {
            vipProfileCoreService.save(VipProfileDTO.builder()
                    .customerCustomerId(customers.get(1).getCustomerId())
                    .tier("GOLD")
                    .discountRate(15.0)
                    .enrollDate(LocalDate.of(2023, 6, 1))
                    .build());

            vipProfileCoreService.save(VipProfileDTO.builder()
                    .customerCustomerId(customers.get(2).getCustomerId())
                    .tier("PLATINUM")
                    .discountRate(25.0)
                    .enrollDate(LocalDate.of(2022, 1, 15))
                    .build());
        }

        log.info("Seeded VIP profiles.");
    }

    private void initOrders() {
        if (!orderCoreService.findAll().isEmpty()) {
            log.info("Orders already exist, skipping.");
            return;
        }
        log.info("Seeding orders...");

        List<ProductDTO> products = productCoreService.findAll();
        if (products.isEmpty()) {
            log.warn("No products found, skipping order creation.");
            return;
        }

        OrderStatus[] statuses = {OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING,
                OrderStatus.SHIPPED, OrderStatus.DELIVERED};
        PaymentMethod[] methods = {PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_CARD,
                PaymentMethod.BANK_TRANSFER, PaymentMethod.PAYPAL, PaymentMethod.CASH};
        String[] notes = {"Rush delivery requested", "Gift wrapping needed", "Leave at front door",
                "Call before delivery", "Standard shipping"};
        String[] trackingNumbers = {"TRK-001-2024", "TRK-002-2024", "TRK-003-2024", "TRK-004-2024", "TRK-005-2024"};

        for (int i = 0; i < 5; i++) {
            // Calculate total from items
            BigDecimal unitPrice1 = products.get(i % products.size()).getPrice();
            BigDecimal unitPrice2 = products.get((i + 3) % products.size()).getPrice();
            int qty1 = (i % 3) + 1;
            int qty2 = (i % 2) + 1;
            BigDecimal total = unitPrice1.multiply(BigDecimal.valueOf(qty1))
                    .add(unitPrice2.multiply(BigDecimal.valueOf(qty2)));

            // Build order items inline
            List<OrderItemDTO> items = new ArrayList<>();
            items.add(OrderItemDTO.builder()
                    .itemSeq(1)
                    .quantity(qty1)
                    .unitPrice(unitPrice1)
                    .discount(BigDecimal.ZERO)
                    .build());
            items.add(OrderItemDTO.builder()
                    .itemSeq(2)
                    .quantity(qty2)
                    .unitPrice(unitPrice2)
                    .discount(new BigDecimal("5.00"))
                    .build());

            orderCoreService.save(OrderDTO.builder()
                    .orderId(UUID.randomUUID().toString())
                    .orderDate(LocalDateTime.now().minusDays(i * 5L))
                    .totalAmount(total)
                    .status(statuses[i])
                    .paymentMethod(methods[i])
                    .notes(notes[i])
                    .trackingNumber(trackingNumbers[i])
                    .items(items)
                    .build());
        }

        log.info("Seeded 5 orders with items.");
    }

    // ========================================================================
    // HR DOMAIN
    // ========================================================================

    private void initHrData() {
        try {
            initDepartments();
            initEmployees();
            initSkills();
            initProjects();
            initProjectAssignments();
        } catch (Exception e) {
            log.error("Failed to initialize HR data", e);
        }
    }

    private void initDepartments() {
        if (!departmentCoreService.findAll().isEmpty()) {
            log.info("Departments already exist, skipping.");
            return;
        }
        log.info("Seeding departments with diverse values for search operator testing...");

        // Department 1: Engineering - Active, high budget, large team
        departmentCoreService.save(DepartmentDTO.builder()
                .code("ENG")
                .name("Engineering")
                .nameInsensitive("engineering")
                .description("Software engineering and development division")
                .budget(new BigDecimal("1000000"))
                .headCount(200)
                .maxCapacity(500)
                .floorNumber(10)
                .minSalary(new BigDecimal("80000"))
                .status(DeptStatus.ACTIVE)
                .namePrefix("Engineering")
                .nameSuffix("Engineering")
                .namePrefixCI("engineering")
                .nameSuffixCI("engineering")
                .nameExactCI("engineering")
                .active(true)
                .build());

        // Department 2: Finance - Active, medium budget, medium team
        departmentCoreService.save(DepartmentDTO.builder()
                .code("FIN")
                .name("Finance")
                .nameInsensitive("finance")
                .description("Financial planning and accounting")
                .budget(new BigDecimal("500000"))
                .headCount(50)
                .maxCapacity(100)
                .floorNumber(5)
                .minSalary(new BigDecimal("60000"))
                .status(DeptStatus.ACTIVE)
                .namePrefix("Finance")
                .nameSuffix("Finance")
                .namePrefixCI("finance")
                .nameSuffixCI("finance")
                .nameExactCI("finance")
                .active(true)
                .build());

        // Department 3: Marketing - Active, lower budget, small team
        departmentCoreService.save(DepartmentDTO.builder()
                .code("MKT")
                .name("Marketing")
                .nameInsensitive("marketing")
                .description("Brand management and digital marketing")
                .budget(new BigDecimal("300000"))
                .headCount(30)
                .maxCapacity(80)
                .floorNumber(3)
                .minSalary(new BigDecimal("45000"))
                .status(DeptStatus.ACTIVE)
                .namePrefix("Marketing")
                .nameSuffix("Marketing")
                .namePrefixCI("marketing")
                .nameSuffixCI("marketing")
                .nameExactCI("marketing")
                .active(true)
                .build());

        // Department 4: Human Resources - Inactive, low budget, small team
        departmentCoreService.save(DepartmentDTO.builder()
                .code("HR")
                .name("Human Resources")
                .nameInsensitive("human resources")
                .description("Employee relations and talent management")
                .budget(new BigDecimal("150000"))
                .headCount(15)
                .maxCapacity(50)
                .floorNumber(2)
                .minSalary(new BigDecimal("40000"))
                .status(DeptStatus.INACTIVE)
                .namePrefix("Human Resources")
                .nameSuffix("Human Resources")
                .namePrefixCI("human resources")
                .nameSuffixCI("human resources")
                .nameExactCI("human resources")
                .active(false)
                .build());

        // Department 5: Legal - Merged, minimal budget, tiny team
        departmentCoreService.save(DepartmentDTO.builder()
                .code("LEG")
                .name("Legal")
                .nameInsensitive("legal")
                .description("Corporate legal affairs and compliance")
                .budget(new BigDecimal("100000"))
                .headCount(10)
                .maxCapacity(30)
                .floorNumber(1)
                .minSalary(new BigDecimal("30000"))
                .status(DeptStatus.MERGED)
                .namePrefix("Legal")
                .nameSuffix("Legal")
                .namePrefixCI("legal")
                .nameSuffixCI("legal")
                .nameExactCI("legal")
                .active(false)
                .build());

        log.info("Seeded 5 departments with diverse search-testable values.");
    }

    private void initEmployees() {
        if (!employeeCoreService.findAll().isEmpty()) {
            log.info("Employees already exist, skipping.");
            return;
        }
        log.info("Seeding employees...");

        String[][] empData = {
                {"Alice", "Park", "alice.park@company.com", "010-1111-2222"},
                {"Bob", "Kim", "bob.kim@company.com", "010-2222-3333"},
                {"Charlie", "Lee", "charlie.lee@company.com", "010-3333-4444"},
                {"Diana", "Choi", "diana.choi@company.com", "010-4444-5555"},
                {"Edward", "Jung", "edward.jung@company.com", "010-5555-6666"},
                {"Fiona", "Han", "fiona.han@company.com", "010-6666-7777"},
                {"George", "Yoon", "george.yoon@company.com", "010-7777-8888"},
                {"Hannah", "Shin", "hannah.shin@company.com", "010-8888-9999"},
                {"Ivan", "Oh", "ivan.oh@company.com", "010-9999-0000"},
                {"Julia", "Kang", "julia.kang@company.com", "010-0000-1111"},
        };

        BigDecimal[] salaries = {
                new BigDecimal("95000"), new BigDecimal("85000"), new BigDecimal("72000"),
                new BigDecimal("88000"), new BigDecimal("110000"), new BigDecimal("67000"),
                new BigDecimal("78000"), new BigDecimal("92000"), new BigDecimal("63000"),
                new BigDecimal("105000")
        };
        double[] bonusRates = {15.0, 12.0, 8.0, 10.0, 20.0, 5.0, 9.0, 14.0, 6.0, 18.0};

        for (int i = 0; i < empData.length; i++) {
            employeeCoreService.save(EmployeeDTO.builder()
                    .empId(UUID.randomUUID().toString())
                    .firstName(empData[i][0])
                    .lastName(empData[i][1])
                    .email(empData[i][2])
                    .phone(empData[i][3])
                    .hireDate(LocalDate.of(2020 + (i % 5), (i % 12) + 1, 1 + i))
                    .salary(salaries[i])
                    .bonusRate(bonusRates[i])
                    .active(i < 8)
                    .build());
        }

        log.info("Seeded 10 employees.");
    }

    private void initSkills() {
        if (!skillCoreService.findAll().isEmpty()) {
            log.info("Skills already exist, skipping.");
            return;
        }
        log.info("Seeding skills...");

        String[][] skillData = {
                {"Programming", "Java"},
                {"Programming", "Python"},
                {"Database", "PostgreSQL"},
                {"Cloud", "AWS"},
                {"DevOps", "Kubernetes"},
        };
        int[] proficiencies = {9, 8, 7, 8, 6};

        for (int i = 0; i < skillData.length; i++) {
            skillCoreService.save(SkillDTO.builder()
                    .category(skillData[i][0])
                    .skillName(skillData[i][1])
                    .proficiency(proficiencies[i])
                    .build());
        }

        log.info("Seeded 5 skills.");
    }

    private void initProjects() {
        if (!projectCoreService.findAll().isEmpty()) {
            log.info("Projects already exist, skipping.");
            return;
        }
        log.info("Seeding projects...");

        projectCoreService.save(ProjectDTO.builder()
                .projectCode("PRJ-ALPHA")
                .name("Project Alpha")
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 12, 31))
                .budget(new BigDecimal("500000"))
                .status(ProjectStatus.ACTIVE)
                .build());

        projectCoreService.save(ProjectDTO.builder()
                .projectCode("PRJ-BETA")
                .name("Project Beta")
                .startDate(LocalDate.of(2024, 3, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .budget(new BigDecimal("800000"))
                .status(ProjectStatus.PLANNED)
                .build());

        projectCoreService.save(ProjectDTO.builder()
                .projectCode("PRJ-GAMMA")
                .name("Project Gamma")
                .startDate(LocalDate.of(2023, 6, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .budget(new BigDecimal("300000"))
                .status(ProjectStatus.COMPLETED)
                .build());

        log.info("Seeded 3 projects.");
    }

    private void initProjectAssignments() {
        if (!projectAssignmentCoreService.findAll().isEmpty()) {
            log.info("Project assignments already exist, skipping.");
            return;
        }
        log.info("Seeding project assignments...");

        List<EmployeeDTO> employees = employeeCoreService.findAll();
        List<ProjectDTO> projects = projectCoreService.findAll();

        if (employees.isEmpty() || projects.isEmpty()) {
            log.warn("No employees or projects found, skipping assignment creation.");
            return;
        }

        String[] roles = {"Lead Developer", "Backend Developer", "Frontend Developer", "DevOps Engineer", "QA Engineer"};
        int[] hours = {1500, 1200, 1000, 800, 600};

        for (int i = 0; i < Math.min(5, employees.size()); i++) {
            projectAssignmentCoreService.save(ProjectAssignmentDTO.builder()
                    .empId(employees.get(i).getEmpId())
                    .projectCode(projects.get(i % projects.size()).getProjectCode())
                    .role(roles[i])
                    .assignDate(LocalDate.of(2024, (i % 12) + 1, 10))
                    .hoursAllocated(hours[i])
                    .build());
        }

        log.info("Seeded 5 project assignments.");
    }

    // ========================================================================
    // INTEGRATION DOMAIN
    // ========================================================================

    private void initIntegrationData() {
        try {
            initApiLogs();
            initSyncRecords();
        } catch (Exception e) {
            log.error("Failed to initialize integration data", e);
        }
    }

    private void initApiLogs() {
        if (!apiLogCoreService.findAll().isEmpty()) {
            log.info("API logs already exist, skipping.");
            return;
        }
        log.info("Seeding API logs...");

        String[][] logData = {
                {"/api/v1/products", "GET", "200"},
                {"/api/v1/products/search", "POST", "200"},
                {"/api/v1/orders", "POST", "201"},
                {"/api/v1/customers/123", "GET", "200"},
                {"/api/v1/customers/999", "GET", "404"},
                {"/api/v1/orders/ORD-001", "PUT", "200"},
                {"/api/v1/auth/login", "POST", "200"},
                {"/api/v1/auth/login", "POST", "401"},
                {"/api/v1/reports/sales", "GET", "200"},
                {"/api/v1/products/bulk", "POST", "500"},
        };

        long[] durations = {45L, 120L, 230L, 35L, 28L, 150L, 80L, 60L, 350L, 5000L};
        String[] requestBodies = {
                null, "{\"keyword\":\"laptop\"}", "{\"items\":[{\"sku\":\"SKU-001\",\"qty\":2}]}",
                null, null, "{\"status\":\"SHIPPED\"}", "{\"username\":\"admin\",\"password\":\"***\"}",
                "{\"username\":\"hacker\",\"password\":\"***\"}", null,
                "{\"products\":[{\"sku\":\"SKU-BULK\"}]}"
        };
        String[] responseBodies = {
                "[{\"id\":1}]", "{\"results\":[]}", "{\"orderId\":\"ORD-NEW\"}",
                "{\"name\":\"John\"}", "{\"error\":\"Not Found\"}", "{\"updated\":true}",
                "{\"token\":\"jwt-xxx\"}", "{\"error\":\"Unauthorized\"}",
                "{\"totalSales\":125000}", "{\"error\":\"Internal Server Error\"}"
        };

        for (int i = 0; i < logData.length; i++) {
            apiLogCoreService.save(ApiLogDTO.builder()
                    .endpoint(logData[i][0])
                    .method(logData[i][1])
                    .statusCode(Integer.parseInt(logData[i][2]))
                    .duration(durations[i])
                    .requestBody(requestBodies[i])
                    .responseBody(responseBodies[i])
                    .createdAt(LocalDateTime.now().minusHours(i * 2L))
                    .build());
        }

        log.info("Seeded 10 API log entries.");
    }

    private void initSyncRecords() {
        if (!syncRecordCoreService.findAll().isEmpty()) {
            log.info("Sync records already exist, skipping.");
            return;
        }
        log.info("Seeding sync records...");

        String[][] syncData = {
                {"ERP-System", "CRM-System", "Customer", "CUST-001"},
                {"CRM-System", "DataWarehouse", "Order", "ORD-100"},
                {"HR-System", "Payroll-System", "Employee", "EMP-050"},
                {"Inventory-System", "ERP-System", "Product", "PRD-200"},
                {"ERP-System", "BI-Platform", "SalesReport", "RPT-030"},
        };

        SyncStatus[] statuses = {SyncStatus.SUCCESS, SyncStatus.SUCCESS, SyncStatus.FAILED,
                SyncStatus.IN_PROGRESS, SyncStatus.PENDING};
        int[] retryCounts = {0, 0, 3, 0, 0};
        String[] errors = {null, null, "Connection timeout after 30s", null, null};

        for (int i = 0; i < syncData.length; i++) {
            syncRecordCoreService.save(SyncRecordDTO.builder()
                    .recordId(UUID.randomUUID().toString())
                    .sourceSystem(syncData[i][0])
                    .targetSystem(syncData[i][1])
                    .entityType(syncData[i][2])
                    .entityId(syncData[i][3])
                    .syncStatus(statuses[i])
                    .syncDate(LocalDateTime.now().minusDays(i))
                    .retryCount(retryCounts[i])
                    .errorMessage(errors[i])
                    .build());
        }

        log.info("Seeded 5 sync records.");
    }

    // ========================================================================
    // CONTENT DOMAIN
    // ========================================================================

    private void initContentData() {
        try {
            initContentCategories();
            initArticles();
            initTags();
            initComments();
        } catch (Exception e) {
            log.error("Failed to initialize content data", e);
        }
    }

    private void initContentCategories() {
        if (!contentCategoryCoreService.findAll().isEmpty()) {
            log.info("Content categories already exist, skipping.");
            return;
        }
        log.info("Seeding content categories...");

        contentCategoryCoreService.save(ContentCategoryDTO.builder()
                .name("Technology")
                .slug("technology")
                .description("Tech news and tutorials")
                .sortOrder(1)
                .build());

        contentCategoryCoreService.save(ContentCategoryDTO.builder()
                .name("Business")
                .slug("business")
                .description("Business strategy and management")
                .sortOrder(2)
                .build());

        contentCategoryCoreService.save(ContentCategoryDTO.builder()
                .name("Science")
                .slug("science")
                .description("Scientific discoveries and research")
                .sortOrder(3)
                .build());

        contentCategoryCoreService.save(ContentCategoryDTO.builder()
                .name("Lifestyle")
                .slug("lifestyle")
                .description("Health, wellness, and daily living")
                .sortOrder(4)
                .build());

        contentCategoryCoreService.save(ContentCategoryDTO.builder()
                .name("Education")
                .slug("education")
                .description("Learning resources and academic content")
                .sortOrder(5)
                .build());

        log.info("Seeded 5 content categories.");
    }

    private void initArticles() {
        if (!articleCoreService.findAll().isEmpty()) {
            log.info("Articles already exist, skipping.");
            return;
        }
        log.info("Seeding articles...");

        String[][] articleData = {
                {"Introduction to Spring Boot 3", "intro-spring-boot-3", "A comprehensive guide to Spring Boot 3"},
                {"Understanding JPA Specifications", "understanding-jpa-specs", "Deep dive into JPA Specification API"},
                {"Microservices Architecture Patterns", "microservices-patterns", "Common patterns for microservices"},
                {"PostgreSQL Performance Tuning", "postgresql-perf-tuning", "Tips for optimizing PostgreSQL queries"},
                {"Docker Best Practices", "docker-best-practices", "Container best practices for production"},
                {"REST API Design Guidelines", "rest-api-design", "Building robust and scalable REST APIs"},
                {"Kubernetes for Beginners", "k8s-beginners", "Getting started with Kubernetes orchestration"},
                {"CI/CD Pipeline Setup", "cicd-pipeline-setup", "Automating your deployment pipeline"},
                {"Data Modeling with ElastiCORE", "data-modeling-elasticore", "DSL-based code generation patterns"},
                {"Cloud Native Development", "cloud-native-dev", "Building applications for the cloud"},
        };

        ArticleStatus[] statuses = {ArticleStatus.PUBLISHED, ArticleStatus.PUBLISHED, ArticleStatus.PUBLISHED,
                ArticleStatus.PUBLISHED, ArticleStatus.DRAFT, ArticleStatus.PUBLISHED,
                ArticleStatus.REVIEW, ArticleStatus.PUBLISHED, ArticleStatus.PUBLISHED,
                ArticleStatus.ARCHIVED};

        long[] viewCounts = {15200L, 8300L, 12500L, 6800L, 0L, 9400L, 200L, 7600L, 4500L, 22000L};
        boolean[] featured = {true, false, true, false, false, true, false, false, true, false};

        for (int i = 0; i < articleData.length; i++) {
            articleCoreService.save(ArticleDTO.builder()
                    .articleId(UUID.randomUUID().toString())
                    .title(articleData[i][0])
                    .slug(articleData[i][1])
                    .summary(articleData[i][2])
                    .publishDate(LocalDateTime.now().minusDays(i * 7L))
                    .viewCount(viewCounts[i])
                    .featured(featured[i])
                    .articleStatus(statuses[i])
                    .ratings(new Integer[]{4, 5, 4, 3, 5})
                    .labels(new String[]{"java", "spring", "backend"})
                    .build());
        }

        log.info("Seeded 10 articles.");
    }

    private void initTags() {
        if (!tagCoreService.findAll().isEmpty()) {
            log.info("Tags already exist, skipping.");
            return;
        }
        log.info("Seeding tags...");

        String[][] tagData = {
                {"Java", "#E76F00"},
                {"Spring", "#6DB33F"},
                {"Docker", "#2496ED"},
                {"PostgreSQL", "#336791"},
                {"Kubernetes", "#326CE5"},
        };

        for (String[] tag : tagData) {
            tagCoreService.save(TagDTO.builder()
                    .name(tag[0])
                    .color(tag[1])
                    .build());
        }

        log.info("Seeded 5 tags.");
    }

    private void initComments() {
        if (!commentCoreService.findAll().isEmpty()) {
            log.info("Comments already exist, skipping.");
            return;
        }
        log.info("Seeding comments...");

        String[][] commentData = {
                {"Great article! Very helpful for beginners.", "Alice Park", "alice@example.com"},
                {"Could you add more examples?", "Bob Kim", "bob@example.com"},
                {"This helped me solve a production issue.", "Charlie Lee", "charlie@example.com"},
                {"Well written and easy to follow.", "Diana Choi", "diana@example.com"},
                {"Looking forward to part 2!", "Edward Jung", "edward@example.com"},
                {"The code samples are excellent.", "Fiona Han", "fiona@example.com"},
                {"I have a question about the configuration.", "George Yoon", "george@example.com"},
                {"Bookmarked for future reference.", "Hannah Shin", "hannah@example.com"},
                {"This is exactly what I was looking for.", "Ivan Oh", "ivan@example.com"},
                {"Please cover advanced topics next.", "Julia Kang", "julia@example.com"},
        };

        boolean[] approvals = {true, true, true, true, false, true, false, true, true, false};

        for (int i = 0; i < commentData.length; i++) {
            commentCoreService.save(CommentDTO.builder()
                    .content(commentData[i][0])
                    .authorName(commentData[i][1])
                    .authorEmail(commentData[i][2])
                    .approved(approvals[i])
                    .build());
        }

        log.info("Seeded 10 comments.");
    }
}
