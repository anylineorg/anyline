package org.anyline.data.jdbc.handler;

import org.anyline.data.handler.ConnectionHandler;
import org.anyline.data.handler.ResultSetHandler;
import org.anyline.entity.DataRow;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleResultSetHandler implements ResultSetHandler {
    private ConnectionHandler handler;
    private ResultSet result;
    private List<String> keys;
    /**
     * 批量数量
     */
    private int size;
    /**
     * 结果集多少列
     */
    private int vol;

    public SimpleResultSetHandler(){}
    public SimpleResultSetHandler(int size){
        this.size = size;
    }

    public int size(){
        return size;
    }
    @Override
    public void handler(ConnectionHandler handler) {
        this.handler = handler;
    }
    @Override
    public boolean keep() {
       return true;
    }

    @Override
    public boolean read(ResultSet result) {
        this.result = result;
        try {
            keys = new ArrayList<>();
            ResultSetMetaData rsmd = result.getMetaData();
            vol = rsmd.getColumnCount();
            for (int i = 1; i <= vol; i++) {
                keys.add(rsmd.getColumnLabel(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public LinkedHashMap<String, Object> map() throws Exception{
        LinkedHashMap<String,Object> map = null;
        if(null != result && !result.isClosed()) {
            if (result.next()) {
                map = new LinkedHashMap<>();
                for (int i = 1; i <= vol; i++) {
                    map.put(keys.get(i - 1), result.getObject(i));
                }
            } else {
                handler.close();
            }
        }
        return map;
    }
    public DataRow row() throws Exception{
        Map<String, Object> map = map();
        if(null != map) {
            return new DataRow(map);
        }
        return null;
    }
    public ResultSet result(){
        return result;
    }
    public void close() throws Exception{
        handler.close();
        result = null;
    }
}
