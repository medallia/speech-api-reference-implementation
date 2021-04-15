package com.medallia.references.speechapi.exceptions;

/**
 * This exception is thrown when the script execution is stopped
 * for any reason.
 */
public class ExecutionNotFinishedException extends RuntimeException {

    public ExecutionNotFinishedException(final String message) {
        super(message);
    }

    public ExecutionNotFinishedException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
