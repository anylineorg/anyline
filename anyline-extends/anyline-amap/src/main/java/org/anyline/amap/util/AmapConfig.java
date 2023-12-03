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


package org.anyline.amap.util;
 
import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;
 
public class AmapConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<>();
	public static String DEFAULT_YUNTU_HOST		= "http://yuntuapi.amap.com";
	public static String DEFAULT_HOST			= "https://restapi.amap.com";
	public static String DEFAULT_KEY			= "";
	public static String DEFAULT_SECRET 		= "";
	public static String DEFAULT_TABLE 			= "";

	public String YUNTU_HOST= DEFAULT_YUNTU_HOST;
	public String HOST		= DEFAULT_HOST      ;
	public String KEY		= DEFAULT_KEY		;
	public String SECRET 	= DEFAULT_SECRET	;
	public String TABLE 	= DEFAULT_TABLE		;

	private static File configDir;
	public static String CONFIG_NAME = "anyline-amap.xml";

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
		parse(AmapConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件 
		load(); 
	} 
	public static void setConfigDir(File dir){
		configDir = dir; 
		init(); 
	} 
	public static AmapConfig getInstance(){
		return getInstance(DEFAULT_KEY); 
	} 
	public static AmapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_KEY; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - AmapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载 
			load(); 
		} 
		return (AmapConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() {
		load(instances, AmapConfig.class, CONFIG_NAME);
		AmapConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){
	}

	public static AmapConfig register(String instance, DataRow row) {
		AmapConfig config = parse(AmapConfig.class, instance, row, instances, compatibles);
		return config;
	}
	public static AmapConfig register(String instance, String key, String secret, String table) {
		DataRow row = new DataRow();
		row.put("KEY", key);
		row.put("SECRET",secret);
		row.put("TABLE", table);
		return register(instance, row);
	}
	public static AmapConfig register(String key, String secret, String table) {
		return register(DEFAULT_INSTANCE_KEY, key, secret, table);
	}
	public static AmapConfig register(String key, String secret) {
		return register(DEFAULT_INSTANCE_KEY, key, secret, null);
	}
} 
