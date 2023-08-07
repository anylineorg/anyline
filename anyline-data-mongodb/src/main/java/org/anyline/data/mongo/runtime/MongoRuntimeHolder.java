package org.anyline.data.mongo.runtime;

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

import javax.sql.DataSource;


@Component("anyline.data.runtime.holder.mongo")
public class MongoRuntimeHolder extends RuntimeHolder {

    public MongoRuntimeHolder(){
        RuntimeHolderProxy.reg(DataSource.class,this);
    }
    @Override
    public DataRuntime runtime(String key, Object source, DriverAdapter adapter) throws Exception{
        MongoRuntime runtime = new MongoRuntime();
        if(source instanceof MongoDatabase){
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            if(source instanceof DataSource){
                MongoDatabase ds = (MongoDatabase) source;
                runtime.setClient(ds);
            }
        }else{
            throw new Exception("请提供:com.mongodb.client.MongoDatabase");
        }
        return runtime;
    }

    /**
     * 注册运行环境
     * @param key 数据源前缀
     * @param ds 数据源bean id
     */
    public static void reg(String key, String ds){
        //ClientHolder.reg(key);
        String ds_key = "anyline.datasource." + key;

        MongoDatabase db = factory.getBean(ds_key, MongoDatabase.class);
        reg(key, db, null);
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
    public static void reg(String datasource, MongoDatabase database, DriverAdapter adapter){
        log.info("[create mongo runtime][key:{}]", datasource);
        DataRuntime runtime = new MongoRuntime(datasource, database, adapter);
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

        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(FixDao.class);
        //daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        daoBuilder.addPropertyValue("runtime", runtime);
        daoBuilder.addPropertyValue("datasource", datasource);
        //daoBuilder.addPropertyValue("listener", SpringContextUtil.getBean(DMListener.class));
        //daoBuilder.addAutowiredProperty("listener");
        daoBuilder.setLazyInit(true);
        BeanDefinition daoDefinition = daoBuilder.getBeanDefinition();
        factory.registerBeanDefinition(dao_key, daoDefinition);


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
            //注销 service dao template
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
            if(factory.containsBeanDefinition("anyline.datasource." + key)){
                factory.destroySingleton("anyline.datasource." + key);
                factory.removeBeanDefinition("anyline.datasource." + key);
            }*/
            if(factory.containsBeanDefinition("anyline.mongo.database." + key)){
                factory.destroySingleton("anyline.mongo.database." + key);
                factory.removeBeanDefinition("anyline.mongo.database." + key);
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