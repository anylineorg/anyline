package org.anyline.exception;

public class AbstractRuntimeException extends RuntimeException {
    public AbstractRuntimeException() {
    }

    public AbstractRuntimeException(String message) {
        super(message);
    }

    public AbstractRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractRuntimeException(Throwable cause) {
        super(cause);
    }

    public AbstractRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
