/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.bean.init;

import org.anyline.bean.BeanDefine;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.util.LinkedHashMap;

public class DefaultBeanDefine implements BeanDefine {
    private static final Log log = LogProxy.get(DefaultBeanDefine.class);
    public DefaultBeanDefine() {}
    public DefaultBeanDefine(Class type) {
        this.type = type;
    }
    public DefaultBeanDefine(String type) {
        this.typeName = type;
    }
    public DefaultBeanDefine(Class type, boolean lazy) {
        this.type = type;
        this.lazy = lazy;
    }
    public DefaultBeanDefine(String type, boolean lazy) {
        this.typeName = type.trim();
        this.lazy = lazy;
    }
    private String typeName;
    private Class type;
    private boolean lazy = true;
    private boolean primary = false;
    private LinkedHashMap<String, Object> values = new LinkedHashMap();

    public String getTypeName() {
        if(null == typeName && null != type) {
            typeName = type.getName();
        }
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName.trim();
    }

    @Override
    public Class getType() {
        if(null == type && null != typeName) {
            try {
                type = Class.forName(typeName);
            }catch (Exception e) {
                log.error("类型异常", e);
            }
        }
        return type;
    }

    @Override
    public BeanDefine setType(Class type) {
        this.type = type;
        return this;
    }

    public boolean isPrimary() {
        return primary;
    }

    public BeanDefine setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }
    public boolean isLazy() {
        return lazy;
    }

    public BeanDefine setLazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public LinkedHashMap<String, Object> getValues() {
        return values;
    }

    public BeanDefine setValues(LinkedHashMap<String, Object> values) {
        this.values = values;
        return this;
    }

    @Override
    public BeanDefine addValue(String name, Object value) {
        values.put(name, value);
        return this;
    }

    @Override
    public BeanDefine addReferenceValue(String name, String value) {
        values.put(name, new DefaultValueReference(value));
        return this;
    }
}
