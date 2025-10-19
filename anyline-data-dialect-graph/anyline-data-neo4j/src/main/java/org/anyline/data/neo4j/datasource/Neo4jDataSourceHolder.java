/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.neo4j.datasource;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.neo4j.runtime.Neo4jRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Neo4jDataSourceHolder extends AbstractDataSourceHolder {

	private static final Neo4jDataSourceHolder instance = new Neo4jDataSourceHolder();
	public static Neo4jDataSourceHolder instance() {
		return instance;
	}

	public Neo4jDataSourceHolder() {
		DataSourceHolder.register(Session.class, this);
	}

	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
				prefix += ".";
			}
			Map<String, Object> map = new HashMap<>();
			String url = value(prefix, map,"url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}
			return inject(key, prefix, map, true);
		} catch (Exception e) {
			log.error("注册Neo4j数据源 异常:", e);
		}
		return null;
	}

	@Override
	public String create(String key, String prefix) {
		return reg(key, prefix);
	}

	@Override
	public boolean validate(DataRuntime runtime) throws Exception {
		return false;
	}

	@Override
	public String regTransactionManager(String key, DataSource datasource, boolean primary) {
		return null;
	}

	@Override
	public String runtime(String key, String datasource, boolean override) throws Exception {
		return datasource;
	}

	@Override
	public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
		DataRuntime runtime = null;
		if(datasource instanceof Driver) {
			if(null != ConfigTable.environment) {
				DataSourceHolder.check(key, override);
				//创建事务管理器

				runtime = Neo4jRuntimeHolder.instance().reg(key, (Driver)datasource);
				if(null == adapter && null != type) {
					adapter = DriverAdapterHolder.getAdapter(type);
				}
				if(null != adapter) {
					runtime.setAdapter(adapter);
				}
			}else{
				//上下文还没加载完先缓存起来，最后统一注册
				if(!caches.containsKey(key) || override) {
					caches.put(key, datasource);
				}
			}
		}
		return runtime;
	}

	@Override
	public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
		DataSourceHolder.check(key, override);
		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		try {
			String url =  value(prefix, params, "url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}

			String adapter = value(prefix, params, "adapter", String.class, null);
			//只解析Neo4j系列
			if(!url.toLowerCase().startsWith("bolt:") && !"neo4j".equalsIgnoreCase(adapter)) {
				return null;
			}
			String user = value(prefix, params, "user", String.class, null);
			String password = value(prefix, params, "password", String.class, null);
			String token = value(prefix, params, "token,ticket", String.class, null);
			Driver driver = null;
			if(BasicUtil.isNotEmpty(token)) {
				driver = GraphDatabase.driver(url, AuthTokens.kerberos(token));
			} else if(BasicUtil.isNotEmpty(user)){
				driver = GraphDatabase.driver(url, AuthTokens.basic(user, password));
			}
			DataSourceHolder.params.put(key, params);
			Neo4jRuntimeHolder.instance().reg(key, driver);
		} catch (Exception e) {
			log.error("[注入数据源失败][type:Neo4j][key:{}][msg:{}]", key, e.toString());
			log.error("注入数据源 异常:", e);
			return null;
		}
		return datasource_id;
	}
}
