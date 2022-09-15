package org.anyline.jdbc.entity;


import org.anyline.entity.DataRow;
import org.anyline.listener.impl.DefaulDDtListener;

import java.util.LinkedHashMap;

public class STable extends Table {
    protected String keyword = "STABLE"            ;
    private LinkedHashMap<String,Table> tables     ; //子表

    public STable(){
        this.listener = new DefaulDDtListener();
    }
    public STable(String name){
        this(null, name);
    }
    public STable(String schema, String table){
        this(null, schema, table);
    }
    public STable(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.listener = new DefaulDDtListener();
    }


    public String getKeyword() {
        return this.keyword;
    }

    public LinkedHashMap<String, Table> getTables() {
        return tables;
    }

    public void setTables(LinkedHashMap<String, Table> tables) {
        this.tables = tables;
    }

    /**
     * 根据值定位子表
     * @param value value
     * @return Table
     */
    public Table getTable(DataRow value){
        Table table = null;
        return table;
    }
    /**
     * 根据标签定位子表
     * @param tags tags
     * @return Table
     */
    public Table getTable(Tag ... tags){
        Table table = null;
        return table;
    }
}
