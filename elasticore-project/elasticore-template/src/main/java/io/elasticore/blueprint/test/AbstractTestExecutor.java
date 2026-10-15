package io.elasticore.blueprint.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for test executors. Each subclass tests a specific category
 * of ElastiCORE features.
 */
public abstract class AbstractTestExecutor {

    /**
     * Returns the test category this executor handles.
     */
    public abstract TestCategory getCategory();

    /**
     * Runs all test methods in this executor.
     * Test methods must start with "test" and return TestResult.
     */
    public List<TestResult> runAll() {
        List<TestResult> results = new ArrayList<>();
        Method[] methods = this.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("test") &&
                method.getReturnType().equals(TestResult.class) &&
                method.getParameterCount() == 0) {
                try {
                    method.setAccessible(true);
                    TestResult result = (TestResult) method.invoke(this);
                    if (result != null) {
                        result.setCategory(getCategory().name());
                        results.add(result);
                    }
                } catch (Exception e) {
                    results.add(TestResult.builder()
                        .testName(method.getName())
                        .category(getCategory().name())
                        .passed(false)
                        .message("Invocation failed: " + e.getMessage())
                        .errorDetail(e.toString())
                        .build());
                }
            }
        }
        return results;
    }

    /**
     * Helper to create a successful test result.
     */
    protected TestResult success(String testName, String message, long startTime) {
        return TestResult.builder()
            .testName(testName)
            .category(getCategory().name())
            .passed(true)
            .message(message)
            .durationMs(System.currentTimeMillis() - startTime)
            .build();
    }

    /**
     * Helper to create a failed test result.
     */
    protected TestResult failure(String testName, String message, long startTime, Exception e) {
        return TestResult.builder()
            .testName(testName)
            .category(getCategory().name())
            .passed(false)
            .message(message)
            .durationMs(System.currentTimeMillis() - startTime)
            .errorDetail(e != null ? e.toString() : null)
            .build();
    }
}
