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


package org.anyline.data.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.proxy.DatasourceHolderProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public abstract class RuntimeHolder {
    protected static Logger log = LoggerFactory.getLogger(RuntimeHolder.class);
    protected static Map<String, DataRuntime> runtimes = new Hashtable();
    protected static DefaultListableBeanFactory factory;
    public static void init(DefaultListableBeanFactory factory){
        RuntimeHolder.factory = factory;
    }

    /**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到spring
     * @param datasource 数据源, 如DruidDataSource, MongoClient, es.RestClient
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public static DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception{
        return DatasourceHolderProxy.temporary(datasource, database, adapter);
    }
    public abstract DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception;
    public static void destroy(String datasource){
        DatasourceHolderProxy.destroy(RuntimeHolder.runtime(datasource));
    }
    public abstract void callDestroy(String datasource);
    public static void reg(String key, DataRuntime runtime){
        runtimes.put(key,  runtime);
    }
    public static DataRuntime runtime(){
        return runtime(null);
    }

    public static Map<String,  DataRuntime> all(){
        return runtimes;
    }
    /**
     * 从origin复制的数据源
     * @param origin 源
     * @return Map
     */
    public static Map<String,  DataRuntime> runtimes(String origin){
        Map<String,  DataRuntime> map = new Hashtable<>();
        for(String key:runtimes.keySet()){
           DataRuntime runtime = runtimes.get(key);
           if(origin.equals(runtime.origin())){
               map.put(key,  runtime);
           }
        }
        return map;
    }
    public static DataRuntime runtime(String datasource){
        DataRuntime runtime = null;
        if(null == datasource){
            //通用数据源
            datasource = "default";
        }
        runtime = runtimes.get(datasource);
        if(null == runtime){
            throw new RuntimeException("未注册数据源:"+datasource);
        }
        return runtime;
    }
    public static boolean contains(String key){
        if(null == key){
            return false;
        }
        return runtimes.containsKey(key);
    }
    public static List<String> keys(){
        List<String> list = new ArrayList<>();
        list.addAll(runtimes.keySet());
        return list;
    }

    public static void destroyBean(String bean){
        if(factory.containsSingleton(bean)){
            factory.destroySingleton(bean);
        }
        if(factory.containsBeanDefinition(bean)){
            factory.removeBeanDefinition(bean);
        }
    }

    /**
     * 验证数据源可用性
     * @param ds 数据源
     * @return boolean
     */
    public static boolean validate(String ds){
        return validate(runtime(ds));
    }
    public static boolean validate(){
        return validate(runtime());
    }
    public static boolean validate(DataRuntime runtime){
        return DatasourceHolderProxy.validate(runtime);
    }

    public static boolean hit(String ds) throws Exception{
        return hit(runtime(ds));
    }
    public static boolean hit() throws Exception{
        return hit(runtime());
    }
    public static boolean hit(DataRuntime runtime) throws Exception{
        return DatasourceHolderProxy.hit(runtime);
    }
}