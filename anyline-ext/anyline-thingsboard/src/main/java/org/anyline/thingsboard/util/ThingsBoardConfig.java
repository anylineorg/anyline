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

    public static Hashtable<String,AnylineConfig>getInstances(){
        return instances;
    }
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
        // 加载配置文件 
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

    public static ThingsBoardConfig register(String instance, DataRow row) {
        ThingsBoardConfig config = parse(ThingsBoardConfig.class, instance, row, instances, compatibles);
        ThingsBoardClient.getInstance(instance);
        return config;
    }

    public static ThingsBoardConfig register(DataRow row) {
        return register(DEFAULT_INSTANCE_KEY, row);
    }
    public static ThingsBoardConfig register(String instance, String host, String account, String password) {
        return register(instance, host, account, null);
    }
    public static ThingsBoardConfig register(String instance, String host, String account, String password, String tenant) {
        DataRow row = new DataRow();
        row.put("HOST", host);
        row.put("ACCOUNT", account);
        row.put("PASSWORD", password);
        row.put("TENANT", tenant);
        ThingsBoardConfig config = parse(ThingsBoardConfig.class, instance, row, instances, compatibles);
        return config;
    }
}
