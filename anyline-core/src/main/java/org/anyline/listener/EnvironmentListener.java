package org.anyline.listener;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.CharUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class EnvironmentListener implements  EnvironmentAware {
    private Logger log = LoggerFactory.getLogger(EnvironmentListener.class);

    @Override
    public void setEnvironment(Environment environment) {
        Field[] fields = ConfigTable.class.getDeclaredFields();
        for(Field field:fields){
            String name = field.getName();
            String value = getProperty("anyline", environment, "."+name);
            if(BasicUtil.isNotEmpty(value)) {
                BeanUtil.setFieldValue(null, field, value);
            }

        }
    }

    /**
     * 根据配置文件提取指定key的值
     * @param prefix 前缀
     * @param env 配置文件环境
     * @param keys key列表 第一个有值的key生效
     * @return String
     */
    private static String getProperty(String prefix, Environment env, String ... keys){
        String value = null;
        if(null == env || null == keys){
            return value;
        }
        if(null == prefix){
            prefix = "";
        }
        for(String key:keys){
            key = prefix + key;
            value = env.getProperty(key);
            if(null != value){
                return value;
            }
            //以中划线分隔的配置文件
            String[] ks = key.split("-");
            String sKey = null;
            for(String k:ks){
                if(null == sKey){
                    sKey = k;
                }else{
                    sKey = sKey + CharUtil.toUpperCaseHeader(k);
                }
            }
            value = env.getProperty(sKey);
            if(null != value){
                return value;
            }

            //以下划线分隔的配置文件
            ks = key.split("_");
            sKey = null;
            for(String k:ks){
                if(null == sKey){
                    sKey = k;
                }else{
                    sKey = sKey + CharUtil.toUpperCaseHeader(k);
                }
            }
            value = env.getProperty(sKey);
            if(null != value){
                return value;
            }

            ks = key.toLowerCase().split("_");
            sKey = null;
            for(String k:ks){
                if(null == sKey){
                    sKey = k;
                }else{
                    sKey = sKey + CharUtil.toUpperCaseHeader(k);
                }
            }
            value = env.getProperty(sKey);
            if(null != value){
                return value;
            }

            //中划线
            sKey = key.replace("_","-");
            value = env.getProperty(sKey);
            if(null != value){
                return value;
            }

            //小写中划线
            sKey = key.toLowerCase().replace("_","-");
            value = env.getProperty(sKey);
            if(null != value){
                return value;
            }
        }
        return value;
    }

}
