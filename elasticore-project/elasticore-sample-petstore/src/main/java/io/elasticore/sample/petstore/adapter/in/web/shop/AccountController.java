package io.elasticore.sample.petstore.adapter.in.web.shop;

import io.elasticore.sample.petstore.application.dto.CustomerDTO;
import io.elasticore.sample.petstore.application.port.in.CustomerUseCase;
import io.elasticore.springboot3.security.CurrentUser;
import io.elasticore.springboot3.security.SessionCurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Signing up, in and out. Who may do what afterwards is the DSL's
 * (@access, @owner) and is enforced by the generated services; this only
 * starts and ends the session they read.
 */
@RestController
@RequestMapping("/api/petstore/account")
public class AccountController {

    private final CustomerUseCase customers;
    private final CurrentUser currentUser = new SessionCurrentUser();

    public AccountController(CustomerUseCase customers) {
        this.customers = customers;
    }

    public record Credentials(String login, String password) {
    }

    /** A new customer, signed in. The service keeps the role out of a visitor's hands. */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO signUp(@Valid @RequestBody CustomerDTO customer, HttpServletRequest request) {
        customer.setCustomerId(null);
        CustomerDTO created = customers.save(customer);
        SessionCurrentUser.signIn(request, created.getCustomerId(), created.getRole().name());
        return created;
    }

    @PostMapping("/signin")
    public ResponseEntity<CustomerDTO> signIn(@RequestBody Credentials credentials, HttpServletRequest request) {
        return customers.authenticate(credentials.login(), credentials.password())
                .map(customer -> {
                    SessionCurrentUser.signIn(request, customer.getCustomerId(), customer.getRole().name());
                    return ResponseEntity.ok(customer);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        SessionCurrentUser.signOut(request);
        return ResponseEntity.noContent().build();
    }

    /** The signed-in customer: GET /customer/{id} with the id the session holds. */
    @GetMapping("/me")
    public ResponseEntity<CustomerDTO> me() {
        Object id = currentUser.id();
        if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.of(customers.findById((Long) id));
    }
}
