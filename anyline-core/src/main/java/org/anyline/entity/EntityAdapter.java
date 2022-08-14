package org.anyline.entity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface EntityAdapter {



    /**
     * 获取指定类关联的表名
     * @param clazz 类
     * @return String
     */
    public String table(Class clazz);

    /**
     * 获取指定类的列名s
     * @param clazz 类
     * @return List
     */
    public List<String> columns(Class clazz);

    /**
     * 获取指定类.属性关联的列名
     * @param clazz 类
     * @param field 属性
     * @return String
     */
    public String column(Class clazz, Field field);

    /**
     * 根据类与列名 获取相关的属性
     * @param clazz 类
     * @param column 列名
     * @return Field
     */
    public Field field(Class clazz, String column);

    /**
     * 获取clazz类相关的主键
     * @param clazz 类
     * @return String
     */
    public String primaryKey(Class clazz);

    /**
     * 获取clazz类相关的主键s
     * @param clazz 类
     * @return List
     */
    public List<String> primaryKeys(Class clazz);

    /**
     * 主键值
     * @param obj obj
     * @return String
     */
    public Map<String,Object> primaryValue(Object obj);
    /**
     * 主键值
     * @param obj obj
     * @return
     */
    public Map<String,Object> primaryValues(Object obj);

    /**
     * 生成主键值
     * @param obj obj
     */
    public void createPrimaryValue(Object obj);
    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param clazz 类
     * @param map map
     * @return T
     * @param <T> T
     */
    public <T> T entity(Class<T> clazz, Map<String,Object> map);


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
