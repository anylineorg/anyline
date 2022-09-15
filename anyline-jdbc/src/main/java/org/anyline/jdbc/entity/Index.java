package org.anyline.jdbc.entity;

import org.anyline.jdbc.config.db.SQLAdapter;
import org.anyline.listener.DDListener;
import org.anyline.service.AnylineService;

import java.util.LinkedHashMap;

public class Index {
    private String catalog      ;
    private String schema       ;
    private String table        ;
    private String name         ;
    private boolean unique      ; //是否唯一
    private boolean cluster     ; //是否聚簇索引
    private Integer type        ; //
    private LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
    private Index update;
    private DDListener listener ;


    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public int getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
    }

    public Index getUpdate() {
        return update;
    }

    public void setUpdate(Index update) {
        this.update = update;
    }

    public DDListener getListener() {
        return listener;
    }

    public Index setListener(DDListener listener) {
        this.listener = listener;
        return this;
    }
    public Index setService(AnylineService service){
        if(null != listener){
            listener.setService(service);
        }
        return this;
    }
    public Index setCreater(SQLAdapter adapter) {
        if (null != listener) {
            listener.setAdapter(adapter);
        }
        return this;
    }
}