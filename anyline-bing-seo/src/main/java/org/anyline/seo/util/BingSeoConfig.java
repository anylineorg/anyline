package org.anyline.seo.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class BingSeoConfig extends AnylineConfig {
    public static String CONFIG_NAME = "anyline-bing-seo.xml";
    private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();

    public static String DEFAULT_SITE = ""				;
    public static String DEFAULT_KEY = ""				;


    public String SITE	     = DEFAULT_SITE				; //站点URL如 http://www.anyline.org
    public String KEY 		 = DEFAULT_KEY			;
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
        parse(BingSeoConfig.class, content, instances ,compatibles);
    }
    /**
     * 初始化默认配置文件
     */
    public static void init() {
        // 加载配置文件 
        load();
    }

    public static BingSeoConfig getInstance(){
        return getInstance(DEFAULT_INSTANCE_KEY);
    }
    public static BingSeoConfig getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = DEFAULT_INSTANCE_KEY;
        }

        if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - BingSeoConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
            // 重新加载 
            load();
        }
        return (BingSeoConfig)instances.get(key);
    }
    /**
     * 加载配置文件 
     * 首先加载anyline-config.xml 
     * 然后加载anyline开头的xml文件并覆盖先加载的配置 
     */
    private synchronized static void load() {
        load(instances, BingSeoConfig.class, CONFIG_NAME);
        BingSeoConfig.lastLoadTime = System.currentTimeMillis();
    }
    private static void debug(){
    }
    public static BingSeoConfig register(String instance, DataRow row){
        BingSeoConfig config = parse(BingSeoConfig.class, instance, row, instances, compatibles);
        BingSeoClient.getInstance(instance);
        return config;
    }
    public static BingSeoConfig register(DataRow row){
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public static BingSeoConfig register(String instance, String site, String key){
        DataRow row = new DataRow();
        row.put("SITE", site);
        row.put("KEY", key);
        return register(instance, row);
    }
    public static BingSeoConfig register(String site, String key){
        return register(DEFAULT_INSTANCE_KEY, site, key);
    }
}
