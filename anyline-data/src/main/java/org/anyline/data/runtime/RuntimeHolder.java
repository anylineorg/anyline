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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public interface RuntimeHolder {
    Logger log = LoggerFactory.getLogger(RuntimeHolder.class);
    Map<String, DataRuntime> runtimes = new Hashtable<>();

    /**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到spring
     * @param datasource 数据源, 如DruidDataSource, MongoClient, es.RestClient
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception;
   static boolean destroy(String datasource) throws Exception {
       DataRuntime runtime = RuntimeHolder.runtime(datasource);
       if(null != runtime) {
           return runtime.destroy();
       }
       return false;
   }

    /**
     * 从origin复制的数据源
     * @param origin 源
     * @return Map
     */
    static Map<String, DataRuntime> runtimes(String origin) {
        Map<String, DataRuntime> map = new Hashtable<>();
        for(String key:RuntimeHolder.runtimes.keySet()) {
            DataRuntime runtime = runtimes.get(key);
            if(origin.equals(runtime.origin())) {
                map.put(key, runtime);
            }
        }
        return map;
    }

    static DataRuntime runtime() {
        return runtime(null);
    }
    /**
     * 数据源相关的runtime
     * @param datasource 数据源
     * @return DataRuntime
     */
    static DataRuntime runtime(String datasource) {
        DataRuntime runtime = null;
        if(null == datasource) {
            //通用数据源
            datasource = "default";
        }
        runtime = runtimes.get(datasource);
        if(null == runtime) {
            throw new RuntimeException("未注册数据源:"+datasource);
        }
        return runtime;
    }

    /**
     * runtime是否存在
     * @param key key
     * @return boolean
     */
    static boolean contains(String key) {
        if(null == key) {
            return false;
        }
        return runtimes.containsKey(key);
    }

    /**
     * 全部runtime.key
     * @return list
     */
    static List<String> keys() {
        List<String> list = new ArrayList<>();
        list.addAll(runtimes.keySet());
        return list;
    }
}