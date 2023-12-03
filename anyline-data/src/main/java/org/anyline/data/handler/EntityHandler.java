package org.anyline.data.handler;

public interface EntityHandler<E> extends StreamHandler{

    /**
     *
     * @param entity 一行
     * @return 返回false中断遍历
     */
    boolean read(E entity);
}
