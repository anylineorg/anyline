package org.anyline.baidu.seo.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class BaiduSeoConfig extends AnylineConfig {
    public static String CONFIG_NAME = "anyline-baidu-seo.xml";
    private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();

    public static String DEFAULT_SITE = ""				;
    public static String DEFAULT_TOKEN = ""				;


    public String SITE	         = DEFAULT_SITE				; //站点URL如 http://www.anyline.org
    public String TOKEN 		 = DEFAULT_TOKEN			;
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
        parse(BaiduSeoConfig.class, content, instances ,compatibles);
    }
    /**
     * 初始化默认配置文件
     */
    public static void init() {
        // 加载配置文件 
        load();
    }

    public static BaiduSeoConfig getInstance(){
        return getInstance(DEFAULT_INSTANCE_KEY);
    }
    public static BaiduSeoConfig getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = DEFAULT_INSTANCE_KEY;
        }

        if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - BaiduSeoConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
            // 重新加载 
            load();
        }
        return (BaiduSeoConfig)instances.get(key);
    }
    /**
     * 加载配置文件 
     * 首先加载anyline-config.xml 
     * 然后加载anyline开头的xml文件并覆盖先加载的配置 
     */
    private synchronized static void load() {
        load(instances, BaiduSeoConfig.class, CONFIG_NAME);
        BaiduSeoConfig.lastLoadTime = System.currentTimeMillis();
    }
    private static void debug(){
    }
    public static BaiduSeoConfig register(String instance, DataRow row){
        BaiduSeoConfig config = parse(BaiduSeoConfig.class, instance, row, instances, compatibles);
        BaiduSeoClient.getInstance(instance);
        return config;
    }
    public static BaiduSeoConfig register(DataRow row){
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public static BaiduSeoConfig register(String instance, String site, String token){
        DataRow row = new DataRow();
        row.put("SITE", site);
        row.put("TOKEN", token);
        return register(instance, row);
    }
    public static BaiduSeoConfig register(String site, String token){
        return register(DEFAULT_INSTANCE_KEY, site, token);
    }
}
