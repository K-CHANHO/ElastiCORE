package io.elasticore.base.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Directory listings in one order on every platform.
 *
 * File.listFiles() returns entries in NTFS's order on Windows (names compared
 * case-insensitively) and in the file system's own order elsewhere. Without
 * sorting, Linux and macOS read domains and model files in a different order
 * than Windows, and so do the models, messages and generated aggregates that
 * follow that order. Every listing that decides an order goes through here;
 * the order is the one Windows already uses, so output made there is unchanged.
 */
public final class FileOrder {

    /** By name: each character upper-cased like NTFS, then the exact name. */
    public static final Comparator<File> BY_NAME = new Comparator<File>() {
        @Override
        public int compare(File a, File b) {
            return compareNames(a.getName(), b.getName());
        }
    };

    private FileOrder() {
    }

    /** The directory's entries sorted by name, or null when it cannot be listed. */
    public static File[] list(File dir) {
        return sorted(dir.listFiles());
    }

    /** Sorts the array by name in place and returns it; null stays null. */
    public static File[] sorted(File[] files) {
        if (files != null) {
            Arrays.sort(files, BY_NAME);
        }
        return files;
    }

    static int compareNames(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            char x = Character.toUpperCase(a.charAt(i));
            char y = Character.toUpperCase(b.charAt(i));
            if (x != y) {
                return x - y;
            }
        }
        if (a.length() != b.length()) {
            return a.length() - b.length();
        }
        return a.compareTo(b);
    }
}
