package org.anyline.web.listener;

import javafx.print.Collation;
import org.anyline.util.ConfigTable;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

@WebListener
public class ConfigListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent e){
        ServletContext context = e.getServletContext();
        Hashtable<String,Object> configs = ConfigTable.getConfigs();
        for(Map.Entry<String, Object> config: configs.entrySet()){
            Object value = config.getValue();
            context.setAttribute(ConfigTable.EL_ATTRIBUTE_DEFAULT_PREFIX + "." + config.getKey(), value);
            if(value instanceof Collation){
                Collection cols = (Collection) value;
                int i = 0;
                for(Object col:cols){
                    context.setAttribute(ConfigTable.EL_ATTRIBUTE_DEFAULT_PREFIX + "." + config.getKey() + "[" + i++ + "]", col);
                }
            }
            if(value instanceof Map){
                Map map = (Map)value;
                for(Object k: map.keySet()){
                    context.setAttribute(ConfigTable.EL_ATTRIBUTE_DEFAULT_PREFIX + "." + config.getKey() + "[" + k + "]", map.get(k));
                }
            }
        }
    }
    public void contextDestroyed(ServletContextEvent sce){}
}
