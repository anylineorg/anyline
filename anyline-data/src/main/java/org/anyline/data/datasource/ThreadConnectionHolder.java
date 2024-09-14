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

import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ThreadConnectionHolder {
    protected static Log log = LogProxy.get(ThreadConnectionHolder.class);
    /**
     * 线程内有效
     */
    private static final ThreadLocal<Map<DataSource, Connection>> connections = new ThreadLocal<>();
    public static Connection get(DataSource ds) {
        Connection con = null;
        Map<DataSource, Connection> cons = connections.get();
        if(null != cons) {
            con = cons.get(ds);
        }
        if(null != con) {
            try {
                log.info("[获取线程内事务连接]");
                if (con.isClosed()) {
                    con = null;
                    log.info("[线程内事务连接异常关闭]");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return con;
    }
    public static void set(DataSource ds, Connection con) {
        Map<DataSource, Connection> cons = connections.get();
        if(null == cons) {
            cons = new HashMap<>();
        }
        cons.put(ds, con);
        connections.set(cons);
    }
    public static void remove(DataSource ds) {
        Map<DataSource, Connection> cons = connections.get();
        if(null != cons) {
            cons.remove(ds);
        }
    }
    public static boolean contains(DataSource ds, Connection connection) {
        Map<DataSource, Connection> cons = connections.get();
        if(null != cons) {
            return connection == cons.get(ds);
        }
        return false;
    }
}
