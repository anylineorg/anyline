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



package org.anyline.data.jdbc.datasource;

import org.anyline.annotation.Component;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceKeyMap;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.jdbc.runtime.JDBCRuntimeHolder;
import org.anyline.data.jdbc.util.DataSourceUtil;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.init.DefaultTransactionManage;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.ConvertException;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
@Component("anyline.environment.data.datasource.holder.jdbc")
public class JDBCDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {

    private static final JDBCDataSourceHolder instance = new JDBCDataSourceHolder();
    public static JDBCDataSourceHolder instance() {
        return instance;
    }
    static {
        ConvertProxy.reg(new Convert() {
            @Override
            public Class getOrigin() {
                return String.class;
            }

            @Override
            public Class getTarget() {
                return java.sql.Driver.class;
            }

            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                if(null != value) {
                    try {
                        return Class.forName(value.toString()).newInstance();
                    }catch (Exception e) {
                        log.error("类型转换 异常:", e);
                    }
                }
                return null;
            }
        });
    }
    public JDBCDataSourceHolder() {
        for(DatabaseType type:DatabaseType.values()) {
            String url = type.url();
            if(url.contains("jdbc:") && url.contains("://")) { // jdbc:postgresql://localhost:35432/simple
                DataSourceHolder.register(type, this);
                DataSourceHolder.register(type.driver(), this);
                DataSourceHolder.register(type.name().toUpperCase(), this);

                String[] chks = url.split("://");
                DataSourceHolder.register(chks[0], this); //jdbc:postgresql
            }
        }
        DataSourceHolder.register("com.alibaba.druid.pool.DruidDataSource", this);
        DataSourceHolder.register(DataSourceUtil.POOL_TYPE_DEFAULT, this);
        DataSourceHolder.register(Connection.class, this);
        DataSourceHolder.register(DataSource.class, this);
    }
    public String reg(String key, String prefix) {
        try {
            if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
                prefix += ".";
            }
            String type = value(prefix, "type", String.class, null);
            if(null == type) {//未设置类型 先取默认数据源类型
                type = value(prefix.substring(0, prefix.length()- key.length()-1), "type", String.class, null);
            }
            if (type == null) {
                type = DataSourceUtil.POOL_TYPE_DEFAULT;
            }
            String url = value(prefix, "url", String.class, null);
            if(BasicUtil.isEmpty(url)) {
                return null;
            }
            if(!url.startsWith("jdbc:")) {
                //只注册jdbc驱动
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            String datasource = inject(key, prefix, map, true);
            if(null == datasource) {//创建数据源失败
                return null;
            }
            runtime(key, datasource, true);
            return datasource;
        } catch (Exception e) {
            log.error("注册JDBC数据源 异常:", e);
        }
        return null;
    }


    @Override
    public String create(String key, DatabaseType database, String url, String user, String password) throws Exception {
        Map params = new HashMap();
        params.put("driverClass", database.driver());
        params.put("url", url);
        params.put("user", user);
        params.put("password", password);
        String ds = inject(key, params, true);
        return runtime(key, ds, false);
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

    public String regTransactionManager(String key, DataSource datasource, boolean primary) {
        if(ConfigTable.IS_OPEN_TRANSACTION_MANAGER) {
            TransactionManage.reg(key, new DefaultTransactionManage(datasource));
        }
        return key;
    }


    @Override
    public String runtime(String key, String datasource, boolean override) throws Exception {
        if(null != datasource) {
            DataSourceHolder.check(key, override);
            regTransactionManager(key, datasource);
            DataRuntime runtime = JDBCRuntimeHolder.instance().reg(key, datasource);
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
                    String catalog = param.get("catalog")+"";
                    if(BasicUtil.isEmpty(catalog)) {
                        catalog = org.anyline.data.util.DataSourceUtil.parseCatalog(url);
                    }
                    runtime.setCatalog(catalog);

                    String schema = param.get("schema")+"";
                    if(BasicUtil.isEmpty(schema)) {
                        schema = org.anyline.data.util.DataSourceUtil.parseSchema(url);
                    }
                    runtime.setSchema(schema);
                }
            }
        }
        return datasource;
    }

    @Override
    public DataRuntime runtime(String key, Object datasource, String database, DatabaseType type, DriverAdapter adapter, boolean override) throws Exception {
        DataRuntime runtime = null;
        if(datasource instanceof DataSource) {
            if(null != ConfigTable.worker) {
                DataSourceHolder.check(key, override);
                //创建事务管理器
                regTransactionManager(key, (DataSource)datasource);
                runtime = JDBCRuntimeHolder.instance().reg(key, (DataSource)datasource);
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
            String url =  value(params, "url", String.class, null);
            if(BasicUtil.isEmpty(url)) {
                url = value(prefix, "url", String.class, null);
            }
            if(BasicUtil.isEmpty(url)) {
                return null;
            }
            //只解析jdbc系列
            if(!url.toLowerCase().startsWith("jdbc:")) {
                return null;
            }
            params.put("url", url);

            String type = value(params, "type", String.class, null);
            if(BasicUtil.isEmpty(type)) {
                type = value(prefix, "type", String.class, null);
            }
            if (type == null) {
                type = DataSourceUtil.POOL_TYPE_DEFAULT;
            }
            Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName(type);
            Object driver =  value(params, "driverClass");
            if(null == driver) {
                driver = value(prefix, "driverClass");
            }
            if(driver instanceof String) {
                Class<?> calzz = Class.forName((String)driver);
                driver = calzz.newInstance();
            }
            if(null != driver) {
                params.put("driver", driver);
            }
            DataSourceHolder.params.put(key, params);
            Map<String, Object> sets = ConfigTable.environment().inject(datasource_id, prefix, params, DataSourceKeyMap.maps, poolClass);
            if(!params.containsKey(key)) {
                params.put(key, sets);
            }
            log.info("[注入数据源][type:JDBC][key:{}][bean:{}]", key, datasource_id);
        } catch (Exception e) {
            log.error("[注入数据源失败][type:JDBC][key:{}][msg:{}]", key, e.toString());
            log.error("注入数据源 异常:", e);
            return null;
        }
        return datasource_id;
    }

}
