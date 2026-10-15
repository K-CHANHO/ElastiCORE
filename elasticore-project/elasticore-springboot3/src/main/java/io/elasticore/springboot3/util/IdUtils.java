package io.elasticore.springboot3.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Random;

/**
 * Utility class for generating unique identifiers with custom formatting.
 * <p>
 * The generated ID includes a prefix derived from the caller class name
 * and a random alphanumeric string using {@link NanoIdUtils}.
 * </p>
 */
public class IdUtils {

    /** Alphabet used for random ID generation (A-Z, a-z, 0-9). */
    private static final char[] alphabet = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    /**
     * Generates a new unique ID consisting of:
     * <ul>
     *   <li>A prefix based on the caller's class name</li>
     *   <li>A random 9-character alphanumeric string</li>
     * </ul>
     *
     * @return the generated unique ID
     */
    public static String newId() {
        Random random = new Random();
        return getCallerClassName() + NanoIdUtils.randomNanoId(random, alphabet, 9);
    }

    /**
     * Generates a new unique ID consisting of:
     * <ul>
     *   <li>A prefix based on the caller's class name</li>
     *   <li>A random 9-character alphanumeric string</li>
     * </ul>
     * @param size the field max size
     * @return the generated unique ID
     */
    public static String newId(int size) {
        Random random = new Random();
        return getCallerClassName() + NanoIdUtils.randomNanoId(random, alphabet, size-3);
    }

    /**
     * Extracts the caller class name from the stack trace
     * and transforms it into a short 3-character prefix.
     *
     * @return the caller class name prefix
     */
    private static String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return extractClassName(stackTrace[3].getClassName());
    }

    /**
     * Extracts and formats the class name into a short 3-character prefix.
     * <p>
     * Format:
     * <ul>
     *   <li>First letter after the last dot (.)</li>
     *   <li>Middle letter between the last dot and end</li>
     *   <li>Last letter of the full class name</li>
     * </ul>
     * </p>
     *
     * Example: {@code io.elasticore.UserInfo}  -- {@code USo}
     *
     * @param className the full class name
     * @return the formatted 3-character prefix
     */
    private static String extractClassName(String className) {
        if (className == null || className.length() < 3) {
            return className;
        }

        int lastDotIndex = className.lastIndexOf('.');
        char firstChar = className.charAt(lastDotIndex + 1);
        int middleIndex = (className.length() - lastDotIndex) / 2;
        char middleChar = className.charAt(middleIndex);
        char lastChar = className.charAt(className.length() - 1);

        return ("" + firstChar + middleChar).toUpperCase() + lastChar;
    }
}
