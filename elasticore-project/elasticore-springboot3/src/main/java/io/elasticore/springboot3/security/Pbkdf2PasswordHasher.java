package io.elasticore.springboot3.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PBKDF2 with HMAC-SHA256, salted per password: {@code pbkdf2-sha256$iterations$salt$hash}.
 *
 * The JDK's own, so a project gets a sound default without a dependency.
 * The iteration count is stored with each hash, so raising it later leaves
 * the passwords already kept still matching.
 */
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String PREFIX = "pbkdf2-sha256";
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final int iterations;
    private final SecureRandom random = new SecureRandom();

    /** 310,000 iterations, OWASP's figure for this algorithm. */
    public Pbkdf2PasswordHasher() {
        this(310_000);
    }

    public Pbkdf2PasswordHasher(int iterations) {
        if (iterations < 1) throw new IllegalArgumentException("iterations must be positive");
        this.iterations = iterations;
    }

    @Override
    public String hash(String raw) {
        if (raw == null) throw new IllegalArgumentException("no password to hash");
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] key = derive(raw, salt, iterations);
        Base64.Encoder b64 = Base64.getEncoder().withoutPadding();
        return PREFIX + "$" + iterations + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(key);
    }

    @Override
    public boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) return false;
        try {
            int count = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derive(raw, salt, count));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String raw, byte[] salt, int count) {
        PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, count, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 is not available", e);
        } finally {
            spec.clearPassword();
        }
    }
}
