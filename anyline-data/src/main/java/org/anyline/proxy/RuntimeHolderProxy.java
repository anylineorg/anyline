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
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RuntimeHolderProxy {
    protected static Logger log = LoggerFactory.getLogger(RuntimeHolderProxy.class);
    private static Map<Class, RuntimeHolder> holders = new HashMap<>();
    public static void reg(Class calzz, RuntimeHolder holder){
        holders.put(calzz, holder);
    }

    public static RuntimeHolder holder(String datasource){
        DataRuntime runtime = RuntimeHolder.runtime(datasource);
        if(null == runtime){
            log.warn("[定位RuntimeHolder][{}不存在在]", datasource);
            return null;
        }
        return holder(runtime);
    }
    public static RuntimeHolder holder(DataRuntime runtime){
        Object processor = runtime.getProcessor();
        Class clazz = processor.getClass();
        RuntimeHolder holder = holder(clazz);
        return holder;
    }
    public static RuntimeHolder holder(Class clazz){
        RuntimeHolder holder = holders.get(clazz);
        //子类
        if(null == holder){
            for(Class c: holders.keySet()){
                if(ClassUtil.isInSub(clazz, c)){
                    RuntimeHolder h = holders.get(c);
                    holders.put(clazz, h);
                    holder = h;
                    break;
                }
            }
        }
        return holder;
    }

    public static DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception{
        RuntimeHolder holder = holder(database.getClass());
        if(null == holder){
            throw new Exception("根据datasource定位失败, 请直接调用相应的*DatasourceHolder.temporary()");
        }
        return holder.callTemporary(datasource, database, adapter);
    }
}
