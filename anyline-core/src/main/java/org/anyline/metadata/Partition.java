package org.anyline.metadata;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Partition  implements Serializable {
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
