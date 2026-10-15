package io.elasticore.springboot3.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A domain class whose JPA mapping is in META-INF/orm.xml (mapping: xml).
 *
 * It says nothing to JPA. It is there for Kotlin, whose classes are final:
 * the all-open and no-arg compiler plugins open a class by an annotation on
 * it, and with @Entity gone this is the one they can key on - otherwise
 * Hibernate cannot proxy the class and a lazy reference loads eagerly.
 *
 * <pre>
 * allOpen { annotation("io.elasticore.springboot3.entity.PersistentModel") }
 * noArg   { annotation("io.elasticore.springboot3.entity.PersistentModel") }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistentModel {
}
