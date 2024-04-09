package org.anyline.environment.solon;


import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.init.DefaultEntityAdapter;
import org.anyline.adapter.init.JavaTypeAdapter;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.DataType;
import org.anyline.proxy.ConvertProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.ConfigTable;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import java.util.Hashtable;
import java.util.Map;

@Component("anyline.environment.configuration.solon")
public class SolonAutoConfiguration {

    //用户自定义数据类型转换器
    @Inject(required = false)
    private Map<String, Convert> converts;
    //用户自定义实体类转换器
    @Inject(required = false)
    private Map<String, EntityAdapter> adapters;

    @Init
    public void init() {
        //内置转换器
        for (Convert convert : converts.values()) {
            Class origin = convert.getOrigin();
            Class target = convert.getTarget();
            Map<Class, Convert> map = ConvertProxy.converts.get(origin);
            if(null == map){
                map = new Hashtable<>();
                ConvertProxy.converts.put(origin, map);
            }
            map.put(target, convert);

            //设置Java数据类型对应的转换器
            DataType type = JavaTypeAdapter.types.get(origin);
            if(null != type){
                type.convert(convert);
            }
        }
        //是否禁用默认adapter
        if(ConfigTable.IS_DISABLED_DEFAULT_ENTITY_ADAPTER ){
            for(String key:adapters.keySet()){
                EntityAdapter adapter = adapters.get(key);
                if(adapter instanceof DefaultEntityAdapter){
                    adapters.remove(key);
                }
            }
        }
        EntityAdapterProxy.setAdapters(adapters);
    }
}
