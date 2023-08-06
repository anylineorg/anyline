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
    public static DataRuntime runtime(String key, Object source, DriverAdapter adapter) throws Exception{
        DataRuntime runtime = null;
        if(null != source){
            Class clazz = source.getClass();
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
                runtime = holder.runtime(key, source, adapter);
            }
        }
        return runtime;
    }
}
