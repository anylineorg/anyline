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


package org.anyline.data.couchbase.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@AnylineComponent("anyline.environment.data.datasource.holder.couchbase")
public class CouchbaseDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {
    private static final CouchbaseDataSourceHolder instance = new CouchbaseDataSourceHolder();

    public static CouchbaseDataSourceHolder instance() {
        return instance;
    }

    public CouchbaseDataSourceHolder() {
    }

    public String reg(String key, String prefix) {
        try {
            if (BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
                prefix += ".";
            }
            String adapter = value(prefix, "adapter", String.class, null);
            if (null == adapter || !adapter.toLowerCase().contains("couchbase")) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            return inject(key, prefix, map, true);
        } catch (Exception e) {
            log.error("注册Couchbase数据源 异常:", e);
        }
        return null;
    }

    public String inject(String key, Map params, boolean over) throws Exception {
        return inject(key, null, params, over);
    }

    public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
        DataSourceHolder.check(key, override);
        // TODO: Couchbase 具体实现待补充
        log.warn("[Couchbase数据源占位][key:{}] 暂未实现，请补充 Couchbase SDK 连接逻辑", key);
        return null;
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
        Object client = runtime.getProcessor();
        return validate(client);
    }

    public boolean validate(Object client) {
        try {
            return exeValidate(client);
        } catch (Exception e) {
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