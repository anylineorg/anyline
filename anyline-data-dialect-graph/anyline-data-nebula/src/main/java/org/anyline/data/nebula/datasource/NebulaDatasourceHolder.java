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
 *//*

 
 
*/
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
 *//*


package org.anyline.data.nebula.datasource;

import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceKeyMap;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.nebula.runtime.NebulaRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.Database;
import org.anyline.proxy.DataSourceHolderProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;

public class NebulaDataSourceHolder extends DataSourceHolder {
	private Logger log = LoggerFactory.getLogger(NebulaDataSourceHolder.class);

	public NebulaDataSourceHolder(){
		DataSourceHolderProxy.reg(DataSource.class, this);
		DataSourceHolderProxy.reg(SessionPool.class, this);
	}

	*/
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
	 * ****************************************************************************************************************//*


	*/
/**
	 * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
	 * @param key 切换数据源依据 默认key=datasource
	 * @param url url
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 *//*

	public String reg(String key, String url, String user, String password) throws Exception {
		Map<String, Object> param = new HashMap<>();
		param.put("type","NebulaDataSource");
		param.put("url", url);
		param.put("user", user);
		param.put("password", password);
		return reg(key, param);
	}

	public String reg(String key, Map<String, Object> param, boolean override) throws Exception {
		String ds_id = inject(key, param, override);
		return init(key, ds_id, override);
	}

	public String reg(String key, Map<String, Object> param) throws Exception {
		return reg(key, param, true);
	}
	public SessionPool reg(String key, SessionPool client, boolean override) throws Exception {
		return init(key, client, override);
	}
	public SessionPool reg(String key, SessionPool client) throws Exception {
		return init(key, client, false);
	}

	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
				prefix += ".";
			}
			//nebula://localhost:39669/simple
			String url = value(prefix, "url", String.class, null);
			if(null == url || !url.contains("nebula")){
				//只注册nebula驱动
				return null;
			}
			Map<String, Object> map = new HashMap<>();
			String ds = inject(key, prefix, map, true);
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

	*/
/**
	 * 根据params创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(key)
	 * @param params 帐号密码等参数
	 * @return bean.id
	 * @throws Exception Exception
	 *//*

	private String inject(String key, Map params, boolean over) throws Exception {
		return inject(key, null, params, over);
	}

	*/
/**
	 * 根据params与配置文件创建数据源, 同时注入到spring上下文
	 * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(“sso”)
	 * @param prefix 配置文件前缀 如 anyline.datasource.sso
	 * @param params map格式参数
	 * @param override 是否覆盖同名数据源
	 * @return bean.di
	 * @throws Exception Exception
	 *//*

	private String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
		Map<String, Object> cache = DataSourceHolder.params.get(key);
		if(null == cache){
			cache = new HashMap<>();
			DataSourceHolder.params.put(key, cache);
		}
		check(key, override);
		String url =  value(params, "url", String.class, null);
		if(BasicUtil.isEmpty(url)){
			url = value(prefix, "url", String.class, null);
		}
		if(BasicUtil.isEmpty(url)){
			return null;
		}
		String type = value(params, "type", String.class, null);
		if(BasicUtil.isEmpty(type)){
			type = value(prefix, "type", String.class, null);
		}
		String user = value(params, "user", String.class, null);
		if(BasicUtil.isEmpty(user)){
			user = value(prefix, "user", String.class, null);
		}
		String password = value(params, "password", String.class, null);
		if(BasicUtil.isEmpty(password)){
			password = value(prefix, "password", String.class, null);
		}

		if(!url.startsWith("nebula:")){
			//只注册nebula驱动
			return null;
		}
		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		try {
			*/
/*
			nebula://192.168.10.100:9669,192.168.10.101:9669,192.168.10.102:9669/crm
			nebular://host/space
			*//*

			String tmp = url.split("//")[1];
			String[] tmps = tmp.split("/");
			String[] hosts = tmps[0].split(",");
			String space = tmps[1];
			List<HostAddress> list = new ArrayList<>();
			for(String host:hosts){
				tmps = host.split(":");
				String ip = tmps[0].replace("//","");
				int port = BasicUtil.parseInt(tmps[1], 9669);
				list.add(new HostAddress(ip, port));
			}

			SessionPoolConfig config = new SessionPoolConfig(list, space, user, password);

			Integer min = value(params, "minPoolSize,minSessionSize", Integer.class, null);
			if(null == min){
				min = value(prefix, "minPoolSize,minSessionSize", Integer.class, null);
			}
			if(null != min) {
				config.setMinSessionSize(min);
			}
			Integer max = value(params, "maxPoolSize,maxSessionSize", Integer.class, null);
			if(null == max){
				max = value(prefix, "maxPoolSize,maxSessionSize", Integer.class, null);
			}
			if(null != max) {
				config.setMaxSessionSize(max);
			}

			Integer wait = value(params, "wait,waitTime", Integer.class, null);
			if(null == wait){
				wait = value(prefix, "wait,waitTime", Integer.class, null);
			}
			if(null != wait) {
				config.setWaitTime(wait);
			}
			Integer timeout = value(params, "timeout,connectionTimeout", Integer.class, null);
			if(null == timeout){
				timeout = value(prefix, "timeout,connectionTimeout", Integer.class, null);
			}
			if(null != timeout) {
				config.setTimeout(timeout);
			}

			SessionPool session = new SessionPool(config);
			if (!session.init()) {
				log.error("[注册数据源失败][type:Nebula][key:{}]", key);
				return null;
			}
			worker.regBean(datasource_id, session);

		} catch (Exception e) {
			log.error("[注册数据源失败][type:Nebula][key:{}][msg:{}]", key, e.toString());
			return null;
		}
		return datasource_id;
	}

	*/
/**
	 * 添加数据源，同时添加事务与service
	 * @param key 数据源名称
	 * @param client 数据源bean id
	 * @param override 是否覆盖同名数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 *//*

	public String init(String key, String client, boolean override) throws Exception {
		if(null != client) {
			check(key, override);
			Object bean = worker.getBean(client);
			if(bean instanceof SessionPool) {
				NebulaRuntimeHolder.reg(key, (SessionPool)bean, null);
			}
		}
		return client;
	}
	private SessionPool init(String key, SessionPool client, boolean override) throws Exception {
		if(null != client) {
			check(key, override);
			NebulaRuntimeHolder.reg(key, client, null);
		}
		return client;
	}

	@Override
	public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return exeTemporary( datasource, database, adapter);
	}
	private DataRuntime exeTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return NebulaRuntimeHolder.temporary( datasource, database, adapter);
	}

	*/
/**
	 * 检测数据源是否连接正常
	 * @param datasource 数据源名称
	 * @return boolean
	 *//*

	public boolean validate(String datasource){
		return validate(RuntimeHolder.runtime(ds));
	}
	public boolean validate(){
		return validate(RuntimeHolder.runtime());
	}
	public boolean validate(DataRuntime runtime){
		SessionPool client = (SessionPool) runtime.getProcessor();
		return validate(client);
	}

	public boolean validate(SessionPool client){
		try{
			return exeValidate(client);
		}catch (Exception e){
			return false;
		}
	}

	public boolean exeValidate(SessionPool client){
		return client.isActive();
	}

	@Override
	public boolean callValidate(DataRuntime runtime) {
		return validate(runtime);
	}

	@Override
	public boolean callHit(DataRuntime runtime) throws Exception {
		return validate(runtime);
	}

	public void destroy(String datasource) {
		exedestroy(datasource);
	}
	@Override
	public void calldestroy(String datasource) {
		exedestroy(datasource);
	}
	private void exedestroy(String datasource){
		NebulaRuntimeHolder.destroy(datasource);
	}
	public List<String> copy(){
		return copy("default");
	}
	public List<String> copy(String datasource){
		DataRuntime runtime = RuntimeHolder.runtime(datasource);
		return copy(runtime);
	}

	*/
/**
	 * 根据当前数据源查询全部数据库列表，每个数据库创建一个数据源
	 * @param runtime runtime
	 * @return 数据源key列表(全大写)
	 *//*

	public List<String> copy(DataRuntime runtime) {
		return exeCopy(runtime);
	}
	@Override
	public List<String> callCopy(DataRuntime runtime) {
		return exeCopy(runtime);
	}
	private List<String> exeCopy(DataRuntime runtime){
		List<String> list = new ArrayList<>();
		//查看结果
		AnylineService service = ServiceProxy.service(runtime.datasource());
		LinkedHashMap<String, Database> databases = service.metadata().databases();
		Map<String,Object> map = params.get(runtime.datasource());
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
 
}
*/
