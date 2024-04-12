package org.anyline.data.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ThreadConnectionHolder {
    /**
     * 线程内有效
     */
    private static final ThreadLocal<Map<DataSource, Connection>> connections = new ThreadLocal<>();
    public static Connection get(DataSource ds){
        Map<DataSource, Connection> cons = connections.get();
        if(null != cons){
            return cons.get(ds);
        }
        return null;
    }
    public static void set(DataSource ds, Connection con){
        Map<DataSource, Connection> cons = connections.get();
        if(null == cons){
            cons = new HashMap<>();
        }
        cons.put(ds, con);
        connections.set(cons);
    }
    public static void remove(DataSource ds){
        Map<DataSource, Connection> cons = connections.get();
        if(null != cons){
            cons.remove(ds);
        }
    }
}
