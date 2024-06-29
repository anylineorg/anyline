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



package org.anyline.environment.spring.data.jdbc;

import org.anyline.data.runtime.DataRuntime;
import org.anyline.service.init.DefaultService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component("anyline.environment.worker.spring.jdbc")
public class SpringJDBCEnvironmentWorker implements ApplicationListener<ContextStartedEvent> {

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) event.getApplicationContext().getAutowireCapableBeanFactory();
        //在这一步先注册service context加载完成后再补充 因为有些项目会提前注入service 如果不提交注册需要@Lazy
        //后注册的主要是为了等项目中默认的数据源加载完成后，用默认数据源注册默认service
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + "default";
        if (!factory.containsBeanDefinition(service_key) && factory.containsBean(service_key)) {
            factory.registerSingleton(service_key, new DefaultService<>());
        }
    }
}

