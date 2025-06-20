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

package org.anyline.data.datasource;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ApplicationConnectionHolder {
    protected static final Log log = LogProxy.get(ApplicationConnectionHolder.class);

    /**
     * 整个应用内有效
     */
    private static final Map<DataSource, Map<String, Connection>> connections = new HashMap<>();
    public static Connection get(DataSource ds, String name) {
        Map<String, Connection> map = connections.get(ds);
        if(null != map) {
            Connection connection = map.get(name);
            try {
                if(null != connection) {
                    log.info("[获取跨线程事务连接][name:{}]", name);
                    if (connection.isClosed()) {
                        map.remove(name);
                        connection = null;
                        log.info("[跨线程事务连接异常关闭]");
                    }
                }
                return connection;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static void set(DataSource ds, String name, Connection con) {
        Map<String, Connection> map = connections.get(ds);
        if(null == map) {
            map = new HashMap<>();
            connections.put(ds, map);
        }
        map.put(name, con);
    }
    public static void remove(DataSource ds, String name) {
        Map<String, Connection> map = connections.get(ds);
        if(null != map) {
            map.remove(name);
        }
    }
    public static boolean contains(DataSource ds, Connection connection) {
        Map<String, Connection> map = connections.get(ds);
        if(null != map && map.containsValue(connection)) {
            return true;
        }
        return false;
    }
}
