package org.anyline.redis.util;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Hashtable;

@Component("anyline.redis.provider")
public class RedisProvider  implements CacheProvider {
    private static Hashtable<String, RedisProvider> instances = new Hashtable();
    @Autowired
    private RedisTemplate template;
    @Override
    public CacheElement get(String channel, String key) {
        return null;
    }

    @Override
    public void put(String channel, String key, Object value) {

    }

    @Override
    public boolean remove(String channel, String key) {
        return false;
    }

    @Override
    public boolean clear(String channel) {
        return false;
    }

    @Override
    public boolean clears() {
        return false;
    }

    @Override
    public HashSet<String> channels() {
        return null;
    }

    @Override
    public int getLvl() {
        return 2;
    }



    private String prefix = ConfigTable.getString("REDIS_PREFIX");
    public static RedisProvider newInstance(String key, String prefix){
        RedisProvider instance = instances.get(key);
        if(null == instance){
            instance = new RedisProvider();
            if(null != prefix) {
                instance.prefix = prefix;
            }
            if(null == instance.prefix){
                instance.prefix = "";
            }
            instances.put(key,instance);
        }
        return instance;
    }
    public static RedisProvider newInstance(String key){
        return newInstance(key,ConfigTable.getString("REDIS_PREFIX"));
    }
    public static RedisProvider getInstance(String key){
        return newInstance(key);
    }

    public static RedisProvider getInstance(){
        return newInstance(ConfigTable.getString("REDIS_PREFIX"));
    }
    public String key(String key){
        return prefix+key;
    }

}
