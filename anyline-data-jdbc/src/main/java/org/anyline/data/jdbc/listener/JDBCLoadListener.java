package org.anyline.data.jdbc.listener;

import org.anyline.annotation.Autowired;
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

    @Autowired(required = false)
    private DMListener dmListener;

    @Autowired(required = false)
    private DDListener ddListener;

    @Autowired(required = false)
    private PrimaryGenerator primaryGenerator;

    private Map<String, DriverAdapter> adapters = null;
    private Map<String, DriverWorker> workers = null;
    private Map<String, DataSourceLoader> loaders = null;

    @Autowired(required = false)
    public void setCacheProvider(CacheProvider provider) {
        CacheProxy.init(provider);
    }

    @Autowired(required = false)
    public void setAdapters(Map<String, DriverAdapter> adapters) {
        this.adapters = adapters;
    }

    @Autowired(required = false)
    public void setWorkers(Map<String, DriverWorker> workers) {
        this.workers = workers;
    }


    @Autowired(required = false)
    public void setLoaders(Map<String, DataSourceLoader> map) {
        loaders = map;
    }

    /**
     * 注入拦截器
     *
     * @param interceptors interceptors
     */
    @Autowired(required = false)
    public void setQueryInterceptors(Map<String, QueryInterceptor> interceptors) {
        InterceptorProxy.setQueryInterceptors(interceptors);
    }

    @Autowired(required = false)
    public void setCountInterceptors(Map<String, CountInterceptor> interceptors) {
        InterceptorProxy.setCountInterceptors(interceptors);
    }

    @Autowired(required = false)
    public void setUpdateInterceptors(Map<String, UpdateInterceptor> interceptors) {
        InterceptorProxy.setUpdateInterceptors(interceptors);
    }

    @Autowired(required = false)
    public void setInsertInterceptors(Map<String, InsertInterceptor> interceptors) {
        InterceptorProxy.setInsertInterceptors(interceptors);
    }

    @Autowired(required = false)
    public void setDeleteInterceptors(Map<String, DeleteInterceptor> interceptors) {
        InterceptorProxy.setDeleteInterceptors(interceptors);
    }

    @Autowired(required = false)
    public void setExecuteInterceptors(Map<String, ExecuteInterceptor> interceptors) {
        InterceptorProxy.setExecuteInterceptors(interceptors);
    }

    @Autowired(required = false)
    public void setDDInterceptors(Map<String, DDInterceptor> interceptors) {
        InterceptorProxy.setDDInterceptors(interceptors);

    }
    @Override
    public void after() {
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
            adapters = ConfigTable.worker.getBeans(DriverAdapter.class);
        }
        if(null == workers || workers.isEmpty()){
            workers = ConfigTable.worker.getBeans(DriverWorker.class);
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

        AnylineService service = ConfigTable.worker.getBean("anyline.service.default", AnylineService.class);
        ServiceProxy.init(service);
    }
}
