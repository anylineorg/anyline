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


package org.anyline.data.pinecone.datasource;

import io.pinecone.clients.Pinecone;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.init.AbstractDataSourceHolder;
import org.anyline.data.pinecone.runtime.PineconeRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@AnylineComponent("anyline.environment.data.datasource.holder.pinecone")
public class PineconeDataSourceHolder extends AbstractDataSourceHolder implements DataSourceHolder {

    private static final PineconeDataSourceHolder instance = new PineconeDataSourceHolder();

    public static PineconeDataSourceHolder instance() {
        return instance;
    }

    public PineconeDataSourceHolder() {
        DataSourceHolder.register(Pinecone.class, this);
        DataSourceHolder.register("pinecone", this);
    }

    public String reg(String key, String prefix) {
        try {
            if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")) {
                prefix += ".";
            }
            String adapter = value(prefix, "adapter", String.class, null);
            if(null == adapter) {
                return null;
            }
            adapter = adapter.toLowerCase();
            if(adapter.contains("pinecone")) {
                Map<String, Object> map = new HashMap<>();
                return inject(key, prefix, map, true);
            }
        } catch (Exception e) {
            log.error("注册Pinecone数据源 异常:", e);
        }
        return null;
    }

    @Override
    public String inject(String key, Map params, boolean over) throws Exception {
        return inject(key, null, params, over);
    }

    @Override
    public String inject(String key, String prefix, Map<String, Object> params, boolean override) throws Exception {
        DataSourceHolder.check(key, override);
        String datasource_id = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        Map<String, Object> cache = DataSourceHolder.params.get(key);
        if(null == cache) {
            cache = new HashMap<>();
            DataSourceHolder.params.put(key, cache);
        }

        String adapter = value(prefix, params, "adapter", String.class, null);
        if(null == adapter) {
            return null;
        }
        adapter = adapter.toLowerCase();
        if(!adapter.contains("pinecone")) {
            return null;
        }

        String apiKey = value(prefix, params, "api,key,apiKey,api_key", String.class, null);
        if(BasicUtil.isEmpty(apiKey)) {
            log.error("[注册Pinecone数据源失败][key:{}][缺少apiKey]", key);
            return null;
        }

        try {
            Pinecone client = new Pinecone.Builder(apiKey).build();
            PineconeRuntimeHolder.instance().reg(key, client);
        } catch (Exception e) {
            log.error("[注册数据源失败][type:pinecone][key:{}][msg:{}]", key, e.toString());
            return null;
        }
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
        Pinecone client = (Pinecone) runtime.getProcessor();
        return validate(client);
    }

    public boolean validate(Pinecone client) {
        try {
            return exeValidate(client);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exeValidate(Pinecone client) {
        try {
            client.listIndexes();
            return true;
        } catch (Exception e) {
            return false;
        }
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