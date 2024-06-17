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



package org.anyline.adapter;

import org.anyline.bean.BeanDefine;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.BasicUtil;

import java.util.HashSet;
import java.util.Map;

public interface EnvironmentWorker {
    /**
     * 从配置文件中获取值
     * @param key key
     * @return Object
     */
    Object get(String key);
    String getString(String key);

    /**
     * 根据配置文件提取指定key的值
     * @param prefix 前缀 多个以,分隔
     * @param key 多个以,分隔 第一个有值的key生效
     * @return String
     */
    String string(String prefix, String key);

    boolean destroyBean(String bean);
    Object getBean(String name);
    <T> T getBean(Class<T> clazz);
    <T> T getBean(String name, Class<T> clazz);
    <T> Map<String, T> getBeans(Class<T> clazz);
    Object instance(BeanDefine define);
    boolean regBean(String name, Object bean);
    boolean regBean(String name, BeanDefine bean);
    boolean containsBean(String name);
    boolean containsSingleton(String name);
    <T> T getSingletonBean(String name, Class<T> clazz);
    Object getSingletonBean(String name);



    default  <T> T value(Map<String, HashSet<String>>  aliasMap, String prefix, String key, Class<T> clazz, T def) {
        if(null != prefix && null != key) {
            String ps[] = prefix.split(",");
            String ks[] = key.split(",");
            for(String p:ps) {
                for (String k : ks) {
                    String value = string(p, k);
                    if (null == value && null != aliasMap) {
                        HashSet<String> alias = aliasMap.get(k);
                        if (null != alias) {
                            for (String item : alias) {
                                if (null == value) {
                                    value = string(p, item);
                                }
                                if (BasicUtil.isNotEmpty(value)) {
                                    break;
                                }
                            }
                        }
                    }
                    if (BasicUtil.isNotEmpty(value)) {
                        return (T) ConvertProxy.convert(value, clazz, false);
                    }
                }
            }
        }
        return def;
    }
    /**
     * 从配置文件中取值
     * @param prefix 前缀 如果有多个用,分隔如如spring.datasource,anyline.datasource
     * @param key 如果有多个用,分隔如driver,driver-class
     * @param clazz 返回数据类型
     * @param def 默认值
     * @return T
     * @param <T> T
     */
    default  <T> T value(String prefix, String key, Map<String, HashSet<String>> alias, Class<T> clazz, T def) {
        if(null != prefix && null != key) {
            String ps[] = prefix.split(",");
            String ks[] = key.split(",");
            for(String p:ps) {
                for (String k : ks) {
                    String value = string(p, k);
                    if (null == value && null != alias) {
                        HashSet<String> aliasList = alias.get(k);
                        if (null != aliasList) {
                            for (String item : aliasList) {
                                value = string(p, item);
                                if (BasicUtil.isNotEmpty(value)) {
                                    break;
                                }
                            }
                        }
                    }
                    if (BasicUtil.isNotEmpty(value)) {
                        return (T) ConvertProxy.convert(value, clazz, false);
                    }
                }
            }
        }
        return def;
    }
    default Object value(String prefix, String keys, Map<String, HashSet<String>> alias) {
        return value(prefix, keys, alias, Object.class, null);
    }
    /**
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param id bean id
     * @param prefix 配置文件前缀 如 anyline.datasource.sso
     * @param params map格式参数
     * @return bean所有赋值的的filed value
     * @throws Exception Exception
     */
    Map<String, Object> inject(String id, String prefix, Map params, Map<String, HashSet<String>> alias, Class clazz) throws Exception;

    /**
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param id bean id
     * @param params map格式参数
     * @return bean所有赋值的的filed value
     * @throws Exception Exception
     */
    Map<String, Object> inject(String id, Map params,  Class clazz) throws Exception;

}
