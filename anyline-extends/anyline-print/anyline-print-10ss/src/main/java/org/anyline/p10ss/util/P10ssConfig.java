/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.p10ss.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;

public class P10ssConfig  extends AnylineConfig {
    private static Hashtable<String, AnylineConfig> instances = new Hashtable<>();


    public static String DEFAULT_APP_ID                = "" ;
    public static String DEFAULT_APP_SECRET            = "" ;
    public static String DEFAULT_TYPE                  = "0"; // 0:自用 1:开放
    public static String DEFAULT_ACCESS_TOKEN_SERVER   = "" ;



    public String APP_ID                = DEFAULT_APP_ID                ;
    public String APP_SECRET            = DEFAULT_APP_SECRET            ;
    public String TYPE                  = DEFAULT_TYPE                  ; // 0:自用 1:开放
    public String ACCESS_TOKEN_SERVER   = DEFAULT_ACCESS_TOKEN_SERVER   ;


    private static File configDir;
    public static String CONFIG_NAME = "anyline-p10ss.xml";

    public static enum URL{
        ACCESS_TOKEN		{public String getCode(){return "https://open-api.10ss.net/oauth/oauth";} 	        public String getName(){return "ACCESS TOKEN";}},
        ADD_PRINTER	        {public String getCode(){return "https://open-api.10ss.net/printer/addprinter";}    public String getName(){return "添加自用打印机";}},
        PRINT_TEXT	        {public String getCode(){return "https://open-api.10ss.net/print/index";}    public String getName(){return "打印文本";}};
        public abstract String getName();
        public abstract String getCode();
    };

    public static Hashtable<String,AnylineConfig>getInstances(){
        return instances;
    }
    static{
        init();
        debug();
    }
    /**
     * 解析配置文件内容
     * @param content 配置文件内容
     */
    public static void parse(String content){
        parse(P10ssConfig.class, content, instances ,compatibles);
    }
    /**
     * 初始化默认配置文件
     */
    public static void init() {
        // 加载配置文件
        load();
    }
    public static P10ssConfig getInstance(){
        return getInstance(DEFAULT_INSTANCE_KEY);
    }
    public static P10ssConfig getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = DEFAULT_INSTANCE_KEY;
        }

        if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - P10ssConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
            // 重新加载
            load();
        }
        return (P10ssConfig)instances.get(key);
    }

    public static P10ssConfig reg(String key, DataRow row){
        return parse(P10ssConfig.class, key, row, instances,compatibles);
    }
    public static P10ssConfig parse(String key, DataRow row){
        return parse(P10ssConfig.class, key, row, instances,compatibles);
    }
    public static Hashtable<String,AnylineConfig> parse(String column, DataSet set){
        for(DataRow row:set){
            String key = row.getString(column);
            parse(key, row);
        }
        return instances;
    }
    /**
     * 加载配置文件
     * 首先加载anyline-config.xml
     * 然后加载anyline开头的xml文件并覆盖先加载的配置
     */
    private synchronized static void load() {
        load(instances, P10ssConfig.class,CONFIG_NAME ,compatibles);
        P10ssConfig.lastLoadTime = System.currentTimeMillis();
    }
    private static void debug(){
    }
    public P10ssConfig register(String instance, DataRow row){
        P10ssConfig config = parse(P10ssConfig.class, instance, row, instances, compatibles);
        return config;
    }
    public P10ssConfig register(DataRow row){
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public P10ssConfig register(String app, String secret){
        DataRow row = new DataRow();
        row.put("APP_ID", app);
        row.put("APP_SECRET",secret);
        return register(row);
    }
}
