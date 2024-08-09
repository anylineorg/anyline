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

package org.anyline.environment.solon;

import org.anyline.adapter.EnvironmentWorker;
import org.anyline.adapter.init.DefaultEnvironmentWorker;
import org.anyline.annotation.Component;
import org.anyline.bean.BeanDefine;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.noear.solon.Solon;
import org.noear.solon.SolonProps;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.event.AppLoadEndEvent;

import java.util.Map;

@Component
public class SolonEnvironmentWorker extends DefaultEnvironmentWorker implements EnvironmentWorker, Plugin {
    private static AppContext context;
    private static SolonProps props;
    public SolonEnvironmentWorker() {}

    @Override
    public void start(AppContext context) throws Throwable {
        log.debug("solon environment start");
        SolonEnvironmentWorker.context = context;
        props = Solon.cfg();
        ConfigTable.environment = this;
        DefaultEnvironmentWorker.start();
        context.onEvent(AppLoadEndEvent.class, e->{
            log.debug("solon end event");
            SolonAutoConfiguration.init();
        });
    }

    public SolonEnvironmentWorker(AppContext context) {
        SolonEnvironmentWorker.context = context;
        props = Solon.cfg();
    }

    public static void setContext(AppContext context) {
        SolonEnvironmentWorker.context = context;
        props = Solon.cfg();
        DefaultEnvironmentWorker.start();
    }

    @Override
    public Object get(String key) {
        return props.get(key);
    }

    @Override
    public String getString(String key) {
        return props.get(key);
    }

    @Override
    public boolean destroyBean(String bean) {
        context.removeWrap(bean);
        return true;
    }

    @Override
    public Object getBean(String name) {
        return context.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        Object bean = context.getBean(name);
        if(null != bean && ClassUtil.isInSub(bean.getClass(), clazz)) {
            return (T) bean;
        }
        return null;
    }

    @Override
    public <T> Map<String, T> getBeans(Class<T> clazz) {
        return context.getBeansMapOfType(clazz);
    }

    public boolean regBean(String name, Object bean) {
        return reg(name, bean);
    }
    public boolean regBean(String name, BeanDefine bean) {
        return reg(name, bean);
    }
    public boolean reg(String name, Object bean) {
        Object type = bean;
        if(bean instanceof BeanDefine) {
            BeanDefine define = (BeanDefine)bean;
            if(name.endsWith("default")) {
                define.setPrimary(true);
            }
            context.wrap(name, instance(null, define));
        }else if(bean instanceof Class) {
            Class clazz = (Class) bean;
            context.beanMake(clazz);
        }else {
            context.wrap(name, bean);
        }
        log.debug("[reg bean][name:{}][instance:{}]", name, type);
        return true;
    }
    public Object instance(String name, BeanDefine define) {
        Object bean = instance(define);
        if(null != name) {
            reg(name, bean);
        }
        return bean;
    }

    @Override
    public boolean containsBean(String name) {
        return null != context.getBean(name);
    }

    @Override
    public Object getSingletonBean(String name) {
        return context.getBean(name);
    }

}
