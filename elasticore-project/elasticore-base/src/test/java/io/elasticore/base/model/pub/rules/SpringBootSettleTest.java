package io.elasticore.base.model.pub.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where no build property reaches - the annotation processor, a JavaExec of
 * one's own - a domain's env.yml says the Spring Boot generation. The build's
 * word wins, and a value taken from one project's env.yml does not survive
 * into the next run in the same JVM.
 */
public class SpringBootSettleTest {

    @AfterEach
    public void clear() {
        RulesConfig.settleSpringBoot(Collections.<String, String>emptyMap());
        System.clearProperty(RulesConfig.PROP_SPRING_BOOT);
    }

    private static Map<String, String> declares(String value) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("erp", value);
        return m;
    }

    @Test
    public void envYmlDeclaresItAndTheNextRunForgetsIt() {
        RulesConfig.settleSpringBoot(declares("4"));
        assertEquals(4, RulesConfig.springBootMajor());

        RulesConfig.settleSpringBoot(Collections.<String, String>emptyMap());
        assertEquals(3, RulesConfig.springBootMajor());
    }

    @Test
    public void theBuildWinsOverEnvYml() {
        System.setProperty(RulesConfig.PROP_SPRING_BOOT, "3.5.6");
        RulesConfig.settleSpringBoot(declares("4"));
        assertEquals(3, RulesConfig.springBootMajor());
    }

    @Test
    public void aValueThatIsNotAVersionGeneratesForBoot3() {
        RulesConfig.settleSpringBoot(declares("boot4"));
        assertEquals(3, RulesConfig.springBootMajor());
    }
}
