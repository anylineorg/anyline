package org.anyline.init;

import org.anyline.entity.DataRow;
import org.anyline.entity.EntityAdapter;
import org.anyline.entity.adapter.KeyAdapter;
import org.anyline.entity.data.Column;
import org.anyline.util.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component("anyline.entity.adapter")
public class DefaultEntityAdapter implements EntityAdapter {
    private static Map<String,String> class2table    = new HashMap<>();  // class.name > table.name
    private static Map<String,String> field2column   = new HashMap<>();  // class.name:field.name > column.name
    private static Map<String,Field> column2field    = new HashMap<>();  // column.name > field
    private static Map<String,List<String>> primarys = new HashMap<>();  // 主键
    private static Map<String,List<String>> insert_columns  = new HashMap<>();
    private static Map<String,List<String>> update_columns  = new HashMap<>();

    /**
     * 清空缓存
     */
    public static void clear(){
        class2table    = new HashMap<>();
        field2column   = new HashMap<>();
        column2field    = new HashMap<>();
        primarys = new HashMap<>();
        insert_columns  = new HashMap<>();
        update_columns  = new HashMap<>();
    }
    @Override
    public String table(Class clazz) {
        String key = clazz.getName();
        // 1.缓存
        String name = class2table.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name;
        }
        // 2.注解 以及父类注解直到Object
        Class parent = clazz;
        while (true){
            name = ClassUtil.parseAnnotationFieldValue(parent, "table.name", "table.value", "tableName.name", "tableName.value");
            if(BasicUtil.isEmpty(name)){
                parent = parent.getSuperclass();
                if(null == parent){
                    break;
                }
            }else{
                class2table.put(key, name);
                return name;
            }
        }
        // 3.类名
        name = clazz.getSimpleName();
        class2table.put(key, name.toString());
        return name;
    }

    @Override
    public List<String> columns(Class clazz) {
        return columns(clazz, false, false);
    }
    @Override
    public List<String> columns(Class clazz, boolean insert, boolean update) {
        List<String> columns = null;
        if(insert) {
            columns = DefaultEntityAdapter.insert_columns.get(clazz.getName());
        }else if(update){
            columns = DefaultEntityAdapter.update_columns.get(clazz.getName());
        }
        if(null == columns) {
            columns = new ArrayList<>();
            List<Field> fields = ClassUtil.getFields(clazz);
            List<Field> ignores = ClassUtil.getFieldsByAnnotation(clazz, "Transient");
            fields.removeAll(ignores);
            for (Field field : fields) {
                String column = column(clazz, field);
                if(insert){
                    //检测是否需要insert
                    String insertable = ClassUtil.parseAnnotationFieldValue(field, "column.insertable");
                    if("false".equalsIgnoreCase(insertable)){
                        continue;
                    }
                }
                if(update){
                    //检测是否需要update
                    String updatable = ClassUtil.parseAnnotationFieldValue(field, "column.updatable");
                    if("false".equalsIgnoreCase(updatable)){
                        continue;
                    }
                }
                if(BasicUtil.isNotEmpty(column)) {
                    columns.add(column);
                }
            }
            if(insert) {
                DefaultEntityAdapter.insert_columns.put(clazz.getName(),columns);
            }else if(update){
                DefaultEntityAdapter.update_columns.put(clazz.getName(),columns);
            }
        }
        List<String> list = new ArrayList<>();
        list.addAll(columns);
        return list;
    }

    @Override
    public String column(Class clazz, Field field, String ... annotations) {
        String key = clazz.getName()+":"+field.getName().toUpperCase();
        // 1.缓存
        String name = field2column.get(key);
        if(BasicUtil.isNotEmpty(name)){
            return name;
        }

        // 2.注解
        if(null == annotations || annotations.length ==0 ){
            if(BasicUtil.isNotEmpty(ConfigTable.ENTITY_COLUMN_ANNOTATION)){
                annotations = ConfigTable.ENTITY_COLUMN_ANNOTATION.split(",");
            }else {
                annotations = "column.name,column.value,TableField.name,TableField.value,TableId.name,TableId.value,Id.name,Id.value".split(",");
            }
        }
        name = ClassUtil.parseAnnotationFieldValue(field, annotations);
        if(BasicUtil.isNotEmpty(name)){
            field2column.put(key, name);
            column2field.put(clazz.getName()+":"+name.toUpperCase(), field);
            return name;
        }

        // 3.属性名转成列名
        if("camel_".equals(ConfigTable.ENTITY_FIELD_COLUMN_MAP)){
            name = BeanUtil.camel_(field.getName());
            field2column.put(key, name);
            column2field.put(clazz.getName()+":"+name.toUpperCase(), field);
            return name;
        }
        // 4.属性名

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

    /**
     * 检测主键(是主键名不是值)<br/>
     * 先检测注解中带TableId或Id的属性名<br/>
     * 如果没有检测到按默认主键DataRow.DEFAULT_PRIMARY_KEY<br/>
     * @param clazz 类
     * @return List
     */
    @Override
    public List<String> primaryKeys(Class clazz) {
        List<String> list = primarys.get(clazz.getName());
        if(null == list) {
            list = new ArrayList<>();
            String annotations = ConfigTable.ENTITY_PRIMARY_KEY_ANNOTATION;
            if(BasicUtil.isEmpty(annotations)){
                //如果配置文件中没有指定
                annotations = "TableId,Id";
            }
            //根据注解提取属性s
            List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, annotations.split(","));
            for (Field field : fields) {
                //根据属性获取相应的列名
                String name = column(clazz, field, annotations.split(","));
                if (BasicUtil.isNotEmpty(name)) {
                    list.add(name);
                }
            }
            if(list.isEmpty()) {
                //从所有属性中 过滤出名称与DataRow.DEFAULT_PRIMARY_KEY相同的属性
                fields = ClassUtil.getFields(clazz);
                Field field = ClassUtil.getField(fields, DataRow.DEFAULT_PRIMARY_KEY, true, true);
                if (null != field) {
                    String name = column(clazz, field);
                    if (BasicUtil.isNotEmpty(name)) {
                        list.add(name);
                    }
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
    public <T> T entity(T entity, Class<T> clazz, Map<String, Object> map, Map columns) {
        List<Field> fields = ClassUtil.getFields(clazz);
        Map<Field,String> fk = new HashMap<>();
        entity = BeanUtil.map2object(entity, map, clazz, columns, false, true, true);
        for(Field field:fields){
            String column = column(clazz, field);
            Object value = map.get(column);
            if(null != value) {
                Column col = null;
                if(null != columns){
                    col = (Column) columns.get(column.toUpperCase());
                }
                BeanUtil.setFieldValue(entity, field, col, map.get(column));
            }
        }
        return entity;
    }

    @Override
    public <T> T entity(Class<T> clazz, Map<String, Object> map, Map columns) {
        return entity(null, clazz, map, columns);
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
        // 注意不要调用 DataRow.public static DataRow parse(DataRow row, Object obj, String... keys) 形成无限递归
        return DataRow.parse(row, KeyAdapter.KEY_CASE.CONFIG, obj, keys);
    }
    @Override
    public DataRow row(Object obj, String... keys) {
        return row(null, obj, keys);
    }


    @Override
    public List<String> column2param(List<String> metadatas) {
        List<String> params = new ArrayList<>();
        for(String metadata:metadatas){
            params.add(column2param(metadata));
        }
        return params;
    }


    @Override
    public String column2param(String metadata){
        String param = null;
        // 注意这里只支持下划线转驼峰
        // 如果数据库中已经是驼峰,不要配置这个参数
        String keyCase = ConfigTable.HTTP_PARAM_KEY_CASE;
        if("camel".equals(keyCase)){
            param = metadata + ":" + BeanUtil.camel(metadata.toLowerCase());
        }else if("Camel".equals(keyCase)){
            String key = CharUtil.toUpperCaseHeader(metadata.toLowerCase());
            param = metadata+":"+BeanUtil.Camel(key);
        }else if("lower".equalsIgnoreCase(keyCase)){
            param = metadata + ":" + metadata.toLowerCase();
        }else if("upper".equalsIgnoreCase(keyCase)){
            param = metadata + ":" + metadata.toUpperCase();
        }else{
            param = metadata + ":" + metadata;
        }
        return param;
    }

}
