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

package org.anyline.environment.spring;

import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.init.DefaultEntityAdapter;
import org.anyline.adapter.init.JavaTypeAdapter;
import org.anyline.listener.DataSourceListener;
import org.anyline.listener.LoadListener;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.DataType;
import org.anyline.proxy.ConvertProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("anyline.environment.configuration.spring")
public class SpringAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    private LinkedHashMap<String, LoadListener> load_listeners;
    private Map<String, DataSourceListener> datasource_listeners;
    private boolean loader_start_status = false;
    private boolean loader_after_status = false;
    @Autowired
    public void setWorker(SpringEnvironmentWorker worker) {
        ConfigTable.setEnvironment(worker);
        loaderStart();
    }

    @Autowired(required = false)
    public void setLoadListeners(Map<String, LoadListener> listeners) {
        if(null == this.load_listeners){
            this.load_listeners = new LinkedHashMap<>();
        }
        this.load_listeners.putAll(listeners);
    }
    @Autowired(required = false)
    public void setDataSourceListeners(Map<String, DataSourceListener> listeners) {
        this.datasource_listeners = listeners;
    }
    //用户自定义数据类型转换器
    @Autowired(required = false)
    public void setConverts(Map<String, Convert> converts) {
        //内置转换器
        for (Convert convert : converts.values()) {
            Class origin = convert.getOrigin();
            Class target = convert.getTarget();
            Map<Class, Convert> map = ConvertProxy.converts.get(origin);
            if(null == map) {
                map = new Hashtable<>();
                ConvertProxy.converts.put(origin, map);
            }
            map.put(target, convert);

            //设置Java数据类型对应的转换器
            DataType type = JavaTypeAdapter.types.get(origin);
            if(null != type) {
                type.convert(convert);
            }
        }
    }
    //用户自定义实体类转换器
    @Autowired(required = false)
    public void setEntityAdapter(Map<String, EntityAdapter> adapters) {
        //是否禁用默认adapter
        if(ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER ) {
            for(String key:adapters.keySet()) {
                EntityAdapter adapter = adapters.get(key);
                if(adapter instanceof DefaultEntityAdapter) {
                    adapters.remove(key);
                }
            }
        }
        EntityAdapterProxy.setAdapters(adapters);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(!loader_start_status) {
            //排序
            List<Map.Entry<String, LoadListener>> entries = new ArrayList<>(this.load_listeners.entrySet());

            entries.sort(Comparator.comparingInt(
                    entry -> entry.getValue().index()
            ));
            this.load_listeners.clear();
            entries.forEach(entry -> this.load_listeners.put(entry.getKey(), entry.getValue()));
            loaderStart();
            loaderAfter();
        }
    }
    private void loaderStart() {
        if(!loader_start_status && null != load_listeners && null != ConfigTable.environment) {
            loader_start_status = true;
            for (LoadListener listener : load_listeners.values()) {
                listener.before(datasource_listeners);
            }
            for (LoadListener listener : load_listeners.values()) {
                listener.start();
            }
        }
    }
    private void loaderAfter() {
        if(!loader_after_status && null != load_listeners && null != ConfigTable.environment) {
            loader_after_status = true;
            for (LoadListener listener : load_listeners.values()) {
                listener.finish();
            }
            for (LoadListener listener : load_listeners.values()) {
                listener.after();
            }
        }
    }
}
