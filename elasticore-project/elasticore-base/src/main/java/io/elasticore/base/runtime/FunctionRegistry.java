package io.elasticore.base.runtime;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry of named value transformers used by {@link EntityConverter}.
 * Built-in lenient functions are registered at class load; users can add more via
 * {@link #register(String, IngestFunction)}.
 *
 * Function names are case-insensitive.
 */
public final class FunctionRegistry {

    private static final ConcurrentMap<String, IngestFunction> REGISTRY = new ConcurrentHashMap<String, IngestFunction>();

    static {
        // Lenient numeric parser: strips commas, whitespace and common currency symbols.
        IngestFunction toNum = new IngestFunction() {
            public Object apply(Object value) {
                if (value == null) return null;
                if (value instanceof Number) return new BigDecimal(value.toString());
                String s = value.toString().trim();
                if (s.isEmpty()) return null;
                // Remove commas, spaces, and common currency symbols
                StringBuilder sb = new StringBuilder(s.length());
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c == ',' || c == ' ' || c == '\u00A0' || c == '\u20A9' /* won */
                            || c == '$' || c == '\u00A5' /* yen */ || c == '\u20AC' /* euro */) {
                        continue;
                    }
                    sb.append(c);
                }
                String cleaned = sb.toString();
                if (cleaned.isEmpty()) return null;
                try {
                    return new BigDecimal(cleaned);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
        };
        register("toNum", toNum);
        register("toNumber", toNum);

        // yyyyMMdd -> yyyy-MM-dd; leaves value untouched if not 8 digits after strip.
        register("ymd", new IngestFunction() {
            public Object apply(Object value) {
                if (value == null) return null;
                String raw = value.toString().trim();
                if (raw.isEmpty()) return null;
                StringBuilder digits = new StringBuilder(raw.length());
                for (int i = 0; i < raw.length(); i++) {
                    char c = raw.charAt(i);
                    if (c >= '0' && c <= '9') digits.append(c);
                }
                String d = digits.toString();
                if (d.length() == 8) {
                    return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8);
                }
                return value;
            }
        });

        register("trim", new IngestFunction() {
            public Object apply(Object value) {
                return value == null ? null : value.toString().trim();
            }
        });

        register("upper", new IngestFunction() {
            public Object apply(Object value) {
                return value == null ? null : value.toString().toUpperCase(Locale.ROOT);
            }
        });

        register("lower", new IngestFunction() {
            public Object apply(Object value) {
                return value == null ? null : value.toString().toLowerCase(Locale.ROOT);
            }
        });

        // "1"/"Y"/"y"/true -> "Y", otherwise "N". Null passes through.
        register("yn", new IngestFunction() {
            public Object apply(Object value) {
                if (value == null) return null;
                if (value instanceof Boolean) return ((Boolean) value).booleanValue() ? "Y" : "N";
                String s = value.toString().trim();
                if ("1".equals(s) || "Y".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s)) return "Y";
                return "N";
            }
        });
    }

    private FunctionRegistry() {
        // static registry
    }

    /**
     * Register or override a named function. Names are normalized to lowercase.
     */
    public static void register(String name, IngestFunction fn) {
        if (name == null || fn == null) {
            throw new IllegalArgumentException("name and fn must be non-null");
        }
        REGISTRY.put(name.toLowerCase(Locale.ROOT), fn);
    }

    /**
     * Remove a registered function. Returns true if it existed.
     */
    public static boolean unregister(String name) {
        if (name == null) return false;
        return REGISTRY.remove(name.toLowerCase(Locale.ROOT)) != null;
    }

    /**
     * Look up a function by name (case-insensitive). Returns null if absent.
     */
    public static IngestFunction get(String name) {
        if (name == null) return null;
        return REGISTRY.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Apply a named function. Throws IllegalArgumentException if the function is not registered.
     */
    public static Object apply(String name, Object value) {
        IngestFunction fn = get(name);
        if (fn == null) {
            throw new IllegalArgumentException("Unknown ingest function: " + name);
        }
        return fn.apply(value);
    }
}
