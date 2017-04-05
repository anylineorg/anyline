package org.anyline.util;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext; 
	
	@Override
	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		 SpringContextUtil.applicationContext = ac;  
	}
	
	public static ApplicationContext getApplicationContext() {  
		return applicationContext;
    }  
	
    public static Object getBean(String name) throws BeansException {  
        return applicationContext.getBean(name);  
    }  
}
