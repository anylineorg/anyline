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


package org.anyline.listener;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Component("anyline.listener.EnvironmentListener")
public class EnvironmentListener implements EnvironmentAware {
    private Logger log = LoggerFactory.getLogger(EnvironmentListener.class);

    @Override
    public void setEnvironment(Environment environment) {
        Field[] fields = ConfigTable.class.getDeclaredFields();
        for(Field field:fields){
            String name = field.getName();
            String value = BeanUtil.value("anyline", environment, "." + name);
            if(BasicUtil.isNotEmpty(value)) {
                if(Modifier.isFinal(field.getModifiers())){
                    continue;
                }
                BeanUtil.setFieldValue(null,  field,  value);
            }
        }
    }
}
