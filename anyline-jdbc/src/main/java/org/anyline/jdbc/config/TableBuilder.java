package org.anyline.jdbc.config;

import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.sql.auto.impl.Join;
import org.anyline.jdbc.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {

    private String table;
    private String datasource;
    private String alias;
    private List<String> columns = new ArrayList<String>();
    private List<Join> joins = new ArrayList<Join>();//关联表

    public static TableBuilder init(){
        TableBuilder builder = new TableBuilder();
        return builder;
    }
    public static TableBuilder init(String table){
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        return builder;
    }
    public static TableBuilder init(String table, String columns){
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        builder.addColumns(columns);
        return builder;
    }

    public TableBuilder setDatasource(String datasoruce){
        this.datasource = datasoruce;
        return this;
    }
    public TableBuilder setAlias(String alias){
        this.alias = alias;
        return this;
    }
    public TableBuilder setTable(String table){
        this.table = table;
        return this;
    }
    public TableBuilder addColumn(String column){
        if(!columns.contains(column)){
            columns.add(column);
        }
        return this;
    }
    public TableBuilder addColumns(String ... columns){
        if(null != columns) {
            for (String column:columns) {
                addColumn(column);
            }
        }
        return this;
    }
    public SQL build(){
        TableSQLImpl sql = new TableSQLImpl();
        sql.setDataSource(datasource);
        sql.setTable(table);
        if(BasicUtil.isNotEmpty(alias)) {
            sql.setAlias(alias);
        }
        for(Join join:joins){
            sql.join(join);
        }
        for(String col:columns) {
            sql.addColumn(col);
        }
        return sql;
    }

    public TableBuilder join(Join join){
        joins.add(join);
        return this;
    }
    public TableBuilder join(Join.TYPE type, String table, String condition){
        Join join = new Join();
        join.setName(table);
        join.setType(type);
        join.setCondition(condition);
        return join(join);
    }
    public String getTable(){
        return table;
    }
    public TableBuilder inner(String table, String condition){
        return join(Join.TYPE.INNER, table, condition);
    }
    public TableBuilder left(String table, String condition){
        return join(Join.TYPE.LEFT, table, condition);
    }
    public TableBuilder right(String table, String condition){
        return join(Join.TYPE.RIGHT, table, condition);
    }
    public TableBuilder full(String table, String condition){
        return join(Join.TYPE.FULL, table, condition);
    }

}
