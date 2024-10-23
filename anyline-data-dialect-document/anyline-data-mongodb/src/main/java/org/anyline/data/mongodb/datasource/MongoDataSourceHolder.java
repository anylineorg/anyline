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

package org.anyline.data.mongodb.datasource;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.anyline.annotation.Component;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.mongodb.runtime.MongoRuntime;
import org.anyline.data.mongodb.runtime.MongoRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component("anyline.environment.data.datasource.holder.mongo")
public class MongoDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {

    private static final MongoDataSourceHolder instance = new MongoDataSourceHolder();
    public static MongoDataSourceHolder instance() {
        return instance;
    }
    public MongoDataSourceHolder() {
        DataSourceHolder.register("mongodb", this);
        DataSourceHolder.register(MongoClient.class, this);
        DataSourceHolder.register(MongoDatabase.class, this);
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
            log.error("注册Mongo数据源 异常:", e);
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
        MongoClient client = ((MongoRuntime)runtime).client();
        client.listDatabaseNames();
        return true;
    }

    @Override
    public String regTransactionManager(String key, DataSource datasource, boolean primary) {
        return "";
    }

    public String regTransactionManager(String key, MongoClient client, MongoDatabase database, boolean primary) {
        return "";
    }

    @Override
    public String runtime(String key, String datasource, boolean override) throws Exception {
        return datasource;
    }

    @Override
    public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
        DataRuntime runtime = null;
        MongoClient client = null;
        if(datasource instanceof MongoClient) {
            client = (MongoClient)datasource;
            datasource = client.getDatabase(database);
        }
        if(datasource instanceof MongoDatabase) {
            if(null != ConfigTable.environment) {
                DataSourceHolder.check(key, override);
                //创建事务管理器
                regTransactionManager(key, client, (MongoDatabase) datasource, true);
                runtime = MongoRuntimeHolder.instance().reg(key, client, (MongoDatabase)datasource);
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
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param key 调用或注销数据源时需要用到  如ServiceProxy.service(“sso”)
     * @param prefix 配置文件前缀 如 anyline.datasource.sso
     * @param params map格式参数
     * @param override 是否覆盖同名数据源
     * @return bean.di
     * @throws Exception Exception
     */
    public String inject(String key, String prefix, Map<String, Object> params,  boolean override) throws Exception {
        DataSourceHolder.check(key, override);
        String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        try {
            String url =  value(prefix, params, "url", String.class, null);
            if(BasicUtil.isEmpty(url)) {
                return null;
            }
            String database = value(prefix, params, "database", String.class, null);
            //只解析Mongo系列
            if(!url.toLowerCase().startsWith("mongodb:")) {
                return null;
            }
            if(BasicUtil.isEmpty(database)) {
                log.error("[注入数据源失败][type:mongo][key:{}][msg:未设置database]", key);
                return null;
            }

            ConnectionString connection = new ConnectionString(url);
            MongoClient client = MongoClients.create(connection);
            DataSourceHolder.params.put(key, params);
            MongoDatabase db = client.getDatabase(database);
            MongoRuntimeHolder.instance().reg(key, client, db);
        } catch (Exception e) {
            log.error("[注入数据源失败][type:mongo][key:{}][msg:{}]", key, e.toString());
            log.error("注入数据源 异常:", e);
            return null;
        }
        return datasource_id;
    }

}
