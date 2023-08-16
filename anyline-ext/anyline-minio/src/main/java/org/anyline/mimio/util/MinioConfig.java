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


package org.anyline.mimio.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;

public class MinioConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	private static File configDir;

	public static String DEFAULT_ACCESS_KEY		= ""	;
	public static String DEFAULT_ACCESS_SECRET	= ""	;
	public static String DEFAULT_ENDPOINT		= ""	;
	public static String DEFAULT_BUCKET			= ""	;
	public static int DEFAULT_PART_SIZE			= 1		;
	public static String DEFAULT_DIR			= ""	;
	public static int DEFAULT_EXPIRE_SECOND 	= 3600	;


	public String ACCESS_KEY	= DEFAULT_ACCESS_KEY	;
	public String ACCESS_SECRET = DEFAULT_ACCESS_SECRET	;
	public String ENDPOINT		= DEFAULT_ENDPOINT		;
	public String BUCKET		= DEFAULT_BUCKET		;
	public int PART_SIZE		= DEFAULT_PART_SIZE		;
	public String DIR			= DEFAULT_DIR			;
	public int EXPIRE_SECOND 	= DEFAULT_EXPIRE_SECOND	;



	public static String CONFIG_NAME = "anyline-minio.xml";



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
		parse(MinioConfig.class, content, instances ,compatibles); 
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
	public static MinioConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static MinioConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - MinioConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载
			load(); 
		} 
		return (MinioConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() {
		load(instances, MinioConfig.class, CONFIG_NAME);
		MinioConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){
	}

	public static MinioConfig register(String instance, DataRow row){
		MinioConfig config = parse(MinioConfig.class, instance, row, instances, compatibles);
		MinioUtil.getInstance(instance);
		return config;
	}
	public static MinioConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
} 
