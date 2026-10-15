package io.elasticore.springboot3.validation;

/**
 * A request the caller got wrong in a way no bean constraint describes - a
 * negative page, a sort on a property the entity does not have. The
 * generated API errors answer it with 400, where an IllegalArgumentException
 * from the same place would have been a 500.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
