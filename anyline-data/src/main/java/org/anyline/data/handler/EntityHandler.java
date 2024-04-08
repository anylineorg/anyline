package org.anyline.data.handler;

public interface EntityHandler<E> extends StreamHandler{

    /**
     * 在while(ResultSet.next())遍历中调用
     * @param entity 返回通过ResultSet中的一行
     * @return boolean 返回false表示中断遍历,read方法不再再次被调用
     */
    boolean read(E entity);
}
