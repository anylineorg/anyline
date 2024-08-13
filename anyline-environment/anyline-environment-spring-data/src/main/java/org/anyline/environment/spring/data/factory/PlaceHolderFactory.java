package org.anyline.environment.spring.data.factory;

import org.anyline.data.runtime.DataRuntime;
import org.anyline.service.init.DefaultService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class PlaceHolderFactory implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        //注入占位 解决@Lazy注入方式
        factory.registerSingleton(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + "default", new DefaultService<>());
    }
}
