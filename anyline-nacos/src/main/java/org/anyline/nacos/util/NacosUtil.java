/*
 * Copyright 2019-2022 www.anyline.org
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
package org.anyline.nacos.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


public class NacosUtil {
	private static Logger log = LoggerFactory.getLogger(NacosUtil.class);
	private NacosConfig config = null;

	private static Hashtable<String, NacosUtil> instances = new Hashtable<String, NacosUtil>();

	static {
		Hashtable<String, AnylineConfig> configs = NacosConfig.getInstances();
		for(String key:configs.keySet()){
			NacosUtil instance = getInstance(key);
			if(null != instance) {
				instances.put(key, instance);
				instance.scan();
			}
		}
	}

	public static Hashtable<String, NacosUtil> getInstances(){
		return instances;
	}

	public void scan(){
		if(null == config){
			return;
		}

		// ConfigTable
		Listener listener = new Listener() {
			@Override
			public void receiveConfigInfo(String content) {
				log.warn("[nacos reload config][group:{}][namespace:{}][data:{}][class:{}]", config.GROUP, config.NAMESPACE, ConfigTable.CONFIG_NAME, ConfigTable.class.getSimpleName());
				ConfigTable.parse(content);
			}
			@Override
			public Executor getExecutor() {
				return null;
			}
		};
		try {
			config(null, ConfigTable.CONFIG_NAME, listener);
		}catch (Exception e){
			log.warn("[nacos config][result:false][group:{}][namespace:{}][class:{}][msg:{}]", config.GROUP, config.NAMESPACE, ConfigTable.class.getSimpleName(),e.toString());
		}

		// AnylineConfig子类
		Map<String, Map<String,Object>> listenerFiles = AnylineConfig.getListeners();
		for (Map.Entry<String, Map<String,Object>> item : listenerFiles.entrySet()) {
			File file = new File(item.getKey());
			Map<String,Object> params = item.getValue();
			Class<AnylineConfig> clazz = (Class<AnylineConfig>)params.get("CLAZZ");
			Hashtable<String, AnylineConfig> instances = (Hashtable<String, AnylineConfig>)params.get("INSTANCES");
			String[] compatibles = (String[])params.get("COMPATIBLES");
			try {
				config(null, file.getName(), clazz);
			} catch (NacosException e) {
				log.warn("[nacos config][result:false][class:{}][msg:{}]",clazz.getSimpleName(),e.toString());
			}
		}
		// 自动扫描
		if(config.AUTO_SCAN){
			String packages = config.SCAN_PACKAGE;
			if(BasicUtil.isNotEmpty(packages)){
				String[] pks = packages.split(",");
				if(null != pks) {
					for (String pk:pks) {
						List<Class<?>> list = ClassUtil.list(pk, true, AnylineConfig.class, ConfigTable.class);
						for(Class<?> clazz:list){
							@SuppressWarnings("unchecked")
							Class<AnylineConfig> configClass = (Class<AnylineConfig>)clazz;
							String configName = (String)BeanUtil.getFieldValue(clazz, "CONFIG_NAME");
							try {
								config(null, configName, configClass);
							} catch (NacosException e) {
								log.warn("[nacos config][result:false][package:{}][msg:{}]", pk, configName);
							}
						}
					}
				}
			}
			String cls = config.SCAN_CLASS;
			if(BasicUtil.isNotEmpty(cls)){
				String[] clas  = cls.split(",");
				for(String c:clas){
					Class clazz = null;
					try {
						clazz = Class.forName(c);
						@SuppressWarnings("unchecked")
						Class<AnylineConfig> configClass = (Class<AnylineConfig>)clazz;
						String configName = (String)BeanUtil.getFieldValue(clazz, "CONFIG_NAME");
						config(null, configName, configClass);
					}catch (Exception e){
						log.warn("[nacos config][result:false][class:{}][msg:{}]", c,e.toString());
					}
				}
			}
		}
	}
	public static NacosUtil getInstance(){
		return getInstance(NacosConfig.DEFAULT_INSTANCE_KEY);
	}
	public static NacosUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = NacosConfig.DEFAULT_INSTANCE_KEY;
		}
		NacosUtil util = instances.get(key);
		if(null == util){
			util = new NacosUtil();
			NacosConfig config = NacosConfig.getInstance(key);
			if(null != config) {
				util.config = config;
				instances.put(key, util);
			}
		}
		return util;
	}
	public String config(String group, String data, final Class<? extends AnylineConfig> T) throws NacosException{
		if(BasicUtil.isEmpty(group)){
			group = config.GROUP;
		}
		log.warn("[nacos config][group:{}][data:{}][class:{}]", group, data, T.getName());
		final String gp = group;
		final String dt = data;
		Listener listener = new Listener() {
			@Override
			public void receiveConfigInfo(String content) {
				log.warn("[nacos reload config][group:{}][data:{}][class:{}]", gp, dt, T.getName());
				parse(T, content);
			}
			@Override
			public Executor getExecutor() {
				return null;
			}
		};
		String config = config(group,data,listener);
		parse(T, config);
		return config;
	}

	/**
	 * 读取配置文件 并 监听配置更新
	 * @param group group
	 * @param data data
	 * @param listener listent
	 * @return String
	 * @throws NacosException NacosException
	 */
	public String config(String group, String data, Listener listener) throws NacosException{
		if(BasicUtil.isEmpty(group)){
			group = config.GROUP;
		}
		log.warn("[nacos config][group:{}][namespace:{}][data:{}][listener:{}]", group, config.NAMESPACE, data, listener);
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.NAMESPACE, config.NAMESPACE);
		String adr = config.ADDRESS;
		if(!adr.contains(":") && config.PORT > 0){
			adr = adr + ":" + config.PORT;
		}
		properties.put(PropertyKeyConst.SERVER_ADDR, adr);
		ConfigService configService = NacosFactory.createConfigService(properties);
		String content = configService.getConfig(data, group, config.TIMEOUT);
		if(null != listener) {
			configService.addListener(data, group, listener);
		}
		return content;
	}
	public String config(String data){
		try{
			Listener listener = null;
			return config(null, data, listener);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public String config(String group, String data){
		try{
			Listener listener = null;
			return config(group, data, listener);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static void parse(Class<? extends AnylineConfig> T, String content) {
		if(BasicUtil.isEmpty(content)){
			log.warn("[nacos config][pull fail][config class:{}]",T.getSimpleName());
			return;
		}
		try{
		    Class<?> clazz = Class.forName(T.getName());
		    Method method = clazz.getMethod("parse", String.class);
		    method.invoke(null, content);
		}catch(Exception e){
			
		}
	}
}
