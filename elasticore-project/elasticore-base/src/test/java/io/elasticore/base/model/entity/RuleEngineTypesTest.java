package io.elasticore.base.model.entity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The value types only the 3.0 rule engine resolves. The 2.x generator
 * imports java.util.* and java.time.*, so a field typed UUID or Instant
 * already compiled there - and its output must not change on upgrade.
 */
public class RuleEngineTypesTest {

    @AfterEach
    void off() {
        BaseFieldType.ruleEngineTypes(false);
    }

    private static final String[] RULE_ENGINE_ONLY = {"Instant", "byte[]"};

    @Test
    public void theTwoXGeneratorResolvesThemAsItAlwaysDid() {
        for (String type : RULE_ENGINE_ONLY) {
            TypeInfo t = new TypeInfo(type);
            assertEquals(BaseFieldType.UNKNOWN, t.getBaseFieldType(), type);
            assertFalse(t.isBaseType(), type);
        }
        // timestamp was the bare word before - no compiler accepted it - and is an instant now in both.
        assertEquals("Instant", new TypeInfo("timestamp").getBaseTypeName());
        // decimal without a size was the bare word too; it is BigDecimal everywhere now.
        assertEquals(BaseFieldType.BIGDECIMAL, new TypeInfo("decimal").getBaseFieldType());
    }

    @Test
    public void theRuleEngineResolvesThemAsValueTypes() {
        BaseFieldType.ruleEngineTypes(true);
        assertEquals(BaseFieldType.INSTANT, new TypeInfo("timestamp").getBaseFieldType());
        assertEquals(BaseFieldType.INSTANT, new TypeInfo("Instant").getBaseFieldType());
        assertEquals(BaseFieldType.BYTES, new TypeInfo("byte[]").getBaseFieldType());
        for (String type : RULE_ENGINE_ONLY) assertTrue(new TypeInfo(type).isBaseType(), type);
        // Not types of their own: int, string(1), timestamp and string @id @genid say the same.
        for (String type : new String[]{"short", "byte", "char", "OffsetDateTime", "ZonedDateTime", "uuid", "UUID"}) {
            assertFalse(new TypeInfo(type).isBaseType(), type);
        }
        assertEquals("java.time.Instant", new TypeInfo("timestamp").getDefaultTypeName());
    }
}
