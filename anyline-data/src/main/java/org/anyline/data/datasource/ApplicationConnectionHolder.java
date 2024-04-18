package org.anyline.data.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ApplicationConnectionHolder {
    /**
     * 整个内有效
     */
    private static final Map<DataSource, Map<String, Connection>> connections = new HashMap<>();
    public static Connection get(DataSource ds, String name){
        Map<String, Connection> map = connections.get(ds);
        if(null != map){
            Connection connection = map.get(name);
            try {
                if(null != connection){
                    if (connection.isClosed()) {
                        map.remove(name);
                        connection = null;
                    }
                }
                return connection;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static void set(DataSource ds, String name, Connection con){
        Map<String, Connection> map = connections.get(ds);
        if(null == map){
            map = new HashMap<>();
            connections.put(ds, map);
        }
        map.put(name, con);
    }
    public static void remove(DataSource ds, String name){
        Map<String, Connection> map = connections.get(ds);
        if(null != map){
            map.remove(name);
        }
    }
    public static boolean contains(DataSource ds, Connection connection){
        Map<String, Connection> map = connections.get(ds);
        if(null != map && map.containsValue(connection)){
            return true;
        }
        return false;
    }
}
