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


package org.anyline.data.couchdb.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.datasource.init.AbstractDataSourceLoader;
import org.anyline.data.couchdb.client.CouchDBClient;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.util.ConfigTable;

import java.util.ArrayList;
import java.util.List;

@AnylineComponent("anyline.environment.data.datasource.loader.couchdb")
public class CouchDBDataSourceLoader extends AbstractDataSourceLoader implements DataSourceLoader {

    private final CouchDBDataSourceHolder holder = CouchDBDataSourceHolder.instance();

    @Override
    public DataSourceHolder holder() {
        return holder;
    }

    @Override
    public List<String> load() {
        List<String> list = new ArrayList<>();
        boolean loadDefault = true;
        if(!ConfigTable.environment().containsBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + ".default")) {
            //如果还没有注册默认数据源, 尝试从Spring上下文中获取已创建的客户端
            Object client = null;
            try {
                client = ConfigTable.environment().getBean(CouchDBClient.class);
            } catch (Exception e) {
                client = null;
            }
            if(null != client) {
                try {
                    holder().create("couchdb.default", client, false);
                    loadDefault = false;
                } catch (Exception e) {
                    log.error("加载couchdb数据源 异常:", e);
                }
            }
        } else {
            loadDefault = false;
        }
        list.addAll(load("spring.datasource", loadDefault));
        list.addAll(load("anyline.datasource", loadDefault));
        return list;
    }
}