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


package org.anyline.data.mongo.runtime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.ClientHolder;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;


public class MongoClientHolder extends ClientHolder {
	private static Logger log = LoggerFactory.getLogger(MongoClientHolder.class);



	/**
	 * 注册数据源
	 * @param key 数据源名称
	 * @param ds 数据源bean id
	 * @param over 是否允许覆盖已有的数据源
	 * @return MongoDatabase
	 * @throws Exception 异常 Exception
	 */
	private static String addDataSource(String key, String ds, boolean over) throws Exception{
		if(dataSources.contains(key)){
			if(!over){
				throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
			}else{
				//清空
				MongoRuntimeHolder.destroy(key);
			}
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[创建数据源][thread:{}][key:{}]", Thread.currentThread().getId(), key);
		}
		reg(key);
		MongoRuntimeHolder.reg(key);
		return ds;
	}
	private static MongoDatabase addDataSource(String key, MongoDatabase ds, boolean over) throws Exception{
		if(dataSources.contains(key)){
			if(!over){
				throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
			}else{
				//清空
				MongoRuntimeHolder.destroy(key);
			}
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[创建数据源][thread:{}][key:{}]", Thread.currentThread().getId(), key);
		}
 		reg(key);
		MongoRuntimeHolder.reg(key);
 		return ds;
	}

	/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=datasource
 	 * @param uri uri
	 * @param database 数据库
	 * @param user 用户名
	 * @param password 密码
	 * @return MongoDatabase
	 * @throws Exception 异常 Exception
	 */
	public static String reg(String key, String uri, String database, String user, String password) throws Exception{
		Map<String, String> param = new HashMap<String, String>();
		param.put("uri", uri);
		param.put("database", database);
		param.put("user", user);
		param.put("password", password);
		return reg(key, param);
	}
	public static String reg(String key, Map param, boolean over) throws Exception{
		return addDataSource(key, reg(key, param), over);
	}

	public static String reg(String key, Map param) throws Exception{
		String ds = build(key, param);
		if(null == ds) {//创建数据源失败
			return null;
		}
		return addDataSource(key, ds, true);
	}


	public static MongoDatabase reg(String key, MongoDatabase ds, boolean over) throws Exception{
		return addDataSource(key, ds, over);
	}
	public static MongoDatabase reg(String key, MongoDatabase ds) throws Exception{
		return addDataSource(key, ds, true);
	}
	public static String parseDatabase(String uri){
		//mongodb://localhost:27017/mydb
		String database = null;
		if(null != uri && uri.contains(":")){
			String[] tmps = uri.split(":");
			tmps = tmps[tmps.length-1].split("/");
			if(tmps.length>1){
				database = tmps[tmps.length-1];
			}
		}
		return database;
	}
	public static String reg(String key, String prefix, Environment env) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
				prefix += ".";
			}
			String uri = BeanUtil.value(prefix, env, "url","uri");
			if(BasicUtil.isEmpty(uri)){
				return null;
			}
			if(!uri.startsWith("mongodb:")){
				//只注册mongo驱动
				return null;
			}
			String database = BeanUtil.value(prefix, env,"database");
			if(BasicUtil.isEmpty(database)){
				database = parseDatabase(uri);
			}
			if(BasicUtil.isEmpty(database)){
				log.warn("缺少Mongo数据库名");
				return null;
			}
			String username = BeanUtil.value(prefix, env,"user","username","user-name");
			String password = BeanUtil.value(prefix, env, "password");

			//DataSource ds =  MongoDatabaseType.newInstance();
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("uri", uri);
			map.put("database",database);
			map.put("user",username);
			map.put("username",username);
			map.put("password",password);
			//BeanUtil.setFieldsValue(ds, map, false);
			String client = build(key, map);
			if(null == client){//创建数据源失败
				return null;
			}
			addDataSource(key, client, false);
			return client;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 创建数据源
	 * @param key key
	 * @param params 帐号密码等参数
	 * @return bean.id
	 * @throws Exception Exception
	 */
	public static String build(String key, Map params) throws Exception{
		MongoClient client = null;
		MongoDatabase db = null;
		String datasource_id = "anyline.datasource." + key;
		String database_id = "anyline.database." + key;
		try {
 			String uri =  (String)BeanUtil.propertyNvl(params,"url","uri");
			if(BasicUtil.isEmpty(uri)){
				return null;
			}
			if(!uri.startsWith("mongodb:")){
				//只注册mongo驱动
				return null;
			}
			String database =  (String)BeanUtil.propertyNvl(params,"database");

			if(BasicUtil.isEmpty(database)){
				database = parseDatabase(uri);
			}
			if(BasicUtil.isEmpty(database)){
				log.warn("缺少Mongo数据库名");
				return null;
			}
			Object user =  BeanUtil.propertyNvl(params,"user","username");
			Map<String,Object> map = new HashMap<String,Object>();
			map.putAll(params);
			map.put("uri", uri);
			map.put("database", database);
			map.put("user",user);
			map.put("username",user);

			DefaultListableBeanFactory factory =(DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();
			client = MongoClients.create(uri);
			db = client.getDatabase(database);
			factory.registerSingleton(datasource_id, client);
			factory.registerSingleton(database_id, db);

		} catch (Exception e) {
			log.error("[注册数据源失败][数据源:{}][msg:{}]", key, e.toString());
			return null;
		}
		return datasource_id;
	}

	/**
	 * 检测数据源是否连接正常
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean validate(String ds){
		return validate(MongoRuntimeHolder.getRuntime(ds));
	}
	public static boolean validate(){
		return validate(MongoRuntimeHolder.getRuntime());
	}
	public static boolean validate(DataRuntime runtime){
		MongoDatabase database = (MongoDatabase) runtime.getProcessor();
		return validate(database);
	}
	public static boolean validate(MongoDatabase database){
		try{
			database.getName();
		}catch (Exception e){
			return false;
		}
		return true;
	}
}
