package org.anyline.entity;


public interface EntityListener {


    /**
     * entity创建完成后调用 AbstractBasicController.entity后调用过
     * @param env 上下文 如request
     * @param entity entity
     */
    public void after(Object env, Object entity);


}
