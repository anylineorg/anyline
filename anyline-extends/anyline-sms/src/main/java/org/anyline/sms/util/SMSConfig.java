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


package org.anyline.sms.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;

import java.util.Hashtable;
 
public class SMSConfig extends AnylineConfig {
	private static Hashtable<String, AnylineConfig> instances = new Hashtable<String, AnylineConfig>();

	public static String DEFAULT_SERVER_HOST 	= "";
	public static String DEFAULT_APP_KEY 		= "";
	public static String DEFAULT_APP_SECRET 	= "";
	public static String DEFAULT_TENANT_CODE 	= "";
	public static String DEFAULT_SIGN 			= "";
	public static String DEFAULT_PLATFORM_CODE 	= "";


	public String SERVER_HOST 	= DEFAULT_SERVER_HOST	;
	public String APP_KEY 		= DEFAULT_APP_KEY		;
	public String APP_SECRET 	= DEFAULT_APP_SECRET	;
	public String TENANT_CODE 	= DEFAULT_TENANT_CODE	;
	public String SIGN 			= DEFAULT_SIGN			;
	public String PLATFORM_CODE = DEFAULT_PLATFORM_CODE	;

	public static String CONFIG_NAME = "anyline-sms.xml";

	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
	static {
		init();
		debug();
	}

	/**
	 * 解析配置文件内容
	 *
	 * @param content 配置文件内容
	 */
	public static void parse(String content) {
		parse(SMSConfig.class, content, instances, compatibles);
	}

	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件
		load();
	}

	public static SMSConfig getInstance() {
		return getInstance(DEFAULT_INSTANCE_KEY);
	}

	public static SMSConfig getInstance(String key) {
		if (BasicUtil.isEmpty(key)) {
			key = DEFAULT_INSTANCE_KEY;
		}
		return (SMSConfig) instances.get(key);
	}

	/**
	 * 加载配置文件
	 */
	private synchronized static void load() {
		load(instances, SMSConfig.class, CONFIG_NAME);
	}

	private static void debug() {
	}

	public static SMSConfig register(String instance, DataRow row) {
		SMSConfig config = parse(SMSConfig.class, instance, row, instances, compatibles);
		return config;
	}
	public static SMSConfig register(String instance, String key, String secret) {
		DataRow row = new DataRow();
		row.put("ACCESS_KEY", key);
		row.put("ACCESS_SECRET", secret);
		SMSConfig config = parse(SMSConfig.class, instance, row, instances, compatibles);
		return config;
	}
}
