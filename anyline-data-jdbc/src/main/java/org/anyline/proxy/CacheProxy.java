package org.anyline.proxy;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.entity.DataRow;
import org.anyline.entity.data.Column;
import org.anyline.entity.data.Tag;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("org.anyline.cache.proxy")
public class CacheProxy {

    public static CacheProvider provider;
    public CacheProxy(){}
    @Autowired(required = false)
    @Qualifier("anyline.cache.provider")
    public void init(CacheProvider provider) {
        CacheProxy.provider = provider;
    }


    private static Map<String,DataRow> cache_columns = new HashMap<>();
    private static Map<String,DataRow> cache_table_maps = new HashMap<>();
    private static Map<String,DataRow> cache_view_maps = new HashMap<>();
    public static String datasource(String datasource){
        if(null == datasource || "common".equalsIgnoreCase(datasource)){
            datasource = DataSourceHolder.curDataSource();
        }
        return datasource.toUpperCase();
    }

    public static String tableName(String datasource, String name){
        DataRow row = cache_table_maps.get(datasource(datasource));
        if(null != row){
            return row.getString(name);
        }
        return name;
    }
    public static String viewName(String datasource, String name){
        DataRow row = cache_view_maps.get(datasource(datasource));
        if(null != row){
            return row.getString(name);
        }
        return name;
    }
    public static void setTableMaps(String datasource, DataRow maps){
        cache_table_maps.put(datasource(datasource), maps);
    }
    public static void setViewMaps(String datasource, DataRow maps){
        cache_view_maps.put(datasource(datasource), maps);
    }

    /**
     * 表缓存
     * @param datasource 数据源名_TYPES crm_TABLE  crm_VIEW
     * @return DataRow
     */
    public static DataRow getTableMaps(String datasource){
        DataRow row = cache_table_maps.get(datasource(datasource));
        if(null == row){
            row = new DataRow();
            cache_table_maps.put(datasource(datasource), row);
        }
        return row;
    }
    /**
     * 视图缓存
     * @param datasource 数据源名_TYPES crm_TABLE  crm_VIEW
     * @return DataRow
     */
    public static DataRow getViewMaps(String datasource){
        DataRow row = cache_view_maps.get(datasource(datasource));
        if(null == row){
            row = new DataRow();
            cache_view_maps.put(datasource(datasource), row);
        }
        return row;
    }

    /**
     * 表或视图的列
     * @param table 表名或视图表 贪婪模式下会带前缀 catalog.schema.table
     * @return LinkedHashMap
     */
    public  static  <T extends Column> LinkedHashMap<String, T> columns(String datasource, String table){
        if(null == table){
            return null;
        }
        LinkedHashMap<String, T> columns = null;
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = datasource(datasource)+"_COLUMNS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            CacheElement cacheElement = provider.get(cache, key);
            if(null != cacheElement){
                columns = (LinkedHashMap<String, T>) cacheElement.getValue();
            }
        }else{
            // 通过静态变量缓存
            DataRow static_cache = cache_columns.get(key);
            if(null != static_cache && (ConfigTable.TABLE_METADATA_CACHE_SECOND <0 || !static_cache.isExpire(ConfigTable.TABLE_METADATA_CACHE_SECOND*1000))) {
                columns = (LinkedHashMap<String, T>) static_cache.get("keys");
            }
        }
        return columns;
    }

    /**
     * 设置缓存
     * @param table 表
     * @param columns 列
     */
    public static  <T extends Column> void columns(String datasource, String table, LinkedHashMap<String, T> columns){
        if(null == table){
            return;
        }
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = datasource(datasource) + "_COLUMNS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            provider.put(cache, key, columns);
        }else{
            DataRow static_cache = new DataRow();
            static_cache.put("keys", columns);
            cache_columns.put(key, static_cache);
        }
    }



    /**
     * 表或视图的Tag
     * @param table 表名或视图表 贪婪模式下会带前缀 catalog.schema.table
     * @return LinkedHashMap
     */
    public static <T extends Tag> LinkedHashMap<String, T> tags(String datasource, String table){
        if(null == table){
            return null;
        }
        LinkedHashMap<String, T> tags = null;
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = datasource(datasource)+"_TAGS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            CacheElement cacheElement = provider.get(cache, key);
            if(null != cacheElement){
                tags = (LinkedHashMap<String, T>) cacheElement.getValue();
            }
        }else{
            // 通过静态变量缓存
            DataRow static_cache = cache_columns.get(key);
            if(null != static_cache && (ConfigTable.TABLE_METADATA_CACHE_SECOND <0 || !static_cache.isExpire(ConfigTable.TABLE_METADATA_CACHE_SECOND*1000))) {
                tags = (LinkedHashMap<String, T>) static_cache.get("keys");
            }
        }
        return tags;
    }

    /**
     * 设置缓存
     * @param table 表
     * @param tags Tag
     */
    public static <T extends Tag> void tags(String datasource, String table, LinkedHashMap<String, T> tags){
        if(null == table){
            return;
        }
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = datasource(datasource) + "_TAGS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            provider.put(cache, key, tags);
        }else{
            DataRow static_cache = new DataRow();
            static_cache.put("keys", tags);
            cache_columns.put(key, static_cache);
        }
    }

    public static void clear(){
        if(null != provider && !ConfigTable.IS_CACHE_DISABLED) {
            String cache = ConfigTable.TABLE_METADATA_CACHE_KEY;
            if(BasicUtil.isNotEmpty(cache)) {
                provider.clear(cache);
            }
        }else{
            cache_columns.clear();
        }
    }


}
