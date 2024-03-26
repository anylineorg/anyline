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

    public GraphProperty addProperty(String name, String type, int precision, int scale){
        GraphProperty property = new GraphProperty(name, type, precision, scale);
        addProperty(property);
        return property;
    }
    public GraphProperty addProperty(String name, String type, int precision){
        GraphProperty property = new GraphProperty(name, type, precision);
        addColumn(property);
        return property;
    }
    public GraphProperty addProperty(String name, String type){
        return addProperty(name, type, true, null);
    }
    public GraphProperty addProperty(String name, TypeMetadata type){
        return addProperty(name, type, true, null);
    }
    public GraphProperty addProperty(String name, String type, boolean nullable, Object def){
        GraphProperty property = new GraphProperty();
        property.setName(name);
        property.nullable(nullable);
        property.setDefaultValue(def);
        property.setTypeName(type);
        addProperty(property);
        return property;
    }
    public GraphProperty addProperty(String name, TypeMetadata type, boolean nullable, Object def){
        GraphProperty property = new GraphProperty();
        property.setName(name);
        property.nullable(nullable);
        property.setDefaultValue(def);
        property.setTypeMetadata(type);
        addProperty(property);
        return property;
    }
    public Column addColumn(String name, String type, int precision, int scale){
        return addProperty(name, type, precision, scale);
    }
    public Column addColumn(String name, String type, int precision){
        return addProperty(name, type, precision);
    }
    public Column addColumn(String name, String type){
        return addProperty(name, type);
    }
    public Column addColumn(String name, TypeMetadata type){
        return addProperty(name, type);
    }
    public Column addColumn(String name, String type, boolean nullable, Object def){
        return addProperty(name, type, nullable, def);
    }
    public Column addColumn(String name, TypeMetadata type, boolean nullable, Object def){
        return addProperty(name, type, nullable, def);
    }
    public LinkedHashMap<String, GraphProperty> getProperties(){
        LinkedHashMap<String, GraphProperty> map = new LinkedHashMap<>();
        for(Map.Entry<String, Column> entry:columns.entrySet()){
            map.put(entry.getKey(), (GraphProperty) entry.getValue());
        }
        return map;
    }
}
