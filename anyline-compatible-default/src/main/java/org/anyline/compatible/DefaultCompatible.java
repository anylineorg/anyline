package org.anyline.compatible;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.compatible.default")
public class DefaultCompatible implements Compatible{
    private static Map<String,String> class2table = new HashMap<>();  // class.name > table.name
    private static Map<String,String> field2column = new HashMap<>(); // class.name:field.name > column.name
    private static Map<String,Field> column2field = new HashMap<>();  // column.name > field
    @Override
    public String table(Class clazz) {
        String key = clazz.getName();
        //1.缓存
        String name = class2table.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name;
        }
        //2.注解
        name = parseAnnotationFieldValue(clazz, "table:name", "table:value", "tableName:name", "tableName:value");
        if(BasicUtil.isNotEmpty(name)){
            class2table.put(key, name.toString());
            return name;
        }
        //3.类名
        name = clazz.getSimpleName();
        class2table.put(key, name.toString());
        return name;
    }

    @Override
    public List<String> columns(Class clazz) {
        List<String> columns = new ArrayList<>();
        List<Field> fields = ClassUtil.getFields(clazz);
        for(Field field:fields){
            String column = column(clazz, field);
            columns.add(column);
        }
        return columns;
    }

    @Override
    public String column(Class clazz, Field field) {
        String key = clazz.getName()+":"+field.getName();
        //1.缓存
        String name = field2column.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name;
        }
        //2.注解
        name = parseAnnotationFieldValue(field, "column:name", "column:value", "TableField:name","TableField:value");
        if(BasicUtil.isNotEmpty(name)){
            field2column.put(key, name.toString());
            return name;
        }
        //3.属性名
        name = field.getName();
        field2column.put(key, name.toString());
        return name;
    }

    @Override
    public Field field(Class clazz, String column) {
        return null;
    }

    @Override
    public String primary(Class clazz) {
        return null;
    }

    @Override
    public List<String> primarys(Class clazz) {
        return null;
    }

    @Override
    public <T> T entity(Class<T> clazz, Map<String, Object> map) {
        T entity = BeanUtil.map2object(map, clazz, false, true, true);
        return entity;
    }

    /**
     * 根据注解名与注解类属性 获取指定类上的注解值
     * @param clazz clazz上的注解
     * @param configs 注册名:注解属性名
     * @return String
     */
    private String parseAnnotationFieldValue(Class clazz, String ... configs){
        for(String config:configs){
            String[] tmps = config.split(":");
            if(tmps.length <2){
                continue;
            }
            Object name = ClassUtil.parseAnnotationFieldValue(clazz, tmps[0], tmps[1]);
            if(BasicUtil.isNotEmpty(name)){
                return name.toString();
            }
        }
        return null;
    }
    /**
     * 根据注解名与注解类属性 获取指定属性上的注解值
     * @param field field上的注解
     * @param configs 注册名:注解属性名
     * @return String
     */
    private String parseAnnotationFieldValue(Field field, String ... configs){
        for(String config:configs){
            String[] tmps = config.split(":");
            if(tmps.length <2){
                continue;
            }
            Object name = ClassUtil.parseAnnotationFieldValue(field, tmps[0], tmps[1]);
            if(BasicUtil.isNotEmpty(name)){
                return name.toString();
            }
        }
        return null;
    }

}
