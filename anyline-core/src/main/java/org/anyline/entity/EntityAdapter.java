package org.anyline.entity;

import java.util.List;

public interface EntityAdapter {
    /**
     * DataRow转换成entity时调用  如果有实现则不再执行DataRow.entity
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param clazz calzz
     * @param row row
     * @param <T> T
     * @return T
     */
    public <T> T entity(Class<T> clazz, DataRow row);

    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    public DataRow parse(Object obj, String ... keys);

    /**
     * entity创建完成后调用 AbstractBasicController.entity后调用过
     * @param env 上下文 如request
     * @param entity entity
     */
    public void after(Object env, Object entity);

    /**
     * 列名转换成http参数时调用
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param metadata metadata
     * @return List
     *
     */
    public List<String> metadata2param(List<String> metadata);
}
