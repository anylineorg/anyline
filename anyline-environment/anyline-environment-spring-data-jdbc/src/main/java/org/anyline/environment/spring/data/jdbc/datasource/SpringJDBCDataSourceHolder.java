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



package org.anyline.environment.spring.data.jdbc.datasource;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.jdbc.datasource.JDBCDataSourceHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.environment.spring.data.jdbc.runtime.SpringJDBCRuntimeHolder;
import org.anyline.environment.spring.data.transaction.SpringTransactionManage;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@Component("anyline.environment.spring.data.datasource.holder.jdbc")
public class SpringJDBCDataSourceHolder extends JDBCDataSourceHolder {
	private static final SpringJDBCDataSourceHolder instance = new SpringJDBCDataSourceHolder();
	public static SpringJDBCDataSourceHolder instance() {
		return instance;
	}

	public SpringJDBCDataSourceHolder() {
		for(DatabaseType type:DatabaseType.values()) {
			String url = type.url();
			if(url.contains("jdbc:") && url.contains("://")) { // jdbc:postgresql://localhost:35432/simple
				DataSourceHolder.register(type, this);
				DataSourceHolder.register(type.driver(), this);
				DataSourceHolder.register(type.name().toUpperCase(), this);

				String[] chks = url.split("://");
				DataSourceHolder.register(chks[0], this); //jdbc:postgresql
			}
		}
		DataSourceHolder.register("com.alibaba.druid.pool.DruidDataSource", this);
		DataSourceHolder.register(org.anyline.data.jdbc.util.DataSourceUtil.POOL_TYPE_DEFAULT, this);
		DataSourceHolder.register(Connection.class, this);
		DataSourceHolder.register(DataSource.class, this);
		DataSourceHolder.register(JdbcTemplate.class, this);
	}

	/**
	 * 原生DataSource
	 * @param key key
	 * @return DataSource
	 */
	public static DataSource datasource(String key) {
		DataSource datasource = null;
		DataRuntime runtime = RuntimeHolder.runtime(key);
		if(null != runtime) {
			JdbcTemplate jdbc = (JdbcTemplate) runtime.getProcessor();
			if(null != jdbc) {
				datasource = jdbc.getDataSource();
			}
		}
		return datasource;
	}

	public static DataSource datasource() {
		return datasource("default");
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
	 * 1.加载配置文件，转发给各个DataSourceLoader, 各个DataSourceLoder过滤适用的数据源(主要根据url和type)
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
	 * 添加数据源，同时添加事务与service
	 * @param key 数据源名称
	 * @param datasource 数据源bean id
	 * @param override 是否覆盖同名数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	public String runtime(String key, String datasource, boolean override) throws Exception {
		if(null != datasource) {
			DataSourceHolder.check(key, override);
			regTransactionManager(key, datasource);
			DataRuntime runtime = SpringJDBCRuntimeHolder.instance().reg(key, datasource);
			if(null != runtime) {
				Map<String, Object> param = params.get(key);
				if(null != param) {
					runtime.setDriver(param.get("driver") + "");
					String url = param.get("url") + "";
					runtime.setUrl(url);
					String adapter = param.get("adapter")+"";
					if(BasicUtil.isEmpty(adapter)) {
						adapter = DataSourceUtil.parseAdapterKey(url);
					}
					runtime.setAdapterKey(adapter);
					String catalog = param.get("catalog")+"";
					if(BasicUtil.isEmpty(catalog)) {
						catalog = DataSourceUtil.parseCatalog(url);
					}
					if(ConfigTable.KEEP_ADAPTER == 1) {
						runtime.setCatalog(catalog);
					}

					String schema = param.get("schema")+"";
					if(BasicUtil.isEmpty(schema)) {
						schema = DataSourceUtil.parseSchema(url);
					}
					if(ConfigTable.KEEP_ADAPTER == 1) {
						runtime.setSchema(schema);
					}
				}
			}
		}
		return datasource;
	}
	public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
		DataRuntime runtime = null;
		if(datasource instanceof DataSource) {
			if(null != ConfigTable.environment) {
				DataSourceHolder.check(key, override);
				//创建事务管理器
				regTransactionManager(key, (DataSource)datasource);
				runtime = SpringJDBCRuntimeHolder.instance().reg(key, (DataSource)datasource);
				if(null == adapter && null != type) {
					adapter = DriverAdapterHolder.getAdapter(type);
				}
				if(null != adapter) {
					runtime.setAdapter(adapter);
				}
			}else{
				//spring还没加载完先缓存起来，最后统一注册
				if(!caches.containsKey(key) || override) {
					caches.put(key, datasource);
				}
			}
		}
		return runtime;
	}

	/**
	 * 在spring启动之前注册的数据源
	 */
	public void loadCache() {
		for(String key:caches.keySet()) {
			Object datasource = caches.get(key);
			try {
				runtime(key, datasource, true);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 检测数据源是否连接正常
	 * @param datasource 数据源名称
	 * @return boolean
	 */
	public boolean validate(String datasource) throws Exception {
		DataRuntime runtime = RuntimeHolder.runtime(datasource);
		return validate(runtime);
	}

	public boolean validate(JdbcTemplate jdbc) throws Exception {
		return validate(jdbc.getDataSource());
	}
	public boolean validate(DataSource datasource) throws Exception {
		Connection con = null;
		try{
			con = DataSourceUtils.getConnection(datasource);
		}finally {
			if (null != con && !DataSourceUtils.isConnectionTransactional(con, datasource)) {
				DataSourceUtils.releaseConnection(con, datasource);
			}
		}
		return true;
	}
	public boolean validate(DataRuntime runtime) throws Exception {
		JdbcTemplate jdbc = (JdbcTemplate) runtime.getProcessor();
		return validate(jdbc);
	}




	/* *****************************************************************************************************************
	 *
	 *                                                事务相关
	 *
	 * -----------------------------------------------------------------------------------------------------------------
	 * 注册事务管理器
	 * 启动事务
	 * 提交事务
	 * 回滚事务
	 *
	 * ****************************************************************************************************************/

	public String regTransactionManager(String key, DataSource datasource, boolean primary) {

		/*String tm_id = DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key;
		if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
			//事务管理器
			BeanDefine define = new DefaultBeanDefine(DataSourceTransactionManager.class);
			define.addValue("dataSource", datasource);
			define.setPrimary(primary);
			ConfigTable.environment().regBean(tm_id, define);
			log.info("[创建事务控制器][数据源:{}][primary:{}][bean:{}]", key, primary, tm_id);
		}*/
		if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
			TransactionManage.reg(key, new SpringTransactionManage(datasource));
		}
		return key;
	}

}
