package org.anyline.exception;

public class NotFoundAdapter extends AbstractRuntimeException {
    public NotFoundAdapter() {
        super("not fount adapter");
    }

    public NotFoundAdapter(String message) {
        super(message);
    }
}
