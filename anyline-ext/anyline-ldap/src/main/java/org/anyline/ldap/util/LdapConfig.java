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


package org.anyline.ldap.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class LdapConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-ldap.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();


	public static String DEFAULT_ADDRESS					;
	public static int DEFAULT_PORT = 389					;
	public static String DEFAULT_DOMAIN						;
	public static String DEFAULT_ROOT						;
	public static String DEFAULT_SECURITY_AUTHENTICATION	;
	public static String DEFAULT_URL						; // ldap:{ADDRESS}:{PORT}
	public static int DEFAULT_CONNECT_TIMEOUT = 0			;
	public static int DEFAULT_READ_TIMEOUT = 0				;



	public String ADDRESS					= DEFAULT_ADDRESS 					;
	public int PORT 						= DEFAULT_PORT						;
	public String DOMAIN					= DEFAULT_DOMAIN					;
	public String ROOT						= DEFAULT_ROOT						;
	public String SECURITY_AUTHENTICATION	= DEFAULT_SECURITY_AUTHENTICATION	;
	public String URL						= DEFAULT_URL						; // ldap:{ADDRESS}:{PORT}
	public int CONNECT_TIMEOUT 				= DEFAULT_CONNECT_TIMEOUT			;
	public int READ_TIMEOUT 				= DEFAULT_READ_TIMEOUT				;

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
		parse(LdapConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件 
		load();
	} 
 
	public static LdapConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static LdapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - LdapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载 
			load(); 
		}
		LdapConfig config = (LdapConfig)instances.get(key);
		if(null == config.URL){
			config.URL = "ldap://" + config.ADDRESS + ":" + config.PORT;
		}
		return config;
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() {
		load(instances, LdapConfig.class, CONFIG_NAME);
		LdapConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){
	}
	public static LdapConfig register(String instance, DataRow row){
		LdapConfig config = parse(LdapConfig.class, instance, row, instances, compatibles);
		LdapUtil.getInstance(instance);
		return config;
	}
	public static LdapConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
}
