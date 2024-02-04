package org.anyline.data.handler.init;

import org.anyline.data.handler.ResultSetHandler;
import org.anyline.entity.DataRow;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class SimpleResultSetHandler implements ResultSetHandler {
    private ResultSet result;
    private List<String> keys;
    private int size;

    @Override
    public boolean read(ResultSet result) {
        this.result = result;
        try {
            keys = new ArrayList<>();
            ResultSetMetaData rsmd = result.getMetaData();
            size = rsmd.getColumnCount();
            for (int i = 1; i <= size; i++) {
                keys.add(rsmd.getColumnLabel(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public LinkedHashMap<String, Object> map() throws Exception{
        LinkedHashMap<String,Object> map = null;
        if(result.next()){
            map = new LinkedHashMap<>();
            for (int i = 1; i <= size; i++) {
                map.put(keys.get(i), result.getObject(i));
            }
        }
        return map;
    }
    public DataRow row() throws Exception{
        return new DataRow(map());
    }
    public ResultSet result(){
        return result;
    }
    public void close(){
        try {
            result.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
