package org.anyline.data.runtime;

import org.anyline.data.util.ClientHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import java.util.Hashtable;
import java.util.Map;


public class RuntimeHolder {

    protected static Logger log = LoggerFactory.getLogger(RuntimeHolder.class);
    protected static Map<String, DataRuntime> runtimes = new Hashtable();
    protected static DefaultListableBeanFactory factory;
    public static void init(DefaultListableBeanFactory factory){
        RuntimeHolder.factory = factory;
    }


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
}