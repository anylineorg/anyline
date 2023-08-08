package org.anyline.proxy;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.util.ClassUtil;

import java.util.HashMap;
import java.util.Map;

public class RuntimeHolderProxy {

    private static Map<Class, RuntimeHolder> holders = new HashMap<>();
    public static void reg(Class calzz, RuntimeHolder holder){
        holders.put(calzz, holder);
    }


    /**
     * 临时数据源
     * @param key 数据源标识,切换数据源时根据key,输出日志时标记当前数据源
     * @param datasource 数据源,如DruidDataSource,MongoClient
     * @param database 数据库,jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter() ,如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public static DataRuntime runtime(String key, Object datasource, String database, DriverAdapter adapter) throws Exception{
        DataRuntime runtime = null;
        if(null != datasource){
            Class clazz = datasource.getClass();
            //类型相同
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
            if(null != holder){
                runtime = holder.runtime(key, datasource, database, adapter);
            }
        }
        return runtime;
    }
}
