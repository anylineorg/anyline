package org.anyline.proxy;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.data.entity.Column;
import org.anyline.data.entity.Tag;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.entity.DataRow;
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


    private static Map<String,DataRow> cache_metadata = new HashMap<>();
    private static Map<String,DataRow> cache_metadatas = new HashMap<>();

    public static LinkedHashMap<String, Column> columns(String table){
        if(null == table){
            return null;
        }
        LinkedHashMap<String, Column> columns = null;
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.curDataSource()+"_COLUMNS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            CacheElement cacheElement = provider.get(cache, key);
            if(null != cacheElement){
                columns = (LinkedHashMap<String, Column>) cacheElement.getValue();
            }
        }else{
            // 通过静态变量缓存
            DataRow static_cache = cache_metadatas.get(key);
            if(null != static_cache && (ConfigTable.TABLE_METADATA_CACHE_SECOND <0 || !static_cache.isExpire(ConfigTable.TABLE_METADATA_CACHE_SECOND*1000))) {
                columns = (LinkedHashMap<String, Column>) static_cache.get("keys");
            }
        }
        return columns;
    }
    public static void columns(String table, LinkedHashMap<String, Column> columns){
        if(null == table){
            return;
        }
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.curDataSource()+"_COLUMNS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            provider.put(cache, key, columns);
        }else{
            DataRow static_cache = new DataRow();
            static_cache.put("keys", columns);
            cache_metadatas.put(key, static_cache);
        }
    }

    public static void clearColumnCache(String catalog, String schema, String table){
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.curDataSource()+"_COLUMNS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED) {
            provider.remove(cache, key);
        }else{
            cache_metadatas.remove(key);
        }
    }
    public static void clearColumnCache(String table){
        clearColumnCache(null, null, table);
    }
    public static void clearColumnCache(){
        if(null != provider && !ConfigTable.IS_CACHE_DISABLED) {
            String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
            if(BasicUtil.isNotEmpty(cache)) {
                provider.clear(cache);
            }
        }else{
            cache_metadatas.clear();
        }
    }



    public static LinkedHashMap<String, Tag> tags(String table){
        if(null == table){
            return null;
        }
        LinkedHashMap<String, Tag> tags = null;
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.curDataSource()+"_TAGS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            CacheElement cacheElement = provider.get(cache, key);
            if(null != cacheElement){
                tags = (LinkedHashMap<String, Tag>) cacheElement.getValue();
            }
        }else{
            // 通过静态变量缓存
            DataRow static_cache = cache_metadatas.get(key);
            if(null != static_cache && (ConfigTable.TABLE_METADATA_CACHE_SECOND <0 || !static_cache.isExpire(ConfigTable.TABLE_METADATA_CACHE_SECOND*1000))) {
                tags = (LinkedHashMap<String, Tag>) static_cache.get("keys");
            }
        }
        return tags;
    }
    public static void tags(String table, LinkedHashMap<String, Tag> tags){
        if(null == table){
            return;
        }
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.curDataSource()+"_TAGS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED){
            provider.put(cache, key, tags);
        }else{
            DataRow static_cache = new DataRow();
            static_cache.put("keys", tags);
            cache_metadatas.put(key, static_cache);
        }
    }
    public static void clearTagCache(String catalog, String schema, String table){
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.curDataSource()+"_TAGS_" + table.toUpperCase();
        if(null != provider && BasicUtil.isNotEmpty(cache) && !ConfigTable.IS_CACHE_DISABLED) {
            provider.remove(cache, key);
        }else{
            cache_metadatas.remove(key);
        }
    }
}
