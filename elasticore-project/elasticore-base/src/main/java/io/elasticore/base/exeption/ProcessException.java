package io.elasticore.base.exeption;

/**
 * Exception thrown during ElastiCore processing operations.
 * Provides context for processing failures with optional cause chaining.
 */
public class ProcessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public ProcessException() {
        super();
    }
    
    public ProcessException(String message) {
        super(message);
    }
    
    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProcessException(Throwable cause) {
        super(cause);
    }
}
