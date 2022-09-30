package org.anyline.data.entity;

import java.util.LinkedHashMap;

public class Partition {
    private String type;
    private LinkedHashMap<String, Column> columns;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
    }
}
