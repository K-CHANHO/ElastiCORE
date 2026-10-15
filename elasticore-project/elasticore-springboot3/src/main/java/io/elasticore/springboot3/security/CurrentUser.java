package io.elasticore.springboot3.security;

import java.util.function.Supplier;

/**
 * Who is asking, as the generated services see it.
 *
 * The default is {@link SessionCurrentUser}: the identity a sign-in put in
 * the HTTP session. An application on Spring Security registers its own bean
 * that answers from the security context instead; the generated services
 * take whichever bean there is.
 *
 * A caller nobody signed in is a visitor - in a request or not. The
 * application acting as itself - loading demo data at start-up, a nightly
 * job - says so, and only for as long as it says so:
 *
 * <pre>
 * CurrentUser.runAsSystem(() -> customers.save(admin));
 * </pre>
 */
public interface CurrentUser {

    /** Whether the caller is the application itself (inside {@link #runAsSystem}). */
    boolean system();

    /** The signed-in user's identity, or null for a visitor. */
    Object id();

    /** Whether the signed-in user holds this role. */
    boolean hasRole(String role);

    default boolean signedIn() {
        return id() != null;
    }

    /** Runs work as the application itself: every @access check passes, on this thread, until it returns. */
    static void runAsSystem(Runnable work) {
        SystemScope.run(() -> {
            work.run();
            return null;
        });
    }

    /** The same, for work that answers. */
    static <T> T callAsSystem(Supplier<T> work) {
        return SystemScope.run(work);
    }

    /** Whether this thread is inside runAsSystem - for a CurrentUser of the application's own. */
    static boolean inSystemScope() {
        return SystemScope.active();
    }
}
