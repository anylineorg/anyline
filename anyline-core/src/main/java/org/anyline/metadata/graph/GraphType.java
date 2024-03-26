package org.anyline.metadata.graph;

import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphType extends Table<GraphType> implements Serializable {

    public GraphType addProperty(GraphProperty property){
        if(setmap && null != update){
            update.addColumn(property);
            return this;
        }
        property.setTable(this);
        if (null == columns) {
            columns = new LinkedHashMap<>();
        }
        columns.put(property.getName().toUpperCase(), property);

        return this;
    }

    public Column addColumn(String name, String type, int precision, int scale){
        throw new RuntimeException("请调用addProperty");
    }
    public Column addColumn(String name, String type, int precision){
        throw new RuntimeException("请调用addProperty");
    }
    public Column addColumn(String name, String type){
        throw new RuntimeException("请调用addProperty");
    }
    public Column addColumn(String name, TypeMetadata type){
        throw new RuntimeException("请调用addProperty");
    }
    public Column addColumn(String name, String type, boolean nullable, Object def){
        throw new RuntimeException("请调用addProperty");
    }
    public Column addColumn(String name, TypeMetadata type, boolean nullable, Object def){
        throw new RuntimeException("请调用addProperty");
    }
    public LinkedHashMap<String, GraphProperty> getProperties(){
        LinkedHashMap<String, GraphProperty> map = new LinkedHashMap<>();
        for(Map.Entry<String, Column> entry:columns.entrySet()){
            map.put(entry.getKey(), (GraphProperty) entry.getValue());
        }
        return map;
    }
}
