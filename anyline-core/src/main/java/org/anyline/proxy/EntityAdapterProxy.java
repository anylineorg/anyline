package org.anyline.proxy;

import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.KeyAdapter;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.data.Column;
import org.anyline.entity.data.Table;
import org.anyline.util.ConfigTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Component("anyline.entity.adapter.proxy")
public class EntityAdapterProxy {

    public static boolean hasAdapter(){
        return null != adapter;
    }

    public static EntityAdapter adapter;
    public static Map<String,EntityAdapter> adapters;





    /**
     * 获取指定类关联的表名
     * @param adapter adapter
     * @param clazz 类
     * @return String
     */
    public static Table table(EntityAdapter adapter, Class clazz){
        if(null != adapter){
            return adapter.table(clazz);
        }
        return null;
    }

    /**
     * Entity对应的表名
     * @param clazz 类
     * @return 表
     */
    public static Table table(Class clazz){
        return table(adapter, clazz);
    }

    /**
     * 获取指定类的列名s
     * @param adapter adapter
     * @param clazz 类
     * @param mode insert/update/ddl
     * @return List
     */
    public static LinkedHashMap<String, Column> columns(EntityAdapter adapter, Class clazz, EntityAdapter.MODE mode){
        if(null != adapter){
            return adapter.columns(clazz, mode);
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
        return columns(adapter, clazz, mode);
    }
    /**
     * 获取指定类.属性关联的列名
     * @param adapter adapter
     * @param clazz 类
     * @param field 属性
     * @return String
     */
    public static Column column(EntityAdapter adapter, Class clazz, Field field){
        if(null != adapter){
            return adapter.column(clazz, field);
        }
        return null;
    }
    public static Column column(Class clazz, Field field){
        return column(adapter, clazz, field);
    }

    /**
     * 根据类与列名 获取相关的属性
     * @param adapter adapter
     * @param clazz 类
     * @param column 列名
     * @return Field
     */
    public static Field field(EntityAdapter adapter, Class clazz, String column){
        if(null != adapter){
            return adapter.field(clazz, column);
        }
        return null;
    }
    public static Field field(Class clazz, String column){
        return field(adapter, clazz, column);
    }
    public static Field field(Class clazz, Column column){
        return field(adapter, clazz, column.getName());
    }

    /**
     * 获取clazz类相关的主键
     * @param adapter adapter
     * @param clazz 类
     * @return String
     */
    public static Column primaryKey(EntityAdapter adapter, Class clazz){
        if(null != adapter){
            return adapter.primaryKey(clazz);
        }
        return null;
    }
    public static Column primaryKey(Class clazz){
        return primaryKey(adapter, clazz);
    }

    public static String primaryKey(Class clazz, boolean simple){
        Column column = primaryKey(adapter, clazz);
        if(null != column){
            return column.getName();
        }
        return null;
    }

    /**
     * 获取clazz类相关的主键s
     * @param adapter adapter
     * @param clazz 类
     * @return List
     */
    public static LinkedHashMap<String, Column> primaryKeys(EntityAdapter adapter, Class clazz){
        if(null != adapter){
            return adapter.primaryKeys(clazz);
        }
        return null;
    }
    public static LinkedHashMap<String, Column> primaryKeys(Class clazz){
        return primaryKeys(adapter, clazz);
    }
    public static List<String> primaryKeys(Class clazz, boolean simple){
        LinkedHashMap<String, Column> cols =  primaryKeys(adapter, clazz);
        List<String> list = new ArrayList<>();
        if(null != cols){
            for(Column col:cols.values()){
                list.add(col.getName());
            }
        }
        return list;
    }

    /**
     * 主键值
     * @param adapter adapter
     * @param obj obj
     * @return String
     */
    public static Map<String,Object> primaryValue(EntityAdapter adapter, Object obj){
        if(null != adapter){
            return adapter.primaryValue(obj);
        }
        return null;
    }
    public static Map<String,Object> primaryValue(Object obj){
        return primaryValue(adapter, obj);
    }
    /**
     * 主键值
     * @param adapter adapter
     * @param obj obj
     * @return Map
     */
    public static Map<String,Object> primaryValues(EntityAdapter adapter, Object obj){
        if(null != adapter){
            return adapter.primaryValues(obj);
        }
        return null;
    }

    public static Map<String,Object> primaryValues(Object obj){
        return primaryValues(adapter, obj);
    }

    /**
     * 生成主键值
     * @param adapter adapter
     * @param obj entity或DataRow
     * @param inserts 需要插入的列,注意成功创建主键后需要把主键key添加到inserts中
     * @return boolean 是否成功
     */
    public static boolean createPrimaryValue(EntityAdapter adapter, Object obj, List<String> inserts){
        if(null != adapter){
            return adapter.createPrimaryValue(obj, inserts);
        }
        return false;
    }
    public static boolean createPrimaryValue(Object obj, List<String> inserts){
        return createPrimaryValue(adapter, obj, inserts);
    }
    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param adapter adapter
     * @param entity 在此基础上执行,如果不提供则新创建
     * @param clazz 类
     * @param map map
     * @return T
     * @param <T> T
     */
    public static <T> T entity(EntityAdapter adapter, T entity,  Class<T> clazz, Map<String,Object> map, LinkedHashMap columns){
        if(null != adapter){
            return adapter.entity(entity, clazz, map, columns);
        }
        return null;
    }
    public static <T> T entity(Class<T> clazz, Map<String,Object> map, LinkedHashMap columns){
        T entity =  entity(adapter, null, clazz, map, columns);
        for(EntityAdapter item : adapters.values()){
            entity = entity(item, entity, clazz, map, columns);
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
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param adapter adapter
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    public static DataRow row(EntityAdapter adapter, DataRow row,  Object obj, String ... keys){
        if(null == row){
            row = new DataRow();
        }
        if(null != adapter){
            row = adapter.row(row, obj, keys);
        }
        return row;
    }
    public static DataRow row(Object obj, String ... keys){
        return row(null, obj, keys);
    }
    public static DataRow row(DataRow row, Object obj, String ... keys){
        if(!ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER){
            row = DataRow.parse(row, KeyAdapter.KEY_CASE.CONFIG, obj, keys);
            if(null != adapter){
                row = row(adapter, row, obj, keys);
            }
        }
        for(EntityAdapter item:adapters.values()){
            row = row(item, row, obj, keys);
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
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param adapter adapter
     * @param metadatas metadatas
     * @return List
     *
     */
    public static List<String> column2param(EntityAdapter adapter, List<String> metadatas){
        if(null != adapter){
            return adapter.column2param(metadatas);
        }
        return null;
    }
    public static String column2param(EntityAdapter adapter, String metadata){
        if(null != adapter){
            return adapter.column2param(metadata);
        }
        return null;
    }

    public static List<String> column2param(List<String> metadatas){
        // 如果有用户设置的adapter
        if(null != adapters && adapters.size()>0){
            return adapters.get(adapters.size()-1).column2param(metadatas);
        }

        // 如果没有禁用默认adapter
        if(!ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER && null != adapter){
            return column2param(adapter, metadatas);
        }
        return metadatas;
    }

    public static String column2param(String metadata){
        // 如果有用户设置的adapter
        if(null != adapters && adapters.size()>0){
            return adapters.get(adapters.size()-1).column2param(metadata);
        }

        // 如果没有禁用默认adapter
        if(!ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER && null != adapter){
            return column2param(adapter, metadata);
        }
        return metadata;
    }


}
