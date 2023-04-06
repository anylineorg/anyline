package org.anyline.util;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Map.Entry;


@Component
public class SpringContextUtil implements ApplicationContextAware {
	 
	public static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext ac) throws BeansException { 
		SpringContextUtil.applicationContext = ac;
		ConfigTable.setEnvironment(ac.getEnvironment());
	}

	 
	public static ApplicationContext getApplicationContext() {   
		return applicationContext; 
    }   
	 
    public static Object getBean(String name) throws BeansException {
		if(applicationContext.containsBean(name)) {
			return applicationContext.getBean(name);
		}else{
			return null;
		}
    }  
    public static <T> Map<String,T> getBeans(Class<T> clazz) throws BeansException{ 
    	return applicationContext.getBeansOfType(clazz); 
    }  
    public static <T> T getBean(Class<T> clazz) throws BeansException{ 
    	Map<String,T> beans = getBeans(clazz);
    	if(null != beans && !beans.isEmpty()){
    		for(Entry<String,T> set:beans.entrySet()){
    			T bean = set.getValue();
    			if(null != bean){
    				return bean;
    			}
    		}
    	}
    	return null;
    }

} 
