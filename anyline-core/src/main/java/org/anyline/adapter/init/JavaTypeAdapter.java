package org.anyline.adapter.init;


import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.DataType;
import org.anyline.entity.metadata.init.DefaultConvert;
import org.anyline.entity.metadata.init.DefaultJavaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;
@Component
public class JavaTypeAdapter {
    private static Map<Class, DataType> types = new Hashtable<>();

    public static org.anyline.entity.metadata.DataType type(Class clazz){
        if(null != clazz){
            return types.get(clazz);
        }else{
            return null;
        }
    }
    static {
        //支持的数据类型
        for(DefaultJavaType type:DefaultJavaType.values()){
            Class clazz = type.supportClass();
            types.put(clazz, type);
        }
        //内置转换器
        for (Convert convert : DefaultConvert.values()) {
            Class origin = convert.getOrigin();
            DataType type = types.get(origin);
            if(null != type){
                type.convert(convert);
            }
        }
    }

    //用户自定义转换器
    @Autowired(required = false)
    public void setConverts(Map<String, Convert> converts) {
        for (Convert item : converts.values()) {
            Class origin = item.getOrigin();
            DataType type = types.get(origin);
            if(null != type){
                type.convert(item);
            }
        }
    }
}
