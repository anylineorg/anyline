package org.anyline.handler;

public interface DataHandler<E> {
    void handle(E event);
}
