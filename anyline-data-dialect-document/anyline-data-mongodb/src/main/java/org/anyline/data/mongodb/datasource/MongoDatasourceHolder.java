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


package org.anyline.data.mongodb.datasource;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DatasourceHolder;
import org.anyline.data.mongodb.runtime.MongoRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.Database;
import org.anyline.proxy.DatasourceHolderProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;


@Component("anyline.data.datasource.holder.mongo")
public class MongoDatasourceHolder extends DatasourceHolder {
	private static Logger log = LoggerFactory.getLogger(MongoDatasourceHolder.class);

	public MongoDatasourceHolder(){
		DatasourceHolderProxy.reg(MongoClient.class,this);
		DatasourceHolderProxy.reg(MongoDatabase.class,this);
	}


	/* *****************************************************************************************************************
	 * reg:[调用入口]<br/>注册数据源(用户或配置监听调用)
	 * inject:创建并注入数据源
	 * init:初始化数据源周边环境(service, jdbc, 事务管理器)
	 * destroy:注销数据源及周边环境
	 * validate:检测数据源可用状态
	 * transaction:事务相关
	 *
	 * 执行线路:
	 * 1.加载配置文件，转发给各个DatasourceLoader,各个DatasourceLoder过滤适用的数据源(主要根据url和type)
	 *   1.1 检测数据源列表(包含默认数据源)
	 *   1.2 逐个数据源调用注册数(reg)
	 *      1.2.1 解析配置文件
	 *      1.2.2 注入数据源(inject)
	 *      1.2.3 初始化周边环境(init)
	 * 2.用户调用注册数据源(reg)
	 *   2.1 注入数据源(inject)
	 *   2.2 初始化周边环境(init)
	 * ****************************************************************************************************************/


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
		return reg(key, param, true);
	}
	public static String reg(String key, Map param, boolean override) throws Exception{
		String ds_id = inject(key, param, override);
		return init(key, ds_id, override);
	}

	public static MongoDatabase reg(String key, MongoDatabase ds, boolean over) throws Exception{
		return init(key, ds, over);
	}
	public static MongoDatabase reg(String key, MongoDatabase ds) throws Exception{
		return init(key, ds, true);
	}
	public static String inject(String key, Map param, boolean override) throws Exception{

		return inject(key, null, param, null, override);
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
			String uri = value(env, prefix, "url", String.class, null);
			if(BasicUtil.isEmpty(uri)){
				return null;
			}
			if(!uri.startsWith("mongodb:")){
				//只注册mongo驱动
				return null;
			}
			String database = value(env, prefix, "database", String.class, null);
			if(BasicUtil.isEmpty(database)){
				database = parseDatabase(uri);
			}
			if(BasicUtil.isEmpty(database)){
				log.warn("缺少Mongo数据库名");
				return null;
			}
			Map<String,Object> map = new HashMap<>();
			map.put("database",database);
			String client = inject(key, prefix, map, env, true);
			if(null == client){//创建数据源失败
				return null;
			}
			init(key, client, false);
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
	public static String inject(String key, String prefix, Map params, Environment env, boolean override) throws Exception{
		Map<String, Object> cache = DatasourceHolder.params.get(key);
		if(null == cache){
			cache = new HashMap<>();
			DatasourceHolder.params.put(key, cache);
		}
		check(key, override);

		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		String database_id = DataRuntime.ANYLINE_DATABASE_BEAN_PREFIX + key;
		MongoClient client = null;
		MongoDatabase db = null;
		try {
 			String uri =  value(params, "url", String.class, null);
			if(BasicUtil.isEmpty(uri)){
				uri =  value(env, prefix,"url", String.class, null);
			}
			if(BasicUtil.isEmpty(uri)){
				return null;
			}
			if(!uri.startsWith("mongodb:")){
				//只注册mongo驱动
				return null;
			}

			String database =  value(params, "database", String.class, null);

			if(BasicUtil.isEmpty(database)){
				database = parseDatabase(uri);
			}
			if(BasicUtil.isEmpty(database)){
				log.warn("缺少Mongo数据库名");
				return null;
			}
			Map<String,Object> map = new HashMap<>();
			map.putAll(params);
			map.put("database", database);

			client = MongoClients.create(uri);
			db = client.getDatabase(database);
			factory.registerSingleton(datasource_id, client);
			factory.registerSingleton(database_id, db);

		} catch (Exception e) {
			log.error("[注册数据源失败][数据源:{}][msg:{}]", key, e.toString());
			e.printStackTrace();
			return null;
		}
		return datasource_id;
	}

	public static String inject(String key, Map params) throws Exception{
		return inject(key, params, true);
	}

	/**
	 * 注册数据源
	 * @param key 数据源名称
	 * @param datasource 数据源bean id
	 * @param over 是否允许覆盖已有的数据源
	 * @return MongoDatabase
	 * @throws Exception 异常 Exception
	 */
	private static String init(String key, String datasource, boolean over) throws Exception{
		if(null != datasource) {
			check(key, over);
			MongoRuntimeHolder.reg(key);
		}
		return datasource;
	}
	private static MongoDatabase init(String key, MongoDatabase database, boolean over) throws Exception{
		if(null != database) {
			check(key, over);
			MongoRuntimeHolder.reg(key);
		}
		return database;
	}
	@Override
	public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return exeTemporary( datasource, database, adapter);
	}

	private static DataRuntime exeTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return MongoRuntimeHolder.temporary( datasource, database, adapter);
	}

	/**
	 * 检测数据源是否连接正常
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean validate(String ds){
		return validate(MongoRuntimeHolder.runtime(ds));
	}
	public static boolean validate(){
		return validate(MongoRuntimeHolder.runtime("mongodb"));
	}
	public static boolean validate(DataRuntime runtime){
		MongoDatabase database = (MongoDatabase) runtime.getProcessor();
		return validate(database);
	}

	public static boolean validate(MongoDatabase database){
		try{
			return exeValidate(database);
		}catch (Exception e){
			return false;
		}
	}

	public static boolean exeValidate(MongoDatabase database) throws Exception{
		database.getName();
		return true;
	}

	@Override
	public boolean callValidate(DataRuntime runtime) {
		return validate(runtime);
	}

	@Override
	public boolean callHit(DataRuntime runtime) throws Exception{
		return exeValidate( (MongoDatabase) runtime.getProcessor());
	}

	public static void destroy(String datasource) {
		exeDestroy(datasource);
	}
	@Override
	public void callDestroy(String datasource) {
		exeDestroy(datasource);
	}

	private static void exeDestroy(String datasource){
		MongoRuntimeHolder.destroy(datasource);
	}
	public static List<String> copy(){
		return copy("default");
	}

	public static List<String> copy(String datasource){
		DataRuntime runtime = RuntimeHolder.runtime(datasource);
		return copy(runtime);
	}
	@Override
	public List<String> callCopy(DataRuntime runtime) {
		return exeCopy(runtime);
	}
	private static List<String> exeCopy(DataRuntime runtime){
		List<String> list = new ArrayList<>();
		//查看结果
		AnylineService service = ServiceProxy.service(runtime.datasource());
		LinkedHashMap<String, Database> databases = service.metadata().databases();
		Map<String,Object> map = params.get(runtime.datasource());
		if(null == map){
			log.warn("不是从anyline创建的数据源获取不到数据源参数");
			return list;
		}
		for(String database:databases.keySet()){
			Map<String, Object> copy_params = new HashMap<>();
			BeanUtil.copy(copy_params, map);
			copy_params.put("database", database);
			String key = runtime.datasource() + "_" + database.toLowerCase();
			if(RuntimeHolder.contains(key)){
				list.add(key);
				continue;
			}
			try {
				String id = reg(key, copy_params, true);
				if(null != id) {
					RuntimeHolder.runtime(key).origin(runtime.getKey());
					list.add(key);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 原生DataSource
	 * @param key key
	 * @return DataSource
	 */
	public static Database database(String key){
		Database db = null;
		DataRuntime runtime = RuntimeHolder.runtime(key);
		if(null != runtime){
			db = (Database) runtime.getProcessor();
		}
		return db;
	}
}
