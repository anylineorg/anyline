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



package org.anyline.proxy;

import org.anyline.adapter.EntityAdapter;
import org.anyline.annotation.Autowired;
import org.anyline.annotation.Component;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.geometry.Point;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component("anyline.entity.adapter.proxy")
public class EntityAdapterProxy {

    public static LinkedHashMap<String, Table> class2table                              = new LinkedHashMap<>();  // class.name > table.name
    public static LinkedHashMap<String, Column> field2column                            = new LinkedHashMap<>();  // class.name:field.name > column.name
    public static LinkedHashMap<String, Field> column2field                             = new LinkedHashMap<>();  // column.name > field
    public static LinkedHashMap<String, LinkedHashMap<String, Column>> primarys         = new LinkedHashMap<>();  // 主键
    public static LinkedHashMap<String, LinkedHashMap<String, Column>> insert_columns   = new LinkedHashMap<>();
    public static LinkedHashMap<String, LinkedHashMap<String, Column>> update_columns   = new LinkedHashMap<>();
    public static LinkedHashMap<String, LinkedHashMap<String, Column>> ddl_columns      = new LinkedHashMap<>();

    public static Map<Class, List<EntityAdapter>> adapters = new HashMap<>();

    //Java类型与sql类型对应
    public static Map<Class, TypeMetadata> types = new HashMap<>();
    static {
        types.put(int.class, StandardTypeMetadata.INT);
        types.put(Integer.class, StandardTypeMetadata.INT);
        types.put(long.class, StandardTypeMetadata.BIGINT);
        types.put(Long.class, StandardTypeMetadata.BIGINT);
        types.put(double.class, StandardTypeMetadata.DOUBLE);
        types.put(Double.class, StandardTypeMetadata.DOUBLE);
        types.put(float.class, StandardTypeMetadata.FLOAT);
        types.put(Float.class, StandardTypeMetadata.FLOAT);
        types.put(BigDecimal.class, StandardTypeMetadata.DECIMAL);
        types.put(short.class, StandardTypeMetadata.INT);
        types.put(Short.class, StandardTypeMetadata.INT);
        types.put(String.class, StandardTypeMetadata.VARCHAR);
        types.put(Date.class, StandardTypeMetadata.DATETIME);
        types.put(LocalDate.class, StandardTypeMetadata.DATE);
        types.put(LocalTime.class, StandardTypeMetadata.TIME);
        types.put(LocalDateTime.class, StandardTypeMetadata.DATETIME);
        types.put(byte[].class, StandardTypeMetadata.BLOB);
        types.put(boolean.class, StandardTypeMetadata.BOOLEAN);
        types.put(Boolean.class, StandardTypeMetadata.BOOLEAN);
        types.put(Point.class, StandardTypeMetadata.POINT);
    }
    public static TypeMetadata type(Class clazz){
        return types.get(clazz);
    }
    /**
     * 清空缓存
     */
    public static void clear(){
        class2table     = new LinkedHashMap<>();
        field2column    = new LinkedHashMap<>();
        column2field    = new LinkedHashMap<>();
        primarys        = new LinkedHashMap<>();
        insert_columns  = new LinkedHashMap<>();
        update_columns  = new LinkedHashMap<>();
    }
    public static boolean hasAdapter(Class calzz){
        return !getAdapters(calzz).isEmpty();
    }
    public static List<EntityAdapter> getAdapters(Class type){
        List<EntityAdapter> list = new ArrayList<>();
        for(Class clazz:adapters.keySet()){
            if(ClassUtil.isInSub(type, clazz)){
                list.addAll(adapters.get(clazz));
            }
        }
        return list;
    }
    @Autowired(required = false)
    public static void setAdapters(Map<String, EntityAdapter> adapters) {
        //是否禁用默认adapter
        String defaultKey = "anyline.entity.adapter.default";
        if(ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER ){
            adapters.remove(defaultKey);
        }
        for (EntityAdapter adapter : adapters.values()) {
            Class type = adapter.type();
            push(type, adapter);
            List<Class> types = adapter.types();
            if(null != types){
                for(Class t:types){
                    push(t, adapter);
                }
            }
        }
        for(List<EntityAdapter> list:EntityAdapterProxy.adapters.values()){
            EntityAdapter.sort(list);
        }
    }
    public static void push(EntityAdapter adapter){
        push(Object.class, adapter);
    }
    public static void push(Class type, EntityAdapter adapter){
        if(null != type){
            List<EntityAdapter> list = EntityAdapterProxy.adapters.get(type);
            if(null == list){
                list = new ArrayList<>();
                EntityAdapterProxy.adapters.put(type, list);
            }
            list.add(adapter);
        }
    }

    /**
     * Entity对应的表名
     * @param clazz 类
     * @return 表
     */
    public static Table table(Class clazz){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            Table table = adapter.table(clazz);
            if(null != table){
                return table;
            }
        }
        return null;
    }
    public static String table(Class clazz, boolean simple){
        Table table = table(clazz);
        if(null != table){
            return table.getName();
        }
        return null;
    }
    /**
     * 获取指定类.属性关联的列名
     * @param clazz 类
     * @param mode insert/update/ddl
     * @return List
     */
    public static LinkedHashMap<String, Column> columns(Class clazz, EntityAdapter.MODE mode){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            LinkedHashMap<String, Column> columns = adapter.columns(clazz, mode);
            if(null != columns && !columns.isEmpty()){
                return columns;
            }
        }
        return new LinkedHashMap<>();
    }
    /**
     * 获取指定类.属性关联的列名
     * @param clazz 类
     * @param field 属性
     * @return String
     */
    public static Column column(Class clazz, Field field){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            Column column = adapter.column(clazz, field);
            if(null != column){
                return column;
            }
        }
        return null;
    }

    public static String column(Class clazz, Field field, boolean simple){
        Column column = column(clazz, field);
        if(null != column){
            return column.getName();
        }
        return null;
    }
    /**
     * 根据类与列名 获取相关的属性
     * @param clazz 类
     * @param column 列名
     * @return Field
     */
    public static Field field(Class clazz, String column){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            Field field = adapter.field(clazz, column);
            if(null != field){
                return field;
            }
        }
        return null;
    }
    public static Field field(Class clazz, Column column){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            Field field = adapter.field(clazz, column);
            if(null != field){
                return field;
            }
        }
        return null;
    }

    /**
     * 获取clazz类相关的主键
     * @param clazz 类
     * @return String
     */
    public static Column primaryKey(Class clazz){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            Column column = adapter.primaryKey(clazz);
            if(null != column){
                return column;
            }
        }
        return null;
    }

    public static String primaryKey(Class clazz, boolean simple){
        Column column = primaryKey(clazz);
        if(null != column){
            return column.getName();
        }
        return null;
    }

    /**
     * 获取clazz类相关的主键s
     * @param clazz 类
     * @return List
     */
    public static LinkedHashMap<String, Column> primaryKeys(Class clazz){
        List<EntityAdapter> list = getAdapters(clazz);
        for(EntityAdapter adapter:list){
            LinkedHashMap<String, Column> pks = adapter.primaryKeys(clazz);
            if(null != pks && !pks.isEmpty()){
                return pks;
            }
        }
        return new LinkedHashMap();
    }
    public static List<String> primaryKeys(Class clazz, boolean simple){
        LinkedHashMap<String, Column> pks =  primaryKeys(clazz);
        List<String> list = new ArrayList<>();
        if(null != pks){
            for(Column col:pks.values()){
                list.add(col.getName());
            }
        }
        return list;
    }

    /**
     * 主键值
     * @param obj obj
     * @return String
     */
    public static Map<String, Object> primaryValue(Object obj){
        List<EntityAdapter> list = getAdapters(obj.getClass());
        for(EntityAdapter adapter:list){
            Map<String, Object> value = adapter.primaryValue(obj);
            if(null != value && !value.isEmpty()){
                return value;
            }
        }
        return new LinkedHashMap();
    }
    /**
     * 主键值
     * @param obj obj
     * @return Map
     */

    public static Map<String, Object> primaryValues(Object obj){
        List<EntityAdapter> list = getAdapters(obj.getClass());
        for(EntityAdapter adapter:list){
            Map<String, Object> value = adapter.primaryValues(obj);
            if(null != value && !value.isEmpty()){
                return value;
            }
        }
        return new LinkedHashMap();
    }

    /**
     * 生成主键值
     * @param obj entity或DataRow
     * @param inserts 需要插入的列, 注意成功创建主键后需要把主键key添加到inserts中
     * @return boolean 是否成功
     */
    public static boolean createPrimaryValue(Object obj, List<String> inserts){
        List<EntityAdapter> list = getAdapters(obj.getClass());
        for(EntityAdapter adapter:list){
            boolean create = adapter.createPrimaryValue(obj, inserts);
            if(create){
                return create;
            }
        }
        return false;
    }
    public static boolean createPrimaryValue(Object obj, LinkedHashMap<String, Column> inserts){
        List<EntityAdapter> list = getAdapters(obj.getClass());
        for(EntityAdapter adapter:list){
            boolean create = adapter.createPrimaryValue(obj, inserts);
            if(create){
                return create;
            }
        }
        return false;
    }
    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity
     * 如果不实现当前可以返回null, 将继续执行默认处理方式
     * @param clazz 类
     * @param map map
     * @param <T> T
     * @return T
     */
    public static <T> T entity(Class<T> clazz, Map<String, Object> map, LinkedHashMap columns){
        List<EntityAdapter> list = getAdapters(clazz);
        T entity = null;
        for(EntityAdapter adapter : list){
            entity = adapter.entity(entity, clazz, map, columns);
        }
        return entity;
    }

    public static <T> EntitySet<T> entitys(Class<T> clazz, DataSet set, LinkedHashMap columns){
        EntitySet<T> entitys = new EntitySet<>();
        if(null != set){
            for(DataRow row:set){
                T entity = entity(clazz, row, columns);
                entitys.add(entity);
            }
            entitys.setNavi(set.getNavi());
        }
        return entitys;
    }

    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null, 将继续执行默认处理方式
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    public static DataRow row(Object obj, String ... keys){
        return row(null, obj, keys);
    }
    public static DataRow row(DataRow row, Object obj, String ... keys){
        if(null == obj){
            return row;
        }
        List<EntityAdapter> list = getAdapters(obj.getClass());
        for(EntityAdapter adapter:list){
            row = adapter.row(row, obj, keys);
        }
        return row;
    }
    public static DataSet set(EntitySet entitys, String ... keys){
        DataSet set = new DataSet();
        if(null != entitys){
            for(Object obj:entitys){
                DataRow row = row(obj, keys);
                set.addRow(row);
            }
            set.setNavi(entitys.getNavi());
        }
        return set;
    }

    /**
     * 列名转换成http参数时调用
     * 如果不实现当前可以返回null, 将继续执行默认处理方式
     * @param metadatas metadatas
     * @return List
     *
     */
    public static List<String> column2param(List<String> metadatas){
        for(List<EntityAdapter> list:adapters.values()){
            for(EntityAdapter adapter:list){
                List<String> params = adapter.column2param(metadatas);
                if(null != params && !params.isEmpty()){
                    return params;
                }
            }
        }
        return metadatas;
    }

    public static String column2param(String metadata){
        for(List<EntityAdapter> list:adapters.values()){
            for(EntityAdapter adapter:list){
                String param = adapter.column2param(metadata);
                if(BasicUtil.isNotEmpty(param)){
                    return param;
                }
            }
        }
        return metadata;
    }

}
