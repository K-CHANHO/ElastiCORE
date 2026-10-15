package io.elasticore.springboot3.security;

/** The caller is signed in and the operation is not theirs to do: 403. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
