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


package org.anyline.data.opensearch.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.opensearch.runtime.OpenSearchRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
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
import org.opensearch.client.Node;
import org.opensearch.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.sql.DataSource;
import java.util.*;

@AnylineComponent("anyline.data.opensearch.datasource.holder")
public class OpenSearchDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {

    private static final OpenSearchDataSourceHolder instance = new OpenSearchDataSourceHolder();

    public static OpenSearchDataSourceHolder instance() {
        return instance;
    }

    public OpenSearchDataSourceHolder() {
        DataSourceHolder.register(RestClient.class, this);
        DataSourceHolder.register(DatabaseType.OpenSearch, this);
    }

    public String reg(String key, String prefix) {
        try {
            if (BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
                prefix += ".";
            }
            String url = value(prefix, params, "url", String.class, null);
            if (BasicUtil.isEmpty(url)) {
                return null;
            }

            String adapter = value(prefix, params, "adapter", String.class, null);
            if (null == adapter) {
                return null;
            }
            adapter = adapter.toLowerCase();
            if (adapter.contains("opensearch")) {
                Map<String, Object> map = new HashMap<>();
                return inject(key, prefix, map, true);
            }
        } catch (Exception e) {
            log.error("注册OpenSearch数据源 异常:", e);
        }
        return null;
    }

    public String inject(String key, Map params, boolean over) throws Exception {
        return inject(key, null, params, over);
    }

    public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
        DataSourceHolder.check(key, override);
        String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        Map<String, Object> cache = DataSourceHolder.params.get(key);
        if (null == cache) {
            cache = new HashMap<>();
            DataSourceHolder.params.put(key, cache);
        }
        String url = value(prefix, params, "url", String.class, null);
        if (BasicUtil.isEmpty(url)) {
            return null;
        }
        String adapter = value(prefix, params, "adapter", String.class, null);
        if (null == adapter) {
            return null;
        }
        adapter = adapter.toLowerCase();
        if (!adapter.contains("opensearch")) {
            return null;
        }
        try {
            String[] hosts = url.split(",");
            HttpHost[] posts = new HttpHost[hosts.length];
            int idx = 0;
            for (String host : hosts) {
                String[] tmps = host.split(":");
                String schema = tmps[0];
                String ip = tmps[1].replace("//", "");
                int port = BasicUtil.parseInt(tmps[2], 9200);
                posts[idx++] = new HttpHost(ip, port, schema);
            }
            RestClient client = RestClient.builder(posts)
                    .setRequestConfigCallback(requestConfigBuilder -> {
                        requestConfigBuilder.setConnectTimeout(value(prefix, params, "connectTimeout", Integer.class, 10000));
                        requestConfigBuilder.setSocketTimeout(value(prefix, params, "socketTimeout", Integer.class, 10000));
                        requestConfigBuilder.setConnectionRequestTimeout(value(prefix, params, "connectionRequestTimeout", Integer.class, 10000));
                        return requestConfigBuilder;
                    }).setFailureListener(new RestClient.FailureListener() {
                        @Override
                        public void onFailure(Node node) {
                            log.error("[ OpenSearchClient ] >>  node :{}, host:{}, fail !", node.getName(),
                                    node.getHost());
                        }
                    }).setHttpClientConfigCallback(httpSyncClientBuilder -> {
                        try {
                            SSLContext sslContext = SSLContexts.createDefault();
                            Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder
                                    .<SchemeIOSessionStrategy>create()
                                    .register("http", NoopIOSessionStrategy.INSTANCE)
                                    .register("https", new SSLIOSessionStrategy(sslContext))
                                    .build();

                            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                                    .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                                    .build();

                            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

                            PoolingNHttpClientConnectionManager poolConnManager = new PoolingNHttpClientConnectionManager(ioReactor,
                                    null, sessionStrategyRegistry, (DnsResolver) null);
                            poolConnManager.setMaxTotal(value(prefix, params, "maxTotalConnect", Integer.class, 100));
                            poolConnManager.setDefaultMaxPerRoute(value(prefix, params, "maxConnectPerRoute", Integer.class, 10));
                            httpSyncClientBuilder.setConnectionManager(poolConnManager);
                            List<Header> headers = getDefaultHeaders();
                            httpSyncClientBuilder.setDefaultHeaders(headers);
                            httpSyncClientBuilder.setKeepAliveStrategy(connectionKeepAliveStrategy(null, value(prefix, params, "keepAliveTime", Integer.class, 10)));
                        } catch (IOReactorException e) {
                            log.error("OpenSearch的Http异步连接池配置错误", e);
                        }
                        String user = value(prefix, params, "user", String.class, null);
                        String password = value(prefix, params, "password", String.class, null);
                        return getHttpAsyncClientBuilder(httpSyncClientBuilder, user, password);
                    }).build();
            OpenSearchRuntimeHolder.instance().reg(key, client);
        } catch (Exception e) {
            log.error("[注册数据源失败][type:OpenSearch][key:{}][msg:{}]", key, e.toString());
            return null;
        }
        return datasource_id;
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
        RestClient client = (RestClient) runtime.getProcessor();
        return validate(client);
    }

    public boolean validate(RestClient client) {
        try {
            return exeValidate(client);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exeValidate(RestClient client) {
        return client.isRunning();
    }

    @Override
    public String regTransactionManager(String key, DataSource datasource, boolean primary) {
        return "";
    }

    @Override
    public String runtime(String key, String datasource, boolean override) throws Exception {
        return datasource;
    }

    @Override
    public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
        return null;
    }

    private List<Header> getDefaultHeaders() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN"));
        headers.add(new BasicHeader("Connection", "Keep-Alive"));
        return headers;
    }

    private HttpAsyncClientBuilder getHttpAsyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder, String user, String password) {
        if (BasicUtil.isEmpty(user) || BasicUtil.isEmpty(password)) {
            return httpAsyncClientBuilder;
        }
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user, password));
        httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        return httpAsyncClientBuilder;
    }

    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy(Map<String, Integer> keepAliveTargetHost, int keepAliveTime) {
        return (response, context) -> {
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
            Optional<Map.Entry<String, Integer>> any =
                    Optional.ofNullable(keepAliveTargetHost).orElseGet(HashMap::new)
                            .entrySet().stream().filter(
                                    e -> e.getKey().equalsIgnoreCase(target.getHostName())).findAny();
            return any.map(en -> en.getValue() * 1000L).orElse(keepAliveTime * 1000L);
        };
    }
}