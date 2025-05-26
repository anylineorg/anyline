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

package org.anyline.data.influxdb.datasource;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.influxdb.runtime.InfluxRuntime;
import org.anyline.data.influxdb.runtime.InfluxRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class InfluxDataSourceHolder extends AbstractDataSourceHolder {

	private static final InfluxDataSourceHolder instance = new InfluxDataSourceHolder();
	public static InfluxDataSourceHolder instance() {
		return instance;
	}

	public InfluxDataSourceHolder() {
		DataSourceHolder.register(InfluxDBClient.class, this);
		DataSourceHolder.register("InfluxDB", this);
		DataSourceHolder.register("Influx", this);
		DataSourceHolder.register(DatabaseType.InfluxDB, this);
	}

	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
				prefix += ".";
			}
			Map<String, Object> map = new HashMap<>();
			String url = value(prefix, map, "url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}
			return inject(key, prefix, map, true);
		} catch (Exception e) {
			log.error("注册Influx数据源 异常:", e);
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
		if(null != datasource) {
			DataSourceHolder.check(key, override);
			DataRuntime runtime = InfluxRuntimeHolder.instance().reg(key, datasource);

			if(null != runtime) {
				Map<String, Object> param = params.get(key);
				if(null != param) {
					runtime.setDriver(param.get("driver") + "");
					String url = param.get("url") + "";
					runtime.setUrl(url);
					String adapter = param.get("adapter")+"";
					if(BasicUtil.isEmpty(adapter)) {
						adapter = org.anyline.data.util.DataSourceUtil.parseAdapterKey(url);
					}
					runtime.setAdapterKey(adapter);
				}
			}
		}
		return datasource;
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
			String url =  value(prefix, params, "url", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}
			String adapter = value(prefix, params, "adapter", String.class, null);
			//只解析Influx系列
			if(null == adapter || !adapter.toLowerCase().contains("influx")) {
				return null;
			}

			String org = value(prefix, params, "org", String.class, null);
			String token = value(prefix, params, "token", String.class, null);
			String bucket = value(prefix, params, "bucket,database", String.class, null);
			String user = value(prefix, params, "user,username,userName", String.class, null);
			String password = value(prefix, params, "password", String.class, null);
			InfluxDBClient client = null;
			if(BasicUtil.isNotEmpty(password)){
				client = InfluxDBClientFactory.create(url, user, password.toCharArray());
			}else {
				InfluxDBClientOptions.Builder builder = InfluxDBClientOptions.builder()
					.url(url)
					.authenticateToken(token.toCharArray());
				if (BasicUtil.isNotEmpty(org)) {
					builder.org(org);
				}
				if (BasicUtil.isNotEmpty(bucket)) {
					builder.bucket(bucket);
				}
				InfluxDBClientOptions options = builder.build();
				client = InfluxDBClientFactory.create(options);
			}

			DataSourceHolder.params.put(key, params);
			InfluxRuntime runtime = (InfluxRuntime)InfluxRuntimeHolder.instance().reg(key, client);
			runtime.bucket(bucket);
			runtime.token(token);
			runtime.org(org);
			runtime.user(user);
			runtime.password(password);
			runtime.setUrl(url);
		} catch (Exception e) {
			log.error("[注入数据源失败][type:Influx][key:{}][msg:{}]", key, e.toString());
			log.error("注入数据源 异常:", e);
			return null;
		}
		return datasource_id;
	}
}
