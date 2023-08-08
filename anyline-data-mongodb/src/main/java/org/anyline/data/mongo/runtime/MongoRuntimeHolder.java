package org.anyline.data.mongo.runtime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.anyline.dao.init.springjdbc.FixDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.proxy.RuntimeHolderProxy;
import org.anyline.service.init.FixService;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.stereotype.Component;


@Component("anyline.data.runtime.holder.mongo")
public class MongoRuntimeHolder extends RuntimeHolder {

    public MongoRuntimeHolder(){
        RuntimeHolderProxy.reg(MongoClient.class,this);
    }
    @Override
    public DataRuntime runtime(String key, Object datasource, String database, DriverAdapter adapter) throws Exception{
        MongoRuntime runtime = new MongoRuntime();
        if(datasource instanceof MongoClient){
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            MongoClient client = (MongoClient) datasource;
            runtime.setClient(client);
            MongoDatabase db = client.getDatabase(database);
            runtime.setDatabase(db);
            log.warn("[注册数据源][key:{}][type:{}]", key, datasource.getClass().getSimpleName());
        }else{
            throw new Exception("请提供:com.mongodb.client.MongoClient");
        }
        return runtime;
    }

    /**
     * 注册运行环境
     * @param key 数据源前缀
     */
    public static void reg(String key){
        //ClientHolder.reg(key);
        String datasource_key = "anyline.datasource." + key;
        String database_key = "anyline.database." + key;

        MongoClient client = factory.getBean(datasource_key, MongoClient.class);
        MongoDatabase database = factory.getBean(database_key, MongoDatabase.class);
        reg(key, client, database, null);
    }


    public static void reg(String key, DataRuntime runtime){
        runtimes.put(key, runtime);
    }

    /**
     * 注册运行环境
     * @param datasource 数据源前缀
     * @param database MongoDatabase
     * @param adapter adapter 可以为空 第一次执行时补齐
     */
    public static void reg(String datasource, MongoClient client, MongoDatabase database, DriverAdapter adapter){
        log.info("[create mongo runtime][key:{}]", datasource);
        DataRuntime runtime = new MongoRuntime(datasource, client, database, adapter);
        if(runtimes.containsKey(datasource)){
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);
        if(!ConfigTable.IS_MULTIPLE_SERVICE){
            return;
        }
        String dao_key = "anyline.dao." + datasource;
        String service_key = "anyline.service." + datasource;
        log.info("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        //dao
        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(FixDao.class);
        //daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        daoBuilder.addPropertyValue("runtime", runtime);
        daoBuilder.addPropertyValue("datasource", datasource);
        //daoBuilder.addPropertyValue("listener", SpringContextUtil.getBean(DMListener.class));
        //daoBuilder.addAutowiredProperty("listener");
        daoBuilder.setLazyInit(true);
        BeanDefinition daoDefinition = daoBuilder.getBeanDefinition();
        factory.registerBeanDefinition(dao_key, daoDefinition);

        //service
        BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder.genericBeanDefinition(FixService.class);
        //serviceBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        serviceBuilder.addPropertyValue("datasource", datasource);
        serviceBuilder.addPropertyReference("dao", dao_key);
        //serviceBuilder.addAutowiredProperty("cacheProvider");
        serviceBuilder.setLazyInit(true);
        BeanDefinition serviceDefinition = serviceBuilder.getBeanDefinition();
        factory.registerBeanDefinition(service_key, serviceDefinition);


    }
    public static void destroyRuntime(String key){
        destroy(key);
    }
    public static void destroy(String key){
        try {
            runtimes.remove(key);
            //注销 service dao client
            if(factory.containsBeanDefinition("anyline.service." + key)){
                factory.destroySingleton("anyline.service." + key);
                factory.removeBeanDefinition("anyline.service." + key);
            }
            if(factory.containsBeanDefinition("anyline.dao." + key)){
                factory.destroySingleton("anyline.dao." + key);
                factory.removeBeanDefinition("anyline.dao." + key);
            }
/*            if(factory.containsBeanDefinition("anyline.jdbc.template." + key)){
                factory.destroySingleton("anyline.jdbc.template." + key);
                factory.removeBeanDefinition("anyline.jdbc.template." + key);
            }
            if(factory.containsBeanDefinition("anyline.transaction." + key)){
                factory.destroySingleton("anyline.transaction." + key);
                factory.removeBeanDefinition("anyline.transaction." + key);
            }
            */
            if(factory.containsBeanDefinition("anyline.datasource." + key)){
                factory.destroySingleton("anyline.datasource." + key);
                factory.removeBeanDefinition("anyline.datasource." + key);
            }
            if(factory.containsBeanDefinition("anyline.database." + key)){
                factory.destroySingleton("anyline.database." + key);
                factory.removeBeanDefinition("anyline.database." + key);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static MongoDatabase getDatabase(String key){
        DataRuntime runtime = getRuntime(key);
        if(null != runtime){
            return (MongoDatabase) runtime.getClient();
        }
        return null;
    }
}