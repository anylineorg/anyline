/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.adapter;

import org.anyline.entity.DataRow;
import org.anyline.entity.generator.GeneratorConfig;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.persistence.ManyToMany;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.*;

import java.lang.reflect.Field;
import java.util.*;

public interface EntityAdapter {
    enum MODE{
        /**
         * 只检测可INSERT的列
         */
        INSERT,
        /**
         * 只检测可UPDATE的列
         */
        UPDATE,
        /**
         * 检测所有的列一般用于定义表
         */
        DDL
    }

    static void sort(List<? extends EntityAdapter> interceptors) {
        Collections.sort(interceptors, new Comparator<EntityAdapter>() {
            public int compare(EntityAdapter r1, EntityAdapter r2) {
                int order1 = r1.order();
                int order2 = r2.order();
                if(order1 > order2) {
                    return 1;
                }else if(order1 < order2) {
                    return -1;
                }
                return 0;
            }
        });
    }
    default int order() {
        return 10;
    }

    /**
     * 针对哪些类有效
     * @return 类
     */
    default Class type() {
        return Object.class;
    }

    /**
     * 针对多个类有效
     * @return list
     */
    default List<Class> types() {
        return new ArrayList<>();
    }

    /**
     * 获取指定类关联的表名
     * @param clazz 类
     * @return String
     */
    default Table table(Class clazz) {
        String key = clazz.getName();
        // 1.缓存
        Table table = EntityAdapterProxy.class2table.get(key.toUpperCase());
        if(null != table) {
            return table;
        }
        String name = null;
        // 2.注解 以及父类注解直到Object
        Class parent = clazz;
        while (true) {
            name = ClassUtil.parseAnnotationFieldValue(parent, "table.name", "table.value", "tableName.name", "tableName.value");
            if (BasicUtil.isEmpty(name)) {
                parent = parent.getSuperclass();
                if (null == parent) {
                    break;
                }
            } else {
                table = new Table(name);
                EntityAdapterProxy.class2table.put(key.toUpperCase(), table);
                break;
            }
        }
        if(null == table) {
            // 3.类名转成表名
            if ("Camel_".equalsIgnoreCase(ConfigTable.ENTITY_CLASS_TABLE_MAP)) {
                name = BeanUtil.camel_(clazz.getSimpleName());
                table = new Table(name);
                EntityAdapterProxy.class2table.put(key.toUpperCase(), table);
            }
        }
        if(null == table) {
            // 4.类名
            name = clazz.getSimpleName();
            table = new Table(name);
            EntityAdapterProxy.class2table.put(key.toUpperCase(), table);
        }
        //comment
        String comment = ClassUtil.parseAnnotationFieldValue(clazz, "table.comment");
        if(BasicUtil.isNotEmpty(comment)) {
            table.setComment(comment);
        }
        //列
        LinkedHashMap<String, Column> columns = columns(clazz);
        table.setColumns(columns);
        return table;
    }

    /**
     * 获取指定类的列名s
     * @param clazz 类
     * @return List
     */
    default LinkedHashMap<String, Column> columns(Class clazz) {
        return columns(clazz, MODE.DDL);
    }

    /**
     * 获取指定类的列名s
     * @param clazz 类
     * @param mode  insert环境  update环境 ddl环境
     * @return List
     */

    default LinkedHashMap<String, Column> columns(Class clazz, MODE mode) {
        LinkedHashMap<String, Column> columns = null;
        if(MODE.INSERT == mode) {
            columns = EntityAdapterProxy.insert_columns.get(clazz.getName().toUpperCase());
        }else if(MODE.UPDATE == mode) {
            columns = EntityAdapterProxy.update_columns.get(clazz.getName().toUpperCase());
        }else if(MODE.DDL == mode) {

        }
        if(null == columns) {
            columns = new LinkedHashMap<>();
            List<Field> fields = ClassUtil.getFields(clazz, false, false);
            List<Field> ignores = ClassUtil.getFieldsByAnnotation(clazz, "Transient","OneToMany","ManyToMany");
            fields.removeAll(ignores);
            for (Field field : fields) {
                Column column = column(clazz, field);
                if(MODE.INSERT == mode) {
                    //检测是否需要insert
                    String insertable = ClassUtil.parseAnnotationFieldValue(field, "column.insertable");
                    if("false".equalsIgnoreCase(insertable)) {
                        continue;
                    }
                }else if(MODE.UPDATE == mode) {
                    //检测是否需要update
                    String updatable = ClassUtil.parseAnnotationFieldValue(field, "column.updatable");
                    if("false".equalsIgnoreCase(updatable)) {
                        continue;
                    }
                }else if(MODE.DDL == mode) {

                }
                if(BasicUtil.isNotEmpty(column)) {
                    columns.put(column.getName().toUpperCase(), column);
                }
            }
            if(MODE.INSERT == mode) {
                EntityAdapterProxy.insert_columns.put(clazz.getName().toUpperCase(), columns);
            }else if(MODE.UPDATE == mode) {
                EntityAdapterProxy.update_columns.put(clazz.getName().toUpperCase(), columns);
            }else if(MODE.DDL == mode) {
                EntityAdapterProxy.ddl_columns.put(clazz.getName().toUpperCase(), columns);
            }
        }
        LinkedHashMap<String, Column> list = new LinkedHashMap();
        list.putAll(columns);
        return list;
    }

    /**
     * 获取指定类.属性关联的列名
     * @param clazz 类
     * @param field 属性
     * @param annotations 根据指定的注解, 以第一个成功取值的注解为准<br/>
     *                    不指定则按默认规则 column.name, column.value, TableField.name, TableField.value, tableId.name, tableId.value, Id.name, Id.value
     *
     * @return String
     */
    default Column column(Class clazz, Field field, String ... annotations) {
        String key = clazz.getName()+":"+field.getName().toUpperCase();
        // 1.缓存
        Column column = EntityAdapterProxy.field2column.get(key.toUpperCase());
        if(null != column) {
            return column;
        }
        String name = null;
        // 2.注解
        if(null == annotations || annotations.length ==0 ) {
            if(BasicUtil.isNotEmpty(ConfigTable.ENTITY_COLUMN_ANNOTATION)) {
                annotations = ConfigTable.ENTITY_COLUMN_ANNOTATION.split(",");
            }else {
                annotations = "column.name, column.value, TableField.name, TableField.value, tableId.name, tableId.value, Id.name, Id.value".split(",");
            }
        }
        name = ClassUtil.parseAnnotationFieldValue(field, annotations);

        // 3.属性名转成列名
        if(BasicUtil.isEmpty(name)) {
            Class c = field.getType();
            //boolean、char、byte、short、int、long、float、double
            if(c == String.class || c == Date.class || ClassUtil.isPrimitiveClass(c)) {
                name = field.getName();
            }
            if(null != name && "camel_".equals(ConfigTable.ENTITY_FIELD_COLUMN_MAP)) {
                name = BeanUtil.camel_(name);
            }
        }


        //创建Column
        if(BasicUtil.isNotEmpty(name)) {
            column = new Column(name);
            EntityAdapterProxy.field2column.put(key.toUpperCase(), column);
            EntityAdapterProxy.column2field.put(clazz.getName().toUpperCase()+":"+name.toUpperCase(), field);
            //类型
            String type = ClassUtil.parseAnnotationFieldValue(field, "column.columnDefinition");
            if(BasicUtil.isNotEmpty(type)) {
                if(type.contains("[]")) {
                    type = type.replace("[]","");
                    column.setArray(true);
                }
                column.setType(type);
            }else{
                //逐个属性解析
                //数据类型
                //@Column(name = "id", type = MySqlTypeConstant.DATETIME, nullAbel=false comment = "是否为超级管理员", defaultValue = "0",length = 10)
                //@TableField(jdbcType = JdbcType.CLOB)
                //length precision scale
                int length = -1;
                String _length = ClassUtil.parseAnnotationFieldValue(field, "column.length", "TableField.length");
                if(BasicUtil.isNotEmpty(_length)) {
                    length = BasicUtil.parseInt(_length, -1);
                }
                String dataType = ClassUtil.parseAnnotationFieldValue(field, "column.type", "column.jdbcType", "TableField.type","TableField.jdbcType");
                if(BasicUtil.isNotEmpty(dataType)) {
                    column.setType(dataType);
                }else {
                    //根据java类型
                    TypeMetadata tm = EntityAdapterProxy.type(ClassUtil.getComponentClass(field));
                    if(null != tm) {
                        if(length == -1 && tm == StandardTypeMetadata.VARCHAR) {
                            length = 255;
                        }
                        column.setTypeMetadata(tm);
                    }else{
                        column.setType("varchar(255)");
                    }
                }
                if(length != -1) {
                    column.setLength(length);
                }
                int precision = -1;
                String _precision = ClassUtil.parseAnnotationFieldValue(field, "column.precision", "TableField.precision");
                if(BasicUtil.isNotEmpty(_precision)) {
                    precision = BasicUtil.parseInt(_precision, -1);
                }
                if(precision != -1) {
                    column.setPrecision(precision);
                }
                int scale = -1;
                String _scale = ClassUtil.parseAnnotationFieldValue(field, "column.scale", "TableField.scale");
                if(BasicUtil.isNotEmpty(_scale)) {
                    scale = BasicUtil.parseInt(_scale, -1);
                }
                if(scale != -1) {
                    column.setScale(scale);
                }
                //默认值
                Object def = ClassUtil.getAnnotationFieldValue(field, "column", "defaultValue");
                if(BasicUtil.isNotEmpty(def)) {
                    column.setDefaultValue(def);
                }
                //非空
                String nullable = ClassUtil.parseAnnotationFieldValue(field, "column.nullAbel", "TableField.nullAbel");
                if(BasicUtil.isNotEmpty(nullable)) {
                    column.setNullable(BasicUtil.parseBoolean(nullable));
                }
                //唯一

                //注释
                String comment = ClassUtil.parseAnnotationFieldValue(field, "column.comment", "TableField.comment");
                if(BasicUtil.isNotEmpty(comment)) {
                    column.setComment(comment);
                }
            }
        }
        return column;
    }

    /**
     * 根据类与列名 获取相关的属性
     * @param clazz 类
     * @param column 列名
     * @return Field
     */

    default Field field(Class clazz, Column column) {
        return field(clazz, column.getName());
    }

    /**
     * 列存放易燃属性
     * @param clazz 类
     * @param column 列名
     * @return 属性
     */
    default Field field(Class clazz, String column) {
        Field field = EntityAdapterProxy.column2field.get(clazz.getName().toUpperCase()+":"+column.toUpperCase());
        if(null == field) {
            fields(clazz);
            field = EntityAdapterProxy.column2field.get(clazz.getName().toUpperCase()+":"+column.toUpperCase());
        }
        //可能是父类属性
        if(null == field || field.getDeclaringClass() != clazz) {
            List<Field> fields = ClassUtil.getFields(clazz, false, false);
            for(Field f:fields) {
                if(f.getName().equalsIgnoreCase(column) && f.getDeclaringClass() == clazz) {
                    field = f;
                    EntityAdapterProxy.column2field.put(clazz.getName().toUpperCase()+":"+column.toUpperCase(), field);
                    break;
                }
            }
        }
        return field;
    }
    default void fields(Class clazz) {
        List<Field> fields = ClassUtil.getFields(clazz, false, false);
        for(Field field:fields) {
            column(clazz, field);
        }
    }

    /**
     * 检测主键(是主键名不是值)<br/>
     * 从primaryKeys中取一个
     * @param clazz 类
     * @return Column
     */
    default Column primaryKey(Class clazz) {
        LinkedHashMap<String, Column> list = primaryKeys(clazz);
        for(Column column:list.values()) {
            return column;
        }
        return new Column(DataRow.DEFAULT_PRIMARY_KEY);
    }

    default PrimaryGenerator generator(String table, Field field) {
        PrimaryGenerator generator = null;
        table = table.toUpperCase();
        if(null == GeneratorConfig.get(table)) {
            Object generatorName = ClassUtil.getAnnotationFieldValue(field, "GeneratedValue","generator");
            if(null != generatorName) {
                String name = generatorName.toString();
                for(PrimaryGenerator.GENERATOR item:PrimaryGenerator.GENERATOR.values()) {
                    if(item.name().equalsIgnoreCase(name)) {
                        generator = item;
                        break;
                    }
                }
            }
            if(null == generator) {
                generator = PrimaryGenerator.GENERATOR.AUTO;
            }
            GeneratorConfig.put(table, generator);
        }
        return generator;
    }
    default PrimaryGenerator generator(Class clazz, Field field) {
        String table = table(clazz).getName().toUpperCase();
        return generator(table, field);
    }

    /**
     * 解析主键生成器(包含当前表及属性关联表)
     * @param clazz clazz
     */
    default void generator(Class clazz) {
        List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "GeneratedValue");
        for(Field field:fields) {
            String table = null;
            try {
                ManyToMany manyToMany = PersistenceAdapter.manyToMany(field);
                if(null != manyToMany) {
                    table = manyToMany.joinTable;
                }
            }catch (Exception e) {
            }
            if(null == table) {
                table = table(clazz).getName();
            }
            generator(table, field);
        }

    }

    /**
     * 检测主键(是主键名不是值)<br/>
     * 根据注解检测主键名s(注解名不区分大小写, 支持模糊匹配如Table*)<br/>
     * 先根据配置文件中的ENTITY_PRIMARY_KEY_ANNOTATION, 如果出现多种主键标识方式可以逗号分隔以先取到的为准<br/>
     * 如果没有检测到再检测注解中带tableId或Id的属性名<br/>
     * 如果没有检测到按默认主键DataRow.DEFAULT_PRIMARY_KEY<br/>
     * @param clazz 类
     * @return List
     */

    default LinkedHashMap<String, Column> primaryKeys(Class clazz) {
        LinkedHashMap<String, Column> list = EntityAdapterProxy.primarys.get(clazz.getName().toUpperCase());
        if(null == list) {
            list = new LinkedHashMap<>();
            String annotations = ConfigTable.ENTITY_PRIMARY_KEY_ANNOTATION;
            if(BasicUtil.isEmpty(annotations)) {
                //如果配置文件中没有指定
                annotations = "tableId, Id";
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
            if (list.isEmpty()) {
                list.put(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase(), new Column(DataRow.DEFAULT_PRIMARY_KEY));
            }
            EntityAdapterProxy.primarys.put(clazz.getName().toUpperCase(), list);
        }

        //解析主键生成器(包含当前表及属性关联表)
        generator(clazz);
        return list;
    }

    /**
     * 主键值
     * @param obj obj
     * @return String
     */
    default Map<String, Object> primaryValue(Object obj) {
        Column primary = primaryKey(obj.getClass());
        Field field = EntityAdapterProxy.column2field.get(obj.getClass().getName().toUpperCase()+":"+primary.getName().toUpperCase());
        Object value = BeanUtil.getFieldValue(obj, field);
        Map<String, Object> map = new HashMap<>();
        map.put(primary.getName().toUpperCase(), value);
        return map;
    }

    /**
     * 生成主键值
     * @param obj entity或DataRow
     * @param inserts 需要插入的列, 注意成功创建主键后需要把主键key添加到inserts中
     * @return boolean 是否成功
     */
    default boolean createPrimaryValue(Object obj, List<String> inserts) {
        return false;
    }
    default boolean createPrimaryValue(Object obj, LinkedHashMap<String, Column> inserts) {
        return false;
    }

    /**
     * 主键值
     * @param obj obj
     * @return Map
     */

    default Map<String, Object> primaryValues(Object obj) {
        LinkedHashMap<String, Column> primarys = primaryKeys(obj.getClass());
        Map<String, Object> map = new HashMap<>();
        for(String primary:primarys.keySet()) {
            Field field = EntityAdapterProxy.column2field.get(obj.getClass().getName().toUpperCase()+":"+primary.toUpperCase());
            Object value = BeanUtil.getFieldValue(obj, field);
            map.put(primary.toUpperCase(), value);
        }
        return map;
    }

    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity<br/>
     * 如果不实现当前可以返回null, 将继续执行默认处理方式<br/>
     * @param clazz 类
     * @param map map
     * @return T
     * @param <T> T
     */

    default <T> T entity(Class<T> clazz, Map<String, Object> map, Map columns) {
        return entity(null, clazz, map, columns);
    }

    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity<br/>
     * 如果不实现当前可以返回null, 将继续执行默认处理方式<br/>
     * @param entity 在此基础上执行, 如果不提供则新创建
     * @param clazz 类
     * @param map map
     * @param metadatas 列属性
     * @return T
     * @param <T> T
     */

    default <T> T entity(T entity, Class<T> clazz, Map<String, Object> map, Map metadatas) {
        List<Field> fields = ClassUtil.getFields(clazz, false, false);
        Map<Field, String> fk = new HashMap<>();
        //entity = BeanUtil.map2object(entity, map, clazz, metadatas, false, true, true);
        if (null == entity) {
            try {
                entity = (T) clazz.newInstance();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        DataRow row = null;
        if(map instanceof DataRow) {
            row = (DataRow) map;
        }
        for(Field field:fields) {
            Object value = null;
            String columnName = field.getName();
            //属性与列同名
            if(null != row) {
                value = row.get(columnName);
            }else{
                value = map.get(columnName);
                if (null == value) {
                    value = map.get(columnName.toUpperCase());
                }
            }

            if(null == value) {
                //根据默认转换规则
                Column column = column(clazz, field);//列名
                columnName = column.getName();
                if (null != row) {
                    value = row.get(columnName);
                } else {
                    value = map.get(columnName);
                    if (null == value) {
                        value = map.get(columnName.toUpperCase());
                    }
                }
            }

            if(null != value) {
                Column metadata = null;  //列属性
                if(map instanceof DataRow) {
                    metadata = ((DataRow)map).getMetadata(columnName);
                }
                if(null == metadata && null != metadatas) {
                    metadata = (Column) metadatas.get(columnName.toUpperCase());
                }
                BeanUtil.setFieldValue(entity, field, metadata, value);

            }
        }
        return entity;
    }

    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null, 将继续执行默认处理方式
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */

    default DataRow row(Object obj, String... keys) {
        return row(null, obj, keys);
    }

    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null, 将继续执行默认处理方式
     * 注意实现时不要调用 DataRow.public static DataRow parse(DataRow row, Object obj, String... keys) 形成无限递归
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    default DataRow row(DataRow row, Object obj, String... keys) {
        // 注意不要调用 DataRow.default static DataRow parse(DataRow row, Object obj, String... keys) 形成无限递归
        return DataRow.parse(row, KeyAdapter.KEY_CASE.CONFIG, obj, keys);
    }

    /**
     * 列名转换成http参数时调用
     * 如果不实现当前可以返回null, 将继续执行默认处理方式
     * @param metadatas metadata
     * @return List
     *
     */
    default List<String> column2param(List<String> metadatas) {
        List<String> params = new ArrayList<>();
        for(String metadata:metadatas) {
            params.add(column2param(metadata));
        }
        return params;
    }

    default String column2param(String metadata) {
        String param = null;
        // 注意这里只支持下划线转驼峰
        // 如果数据库中已经是驼峰, 不要配置这个参数
        String keyCase = ConfigTable.HTTP_PARAM_KEY_CASE;
        if("camel".equals(keyCase)) {
            param = metadata + ":" + BeanUtil.camel(metadata.toLowerCase());
        }else if("Camel".equals(keyCase)) {
            String key = CharUtil.toUpperCaseHeader(metadata.toLowerCase());
            param = metadata+":"+BeanUtil.Camel(key);
        }else if("lower".equalsIgnoreCase(keyCase)) {
            param = metadata + ":" + metadata.toLowerCase();
        }else if("upper".equalsIgnoreCase(keyCase)) {
            param = metadata + ":" + metadata.toUpperCase();
        }else{
            param = metadata + ":" + metadata;
        }
        return param;
    }

    static Object getPrimaryValue(Object obj) {
        if(null == obj) {
            return null;
        }if(obj instanceof DataRow) {
            return  ((DataRow)obj).getPrimaryValue();
        } else if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
            return EntityAdapterProxy.primaryValue(obj);
        }else{
            return BeanUtil.getFieldValue(obj, ConfigTable.DEFAULT_PRIMARY_KEY, true);
        }
    }
    static void setPrimaryValue(Object obj, Object value) {
        if(null == obj) {
            return;
        }
        if(obj instanceof DataRow) {
            DataRow row = (DataRow)obj;
            row.setPrimaryValue(value);
        }else{
            Column key = EntityAdapterProxy.primaryKey(obj.getClass());
            Field field = EntityAdapterProxy.field(obj.getClass(), key);
            BeanUtil.setFieldValue(obj, field, value);
        }
    }
}
