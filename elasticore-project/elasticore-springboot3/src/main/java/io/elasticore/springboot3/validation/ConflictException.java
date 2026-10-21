package io.elasticore.springboot3.validation;

/**
 * A request that conflicts with what is stored, in a way no database constraint
 * describes - no copy of the book is left to lend, the loan was already returned.
 * The generated API errors answer it with 409 and this message, where a
 * DataIntegrityViolationException from the same place would have been answered
 * with the generic text for a broken unique or foreign key.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
