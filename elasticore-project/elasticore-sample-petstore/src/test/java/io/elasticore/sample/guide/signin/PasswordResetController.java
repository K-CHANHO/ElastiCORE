package io.elasticore.sample.guide.signin;

import io.elasticore.sample.petstore.application.dto.CustomerDTO;
import io.elasticore.springboot3.security.SessionCurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Ask with a login; confirm with the token the mail carried and a new password. */
@RestController
@RequestMapping("/api/petstore/account/password-reset")
public class PasswordResetController {

    private final PasswordReset reset;

    public PasswordResetController(PasswordReset reset) {
        this.reset = reset;
    }

    /** 202 whether there is such a customer or not, so the answer tells nobody who has an account. */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ask(@RequestBody Map<String, String> body) {
        reset.ask(body.get("login"));
    }

    /** The new password set, and the customer signed in - in a new session. */
    @PostMapping("/confirm")
    public CustomerDTO confirm(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String password = body.get("password");
        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A password has at least 8 characters");
        }
        CustomerDTO customer = reset.confirm(body.get("token"), password)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The link is invalid or has expired"));
        SessionCurrentUser.signIn(request, customer.getCustomerId(), customer.getRole() == null ? null : customer.getRole().name());
        return customer;
    }
}
