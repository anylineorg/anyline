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


package org.anyline.wechat.program;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.wechat.util.WechatConfig;

import java.util.Hashtable;


public class WechatProgramConfig extends WechatConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<>();
	public static String CONFIG_NAME = "anyline-wechat-programe.xml";
	 
	static{
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(WechatProgramConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件 
		load(); 
	}

	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
	public static WechatProgramConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static WechatProgramConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WechatProgramConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载 
			load(); 
		} 
		return (WechatProgramConfig)instances.get(key);
	} 
 
	public static WechatProgramConfig reg(String key, DataRow row){
		return parse(WechatProgramConfig.class, key, row, instances,compatibles);
	} 
	public static WechatProgramConfig parse(String instance, DataRow row){
		WechatProgramConfig config = parse(WechatProgramConfig.class, instance, row, instances,compatibles);
		WechatProgramUtil.getInstance(instance);
		return config;
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
		load(instances, WechatProgramConfig.class, CONFIG_NAME,compatibles);
		WechatProgramConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){
	}

	public static WechatProgramConfig register(String key, DataRow row){
		return parse(WechatProgramConfig.class, key, row, instances,compatibles);
	}
	public static WechatProgramConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
} 
