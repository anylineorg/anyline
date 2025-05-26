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

package org.anyline.data.listener.init;

import org.anyline.annotation.AnylineComponent;
import org.anyline.cache.CacheProvider;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.interceptor.*;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.listener.DataSourceListener;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.listener.LoadListener;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AnylineComponent("anyline.environment.data.listener.jdbc")
public class DataSourceLoadListener implements LoadListener {
    private static Log log = LogProxy.get(DataSourceLoadListener.class);

    //项目注册事件
    private List<DataSourceListener> listeners = new ArrayList<>();
    public void before(Object bean) {
        if(bean instanceof Map){
            Map map = (Map) bean;
            for(Object value:map.values()){
                if(value instanceof DataSourceListener){
                    listeners.add((DataSourceListener) value);
                }
            }
        }
    }
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
        Map<String, DriverActuator> actuators = ConfigTable.environment().getBeans(DriverActuator.class);
        Map<String, DataSourceLoader> loaders =ConfigTable.environment().getBeans(DataSourceLoader.class);
        DataSourceMonitor monitor = ConfigTable.environment().getBean(DataSourceMonitor.class);
        DriverAdapterHolder.setMonitor(monitor);
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
                String delimiter = ConfigTable.getString("anyline.data.jdbc.delimiter." + adapter.type().name().toLowerCase());
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
        if(null == adapters || adapters.isEmpty()) {
            adapters = ConfigTable.environment().getBeans(DriverAdapter.class);
        }
        if(null == actuators || actuators.isEmpty()) {
            actuators = ConfigTable.environment().getBeans(DriverActuator.class);
        }
        if(null != actuators && null != adapters) {
            for(DriverActuator actuator:actuators.values()) {
                Class clazz = actuator.supportAdapterType();
                for(DriverAdapter adapter:adapters.values()) {
                    if(ClassUtil.isInSub(adapter.getClass(), clazz)) {
                        DriverActuator origin = adapter.getActuator();
                        //没有设置过actuator 或原来的优先级更低
                        if(null == origin || origin.priority() < actuator.priority()) {
                            adapter.setActuator(actuator);
                        }
                    }
                }
            }
        }
        if(null != adapters) {
            for(DriverAdapter adapter:adapters.values()) {
                if(null == adapter.getActuator()) {
                    log.warn("[not found actuator][adapter:{}]", adapter);
                }
            }
        }
    }

    @Override
    public void finish() {
        if(ConfigTable.environment().containsBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + "default")) {
            ConfigTable.environment().regAlias(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + "default", "anyline.service");
            AnylineService service = ConfigTable.environment().getBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + "default", AnylineService.class);
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
    @Override
    public void after(){
        for(DataSourceListener listener: listeners){
            listener.after();
        }
    }

}
