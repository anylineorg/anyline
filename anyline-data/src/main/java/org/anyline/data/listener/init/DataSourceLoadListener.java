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



package org.anyline.data.listener.init;

import org.anyline.annotation.Component;
import org.anyline.bean.LoadListener;
import org.anyline.cache.CacheProvider;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.interceptor.*;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;

import java.util.Map;

@Component("anyline.environment.data.listener.jdbc")
public class DataSourceLoadListener implements LoadListener {
    @Override
    public void start() {
        //缓存
        CacheProvider provider = ConfigTable.environment().getBean(CacheProvider.class);
        CacheProxy.init(provider);
        //注入拦截器
        InterceptorProxy.setQueryInterceptors(ConfigTable.environment().getBeans(QueryInterceptor.class));
        InterceptorProxy.setCountInterceptors(ConfigTable.environment().getBeans(CountInterceptor.class));
        InterceptorProxy.setUpdateInterceptors(ConfigTable.environment().getBeans(UpdateInterceptor.class));
        InterceptorProxy.setInsertInterceptors(ConfigTable.environment().getBeans(InsertInterceptor.class));
        InterceptorProxy.setDeleteInterceptors(ConfigTable.environment().getBeans(DeleteInterceptor.class));
        InterceptorProxy.setExecuteInterceptors(ConfigTable.environment().getBeans(ExecuteInterceptor.class));
        InterceptorProxy.setDDInterceptors(ConfigTable.environment().getBeans(DDInterceptor.class));

        PrimaryGenerator primaryGenerator = ConfigTable.environment().getBean(PrimaryGenerator.class);
        DMListener dmListener = ConfigTable.environment().getBean(DMListener.class);
        DDListener ddListener = ConfigTable.environment().getBean(DDListener.class);
        Map<String, DriverAdapter> adapters = ConfigTable.environment().getBeans(DriverAdapter.class);
        Map<String, DriverActuator> workers = ConfigTable.environment().getBeans(DriverActuator.class);
        Map<String, DataSourceLoader> loaders =ConfigTable.environment().getBeans(DataSourceLoader.class);

        //数据库操作适配器
        if(null != adapters) {
            DriverAdapterHolder.setAdapters(adapters);
            for(DriverAdapter adapter:adapters.values()) {
                if(null != dmListener) {
                    adapter.setListener(dmListener);
                }
                if(null != ddListener) {
                    adapter.setListener(ddListener);
                }
                if(null != primaryGenerator) {
                    adapter.setGenerator(primaryGenerator);
                }
                //anyline.data.jdbc.delimiter.db2
                String delimiter = ConfigTable.getString("anyline.data.jdbc.delimiter."+adapter.type().name().toLowerCase());
                if(null != delimiter) {
                    adapter.setDelimiter(delimiter);
                }
            }
        }
        if(null != loaders) {
            for(DataSourceLoader loader:loaders.values()) {
                loader.load();
            }
        }
        Object def = ConfigTable.environment().getBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX+"default");
        if(null == ConfigTable.environment().getBean("anyline.service") && null != def) {
            ConfigTable.environment().regBean("anyline.service", def);
        }
        if(null == adapters || adapters.isEmpty()) {
            adapters = ConfigTable.environment().getBeans(DriverAdapter.class);
        }
        if(null == workers || workers.isEmpty()) {
            workers = ConfigTable.environment().getBeans(DriverActuator.class);
        }
        if(null != workers && null != adapters) {
            for(DriverActuator worker:workers.values()) {
                Class clazz = worker.supportAdapterType();
                for(DriverAdapter adapter:adapters.values()) {
                    if(ClassUtil.isInSub(adapter.getClass(), clazz)) {
                        DriverActuator origin = adapter.getActuator();
                        //没有设置过worker 或原来的优先级更低
                        if(null == origin || origin.priority() < worker.priority()) {
                            adapter.setActuator(worker);
                        }
                    }
                }
            }
        }
        if(ConfigTable.environment().containsBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX+"default")) {
            AnylineService service = ConfigTable.environment().getBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX+"default", AnylineService.class);
            if(null != service) {
                ServiceProxy.init(service);
                Map<String, AnylineService> services = ConfigTable.environment().getBeans(AnylineService.class);
                for(AnylineService item:services.values()) {
                    if(null == item.getDao()) {
                        item.setDao(service.getDao());
                    }
                }
            }
        }
    }
}
