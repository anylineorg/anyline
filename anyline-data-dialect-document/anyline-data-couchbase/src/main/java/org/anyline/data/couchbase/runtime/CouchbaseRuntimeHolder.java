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


package org.anyline.data.couchbase.runtime;

import org.anyline.bean.BeanDefine;
import org.anyline.bean.init.DefaultBeanDefine;
import org.anyline.dao.init.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.couchbase.adapter.CouchbaseAdapter;
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.runtime.init.AbstractRuntimeHolder;
import org.anyline.service.init.DefaultService;
import org.anyline.util.ConfigTable;

import java.util.HashMap;
import java.util.Map;

public class CouchbaseRuntimeHolder extends AbstractRuntimeHolder {

    private static Map<String, Object> temporary = new HashMap<>();

    private static final CouchbaseRuntimeHolder instance = new CouchbaseRuntimeHolder();

    public CouchbaseRuntimeHolder() {
    }

    public static CouchbaseRuntimeHolder instance() {
        return instance;
    }

    public DataRuntime temporary(Object client, String database, DriverAdapter adapter) throws Exception {
        CouchbaseRuntime runtime = new CouchbaseRuntime();
        if (null == adapter) {
            adapter = ConfigTable.environment().getBean(CouchbaseAdapter.class);
        }
        String key = "temporary_couchbase";
        temporary.remove(key);
        runtime.setKey(key);
        runtime.setAdapter(adapter);
        runtime.setProcessor(client);
        temporary.put(key, client);
        log.warn("[创建临时数据源][key:{}][type:{}]", key, client.getClass().getSimpleName());
        return runtime;
    }

    public DataRuntime reg(String key, Object client) {
        String datasource_key = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        log.info("[注入数据源][type:couchbase][key:{}][bean:{}]", key, datasource_key);
        ConfigTable.environment().regBean(datasource_key, client);
        return reg(key, client, null);
    }

    public CouchbaseRuntime reg(String datasource, Object client, DriverAdapter adapter) {
        log.debug("[create couchbase runtime][key:{}]", datasource);
        if (null == adapter) {
            adapter = ConfigTable.environment().getBean(CouchbaseAdapter.class);
        }
        CouchbaseRuntime runtime = new CouchbaseRuntime(datasource, client, adapter);
        if (runtimes.containsKey(datasource)) {
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);

        String dao_key = DataRuntime.ANYLINE_DAO_BEAN_PREFIX + datasource;
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + datasource;
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
        if (null != monitor) {
            CouchbaseRuntime runtime = (CouchbaseRuntime) runtimes.get(key);
            if (null != runtime) {
                close = monitor.destroy(runtime, key, runtime.getProcessor());
            }
        }
        try {
            runtimes.remove(key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_DAO_BEAN_PREFIX + key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX + key);
            ConfigTable.environment().destroyBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
            log.warn("[注销数据源及相关资源][key:{}]", key);
            Map<String, DataRuntime> runtimes = RuntimeHolder.runtimes(key);
            for (String item : runtimes.keySet()) {
                destroy(item);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}