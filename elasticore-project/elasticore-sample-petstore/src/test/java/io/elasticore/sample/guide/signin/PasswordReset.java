package io.elasticore.sample.guide.signin;

import io.elasticore.sample.petstore.application.dto.CustomerDTO;
import io.elasticore.sample.petstore.application.port.in.CustomerUseCase;
import io.elasticore.sample.petstore.application.port.out.CustomerQueryPort;
import io.elasticore.sample.petstore.domain.model.Customer;
import io.elasticore.sample.petstore.domain.repository.CustomerRepositoryPort;
import io.elasticore.springboot3.security.CurrentUser;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * A password reset by mail: an application service of the developer's, beside
 * the generated one. The rows are read through the generated ports, since the
 * password hash the token is bound to is in no DTO; the new password is written
 * through the use case, which hashes it as on every write.
 */
@Service
public class PasswordReset {

    private static final Duration ASK_AGAIN_AFTER = Duration.ofMinutes(1);

    private final CustomerQueryPort query;
    private final CustomerRepositoryPort repository;
    private final CustomerUseCase customers;
    private final PasswordResetTokens tokens;
    private final PasswordResetMail mail;
    private final Map<String, Instant> asked = new ConcurrentHashMap<String, Instant>();

    public PasswordReset(CustomerQueryPort query, CustomerRepositoryPort repository, CustomerUseCase customers,
            PasswordResetTokens tokens, PasswordResetMail mail) {
        this.query = query;
        this.repository = repository;
        this.customers = customers;
        this.tokens = tokens;
        this.mail = mail;
    }

    /** A mail to the address on the row, if there is such a login and it has one. */
    public void ask(String login) {
        if (login == null || !firstAskInAWhile(login)) return;
        Specification<Customer> byLogin = (r, q, cb) -> cb.equal(r.get("login"), login);
        for (Customer customer : query.findAll(byLogin)) {
            if (customer.getEmail() != null) {
                mail.send(customer.getEmail(), tokens.issue(customer.getCustomerId(), customer.getPassword()));
            }
        }
    }

    /** The customer with the new password, or empty when the token is not one of ours, has expired or was used. */
    public Optional<CustomerDTO> confirm(String token, String password) {
        Optional<Customer> customer = tokens.customerOf(token)
                .flatMap(repository::findById)
                .filter(found -> tokens.matches(token, found.getPassword()));
        if (!customer.isPresent()) return Optional.empty();

        CustomerDTO change = new CustomerDTO();
        change.setCustomerId(customer.get().getCustomerId());
        change.setPassword(password);
        // The customer is not signed in yet: the application writes, as itself.
        return Optional.of(CurrentUser.callAsSystem(() -> customers.update(change)));
    }

    /** Nothing is sent when the same login asked less than a minute ago. In memory, on this instance. */
    private boolean firstAskInAWhile(String login) {
        Instant now = Instant.now();
        Instant last = asked.put(login, now);
        return last == null || last.plus(ASK_AGAIN_AFTER).isBefore(now);
    }
}
