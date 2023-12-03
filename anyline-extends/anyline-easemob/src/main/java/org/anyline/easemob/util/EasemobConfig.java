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


package org.anyline.easemob.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
 
public class EasemobConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-easemob.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<>();

	public static String DEFAULT_HOST 			= "";
	public static String DEFAULT_CLIENT_ID 		= "";
	public static String DEFAULT_CLIENT_SECRET 	= "";
	public static String DEFAULT_ORG_NAME 		= "";
	public static String DEFAULT_APP_NAME 		= "";


	public String HOST 			= DEFAULT_HOST			;
	public String CLIENT_ID 	= DEFAULT_CLIENT_ID		;
	public String CLIENT_SECRET = DEFAULT_CLIENT_SECRET	;
	public String ORG_NAME 		= DEFAULT_ORG_NAME		;
	public String APP_NAME 		= DEFAULT_APP_NAME		;

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
		parse(EasemobConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件 
		load(); 
	} 
	public static EasemobConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static EasemobConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - EasemobConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载 
			load(); 
		} 
		return (EasemobConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() {
		load(instances, EasemobConfig.class, CONFIG_NAME);
		EasemobConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	 
	private static void debug(){
	}
	public static EasemobConfig register(String instance, DataRow row){
		EasemobConfig config = parse(EasemobConfig.class, instance, row, instances, compatibles);
		EasemobUtil.getInstance(instance);
		return config;
	}
	public static EasemobConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
} 
