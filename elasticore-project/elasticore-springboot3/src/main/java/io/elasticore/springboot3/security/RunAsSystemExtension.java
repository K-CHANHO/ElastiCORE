package io.elasticore.springboot3.security;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

/**
 * Runs each test method - and its @BeforeEach and @AfterEach - as the
 * application itself (CurrentUser.runAsSystem).
 *
 * For tests of what a service does rather than of who may ask it: the
 * generated service tests of an entity with @access carry it, since nobody
 * signs in to a unit test. A test of the access rules themselves leaves it
 * out and signs in as the users it is about.
 */
public class RunAsSystemExtension implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        asSystem(invocation);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        asSystem(invocation);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        asSystem(invocation);
    }

    private static void asSystem(Invocation<Void> invocation) throws Throwable {
        final Throwable[] failure = new Throwable[1];
        CurrentUser.runAsSystem(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                failure[0] = t;
            }
        });
        if (failure[0] != null) throw failure[0];
    }
}
