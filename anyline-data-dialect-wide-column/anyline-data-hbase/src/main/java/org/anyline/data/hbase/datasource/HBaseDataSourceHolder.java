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




package org.anyline.data.hbase.datasource;

import org.anyline.annotation.Component;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.hbase.runtime.HBaseRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component("anyline.environment.data.datasource.holder.hbase")
public class HBaseDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder{
	private static final HBaseDataSourceHolder instance = new HBaseDataSourceHolder();
	public static HBaseDataSourceHolder instance() {
		return instance;
	}
	public HBaseDataSourceHolder() {
		DataSourceHolder.register(Connection.class, this);
	}

	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
				prefix += ".";
			}
			String url = value(prefix, "url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}
			String adapter = value(prefix, "adapter", String.class, null);
			if(null == adapter) {
				//只注册HBase驱动
				return null;
			}
			adapter = adapter.toLowerCase();
			if(adapter.contains("hbase")) {
				Map<String, Object> map = new HashMap<>();
				return inject(key, prefix, map, true);
			}
		} catch (Exception e) {
			log.error("注册HBase数据源 异常:", e);
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

	public String inject(String key, Map params, boolean over) throws Exception {
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

	public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
		DataSourceHolder.check(key, override);
		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		Map<String, Object> cache = DataSourceHolder.params.get(key);
		if(null == cache) {
			cache = new HashMap<>();
			DataSourceHolder.params.put(key, cache);
		}
		String hosts =  value(prefix, params, "quorum,hosts", String.class, null);
		String port = value(prefix, params, "port", String.class, null);
		if(BasicUtil.isEmpty(hosts)) {
			return null;
		}
		String adapter = value(prefix, params, "adapter", String.class, null);
		if(null == adapter){
			return null;
		}
		adapter = adapter.toLowerCase();
		if(!adapter.contains("hbase")) {
			//只注册HBase类型
			return null;
		}
		Connection connection = null;
		try {
			//参数说明 https://hbase.apache.org/book.html#hbase_default_configurations
			Configuration conf = HBaseConfiguration.create();
			conf.set("hbase.zookeeper.quorum", hosts);
			conf.set("hbase.zookeeper.property.client", port);
			for (String k:params.keySet()){
				if(k.startsWith("hbase.")) {
					Object v = params.get(k);
					if (null != v) {
						conf.set(k, v.toString());
					}
				}
			}
			try{
				connection = ConnectionFactory.createConnection(conf);
			}catch (Exception e){
				e.printStackTrace();
			}
			HBaseRuntimeHolder.instance().reg(key, connection);
		} catch (Exception e) {
			log.error("[注册数据源失败][type:HBase][key:{}][msg:{}][参数说明:https://hbase.apache.org/book.html#hbase_default_configurations]", key, e.toString());
			return null;
		}
		return datasource_id;
	}


	@Override
	public String create(String key, DatabaseType type, String url, String user, String password) throws Exception {
		return null;
	}

	@Override
	public String create(String key, String prefix) {
		return reg(key, prefix);
	}

	/**
	 * 检测数据源是否连接正常
	 * @param ds 数据源名称
	 * @return boolean
	 */

	public boolean validate(String ds) {
		return validate(RuntimeHolder.runtime(ds));
	}
	public boolean validate() {
		return validate(RuntimeHolder.runtime());
	}
	public boolean validate(DataRuntime runtime) {
		Connection client = (Connection) runtime.getProcessor();
		return validate(client);
	}

	public boolean validate(Connection client) {
		try{
			return exeValidate(client);
		}catch (Exception e) {
			return false;
		}
	}

	public boolean exeValidate(Connection client) {
		return !client.isClosed();
	}

	@Override
	public String regTransactionManager(String key, DataSource datasource, boolean primary) {
		return "";
	}

	@Override
	public String runtime(String key, String datasource, boolean override) throws Exception {
		return null;
	}

	@Override
	public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
		return null;
	}
}

