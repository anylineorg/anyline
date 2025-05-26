/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.cache.ehcache;

import net.sf.ehcache.CacheManager;
import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

public class EHCacheConfig extends AnylineConfig {
	private static Hashtable<String, AnylineConfig> instances = new Hashtable<String, AnylineConfig>();
	public static String CONFIG_NAME = "anyline-ehcache.xml";

	public static Hashtable<String,AnylineConfig>getInstances() {
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
		CacheManager.create(new ByteArrayInputStream(content.getBytes()));
	}
	

	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件
		load();
	}

	public static EHCacheConfig getInstance() {
		return getInstance(DEFAULT_INSTANCE_KEY);
	}

	public static EHCacheConfig getInstance(String key) {
		if (BasicUtil.isEmpty(key)) {
			key = DEFAULT_INSTANCE_KEY;
		}
		return (EHCacheConfig) instances.get(key);
	}

	/**
	 * 加载配置文件
	 */
	private synchronized static void load() {
		load(instances, EHCacheConfig.class, CONFIG_NAME);
	}

	private static void debug() {
	}

	public static EHCacheConfig register(String instance, DataRow row) {
		EHCacheConfig config = parse(EHCacheConfig.class, instance, row, instances, compatibles);
		return config;
	}
	public static EHCacheConfig register(String instance, String key, String secret) {
		DataRow row = new DataRow();
		row.put("ACCESS_KEY", key);
		row.put("ACCESS_SECRET", secret);
		EHCacheConfig config = parse(EHCacheConfig.class, instance, row, instances, compatibles);
		return config;
	}
}
