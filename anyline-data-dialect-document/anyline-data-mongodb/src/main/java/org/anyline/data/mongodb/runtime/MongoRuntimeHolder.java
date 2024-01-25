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


package org.anyline.data.mongodb.runtime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.anyline.dao.init.springjdbc.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.proxy.RuntimeHolderProxy;
import org.anyline.service.init.DefaultService;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.stereotype.Component;


@Component("anyline.data.runtime.holder.mongo")
public class MongoRuntimeHolder extends RuntimeHolder {

    public MongoRuntimeHolder(){
        RuntimeHolderProxy.reg(MongoClient.class, this);
    }

    /**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到spring
     * @param datasource 数据源, 如DruidDataSource, MongoClient
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public static DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        MongoRuntime runtime = new MongoRuntime();
        if(datasource instanceof MongoClient){
            String key = "temporary_mongo";
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

    @Override
    public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        return temporary( datasource, database, adapter);
    }

    /**
     * 注册运行环境
     * @param key 数据源前缀
     */
    public static void reg(String key){
        //ClientHolder.reg(key);
        String datasource_key = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        String database_key = DataRuntime.ANYLINE_DATABASE_BEAN_PREFIX + key;

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
        String dao_key = DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  datasource;
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  datasource;
        log.info("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        //dao
        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultDao.class);
        //daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        daoBuilder.addPropertyValue("runtime", runtime);
        daoBuilder.addPropertyValue("datasource", datasource);
        //daoBuilder.addPropertyValue("listener", SpringContextUtil.getBean(DMListener.class));
        //daoBuilder.addAutowiredProperty("listener");
        daoBuilder.setLazyInit(true);
        BeanDefinition daoDefinition = daoBuilder.getBeanDefinition();
        factory.registerBeanDefinition(dao_key, daoDefinition);

        //service
        BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultService.class);
        //serviceBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        serviceBuilder.addPropertyValue("datasource", datasource);
        serviceBuilder.addPropertyReference("dao", dao_key);
        //serviceBuilder.addAutowiredProperty("cacheProvider");
        serviceBuilder.setLazyInit(true);
        BeanDefinition serviceDefinition = serviceBuilder.getBeanDefinition();
        factory.registerBeanDefinition(service_key, serviceDefinition);


    }
    public static void destroy(String key){
        try {
            runtimes.remove(key);
            //注销 service dao client
            if(factory.containsBeanDefinition(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key)){
                factory.destroySingleton(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key);
                factory.removeBeanDefinition(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key);
            }
            if(factory.containsBeanDefinition(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key)){
                factory.destroySingleton(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key);
                factory.removeBeanDefinition(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key);
            }
/*            if(factory.containsBeanDefinition(DataRuntime.ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX +  key)){
                factory.destroySingleton(DataRuntime.ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX +  key);
                factory.removeBeanDefinition(DataRuntime.ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX +  key);
            }
            if(factory.containsBeanDefinition(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key)){
                factory.destroySingleton(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key);
                factory.removeBeanDefinition(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key);
            }
            */
            if(factory.containsBeanDefinition(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key)){
                factory.destroySingleton(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
                factory.removeBeanDefinition(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
            }
            if(factory.containsBeanDefinition(DataRuntime.ANYLINE_DATABASE_BEAN_PREFIX + key)){
                factory.destroySingleton(DataRuntime.ANYLINE_DATABASE_BEAN_PREFIX + key);
                factory.removeBeanDefinition(DataRuntime.ANYLINE_DATABASE_BEAN_PREFIX + key);
            }

            //从当前数据原复制的一块注销
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void callDestroy(String key) {
        destroy(key);
    }

    public static MongoDatabase getDatabase(String key){
        DataRuntime runtime = runtime(key);
        if(null != runtime){
            return (MongoDatabase) runtime.getProcessor();
        }
        return null;
    }
}