package org.anyline.adapter;

import org.anyline.entity.DataRow;
import org.anyline.entity.EntityAdapter;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component("anyline.entity.adapter")
public class DefaultAdapter implements EntityAdapter {
    private static Map<String,String> class2table    = new HashMap<>();  // class.name > table.name
    private static Map<String,String> field2column   = new HashMap<>();  // class.name:field.name > column.name
    private static Map<String,Field> column2field    = new HashMap<>();  // column.name > field
    private static Map<String,List<String>> primarys = new HashMap<>();  // 主键
    private static Map<String,List<String>> columns  = new HashMap<>();
    @Override
    public String table(Class clazz) {
        String key = clazz.getName();
        //1.缓存
        String name = class2table.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name;
        }
        //2.注解
        name = ClassUtil.parseAnnotationFieldValue(clazz, "table.name", "table.value", "tableName.name", "tableName.value");
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
        List<String> columns = DefaultAdapter.columns.get(clazz.getName());
        if(null == columns) {
            columns = new ArrayList<>();
            List<Field> fields = ClassUtil.getFields(clazz);
            List<Field> ignores = ClassUtil.getFieldsByAnnotation(clazz, "Transient");
            fields.removeAll(ignores);
            for (Field field : fields) {
                String column = column(clazz, field);
                if(BasicUtil.isNotEmpty(column)) {
                    columns.add(column);
                }
            }
            DefaultAdapter.columns.put(clazz.getName(),columns);
        }
        return columns;
    }

    @Override
    public String column(Class clazz, Field field) {
        String key = clazz.getName()+":"+field.getName().toUpperCase();
        //1.缓存
        String name = field2column.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name;
        }
        //2.注解
        name = ClassUtil.parseAnnotationFieldValue(field, "column.name", "column.value", "TableField.name","TableField.value","TableId.value");
        if(BasicUtil.isNotEmpty(name)){
            field2column.put(key, name);
            column2field.put(clazz.getName()+":"+name.toUpperCase(), field);
            return name;
        }

        //3.属性名转成列名
        if("camel_".equals(ConfigTable.getString("ENTITY_FIELD_COLUMN_MAP"))){
            name = BeanUtil.camel_(field.getName());
            field2column.put(key, name);
            column2field.put(clazz.getName()+":"+name.toUpperCase(), field);
            return name;
        }
        //4.属性名

        Class c = field.getType();
        if(c == String.class || c == Date.class || ClassUtil.isPrimitiveClass(c)) {
            name = field.getName();
            field2column.put(key, name);
            column2field.put(clazz.getName()+":"+name.toUpperCase(), field);
            return name;
        }
        return null;
    }

    @Override
    public Field field(Class clazz, String column) {
        return column2field.get(clazz.getName()+":"+column.toUpperCase());
    }

    @Override
    public String primaryKey(Class clazz) {
       List<String> list = primaryKeys(clazz);
       if(list.size()>0){
           return list.get(0);
       }
       return DataRow.DEFAULT_PRIMARY_KEY;
    }

    @Override
    public List<String> primaryKeys(Class clazz) {
        List<String> list = primarys.get(clazz.getName());
        if(null == list) {
            list = new ArrayList<>();
            List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "TableId", "Id");
            for (Field field : fields) {
                String name = column(clazz, field);
                if (BasicUtil.isNotEmpty(name)) {
                    list.add(name);
                }
            }
            fields = ClassUtil.getFields(clazz);
            Field field = ClassUtil.getField(fields, DataRow.DEFAULT_PRIMARY_KEY, true, true);
            if(null != field){
                String name = column(clazz, field);
                if (BasicUtil.isNotEmpty(name)) {
                    list.add(name);
                }
            }
            if (list.size() == 0) {
                list.add(DataRow.DEFAULT_PRIMARY_KEY);
            }
            primarys.put(clazz.getName(), list);
        }
        return list;
    }

    @Override
    public <T> T entity(Class<T> clazz, Map<String, Object> map) {
        List<Field> fields = ClassUtil.getFields(clazz);
        Map<Field,String> fk = new HashMap<>();
        T entity = BeanUtil.map2object(map, clazz, false, true, true);
        for(Field field:fields){
            String column = column(clazz, field);
            BeanUtil.setFieldValue(entity, field, map.get(column));
        }
        return entity;
    }

    @Override
    public Map<String, Object> primaryValue(Object obj) {
        String primary = primaryKey(obj.getClass());
        Field field = column2field.get(obj.getClass().getName()+":"+primary.toUpperCase());
        Object value = BeanUtil.getFieldValue(obj, field);
        Map<String,Object> map = new HashMap<>();
        map.put(primary, value);
        return map;
    }

    @Override
    public void createPrimaryValue(Object obj) {

    }

    @Override
    public Map<String, Object> primaryValues(Object obj) {
        List<String> primarys = primaryKeys(obj.getClass());
        Map<String,Object> map = new HashMap<>();
        for(String primary:primarys){
            Field field = column2field.get(obj.getClass().getName()+":"+primary.toUpperCase());
            Object value = BeanUtil.getFieldValue(obj, field);
            map.put(primary, value);
        }
        return map;
    }

    @Override
    public DataRow parse(Object obj, String... keys) {
        return null;
    }

    @Override
    public void after(Object env, Object entity) {

    }

    @Override
    public List<String> metadata2param(List<String> metadata) {
        return null;
    }

}
