package org.anyline.jdbc.config;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private String datasoruce;
    private String schema;
    private String name;
    private List<String> columns = new ArrayList<String>();
    private TYPE type = TYPE.TABLE;
    private List<Join> joins = new ArrayList<Join>();//关联表

    public static enum TYPE{
        TABLE			{public String getCode(){return "TABLE";} 	public String getName(){return "表视图";}},
        XML				{public String getCode(){return "XML";} 	public String getName(){return "XML定义SQL";}},
        TEXT			{public String getCode(){return "TEXT";} 	public String getName(){return "原生SQL";}};
        public abstract String getName();
        public abstract String getCode();
    }

    public static void main(String[] args) {
        Table table = new Table("<ds>dbo.user(id,nm)","id,nm,ss");
    }
    public Table(String name, String columns){
        this.name = name;
        parseName();
        parseColumn(columns);
    }
    private void parseName(){
        if(null != name){
            if(name.startsWith("<")){
                datasoruce = name.substring(1,name.indexOf(">"));
                name = name.substring(name.indexOf(">")+1);
            }
            if(null != name && name.contains(".")){
                String[] tbs = name.split("\\.");
                name = tbs[1];
                schema = tbs[0];
            }
            if(name.contains("(")){
                String[] cols = name.substring(name.indexOf("(")+1, name.indexOf(")")).split(",");
                name = name.substring(0,name.indexOf("("));
                for(String col:cols){
                    if(!columns.contains(col)) {
                        columns.add(col);
                    }
                }
            }
        }
    }
    public Table join(Join join){
        joins.add(join);
        return this;
    }
    public Table join(Join.TYPE type, String table, String condition){
        Join join = new Join();
        join.setName(table);
        join.setType(type);
        join.setCondition(condition);
        joins.add(join);
        return this;
    }
    private void parseColumn(String columns){
        if(null != columns){
            String[] cols = columns.split(",");
            for(String col:cols){
                if(!this.columns.contains(col)) {
                    this.columns.add(col);
                }
            }
        }
    }

    public String getDatasoruce() {
        return datasoruce;
    }

    public void setDatasoruce(String datasoruce) {
        this.datasoruce = datasoruce;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }
}
