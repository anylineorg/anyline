package org.anyline.web.listener;

import javafx.print.Collation;
import org.anyline.util.ConfigTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

@Component
public class ConfigListener implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        if(ac instanceof WebApplicationContext){
            WebApplicationContext servlet = (WebApplicationContext)ac;
            ServletContext context = servlet.getServletContext();
            Hashtable<String,Object> configs = ConfigTable.getConfigs();
            for(Map.Entry<String, Object> config: configs.entrySet()){
                Object value = config.getValue();
                context.setAttribute(ConfigTable.EL_ATTRIBUTE_PREFIX + "." + config.getKey(), value);
                if(value instanceof Collation){
                    Collection cols = (Collection) value;
                    int i = 0;
                    for(Object col:cols){
                        context.setAttribute(ConfigTable.EL_ATTRIBUTE_PREFIX + "." + config.getKey() + "[" + i++ + "]", col);
                    }
                }
                if(value instanceof Map){
                    Map map = (Map)value;
                    for(Object k: map.keySet()){
                        context.setAttribute(ConfigTable.EL_ATTRIBUTE_PREFIX + "." + config.getKey() + "[" + k + "]", map.get(k));
                    }
                }
            }
        }
    }
}
