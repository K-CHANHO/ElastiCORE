package io.elasticore.springboot3.security;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** @signin(refresh=request): what the filter the generator subclasses does to a session. */
public class SessionRefreshFilterTest {

    @AfterEach
    void unbind() {
        RequestContextHolder.resetRequestAttributes();
    }

    /** A filter whose row is whatever the test says, counting how often it was read. */
    private static final class Row extends SessionRefreshFilter {
        final AtomicInteger reads = new AtomicInteger();
        final Supplier<Optional<? extends Collection<String>>> row;
        Object idSeen;

        Row(Supplier<Optional<? extends Collection<String>>> row) {
            this.row = row;
        }

        @Override
        protected Optional<? extends Collection<String>> signedInRoles() {
            reads.incrementAndGet();
            // What the generated subclass reads through me(): the session's key, by CurrentUser.
            idSeen = new SessionCurrentUser().id();
            return row.get();
        }
    }

    private static MockHttpServletRequest signedIn(Object id, String... roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionCurrentUser.signIn(request, id, roles);
        return request;
    }

    private static MockFilterChain run(SessionRefreshFilter filter, MockHttpServletRequest request) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), "the request goes on, whatever the row said");
        return chain;
    }

    @Test
    void aRaisedRoleIsTheSessionsAndTheSessionIdStays() throws Exception {
        MockHttpServletRequest request = signedIn(7L, "USER");
        HttpSession session = request.getSession(false);
        String id = session.getId();
        Row filter = new Row(() -> Optional.of(Collections.singleton("ADMIN")));

        run(filter, request);

        assertSame(session, request.getSession(false), "the same session, not a new one");
        assertEquals(id, request.getSession(false).getId());
        assertEquals(
                new LinkedHashSet<String>(Collections.singleton("ADMIN")),
                request.getSession(false).getAttribute(SessionCurrentUser.ROLES));
        assertEquals(7L, request.getSession(false).getAttribute(SessionCurrentUser.ID));
        assertEquals(7L, filter.idSeen, "the row is read by the session's key");
    }

    @Test
    void aRoleTakenAwayIsGoneAndANullRoleIsNone() throws Exception {
        MockHttpServletRequest request = signedIn(7L, "ADMIN");
        run(new Row(() -> Optional.of(SessionRefreshFilter.roles((String) null))), request);
        assertEquals(Collections.emptySet(), request.getSession(false).getAttribute(SessionCurrentUser.ROLES));
    }

    @Test
    void theSameRoleWritesNothing() throws Exception {
        MockHttpServletRequest request = signedIn(7L, "USER");
        Object held = request.getSession(false).getAttribute(SessionCurrentUser.ROLES);
        run(new Row(() -> Optional.of(Arrays.asList("USER"))), request);
        assertSame(
                held,
                request.getSession(false).getAttribute(SessionCurrentUser.ROLES),
                "a session store that saves on change is not written for nothing");
    }

    @Test
    void aRowThatIsGoneEndsTheSessionAndTheRequestIsAVisitors() throws Exception {
        MockHttpServletRequest request = signedIn(7L, "USER");
        HttpSession session = request.getSession(false);
        Row filter = new Row(Optional::empty);
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                RequestContextHolder.setRequestAttributes(new ServletRequestAttributes((MockHttpServletRequest) req));
                assertNull(new SessionCurrentUser().id(), "the rest of the request sees a visitor");
                assertFalse(new SessionCurrentUser().hasRole("USER"));
            }
        };

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(request.getSession(false));
        assertTrue(((org.springframework.mock.web.MockHttpSession) session).isInvalid());
    }

    @Test
    void aSessionNobodySignedInToIsNotRead() throws Exception {
        Row filter = new Row(() -> Optional.of(Collections.singleton("ADMIN")));

        MockHttpServletRequest noSession = new MockHttpServletRequest();
        run(filter, noSession);
        assertNull(noSession.getSession(false), "no session is made");

        // A session of the application's own (Spring Security keeps its context in one): no signed-in key.
        MockHttpServletRequest other = new MockHttpServletRequest();
        other.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", "theirs");
        run(filter, other);
        assertNull(other.getSession(false).getAttribute(SessionCurrentUser.ROLES));
        assertEquals("theirs", other.getSession(false).getAttribute("SPRING_SECURITY_CONTEXT"));

        assertEquals(0, filter.reads.get());
    }

    @Test
    void theRequestContextIsLentForTheReadAloneWhenNobodySetIt() throws Exception {
        MockHttpServletRequest request = signedIn(7L, "USER");
        Row filter = new Row(() -> Optional.of(Collections.singleton("USER")));

        assertNull(RequestContextHolder.getRequestAttributes());
        run(filter, request);
        assertEquals(7L, filter.idSeen);
        assertNull(RequestContextHolder.getRequestAttributes(), "given back as it was");

        // Spring Boot's RequestContextFilter set it first: used, and left in place.
        RequestAttributes outer = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(outer);
        run(filter, request);
        assertSame(outer, RequestContextHolder.getRequestAttributes());
        assertEquals(2, filter.reads.get());
    }

    @Test
    void refreshKeepsASessionNobodySignedInToAsItIs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionCurrentUser.refresh(request, Collections.singleton("ADMIN"));
        assertNull(request.getSession(false));
        request.getSession(true);
        SessionCurrentUser.refresh(request, Collections.singleton("ADMIN"));
        assertNull(request.getSession(false).getAttribute(SessionCurrentUser.ROLES), "no role without a signed-in key");
    }

    @Test
    void rolesSkipNull() {
        Set<String> roles = SessionRefreshFilter.roles("USER", null);
        assertEquals(Collections.singleton("USER"), roles);
        assertEquals(Collections.emptySet(), SessionRefreshFilter.roles());
    }
}
