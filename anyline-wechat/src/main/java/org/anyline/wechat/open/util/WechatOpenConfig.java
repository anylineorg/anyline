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
package org.anyline.wechat.open.util;
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.wechat.util.WechatConfig;

import java.util.Hashtable;
 
 
public class WechatOpenConfig extends WechatConfig{
	public static String CONFIG_NAME = "anyline-wechat-open.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	static{ 
		init(); 
		debug(); 
	} 
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
 
	public static WechatOpenConfig getInstance(){
		return getInstance("default"); 
	} 
	public static WechatOpenConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WechatOpenConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载 
			load(); 
		} 
		return (WechatOpenConfig)instances.get(key);
	} 
 
	public static WechatOpenConfig parse(String key, DataRow row){
		return parse(WechatOpenConfig.class, key, row, instances, compatibles);
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
		load(instances, WechatOpenConfig.class, CONFIG_NAME,compatibles);
		WechatOpenConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){ 
	} 
} 
