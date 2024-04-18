package org.anyline.data.jdbc.listener;

import org.anyline.annotation.Component;
import org.anyline.bean.LoadListener;
import org.anyline.cache.CacheProvider;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.adapter.DriverWorker;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.interceptor.*;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;

import java.util.Map;

@Component("anyline.environment.data.listener.jdbc")
public class JDBCLoadListener implements LoadListener {
    @Override
    public void after() {
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

        DMListener dmListener = ConfigTable.environment().getBean(DMListener.class);
        PrimaryGenerator primaryGenerator = ConfigTable.environment().getBean(PrimaryGenerator.class);
        DDListener ddListener = ConfigTable.environment().getBean(DDListener.class);
        Map<String, DriverAdapter> adapters = ConfigTable.environment().getBeans(DriverAdapter.class);
        Map<String, DriverWorker> workers = ConfigTable.environment().getBeans(DriverWorker.class);
        Map<String, DataSourceLoader> loaders =ConfigTable.environment().getBeans(DataSourceLoader.class);
        if(null != adapters){
            DriverAdapterHolder.setAdapters(adapters);
            for(DriverAdapter adapter:adapters.values()){
                if(null != dmListener){
                    adapter.setListener(dmListener);
                }
                if(null != ddListener){
                    adapter.setListener(ddListener);
                }
                if(null != primaryGenerator){
                    adapter.setGenerator(primaryGenerator);
                }
                //anyline.data.jdbc.delimiter.db2
                String delimiter = ConfigTable.getString("anyline.data.jdbc.delimiter."+adapter.type().name().toLowerCase());
                if(null != delimiter){
                    adapter.setDelimiter(delimiter);
                }
            }
        }
        if(null != loaders){
            for(DataSourceLoader loader:loaders.values()){
                loader.load();
            }
        }
        if(null == adapters || adapters.isEmpty()){
            adapters = ConfigTable.environment().getBeans(DriverAdapter.class);
        }
        if(null == workers || workers.isEmpty()){
            workers = ConfigTable.environment().getBeans(DriverWorker.class);
        }
        if(null != workers && null != adapters){
            for(DriverWorker worker:workers.values()){
                Class clazz = worker.supportAdapterType();
                for(DriverAdapter adapter:adapters.values()){
                    if(ClassUtil.isInSub(adapter.getClass(), clazz)){
                        DriverWorker origin = adapter.getWorker();
                        //没有设置过worker 或原来的优先级更低
                        if(null == origin || origin.priority() < worker.priority()) {
                            adapter.setWorker(worker);
                        }
                    }
                }
            }
        }
        if(ConfigTable.environment().containsBean("anyline.service.default")) {
            AnylineService service = ConfigTable.environment().getBean("anyline.service.default", AnylineService.class);
            ServiceProxy.init(service);
            if(null != service){
                Map<String, AnylineService> services = ConfigTable.environment().getBeans(AnylineService.class);
                for(AnylineService item:services.values()){
                    if(null == item.getDao()){
                        item.setDao(service.getDao());
                    }
                }
            }
        }
    }
}
