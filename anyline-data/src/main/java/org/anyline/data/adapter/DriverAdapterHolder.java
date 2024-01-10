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

import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Repository("anyline.data.DriverAdapterHolder")
public class DriverAdapterHolder {

	private static final Logger log = LoggerFactory.getLogger(DriverAdapterHolder.class);
	private static HashSet<DriverAdapter> adapters= new HashSet<>();
	private static HashSet<DatabaseType> supports= new HashSet<>();
	private static List<DriverAdapterHolder> utils = new ArrayList<>();
	public DriverAdapterHolder(){}

	/**
	 * 获取支持数据库的适配器,注意有可能获取到多个
	 * @param type 数据库类型
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(DatabaseType type){
		List<DriverAdapter> list = getAdapters(type);
		if(list.isEmpty()){
			return null;
		}
		return list.get(0);
	}
	public static List<DriverAdapter> getAdapters(DatabaseType type){
		List<DriverAdapter> list = new ArrayList<>();
		for(DriverAdapter adapter:adapters){
			if(adapter.type() == type){
				list.add(adapter);
			}
		}
		return list;
	}
	public static List<DriverAdapter> getAdapters(){
		List<DriverAdapter> list = new ArrayList<>();
		list.addAll(adapters);
		return list;
	}
	@Autowired(required = false)
	public void setAdapters(Map<String, DriverAdapter> map){
		for (DriverAdapter adapter:map.values()){
			adapters.add(adapter);
		}
	}
	public static boolean support(DatabaseType type){
		return supports.contains(type);
	}

	private static DriverAdapter defaultAdapter = null;	// 如果当前项目只有一个adapter则不需要多次识别

	/**
	 * 定位适配器
	 * @param datasource 数据源名称(配置文件中的key)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(String datasource, DataRuntime runtime){
		//项目中只有一个适配器时直接返回
		if(null != defaultAdapter){
			return defaultAdapter;
		}
		if(adapters.size() ==1){
			defaultAdapter = adapters.iterator().next();
			return defaultAdapter;
		}
		DriverAdapter adapter = null;
		try {
			for (DriverAdapter item:adapters){
				if(item.match(runtime, false)){
					adapter = item;
					break;
				}
			}
			if(null == adapter){
				for (DriverAdapter item:adapters){
					if(item.match(runtime, true)){
						adapter = item;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null == adapter){
			log.error("[检测数据库适配器][检测失败][可用适配器数量:{}][检测其他可用的适配器]", adapters.size());
		}else{
			log.info("[检测数据库适配器][数据源:{}][特征:{}][适配结果:{}]", datasource, runtime.getFeature(), adapter);
		}
		return adapter;
	}

}
