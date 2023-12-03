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


package org.anyline.tencent.cos;
 
import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;

public class COSConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<>(); 
	private static File configDir;

	public static String DEFAULT_ACCESS_ID		= ""	; // 
	public static String DEFAULT_ACCESS_SECRET 	= ""	; // 
	public static String DEFAULT_ENDPOINT		= ""	; // 
	public static String DEFAULT_BUCKET			= ""	; // 
	public static String DEFAULT_DIR			= ""	; // 
	public static int DEFAULT_EXPIRE_SECOND 	= 3600	; // 


	public String ACCESS_ID		= DEFAULT_ACCESS_ID				; // 
	public String ACCESS_SECRET = DEFAULT_ACCESS_SECRET			; // 
	public String ENDPOINT		= DEFAULT_ENDPOINT				; // 
	public String BUCKET		= DEFAULT_BUCKET				; // 
	public String DIR			= DEFAULT_DIR					; // 
	public int EXPIRE_SECOND 	= DEFAULT_EXPIRE_SECOND			; // 
	public static String CONFIG_NAME = "anyline-tencent-cos.xml"; // 

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
		parse(COSConfig.class, content, instances ,compatibles); 
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
	public static COSConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static COSConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - COSConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载 
			load(); 
		} 
		return (COSConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() {
		load(instances, COSConfig.class, CONFIG_NAME);
		COSConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){
	}
	public static COSConfig register(String instance, DataRow row){
		COSConfig config = parse(COSConfig.class, instance, row, instances, compatibles);
		return config;
	}
	public static COSConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
} 
