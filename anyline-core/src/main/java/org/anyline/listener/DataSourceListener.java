
package org.anyline.data.listener;

public interface DataSourceListener {
    default void before(Object object){}
    default void start(){}
    default void finish(){}

    /**
     * 数据源注册完成后调用
     */
    default void after(){}
}
