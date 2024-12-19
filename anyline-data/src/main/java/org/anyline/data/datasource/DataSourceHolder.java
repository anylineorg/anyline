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

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.metadata.Database;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.sql.DataSource;
import java.util.*;

public interface DataSourceHolder {
	Log log = LogProxy.get(DataSourceHolder.class);
	/**
	 * 注册数据源的参数
	 */
	Map<String, Map<String, Object>> params = new HashMap<>();
	/**
	 * 驱动类型 与 holder对应关系
	 * DataRuntime, DatabaseType, jdbc协议(jdbc:mysql), DataSource.class, JdbcTemplate.class, 连接池class, Driver.class
	 * 最后检测协议 和 adapter(因为不一定唯一,如neo4j会实现两种adapter对应两个holder)
	 */
	Map<Object, DataSourceHolder> instances = new HashMap<>();

	/**
	 * 运行环境未启动之前注册的数据源参数 缓存
	 */
	Map<String, Object> caches = new HashMap<>();

	/**
	 * 驱动类型与holder对应关系
	 * 由holder主动上报
	 * @param check 驱动类型
	 * @param holder holder
	 */
	static void register(Object check, DataSourceHolder holder) {
		if(check instanceof String){
			check = ((String) check).toUpperCase();
		}
		instances.put(check, holder);
	}

	static DataSourceHolder instance(DataRuntime runtime) {
		Object processor = runtime.getProcessor();
		Class clazz = processor.getClass();
		DataSourceHolder holder = instance(clazz);
		return holder;
	}
	static DataSourceHolder instance(DatabaseType type) {
		return instances.get(type);
	}
	static DataSourceHolder instance(Class clazz) {
		DataSourceHolder holder = instances.get(clazz);
		//子类
		if(null == holder) {
			for(Object item: instances.keySet()) {
				if(item instanceof Class) {
					Class c = (Class) item;
					if(ClassUtil.isInSub(clazz, c)) {
						DataSourceHolder h = instances.get(c);
						instances.put(clazz, h);
						holder = h;
						break;
					}
				}
			}
		}
		if(instances.isEmpty()) {
			log.warn("[没有可用的DataSourceHolder][有可能是上下文没有加载完成 或 pom中没有依赖anyline-environment-* 或 纯Java环境没有启动DefaultEnvironmentWorker.start()]");
		}
		return holder;
	}

	/**
	 * 根据数据源定义holder
	 * @param check 数据源|jdbc协议|driver.class|url
	 * @return DataSourceHolder
	 */
	static DataSourceHolder instance(String check) {
		DataSourceHolder holder = instances.get(check.toUpperCase());
		if(null == holder) {
			try {
				DataRuntime runtime = RuntimeHolder.runtime(check);
				if (null != runtime) {
					holder = instance(runtime);
				}
			}catch (Exception e) {}
		}
		//先提取协议
		if(null == holder) {
			if(check.contains("://")) { // jdbc:postgresql://localhost:35432/simple
				String[] chks = check.split("://");
				holder = instances.get(chks[0]); //jdbc:postgresql
			}
		}
		//再提取adapter(数据库类型)
		if(null == holder) {
			String adapter = DataSourceUtil.parseAdapterKey(check);
			if(null != adapter) {
				holder = instances.get(adapter.toUpperCase());
			}
		}
		return holder;
	}
	static DataSourceHolder instance(Object chk) {
		DataSourceHolder holder = null;
		if(chk instanceof DataRuntime) {
			holder = instance((DataRuntime) chk);
		}else if(chk instanceof Class) {
			holder = instance((Class) chk);
		}else if(chk instanceof DatabaseType) {
			holder = instance((DatabaseType) chk);
		}else if(chk instanceof String) {
			holder = instance((String)chk);
		}
		return holder;
	}

	/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=datasource
	 * @param pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
	 * @param driver 驱动类 如 com.mysql.cj.jdbc.Driver
	 * @param url url
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	static String reg(String key, String pool, String driver, String url, String user, String password) throws Exception {
		DataSourceHolder instance = instance(pool);
		if(null == instance) {
			instance = instance(driver);
		}
		if(null == instance) {
			instance = instance(url);
		}
		if(null != instance) {
			return instance.create(key, pool, driver, url, user, password);
		}
		return null;
	}

	/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=datasource
	 * @param url url
	 * @param type 数据库类型
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	static String reg(String key, DatabaseType type, String url, String user, String password) throws Exception {
		DataSourceHolder instance = instance(type);
		if(null == instance) {
			instance = instance(url);
		}
		if(null != instance) {
			return instance.create(key, type, url, user, password);
		}
		return null;
	}

	static String reg(String key, Map<String, Object> param) throws Exception {
		return reg(key, param, (DatabaseType)null,true);
	}

	static String reg(String key, Map<String, Object> param, boolean override) throws Exception {
		return reg(key, param, (DatabaseType)null, override);
	}

	static String reg(String key, Map<String, Object> param, DatabaseType type) throws Exception {
		return reg(key, param, type,true);
	}

	static String reg(String key, Map<String, Object> param, DatabaseType type, boolean override) throws Exception {
		DataSourceHolder instance = null;

		if(null == instance) {
			instance = instance(BeanUtil.value(param, "adapter", DataSourceKeyMap.maps, String.class, null));
		}
		if(null == instance && null != type) {
			instance = instance(type);
		}
		if(null == instance) {
			instance = instance(BeanUtil.value(param, "driver", DataSourceKeyMap.maps, String.class, null));
		}
		if(null == instance) {
			instance = instance(BeanUtil.value(param, "type", DataSourceKeyMap.maps, String.class, null));
		}
		if(null == instance) {
			instance = instance(BeanUtil.value(param, "url", DataSourceKeyMap.maps, String.class, null));
		}
		if(!param.containsKey("adapter") && null != type){
			param.put("adapter", type.name());
		}
		if(null != instance) {
			return instance.create(key, param, override);
		}
		throw new Exception("DataSourceHolder实例异常");
	}

	static DataRuntime reg(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
		DataSourceHolder instance = null;
		if(null != datasource) {
			instance = instance(datasource.getClass());
		}
		if(null == instance) {
			instance = instance(type);
		}
		if (null != instance) {
			return instance.create(key, datasource, database, type, adapter, override);
		}
		return null;
	}
	static DataRuntime reg(String key, Object datasource, String database, boolean override) throws Exception {
		return reg(key, datasource, database, null, null,override);
	}

	static DataRuntime reg(String key, Object datasource, boolean override) throws Exception {
		return reg(key, datasource, (String) null, override);
	}
	static DataRuntime reg(String key, Object datasource, String database) throws Exception {
		return reg(key, datasource, database,true);
	}
	static DataRuntime reg(String key, Object datasource) throws Exception {
		return reg(key, datasource, (String) null, true);
	}
	static DataRuntime reg(String key, Object datasource, DatabaseType type, boolean override) throws Exception {
		return reg(key, datasource, null, type, null, override);
	}
	static DataRuntime reg(String key, Object datasource, String database, DatabaseType type) throws Exception {
		return reg(key, datasource, database, type, null,true);
	}
	static DataRuntime reg(String key, Object datasource, DatabaseType type) throws Exception {
		return reg(key, datasource, null, type, null,true);
	}
	static boolean destroy(String datasource) throws Exception {
		return RuntimeHolder.destroy(datasource);
	}
	/**
	 * 检测是否可注册(没有重复名称或呆以覆盖)
	 * 如果已存在但可覆盖 需要把原来的注销掉
	 * @param key 数据源key
	 * @param override 是否可覆盖
	 * @throws Exception 如果存在 并 不可覆盖会抛出异常
	 */
	static void check(String key, boolean override) throws Exception {
		if(contains(key)) {
			if(!override) {
				throw new Exception("[数据源重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
			}else{
				//清空
				RuntimeHolder.destroy(key);
			}
		}
	}

	/**
	 * 数据源列表中是否已包含指定数据源
	 * @param datasource 数据源名称
	 * @return boolean
	 */
	static boolean contains(String datasource) {
		return RuntimeHolder.contains(datasource);
	}

	/**
	 * 数据源对应的数据库类型
	 * @param datasource 数据源名称
	 * @return 数据库类型
	 */
	static DatabaseType dialect(String datasource) {
		return ServiceProxy.service(datasource).metadata().type();
	}

	static DatabaseType dialect() {
		return ServiceProxy.service().metadata().type();
	}

	/**
	 * 已注册成功的所有数据源
	 * @return List
	 */
	static List<String> list() {
		return RuntimeHolder.keys();
	}

	/**
	 * 数据源是否存在
	 * @param datasource 数据源key对应注册时的key
	 * @return boolean
	 */
	static boolean exists(String datasource) {
		if(null != datasource) {
			return RuntimeHolder.keys().contains(datasource);
		}
		return false;
	}
	/**
	 * 检测数据源是否可用
	 * @param datasource 数据源
	 * @return boolean
	 */
	static boolean validity(String datasource) {
		DataSourceHolder instance = instance(datasource);
		if(null != instance) {
			try {
				return instance.validate(datasource);
			}catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	/**
	 * 检测数据源是否可用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return boolean
	 */
	static boolean validity(DataRuntime runtime) {
		DataSourceHolder instance = instance(runtime);
		if(null != instance) {
			try {
				return instance.validate(runtime);
			}catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * 检测数据源是否可用 如果不可用 会抛出异常
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return boolean
	 * @throws Exception Exception
	 */
	static boolean hit(DataRuntime runtime) throws Exception {
		DataSourceHolder instance = instance(runtime);
		return instance.validate(runtime);
	}

	/**
	 * 检测数据源是否可用 如果不可用 会抛出异常
	 * @param datasource 数据源
	 * @return boolean
	 * @throws Exception Exception
	 */
	static boolean hit(String datasource) throws Exception {
		DataSourceHolder instance = instance(datasource);
		return instance.validate(datasource);
	}
	

	/* *****************************************************************************************************************
	 * create:[调用入口]<br/>注册数据源(用户或配置监听调用)
	 * inject:创建并注入数据源
	 * init:初始化数据源周边环境(service, jdbc, 事务管理器)
	 * destroy:注销数据源及周边环境
	 * validate:检测数据源可用状态
	 * transaction:事务相关
	 *
	 * 执行线路:
	 * 1.加载配置文件，转发给各个DataSourceLoader, 各个DataSourceLoder过滤适用的数据源(主要根据url和type)
	 *   1.1 检测数据源列表(包含默认数据源)
	 *   1.2 逐个数据源调用注册数(create)
	 *      1.2.1 解析配置文件
	 *      1.2.2 注入数据源(inject)
	 *      1.2.3 初始化周边环境(init)
	 * 2.用户调用注册数据源(create)
	 *   2.1 注入数据源(inject)
	 *   2.2 初始化周边环境(init)
	 * ****************************************************************************************************************/

	/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=datasource
	 * @param pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
	 * @param driver 驱动类 如 com.mysql.cj.jdbc.Driver
	 * @param url url
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	default String create(String key, String pool, String driver, String url, String user, String password) throws Exception {
		Map<String, Object> param = new HashMap<>();
		param.put("type", pool);
		param.put("driver", driver);
		param.put("url", url);
		param.put("user", user);
		param.put("password", password);
		return create(key, param);
	}

	/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=datasource
	 * @param url url
	 * @param type 数据库类型
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	default String create(String key, DatabaseType type, String url, String user, String password) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("url", url);
		params.put("user", user);
		params.put("password", password);
		if(null != type) {
			params.put("adapter", type.name());
		}
		String ds = inject(key, params, true);
		return runtime(key, ds, false);
	}
	/**
	 * 根据配置文件创建数据源
	 * @param key 数据源key
	 * @param prefix 配置文件前缀
	 * @return bean.id
	 */
	String create(String key, String prefix);

	default String create(String key, Map<String, Object> param) throws Exception {
		return create(key, param, true);
	}

	default String create(String key, Map<String, Object> param, boolean override) throws Exception {
		String datasource_id = inject(key, param, override);
		return runtime(key, datasource_id, override);
	}

	default DataRuntime create(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
		return runtime(key, datasource, database, type, adapter, override);
	}
	default DataRuntime create(String key, Object datasource) throws Exception {
		return create(key, datasource, null, null, null,false);
	}
	default DataRuntime create(String key, Object datasource, boolean override) throws Exception {
		return create(key, datasource, null, null, null, override);
	}
	default DataRuntime create(String key, Object datasource, DatabaseType type, boolean override) throws Exception {
		return create(key, datasource, null, type,null, override);
	}
	default DataRuntime create(String key, Object datasource, DatabaseType type) throws Exception {
		DataRuntime runtime = runtime(key, datasource, false);
		if(null != runtime && null != type) {
			runtime.setAdapterKey(type.name());
		}
		return runtime;
	}

	/**
	 * 验证数据源可用性 如果不可用抛出异常
	 * @param datasource 数据源
	 * @return boolean
	 * @throws Exception 不可用时抛出异常
	 */
	default boolean validate(String datasource) throws Exception {
		DataRuntime runtime = RuntimeHolder.runtime(datasource);
		return validate(runtime);
	}

	/**
	 * 验证当前数据源可用性 如果不可用抛出异常
	 * @return boolean
	 * @throws Exception 不可用时抛出异常
	 */
	default boolean validate() throws Exception {
		return validate(RuntimeHolder.runtime());
	}
	/**
	 * 验证数据源可用性 如果不可用抛出异常
	 * @param runtime 数据源
	 * @return boolean
	 * @throws Exception 不可用时抛出异常
	 */
	boolean validate(DataRuntime runtime) throws Exception;

	/**
	 * 复制数据源
	 * @param datasource 数据源
	 * @return list
	 */
	static List<String> copy(String datasource) {
		return copy(RuntimeHolder.runtime(datasource));
	}
	static List<String> copy() {
		return copy(RuntimeHolder.runtime());
	}
	static List<String> copy(DataRuntime runtime) {
		List<String> list = new ArrayList<>();
		//查看结果
		AnylineService service = ServiceProxy.service(runtime.datasource());
		LinkedHashMap<String, Database> databases = service.metadata().databases();
		Map<String, Object> map = params.get(runtime.datasource());
		if(null == map) {
			log.warn("不是从anyline创建的数据源获取不到数据源参数");
			return list;
		}
		for(String database:databases.keySet()) {
			Map<String, Object> copy_params = new HashMap<>();
			BeanUtil.copy(copy_params, map);
			String key = runtime.datasource() + "_" + database.toLowerCase();
			if(RuntimeHolder.contains(key)) {
				list.add(key);
				continue;
			}
			HashSet<String> fieldatasource = DataSourceKeyMap.alias("url");
			for(String field:fieldatasource) {
				String value = (String) copy_params.get(field);
				if(null != value) {
					// jdbc:mysql://localhost:36932/db?
					String head = value.split("\\?")[0];
					String db = head.substring(head.lastIndexOf("/")+1);
					if(db == null || db.equalsIgnoreCase(database)) {
						continue;
					}
					value = value.replace("/"+db, "/"+database);
					copy_params.put(field, value);
				}
			}
			try {
				String id = reg(key, copy_params);
				if(null != id) {
					RuntimeHolder.runtime(key).origin(runtime.getKey());
					list.add(key);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	default String regTransactionManager(String key, DataSource datasource) {
		return regTransactionManager(key, datasource, false);
	}
	String regTransactionManager(String key, DataSource datasource, boolean primary);

	default String regTransactionManager(String key, String datasource) {
		return regTransactionManager(key, datasource, false);
	}
	default String regTransactionManager(String key, String datasource, boolean primary) {
		return regTransactionManager(key, ConfigTable.environment().getBean(datasource, DataSource.class), primary);
	}
	/**
	 * 添加数据源，同时添加事务与service
	 * @param key 数据源名称
	 * @param datasource 数据源bean id
	 * @param override 是否覆盖同名数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	String runtime(String key, String datasource, boolean override) throws Exception;
	DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception;
	default DataRuntime runtime(String key, Object datasource, boolean override) throws Exception {
		return runtime(key, datasource, null, null, null, override);
	}
	/**
	 * 根据params创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(key)
	 * @param params 帐号密码等参数
	 * @return bean.id
	 * @throws Exception Exception
	 */
	default String inject(String key, Map params, boolean over) throws Exception {
		return inject(key, null, params, over);
	}

	/**
	 * 根据params与配置文件创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(“sso”)
	 * @param prefix 配置文件前缀 如 anyline.datasource.sso
	 * @param params map格式参数
	 * @param override 是否覆盖同名数据源
	 * @return bean.di
	 * @throws Exception Exception
	 */
	String inject(String key, String prefix, Map<String, Object> params,  boolean override) throws Exception;

	default  <T> T value(Map map, String keys, Class<T> clazz, T def) {
		return BeanUtil.value(map, keys, DataSourceKeyMap.maps, clazz, def);
	}
	default <T> T value(String prefix, String keys, Class<T> clazz, T def) {
		return ConfigTable.environment().value(prefix, keys, DataSourceKeyMap.maps, clazz, def);
	}

}
