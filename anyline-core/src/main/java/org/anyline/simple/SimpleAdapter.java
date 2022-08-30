package org.anyline.simple;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntityAdapter;
import org.anyline.entity.EntitySet;
import org.anyline.entity.adapter.KeyAdapter;
import org.anyline.util.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component("anyline.entity.adapter")
public class SimpleAdapter implements EntityAdapter {
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
        List<String> columns = SimpleAdapter.columns.get(clazz.getName());
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
            SimpleAdapter.columns.put(clazz.getName(),columns);
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
    public <T> T entity(T entity, Class<T> clazz, Map<String, Object> map) {
        List<Field> fields = ClassUtil.getFields(clazz);
        Map<Field,String> fk = new HashMap<>();
        entity = BeanUtil.map2object(entity, map, clazz, false, true, true);
        for(Field field:fields){
            String column = column(clazz, field);
            Object value = map.get(column);
            if(null != value) {
                BeanUtil.setFieldValue(entity, field, map.get(column));
            }
        }
        return entity;
    }

    @Override
    public <T> T entity(Class<T> clazz, Map<String, Object> map) {
        return entity(null, clazz, map);
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
    public DataRow row(DataRow row, Object obj, String... keys) {
        //注意不要调用 DataRow.public static DataRow parse(DataRow row, Object obj, String... keys) 形成无限递归
        return DataRow.parse(row, KeyAdapter.KEY_CASE.CONFIG, obj, keys);
    }
    @Override
    public DataRow row(Object obj, String... keys) {
        return row(null, obj, keys);
    }


    @Override
    public List<String> metadata2param(List<String> metadatas) {
        List<String> params = new ArrayList<>();
        for(String metadata:metadatas){
            params.add(metadata2param(metadata));
        }
        return params;
    }


    @Override
    public String metadata2param(String metadata){
        String param = null;
        //注意这里只支持下划线转驼峰
        //如果数据库中已经是驼峰,不要配置这个参数
        String keyCase = ConfigTable.getString("HTTP_PARAM_KEYS_CASE");
        if("camel".equals(keyCase)){
            param = metadata + ":" + BeanUtil.camel(metadata.toLowerCase());
        }else if("Camel".equals(keyCase)){
            String key = CharUtil.toUpperCaseHeader(metadata.toLowerCase());
            param = key+":"+BeanUtil.Camel(key);
        }else{
            param = metadata + ":" + metadata;
        }
        return param;
    }

}
