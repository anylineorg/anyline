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
 * 
 *           
 */ 
 
 
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


package org.anyline.proxy;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DatasourceHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasourceHolderProxy {

    protected static Logger log = LoggerFactory.getLogger(DatasourceHolderProxy.class);
    private static Map<Class, DatasourceHolder> holders = new HashMap<>();
    public static void reg(Class calzz, DatasourceHolder holder){
        holders.put(calzz, holder);
    }

    public static DatasourceHolder holder(String datasource){
        DataRuntime runtime = RuntimeHolder.runtime(datasource);
        if(null == runtime){
            log.warn("[定位DatasourceHolder][{}不存在在]", datasource);
            return null;
        }
        return holder(runtime);
    }
    public static DatasourceHolder holder(DataRuntime runtime){
        Object processor = runtime.getProcessor();
        Class clazz = processor.getClass();
        DatasourceHolder holder = holder(clazz);
        return holder;
    }
    public static DatasourceHolder holder(Class clazz){
        DatasourceHolder holder = holders.get(clazz);
        //子类
        if(null == holder){
            for(Class c: holders.keySet()){
                if(ClassUtil.isInSub(clazz, c)){
                    DatasourceHolder h = holders.get(c);
                    holders.put(clazz, h);
                    holder = h;
                    break;
                }
            }
        }
        if(holders.isEmpty()){
            log.warn("[没有可用的DatasourceHolder][有可能是容器中没有注入JDBCDatasourceHolder/ElasticSearchDatasourceHolder等]");
        }
        if(null == holder){
            log.warn("[定位DatasourceHolder失败][class:{}]", clazz.getName());
        }
        return holder;
    }

    /**
     * 临时数据源
     * @param datasource 数据源, 如DruidDataSource, MongoClient
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public static DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        DataRuntime runtime = null;
        if(null != datasource){
            Class clazz = datasource.getClass();
            //类型相同
            DatasourceHolder holder = holder(clazz);
            if(null != holder){
                runtime = holder.callTemporary(datasource, database, adapter);
            }
        }
        return runtime;
    }
    public static boolean validate(DataRuntime runtime){
        DatasourceHolder holder = holder(runtime);
        if(null != holder){
            try {
                return holder.callValidate(runtime);
            }catch (Exception e){
                return false;
            }
        }
        return false;
    }
    public static boolean hit(DataRuntime runtime) throws Exception {
        DatasourceHolder holder = holder(runtime);
        return holder.callHit(runtime);
    }
    public static List<String> copy(DataRuntime runtime){
        DatasourceHolder holder = holder(runtime);
        if(null != holder){
            try {
                return holder.callCopy(runtime);
            }catch (Exception e){
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
    public static void destroy(DataRuntime runtime){
        DatasourceHolder holder = holder(runtime);
        if(null != holder){
            try {
                holder.callDestroy(runtime.getKey());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
