package io.elasticore.springboot3.security;

/**
 * How a {@code @password} field is kept: hashed when a request writes it,
 * matched when someone signs in.
 *
 * The default is {@link Pbkdf2PasswordHasher}, from the JDK alone. An
 * application with Spring Security registers a bean wrapping its own
 * PasswordEncoder; the generated services take whichever bean there is.
 */
public interface PasswordHasher {

    String hash(String raw);

    boolean matches(String raw, String stored);

    static PasswordHasher standard() {
        return new Pbkdf2PasswordHasher();
    }
}
