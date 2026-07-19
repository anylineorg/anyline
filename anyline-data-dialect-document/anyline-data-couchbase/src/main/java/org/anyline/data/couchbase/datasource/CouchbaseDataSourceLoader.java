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
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.datasource.init.AbstractDataSourceLoader;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.util.ArrayList;
import java.util.List;

@AnylineComponent("anyline.environment.data.datasource.loader.couchbase")
public class CouchbaseDataSourceLoader extends AbstractDataSourceLoader implements DataSourceLoader {
    public static final Log log = LogProxy.get(CouchbaseDataSourceLoader.class);

    private final CouchbaseDataSourceHolder holder = CouchbaseDataSourceHolder.instance();

    @Override
    public DataSourceHolder holder() {
        return holder;
    }

    @Override
    public List<String> load() {
        List<String> list = new ArrayList<>();
        list.addAll(load("spring.datasource", false));
        list.addAll(load("anyline.datasource", false));
        return list;
    }

}