package org.anyline.proxy;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.data.util.ClientHolder;
import org.anyline.entity.DataRow;
import org.anyline.metadata.Column;
import org.anyline.metadata.Tag;
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
    private static Map<String,Map<String,String>>  cache_names = new HashMap<>();
    private static Map<String,DataRow> cache_table_maps = new HashMap<>();
    private static Map<String,DataRow> cache_view_maps = new HashMap<>();
    public static void name(String catalog, String schema, String name, String origin){
        String group_key = catalog + "_" + schema;
        group_key = group_key.toUpperCase();
        Map<String,String> maps = cache_names.get(group_key);
        if(null == maps){
            maps = new HashMap<>();
            cache_names.put(group_key, maps);
        }
        maps.put(name.toUpperCase(), origin);
    }
    public static Map<String, String> names(String catalog, String schema){
        String key = catalog + "_" + schema;
        return cache_names.get(key.toUpperCase());
    }
    public static String name(boolean greedy, String catalog, String schema, String name){
        if(null == name){
            return null;
        }
        String group_key = catalog + "_" + schema;
        group_key = group_key.toUpperCase();
        Map<String,String> maps = cache_names.get(group_key);
        if(null != maps){
            String origin = maps.get(name.toUpperCase());
            if(null != origin){
                return origin;
            }
        }
        if(greedy) {
            for (Map<String, String> names : cache_names.values()) {
                String origin = names.get(name.toUpperCase());
                if(null != origin){
                    return origin;
                }
            }
        }
        return null;
    }
    public static String datasource(String datasource){
        if(null == datasource || "common".equalsIgnoreCase(datasource)){
            datasource = ClientHolder.curDataSource();
        }
        if(null == datasource){
            datasource = "default";
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
        if(null == columns){
            columns = new LinkedHashMap<>();
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
        if(null == tags){
            tags = new LinkedHashMap<>();
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

        cache_table_maps.clear();
        cache_view_maps.clear();
        cache_names.clear();
    }


}
