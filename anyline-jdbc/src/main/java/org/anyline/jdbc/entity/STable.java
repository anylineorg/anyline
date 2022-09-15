package org.anyline.jdbc.entity;


import org.anyline.entity.DataRow;

import java.util.LinkedHashMap;

public class STable extends Table {
    protected String keyword = "STABLE"            ;
    private LinkedHashMap<String,Table> tables     ; //子表

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
