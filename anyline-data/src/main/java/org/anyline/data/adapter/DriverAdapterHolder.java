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



package org.anyline.data.adapter;

import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.exception.NotFoundAdapterException;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DriverAdapterHolder {
	private static final Logger log = LoggerFactory.getLogger(DriverAdapterHolder.class);
	/**
	 * 项目注册adapter用来覆盖adapters
	 */
	public static LinkedHashMap<DatabaseType, DriverAdapter> user_adapters = new LinkedHashMap<>();
	private static HashSet<DriverAdapter> adapters = new HashSet<>();
	private static HashSet<DatabaseType> supports = new HashSet<>();

	private static DataSourceMonitor monitor;
	public DriverAdapterHolder() {}
	public static void reg(DatabaseType type, DriverAdapter adapter) {
		user_adapters.put(type, adapter);
	}
	public static void setMonitor(DataSourceMonitor monitor) {
		DriverAdapterHolder.monitor = monitor;
	}

	public static DataSourceMonitor getMonitor() {
		return monitor;
	}
	public static boolean keepAdapter(Object datasource){
		boolean keep = ConfigTable.KEEP_ADAPTER == 1;
		if(ConfigTable.KEEP_ADAPTER == 2 && null != monitor){
			keep = monitor.keepAdapter(datasource);
		}
		return keep;
	}
	public static String feature(Object datasource){
		String feature = null;
		if(ConfigTable.KEEP_ADAPTER == 2 && null != monitor){
			feature = monitor.feature(datasource);
		}
		return feature;
	}
	/**
	 * 获取支持数据库的适配器,注意有可能获取到多个
	 * @param type 数据库类型
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(DatabaseType type) {
		DriverAdapter adapter = user_adapters.get(type);
		if(null == adapter) {
			List<DriverAdapter> list = getAdapters(type);
			if (!list.isEmpty()) {
				adapter = list.get(0);
			}
		}
		return adapter;
	}
	public static List<DriverAdapter> getAdapters(DatabaseType type) {
		List<DriverAdapter> list = new ArrayList<>();
		for(DriverAdapter adapter:adapters) {
			if(adapter.type() == type) {
				list.add(adapter);
			}
		}
		return list;
	}
	public static List<DriverAdapter> getAdapters() {
		List<DriverAdapter> list = new ArrayList<>();
		list.addAll(adapters);
		return list;
	}
	public static void setAdapters(Map<String, DriverAdapter> map) {
		if(null != map) {
			for (DriverAdapter adapter:map.values()) {
				adapters.add(adapter);
			}
		}
	}
	public static boolean support(DatabaseType type) {
		return supports.contains(type);
	}

	private static DriverAdapter defaultAdapter = null;	// 如果当前项目只有一个adapter则不需要多次识别

	/**
	 * 定位适配器
	 * @param datasource 数据源名称(配置文件中的key)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(String datasource, DataRuntime runtime) {
		boolean keep = keepAdapter(runtime.getProcessor());
		//项目中只有一个适配器时直接返回
		if(null != defaultAdapter && keep) {
			return defaultAdapter;
		}
		if(adapters.size() == 1 && keep) {
			defaultAdapter = adapters.iterator().next();
			return defaultAdapter;
		}else if(adapters.size() == 2 && keep) {
			boolean common = false;
			for (DriverAdapter adapter:adapters) {
				if(adapter.getClass().getName().toLowerCase().contains("common")) {
					common = true;
				}
			}
			if(common) {
				for (DriverAdapter adapter:adapters) {
					if(!adapter.getClass().getName().toLowerCase().contains("common")) {
						defaultAdapter = adapter;
						return defaultAdapter;
					}
				}
			}
		}
		DriverAdapter adapter = null;
		String feature = runtime.getFeature();
		String adapter_key = runtime.getAdapterKey();
		try {
			//执行两次匹配, 第一次失败后，会再匹配一次，第二次传入true
			for (DriverAdapter item:adapters) {
				if(item.match(feature, adapter_key, false)) {
					adapter = item;
					break;
				}
			}
			if(null == adapter) {
				for (DriverAdapter item:adapters) {
					if(item.match(feature, adapter_key, true)) {
						adapter = item;
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error("检测适配器 异常:", e);
		}
		if(null == adapter) {
			log.error("[检测数据库适配器][检测失败][可用适配器数量:{}][检测其他可用的适配器]", adapters.size());
			throw new NotFoundAdapterException("检测数据库适配器失败");
		}else if(log.isDebugEnabled()){
			log.debug("[检测数据库适配器][数据源:{}][特征:{}][适配结果:{}]", datasource, feature, adapter);
		}
		return adapter;
	}

}
