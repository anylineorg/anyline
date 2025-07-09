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

package org.anyline.data.adapter;

import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.exception.NotFoundAdapterException;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.*;

public class DriverAdapterHolder {
	private static final Log log = LogProxy.get(DriverAdapterHolder.class);
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

	/**
	 * 同一个数据源是否保持同一个adapter<br/>
	 * 这里通常根据类型判断 如HikariDataSource DruidDataSource<br/>
	 * 针对同一个数据源对应多个不同类型数据库时才需要返回false(如一些动态数据源类型)<br/>
	 * @param datasource 数据源
	 * @return boolean false:每次操作都会检测一次adapter true:同一数据源使用同一个adapter
	 */
	public static boolean keepAdapter(DataRuntime runtime,Object datasource) {
		boolean keep = ConfigTable.KEEP_ADAPTER == 1;
		if(ConfigTable.KEEP_ADAPTER == 2 && null != monitor) {
			keep = monitor.keepAdapter(runtime, datasource);
		}
		return keep;
	}
	public static DriverAdapter getAdapterByMonitor(DataRuntime runtime, Object datasource) {
		DriverAdapter adapter = null;
		if(null != monitor) {
			adapter = monitor.adapter(runtime, datasource);
		}
		return adapter;
	}

	/**
	 * 数据源特征 默认不需要实现  由上层方法自动提取一般会通过 driver_产品名_url 合成
	 * @param datasource 数据源
	 * @return String 返回null由上层自动提取
	 */
	public static String feature(DataRuntime runtime, Object datasource) {
		String feature = null;
		if(ConfigTable.KEEP_ADAPTER == 2 && null != monitor) {
			feature = monitor.feature(runtime, datasource);
		}
		return feature;
	}
	public static DriverAdapter after(DataRuntime runtime, Object datasource, DriverAdapter adapter) {
		if(null != monitor) {
			return monitor.after(runtime, datasource, adapter);
		}
		return adapter;
	}
	public static String key(DataRuntime runtime, Object datasource) {
		if(null != monitor) {
			return monitor.key(runtime, datasource);
		}
		return null;
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
	 * 过程:
	 * 1.检测环境中是否只有1个adapter,如果是直接返回
	 * 2.检测环境中是否只有2个adapter并且包含1个common,如果是直接返回非common的adapter
	 * 3.根据项目实现的DataSourceMonitor接口adapter(Object datasource)定位adapter
	 * 4.检测
	 * @param datasource 数据源名称(配置文件中的key)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(String datasource, DataRuntime runtime) {
		if(null != defaultAdapter) {
			return defaultAdapter;
		}
		//环境中只有一个adapter时快速匹配所有数据源(不检测数据库特征)
		if(ConfigTable.IS_ENABLE_ADAPTER_FAST_MATCH) {
			if (adapters.size() == 1) {
				defaultAdapter = adapters.iterator().next();
				return defaultAdapter;
			} else if (adapters.size() == 2) {
				boolean common = false;
				for (DriverAdapter adapter : adapters) {
					if (adapter.getClass().getName().toLowerCase().contains("common")) {
						common = true;
					}
				}
				if (common) {
					for (DriverAdapter adapter : adapters) {
						if (!adapter.getClass().getName().toLowerCase().contains("common")) {
							defaultAdapter = adapter;
							return defaultAdapter;
						}
					}
				}
			}
		}
		DriverAdapter adapter = getAdapterByMonitor(runtime, runtime.getProcessor());
		if(null == adapter) {
			String feature = runtime.getFeature(false);
			String adapter_key = runtime.getAdapterKey();
			try {
				//先检测项目注册adapters再检测内容adapters
				//执行两次匹配, 第一次失败后，会再匹配一次，第二次传入true
				for (DriverAdapter item:user_adapters.values()) {
					if(item.match(runtime, feature, adapter_key, false)) {
						adapter = item;
						break;
					}
				}
				if(null == adapter) {
					feature = runtime.getFeature(true);
					for (DriverAdapter item:user_adapters.values()) {
						if(item.match(runtime, feature, adapter_key, true)) {
							adapter = item;
							break;
						}
					}
				}

				if(null == adapter) {
					for (DriverAdapter item : adapters) {
						if (item.match(runtime, feature, adapter_key, false)) {
							adapter = item;
							break;
						}
					}
				}
				if(null == adapter) {
					feature = runtime.getFeature(true);
					for (DriverAdapter item:adapters) {
						if(item.match(runtime, feature, adapter_key, true)) {
							adapter = item;
							break;
						}
					}
				}
			} catch (Exception e) {
				log.error("检测适配器 异常:", e);
			}
			if(null != adapter && log.isDebugEnabled()) {
				log.debug("[检测数据库适配器][数据源:{}][特征:{}][适配结果:{}]", datasource, feature, adapter);
			}
		}
		if(null != adapter) {
			adapter = after(runtime, runtime.getProcessor(), adapter);
		}
		if(null == adapter) {
			log.error("[检测数据库适配器][检测失败][version:{}][可用适配器:{}]", ConfigTable.getVersion(), BeanUtil.concat(adapters));
			String title = "检测数据库适配器失败(请参考 http://doc.anyline.org/aa/6f_15195)";
			throw new NotFoundAdapterException(title);
		}
		return adapter;
	}

}
