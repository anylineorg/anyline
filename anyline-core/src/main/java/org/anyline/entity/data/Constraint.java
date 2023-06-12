package org.anyline.entity.data;

import java.util.LinkedHashMap;

public class Constraint {

    private String catalog      ;
    private String schema       ;
    private String tableName    ;
    private Table table         ;
    private String name         ;
    private boolean unique      ; // 是否唯一
    private String type         ; //
    protected String comment    ; // 备注
    private LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
    private Index update;
    public Constraint(){
    }

    public String getCatalog() {
        return catalog;
    }

    public Constraint setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public Constraint setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getTableName() {
        if(null != table){
            return table.getName();
        }
        return table.getName();
    }

    public Table getTable() {
        if(null == table && null != tableName){
            return new Table(tableName);
        }
        return table;
    }

    public Constraint setTable(Table table) {
        this.table = table;
        return this;
    }
    public Constraint setTable(String table) {
        this.table = new Table(table);
        return this;
    }

    public Constraint setTableName(String tableName) {
        this.tableName = tableName;
        this.table = new Table(tableName);
        return this;
    }

    public String getName() {
        return name;
    }

    public Constraint setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public Constraint setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public String getType() {
        return type;
    }

    public Constraint setType(String type) {
        this.type = type;
        return this;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }
    public Column getColumn(String name) {
        if(null != columns && null != name){
            return columns.get(name.toUpperCase());
        }
        return null;
    }

    public Constraint setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
        return this;
    }
    public Constraint addColumn(Column column){
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);
        return this;
    }

    public Constraint addColumn(String column){
        return addColumn(new Column(column));
    }

    public Constraint addColumn(String column, String order){
        return addColumn(new Column(column).setOrder(order));
    }
    public Constraint addColumn(String column, String order, int position){
        return addColumn(new Column(column).setOrder(order).setPosition(position));
    }


    public Index getUpdate() {
        return update;
    }

    public Constraint setUpdate(Index update) {
        this.update = update;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
