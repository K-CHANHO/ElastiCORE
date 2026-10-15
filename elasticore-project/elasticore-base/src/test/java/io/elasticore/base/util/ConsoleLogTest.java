package io.elasticore.base.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConsoleLog Utility Tests")
class ConsoleLogTest {

    @BeforeEach
    void setUp() {
        ConsoleLog.clear();
    }

    @Test
    @DisplayName("Should store and retrieve logs")
    void testStoreAndRetrieveLogs() {
        // Given
        String key = "test-key";
        String message = "test message";

        // When
        ConsoleLog.storeLog(key, message);

        // Then
        assertTrue(ConsoleLog.hasStoredLogkey(key));
        assertEquals(1, ConsoleLog.countStoredLogkey(key));
    }

    @Test
    @DisplayName("Should handle multiple log entries")
    void testMultipleLogEntries() {
        // Given
        String key = "multi-test";
        String message1 = "message 1";
        String message2 = "message 2";

        // When
        ConsoleLog.storeLog(key, message1);
        ConsoleLog.storeLog(key, message2);

        // Then
        assertTrue(ConsoleLog.hasStoredLogkey(key));
        assertEquals(2, ConsoleLog.countStoredLogkey(key));
    }

    @Test
    @DisplayName("Should clear all stored logs")
    void testClearStoredLogs() {
        // Given
        ConsoleLog.storeLog("key1", "message1");
        ConsoleLog.storeLog("key2", "message2");

        // When
        ConsoleLog.clear();

        // Then
        assertFalse(ConsoleLog.hasStoredLogkey("key1"));
        assertFalse(ConsoleLog.hasStoredLogkey("key2"));
        assertEquals(0, ConsoleLog.countStoredLogkey("key1"));
        assertEquals(0, ConsoleLog.countStoredLogkey("key2"));
    }

    @Test
    @DisplayName("Should handle non-existent keys")
    void testNonExistentKeys() {
        // When & Then
        assertFalse(ConsoleLog.hasStoredLogkey("non-existent"));
        assertEquals(0, ConsoleLog.countStoredLogkey("non-existent"));
    }

    @Test
    @DisplayName("Should handle print operations without error")
    void testPrintOperations() {
        // When & Then - should not throw exceptions
        assertDoesNotThrow(() -> {
            ConsoleLog.print("test message");
            ConsoleLog.printInfo("info message");
            ConsoleLog.printWarn("warning message");
        });
    }

    @Test
    @DisplayName("Should handle stored log printing")
    void testStoredLogPrinting() {
        // Given
        String key = "print-test";
        ConsoleLog.storeLog(key, "message to print");

        // When & Then - should not throw exceptions
        assertDoesNotThrow(() -> {
            ConsoleLog.printStoredLog(key, "  ");
            ConsoleLog.printStoredInfoLog(key, "  ");
            ConsoleLog.printStoredWarnLog(key, "  ");
        });
    }
}