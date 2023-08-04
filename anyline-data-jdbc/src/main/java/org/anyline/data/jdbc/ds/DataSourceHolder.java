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
 
 
package org.anyline.data.jdbc.ds;

import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.data.jdbc.runtime.JdbcRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.ClientHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class DataSourceHolder extends ClientHolder {
	private static Logger log = LoggerFactory.getLogger(DataSourceHolder.class);
	private static Map<TransactionStatus, String> transactionStatus = new Hashtable<>();
	public static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";



	/**
	 * 注册新的数据源,只是把spring context中现有的数据源名称添加到数据源名称列表
	 * @param ds 数据源名称
	 */
	public static void reg(String ds){
		if(!dataSources.contains(ds)){
			dataSources.add(ds); 
		} 
	}

	/**
	 * 启动事务
	 * @param datasource 数据源
	 * @param definition 事务定义相关参数
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus startTransaction(String datasource, DefaultTransactionDefinition definition){
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource) || !ConfigTable.IS_OPEN_TRANSACTION_MANAGER){
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("transactionManager");
			datasource = "";
		}else {
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("anyline.transaction." + datasource);
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
	 * 更多参数调用startTransaction(String datasource, DefaultTransactionDefinition definition)
	 */
	public static TransactionStatus startTransaction(String datasource, int behavior){
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		// 定义事务传播方式
		definition.setPropagationBehavior(behavior);
		return startTransaction(datasource, definition);
	}

	/**
	 * 启动事务
	 * 	 * @param datasource 数据源
	 * 更多参数调用startTransaction(String datasource, DefaultTransactionDefinition definition)
	 */
	public static TransactionStatus startTransaction(String datasource){
		return startTransaction(datasource, TransactionDefinition.PROPAGATION_REQUIRED);
	}

	/**
	 * 启动事务(默认数据源)
	 * @param definition 事务定义相关参数
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus startTransaction(DefaultTransactionDefinition definition){
		return startTransaction(curDataSource(), definition);
	}
	/**
	 * 开启事务
	 * @param behavior 事务传播方式<br/>
	 * 更多参数调用startTransaction(String datasource, DefaultTransactionDefinition definition)
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus startTransaction(int behavior){
		return startTransaction(curDataSource(), behavior);
	}

	/**
	 * 开启事务
	 * @return TransactionStatus 回溯可提交时需要
	 */
	public static TransactionStatus startTransaction(){
		return startTransaction(curDataSource());
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
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("anyline.transaction." + datasource);
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
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("anyline.transaction." + datasource);
		}
		dtm.rollback(status);
		transactionStatus.remove(status);
	}
	/**
	 * 数据源列表中是否已包含指定数据源
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean contains(String ds){
		return dataSources.contains(ds); 
	}


	public static String regTransactionManager(String key, DataSource ds){
		return regTransactionManager(key, ds, false);
	}
	public static String regTransactionManager(String key, DataSource ds, boolean primary){
		String tm_id = "anyline.transaction." + key;
		if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
			//事务管理器
			DefaultListableBeanFactory factory = (DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();
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
		String tm_id = "anyline.transaction." + key;
		if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
			//事务管理器
			DefaultListableBeanFactory factory = (DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();
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
	 * 注册数据源
	 * @param key 数据源名称
	 * @param ds 数据源bean id
	 * @param over 是否允许覆盖已有的数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	private static String addDataSource(String key, String ds, boolean over) throws Exception{
		if(dataSources.contains(key)){
			if(!over){
				throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
			}else{
				//清空
				JdbcRuntimeHolder.destroy(key);
			}
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[创建数据源][thread:{}][key:{}]", Thread.currentThread().getId(), key);
		}
		regTransactionManager(key, ds);
		reg(key);
		JdbcRuntimeHolder.reg(key, ds);
		return ds;
	}
	private static DataSource addDataSource(String key, DataSource ds, boolean over) throws Exception{
		if(dataSources.contains(key)){
			if(!over){
				throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
			}else{
				//清空
				JdbcRuntimeHolder.destroy(key);
			}
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[创建数据源][thread:{}][key:{}]", Thread.currentThread().getId(), key);
		}
		regTransactionManager(key, ds);
		reg(key);
		JdbcRuntimeHolder.reg(key, ds);
		return ds;
	}

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
		Map<String,String> param = new HashMap<String,String>();
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
		return reg(key, "om.zaxxer.hikari.HikariDataSource", type.driver(), url, user, password);
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


	public static DataSource reg(String key, DataSource ds, boolean over) throws Exception{
		return addDataSource(key, ds, over);
	}
	public static DataSource reg(String key, DataSource ds) throws Exception{
		return addDataSource(key, ds, true);
	}



	public static DataSource getDataSource(){
		return JdbcRuntimeHolder.getDataSource();
	}
	public static DataSource getDataSource(String key){
		return JdbcRuntimeHolder.getDataSource(key);
	}

	public static String reg(String key, String prefix, Environment env) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
				prefix += ".";
			}
			String type = BeanUtil.value(prefix, env, "type");
			if(null == type){
				type = BeanUtil.value("spring.datasource.", env, "type");
			}
			if (type == null) {
				type = DATASOURCE_TYPE_DEFAULT;
			}
			String url = BeanUtil.value(prefix, env, "url","jdbc-url");
			if(!url.startsWith("jdbc:")){
				//只注册jdbc驱动
				return null;
			}
			String driverClassName = BeanUtil.value(prefix, env, "driver","driver-class","driver-class-name");
			String username = BeanUtil.value(prefix, env,"user","username","user-name");
			String password = BeanUtil.value(prefix, env, "password");

			//DataSource ds =  dataSourceType.newInstance();
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("url", url);
			map.put("jdbcUrl", url);
			map.put("driver",driverClassName);
			map.put("driverClass",driverClassName);
			map.put("driverClassName",driverClassName);
			map.put("user",username);
			map.put("username",username);
			map.put("password",password);
			//BeanUtil.setFieldsValue(ds, map, false);
			String ds = build(key, map);
			if(null == ds){//创建数据源失败
				return null;
			}
			addDataSource(key, ds, false);
			return ds;
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
		String ds_id = "anyline.datasource." + key;
		try {
			String type = (String)params.get("pool");
			if(BasicUtil.isEmpty(type)){
				type = (String)params.get("type");
			}
			if (type == null) {
				// throw new Exception("未设置数据源类型(如:pool=com.zaxxer.hikari.HikariDataSource)");
				type = DATASOURCE_TYPE_DEFAULT;
			}
			Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName(type);

			Object driver =  BeanUtil.propertyNvl(params,"driver","driver-class","driver-class-name");
			if(null == driver){
				return null;
			}
			if(driver instanceof String) {
				Class.forName(driver.toString());
			}else if(driver instanceof Class){
				driver = ((Class)driver).newInstance();
			}
			Object url =  BeanUtil.propertyNvl(params,"url","jdbc-url");
			Object user =  BeanUtil.propertyNvl(params,"user","username");
			Map<String,Object> map = new HashMap<String,Object>();
			map.putAll(params);
			map.put("url", url);
			map.put("jdbcUrl", url);
			map.put("driver",driver);
			map.put("driverClass",driver);
			map.put("driverClassName",driver);
			map.put("user",user);
			map.put("username",user);

			DefaultListableBeanFactory factory =(DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();

			//数据源
			BeanDefinitionBuilder ds_builder = BeanDefinitionBuilder.genericBeanDefinition(poolClass);
			List<Field> fields = ClassUtil.getFields(poolClass, false, false);
			for(Field field:fields){
				String name = field.getName();
				Object value = map.get(name);
				value = ConvertAdapter.convert(value, field.getType());
				if(null != value) {
					ds_builder.addPropertyValue(name, value);
				}
			}

			BeanDefinition ds_definition = ds_builder.getBeanDefinition();
			factory.registerBeanDefinition(ds_id, ds_definition);


		} catch (Exception e) {
			log.error("[注册数据源失败][数据源:{}][msg:{}]", key, e.toString());
			return null;
		}
		return ds_id;
	}

	/**
	 * 检测数据源是否连接正常
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean validate(String ds){
		return validate(JdbcRuntimeHolder.getRuntime(ds));
	}
	public static boolean validate(){
		return validate(JdbcRuntimeHolder.getRuntime());
	}
	public static boolean validate(DataRuntime runtime){
		JdbcTemplate jdbc = (JdbcTemplate) runtime.getClient();
		return validate(jdbc);
	}
	public static boolean validate(JdbcTemplate jdbc){
		try{
			DataSource ds = jdbc.getDataSource();
			Connection con = ds.getConnection();
			if (!DataSourceUtils.isConnectionTransactional(con, ds)) {
				DataSourceUtils.releaseConnection(con, ds);
			}
		}catch (Exception e){
			return false;
		}
		return true;
	}
}
