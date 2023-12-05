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
import org.anyline.util.BasicUtil;
import org.anyline.util.LogUtil;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository("anyline.data.DriverAdapterHolder")
public class DriverAdapterHolder {

	private static final Logger log = LoggerFactory.getLogger(DriverAdapterHolder.class);
	private static HashSet<DriverAdapter> adapters= new HashSet<>();
	private static HashSet<DatabaseType> supports= new HashSet<>();
	private static List<DriverAdapterHolder> utils = new ArrayList<>();
	public DriverAdapterHolder(){}

	/**
	 * 临时数据源时相同的key会出现不同的adapter
	 * 注册临时数据源时先清空缓存adapter
	 * @param datasource 数据源key
	 */
	public static void remove(String datasource){
		adapters.remove("al-ds:"+datasource);
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
	 * 定准适配器
	 * @param datasource 数据源名称(配置文件中的key)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(String datasource, DataRuntime runtime){
		if(null != defaultAdapter){
			return defaultAdapter;
		}
		if(adapters.size() ==1){
			defaultAdapter = adapters.iterator().next();
			return defaultAdapter;
		}
		DriverAdapter adapter = null;
		if(null != adapter){
			return adapter;
		}
		try {
			//数据库名称(connection.getDatabaseProductName)或客户端class
			String feature = runtime.getFeature();
			if(null != feature){
				//根据特征(先不要加版本号，提取版本号需要建立连接太慢)
				adapter = getAdapter(datasource, feature, runtime.getVersion());
				if(null == adapter){
					//根据特征+版本号
					feature = runtime.getFeature(true);
					adapter = getAdapter(datasource, feature, runtime.getVersion());
				}
			}
			if(null == adapter){
				log.error("[检测数据库适配器][检测失败][可用适配器数量:{}][检测其他可用的适配器]", adapters.size());
				//adapter = SpringContextUtil.getBean(DriverAdapter.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null == adapter){
			log.error("[检测数据库适配器][检测其他可用的适配器失败][可用适配器数量:{}][{}]", adapters.size(), LogUtil.format("可能没有依赖anyline-data-jdbc-*(如mysql,neo4j)或没有扫描org.anyline包", 31));
		}
		return adapter;
	}
	private static DriverAdapter getAdapter(){
		return null;
	}

	/**
	 * 检测可支持版本数量
	 * @return boolean
	 */
	private static int versions(DatabaseType type, String ... versions){
		int qty = 0;
		for(String version:versions){
			if(adapters.containsKey(type.name()+"_"+version)){
				qty ++;
			}
		}
		return qty;
	}

	/**
	 * 取第一个存在的版本
	 * @param type DatabaseType
	 * @param versions 版本号
	 * @return adapter
	 */
	private static DriverAdapter adapter(DatabaseType type, String ... versions){
		for(String version:versions){
			DriverAdapter adapter = adapters.get(type.name() + "_" + version);
			if(null != adapter){
				return adapter;
			}
		}
		return null;
	}
	private static boolean check(DriverAdapter adapter, String datasource, String feature){
		DatabaseType type = adapter.type();
		if(null != type){
			List<String> keywords = type.keywords();
			if(null != keywords){
				for (String k:keywords){
					if(BasicUtil.isEmpty(k)){
						continue;
					}
					if(feature.contains(k)){
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * 根据数据源特征定位适配器
	 * @param datasource 数据源key
	 * @param feature 特征
	 * @param version 版本
	 * @return 适配器
	 */
	private static DriverAdapter getAdapter(String datasource, String feature, String version){
		DriverAdapter adapter = null;
		if(null != version){
			version = version.toLowerCase();
		}
		if(null != feature){
			feature = feature.toLowerCase();
		}
		for(DriverAdapter item:adapters.values()){
			boolean chk = check(item, datasource, feature);
			if(chk){
				String type = item.type().toString().toLowerCase();
				if(null != version) {
					if (type.contains("mssql")) {
						item = mssql(datasource, feature, version);
					} else if (type.contains("oracle")) {
						item = oracle(datasource, feature, version);
					}else if(type.contains("kingbase")){
						item = kingbase(datasource, feature, version);
					}
				}
				if(null != item) {
					adapters.put("al-ds:" + datasource, item);
					log.info("[检测数据库适配器][datasource:{}][特征:{}][适配器:{}]", datasource, feature, item);
					return item;
				}
			}
		}
		return null;
	}
	private static DriverAdapter mssql(String datasource, String feature, String version) {
		DriverAdapter adapter = null;
		if(null != version ){
			version = version.split("\\.")[0];
			double v = BasicUtil.parseDouble(version, 0d);
			String key = null;
			if(v >= 9.0){
				key = "2005";
			}else{
				key = "2000";
			}
			adapter =  adapters.get(DatabaseType.MSSQL.name()+"_"+key);
		}else{
			//如果没有提供版本号并且环境中只有一个版本
			if(versions(DatabaseType.MSSQL, "2000", "2005") == 1 ){
				adapter = adapter(DatabaseType.MSSQL, "2005", "2000");
			}
		}
		if(null == adapter){
			adapter =  adapters.get(DatabaseType.MSSQL.name()+"_2005");
		}
		return adapter;
	}
	private static DriverAdapter oracle(String datasource, String feature, String version){
		DriverAdapter adapter = null;
		/*Oracle Database 11g Enterprise Edition Release 11.2.0.1.0 - 64bit Production With the Partitioning, OLAP, Data Mining and Real Application Testing options*/
		if(null != version ){
			version = RegularUtil.cut(version, "release","-");
			if(null != version){
				//11.2.0.1.0
				version = version.split("\\.")[0];
			}
			double v = BasicUtil.parseDouble(version, 0d);
			String key = null;
			if(v >= 12.0){
				key = "12";
			}else{
				key = "11";
			}
			adapter =  adapters.get(DatabaseType.ORACLE.name()+"_"+key);
		}else{
			//如果没有提供版本号并且环境中只有一个版本
			if(versions(DatabaseType.ORACLE, "11", "12") == 1 ){
				adapter = adapter(DatabaseType.ORACLE, "11", "12");
			}
		}
		if(null == adapter) {
			adapter = adapters.get(DatabaseType.ORACLE.name() + "_" + version);
		}
		return adapter;
	}
	private static DriverAdapter kingbase(String datasource, String feature, String version){
		DriverAdapter adapter = null;
		/*Oracle Database 11g Enterprise Edition Release 11.2.0.1.0 - 64bit Production With the Partitioning, OLAP, Data Mining and Real Application Testing options*/
		if(null != version ){
			version = RegularUtil.cut(version, "release","-");
			if(null != version){
				//11.2.0.1.0
				version = version.split("\\.")[0];
			}
			double v = BasicUtil.parseDouble(version, 0d);
			String key = null;
			if(v >= 12.0){
				key = "12";
			}else{
				key = "11";
			}
			adapter =  adapters.get(DatabaseType.ORACLE.name()+"_"+key);
		}else{
			//如果没有提供版本号并且环境中只有一个版本
			if(versions(DatabaseType.ORACLE, "11", "12") == 1 ){
				adapter = adapter(DatabaseType.ORACLE, "11", "12");
			}
		}
		if(null == adapter) {
			adapter = adapters.get(DatabaseType.ORACLE.name() + "_" + version);
		}
		return adapter;
	}
}
