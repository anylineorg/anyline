package org.anyline.redis.util;

import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Hashtable;

public class RedisProvider {
    private static Hashtable<String, RedisProvider> instances = new Hashtable();
    @Autowired
    private RedisTemplate template;

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
