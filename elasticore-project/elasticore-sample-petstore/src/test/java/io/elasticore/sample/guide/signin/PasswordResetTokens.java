package io.elasticore.sample.guide.signin;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A reset token that needs no table: the customer's key, an expiry, and an
 * HMAC over both and the password hash the row holds now. A new password
 * changes the hash, so the token stops working once it has been used.
 */
public class PasswordResetTokens {

    private static final Duration VALID = Duration.ofMinutes(30);

    private final byte[] secret;
    private final Clock clock;

    public PasswordResetTokens(byte[] secret, Clock clock) {
        this.secret = secret.clone();
        this.clock = clock;
    }

    public String issue(Long customerId, String passwordHash) {
        // The entity's hash: a DTO never carries it, and a token bound to null would work forever.
        if (passwordHash == null) throw new IllegalArgumentException("no password hash to bind the token to");
        String payload = customerId + "." + clock.instant().plus(VALID).getEpochSecond();
        return payload + "." + mac(payload, passwordHash);
    }

    /** The customer a well-formed, unexpired token names; matches() then checks it against the row. */
    public Optional<Long> customerOf(String token) {
        String[] part = token == null ? new String[0] : token.split("\\.");
        if (part.length != 3) return Optional.empty();
        try {
            if (clock.instant().getEpochSecond() > Long.parseLong(part[1])) return Optional.empty();
            return Optional.of(Long.valueOf(part[0]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public boolean matches(String token, String passwordHash) {
        if (passwordHash == null) return false;
        int cut = token.lastIndexOf('.');
        byte[] expected = mac(token.substring(0, cut), passwordHash).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, token.substring(cut + 1).getBytes(StandardCharsets.US_ASCII));
    }

    private String mac(String payload, String passwordHash) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            mac.update((payload + "\n" + passwordHash).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
