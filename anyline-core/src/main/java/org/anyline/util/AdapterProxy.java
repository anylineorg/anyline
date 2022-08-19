package org.anyline.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.EntityAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Component("entity.adatper.proxy")
public class AdapterProxy {

    public static boolean hasAdapter(){
        return null != adapter;
    }

    public static EntityAdapter adapter;
    public static Map<String,EntityAdapter> adapters;



    @Autowired(required = false)
    public void setAdapter(Map<String,EntityAdapter> adapters) {
        String defaultKey = "anyline.entity.adapter";
        if(ConfigTable.getBoolean("IS_DISABLED_DEFAULT_ENTITY_ADAPTER", false)){
            adapters.remove(defaultKey);
        }else{
            adapter = adapters.get(defaultKey);
        }
        AdapterProxy.adapters = adapters;
        for (String key:adapters.keySet()){
            if(!key.equals(defaultKey)){
                adapter = adapters.get(key);
            }
        }

    }


    /**
     * 获取指定类关联的表名
     * @param clazz 类
     * @return String
     */
    public static String table(Class clazz){
        if(null != adapter){
            return adapter.table(clazz);
        }
        return null;
    }

    /**
     * 获取指定类的列名s
     * @param clazz 类
     * @return List
     */
    public static List<String> columns(Class clazz){
        if(null != adapter){
            return adapter.columns(clazz);
        }
        return null;
    }

    /**
     * 获取指定类.属性关联的列名
     * @param clazz 类
     * @param field 属性
     * @return String
     */
    public static String column(Class clazz, Field field){
        if(null != adapter){
            return adapter.column(clazz, field);
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
        if(null != adapter){
            return adapter.field(clazz, column);
        }
        return null;
    }

    /**
     * 获取clazz类相关的主键
     * @param clazz 类
     * @return String
     */
    public static String primaryKey(Class clazz){
        if(null != adapter){
            return adapter.primaryKey(clazz);
        }
        return null;
    }

    /**
     * 获取clazz类相关的主键s
     * @param clazz 类
     * @return List
     */
    public static List<String> primaryKeys(Class clazz){
        if(null != adapter){
            return adapter.primaryKeys(clazz);
        }
        return null;
    }

    /**
     * 主键值
     * @param obj obj
     * @return String
     */
    public static Map<String,Object> primaryValue(Object obj){
        if(null != adapter){
            return adapter.primaryValue(obj);
        }
        return null;
    }
    /**
     * 主键值
     * @param obj obj
     * @return Map
     */
    public static Map<String,Object> primaryValues(Object obj){
        if(null != adapter){
            return adapter.primaryValues(obj);
        }
        return null;
    }

    /**
     * 生成主键值
     * @param obj obj
     */
    public static void createPrimaryValue(Object obj){
        if(null != adapter){
            adapter.createPrimaryValue(obj);
        }
    }
    /**
     * DataRow转换成entity时调用  如果有实现则不再执行 DataRow.entity
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param clazz 类
     * @param map map
     * @return T
     * @param <T> T
     */
    public static <T> T entity(Class<T> clazz, Map<String,Object> map){
        if(null != adapter){
        return adapter.entity(clazz, map);
    }
        return null;
}


    /**
     * entity转换成DataRow时调用 如果有实现则不再执行DataRow.parse
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param obj obj
     * @param keys keys
     * @return DataRow
     */
    public static DataRow parse(Object obj, String ... keys){
        if(null != adapter){
            return adapter.parse(obj, keys);
        }
        return null;
    }


    /**
     * 列名转换成http参数时调用
     * 如果不实现当前可以返回null,将继续执行默认处理方式
     * @param metadata metadata
     * @return List
     *
     */
    public static List<String> metadata2param(List<String> metadata){
        if(null != adapter){
            return adapter.metadata2param(metadata);
        }
        return null;
    }


}
