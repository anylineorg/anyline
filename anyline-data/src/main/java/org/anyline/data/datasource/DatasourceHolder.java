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
 * 
 *           
 */ 
 
 
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


package org.anyline.data.datasource;

import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.DatasourceHolderProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public abstract class DatasourceHolder {
	public static Logger log = LoggerFactory.getLogger(DatasourceHolder.class);

	protected static DefaultListableBeanFactory factory;
	public static void init(DefaultListableBeanFactory factory){
		DatasourceHolder.factory = factory;
	}
	public static DefaultListableBeanFactory factory(){
		return factory;
	}
	//数据源对应的数据库类型
	protected static Map<String, DatabaseType> types = new HashMap<>();

	//注册数据源的参数
	public static Map<String, Map<String, Object>> params = new HashMap<>();

	/**
	 * 已注册成功的所有数据源
	 * @return List
	 */
	public static List<String> list(){
		return RuntimeHolder.keys();
	}

	public static DatabaseType dialect(String datasource){
		return types.get(datasource);
	}
	public static void dialect(String ds, DatabaseType type){
		types.put(ds, type);
	}

	/**
	 * 数据源列表中是否已包含指定数据源
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean contains(String ds){
		return RuntimeHolder.contains(ds);
	}

	public static Object value(Environment env, String prefix, String name){
		return value(env, prefix, name, Object.class, null);
	}
	public static <T> T value(Environment env, String prefix, String name, Class<T> clazz, T def){
		T result = null;
		if(null != env && null != prefix && null != name) {
			String value = BeanUtil.value(prefix, env, name);
			if (null == value) {
				HashSet<String> alias = DataSourceKeyMap.alias(name);
				if (null != alias) {
					for (String item : alias) {
						if (null == value) {
							value = BeanUtil.value(prefix, env, item);
						}
						if (null != value) {
							break;
						}
					}
				}
			}
			if(null != value){
				result = (T) ConvertAdapter.convert(value, clazz, false);
			}
			if(null == result){
				result = def;
			}
		}
		return result;
	}

	public static Object value(Map map, String name){
		return value(map, name, Object.class, null);
	}
	public static <T> T value(Map map, String name, Class<T> clazz, T def){
		T result = null;
		Object value = map.get(name);
		if(null == value) {
			HashSet<String> alias = DataSourceKeyMap.alias(name);
			if (null != alias) {
				for (String item : alias) {
					if (null == value) {
						value = map.get(item);
					}
					if (null != value) {
						break;
					}
				}
			}
		}
		if(null != value){
			result = (T) ConvertAdapter.convert(value, clazz, false);
		}
		if(null == result){
			result = def;
		}
		return result;
	}

	protected static void check(String key, boolean override) throws Exception{
		if(contains(key)){
			if(!override){
				throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
			}else{
				//清空
				destroy(key);
			}
		}
	}
	public static DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception{
		return DatasourceHolderProxy.temporary(datasource, database, adapter);
	}
	public abstract DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception;

	/**
	 * 验证数据源可用性
	 * @param ds 数据源
	 * @return boolean
	 */
	public static boolean validate(String ds){
		return validate(RuntimeHolder.runtime(ds));
	}
	public static boolean validate(){
		return validate(RuntimeHolder.runtime());
	}
	public static boolean validate(DataRuntime runtime){
		return DatasourceHolderProxy.validate(runtime);
	}
	public abstract boolean callValidate(DataRuntime runtime);


	public static boolean hit(String ds) throws Exception{
		return hit(RuntimeHolder.runtime(ds));
	}
	public static boolean hit() throws Exception{
		return hit(RuntimeHolder.runtime());
	}
	public static boolean hit(DataRuntime runtime)  throws Exception{
		return DatasourceHolderProxy.hit(runtime);
	}
	public abstract boolean callHit(DataRuntime runtime) throws Exception;

	/**
	 * 注销数据源及相关资源
	 * @param datasource 数据源key
	 */
	public static void destroy(String datasource){
		DatasourceHolderProxy.destroy(RuntimeHolder.runtime(datasource));
	}
	public abstract void callDestroy(String datasource);
	public static List<String> copy(String datasource){
		return DatasourceHolderProxy.copy(RuntimeHolder.runtime(datasource));
	}
	public static List<String> copy(){
		return DatasourceHolderProxy.copy(RuntimeHolder.runtime());
	}
	public static List<String> copy(DataRuntime runtime){
		return DatasourceHolderProxy.copy(runtime);
	}

	public abstract  List<String> callCopy(DataRuntime runtime);

	public static String parseAdapterKey(String url){
		String adapter = null;
		if(url.contains("adapter")){
			adapter = RegularUtil.cut(url, "adapter=", "&");
			if(BasicUtil.isEmpty(adapter)){
				adapter = RegularUtil.cut(url, "adapter=", RegularUtil.TAG_END);
			}
		}
		return adapter;
	}
}
