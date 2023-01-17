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
     * 获取指定类的列名s
     * @param clazz 类
     * @param insert 是否insert环境
     * @param update 是否update环境
     * @return List
     */
    public List<String> columns(Class clazz, boolean insert, boolean update);

    /**
     * 获取指定类.属性关联的列名
     * @param clazz 类
     * @param field 属性
     * @param annotations 根据指定的注解 ,以第一个成功取值的注解为准
     *                    不指定则按默认规则 column.name,column.value,TableField.name,TableField.value,TableId.name,TableId.value,Id.name,Id.value
     *
     * @return String
     */
    public String column(Class clazz, Field field, String ... annotations);

    /**
     * 根据类与列名 获取相关的属性
     * @param clazz 类
     * @param column 列名
     * @return Field
     */
    public Field field(Class clazz, String column);


    /**
     * 检测主键(是主键名不是值)<br/>
     * 从primaryKeys中取一个
     * @param clazz 类
     * @return String
     */
    public String primaryKey(Class clazz);


    /**
     * 检测主键(是主键名不是值)<br/>
     * 根据注解检测主键名s(注解名不区分大小写,支持模糊匹配如Table*)<br/>
     * 先根据配置文件中的ENTITY_PRIMARY_KEY_ANNOTATION,如果出现多种主键标识方式可以逗号分隔以先取到的为准<br/>
     * 如果没有检测到再检测注解中带TableId或Id的属性名<br/>
     * 如果没有检测到按默认主键DataRow.DEFAULT_PRIMARY_KEY<br/>
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
     * @return Map
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
    public <T> T entity(Class<T> clazz, Map<String,Object> map, Map columns);

    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param entity 在此基础上执行,如果不提供则新创建
     * @param clazz 类
     * @param map map
     * @param columns 列属性
     * @return T
     * @param <T> T
     */
    public <T> T entity(T entity, Class<T> clazz, Map<String,Object> map, Map columns);


    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    public DataRow row(Object obj, String ... keys);



    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * 注意实现时不要调用 DataRow.public static DataRow parse(DataRow row, Object obj, String... keys) 形成无限递归
     * @param row 在此基础上执行,如果不提供则新创建
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    public DataRow row(DataRow row, Object obj, String ... keys);

    /**
     * 列名转换成http参数时调用
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param metadata metadata
     * @return List
     *
     */
    public List<String> column2param(List<String> metadata);
    public String column2param(String metadata);

}
