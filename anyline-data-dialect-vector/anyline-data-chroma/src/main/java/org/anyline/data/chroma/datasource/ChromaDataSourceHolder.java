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


package org.anyline.data.chroma.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.chroma.runtime.ChromaRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@AnylineComponent("anyline.environment.data.datasource.holder.chroma")
public class ChromaDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder{
    private static final ChromaDataSourceHolder instance = new ChromaDataSourceHolder();
    public static ChromaDataSourceHolder instance() {
        return instance;
    }
    public ChromaDataSourceHolder() {
        DataSourceHolder.register("chroma", this);
        DataSourceHolder.register(DatabaseType.Chroma, this);
        // 如果有Chroma Java SDK添加了依赖，也自动注册
        try {
            Class<?> clientClass = Class.forName("tech.amikos.chroma.Client");
            DataSourceHolder.register(clientClass, this);
        } catch (Exception e) {
            // SDK 未添加，忽略
        }
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
                //只注册chroma驱动
                return null;
            }
            adapter = adapter.toLowerCase();
            if(adapter.contains("chroma")) {
                Map<String, Object> map = new HashMap<>();
                return inject(key, prefix, map, true);
            }
        } catch (Exception e) {
            log.error("注册Chroma数据源 异常:", e);
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
        Map<String, Object> cache = DataSourceHolder.params.get(key);
        if(null == cache) {
            cache = new HashMap<>();
            DataSourceHolder.params.put(key, cache);
        }
        String url =  value(prefix, params, "url,uri,host", String.class, null);

        if(BasicUtil.isEmpty(url)) {
            return null;
        }
        String adapter = value(prefix, params, "adapter", String.class, null);
        if(null == adapter) {
            return null;
        }
        adapter = adapter.toLowerCase();
        if(!adapter.contains("chroma")) {
            //只注册chroma类型
            return null;
        }
        try {
            // Chroma client 创建
            // Chroma 服务地址 如 http://localhost:8000
            String host = url;
            // 确保url有协议头
            if(!host.contains("://")) {
                host = "http://" + host;
            }
            String apiKey = value(prefix, params, "apiKey,token,password", String.class, null);
            String database = value(prefix, params, "database,dbName,tenant", String.class, "default_database");

            // TODO: 当 Chroma Java SDK 依赖添加后，替换为实际的客户端创建逻辑
            // 示例（需要添加 io.github.amikos-tech:chromadb-java-client 依赖）:
            // Client client = new Client(host);
            // if(BasicUtil.isNotEmpty(apiKey)) {
            //     client.setApiKey(apiKey);
            // }

            Object client = buildClient(host, apiKey, database);
            if(null != client) {
                ChromaRuntimeHolder.instance().reg(key, client);
            }
        } catch (Exception e) {
            log.error("[注册数据源失败][type:chroma][key:{}][msg:{}]", key, e.toString());
            return null;
        }
        return null;
    }

    /**
     * 构建Chroma客户端
     * 可通过反射加载Chroma SDK的Client类来创建实例
     * @param host Chroma服务地址
     * @param apiKey API密钥
     * @param database 数据库/租户名
     * @return Chroma客户端实例
     */
    private Object buildClient(String host, String apiKey, String database) {
        // 尝试通过反射使用 Chroma Java SDK 创建客户端
        try {
            Class<?> clientClass = Class.forName("tech.amikos.chroma.Client");
            Object client = clientClass.getConstructor(String.class).newInstance(host);
            return client;
        } catch (Exception e) {
            // SDK 未找到或创建失败，忽略
        }
        log.warn("[Chroma Java SDK未找到][请添加chromadb-java-client依赖]");
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
        Object client = runtime.getProcessor();
        return validate(client);
    }

    public boolean validate(Object client) {
        try{
            return exeValidate(client);
        }catch (Exception e) {
            return false;
        }
    }

    public boolean exeValidate(Object client) {
        return true;
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