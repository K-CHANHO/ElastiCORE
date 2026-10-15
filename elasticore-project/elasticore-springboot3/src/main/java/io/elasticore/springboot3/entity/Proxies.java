package io.elasticore.springboot3.entity;

import java.lang.reflect.Method;

/**
 * The object behind a lazy proxy, for generated domain code that must not
 * name the ORM.
 *
 * A member's removal check reads its root, which may still be a proxy whose
 * own fields are empty - Kotlin's final property getters are not intercepted.
 * Hibernate unwraps it; this reaches Hibernate without the domain class
 * importing it, so a domain whose mapping is in orm.xml names no persistence
 * technology at all. Without Hibernate on the classpath the object is
 * returned as it is.
 */
public final class Proxies {

    private static final Method UNPROXY = find();

    private Proxies() {
    }

    private static Method find() {
        try {
            return Class.forName("org.hibernate.Hibernate").getMethod("unproxy", Object.class, Class.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /** The real object, as the given type. */
    public static <T> T real(Object candidate, Class<T> type) {
        if (candidate == null) return null;
        if (UNPROXY == null) return type.cast(candidate);
        try {
            return type.cast(UNPROXY.invoke(null, candidate, type));
        } catch (ReflectiveOperationException e) {
            return type.cast(candidate);
        }
    }
}
