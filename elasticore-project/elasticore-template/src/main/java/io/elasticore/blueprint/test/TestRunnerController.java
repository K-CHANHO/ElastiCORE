package io.elasticore.blueprint.test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for executing ElastiCORE feature tests.
 * Provides endpoints for running all tests, category tests, and individual tests.
 */
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestRunnerController {

    private final List<AbstractTestExecutor> executors;

    @GetMapping("/run-all")
    public Map<String, Object> runAll() {
        List<TestResult> results = new ArrayList<>();
        for (AbstractTestExecutor executor : executors) {
            results.addAll(executor.runAll());
        }
        return buildResponse(results);
    }

    @GetMapping("/run/{category}")
    public Map<String, Object> runCategory(@PathVariable String category) {
        List<TestResult> results = new ArrayList<>();
        for (AbstractTestExecutor executor : executors) {
            if (executor.getCategory().name().equalsIgnoreCase(category)) {
                results.addAll(executor.runAll());
            }
        }
        return buildResponse(results);
    }

    @GetMapping("/categories")
    public List<Map<String, Object>> getCategories() {
        return Arrays.stream(TestCategory.values())
            .map(cat -> {
                long testCount = executors.stream()
                    .filter(e -> e.getCategory() == cat)
                    .count();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", cat.name());
                m.put("hasExecutor", testCount > 0);
                return m;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> buildResponse(List<TestResult> results) {
        long passed = results.stream().filter(TestResult::isPassed).count();
        long failed = results.size() - passed;
        double rate = results.isEmpty() ? 0 : (passed * 100.0 / results.size());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total", results.size());
        response.put("passed", passed);
        response.put("failed", failed);
        response.put("rate", Math.round(rate * 100.0) / 100.0);
        response.put("results", results);

        // Group by category
        Map<String, List<TestResult>> grouped = results.stream()
            .collect(Collectors.groupingBy(TestResult::getCategory));
        Map<String, Map<String, Object>> categoryStats = new LinkedHashMap<>();
        for (Map.Entry<String, List<TestResult>> entry : grouped.entrySet()) {
            long catPassed = entry.getValue().stream().filter(TestResult::isPassed).count();
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("total", entry.getValue().size());
            stat.put("passed", catPassed);
            stat.put("failed", entry.getValue().size() - catPassed);
            stat.put("rate", entry.getValue().isEmpty() ? 0 :
                Math.round(catPassed * 10000.0 / entry.getValue().size()) / 100.0);
            categoryStats.put(entry.getKey(), stat);
        }
        response.put("categoryStats", categoryStats);

        return response;
    }
}
