package io.elasticore.springboot3.validation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bean Validation for the calls the controller cannot validate.
 *
 * A generated PUT carries only what changed: the mapper merges non-null
 * fields onto the stored row. Putting @Valid on it would reject every such
 * request for the @NotNull fields it left out - which is why the update
 * endpoint had none, and why a 60-character value for a 50-character field
 * went straight through to the database and came back as a 500.
 *
 * {@link #validatePresent} is the check that fits a partial update: every
 * constraint on every property the request did send, and nothing about the
 * ones it did not. A skip-null merge can never null a field, so a required
 * field that is absent from the request is still present on the row.
 *
 * Where the application has no Bean Validation provider on its classpath,
 * both methods do nothing - the same as a controller's @Valid would.
 */
public final class BeanValidation {

    private BeanValidation() {
    }

    private static final boolean AVAILABLE = present("jakarta.validation.Validation");

    private static boolean present(String className) {
        try {
            Class.forName(className, false, BeanValidation.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Every constraint on the object, as @Valid would check it. */
    public static void validate(Object target) {
        if (target == null || !AVAILABLE) return;
        Checks.all(target);
    }

    /**
     * The validator Spring configured, when the application has one - so
     * constraint validators with injected dependencies, and the application's
     * messages, are the ones used. Set by the application (for example from
     * a @Bean method or a SmartInitializingSingleton); the default factory
     * is used until then.
     */
    public static void use(Object springValidator) {
        if (AVAILABLE) Checks.use(springValidator);
    }

    /**
     * The constraints of the properties that carry a value - a partial
     * update's rules.
     *
     * A value that is itself a DTO, or a collection of them - the members an
     * aggregate is sent with - is checked too: a collection's elements whole,
     * since they replace what was there, and a single member as a partial
     * update of its own, since it is merged into the member in place.
     */
    public static void validatePresent(Object target) {
        if (target == null || !AVAILABLE) return;
        validatePresent(target, 0);
    }

    private static void validatePresent(Object target, int depth) {
        List<String> present = new ArrayList<String>();
        List<Object> nested = new ArrayList<Object>();
        List<Object> nestedWhole = new ArrayList<Object>();
        for (Class<?> type = target.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(target);
                    if (value == null) continue;
                    present.add(field.getName());
                    if (value instanceof Iterable) {
                        for (Object each : (Iterable<?>) value) {
                            if (isModel(each)) nestedWhole.add(each);
                        }
                    } else if (isModel(value)) {
                        nested.add(value);
                    }
                } catch (Exception ignored) {
                    // A field reflection cannot read is not one this can check.
                }
            }
        }
        Checks.properties(target, present);
        if (depth >= 4) return;
        for (Object each : nestedWhole) Checks.all(each);
        for (Object each : nested) validatePresent(each, depth + 1);
    }

    /** An application object - not a JDK value, an enum or an array. */
    private static boolean isModel(Object value) {
        if (value == null || value.getClass().isEnum() || value.getClass().isArray()) return false;
        String name = value.getClass().getName();
        return !(name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jakarta.")
                || name.startsWith("kotlin.") || name.startsWith("org.springframework."));
    }

    /**
     * Everything that names a jakarta.validation type, kept out of the outer
     * class so that loading BeanValidation never requires the API.
     */
    private static final class Checks {

        private static volatile jakarta.validation.Validator validator;
        private static volatile boolean unavailable;

        static void use(Object springValidator) {
            if (springValidator instanceof jakarta.validation.Validator) {
                validator = (jakarta.validation.Validator) springValidator;
                unavailable = false;
            }
        }

        private static jakarta.validation.Validator validator() {
            if (validator == null && !unavailable) {
                synchronized (Checks.class) {
                    if (validator == null && !unavailable) {
                        try {
                            validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
                        } catch (RuntimeException noProvider) {
                            // The API without an implementation: nothing to check with.
                            unavailable = true;
                        }
                    }
                }
            }
            return validator;
        }

        static void all(Object target) {
            jakarta.validation.Validator v = validator();
            if (v == null) return;
            raise(v.validate(target));
        }

        static void properties(Object target, List<String> names) {
            jakarta.validation.Validator v = validator();
            if (v == null) return;
            Set<jakarta.validation.ConstraintViolation<Object>> found =
                    new LinkedHashSet<jakarta.validation.ConstraintViolation<Object>>();
            for (String name : names) {
                try {
                    found.addAll(v.validateProperty(target, name));
                } catch (IllegalArgumentException notAProperty) {
                    // A field with no bean property behind it carries no constraint to check.
                }
            }
            raise(found);
        }

        private static void raise(Set<? extends jakarta.validation.ConstraintViolation<?>> violations) {
            if (violations == null || violations.isEmpty()) return;
            throw new jakarta.validation.ConstraintViolationException(
                    new LinkedHashSet<jakarta.validation.ConstraintViolation<?>>(violations));
        }
    }
}
