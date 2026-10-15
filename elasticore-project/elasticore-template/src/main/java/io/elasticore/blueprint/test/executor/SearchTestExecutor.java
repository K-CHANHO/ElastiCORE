package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.domain.hr.dto.DepartmentDTO;
import io.elasticore.blueprint.domain.hr.dto.DepartmentSrchDTO;
import io.elasticore.blueprint.domain.hr.enums.DeptStatus;
import io.elasticore.blueprint.domain.hr.service.DepartmentCoreService;
import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests all search operators via the HR Department search API.
 * Each test method exercises a specific search condition type
 * (eq, like, between, gte, lte, gt, lt, starts-with, ends-with, etc.)
 * and verifies that the search mechanism executes without error.
 *
 * Tests are defensive: an empty result set is acceptable as long as
 * no exception is thrown, because data availability depends on
 * DataInitializer running first.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTestExecutor extends AbstractTestExecutor {

    private final DepartmentCoreService departmentCoreService;

    @Override
    public TestCategory getCategory() {
        return TestCategory.SEARCH;
    }

    // ===== 1. Exact match (eq) on code =====
    public TestResult testSearchEq() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setCode("ENG");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchEq",
                    "EQ search on code executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchEq failed", e);
            return failure("testSearchEq", "EQ search on code failed", startTime, e);
        }
    }

    // ===== 2. LIKE search on name (%_% pattern) =====
    public TestResult testSearchLike() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setName("Eng");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchLike",
                    "LIKE search on name executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchLike failed", e);
            return failure("testSearchLike", "LIKE search on name failed", startTime, e);
        }
    }

    // ===== 3. Case-insensitive LIKE search (%__% pattern) =====
    public TestResult testSearchCaseInsensitive() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setNameInsensitive("engineering");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchCaseInsensitive",
                    "Case-insensitive LIKE search executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchCaseInsensitive failed", e);
            return failure("testSearchCaseInsensitive", "Case-insensitive LIKE search failed", startTime, e);
        }
    }

    // ===== 4. Dynamic search (like if %, else eq) =====
    public TestResult testSearchDynamic() {
        long startTime = System.currentTimeMillis();
        try {
            // Test with % for LIKE behavior
            DepartmentSrchDTO dtoLike = new DepartmentSrchDTO();
            dtoLike.setDescription("%tech%");
            List<DepartmentDTO> likeResults = departmentCoreService.listBySearch(dtoLike);

            // Test without % for EQ behavior
            DepartmentSrchDTO dtoEq = new DepartmentSrchDTO();
            dtoEq.setDescription("Technology Department");
            List<DepartmentDTO> eqResults = departmentCoreService.listBySearch(dtoEq);

            return success("testSearchDynamic",
                    "Dynamic search executed successfully, LIKE found " + likeResults.size()
                            + " results, EQ found " + eqResults.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchDynamic failed", e);
            return failure("testSearchDynamic", "Dynamic search failed", startTime, e);
        }
    }

    // ===== 5. BETWEEN search on budget =====
    public TestResult testSearchBetween() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setBudgetFrom(new BigDecimal("10000"));
            dto.setBudgetTo(new BigDecimal("1000000"));

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchBetween",
                    "BETWEEN search on budget executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchBetween failed", e);
            return failure("testSearchBetween", "BETWEEN search on budget failed", startTime, e);
        }
    }

    // ===== 6. GTE (>=) search on headCount =====
    public TestResult testSearchGte() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setHeadCount(5);

            long count = departmentCoreService.countBySearch(dto);
            return success("testSearchGte",
                    "GTE search on headCount executed successfully, count = " + count,
                    startTime);
        } catch (Exception e) {
            log.error("testSearchGte failed", e);
            return failure("testSearchGte", "GTE search on headCount failed", startTime, e);
        }
    }

    // ===== 7. LTE (<=) search on maxCapacity =====
    public TestResult testSearchLte() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setMaxCapacity(100);

            long count = departmentCoreService.countBySearch(dto);
            return success("testSearchLte",
                    "LTE search on maxCapacity executed successfully, count = " + count,
                    startTime);
        } catch (Exception e) {
            log.error("testSearchLte failed", e);
            return failure("testSearchLte", "LTE search on maxCapacity failed", startTime, e);
        }
    }

    // ===== 8. GT (>) search on floorNumber =====
    public TestResult testSearchGt() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setFloorNumber(1);

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchGt",
                    "GT search on floorNumber executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchGt failed", e);
            return failure("testSearchGt", "GT search on floorNumber failed", startTime, e);
        }
    }

    // ===== 9. LT (<) search on minSalary =====
    public TestResult testSearchLt() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setMinSalary(new BigDecimal("100000"));

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchLt",
                    "LT search on minSalary executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchLt failed", e);
            return failure("testSearchLt", "LT search on minSalary failed", startTime, e);
        }
    }

    // ===== 10. Starts-with (_%) search on namePrefix =====
    public TestResult testSearchStartsWith() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setNamePrefix("Eng");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchStartsWith",
                    "STARTS_WITH search on namePrefix executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchStartsWith failed", e);
            return failure("testSearchStartsWith", "STARTS_WITH search on namePrefix failed", startTime, e);
        }
    }

    // ===== 11. Ends-with (%_) search on nameSuffix =====
    public TestResult testSearchEndsWith() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setNameSuffix("ing");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchEndsWith",
                    "ENDS_WITH search on nameSuffix executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchEndsWith failed", e);
            return failure("testSearchEndsWith", "ENDS_WITH search on nameSuffix failed", startTime, e);
        }
    }

    // ===== 12. Case-insensitive starts-with (__%) search on namePrefixCI =====
    public TestResult testSearchStartsWithCI() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setNamePrefixCI("eng");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchStartsWithCI",
                    "STARTS_WITH CI search on namePrefixCI executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchStartsWithCI failed", e);
            return failure("testSearchStartsWithCI", "STARTS_WITH CI search on namePrefixCI failed", startTime, e);
        }
    }

    // ===== 13. Case-insensitive ends-with (%__) search on nameSuffixCI =====
    public TestResult testSearchEndsWithCI() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setNameSuffixCI("ING");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchEndsWithCI",
                    "ENDS_WITH CI search on nameSuffixCI executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchEndsWithCI failed", e);
            return failure("testSearchEndsWithCI", "ENDS_WITH CI search on nameSuffixCI failed", startTime, e);
        }
    }

    // ===== 14. Case-insensitive exact match (__=) search on nameExactCI =====
    public TestResult testSearchExactCI() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setNameExactCI("ENGINEERING");

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchExactCI",
                    "EXACT CI search on nameExactCI executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchExactCI failed", e);
            return failure("testSearchExactCI", "EXACT CI search on nameExactCI failed", startTime, e);
        }
    }

    // ===== 15. Boolean EQ search on active =====
    public TestResult testSearchBoolean() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setActive(true);

            long count = departmentCoreService.countBySearch(dto);
            return success("testSearchBoolean",
                    "Boolean EQ search on active executed successfully, count = " + count,
                    startTime);
        } catch (Exception e) {
            log.error("testSearchBoolean failed", e);
            return failure("testSearchBoolean", "Boolean EQ search on active failed", startTime, e);
        }
    }

    // ===== 16. IN search on status (enum) =====
    public TestResult testSearchIn() {
        long startTime = System.currentTimeMillis();
        try {
            DepartmentSrchDTO dto = new DepartmentSrchDTO();
            dto.setStatus(DeptStatus.ACTIVE);

            List<DepartmentDTO> results = departmentCoreService.listBySearch(dto);
            return success("testSearchIn",
                    "IN search on status executed successfully, found " + results.size() + " results",
                    startTime);
        } catch (Exception e) {
            log.error("testSearchIn failed", e);
            return failure("testSearchIn", "IN search on status failed", startTime, e);
        }
    }
}
