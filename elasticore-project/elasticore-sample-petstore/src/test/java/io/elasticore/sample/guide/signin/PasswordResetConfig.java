package io.elasticore.sample.guide.signin;

import java.time.Clock;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The token's secret comes from the configuration: 32 random bytes, Base64, kept out of the repository. */
@Configuration
public class PasswordResetConfig {

    @Bean
    PasswordResetTokens passwordResetTokens(@Value("${shop.password-reset.secret}") String secret) {
        return new PasswordResetTokens(Base64.getDecoder().decode(secret), Clock.systemUTC());
    }
}
