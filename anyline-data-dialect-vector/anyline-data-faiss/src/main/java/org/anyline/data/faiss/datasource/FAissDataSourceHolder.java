/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.faiss.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.faiss.runtime.FAissRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@AnylineComponent("anyline.environment.data.datasource.holder.faiss")
public class FAissDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {
	private static final FAissDataSourceHolder instance = new FAissDataSourceHolder();

	public static FAissDataSourceHolder instance() {
		return instance;
	}

	public FAissDataSourceHolder() {
		DataSourceHolder.register("faiss", this);
		DataSourceHolder.register(DatabaseType.Faiss, this);
	}

	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
				prefix += ".";
			}
			String url = value(prefix, "url,uri,host", String.class, null);
			if(BasicUtil.isEmpty(url)) {
				return null;
			}
			String adapter = value(prefix, "adapter", String.class, null);
			if(null == adapter) {
				//只注册faiss驱动
				return null;
			}
			adapter = adapter.toLowerCase();
			if(adapter.contains("faiss")) {
				Map<String, Object> map = new HashMap<>();
				return inject(key, prefix, map, true);
			}
		} catch (Exception e) {
			log.error("注册Faiss数据源 异常:", e);
		}
		return null;
	}

	public String inject(String key, Map params, boolean over) throws Exception {
		return inject(key, null, params, over);
	}

	/**
	 * 根据params与配置文件创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service("sso")
	 * @param prefix 配置文件前缀 如 anyline.datasource.sso
	 * @param params map格式参数
	 * @param override 是否覆盖同名数据源
	 * @return bean.id
	 * @throws Exception Exception
	 */
	public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
		DataSourceHolder.check(key, override);
		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		String url = value(prefix, params, "url,uri,host,path", String.class, null);
		if(BasicUtil.isEmpty(url)) {
			return null;
		}
		String adapter = value(prefix, params, "adapter", String.class, null);
		if(null == adapter) {
			return null;
		}
		adapter = adapter.toLowerCase();
		if(!adapter.contains("faiss")) {
			//只注册faiss类型
			return null;
		}
		try {
			//Faiss 官方没有 Java SDK, 也没有官方服务端协议
			//这里把连接参数封装成客户端交给runtime, 由使用方注入的自定义客户端(自建服务或JNI)替换
			Map<String, Object> config = new LinkedHashMap<>();
			config.put("url", url);
			config.put("index", value(prefix, params, "index,database,dbName", String.class, null));
			config.put("dimension", value(prefix, params, "dimension,dim", Integer.class, null));
			Object client = buildClient(config);
			if(null != client) {
				FAissRuntimeHolder.instance().reg(key, client);
			}
		} catch (Exception e) {
			log.error("[注册数据源失败][type:faiss][key:{}][msg:{}]", key, e.toString());
			return null;
		}
		return datasource_id;
	}

	/**
	 * 构建Faiss客户端<br/>
	 * 默认返回连接参数(索引地址/索引名/维度), 业务方可注入自己的客户端(自建服务或JNI封装)覆盖本方法
	 */
	protected Object buildClient(Map<String, Object> config) {
		return config;
	}

	@Override
	public String create(String key, String prefix) {
		return reg(key, prefix);
	}

	public boolean validate(String ds) {
		return validate(RuntimeHolder.runtime(ds));
	}

	public boolean validate() {
		return validate(RuntimeHolder.runtime());
	}

	public boolean validate(DataRuntime runtime) {
		Object client = runtime.getProcessor();
		return validate(client);
	}

	public boolean validate(Object client) {
		try {
			return exeValidate(client);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean exeValidate(Object client) {
		//Faiss 没有统一的探活接口, 能拿到客户端即认为可用
		return null != client;
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