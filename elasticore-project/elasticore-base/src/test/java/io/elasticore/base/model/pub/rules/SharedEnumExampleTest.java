package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A field typed with another domain's enum documents itself like one of its own domain's. */
public class SharedEnumExampleTest {

    private static EnumSpec indicator(String label) {
        return new EnumSpec("Indicator").constant("YES", "Y", label).constant("NO", "N", "NO")
                .pkg("enumeration", "com.acme.common.enums");
    }

    @Test
    public void aSharedEnumGivesTheFieldItsExample() {
        EntitySpec menu = new EntitySpec("MenuDTO").domain("account", "Account")
                .pkg("dto", "com.acme.account.dto")
                .field("isFavoritable", "Indicator");
        menu.documentEnum(indicator("YES"));
        String out = RuleCatalog.standard().engineFor("jpa-springboot")
                .templateSet(SourceCode.TemplateSet.COMPATIBLE).run(menu, "dto").render();
        assertTrue(out.contains("example=\"YES: Y YES | NO: N NO\""), out);
    }

    @Test
    public void theDomainsOwnEnumWinsASharedName() {
        EntitySpec menu = new EntitySpec("MenuDTO").domain("account", "Account");
        menu.documentEnum(indicator("mine"));
        menu.documentEnum(indicator("theirs"));
        assertEquals("mine", menu.enumSpec("Indicator").constants().get(0).values().get(1));
        assertFalse(menu.isKnownEnum("Indicator"), "documenting an enum does not make it the domain's own");
    }
}
