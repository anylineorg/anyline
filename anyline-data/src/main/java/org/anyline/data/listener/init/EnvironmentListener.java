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


package org.anyline.data.listener.init;

import org.anyline.data.datasource.DatasourceHolder;
import org.anyline.data.listener.DatasourceLoader;
import org.anyline.data.runtime.RuntimeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.listener.data.environment")
public class EnvironmentListener implements ApplicationContextAware {
    public static Logger log = LoggerFactory.getLogger(EnvironmentListener.class);
    private static Map<String, DatasourceLoader> loaders = new HashMap<>();
    @Autowired(required = false)
    public void setAdapters(Map<String, DatasourceLoader> map){
        loaders = map;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        DatasourceHolder.init(factory);
        RuntimeHolder.init(factory);
        for(DatasourceLoader loader:loaders.values()){
            loader.load(applicationContext);
        }
    }
}
