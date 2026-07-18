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


package org.anyline.data.chroma.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.datasource.init.AbstractDataSourceLoader;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.ArrayList;
import java.util.List;

@AnylineComponent("anyline.environment.data.datasource.loader.chroma")
public class ChromaDataSourceLoader extends AbstractDataSourceLoader implements DataSourceLoader {
    public static final Log log = LogProxy.get(ChromaDataSourceLoader.class);

    private final ChromaDataSourceHolder holder = ChromaDataSourceHolder.instance();

    @Override
    public DataSourceHolder holder() {
        return holder;
    }

    @Override
    public List<String> load() {
        List<String> list = new ArrayList<>();
        boolean loadDefault = true;
        if(!ConfigTable.environment().containsBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + ".default")) {
            //如果还没有注册默认数据源

            DataRuntime runtime = null;
            Object client = null;
            try{
                // 尝试从Spring上下文中获取Chroma客户端bean
                client = findChromaClient();
            }catch (Exception e) {
                runtime = null;
            }
            if(null != client) {
                try {
                    runtime =  holder().create("chroma.default", client, false);
                    loadDefault = false;
                }catch (Exception e) {
                    runtime = null;
                    log.error("加载Chroma数据源 异常:", e);
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

    /**
     * 从Spring上下文中查找Chroma客户端bean
     * 尝试多种可能存在的Chroma Java SDK类名
     * @return Chroma客户端实例，未找到返回null
     */
    private Object findChromaClient() {
        // 尝试通过已知的Chroma Java SDK类名查找
        String[] clientClassNames = {
            "tech.amikos.chroma.Client",
        };
        for(String className : clientClassNames) {
            try {
                Class<?> clientClass = Class.forName(className);
                return ConfigTable.environment().getBean(clientClass);
            } catch (Exception e) {
                // 类不存在或没有bean注册，尝试下一个
            }
        }
        return null;
    }
}