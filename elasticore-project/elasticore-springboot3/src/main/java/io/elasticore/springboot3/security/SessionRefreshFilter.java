package io.elasticore.springboot3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Brings a signed-in session up to date with its row, once per request.
 * With {@code @signin(refresh=request)}, the generator writes a subclass of
 * this filter for the entity that signs in.
 *
 * At sign-in, the session takes the row's key and role. Without this filter
 * it keeps them until the next sign-in, so a role the administrator changes
 * waits for that sign-in, and a deleted member keeps a working session. With
 * this filter, a request whose session holds a signed-in key first reads the
 * row again:
 *
 * <ul>
 *   <li>no row, or one kept under @deleted: the session ends, and the
 *       request goes on as a visitor's;</li>
 *   <li>a row whose role differs: the session takes the new role and keeps
 *       its id ({@link SessionCurrentUser#refresh});</li>
 *   <li>a row whose role is the same: nothing is written.</li>
 * </ul>
 *
 * A session with no signed-in key is left alone. That covers a visitor, and
 * also an application whose CurrentUser is its own (Spring Security, say),
 * since the generated sign-in is what puts the key in the session.
 */
public abstract class SessionRefreshFilter extends OncePerRequestFilter {

    /**
     * The roles of the row the session names, read again now, or empty when
     * that row is gone. The generated subclass answers it through the
     * service's me().
     */
    protected abstract Optional<? extends Collection<String>> signedInRoles();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionCurrentUser.ID) != null) {
            refresh(request, response);
        }
        chain.doFilter(request, response);
    }

    private void refresh(HttpServletRequest request, HttpServletResponse response) {
        // me() asks CurrentUser, and SessionCurrentUser reads the session
        // through RequestContextHolder. In an application, Spring Boot's
        // RequestContextFilter runs before this one and has already set it.
        // A filter chain without it, such as a MockMvc built by hand, gets it
        // here for the read alone.
        RequestAttributes outer = RequestContextHolder.getRequestAttributes();
        if (outer == null) RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        try {
            Optional<? extends Collection<String>> roles = signedInRoles();
            if (roles.isPresent()) {
                SessionCurrentUser.refresh(request, roles.get());
            } else {
                SessionCurrentUser.signOut(request);
            }
        } finally {
            if (outer == null) RequestContextHolder.resetRequestAttributes();
        }
    }

    /** The roles a row holds, in the form signIn takes them: a null role means none. */
    public static Set<String> roles(String... roles) {
        Set<String> held = new LinkedHashSet<String>();
        if (roles != null) {
            for (String role : roles) if (role != null) held.add(role);
        }
        return held;
    }
}
