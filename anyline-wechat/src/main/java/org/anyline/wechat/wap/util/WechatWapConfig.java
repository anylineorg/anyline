/*
 * Copyright 2015-2022 www.anyline.org
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
 *
 *
 */
package org.anyline.wechat.wap.util;
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.wechat.util.WechatConfig;

import java.util.Hashtable;
 
 
public class WechatWapConfig extends WechatConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
	public static String CONFIG_NAME = "anyline-wechat-wap.xml";
 
	static{ 
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(WechatWapConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
 
	public static WechatWapConfig getInstance(){
		return getInstance("default"); 
	} 
	public static WechatWapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WechatWapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载 
			load(); 
		} 
		return (WechatWapConfig)instances.get(key);
	} 
 
	public static WechatWapConfig parse(String key, DataRow row){
		return parse(WechatWapConfig.class, key, row, instances, compatibles);
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
	 */ 
	private synchronized static void load() { 
		load(instances, WechatWapConfig.class, CONFIG_NAME,compatibles);
		WechatWapConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){ 
	} 
} 
