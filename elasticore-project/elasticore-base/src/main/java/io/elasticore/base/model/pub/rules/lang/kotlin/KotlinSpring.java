package io.elasticore.base.model.pub.rules.lang.kotlin;

import io.elasticore.base.model.pub.rules.RulesConfig;

/**
 * What Kotlin code written against Spring Boot 4 has to say that Spring Boot
 * 3 code does not. Spring Framework 7 and Spring Data 4 declare their API
 * with JSpecify, which Kotlin reads as types: a Specification's entity type
 * is non-null, and Page.map wants a function returning a value. Empty for
 * Spring Boot 3, so its output stays as it was.
 */
public final class KotlinSpring {

    private KotlinSpring() {
    }

    /** The bound a type parameter used as a Specification's type needs. */
    public static String specBound() {
        return RulesConfig.springBootMajor() >= 4 ? " : Any" : "";
    }

    /** What a mapper call that returns a nullable DTO needs inside Page.map. */
    public static String pageNonNull() {
        return RulesConfig.springBootMajor() >= 4 ? "!!" : "";
    }
}
