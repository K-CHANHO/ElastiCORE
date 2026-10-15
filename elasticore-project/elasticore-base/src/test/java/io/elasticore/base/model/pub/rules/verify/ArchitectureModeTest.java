package io.elasticore.base.model.pub.rules.verify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A domain that names a rule existing only as a v3 reading is generated in
 * v3 mode without a separate switch - found by a project that declared
 * hexagonal and got 26 "template resource not found" failures.
 */
public class ArchitectureModeTest {

    @Test
    public void v3OnlyRuleNamesAskForV3() {
        assertTrue(RulesGenRunner.needsV3("hexagonal-springboot"));
        assertTrue(RulesGenRunner.needsV3("ddd-springboot"));
        assertTrue(RulesGenRunner.needsV3("ddd-aggregate"));
        assertTrue(RulesGenRunner.needsV3("jpa-springboot-v3"));
        assertTrue(RulesGenRunner.needsV3(" jpa-write-guard "));
        assertTrue(RulesGenRunner.needsV3("spring-api-errors"));
    }

    @Test
    public void compatRuleNamesLeaveTheModeAlone() {
        assertFalse(RulesGenRunner.needsV3("jpa-springboot"));
        assertFalse(RulesGenRunner.needsV3("jpa-springboot-kt"));
        assertFalse(RulesGenRunner.needsV3("ossie-export"));
        assertFalse(RulesGenRunner.needsV3(null));
        assertFalse(RulesGenRunner.needsV3(""));
    }

    @Test
    public void noModelsAskForNothing() {
        assertTrue(RulesGenRunner.domainAskingForV3(null) == null);
        assertTrue(RulesGenRunner.domainAskingForV3(new java.util.ArrayList<>()) == null);
    }
}
