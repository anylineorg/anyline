package org.anyline.data.datasource;

import java.util.*;

public class DataSourceKeyMap {
    public static Map<String, HashSet<String>> maps = new HashMap<>();
    static {
        reg("username","userName","user");
        reg("url","jdbcUrl","uri","uris","host","hosts");
        reg("driverClass","driverClassName");//,"driver" 表示一个对象
        reg("IdleTimeout","idleTimeoutMs");
        reg("maxLifetime","maxLifetimeMs");
        reg("maxPoolSize","maximumPoolSize");
        reg("validationTimeout","validationTimeoutMs");
        reg("datasourceJndiName","jndiDataSource");
        reg("transactionIsolationName","transactionIsolation");
    }
    public static HashSet<String> alias(String key){
        return maps.get(key);
    }
    public static void reg(String ... keys){
        for(String key:keys){
            HashSet<String> list = maps.get(key);
            if(null == list){
                list = new HashSet<>();
                maps.put(key, list);
            }
            for (String item:keys){
                if(!key.equals(item) && !list.contains(item)){
                    list.add(item);
                }
            }
        }
    }

}
