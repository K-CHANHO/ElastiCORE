package io.elasticore.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ElastiCore Processor Simple Tests")
class ElastiCoreProcessorSimpleTest {

    private ElastiCoreProcessor processor;
    private ProcessingEnvironment mockProcessingEnv;
    private Map<String, String> processingOptions;

    @BeforeEach
    void setUp() {
        processor = new ElastiCoreProcessor();
        mockProcessingEnv = mock(ProcessingEnvironment.class);
        processingOptions = new HashMap<>();
        processingOptions.put("projectPath", "/test/project");
        processingOptions.put("generatedPath", "src/main/generated");
        
        when(mockProcessingEnv.getOptions()).thenReturn(processingOptions);
    }

    @Test
    @DisplayName("Should create ElastiCoreProcessor")
    void testCreateProcessor() {
        // When
        ElastiCoreProcessor proc = new ElastiCoreProcessor();

        // Then
        assertNotNull(proc);
    }

    @Test
    @DisplayName("Should initialize with processing environment")
    void testInitWithProcessingEnvironment() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            processor.init(mockProcessingEnv);
        });

        // Verify that options were accessed
        verify(mockProcessingEnv).getOptions();
    }

    @Test
    @DisplayName("Should handle empty processing options")
    void testInitWithEmptyOptions() {
        // Given
        when(mockProcessingEnv.getOptions()).thenReturn(new HashMap<>());

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            processor.init(mockProcessingEnv);
        });
    }

    @Test
    @DisplayName("Should handle null processing environment gracefully")
    void testInitWithNullEnvironment() {
        // When & Then - may throw exception but should be handled
        assertThrows(Exception.class, () -> {
            processor.init(null);
        });
    }
}