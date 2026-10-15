package io.elasticore.springboot3.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The user a sign-in stored in the HTTP session - the default
 * {@link CurrentUser}, for an application without Spring Security.
 *
 * <pre>
 * SessionCurrentUser.signIn(request, customer.getCustomerId(), customer.getRole().name());
 * SessionCurrentUser.signOut(request);
 * </pre>
 *
 * Outside a request nobody is signed in: the caller is a visitor, unless the
 * application runs the work with CurrentUser.runAsSystem.
 */
public class SessionCurrentUser implements CurrentUser {

    public static final String ID = "elasticore.user.id";
    public static final String ROLES = "elasticore.user.roles";

    /**
     * Only inside CurrentUser.runAsSystem. No request is not the same as the
     * application itself: a WebFlux handler, a gRPC call or an @Async task
     * has no servlet request either, and is a visitor here.
     */
    @Override
    public boolean system() {
        return CurrentUser.inSystemScope();
    }

    @Override
    public Object id() {
        RequestAttributes request = RequestContextHolder.getRequestAttributes();
        return request == null ? null : request.getAttribute(ID, RequestAttributes.SCOPE_SESSION);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasRole(String role) {
        RequestAttributes request = RequestContextHolder.getRequestAttributes();
        if (request == null || role == null) return false;
        Object roles = request.getAttribute(ROLES, RequestAttributes.SCOPE_SESSION);
        return roles instanceof Collection && ((Collection<String>) roles).contains(role);
    }

    /**
     * Signs in: a new session - so an id fixed before sign-in is worthless
     * after it - holding the identity and the roles.
     */
    public static void signIn(HttpServletRequest request, Object id, String... roles) {
        signIn(request, id, roles == null ? Collections.<String>emptyList() : Arrays.asList(roles));
    }

    public static void signIn(HttpServletRequest request, Object id, Collection<String> roles) {
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute(ID, id);
        Set<String> held = new LinkedHashSet<String>();
        if (roles != null) {
            for (String role : roles) if (role != null) held.add(role);
        }
        session.setAttribute(ROLES, held);
    }

    public static void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }
}
