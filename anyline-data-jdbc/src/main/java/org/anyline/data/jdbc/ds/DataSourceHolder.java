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

import org.anyline.data.jdbc.util.DataSourceUtil;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.data.DatabaseType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.util.*;


public class DataSourceHolder {
	public static Logger log = LoggerFactory.getLogger(DataSourceHolder.class);

	// 切换前数据源 
    private static final ThreadLocal<String> THREAD_RECALL_SOURCE = new ThreadLocal<String>(); 
	// 当前数据源 
    private static final ThreadLocal<String> THREAD_CUR_SOURCE = new ThreadLocal<String>(); 
    // 是否还原默认数据源,执行一次操作后还原回  切换之前的数据源
    private static final ThreadLocal<Boolean> THREAD_AUTO_RECOVER = new ThreadLocal<Boolean>(); 
    private static List<String> dataSources = new ArrayList<>();
	//数据源对应的数据库类型
	private static Map<String, DatabaseType> types = new HashMap<>();

	private static Map<TransactionStatus, String> transactionStatus = new Hashtable<>();
    static{ 
    	THREAD_AUTO_RECOVER.set(false); 
    }


	/**
	 * 已注册成功的所有数据源
	 * @return List
	 */
	public static List<String> list(){
		return dataSources;
	}

    public static String curDataSource() {
        return THREAD_CUR_SOURCE.get();
    }

	public static DatabaseType dialect(){
		String ds = curDataSource();
		return types.get(ds);
	}
	public static void dialect(String ds, DatabaseType type){
		types.put(ds, type);
	}

	/**
	 * 设置当前数据源名称
	 * @param dataSource 数据源在spring context中注册的名称
	 */
	public static void setDataSource(String dataSource) {
		setDataSource(dataSource, false);
    }
	public static void destroyDataSource(String dataSource){
		dataSources.remove(dataSource);
		RuntimeHolder.destroyRuntime(dataSource);
	}

	/**
	 * 设置当前数据源名称
	 * @param dataSource 数据源在spring context中注册的名称
	 * @param auto 执行完后切换回原来的数据库
	 */
    public static void setDataSource(String dataSource, boolean auto) {

		//不要切换到默认数据源，避免误操作
		/*if(BasicUtil.isEmpty(dataSource)){
			setDefaultDataSource();
			return;
		}*/
		if(null == dataSource || !dataSources.contains(dataSource)){
			throw new RuntimeException("数据源未注册:"+dataSource);
		}
    	if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){ 
    		log.warn("[切换数据源][thread:{}][数据源:{}>{}][auto recover:{}]", Thread.currentThread().getId(), THREAD_RECALL_SOURCE.get(), dataSource, auto);
    	} 
    	THREAD_RECALL_SOURCE.set(THREAD_CUR_SOURCE.get());//记录切换前数据源 
    	THREAD_CUR_SOURCE.set(dataSource); 
    	THREAD_AUTO_RECOVER.set(auto); 
    } 
    // 恢复切换前数据源 
    public static void recoverDataSource(){ 
    	THREAD_CUR_SOURCE.set(THREAD_RECALL_SOURCE.get()); 
    } 
    public static void setDefaultDataSource(){ 
    	clearDataSource();
		if(dataSources.contains("dataSource")){
			setDataSource("dataSource");
		}else if(dataSources.contains("default")){
			setDataSource("default");
		}
    	THREAD_AUTO_RECOVER.set(false);
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
			log.warn("[切换数据源][thread:{}][数据源:{}>默认数据源]",Thread.currentThread().getId(), THREAD_RECALL_SOURCE.get());
		}
	}
    public static void clearDataSource() { 
    	THREAD_CUR_SOURCE.remove(); 
    } 
    public static boolean isAutoDefault(){ 
    	if(null == THREAD_AUTO_RECOVER || null == THREAD_AUTO_RECOVER.get()){ 
    		return false; 
    	} 
    	return THREAD_AUTO_RECOVER.get(); 
    } 
 
	/** 
	 * 解析数据源,并返回修改后的SQL 
	 * &lt;mysql_ds&gt;crm_user 
	 * @param src  src
	 * @return String
	 */ 
	public static String parseDataSource(String src){
		if(null != src && src.startsWith("<")){ 
			int fr = src.indexOf("<"); 
			int to = src.indexOf(">"); 
			if(fr != -1){ 
				String ds = src.substring(fr+1,to); 
				src = src.substring(to+1); 
				setDataSource(ds, true); 
			} 
		} 
		return src;
	}
	public static String parseDataSource(String dest, Object obj){
		if(BasicUtil.isNotEmpty(dest) || null == obj){
			return parseDataSource(dest);
		}
		String result = "";
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			String link = row.getDataLink();
			if(BasicUtil.isNotEmpty(link)){
				DataSourceHolder.setDataSource(link, true);
			}
			result = row.getDataSource();
		}else if(obj instanceof DataSet){
			DataSet set = (DataSet)obj;
			if(set.size()>0){
				result = parseDataSource(dest, set.getRow(0));
			}
		} else if (obj instanceof Collection) {
			Object first = ((Collection)obj).iterator().next();
			result = EntityAdapterProxy.table(first.getClass()).getName();
		} else{
			result = EntityAdapterProxy.table(obj.getClass()).getName();
		}
		result = parseDataSource(result);
		return result;
	}
	/**
	 * 注册新的数据源,只是把spring context中现有的数据源名称添加到数据源名称列表
	 * @param ds 数据源名称
	 */
	public static void reg(String ds){ 
		if(!dataSources.contains(ds)){ 
			dataSources.add(ds); 
		} 
	}

	public static TransactionStatus startTransaction(String datasource, DefaultTransactionDefinition definition){
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource)){
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("transactionManager");
		}else {
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("anyline.transaction." + datasource);
		}
		// 获取事务
		TransactionStatus status = dtm.getTransaction(definition);
		transactionStatus.put(status, datasource);
		return status;
	}
	public static TransactionStatus startTransaction(String datasource, int behavior){
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		// 定义事务传播方式
		definition.setPropagationBehavior(behavior);
		return startTransaction(datasource, definition);
	}

	public static TransactionStatus startTransaction(String datasource){
		return startTransaction(datasource, TransactionDefinition.PROPAGATION_REQUIRED);
	}

	public static TransactionStatus startTransaction(DefaultTransactionDefinition definition){
		return startTransaction(curDataSource(), definition);
	}
	public static TransactionStatus startTransaction(int behavior){
		return startTransaction(curDataSource(), behavior);
	}

	public static TransactionStatus startTransaction(){
		return startTransaction(curDataSource());
	}
	public static void commit(TransactionStatus status){
		String datasource = transactionStatus.get(status);
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource)){
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("transactionManager");
		}else {
			dtm = (DataSourceTransactionManager) SpringContextUtil.getBean("anyline.transaction." + datasource);
		}
		dtm.commit(status);
		transactionStatus.remove(status);
	}

	public static void rollback(TransactionStatus status){
		String datasource = transactionStatus.get(status);
		DataSourceTransactionManager dtm = null;
		if(BasicUtil.isEmpty(datasource)){
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


	public static String regDataSourceTransactionManager(String key, DataSource ds){
		String tm_id = "anyline.transaction." + key;
		//事务管理器
		DefaultListableBeanFactory factory =(DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();
		BeanDefinitionBuilder tm_builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
		tm_builder.addPropertyValue("dataSource", ds);
		BeanDefinition tm_definition = tm_builder.getBeanDefinition();
		factory.registerBeanDefinition(tm_id, tm_definition);
		log.warn("[创建事务控制器][数据源:{}][bean:{}]", key, tm_id);
		return tm_id;
	}
	public static String regDataSourceTransactionManager(String key, String ds){
		String tm_id = "anyline.transaction." + key;
		//事务管理器
		DefaultListableBeanFactory factory =(DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();
		BeanDefinitionBuilder tm_builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
		tm_builder.addPropertyReference("dataSource", ds);
		BeanDefinition tm_definition = tm_builder.getBeanDefinition();
		factory.registerBeanDefinition(tm_id, tm_definition);
		log.warn("[创建事务控制器][数据源:{}][bean:{}]", key, tm_id);
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
		if(!over && dataSources.contains(key)){
			throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[创建数据源][thread:{}][key:{}]", Thread.currentThread().getId(), key);
		}
		regDataSourceTransactionManager(key, ds);
		reg(key);
		RuntimeHolder.reg(key, ds);
		return ds;
	}
	private static DataSource addDataSource(String key, DataSource ds, boolean over) throws Exception{
		if(!over && dataSources.contains(key)){
			throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[创建数据源][thread:{}][key:{}]", Thread.currentThread().getId(), key);
		}
		regDataSourceTransactionManager(key, ds);
		reg(key);
		RuntimeHolder.reg(key, ds);
		return ds;
	}

	/**
	 *
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
	public static String reg(String key, DatabaseType type, String url, String user, String password) throws Exception{
		return reg(key, "om.zaxxer.hikari.HikariDataSource", type.getDriver(), url, user, password);
	}

	public static String reg(String key, Map param, boolean over) throws Exception{
		return addDataSource(key, DataSourceUtil.regDatasource(key, param), over);
	}
	public static String reg(String key, Map param) throws Exception{
		String ds = DataSourceUtil.regDatasource(key, param);
		return addDataSource(key, ds, true);
	}


	public static DataSource reg(String key, DataSource ds, boolean over) throws Exception{
		return addDataSource(key, ds, over);
	}
	public static DataSource reg(String key, DataSource ds) throws Exception{
		return addDataSource(key, ds, true);
	}



	public static DataSource getDataSource(){
		return RuntimeHolder.getDataSource();
	}
	public static DataSource getDataSource(String key){
		return RuntimeHolder.getDataSource(key);
	}


}
