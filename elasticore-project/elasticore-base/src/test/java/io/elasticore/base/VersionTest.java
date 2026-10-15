package io.elasticore.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Version Tests")
class VersionTest {

    @Test
    @DisplayName("Should have major version information")
    void testMajorVersionInfo() {
        // When
        String majorVersion = Version.MAJOR;

        // Then
        assertNotNull(majorVersion, "Major version should not be null");
        assertFalse(majorVersion.isEmpty(), "Major version should not be empty");
        // Major version is derived from the build version (e.g. "2.2.260617" -> "2.2").
        // Assert the format rather than a hardcoded value so this test survives version bumps.
        assertTrue(majorVersion.matches("\\d+\\.\\d+"),
                "Major version should match the '<major>.<minor>' format but was: " + majorVersion);
    }
}