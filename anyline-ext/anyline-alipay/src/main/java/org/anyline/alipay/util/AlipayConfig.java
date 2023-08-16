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


package org.anyline.alipay.util;
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
 
public class AlipayConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();

	public static String DEFAULT_APP_PRIVATE_KEY 	= ""		;
	public static String DEFAULT_PLATFORM_PUBLIC_KEY= ""		;
	public static String DEFAULT_APP_ID 			= ""		;
	public static String DEFAULT_DATA_FORMAT 		= "json"	;
	public static String DEFAULT_ENCODE 			= "utf-8"	;
	public static String DEFAULT_SIGN_TYPE			= "RSA"		;
	public static String DEFAULT_RETURN_URL			= ""		;
	public static String DEFAULT_NOTIFY_URL			= ""		;


	public String APP_PRIVATE_KEY 	= DEFAULT_APP_PRIVATE_KEY;
	public String PLATFORM_PUBLIC_KEY = DEFAULT_PLATFORM_PUBLIC_KEY;
	public String APP_ID 			= DEFAULT_APP_ID;
	public String DATA_FORMAT 		= DEFAULT_DATA_FORMAT;
	public String ENCODE 			= DEFAULT_ENCODE;
	public String SIGN_TYPE 		= DEFAULT_SIGN_TYPE;
	public String RETURN_URL		= DEFAULT_RETURN_URL;
	public String NOTIFY_URL		= DEFAULT_NOTIFY_URL;

	public static String CONFIG_NAME = "anyline-alipay.xml";

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
		parse(AlipayConfig.class, content, instances ,compatibles); 
	}


	public static AlipayConfig parse(String key, DataRow row){
		return parse(AlipayConfig.class, key, row, instances,compatibles);
	}
	public static Hashtable<String,AnylineConfig> parse(String column, DataSet set){
		for(DataRow row:set){
			String key = row.getString(column);
			parse(key, row);
		}
		return instances;
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件
		load(); 
	} 
	public static AlipayConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static AlipayConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - AlipayConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载
			load(); 
		} 
		 
		return (AlipayConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() {
		load(instances, AlipayConfig.class, CONFIG_NAME);
		AlipayConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	public String getString(String key){
		return kvs.get(key); 
	} 
	private static void debug(){
	}

	public static AlipayConfig register(String instance, DataRow row){
		AlipayConfig config = parse(AlipayConfig.class, instance, row, instances,compatibles);
		AlipayUtil.getInstance(instance);
		return config;
	}
} 
