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




package org.anyline.data.milvus.datasource;

import io.milvus.v2.client.MilvusClientV2;
import org.anyline.annotation.Component;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.datasource.init.AbstractDataSourceLoader;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component("anyline.environment.data.datasource.loader.milvus")
public class MilvusDataSourceLoader extends AbstractDataSourceLoader implements DataSourceLoader {
    public static Logger log = LoggerFactory.getLogger(MilvusDataSourceLoader.class);

    private final MilvusDataSourceHolder holder = MilvusDataSourceHolder.instance();

    @Override
    public DataSourceHolder holder() {
        return holder;
    }
    @Override
    public List<String> load() {
        List<String> list = new ArrayList<>();
        boolean loadDefault = true; //是否需要加载default
        if(!ConfigTable.environment().containsBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + ".default")) {
            //如果还没有注册默认数据源

            DataRuntime runtime = null;
            MilvusClientV2 client = null;
            try{
                client = ConfigTable.environment().getBean(MilvusClientV2.class);
            }catch (Exception e) {
                runtime = null;
            }
            if(null != client) {
                try {
                    runtime =  holder().create("default", client, false);
                    loadDefault = false;
                }catch (Exception e) {
                    runtime = null;
                    log.error("加载Milvus数据源 异常:", e);
                }
            }

            //有不支持通过connection返回获取连接信息的驱动，所以从配置文件中获取
            if(null != runtime) {
                String url = ConfigTable.environment().string( "spring.datasource.,anyline.datasource.", "url,uri");
                runtime.setUrl(url);
                if (BasicUtil.isNotEmpty(url)) {
                    runtime.setAdapterKey(DataSourceUtil.parseAdapterKey(url));
                }else{
                    String adapterKey = ConfigTable.environment().string("spring.datasource.,anyline.datasource.", "adapter");
                    if(BasicUtil.isNotEmpty(adapterKey)) {
                        runtime.setAdapterKey(adapterKey);
                    }
                }
            }
        }else{
            loadDefault = false;
        }
        list.addAll(load("spring.datasource", loadDefault));
        list.addAll(load("anyline.datasource", loadDefault));
        return list;
    }

}
