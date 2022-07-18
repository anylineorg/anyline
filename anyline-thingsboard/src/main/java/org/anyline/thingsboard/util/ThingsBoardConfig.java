package org.anyline.thingsboard.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;

import java.io.File;
import java.util.Hashtable;

public class ThingsBoardConfig extends AnylineConfig{

    private static Hashtable<String, AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
    private static File configDir;

    public static String DEFAULT_ACCOUNT		= "";
    public static String DEFAULT_PASSWORD       = "";
    public static String DEFAULT_HOST		    = "";
    public static String DEFAULT_TENANT		    = "";


    public String ACCOUNT		= DEFAULT_ACCOUNT   ;
    public String PASSWORD      = DEFAULT_PASSWORD  ;
    public String HOST		    = DEFAULT_HOST      ;
    public String TENANT	    = DEFAULT_TENANT    ;
    
    public static String CONFIG_NAME = "anyline-thingsboard.xml";

    static {
        init();
        debug();
    }

    /**
     * 解析配置文件内容
     *
     * @param content 配置文件内容
     */
    public static void parse(String content) {
        parse(ThingsBoardConfig.class, content, instances, compatibles);
    }

    /**
     * 初始化默认配置文件
     */
    public static void init() {
        //加载配置文件 
        load();
    }

    public static ThingsBoardConfig getInstance() {
        return getInstance(DEFAULT_INSTANCE_KEY);
    }

    public static ThingsBoardConfig getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = DEFAULT_INSTANCE_KEY;
        }
        return (ThingsBoardConfig) instances.get(key);
    }

    /**
     * 加载配置文件
     */
    private synchronized static void load() {
        load(instances, ThingsBoardConfig.class, CONFIG_NAME);
    }

    private static void debug() {
    }

    public static ThingsBoardConfig register(String id, DataRow row) {
        ThingsBoardConfig config = parse(ThingsBoardConfig.class, id, row, instances, compatibles);
        return config;
    }

    public static ThingsBoardConfig register(DataRow row) {
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public static ThingsBoardConfig register(String id, String host, String account, String password) {
        return register(id, host, account, null);
    }
    public static ThingsBoardConfig register(String id, String host, String account, String password, String tenant) {
        DataRow row = new DataRow();
        row.put("HOST", host);
        row.put("ACCOUNT", account);
        row.put("PASSWORD", password);
        row.put("TENANT", tenant);
        ThingsBoardConfig config = parse(ThingsBoardConfig.class, id, row, instances, compatibles);
        return config;
    }
}
