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

package org.anyline.data.hbase.runtime;

import org.anyline.bean.BeanDefine;
import org.anyline.bean.init.DefaultBeanDefine;
import org.anyline.dao.init.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.hbase.adapter.HBaseAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.runtime.init.AbstractRuntimeHolder;
import org.anyline.service.init.DefaultService;
import org.anyline.util.ConfigTable;
import org.apache.hadoop.hbase.client.Connection;

import java.util.HashMap;
import java.util.Map;

public class HBaseRuntimeHolder extends AbstractRuntimeHolder {
    /**
     * 临时数据源
     */
    private static Map<String, Connection> temporary = new HashMap<>();

    private static final HBaseRuntimeHolder instance = new HBaseRuntimeHolder();
    public HBaseRuntimeHolder() {
    }
    public static HBaseRuntimeHolder instance() {
        return instance;
    }

	/**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到spring
     * @param client 数据源, 如DruidDataSource, MongoClient, es.Connection
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public DataRuntime temporary(Object client, String database, DriverAdapter adapter) throws Exception {
        HBaseRuntime runtime = new HBaseRuntime();
        if(null == adapter) {
            adapter = ConfigTable.environment().getBean(HBaseAdapter.class);
        }
        if(client instanceof Connection) {
            String key = "temporary_es";
            temporary.remove(key);
            //DriverAdapterHolder.remove(key);
            //创建新数据源
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            runtime.setProcessor(client);
            temporary.put(key, (Connection)client);
            log.warn("[创建临时数据源][key:{}][type:{}]", key, client.getClass().getSimpleName());
        }else{
            throw new Exception("请提供Connection兼容类型");
        }
        //runtime.setHolder(this);
        return runtime;
    }
    public DataRuntime reg(String key, Connection client) {
        String datasource_key = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        log.info("[注入数据源][type:HBase][key:{}][bean:{}]", key, datasource_key);
        ConfigTable.environment().regBean(datasource_key, client);
        return reg(key, client, null);
    }

	/**
     * 注册运行环境
     * @param datasource 数据源前缀
     * @param client Connection
     * @param adapter adapter 可以为空 第一次执行时补齐
     */
    public HBaseRuntime reg(String datasource, Connection client, DriverAdapter adapter) {
        log.debug("[create HBase runtime][key:{}]", datasource);
        if(null == adapter) {
            adapter = ConfigTable.environment().getBean(HBaseAdapter.class);
        }
        HBaseRuntime runtime = new HBaseRuntime(datasource, client, adapter);
        if(runtimes.containsKey(datasource)) {
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);

        String dao_key = DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  datasource;
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  datasource;
        log.info("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        BeanDefine daoDefine = new DefaultBeanDefine(DefaultDao.class);
        daoDefine.addValue("runtime", runtime);
        ConfigTable.environment().regBean(dao_key, daoDefine);

        BeanDefine serviceDefine = new DefaultBeanDefine(DefaultService.class);
        serviceDefine.addReferenceValue("dao", dao_key);
        ConfigTable.environment().regBean(service_key, serviceDefine);
        return runtime;

    }

    public boolean destroy(String key) {

        int close = 0;
        DataSourceMonitor monitor = DriverAdapterHolder.getMonitor();
        if(null != monitor) {
            HBaseRuntime runtime = (HBaseRuntime) runtimes.get(key);
            if(null != runtime) {
                //这一步有可能抛出 异常
                close = monitor.destroy(runtime, key, runtime.getProcessor());
            }
        }
        try {
            runtimes.remove(key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key);

            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
            log.warn("[注销数据源及相关资源][key:{}]", key);
            //从当前数据源复制的 子源一块注销
            Map<String, DataRuntime> runtimes = RuntimeHolder.runtimes(key);
            for(String item:runtimes.keySet()) {
                destroy(item);
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

}
