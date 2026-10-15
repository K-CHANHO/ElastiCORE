package io.elasticore.blueprint.test;

import lombok.*;

/**
 * Represents the result of a single test case execution.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    private String testName;
    private String category;
    private boolean passed;
    private String message;
    private long durationMs;
    private String errorDetail;
}
