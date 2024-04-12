package org.anyline.handler;

public interface AnyHandler<E> {
    void handle(E event);
}
