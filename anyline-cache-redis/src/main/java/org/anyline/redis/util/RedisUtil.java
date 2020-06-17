package org.anyline.redis.util;

import org.anyline.util.BeanUtil;
import redis.clients.jedis.JedisCluster;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class RedisUtil {
    private static Hashtable<String, RedisUtil> instances = new Hashtable<String, RedisUtil>();
    private JedisCluster cluster;
    private String prefix = "";
    public static RedisUtil newInstance(String key, JedisCluster cluster, String prefix){
        RedisUtil util = instances.get(key);
        if(null == util){
            util = new RedisUtil();
            util.cluster = cluster;
            util.prefix = prefix;
            instances.put(key,util);
        }
        return util;
    }
    public static RedisUtil getInstance(String key){
        return instances.get(key);
    }

    public String key(String key){
        return prefix + key;
    }
    public void set(String key, String value){
        cluster.set(key(key),value);
    }
    public void setex(String key, int seconds, String value){
        cluster.setex(key(key), seconds, value);
    }
    public String get(String key){
        return cluster.get(key(key));
    }
    public void delete(String key){
        cluster.del(key(key));
    }
    public void string(String key, String value){
        cluster.set(key(key),(String)value);
    }
    public String string(String key){
        return cluster.get(key(key));
    }
    public boolean exists(String key){
        return cluster.exists(key(key));
    }
    public void object(String key, Object value){
        key = key(key);
        if(null != value){
            if(value instanceof String){
                cluster.set(key,(String)value);
            }else{
                cluster.set(key.getBytes(), BeanUtil.serialize(value));
            }
        }
    }
    public Object object(String key){
        Object result = null;
        key = key(key);
        if(cluster.exists(key.getBytes())){
            return null;
        }
        byte[] in = cluster.get(key.getBytes());
        result = BeanUtil.deserialize(in);
        return result;
    }
    public <T> List<T> list(String key){
        return (List<T>)object(key);
    }
    public <T> Map<String,T> map(String key){
        return (Map<String,T>)object(key);
    }

}
