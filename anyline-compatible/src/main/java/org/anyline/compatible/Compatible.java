package org.anyline.compatible;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface Compatible {
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
     * @param obj 类
     * @return String
     */
    public Map<String,Object> primaryValue(Object obj);
    /**
     * 主键值
     * @param obj 类
     * @return
     */
    public Map<String,Object> primaryValues(Object obj);
    /**
     * map结构转换成clazz对象
     * @param clazz 类
     * @param map map
     * @return T
     * @param <T> T
     */
    public <T> T entity(Class<T> clazz, Map<String,Object> map);
}
