package org.anyline.data.adapter;

import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.KeyAdapter;
import org.anyline.entity.DataRow;
import org.anyline.entity.data.Column;
import org.anyline.entity.data.Table;
import org.anyline.util.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component("anyline.entity.adapter")
public class DefaultEntityAdapter implements EntityAdapter {
    private static LinkedHashMap<String, Table> class2table    = new LinkedHashMap<>();  // class.name > table.name
    private static LinkedHashMap<String, Column> field2column   = new LinkedHashMap<>();  // class.name:field.name > column.name
    private static LinkedHashMap<String, Field> column2field    = new LinkedHashMap<>();  // column.name > field
    private static LinkedHashMap<String, LinkedHashMap<String, Column>> primarys = new LinkedHashMap<>();  // 主键
    private static LinkedHashMap<String, LinkedHashMap<String, Column>> insert_columns  = new LinkedHashMap<>();
    private static LinkedHashMap<String, LinkedHashMap<String, Column>> update_columns  = new LinkedHashMap<>();
    private static LinkedHashMap<String, LinkedHashMap<String, Column>> ddl_columns  = new LinkedHashMap<>();

    /**
     * 清空缓存
     */
    public static void clear(){
        class2table    = new LinkedHashMap<>();
        field2column   = new LinkedHashMap<>();
        column2field    = new LinkedHashMap<>();
        primarys = new LinkedHashMap<>();
        insert_columns  = new LinkedHashMap<>();
        update_columns  = new LinkedHashMap<>();
    }
    @Override
    public Table table(Class clazz) {
        String key = clazz.getName();
        // 1.缓存
        Table table = class2table.get(key.toUpperCase());
        if(null != table){
            return table;
        }
        // 2.注解 以及父类注解直到Object
        Class parent = clazz;
        String name = null;
        while (true){
            name = ClassUtil.parseAnnotationFieldValue(parent, "table.name", "table.value", "tableName.name", "tableName.value");
            if(BasicUtil.isEmpty(name)){
                parent = parent.getSuperclass();
                if(null == parent){
                    break;
                }
            }else{
                table = new org.anyline.data.entity.Table(name);
                class2table.put(key.toUpperCase(), table);
                return table;
            }
        }
        // 3.类名转成表名
        if("Camel_".equalsIgnoreCase(ConfigTable.ENTITY_CLASS_TABLE_MAP)){
            name = BeanUtil.camel_(clazz.getSimpleName());
            table = new org.anyline.data.entity.Table(name);
            class2table.put(key.toUpperCase(), table);
            return table;
        }
        // 4.类名
        name = clazz.getSimpleName();
        table = new org.anyline.data.entity.Table(name);
        class2table.put(key.toUpperCase(), table);
        return table;
    }

    @Override
    public LinkedHashMap<String, Column> columns(Class clazz) {
        return columns(clazz, MODE.DDL);
    }
    @Override
    public LinkedHashMap<String, Column> columns(Class clazz, MODE mode) {
        LinkedHashMap<String, Column> columns = null;
        if(MODE.INSERT == mode) {
            columns = DefaultEntityAdapter.insert_columns.get(clazz.getName().toUpperCase());
        }else if(MODE.UPDATE == mode){
            columns = DefaultEntityAdapter.update_columns.get(clazz.getName().toUpperCase());
        }else if(MODE.DDL == mode){

        }
        if(null == columns) {
            columns = new LinkedHashMap<>();
            List<Field> fields = ClassUtil.getFields(clazz, false, false);
            List<Field> ignores = ClassUtil.getFieldsByAnnotation(clazz, "Transient");
            fields.removeAll(ignores);
            for (Field field : fields) {
                Column column = column(clazz, field);
                if(MODE.INSERT == mode){
                    //检测是否需要insert
                    String insertable = ClassUtil.parseAnnotationFieldValue(field, "column.insertable");
                    if("false".equalsIgnoreCase(insertable)){
                        continue;
                    }
                }else if(MODE.UPDATE == mode){
                    //检测是否需要update
                    String updatable = ClassUtil.parseAnnotationFieldValue(field, "column.updatable");
                    if("false".equalsIgnoreCase(updatable)){
                        continue;
                    }
                }else if(MODE.DDL == mode){

                }
                if(BasicUtil.isNotEmpty(column)) {
                    columns.put(column.getName().toUpperCase(), column);
                }
            }
            if(MODE.INSERT == mode) {
                DefaultEntityAdapter.insert_columns.put(clazz.getName().toUpperCase(),columns);
            }else if(MODE.UPDATE == mode){
                DefaultEntityAdapter.update_columns.put(clazz.getName().toUpperCase(),columns);
            }else if(MODE.DDL == mode){
                DefaultEntityAdapter.ddl_columns.put(clazz.getName().toUpperCase(),columns);
            }
        }
        LinkedHashMap<String, Column> list = new LinkedHashMap();
        list.putAll(columns);
        return list;
    }

    @Override
    public Column column(Class clazz, Field field, String ... annotations) {
        String key = clazz.getName()+":"+field.getName().toUpperCase();
        // 1.缓存
        Column column = field2column.get(key.toUpperCase());
        if(null != column){
            return column;
        }
        String name = null;
        // 2.注解
        if(null == annotations || annotations.length ==0 ){
            if(BasicUtil.isNotEmpty(ConfigTable.ENTITY_COLUMN_ANNOTATION)){
                annotations = ConfigTable.ENTITY_COLUMN_ANNOTATION.split(",");
            }else {
                annotations = "column.name,column.value,TableField.name,TableField.value,TableId.name,TableId.value,Id.name,Id.value".split(",");
            }
        }
        name = ClassUtil.parseAnnotationFieldValue(field, annotations);

        // 3.属性名转成列名
        if(BasicUtil.isEmpty(name)){
            if("camel_".equals(ConfigTable.ENTITY_FIELD_COLUMN_MAP)){
                name = BeanUtil.camel_(field.getName());
            }
        }
        // 4.属性名
        if(BasicUtil.isEmpty(name)){
            Class c = field.getType();
            //boolean、char、byte、short、int、long、float、double
            if(c == String.class || c == Date.class || ClassUtil.isPrimitiveClass(c)) {
                name = field.getName();
            }
        }

        //创建Column
        if(BasicUtil.isNotEmpty(name)){
            column = new org.anyline.data.entity.Column(name);
            field2column.put(key.toUpperCase(), column);
            column2field.put(clazz.getName().toUpperCase()+":"+name.toUpperCase(), field);
            return column;
        }
        return null;
    }

    @Override
    public Field field(Class clazz, Column column) {
        return field(clazz, column.getName());
    }
    @Override
    public Field field(Class clazz, String column) {
        Field field = column2field.get(clazz.getName().toUpperCase()+":"+column.toUpperCase());
        if(null == field){
            fields(clazz);
            field = column2field.get(clazz.getName().toUpperCase()+":"+column.toUpperCase());
        }
        return field;
    }
    public void fields(Class clazz){
        List<Field> fields = ClassUtil.getFields(clazz);
        for(Field field:fields){
            column(clazz, field);
        }
    }

    @Override
    public Column primaryKey(Class clazz) {
        LinkedHashMap<String, Column> list = primaryKeys(clazz);
        for(Column column:list.values()){
            return column;
        }
        return new org.anyline.data.entity.Column(DataRow.DEFAULT_PRIMARY_KEY);
    }

    /**
     * 检测主键(是主键名不是值)<br/>
     * 先检测注解中带TableId或Id的属性名<br/>
     * 如果没有检测到按默认主键DataRow.DEFAULT_PRIMARY_KEY<br/>
     * @param clazz 类
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> primaryKeys(Class clazz) {
        LinkedHashMap<String, Column> list = primarys.get(clazz.getName().toUpperCase());
        if(null == list) {
            list = new LinkedHashMap<>();
            String annotations = ConfigTable.ENTITY_PRIMARY_KEY_ANNOTATION;
            if(BasicUtil.isEmpty(annotations)){
                //如果配置文件中没有指定
                annotations = "TableId,Id";
            }
            //根据注解提取属性s
            List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, annotations.split(","));
            for (Field field : fields) {
                //根据属性获取相应的列名
                Column column = column(clazz, field, annotations.split(","));
                if (null != column) {
                    list.put(column.getName().toUpperCase(), column);
                }
            }
            if(list.isEmpty()) {
                //从所有属性中 过滤出名称与DataRow.DEFAULT_PRIMARY_KEY相同的属性
                fields = ClassUtil.getFields(clazz, false, false);
                Field field = ClassUtil.getField(fields, DataRow.DEFAULT_PRIMARY_KEY, true, true);
                if (null != field) {
                    Column column = column(clazz, field, annotations.split(","));
                    if (null != column) {
                        list.put(column.getName().toUpperCase(), column);
                    }
                }
            }
            if (list.size() == 0) {
                list.put(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase(), new org.anyline.data.entity.Column(DataRow.DEFAULT_PRIMARY_KEY));
            }
            primarys.put(clazz.getName().toUpperCase(), list);

        }
        return list;
    }

    @Override
    public <T> T entity(T entity, Class<T> clazz, Map<String, Object> map, Map metadatas) {
        List<Field> fields = ClassUtil.getFields(clazz, false, false);
        Map<Field,String> fk = new HashMap<>();
        //entity = BeanUtil.map2object(entity, map, clazz, metadatas, false, true, true);
        if (null == entity) {
            try {
                entity = (T) clazz.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for(Field field:fields){
            Column column = column(clazz, field);//列名
            Object value = map.get(column.getName());
            if(null != value) {
                Column metadata = null;  //列属性
                if(map instanceof DataRow){
                    metadata = ((DataRow)map).getMetadata(column.getName());
                }
                if(null == metadata && null != metadatas){
                    metadata = (Column) metadatas.get(column.getName().toUpperCase());
                }
                BeanUtil.setFieldValue(entity, field, metadata, value);

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
        Column primary = primaryKey(obj.getClass());
        Field field = column2field.get(obj.getClass().getName().toUpperCase()+":"+primary.getName().toUpperCase());
        Object value = BeanUtil.getFieldValue(obj, field);
        Map<String,Object> map = new HashMap<>();
        map.put(primary.getName().toUpperCase(), value);
        return map;
    }

    @Override
    public void createPrimaryValue(Object obj) {

    }

    @Override
    public Map<String, Object> primaryValues(Object obj) {
        LinkedHashMap<String, Column> primarys = primaryKeys(obj.getClass());
        Map<String,Object> map = new HashMap<>();
        for(String primary:primarys.keySet()){
            Field field = column2field.get(obj.getClass().getName().toUpperCase()+":"+primary.toUpperCase());
            Object value = BeanUtil.getFieldValue(obj, field);
            map.put(primary.toUpperCase(), value);
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
