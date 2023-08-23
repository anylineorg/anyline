package org.anyline.data.handler;

public interface EntityHandler<E> extends StreamHandler{
    boolean read(E entity);
}
