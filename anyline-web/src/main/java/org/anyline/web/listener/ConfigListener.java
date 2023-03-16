package org.anyline.web.listener;

import org.anyline.util.ConfigTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

@Component
public class ConfigListener implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        if(ac instanceof WebApplicationContext){
            WebApplicationContext servlet = (WebApplicationContext)ac;
            ServletContext context = servlet.getServletContext();
            context.setAttribute(ConfigTable.EL_ATTRIBUTE_PREFIX, ConfigTable.getConfigs());
        }
    }
}
