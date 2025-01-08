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
import org.anyline.bean.BeanDefine;
import org.anyline.util.ClassUtil;
import org.noear.solon.Solon;
import org.noear.solon.core.BeanWrap;

import java.util.Map;

public class SolonEnvironmentWorker extends DefaultEnvironmentWorker implements EnvironmentWorker {
    @Override
    public Object get(String key) {
        return Solon.cfg().get(key);
    }

    @Override
    public String getString(String key) {
        return Solon.cfg().get(key);
    }

    @Override
    public boolean destroyBean(String bean) {
        Solon.context().removeWrap(bean);
        return true;
    }

    @Override
    public Object getBean(String name) {
        return Solon.context().getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return Solon.context().getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        Object bean = Solon.context().getBean(name);
        if (null != bean && ClassUtil.isInSub(bean.getClass(), clazz)) {
            return (T) bean;
        }
        return null;
    }

    @Override
    public <T> Map<String, T> getBeans(Class<T> clazz) {
        //todo: 在 solon 里，如果没有名字的注册，无法形成 Map[Bean] 的形式
        return Solon.context().getBeansMapOfType(clazz);
    }

    public boolean regBean(String name, Object bean) {
        return reg(name, bean);
    }

    public boolean regBean(String name, BeanDefine bean) {
        return reg(name, bean);
    }

    public boolean reg(String name, Object bean) {
        Object type = bean;
        if (bean instanceof BeanDefine) {
            BeanDefine define = (BeanDefine) bean;
            if (name.endsWith("default")) {
                define.setPrimary(true);
            }

            BeanWrap bw = Solon.context().wrap(name, instance(null, define), define.isPrimary());
            //todo:包装后还要注册
            Solon.context().putWrap(name, bw);
            if (define.isPrimary()) {
                //默认形态，则再添加类型注册
                Solon.context().putWrap(define.getType(), bw);
            }
        } else if (bean instanceof Class) {
            Class clazz = (Class) bean;
            Solon.context().beanMake(clazz);
        } else {
            BeanWrap bw = Solon.context().wrap(name, bean);
            //todo:包装后还要注册
            Solon.context().putWrap(name, bw);
        }

        log.debug("[reg bean][name:{}][instance:{}]", name, type);
        return true;
    }

    public Object instance(String name, BeanDefine define) {
        Object bean = instance(define);
        if (null != name) {
            reg(name, bean);
        }
        return bean;
    }

    @Override
    public boolean containsBean(String name) {
        return null != Solon.context().getBean(name);
    }

    @Override
    public Object getSingletonBean(String name) {
        return Solon.context().getBean(name);
    }

    public void regAlias(String name, String alias) {
        Object bean = getBean(name);
        reg(alias, bean);
    }
}
