package org.anyline.adapter.init;


import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.entity.metadata.init.DefaultConvert;
import org.anyline.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;

public class ConvertAdapter {

    private static final Logger log = LoggerFactory.getLogger(ConvertAdapter.class);
    public static Map<Class, Map<Class,Convert>> converts = new Hashtable();    //DATE > {STRING>SrintCovert,TIME>timeConvert}
    public ConvertAdapter(){}
    static {
        //内置转换器
        Convert[] array = DefaultConvert.values();
        for (Convert convert : array) {
            Class origin = convert.getOrigin();
            Class target = convert.getTarget();
            Map<Class,Convert> map = converts.get(origin);
            if(null == map){
                map = new Hashtable<>();
                converts.put(origin, map);
            }
            map.put(target, convert);
        }
    }


    public static  Object convert(Object value, Class target){
        return convert(value, target, null);
    }
    public static  Object convert(Object value, Class target, Object def){
        Object result = value;
        if(null != value && null != target){
            Class clazz = value.getClass();
            if(clazz == target){
                return value;
            }
            boolean success = false;

            Map<Class, Convert> map = converts.get(clazz);
            if(null != map) {
                Convert convert = map.get(target);
                if(null != convert) {
                    try {
                        result = convert.exe(value, def);
                        success = true;
                    }catch (ConvertException e){
                        //TODO 根据异常信息 决定下一行
                        e.printStackTrace();
                    }
                }else if(target == String.class){
                    result = value.toString();
                    success = true;
                }
            }
            if(!success){
                try{
                    result = target.cast(value);
                    success = true;
                }catch (Exception e){
                }
            }
            if(!success){
                log.warn("[{}][origin class:{}][target class:{}]", LogUtil.format("convert定位失败",31), clazz, target);
                //throw new RuntimeException();
            }
        }
        return result;
    }
}
