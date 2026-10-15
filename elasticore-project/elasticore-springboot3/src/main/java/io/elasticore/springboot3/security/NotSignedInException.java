package io.elasticore.springboot3.security;

/** The operation is for signed-in users and the caller is not one: 401. */
public class NotSignedInException extends RuntimeException {

    public NotSignedInException(String message) {
        super(message);
    }
}
