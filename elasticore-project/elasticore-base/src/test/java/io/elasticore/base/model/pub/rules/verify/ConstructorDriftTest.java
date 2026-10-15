package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The comparison behind the "constructor out of date" warning compares what a
 * line passes to the core service, not how the line is written - a user who
 * adds a class body or reformats must not be told their file is stale.
 */
public class ConstructorDriftTest {

    @Test
    public void javaSuperCallsCompareByArguments() {
        assertEquals("repository,query,entityManager",
                RulesGenRunner.superArguments("        super(repository, query, entityManager);"));
        assertEquals("helper", RulesGenRunner.superArguments("super( helper );"));
    }

    @Test
    public void kotlinSupertypeListsCompareByArguments() {
        String generated = ") : OrderCoreService(repository, query, entityManager), io.x.OrderUseCase";
        String extended = ") : OrderCoreService(repository, query, entityManager), io.x.OrderUseCase {";
        assertEquals(RulesGenRunner.superArguments(generated), RulesGenRunner.superArguments(extended));
    }

    @Test
    public void otherLinesAreNotConstructorCalls() {
        assertNull(RulesGenRunner.superArguments("return super.findAll();"));
        assertNull(RulesGenRunner.superArguments("private final OrderUseCase orderService;"));
    }
}
