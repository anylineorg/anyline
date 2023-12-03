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


package org.anyline.baidu.map.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class BaiduMapConfig extends AnylineConfig {
    public static String CONFIG_NAME = "anyline-baidu-map.xml";
    private static Hashtable<String,AnylineConfig> instances = new Hashtable<>();

    public static String DEFAULT_AK = ""				;
    public static String DEFAULT_SK = ""				;


    public String AK			 = DEFAULT_AK				;
    public String SK 			 = DEFAULT_SK				;
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
        parse(BaiduMapConfig.class, content, instances ,compatibles);
    }
    /**
     * 初始化默认配置文件
     */
    public static void init() {
        // 加载配置文件 
        load();
    }

    public static BaiduMapConfig getInstance(){
        return getInstance(DEFAULT_INSTANCE_KEY);
    }
    public static BaiduMapConfig getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = DEFAULT_INSTANCE_KEY;
        }

        if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - BaiduMapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
            // 重新加载 
            load();
        }
        return (BaiduMapConfig)instances.get(key);
    }
    /**
     * 加载配置文件 
     * 首先加载anyline-config.xml 
     * 然后加载anyline开头的xml文件并覆盖先加载的配置 
     */
    private synchronized static void load() {
        load(instances, BaiduMapConfig.class, CONFIG_NAME);
        BaiduMapConfig.lastLoadTime = System.currentTimeMillis();
    }
    private static void debug(){
    }
    public static BaiduMapConfig register(String instance, DataRow row){
        BaiduMapConfig config = parse(BaiduMapConfig.class, instance, row, instances, compatibles);
        BaiduMapClient.getInstance(instance);
        return config;
    }
    public static BaiduMapConfig register(DataRow row){
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public static BaiduMapConfig register(String instance,  String ak, String sk){
        DataRow row = new DataRow();
        row.put("AK", ak);
        row.put("SK", sk);
        return register(instance, row);
    }
    public static BaiduMapConfig register(String ak, String sk){
        return register(DEFAULT_INSTANCE_KEY, ak, sk);
    }
}
