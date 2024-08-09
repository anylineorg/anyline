/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.environment.spring;

import org.anyline.adapter.EnvironmentWorker;
import org.anyline.adapter.init.DefaultEnvironmentWorker;
import org.anyline.bean.BeanDefine;
import org.anyline.bean.ValueReference;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Component("anyline.environment.worker.spring")
public class SpringEnvironmentWorker extends DefaultEnvironmentWorker implements EnvironmentAware, EnvironmentWorker, ApplicationContextAware {
    private static Logger log = LoggerFactory.getLogger(SpringEnvironmentWorker.class);
    private Environment environment;
    private static DefaultListableBeanFactory factory;
    public ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
        factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        ConfigTable.setEnvironment(this);
    }

    @Override
    public void setEnvironment(Environment environment) {
        ConfigTable.setEnvironment(this);
        this.environment = environment;
        Field[] fields = ConfigTable.class.getDeclaredFields();
        for(Field field:fields) {
            String name = field.getName();
            String value = string("anyline",  "." + name);
            if(BasicUtil.isNotEmpty(value)) {
                if(Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                BeanUtil.setFieldValue(null, field, value);
            }
        }
    }

    public Object getBean(String name) {
        if(context.containsBean(name)) {
            return context.getBean(name);
        }else{
            return null;
        }
    }
    public <T> Map<String, T> getBeans(Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }
    private static BeanDefinition convert(BeanDefine define) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(define.getType());
        LinkedHashMap<String, Object> values = define.getValues();
        for(String key:values.keySet()) {
            Object value = values.get(key);
            if(value instanceof ValueReference) {
                builder.addPropertyReference(key, ((ValueReference)value).getName());
            }else{
                builder.addPropertyValue(key, value);
            }
        }
        builder.setPrimary(define.isPrimary());
        builder.setLazyInit(define.isLazy());
        return builder.getBeanDefinition();
    }
    public boolean regBean(String name, BeanDefine bean) {
        return reg(name, bean);
    }
    public boolean regBean(String name, Object bean) {
        return reg(name, bean);
    }
    public boolean reg(String name, Object bean) {
        if(bean instanceof BeanDefine) {
            BeanDefine define = (BeanDefine)bean;
            if(name.endsWith("default")) {
                define.setPrimary(true);
            }
            factory.registerBeanDefinition(name, convert(define));
        }else if(bean instanceof BeanDefinition) {
            BeanDefinition definition = (BeanDefinition)bean;
            if(name.endsWith("default")) {
                definition.setPrimary(true);
            }
            factory.registerBeanDefinition(name, definition);
        }else if(bean instanceof Class) {
            Class clazz = (Class) bean;
            try {
                factory.registerSingleton(name, clazz.newInstance());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            factory.registerSingleton(name, bean);
        }
        return true;
    }
    public boolean destroyBean(String bean) {
        if(factory.containsBean(bean)) {
            factory.destroySingleton(bean);
        }
        if(factory.containsBeanDefinition(bean)) {
            factory.removeBeanDefinition(bean);
        }
        return true;
    }
    public <T> T getBean(Class<T> clazz) {
        Map<String, T> beans = getBeans(clazz);
        if(null != beans && !beans.isEmpty()) {
            for(Map.Entry<String, T> set:beans.entrySet()) {
                T bean = set.getValue();
                if(null != bean) {
                    return bean;
                }
            }
        }
        return null;
    }
    public <T> T getBean(String name, Class<T> clazz) {
        return factory.getBean(name, clazz);
    }

    public boolean containsBean(String name) {
        return factory.containsBean(name);
    }
    public Object getSingletonBean(String name) {
        return factory.getSingleton(name);
    }

    public boolean containsSingleton(String name) {
        return factory.containsSingleton(name);
    }
    public <T> T getSingletonBean(String name, Class<T> clazz) {
        return (T) factory.getSingleton(name);
    }

    @Override
    public Object get(String key) {
        Object val = environment.getProperty(key);
        if(null == val) {
            if(key.startsWith("anyline.")) {
                key = key.replace("anyline.","");
            }else{
                key = "anyline." + key;
            }
            val = environment.getProperty(key);
        }
        return val;
    }
    public String getString(String key) {
        return environment.getProperty(key);
    }

    /**
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param id bean id
     * @param prefix 配置文件前缀 如 anyline.datasource.sso
     * @param params map格式参数
     * @return bean.id
     * @throws Exception Exception
     */
    public Map<String, Object> inject(String id, String prefix, Map params, Map<String, HashSet<String>> alias, Class clazz) throws Exception {
        Map<String, Object> cache = new HashMap<>();
        BeanDefinitionBuilder ds_builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        //List<Field> fields = ClassUtil.getFields(poolClass, false, false);
        List<Method> methods = ClassUtil.getMethods(clazz, true);
        for(Method method:methods) {
            if (method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers())) {
                String name = method.getName();
                // public void setMaximumPoolSize(int maxPoolSize) {this.maxPoolSize = maxPoolSize;}
                if(name.startsWith("set")) {
                    //根据方法名
                    name = name.substring(3, 4).toLowerCase() + name.substring(4);
                    Class paramType = method.getParameters()[0].getType();
                    Object value = BeanUtil.value(params, name, alias, paramType, null);
                    if(null == value) {
                        value = value(prefix, name, alias, paramType, null);
                    }
                    if(null != value) {
                        cache.put(name, value);
                        ds_builder.addPropertyValue(name, value);

                    }
                }
            }
        }
        BeanDefinition ds_definition = ds_builder.getBeanDefinition();
        regBean(id, ds_definition);
        log.info("[inject bean][type:{}][bean:{}]", clazz, id);

        return cache;
    }

    /**
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param id bean id
     * @param params map格式参数
     * @return bean.id
     * @throws Exception Exception
     */
    public Map<String, Object> inject(String id, Map params, Class clazz) throws Exception {
        Map<String, Object> cache = new HashMap<>();
        BeanDefinitionBuilder ds_builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        List<Method> methods = ClassUtil.getMethods(clazz, true);
        for(Method method:methods) {
            if (method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers())) {
                String name = method.getName();
                if(name.startsWith("set")) {
                    //根据方法名
                    name = name.substring(3, 4).toLowerCase() + name.substring(4);
                    Object value = BeanUtil.value(params, name, null, Object.class, null);
                    if(null != value) {
                        Class tp = method.getParameters()[0].getType();
                        value = ConvertProxy.convert(value, tp, false);
                        if (null != value) {
                            cache.put(name, value);
                            ds_builder.addPropertyValue(name, value);
                        }
                    }
                }
            }
        }
        BeanDefinition ds_definition = ds_builder.getBeanDefinition();
        regBean(id, ds_definition);
        log.info("[inject bean][type:{}][bean:{}]", clazz, id);

        return cache;
    }
}
