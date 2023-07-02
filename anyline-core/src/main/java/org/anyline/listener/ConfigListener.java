package org.anyline.listener;

import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.adapter.init.JavaTypeAdapter;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.DataType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;

@Component("org.anyline.listener.config")
public class ConfigListener {

    //用户自定义转换器
    @Autowired(required = false)
    public void setConverts(Map<String, Convert> converts) {
        //内置转换器
        for (Convert convert : converts.values()) {
            Class origin = convert.getOrigin();
            Class target = convert.getTarget();
            Map<Class,Convert> map = ConvertAdapter.converts.get(origin);
            if(null == map){
                map = new Hashtable<>();
                ConvertAdapter.converts.put(origin, map);
            }
            map.put(target, convert);

            //设置Java数据类型对应的转换器
            DataType type = JavaTypeAdapter.types.get(origin);
            if(null != type){
                type.convert(convert);
            }
        }
    }

    @Autowired(required = false)
    public void setAdapter(Map<String, EntityAdapter> adapters) {
        EntityAdapterProxy.adapters = adapters;
        String defaultKey = "anyline.entity.adapter";
        if(ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER ){
            // 如果禁用 adapter 引用 随机引用一个 , adapters引用其他
            // 计算时先调用 adapter 再用其他覆盖
            adapters.remove(defaultKey);
            for (String key : adapters.keySet()) {
                // 如果没有default 则随机引用一个
                EntityAdapterProxy.adapter = adapters.get(key);
                adapters.remove(key);
                break;
            }
        }else{
            // 如果不禁用 adapter 引用 default , adapters引用其他
            // 计算时先调用 adapter 再用其他覆盖

            EntityAdapterProxy.adapter = adapters.get(defaultKey);
            if(null == EntityAdapterProxy.adapter) {
                for (String key : adapters.keySet()) {
                    // 如果没有default 则随机引用一个
                    EntityAdapterProxy.adapter = adapters.get(key);
                    adapters.remove(key);
                    break;
                }
            }else{
                adapters.remove(defaultKey);
            }

        }

    }
}
