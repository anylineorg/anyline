package org.anyline.data.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.util.ClientHolder;
import org.anyline.proxy.RuntimeHolderProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Hashtable;
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
     * @param key 数据源标识,切换数据源时根据key,输出日志时标记当前数据源
     * @param datasource 数据源,如DruidDataSource,MongoClient
     * @param database 数据库,jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter() ,如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public DataRuntime temporary(String key, Object datasource, String database, DriverAdapter adapter) throws Exception{
        return RuntimeHolderProxy.temporary(key, datasource, database, adapter);
    }
    public abstract DataRuntime regTemporary(String key, Object datasource, String database, DriverAdapter adapter) throws Exception;
    public static void destroy(String key){
        RuntimeHolderProxy.destroy(key);
    }
    public abstract void exeDestroy(String key);
    public static void reg(String key, DataRuntime runtime){
        runtimes.put(key, runtime);
    }
    public static DataRuntime getRuntime(){
        return getRuntime(ClientHolder.curDataSource());
    }
    public static DataRuntime getRuntime(String datasource){
        DataRuntime runtime = null;
        if(null == datasource){
            if(null == runtime){
                //通用数据源
                datasource = "common";
                runtime = runtimes.get(datasource);
            }
        }else {
            runtime = runtimes.get(datasource);
        }
        if(null == runtime){
            throw new RuntimeException("未注册数据源:"+datasource);
        }
        return runtime;
    }

    public static void destroyBean(String bean){
        if(factory.containsSingleton(bean)){
            factory.destroySingleton(bean);
        }
        if(factory.containsBeanDefinition(bean)){
            factory.removeBeanDefinition(bean);
        }
    }
}