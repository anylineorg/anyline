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


package org.anyline.data.arango.datasource;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.entity.LoadBalancingStrategy;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.arango.runtime.ArangoRuntime;
import org.anyline.data.arango.runtime.ArangoRuntimeHolder;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@AnylineComponent("anyline.environment.data.datasource.holder.arango")
public class ArangoDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {

    private static final ArangoDataSourceHolder instance = new ArangoDataSourceHolder();
    public static ArangoDataSourceHolder instance() {
        return instance;
    }
    public ArangoDataSourceHolder() {
        DataSourceHolder.register("arango", this);
        DataSourceHolder.register(ArangoDB.class, this);
        DataSourceHolder.register(ArangoDatabase.class, this);
    }
    public String reg(String key, String prefix) {
        try {
            if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
                prefix += ".";
            }
            Map<String, Object> map = new HashMap<>();
            // 支持 url(别名包括 host) 或 configFile 触发注册
            String url = value(prefix, "url", String.class, null);
            String configFile = value(prefix, "configFile,config-file", String.class, null);
            if(BasicUtil.isEmpty(url) && BasicUtil.isEmpty(configFile)) {
                return null;
            }
            return inject(key, prefix, map, true);
        } catch (Exception e) {
            log.error("注册Arango数据源 异常:", e);
        }
        return null;
    }

    @Override
    public String create(String key, DatabaseType database, String url, String user, String password) throws Exception {
        return null;
    }

    @Override
    public String create(String key, String prefix) {
        return reg(key, prefix);
    }

    @Override
    public boolean validate(DataRuntime runtime) throws Exception {
        ArangoDatabase database = ((ArangoRuntime)runtime).getDatabase();
        // 通过 exists() 真正检测连通性, 而不仅读取元数据
        return database.exists();
    }

    @Override
    public String regTransactionManager(String key, DataSource datasource, boolean primary) {
        return "";
    }

    public String regTransactionManager(String key, ArangoDB client, ArangoDatabase database, boolean primary) {
        return "";
    }

    @Override
    public String runtime(String key, String datasource, boolean override) throws Exception {
        return datasource;
    }

    @Override
    public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
        DataRuntime runtime = null;
        ArangoDB client = null;
        if(datasource instanceof ArangoDB) {
            client = (ArangoDB)datasource;
            datasource = client.db(database);
        }
        if(datasource instanceof ArangoDatabase) {
            if(null != ConfigTable.environment) {
                DataSourceHolder.check(key, override);
                //创建事务管理器
                regTransactionManager(key, client, (ArangoDatabase) datasource, true);
                runtime = ArangoRuntimeHolder.instance().reg(key, client, (ArangoDatabase)datasource);
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
     * 根据params与配置文件创建数据源, 同时注入到spring上下文<br/>
     * 支持从配置文件(Anyline前缀)或ArangoDB原生 arangodb.properties 中读取参数<br/>
     * <br/>
     * 基础配置示例:<br/>
     * <pre>
     * anyline.datasource.default.host=localhost
     * anyline.datasource.default.port=8529
     * anyline.datasource.default.user=root
     * anyline.datasource.default.password=openSesame
     * anyline.datasource.default.database=mydb
     * </pre>
     * 生产环境连接池配置示例:<br/>
     * <pre>
     * anyline.datasource.default.maxConnections=20
     * anyline.datasource.default.connectionTtl=300000
     * anyline.datasource.default.keepAliveInterval=30
     * </pre>
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
        try {
            // ====== 1. 读取连接参数 ======
            String host = value(prefix, params, "host,hosts", String.class, null);
            int port = value(prefix, params, "port", Integer.class, 8529);
            String user = value(prefix, params, "user,user-name", String.class, null);
            String password = value(prefix, params, "password", String.class, "");
            String jwt = value(prefix, params, "jwt", String.class, "");
            String database = value(prefix, params, "database,db", String.class, null);

            // 可以从 ArangoDB 原生配置文件中加载
            String configFile = value(prefix, params, "configFile,config-file", String.class, null);

            if(BasicUtil.isEmpty(database)) {
                log.error("[注入数据源失败][type:arango][key:{}][msg:未设置database]", key);
                return null;
            }

            // ====== 2. 读取高级配置参数 ======
            // 协议: HTTP2_JSON, HTTP_JSON, HTTP2_VPACK, HTTP_VPACK, VST
            String protocol = value(prefix, params, "protocol", String.class, null);
            // 连接/请求超时(毫秒), 0=无超时
            Integer timeout = value(prefix, params, "timeout", Integer.class, null);
            // SSL
            Boolean useSsl = value(prefix, params, "useSsl,use-ssl,ssl", Boolean.class, null);
            Boolean verifyHost = value(prefix, params, "verifyHost,verify-host", Boolean.class, null);

            // ====== 3. 连接池参数(生产环境必备) ======
            // 每个主机的最大连接数 VST/HTTP2默认1, HTTP1.1默认20
            Integer maxConnections = value(prefix, params, "maxConnections,max-connections", Integer.class, null);
            // 连接最大存活时间(毫秒), 过期自动关闭重建
            Long connectionTtl = value(prefix, params, "connectionTtl,connection-ttl", Long.class, null);
            // VST保活探测间隔(秒)
            Integer keepAliveInterval = value(prefix, params, "keepAliveInterval,keep-alive-interval", Integer.class, null);

            // ====== 4. 集群/故障转移参数 ======
            // 是否自动发现集群中所有协调节点
            Boolean acquireHostList = value(prefix, params, "acquireHostList,acquire-host-list", Boolean.class, null);
            // 主机列表刷新间隔(毫秒), 默认3600000(1小时)
            Integer acquireHostListInterval = value(prefix, params, "acquireHostListInterval,acquire-host-list-interval", Integer.class, null);
            // 负载均衡策略: NONE, ROUND_ROBIN, ONE_RANDOM
            String loadBalancingStrategy = value(prefix, params, "loadBalancingStrategy,load-balancing-strategy", String.class, null);

            // ====== 5. 构建 ArangoDB 客户端 ======
            ArangoDB.Builder builder = new ArangoDB.Builder();

            // 5a. 如果指定了 ArangoDB 原生配置文件, 先加载作为基础配置
            if(BasicUtil.isNotEmpty(configFile)) {
                try {
                    ArangoConfigProperties props = ArangoConfigProperties.fromFile(configFile, null);
                    builder.loadProperties(props);
                    log.info("[arango config file loaded][file:{}]", configFile);
                } catch (Exception e) {
                    log.warn("[arango config file load failed][file:{}][msg:{}]", configFile, e.toString());
                }
            }

            // 5b. 认证 — JWT优先
            if(BasicUtil.isNotEmpty(jwt)) {
                builder.jwt(jwt);
            } else {
                if(BasicUtil.isNotEmpty(user)) {
                    builder.user(user);
                }
                if(BasicUtil.isNotEmpty(password)) {
                    builder.password(password);
                }
            }

            // 5c. 主机列表 — 支持逗号分隔多主机
            if(BasicUtil.isNotEmpty(host)) {
                String[] hosts = host.split(",");
                for(String h : hosts) {
                    h = h.trim();
                    if(h.isEmpty()) continue;
                    if(h.contains(":")) {
                        String[] parts = h.split(":");
                        builder.host(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    } else {
                        builder.host(h, port);
                    }
                }
            }

            // 5d. 协议
            if(BasicUtil.isNotEmpty(protocol)) {
                try {
                    builder.protocol(Protocol.valueOf(protocol));
                } catch (IllegalArgumentException e) {
                    log.warn("[arango 不支持的协议][protocol:{}][支持:VST,HTTP_JSON,HTTP_VPACK,HTTP2_JSON,HTTP2_VPACK]", protocol);
                }
            }

            // 5e. 超时
            if(null != timeout) {
                builder.timeout(timeout);
            }

            // 5f. SSL
            if(null != useSsl) {
                builder.useSsl(useSsl);
            }
            if(null != verifyHost) {
                builder.verifyHost(verifyHost);
            }

            // 5g. 连接池
            if(null != maxConnections) {
                builder.maxConnections(maxConnections);
            }
            if(null != connectionTtl) {
                builder.connectionTtl(connectionTtl);
            }
            if(null != keepAliveInterval) {
                builder.keepAliveInterval(keepAliveInterval);
            }

            // 5h. 集群/故障转移
            if(null != acquireHostList) {
                builder.acquireHostList(acquireHostList);
            }
            if(null != acquireHostListInterval) {
                builder.acquireHostListInterval(acquireHostListInterval);
            }
            if(BasicUtil.isNotEmpty(loadBalancingStrategy)) {
                try {
                    builder.loadBalancingStrategy(LoadBalancingStrategy.valueOf(loadBalancingStrategy));
                } catch (IllegalArgumentException e) {
                    log.warn("[arango 不支持的负载均衡策略][strategy:{}][支持:NONE,ROUND_ROBIN,ONE_RANDOM]", loadBalancingStrategy);
                }
            }

            // 5i. 构建
            ArangoDB client = builder.build();
            DataSourceHolder.params.put(key, params);
            ArangoDatabase db = client.db(database);
            ArangoRuntimeHolder.instance().reg(key, client, db);

        } catch (Exception e) {
            log.error("[注入数据源失败][type:arango][key:{}][msg:{}]", key, e.toString());
            log.error("注入数据源 异常:", e);
            return null;
        }
        return datasource_id;
    }

}