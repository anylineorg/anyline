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



package org.anyline.data.datasource;

import java.util.*;

public class DataSourceKeyMap {
    public static Map<String, HashSet<String>> maps = new HashMap<>();
    static {
        reg("username","userName","user","user-name");
        reg("url","jdbcUrl","uri","uris","host","hosts");
        reg("driverClass","driverClassName","driver-class","driver-class-name");//,"driver" 表示一个对象
        reg("IdleTimeout","idleTimeoutMs","idle-timeout","idle-timeout-ms");
        reg("maxLifetime","maxLifetimeMs","max-lifetime","max-lifetime-ms");
        reg("maxPoolSize","maximumPoolSize","max-pool-size","maximum-pool-size");
        reg("validationTimeout","validationTimeoutMs","validation-timeout","validation-timeout-ms");
        reg("validationTimeout","validationTimeoutMs","validation-timeout","validation-timeout-ms");
        reg("transactionIsolationName","transactionIsolation","transaction-isolation-name","transaction-isolation");
    }
    public static HashSet<String> alias(String key) {
        return maps.get(key);
    }
    public static void reg(String ... keys) {
        for(String key:keys) {
            HashSet<String> list = maps.get(key);
            if(null == list) {
                list = new HashSet<>();
                maps.put(key, list);
            }
            for (String item:keys) {
                if(!key.equals(item) && !list.contains(item)) {
                    list.add(item);
                }
            }
        }
    }

}
