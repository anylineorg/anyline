package org.anyline.qq.map.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class QQMapConfig extends AnylineConfig {
    public static String CONFIG_NAME = "anyline-qq-map.xml";
    private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
    public static String HOST           = "https://apis.map.qq.com";
    public static String DEFAULT_KEY    = ""			    ;
    public static String DEFAULT_SECRET = ""				;


    public String KEY			 = DEFAULT_KEY				;
    public String SECRET 	     = DEFAULT_SECRET			;
    static{
        init();
        debug();
    }
    public static Hashtable<String,AnylineConfig>getInstances(){
        return instances;
    }
    /**
     * 解析配置文件内容
     * @param content 配置文件内容
     */
    public static void parse(String content){
        parse(QQMapConfig.class, content, instances ,compatibles);
    }
    /**
     * 初始化默认配置文件
     */
    public static void init() {
        //加载配置文件 
        load();
    }

    public static QQMapConfig getInstance(){
        return getInstance(DEFAULT_INSTANCE_KEY);
    }
    public static QQMapConfig getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = DEFAULT_INSTANCE_KEY;
        }

        if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - QQMapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
            //重新加载 
            load();
        }
        return (QQMapConfig)instances.get(key);
    }
    /**
     * 加载配置文件 
     * 首先加载anyline-config.xml 
     * 然后加载anyline开头的xml文件并覆盖先加载的配置 
     */
    private synchronized static void load() {
        load(instances, QQMapConfig.class, CONFIG_NAME);
        QQMapConfig.lastLoadTime = System.currentTimeMillis();
    }
    private static void debug(){
    }
    public static QQMapConfig register(String instance, DataRow row){
        QQMapConfig config = parse(QQMapConfig.class, instance, row, instances, compatibles);
        QQMapUtil.getInstance(instance);
        return config;
    }
    public static QQMapConfig register(DataRow row){
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public static QQMapConfig register(String instance, String key, String secret){
        DataRow row = new DataRow();
        row.put("KEY", key);
        row.put("SECRET", secret);
        return register(instance, row);
    }
    public static QQMapConfig register(String ak, String sk){
        return register(DEFAULT_INSTANCE_KEY, ak, sk);
    }
}
