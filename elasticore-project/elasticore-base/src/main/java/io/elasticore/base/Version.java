package io.elasticore.base;

import java.io.InputStream;
import java.util.Properties;

/**
 * Provides ElastiCORE framework version information.
 * Version is loaded from elasticore-version.properties (injected by Gradle at build time).
 */
public class Version {

    public static final String MAJOR;
    private static final String VERSION;

    static {
        String v = "unknown";
        try (InputStream is = Version.class.getClassLoader()
                .getResourceAsStream("elasticore-version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                v = props.getProperty("version", "unknown");
            }
        } catch (Exception e) {
            // Fallback to unknown
        }
        VERSION = v;
        // Extract major.minor from version (e.g., "2.0" from "2.0.260213-SNAPSHOT")
        int secondDot = v.indexOf('.', v.indexOf('.') + 1);
        MAJOR = secondDot > 0 ? v.substring(0, secondDot) : v;
    }

    /**
     * Returns the full version string (e.g., "2.0.260213-SNAPSHOT").
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Returns a one-line banner string for logging.
     */
    public static String getBanner() {
        return "ElastiCORE Framework  v" + VERSION;
    }
}
