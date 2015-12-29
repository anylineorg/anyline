package org.anyline.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringBeanUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static SpringBeanUtil utils = null;

	public synchronized static SpringBeanUtil init() {
		if (utils == null) {
			utils = new SpringBeanUtil();
		}
		return utils;
	}
	public synchronized void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SpringBeanUtil.applicationContext = applicationContext;
	}
	public static <T> T getBean(String beanName, Class<T> clazs) {
		return clazs.cast(getBean(beanName));
	}
	public synchronized static Object getBean(String beanName) {
		return  applicationContext.getBean(beanName);
	}
	public synchronized static Object getBean(Class<?> clazz){
		return  applicationContext.getBean(clazz);
	}
}