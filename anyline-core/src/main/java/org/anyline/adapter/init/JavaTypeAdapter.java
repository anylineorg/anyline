package org.anyline.adapter.init;


import org.anyline.metadata.type.DataType;
import org.anyline.metadata.type.init.DefaultJavaType;

import java.util.Hashtable;
import java.util.Map;
public class JavaTypeAdapter {
    public static Map<Class, DataType> types = new Hashtable<>();
    public JavaTypeAdapter(){

    }
    public static DataType type(Class clazz){
        if(null != clazz){
            return types.get(clazz);
        }else{
            return null;
        }
    }
    public static void reg(Class clazz, DataType type){
        types.put(clazz, type);
    }
    static {
        //支持的数据类型
        for(DefaultJavaType type:DefaultJavaType.values()){
            Class clazz = type.supportClass();
            reg(clazz, type);
        }
    }
}
