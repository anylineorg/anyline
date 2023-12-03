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


package org.anyline.qq.mp.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
 
public class QQMPConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-qq-mp.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<>();

	public static String DEFAULT_APP_ID = ""				; // AppID(应用ID)
	public static String DEFAULT_API_KEY = ""				; // APPKEY(应用密钥)
	public static String DEFAULT_OAUTH_REDIRECT_URL		; // 登录成功回调URL

	/** 
	 * 服务号相关信息 
	 */ 
	public String APP_ID			 = DEFAULT_APP_ID				; // AppID(应用ID)
	public String API_KEY 			 = DEFAULT_API_KEY				; // APPKEY(应用密钥)
	public String OAUTH_REDIRECT_URL = DEFAULT_OAUTH_REDIRECT_URL	; // 登录成功回调URL

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
		parse(QQMPConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件
		load(); 
	} 
 
	public static QQMPConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static QQMPConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - QQMPConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载
			load(); 
		} 
		return (QQMPConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() {
		load(instances, QQMPConfig.class, CONFIG_NAME);
		QQMPConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){
	}
	public static QQMPConfig register(String instance, DataRow row){
		QQMPConfig config = parse(QQMPConfig.class, instance, row, instances, compatibles);
		QQMPUtil.getInstance(instance);
		return config;
	}
	public static QQMPConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
	public static QQMPConfig register(String instance,  String app, String key, String redirect){
		DataRow row = new DataRow();
		row.put("DEFAULT_APP_ID", app);
		row.put("DEFAULT_API_KEY", key);
		row.put("OAUTH_REDIRECT_URL", redirect);
		return register(instance, row);
	}
	public static QQMPConfig register(String app, String key, String redirect){
		return register(DEFAULT_INSTANCE_KEY, app, key, redirect);
	}
} 
