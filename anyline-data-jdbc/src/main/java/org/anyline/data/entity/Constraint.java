package org.anyline.data.entity;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.init.DefaultDDListener;
import org.anyline.data.util.ThreadConfig;
import org.anyline.service.AnylineService;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;

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
    public Constraint(){
        DDListener listener = null;
        ConfigTable config = ThreadConfig.check(DataSourceHolder.curDataSource());
        if(config instanceof ThreadConfig){
            listener = ((ThreadConfig)config).getDdListener();
        }
        if(null == listener) {
            listener = SpringContextUtil.getBean(DDListener.class);
        }
        if(null == listener){
            listener = new DefaultDDListener();
        }
        this.listener = listener;
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
