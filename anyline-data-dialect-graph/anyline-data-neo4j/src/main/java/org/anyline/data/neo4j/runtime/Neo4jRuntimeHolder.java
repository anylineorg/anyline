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

package org.anyline.data.neo4j.runtime;

import org.anyline.bean.BeanDefine;
import org.anyline.bean.init.DefaultBeanDefine;
import org.anyline.bean.init.DefaultValueReference;
import org.anyline.dao.init.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.neo4j.adapter.Neo4jAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.runtime.init.AbstractRuntimeHolder;
import org.anyline.service.init.DefaultService;
import org.anyline.util.ConfigTable;
import org.neo4j.driver.Driver;

import java.util.Map;

public class Neo4jRuntimeHolder extends AbstractRuntimeHolder {
    private static final Neo4jRuntimeHolder instance = new Neo4jRuntimeHolder();
    public static Neo4jRuntimeHolder instance() {
        return instance;
    }
    public Neo4jRuntimeHolder() {
    }

    /**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到
     * @param datasource 数据源, neo4jDatabase, neo4jClient
     * @param database 数据库, neo4j类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        Neo4jRuntime runtime = new Neo4jRuntime();
        if(datasource instanceof Driver) {
            String key = "temporary_neo4j";
            //关闭上一个
            close(key);
            temporary.remove(key);
            runtimes.remove(key);
            //DriverAdapterHolder.remove(key);
            //创建新数据源
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            runtime.setProcessor(datasource);
            temporary.put(key, datasource);
            log.warn("[创建临时数据源][key:{}][type:{}]", key, datasource.getClass().getSimpleName());
            runtimes.put(key, runtime);
        }else{
            throw new Exception("请提供 org.neo4j.driver.Driver 兼容类型");
        }
        //runtime.setHolder(this);
        return runtime;
    }

    /**
     * 注册运行环境
     * @param key 数据源前缀
     * @param datasource 数据源bean id
     */
    public DataRuntime reg(String key, String datasource) {
        return null;
    }

    public Neo4jRuntime reg(String key, Driver driver) {
        String datasource_key = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        log.info("[注入数据源][type:neo4j][key:{}][bean:{}]", key, datasource_key);
        ConfigTable.environment().regBean(datasource_key, driver);
        return reg(key, driver, null);
    }

    /**
     * 注册运行环境
     * @param datasource 数据源前缀
     * @param driver driver
     * @param adapter adapter 可以为空 第一次执行时补齐
     */
    public Neo4jRuntime reg(String datasource, Driver driver, Neo4jAdapter adapter) {
        log.debug("[create neo4j runtime][key:{}]", datasource);
        Neo4jRuntime runtime = new Neo4jRuntime(datasource, driver, adapter);
        if(runtimes.containsKey(datasource)) {
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);

        String dao_key = DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  datasource;
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  datasource;
        log.debug("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        BeanDefine daoDefine = new DefaultBeanDefine(DefaultDao.class);
        daoDefine.addValue("runtime", runtime);
        daoDefine.setLazy(true);
        ConfigTable.environment().regBean(dao_key, daoDefine);

        BeanDefine serviceDefine = new DefaultBeanDefine(DefaultService.class);
        serviceDefine.addValue("dao", new DefaultValueReference(dao_key));
        serviceDefine.setLazy(true);
        ConfigTable.environment().regBean(service_key, serviceDefine);
        return runtime;
    }
    public boolean destroy(String key) {
        int close = 0;
        Neo4jRuntime runtime = (Neo4jRuntime) runtimes.get(key);
        DataSourceMonitor monitor = DriverAdapterHolder.getMonitor();
        if(null != monitor) {
            if(null != runtime) {
                //这一步有可能抛出 异常
                close = monitor.destroy(runtime, key, runtime.driver());
            }
        }
        try {
            runtimes.remove(key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key);
            if(close == 0) {
                close(runtime.driver());
            }
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
            log.warn("[注销数据源及相关资源][key:{}]", key);
            //从当前数据源复制的 子源一块注销
            Map<String, DataRuntime> runtimes = RuntimeHolder.runtimes(key);
            for(String item:runtimes.keySet()) {
                destroy(item);
            }
        }catch (Exception e) {
            log.error("注销数据源 异常:", e);
            return false;
        }
        return true;
    }

    public static void close(Driver driver) {
        try{
            driver.close();
        }catch (Exception e){
            log.error("关闭neo4j连接 异常:", e);
        }
    }
    public static void close(String key) {
        Neo4jRuntime runtime = (Neo4jRuntime) runtimes.get(key);
        close(runtime.driver());
    }
    public static void closeConnection(Object datasource) throws Exception {
    }
}