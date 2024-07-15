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

package org.anyline.data.nebula.datasource;

import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.nebula.runtime.NebulaRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NebulaDataSourceHolder extends AbstractDataSourceHolder {

	private static final NebulaDataSourceHolder instance = new NebulaDataSourceHolder();
	public static NebulaDataSourceHolder instance() {
		return instance;
	}

	public NebulaDataSourceHolder(){
		DataSourceHolder.register(SessionPool.class, this);
	}

	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
				prefix += ".";
			}
			Map<String, Object> map = new HashMap<>();
			String url = value(prefix, "url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}
			return inject(key, prefix, map, true);
		} catch (Exception e) {
			log.error("注册nebula数据源 异常:", e);
		}
		return null;
	}

	@Override
	public String create(String key, DatabaseType type, String url, String user, String password) throws Exception {
		return null;
	}

	@Override
	public String create(String key, String prefix) {
		return reg(key, prefix);
	}

	@Override
	public DataSource create(String key, Connection connection, boolean override) {
		return null;
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
		return null;
	}

	@Override
	public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
		return null;
	}

	@Override
	public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
		DataSourceHolder.check(key, override);
		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		try {
			String url =  value(params, "url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				url = value(prefix, "url", String.class, null);
			}
			if(BasicUtil.isEmpty(url)) {
				return null;
			}

			String adapter = value(prefix, params, "adapter", String.class, null);
			//只解析nebula系列
			if(!url.toLowerCase().startsWith("nebula:") && !"nebula".equalsIgnoreCase(adapter)) {
				return null;
			}

			List<HostAddress> hosts = new ArrayList<>();
			String[] urls = url.split(",");
			String space = value(prefix, params, "space", String.class, null);
			for(String item:urls){
				String[] splits = item.split(":");
				String ip = splits[0];
				//    nebula://localhost:9696,localhost:9696/sso
				if(ip.contains("nebula://")){
					ip = ip.replace("nebula://", "");
				}
				String point_ = splits[1];
				if(point_.contains("/")){
					String[] tmps = point_.split("/");
					point_ = tmps[0];
					space = tmps[1];
				}

				int point = BasicUtil.parseInt(point_, 9669);
				HostAddress host = new HostAddress(ip, point);
				hosts.add(host);
			}
			String user = value(prefix, params, "user", String.class, null);
			String password = value(prefix, params, "password", String.class, null);

			SessionPoolConfig config = new SessionPoolConfig(hosts, space, user, password);
			int cleanTime = value(prefix, params, "cleanTime", Integer.class, 0);
			if(cleanTime >0) {
				config.setCleanTime(cleanTime);
			}
			int healthCheckTime = value(prefix, params, "healthCheckTime", Integer.class, 0);
			if(healthCheckTime >0) {
				config.setHealthCheckTime(healthCheckTime);
			}
			int timeout = value(prefix, params, "timeout", Integer.class, 0);
			if(timeout >0) {
				config.setTimeout(timeout);
			}
			int maxSessionSize = value(prefix, params, "maxSessionSize", Integer.class, 0);
			if(timeout >0) {
				config.setMaxSessionSize(maxSessionSize);
			}
			int minSessionSize = value(prefix, params, "minSessionSize", Integer.class, 0);
			if(timeout >0) {
				config.setMinSessionSize(minSessionSize);
			}

			SessionPool pool = new SessionPool(config);
			if (!pool.init()) {
				throw new RuntimeException("Nebula连接池初始化失败");
			}
			DataSourceHolder.params.put(key, params);
			NebulaRuntimeHolder.instance().reg(key, pool);
		} catch (Exception e) {
			log.error("[注入数据源失败][type:nebula][key:{}][msg:{}]", key, e.toString());
			log.error("注入数据源 异常:", e);
			return null;
		}
		return datasource_id;
	}
}
