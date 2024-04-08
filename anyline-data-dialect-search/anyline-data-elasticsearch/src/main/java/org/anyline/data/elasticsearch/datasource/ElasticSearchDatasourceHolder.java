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


package org.anyline.data.elasticsearch.datasource;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceKeyMap;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.elasticsearch.runtime.ElasticSearchRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.Database;
import org.anyline.proxy.DataSourceHolderProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.sql.DataSource;
import java.util.*;

public class ElasticSearchDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder{
	private Logger log = LoggerFactory.getLogger(ElasticSearchDataSourceHolder.class);

	public ElasticSearchDataSourceHolder(){
		DataSourceHolderProxy.reg(DataSource.class, this);
		DataSourceHolderProxy.reg(RestClient.class, this);
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
		param.put("type","ElasticSearchDataSource");
		param.put("url", url);
		param.put("user", user);
		param.put("password", password);
		return reg(key, param);
	}

	@Override
	public String reg(String key, Map<String, Object> param, boolean override) throws Exception {
		String ds_id = inject(key, param, override);
		return init(key, ds_id, override);
	}

	@Override
	public String reg(String key, Map<String, Object> param) throws Exception {
		return reg(key, param, true);
	}

	public RestClient reg(String key, RestClient client, boolean override) throws Exception {
		return init(key, client, override);
	}
	public RestClient reg(String key, RestClient client) throws Exception {
		return init(key, client, false);
	}

	@Override
	public String reg(String key, String prefix) {
		try {
			if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
				prefix += ".";
			}
			String url = ConfigTable.worker.string(prefix, "url");
			if(BasicUtil.isEmpty(url)){
				return null;
			}

			String type = ConfigTable.worker.string(prefix, "type");
			if(null == type){//未设置类型 先取默认数据源类型
				type = ConfigTable.worker.string(prefix.substring(0, prefix.length()- key.length()-1), "type");
			}

			if(null == type || !type.contains("ElasticSearchDataSource")){
				//只注册es驱动
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

	public String inject(String key, Map params, boolean over) throws Exception {
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
			url = ConfigTable.worker.string(prefix, "url");
		}
		if(BasicUtil.isEmpty(url)){
			return null;
		}
		String type = value(params, "type", String.class, null);
		if(BasicUtil.isEmpty(type)){
			type = ConfigTable.worker.string(prefix, "type");
		}
		if(null == type || !type.contains("ElasticSearchDataSource")){
			//只注册ElasticSearchDataSource类型
			return null;
		}

		String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
		try {
			String[] hosts = url.split(",");
			HttpHost[] posts = new HttpHost[hosts.length];
			int idx = 0;
			for(String host:hosts){
				String[] tmps = host.split(":");
				String schema = tmps[0];
				String ip = tmps[1].replace("//","");
				int port = BasicUtil.parseInt(tmps[2], 9200);
				posts[idx++] = new HttpHost(ip, port, schema);
			}
			RestClient client = RestClient.builder(posts)
					.setRequestConfigCallback(requestConfigBuilder -> {
						//设置连接超时时间
						requestConfigBuilder.setConnectTimeout(value(prefix, "connectTimeout", Integer.class, 10000));
						requestConfigBuilder.setSocketTimeout(value(prefix, "socketTimeout", Integer.class, 10000));
						requestConfigBuilder.setConnectionRequestTimeout(value(prefix, "connectionRequestTimeout", Integer.class, 10000));
						return requestConfigBuilder;
					}).setFailureListener(new RestClient.FailureListener() {
						//某节点失败
						@Override
						public void onFailure(Node node) {
							log.error("[ ElasticSearchClient ] >>  node :{}, host:{}, fail !", node.getName(),
									node.getHost());
						}
					}).setHttpClientConfigCallback(httpSyncClientBuilder -> {
						try {
							//设置信任ssl访问
							SSLContext sslContext = SSLContexts.createDefault();
							// 设置协议http和https对应的处理socket链接工厂的对象
							Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder
									.<SchemeIOSessionStrategy>create()
									.register("http", NoopIOSessionStrategy.INSTANCE)
									.register("https", new SSLIOSessionStrategy(sslContext))
									.build();

							// 配置io线程
							IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
									.setIoThreadCount(Runtime.getRuntime().availableProcessors())
									// .setIoThreadCount(2)
									.build();

							ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

							//使用Httpclient连接池的方式配置(推荐)，同时支持netty，okHttp以及其他http框架
							PoolingNHttpClientConnectionManager poolConnManager = new PoolingNHttpClientConnectionManager(ioReactor,
									null, sessionStrategyRegistry, (DnsResolver) null);
							// 最大连接数
							poolConnManager.setMaxTotal(value(prefix, "maxTotalConnect", Integer.class, 100));
							// 同路由并发数
							poolConnManager.setDefaultMaxPerRoute(value(prefix, "maxConnectPerRoute", Integer.class, 10));
							//配置连接池
							httpSyncClientBuilder.setConnectionManager(poolConnManager);
							//设置默认请求头
							List<Header> headers = getDefaultHeaders();
							httpSyncClientBuilder.setDefaultHeaders(headers);
							// 设置长连接策略
							httpSyncClientBuilder.setKeepAliveStrategy(connectionKeepAliveStrategy(null, value(prefix, "keepAliveTime", Integer.class, 10) ));
							httpSyncClientBuilder.disableAuthCaching();
						} catch (IOReactorException e) {
							log.error("ES的Http异步连接池配置错误", e);
						}
						String user = value(prefix, "user", String.class, null);
						String password = value(prefix, "password", String.class, null);
						return getHttpAsyncClientBuilder(httpSyncClientBuilder, user, password);
					}).build();
			ConfigTable.worker.regBean(datasource_id, client);

		} catch (Exception e) {
			log.error("[注册数据源失败][type:ElasticSearch][key:{}][msg:{}]", key, e.toString());
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

	private String init(String key, String client, boolean override) throws Exception {
		if(null != client) {
			check(key, override);
			Object bean = ConfigTable.worker.getBean(client);
			if(bean instanceof RestClient) {
				ElasticSearchRuntimeHolder.reg(key, (RestClient)bean, null);
			}
		}
		return client;
	}
	private RestClient init(String key, RestClient client, boolean override) throws Exception {
		if(null != client) {
			check(key, override);
			ElasticSearchRuntimeHolder.reg(key, client, null);
		}
		return client;
	}

	@Override
	public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return exeTemporary( datasource, database, adapter);
	}
	private DataRuntime exeTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
		return ElasticSearchRuntimeHolder.temporary( datasource, database, adapter);
	}

	*/
/**
	 * 检测数据源是否连接正常
	 * @param ds 数据源名称
	 * @return boolean
	 *//*

	public boolean validate(String ds){
		return validate(RuntimeHolder.runtime(ds));
	}
	public boolean validate(){
		return validate(RuntimeHolder.runtime());
	}
	public boolean validate(DataRuntime runtime){
		RestClient client = (RestClient) runtime.getProcessor();
		return validate(client);
	}

	public boolean validate(RestClient client){
		try{
			return exeValidate(client);
		}catch (Exception e){
			return false;
		}
	}

	public boolean exeValidate(RestClient client){
		return client.isRunning();
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
		ElasticSearchRuntimeHolder.destroy(datasource);
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

	*/
/**
	 * 设置请求头
	 *
	 * @return
	 *//*

	private List<Header> getDefaultHeaders() {
		List<Header> headers = new ArrayList<>();
*/
/*        headers.add(new BasicHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537" +
                        ".36"));
        headers.add(new BasicHeader("Accept-Encoding","gzip,deflate"));
        headers.add(new BasicHeader("Accept-Language","zh-CN"));*//*

		headers.add(new BasicHeader("Connection","Keep-Alive"));
		return headers;
	}
	private HttpAsyncClientBuilder getHttpAsyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder, String user, String password) {
		if (BasicUtil.isEmpty(user) || BasicUtil.isEmpty(password)) {
			return httpAsyncClientBuilder;
		}
		//账密设置
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		//es账号密码（一般使用,用户elastic）
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(user, password));
		httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		return httpAsyncClientBuilder;
	}

	*/
/**
	 * 配置长连接保持策略
	 *
	 * @return ConnectionKeepAliveStrategy
	 *//*

	private ConnectionKeepAliveStrategy connectionKeepAliveStrategy(Map<String, Integer> keepAliveTargetHost, int keepAliveTime) {
		return (response, context) -> {
			// Honor 'keep-alive' header
			HeaderElementIterator it = new BasicHeaderElementIterator(
					response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			while (it.hasNext()) {
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();
				if (value != null && "timeout".equalsIgnoreCase(param)) {
					try {
						return Long.parseLong(value) * 1000;
					} catch (NumberFormatException ignore) {
						log.error("解析长连接过期时间异常", ignore);
					}
				}
			}
			HttpHost target = (HttpHost) context.getAttribute(
					HttpClientContext.HTTP_TARGET_HOST);
			//如果请求目标地址,单独配置了长连接保持时间,使用该配置
			Optional<Map.Entry<String, Integer>> any =
					Optional.ofNullable(keepAliveTargetHost).orElseGet(HashMap::new)
							.entrySet().stream().filter(
									e -> e.getKey().equalsIgnoreCase(target.getHostName())).findAny();
			//否则使用默认长连接保持时间
			return any.map(en -> en.getValue() * 1000L).orElse(keepAliveTime * 1000L);
		};
	}
}
*/
