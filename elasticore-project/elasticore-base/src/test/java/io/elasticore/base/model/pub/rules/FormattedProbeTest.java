package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** The mistakes this check exists to catch, and the calls it must leave alone. */
public class FormattedProbeTest {

    /** The nested-format slip: a format built from another format. */
    @Test
    public void surplusArgument_failsWhereTheMistakeIs() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> AnnoCode.of("jakarta.validation.constraints.%s", "Decimal%s", "Min"));
        assertTrue(e.getMessage().contains("1 argument(s) but 2"), e.getMessage());
    }

    /** A slot left unfilled would otherwise reach the source as a literal %s. */
    @Test
    public void missingArgument_failsToo() {
        assertThrows(IllegalArgumentException.class,
                () -> AnnoCode.raw("x.Y", "@Y(name = %s, value = %s)", "onlyOne"));
    }

    /** Emitted code carries per-cent signs of its own; no arguments means verbatim. */
    @Test
    public void noArguments_takesTheTextAsItStands() {
        assertEquals("@Pattern(regexp=\"%d%%\")",
                AnnoCode.raw("x.Y", "@Pattern(regexp=\"%d%%\")").render());
    }

    /** A literal per-cent consumes nothing, so this one-slot call is correct. */
    @Test
    public void escapedPercent_isNotASlot() {
        assertEquals("@Y(100%)", AnnoCode.raw("x.Y", "@Y(%s%%)", 100).render());
    }
}
