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

/*


package org.anyline.data.nebula.runtime;

import com.vesoft.nebula.client.graph.SessionPool;
import org.anyline.bean.BeanDefine;
import org.anyline.bean.init.DefaultBeanDefine;
import org.anyline.bean.init.DefaultValueReference;
import org.anyline.dao.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.nebula.adapter.NebulaAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.proxy.RuntimeHolderProxy;
import org.anyline.service.init.DefaultService;

import java.util.HashMap;
import java.util.Map;

public class NebulaRuntimeHolder extends RuntimeHolder {
    */
/**
     * 临时数据源
     *//*

    private static Map<String, SessionPool> temporary = new HashMap<>();

    public NebulaRuntimeHolder() {
        RuntimeHolderProxy.reg(SessionPool.class, this);
    }

    */
/**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到spring
     * @param client 数据源, 如DruidDataSource, MongoClient, es.RestClient
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     *//*


    public static DataRuntime temporary(Object client, String database, DriverAdapter adapter) throws Exception {
        return exeTemporary(client, database, adapter);

    }

    @Override
    public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        return exeTemporary( datasource, database, adapter);
    }

    private static DataRuntime exeTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        NebulaRuntime runtime = new NebulaRuntime();
        if(null == adapter) {
            adapter = worker.getBean(NebulaAdapter.class);
        }
        if(datasource instanceof SessionPool) {
            String key = "temporary_es";
            temporary.remove(key);
            //DriverAdapterHolder.remove(key);
            //创建新数据源
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            SessionPool client = (SessionPool) datasource;
            runtime.setProcessor(client);
            temporary.put(key, client);
            log.warn("[创建临时数据源][key:{}][type:{}]", key, datasource.getClass().getSimpleName());
        }else{
            throw new Exception("请提供org.Nebula.client.RestClient兼容类型");
        }
        //runtime.setHolder(this);
        return runtime;
    }
    */
/**
     * 注册运行环境
     * @param key 数据源前缀
     *//*

    public static void reg(String key) {
        String datasource_key = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        SessionPool client = worker.getBean(datasource_key, SessionPool.class);
        reg(key, client, null);
    }

    */
/**
     * 注册运行环境
     * @param datasource 数据源前缀
     * @param client RestClient
     * @param adapter adapter 可以为空 第一次执行时补齐
     *//*

    public static NebulaRuntime reg(String datasource, SessionPool client, DriverAdapter adapter) {
        log.info("[create nebula runtime][key:{}]", datasource);
        if(null == adapter) {
            adapter = worker.getBean(NebulaAdapter.class);
        }
        NebulaRuntime runtime = new NebulaRuntime(datasource, client, adapter);
        if(runtimes.containsKey(datasource)) {
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);

        String dao_key = DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  datasource;
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  datasource;
        log.info("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        BeanDefine daoDefine = new DefaultBeanDefine(DefaultDao.class);
        daoDefine.addValue("runtime", runtime);
        daoDefine.setLazy(true);
        worker.regBean(dao_key, daoDefine);


        BeanDefine serviceDefine = new DefaultBeanDefine(DefaultService.class);
        serviceDefine.addValue("dao", new DefaultValueReference(dao_key));
        serviceDefine.setLazy(true);
        worker.regBean(service_key, serviceDefine);
        return runtime;

    }

    public static void destroy(String key) {
        exedestroy(key);
    }
    private static void exedestroy(String key) {
        try {
            runtimes.remove(key);
            destroyBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key);
            destroyBean(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key);
            destroyBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key);
            destroyBean(DataRuntime.ANYLINE_DATABASE_BEAN_PREFIX +  key);
            log.warn("[注销数据源及相关资源][key:{}]", key);
            //从当前数据源复制的 子源一块注销
            Map<String, DataRuntime> runtimes = runtimes(key);
            for(String item:runtimes.keySet()) {
                destroy(item);
            }
        }catch (Exception e) {
                log.error("注销数据源 异常:", e);
        }
    }

    @Override
    public void calldestroy(String key) {
        exedestroy(key);
    }

}*/
