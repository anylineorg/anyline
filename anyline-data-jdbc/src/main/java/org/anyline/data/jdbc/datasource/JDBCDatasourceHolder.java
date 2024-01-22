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


package org.anyline.data.jdbc.datasource;

import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceKeyMap;
import org.anyline.data.datasource.DatasourceHolder;
import org.anyline.data.jdbc.runtime.JDBCRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.Database;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.ConvertException;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.DatasourceHolderProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.*;


@Component("anyline.data.datasource.holder.jdbc")
public class JDBCDatasourceHolder extends DatasourceHolder {
	private static Logger log = LoggerFactory.getLogger(JDBCDatasourceHolder.class);

	protected static Map<String, DataSource> caches = new HashMap<>();
	private static Map<TransactionStatus, String> transactionStatus = new Hashtable<>();
	public static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

	public JDBCDatasourceHolder(){
		DatasourceHolderProxy.reg(DataSource.class, this);
		DatasourceHolderProxy.reg(JdbcTemplate.class, this);
		ConvertAdapter.reg(new Convert() {
			@Override
			public Class getOrigin() {
				return String.class;
			}

			@Override
			public Class getTarget() {
				return java.sql.Driver.class;
			}

			@Override
			public Object exe(Object value, Object def) throws ConvertException {
				if(null != value){
					try {
						return Class.forName(value.toString()).newInstance();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				return null;
			}
		});
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
	 * 1.加载配置文件，转发给各个DatasourceLoader, 各个DatasourceLoder过滤适用的数据源(主要根据url和type)
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
	 * @param key 切换数据源依据 默认key=dataSource
	 * @param pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
	 * @param driver 驱动类 如 com.mysql.cj.jdbc.Driver
	 * @param url url
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	public static String reg(String key, String pool, String driver, String url, String user, String password) throws Exception{
		Map<String, Object> param = new HashMap<>();
		param.put("type", pool);
		param.put("driver", driver);
		param.put("url", url);
		param.put("user", user);
		param.put("password", password);
		return reg(key, param);
	}
	/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=dataSource
	 * @param url url
	 * @param type 数据库类型
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	public static String reg(String key, DatabaseType type, String url, String user, String password) throws Exception{
		return reg(key, DATASOURCE_TYPE_DEFAULT, type.driver(), url, user, password);
	}

	public static String reg(String key, Map<String, Object> param, boolean override) throws Exception{
		String ds_id = inject(key, param, override);
		return init(key, ds_id, override);
	}

	public static String reg(String key, Map<String, Object> param) throws Exception{
		return reg(key, param, true);
	}
	public static DataRuntime reg(String key, DataSource ds, boolean override) throws Exception{
		return init(key, ds, override);
	}
	public static DataRuntime reg(String key, DataSource ds) throws Exception{
		return init(key, ds, false);
	}
	public static DataSource reg(String key, Connection connection, boolean override){
		return null;
	}
	public static DataSource reg(String key, Connection connection){
		return reg(key, connection, false);
	}
	public static String reg(String key, String prefix, Environment env) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
				prefix += ".";
			}
			String type = value(env, prefix, "type", String.class, null);
			if(null == type){//未设置类型 先取默认数据源类型
				type = value(env, prefix.substring(0, prefix.length()- key.length()-1), "type", String.class, null);
			}
			if (type == null) {
				type = DATASOURCE_TYPE_DEFAULT;
			}
			String url = value(env, prefix, "url", String.class, null);
			if(BasicUtil.isEmpty(url)){
				return null;
			}
			if(!url.startsWith("jdbc:")){
				//只注册jdbc驱动
				return null;
			}
			Map<String, Object> map = new HashMap<>();
			map.put("type", type);
			String ds = inject(key, prefix, map, env, true);
			if(null == ds){//创建数据源失败
				return null;
			}
			init(key, ds, false);
			return ds;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 根据params创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(key)
	 * @param params 帐号密码等参数
	 * @return bean.id
	 * @throws Exception Exception
	 */
	private static String inject(String key, Map params, boolean over) throws Exception{
		return inject(key, null, params, null, over);
	}

	/**
	 * 根据params与配置文件创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(“sso”)
	 * @param prefix 配置文件前缀 如 anyline.datasource.sso
	 * @param params map格式参数
	 * @param env 配置文件
	 * @param override 是否覆盖同名数据源
	 * @return bean.di
	 * @throws Exception Exception
	 */
	private static String inject(String key, String prefix, Map params, Environment env, boolean override) throws Exception{
		Map<String, Object> cache = DatasourceHolder.params.get(key);
		if(null == cache){
			cache = new HashMap<>();
			DatasourceHolder.params.put(key, cache);
		}
		check(key, override);
		String ds_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		try {
			String url =  value(params, "url", String.class, null);
			if(BasicUtil.isEmpty(url)){
				url = value(env, prefix, "url", String.class, null);
			}
			if(BasicUtil.isEmpty(url)){
				return null;
			}
			//只解析jdbc系列
			if(!url.toLowerCase().startsWith("jdbc:")){
				return null;
			}

			String type = value(params, "type", String.class, null);
			if(BasicUtil.isEmpty(type)){
				type = value(env, prefix, "type", String.class, null);
			}
			if (type == null) {
				type = DATASOURCE_TYPE_DEFAULT;
			}
			Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName(type);
			Object driver =  value(params, "driverClass");
			if(null == driver){
				driver = value(env, prefix, "driverClass");
			}
			if(driver instanceof String) {
				Class calzz = Class.forName((String)driver);
				//if(type.contains("druid")){
					driver = calzz.newInstance();
				//}
			}
			if(null != driver) {
				params.put("driver", driver);
			}
			cache.put("driver", driver);
			cache.put("url", url);
			//数据源
			BeanDefinitionBuilder ds_builder = BeanDefinitionBuilder.genericBeanDefinition(poolClass);
			//List<Field> fields = ClassUtil.getFields(poolClass, false, false);
			List<Method> methods = ClassUtil.getMethods(poolClass, true);
			for(Method method:methods){
				if (method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers())){
					String name = method.getName();
					// public void setMaximumPoolSize(int maxPoolSize){this.maxPoolSize = maxPoolSize;}
					if(name.startsWith("set")){
						//根据方法名
						name = name.substring(3, 4).toLowerCase() + name.substring(4);
						Object value = value(params, name);
						if(null == value){
							value = value(env, prefix, name);
						}
						//根据参数名 args0
						/*if(null == value && null != prefix && null != env){
							name = method.getParameters()[0].getName();
							value = value(preifx, name, enve, null);
						}*/
						if(null != value) {
							Class tp = method.getParameters()[0].getType();
							value = ConvertAdapter.convert(value, tp, false);
							if (null != value) {
								cache.put(name, value);
								ds_builder.addPropertyValue(name, value);
							}
						}
					}
				}
			}
		/*	if(type.contains("druid")){
				ds_builder.addPropertyValue("url", url);
			}
*/
			BeanDefinition ds_definition = ds_builder.getBeanDefinition();
			factory.registerBeanDefinition(ds_id, ds_definition);
			log.info("[注入数据源][type:JDBC][key:{}][bean:{}]", key, ds_id);
		} catch (Exception e) {
			log.error("[注入数据源失败][type:JDBC][key:{}][msg:{}]", key, e.toString());
			e.printStackTrace();
			return null;
		}
		return ds_id;
	}

	/**
	 * 添加数据源，同时添加事务与service
	 * @param key 数据源名称
	 * @param datasource 数据源bean id
	 * @param override 是否覆盖同名数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	private static String init(String key, String datasource, boolean override) throws Exception{
		if(null != datasource) {
			check(key, override);
			regTransactionManager(key, datasource);
			DataRuntime runtime = JDBCRuntimeHolder.reg(key, datasource);
			if(null != runtime){
				Map<String, Object> param = params.get(key);
				if(null != param) {
					runtime.setDriver(param.get("driver") + "");
					String url = param.get("url") + "";
					runtime.setUrl(url);
					String adapter = param.get("adapter")+"";
					if(BasicUtil.isEmpty(adapter)){
						adapter = parseAdapterKey(url);
					}
					runtime.setAdapterKey(adapter);
				}
			}
		}
		return datasource;
	}
	private static DataRuntime init(String key, DataSource datasource, boolean override) throws Exception{
		DataRuntime runtime = null;
		if(null != datasource) {
			if(null != factory) {
				check(key, override);
				regTransactionManager(key, datasource);
				runtime =JDBCRuntimeHolder.reg(key, datasource);
			}else{
				//spring还没加载完先缓存起来，最后统一注册
				if(!caches.containsKey(key) || override){
					caches.put(key, datasource);
				}
			}
		}
		return runtime;
	}

	/**
	 * 在spring启动之前注册的数据源
	 */
	public static void loadCache(){
		for(String key:caches.keySet()){
			DataSource ds = caches.get(key);
			try {
				reg(key, ds);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return exeTemporary( datasource, database, adapter);
	}
	private static DataRuntime exeTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return JDBCRuntimeHolder.temporary( datasource, database, adapter);
	}
	/**
	 * 检测数据源是否连接正常
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean validate(String ds){
		return validate(JDBCRuntimeHolder.runtime(ds));
	}
	public static boolean validate(){
		return validate(JDBCRuntimeHolder.runtime());
	}
	public static boolean validate(DataRuntime runtime){
		try {
			return exeValidate(runtime);
		}catch (Exception e){
			return false;
		}
	}


	public static boolean validate(JdbcTemplate jdbc) throws Exception{
		return validate(jdbc.getDataSource());
	}
	public static boolean validate(DataSource ds) throws Exception{
		Connection con = null;
		try{
			con = ds.getConnection();
		}finally {
			if (null != con && !DataSourceUtils.isConnectionTransactional(con, ds)) {
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return true;
	}
	public static boolean exeValidate(DataRuntime runtime) throws Exception{
		JdbcTemplate jdbc = (JdbcTemplate) runtime.getProcessor();
		return validate(jdbc);
	}
	@Override
	public boolean callValidate(DataRuntime runtime) {
		return validate(runtime);
	}

	@Override
	public boolean callHit(DataRuntime runtime) throws Exception {
		return exeValidate(runtime);
	}

	public static void destroy(String datasource) {
		exeDestroy(datasource);
	}
	@Override
	public void callDestroy(String datasource) {
		exeDestroy(datasource);
	}
	private static void exeDestroy(String datasource){
		JDBCRuntimeHolder.destroy(datasource);
	}
	public static List<String> copy(){
		return copy("default");
	}
	public static List<String> copy(String datasource){
		DataRuntime runtime = RuntimeHolder.runtime(datasource);
		return copy(runtime);
	}

	/**
	 * 根据当前数据源查询全部数据库列表，每个数据库创建一个数据源
	 * @param runtime runtime
	 * @return 数据源key列表(全大写)
	 */
	public static List<String> copy(DataRuntime runtime) {
		return exeCopy(runtime);
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
		Map<String, Object> map = params.get(runtime.datasource());
		if(null == map){
			log.warn("不是从anyline创建的数据源获取不到数据源参数");
			return list;
		}
		for(String database:databases.keySet()){
			Map<String, Object> copy_params = new HashMap<>();
			BeanUtil.copy(copy_params, map);
			String key = runtime.datasource() + "_" + database.toLowerCase();
			if(RuntimeHolder.contains(key)){
				list.add(key);
				continue;
			}
			HashSet<String> fields = DataSourceKeyMap.alias("url");
			for(String field:fields){
				String value = (String) copy_params.get(field);
				if(null != value){
					// jdbc:mysql://localhost:36932/db?
					String head = value.split("\\?")[0];
					String db = head.substring(head.lastIndexOf("/")+1);
					if(db == null || db.equalsIgnoreCase(database)){
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
	public static DataSource datasource(String key){
		DataSource ds = null;
		DataRuntime runtime = RuntimeHolder.runtime(key);
		if(null != runtime){
			JdbcTemplate jdbc = (JdbcTemplate) runtime.getProcessor();
			if(null != jdbc){
				ds = jdbc.getDataSource();
			}
		}
		return ds;
	}
	public static DataSource datasource(){
		return datasource("default");
	}


	/* *****************************************************************************************************************
	 *
	 *                                                事务相关
	 *
	 * -----------------------------------------------------------------------------------------------------------------
	 * 注册事务管理器
	 * 开启事务
	 * 提交事务
	 * 回滚事务
	 *
	 * ****************************************************************************************************************/


	public static String regTransactionManager(String key, DataSource ds){
		return regTransactionManager(key, ds, false);
	}
	public static String regTransactionManager(String key, DataSource ds, boolean primary){
		String tm_id = DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key;
		if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
			//事务管理器
			BeanDefinitionBuilder tm_builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
			tm_builder.addPropertyValue("dataSource", ds);
			tm_builder.setPrimary(primary);
			BeanDefinition tm_definition = tm_builder.getBeanDefinition();
			factory.registerBeanDefinition(tm_id, tm_definition);
			log.info("[创建事务控制器][数据源:{}][bean:{}]", key, tm_id);
		}
		return tm_id;
	}

	public static String regTransactionManager(String key, String ds){
		return regTransactionManager(key, ds, false);
	}
	public static String regTransactionManager(String key, String ds, boolean primary){
		String tm_id = DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key;
		if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
			//事务管理器
			BeanDefinitionBuilder tm_builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
			tm_builder.addPropertyReference("dataSource", ds);
			tm_builder.setPrimary(primary);
			BeanDefinition tm_definition = tm_builder.getBeanDefinition();
			factory.registerBeanDefinition(tm_id, tm_definition);
			log.info("[创建事务控制器][数据源:{}][bean:{}]", key, tm_id);
		}
		return tm_id;
	}

	/**
	 * 启动事务
	 * @param datasource 数据源
	 * @param definition 事务定义相关参数
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus start(String datasource, DefaultTransactionDefinition definition){
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource) || "default".equals(datasource) ||!ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("transactionManager");
			datasource = "default";
		}else {
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  datasource);
		}
		// 获取事务
		TransactionStatus status = dtm.getTransaction(definition);
		transactionStatus.put(status, datasource);
		return status;
	}
	/**
	 * 启动事务
	 * @param datasource 数据源
	 * @return behavior 事务传播方式<br/>
	 * 更多参数调用start(String datasource, DefaultTransactionDefinition definition)
	 */
	public static TransactionStatus start(String datasource, int behavior){
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		// 定义事务传播方式
		definition.setPropagationBehavior(behavior);
		return start(datasource, definition);
	}

	/**
	 * 启动事务
	 * 	 * @param datasource 数据源
	 * 更多参数调用start(String datasource, DefaultTransactionDefinition definition)
	 */
	public static TransactionStatus start(String datasource){
		return start(datasource, TransactionDefinition.PROPAGATION_REQUIRED);
	}

	/**
	 * 启动事务(默认数据源)
	 * @param definition 事务定义相关参数
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus start(DefaultTransactionDefinition definition){
		return start(RuntimeHolder.runtime().getKey(), definition);
	}
	/**
	 * 开启事务
	 * @param behavior 事务传播方式<br/>
	 * 更多参数调用start(String datasource, DefaultTransactionDefinition definition)
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus start(int behavior){
		return start(RuntimeHolder.runtime().getKey(), behavior);
	}

	/**
	 * 开启事务
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus start(){
		return start(RuntimeHolder.runtime().getKey());
	}

	/**
	 * 提交事务
	 * @param status 开启事务时返回TransactionStatus
	 */
	public static void commit(TransactionStatus status){
		String datasource = transactionStatus.get(status);
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource) || !ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("transactionManager");
		}else {
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  datasource);
		}
		dtm.commit(status);
		transactionStatus.remove(status);
	}

	/**
	 * 回滚事务
	 * @param status 开启事务时返回TransactionStatus
	 */
	public static void rollback(TransactionStatus status){
		String datasource = transactionStatus.get(status);
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource) || !ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("transactionManager");
		}else {
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  datasource);
		}
		dtm.rollback(status);
		transactionStatus.remove(status);
	}

	public static String url(DatabaseType type, Map<String, String> params){
		String url = type.url();
		return url;
	}

}
