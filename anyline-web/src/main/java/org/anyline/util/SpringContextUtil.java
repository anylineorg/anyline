package org.anyline.util; 
 
 
import java.util.Map; 
 
import org.springframework.beans.BeansException; 
import org.springframework.context.ApplicationContext; 
import org.springframework.context.ApplicationContextAware; 
import org.springframework.stereotype.Component; 
 
@Component 
public class SpringContextUtil implements ApplicationContextAware { 
	 
	private static ApplicationContext applicationContext;  
	 
	@Override 
	public void setApplicationContext(ApplicationContext ac) throws BeansException { 
		 SpringContextUtil.applicationContext = ac;   
	} 
	 
	public static ApplicationContext getApplicationContext() {   
		return applicationContext; 
    }   
	 
    public static Object getBean(String name) throws BeansException {   
        return applicationContext.getBean(name);   
    }  
    public static <T> Map<String,T> getBeans(Class<T> clazz) throws BeansException{ 
    	return applicationContext.getBeansOfType(clazz); 
    }  
    public static <T> T getBean(Class<T> clazz) throws BeansException{ 
    	return applicationContext.getBean(clazz); 
    }  
} 
