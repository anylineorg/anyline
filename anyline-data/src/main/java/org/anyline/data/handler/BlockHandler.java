package org.anyline.data.handler;

public interface BlockHandler<E> extends DataHandler {
    /**
     * 全部结算集封装完成后回调
     */
    void finish(E result);

}
