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
    private static Map<String,String> tables = new HashMap<>();
    private static Map<String,String> columns = new HashMap<>();
    @Override
    public String table(Class clazz) {
        String key = clazz.getName();
        Object name = tables.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(clazz, "table", "name");
        if(BasicUtil.isNotEmpty(name)){
            tables.put(key, name.toString());
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(clazz, "table", "value");
        if(BasicUtil.isNotEmpty(name)){
            tables.put(key, name.toString());
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(clazz, "tableName", "name");
        if(BasicUtil.isNotEmpty(name)){
            tables.put(key, name.toString());
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(clazz, "tableName", "value");
        if(BasicUtil.isNotEmpty(name)){
            tables.put(key, name.toString());
            return name.toString();
        }
        name = clazz.getSimpleName();
        tables.put(key, name.toString());
        return name.toString();
    }

    @Override
    public List<String> columns(Class clazz) {
        List<String> columns = new ArrayList<>();
        List<Field> fields = BeanUtil.getFields(clazz);
        for(Field field:fields){
            String name = field.getName();
            String column = BeanUtil.camel_(name);
            columns.add(column);
        }
        return columns;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public String column(Class clazz, Field field) {
        String key = clazz.getName()+":"+field.getName();
        Object name = columns.get(field.getName());
        if(BasicUtil.isNotEmpty(name)){
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(field, "column", "name");
        if(BasicUtil.isNotEmpty(name)){
            columns.put(key, name.toString());
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(field, "column", "value");
        if(BasicUtil.isNotEmpty(name)){
            columns.put(key, name.toString());
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(field, "TableField", "name");
        if(BasicUtil.isNotEmpty(name)){
            columns.put(key, name.toString());
            return name.toString();
        }
        name = ClassUtil.parseAnnotationFieldValue(field, "TableField", "value");
        if(BasicUtil.isNotEmpty(name)){
            columns.put(key, name.toString());
            return name.toString();
        }
        name = field.getName();
        columns.put(key, name.toString());
        return name.toString();
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
}
