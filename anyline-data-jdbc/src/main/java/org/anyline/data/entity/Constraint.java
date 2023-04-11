package org.anyline.data.entity;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.service.AnylineService;

import java.util.LinkedHashMap;

public class Constraint {

    private String catalog      ;
    private String schema       ;
    private String tableName    ;
    private Table table         ;
    private String name         ;
    private boolean unique      ; // 是否唯一
    private Integer type        ; //
    private LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
    private Index update;
    private transient DDListener listener ;


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

    public Constraint setTableName(String tableName) {
        this.tableName = tableName;
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

    public int getType() {
        return type;
    }

    public Constraint setType(Integer type) {
        this.type = type;
        return this;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
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

    public Index getUpdate() {
        return update;
    }

    public Constraint setUpdate(Index update) {
        this.update = update;
        return this;
    }

    public DDListener getListener() {
        return listener;
    }

    public Constraint setListener(DDListener listener) {
        this.listener = listener;
        return this;
    }
    public Constraint setService(AnylineService service){
        if(null != listener){
            listener.setService(service);
        }
        return this;
    }
    public Constraint setCreater(JDBCAdapter adapter) {
        if (null != listener) {
            listener.setAdapter(adapter);
        }
        return this;
    }
}
