package org.anyline.adapter.init;


import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.entity.metadata.init.DefaultConvert;
import org.anyline.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Map;
@Component
public class ConvertAdapter {

    private static final Logger log = LoggerFactory.getLogger(ConvertAdapter.class);
    private static Map<Class, Map<Class,Convert>> converts = new Hashtable();    //DATE > {STRING>SrintCovert,TIME>timeConvert}

    static {
        //内置转换器
        for (Convert convert : DefaultConvert.values()) {
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
        }
    }

    public static Object convert(Object value, Class target){
        return convert(value, target, null);
    }
    public static Object convert(Object value, Class target, Object def){
        Object result = value;
        if(null != value){
            Class clazz = value.getClass();
            if(clazz == target){
                return value;
            }
            Map<Class, Convert> map = converts.get(clazz);
            if(null != map) {
                Convert convert = map.get(target);
                if(null != convert) {
                    try {
                        result = convert.exe(value, def);
                    }catch (ConvertException e){
                        //TODO 根据异常信息 决定下一行
                        e.printStackTrace();
                    }
                }else if(target == String.class){
                    result = value.toString();
                }else{
                    log.warn("[{}][origin class:{}][target class:{}]", LogUtil.format("convert定位失败",31), clazz, target);
                }
            }else{
                log.warn("[{}][origin class:{}][target class:{}]", LogUtil.format("convert定位失败",31), clazz, target);
            }
        }
        return result;
    }
}
