package org.anyline.metadata.graph;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;

public class GraphProperty extends Column {

    public GraphProperty(){
    }
    public GraphProperty(GraphType table, String name, String type){
        setTable(table);
        setName(name);
        setType(type);
    }
    public GraphProperty(String name){
        setName(name);
    }
    public GraphProperty(Schema schema, String table, String name){
        this(null, schema, table, name);
    }
    public GraphProperty(Catalog catalog, Schema schema, String table, String name){
        setCatalog(catalog);
        setSchema(schema);
        setName(name);
        setTable(table);
    }
    public GraphProperty(String name, String type, int precision, int scale){
        this.name = name;
        setType(type);
        this.precision = precision;
        this.scale = scale;
    }

    public GraphProperty(String name, String type, int precision){
        this.name = name;
        setType(type);
        this.precision = precision;
    }
    public GraphProperty(GraphType table, String name, String type, int precision, int scale){
        setTable(table);
        this.name = name;
        setType(type);
        this.precision = precision;
        this.scale = scale;
    }

    public GraphProperty(Table table, String name, String type, int precision){
        setTable(table);
        this.name = name;
        setType(type);
        this.precision = precision;
    }

    public GraphProperty(String name, String type){
        this.name = name;
        setType(type);
    }

    /**
     * 相关表
     * @param update 是否检测update
     * @return table
     */
    public GraphType getGraphType(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return (GraphType) table.getUpdate();
            }
        }
        return (GraphType)table;
    }

    public GraphType getGraphType() {
        return getGraphType(false);
    }

    public void setType(GraphType table) {
        this.table = table;
    }

    public String getGraphTypeName(boolean update) {
        GraphType type = getGraphType(update);
        if(null != type){
            return type.getName();
        }
        return null;
    }

    public String getGraphTypeName() {
        return getGraphTypeName(false);
    }

}
