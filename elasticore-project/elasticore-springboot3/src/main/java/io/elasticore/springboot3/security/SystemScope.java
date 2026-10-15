package io.elasticore.springboot3.security;

import java.util.function.Supplier;

/**
 * The thread-bound mark CurrentUser.runAsSystem sets. Explicit and scoped:
 * a thread is never the application itself by accident - not a WebFlux
 * handler, not a gRPC call, not an @Async task, none of which has a servlet
 * request to tell it apart from start-up code.
 */
final class SystemScope {

    private static final ThreadLocal<Integer> DEPTH = new ThreadLocal<Integer>();

    private SystemScope() {
    }

    static boolean active() {
        Integer d = DEPTH.get();
        return d != null && d > 0;
    }

    static <T> T run(Supplier<T> work) {
        Integer before = DEPTH.get();
        DEPTH.set(before == null ? 1 : before + 1);
        try {
            return work.get();
        } finally {
            if (before == null) DEPTH.remove();
            else DEPTH.set(before);
        }
    }
}
