package io.elasticore.sample.guide.signin;

/**
 * Sends the reset mail: an outbound port. The adapter is the application's -
 * it writes the message, puts the token in a link and sends it (JavaMailSender,
 * a mail API).
 */
public interface PasswordResetMail {

    void send(String to, String token);
}
