package org.anyline.exception;

public class NotFoundAdapterException extends AbstractRuntimeException {
    public NotFoundAdapterException() {
        super("not found adapter");
    }

    public NotFoundAdapterException(String message) {
        super(message);
    }
}
