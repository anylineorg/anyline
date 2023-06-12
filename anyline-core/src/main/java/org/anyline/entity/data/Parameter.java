package org.anyline.entity.data;

import org.anyline.entity.metadata.ColumnType;

public class Parameter {
    private Object value;
    private ColumnType columnType;
    private Integer type = java.sql.Types.VARCHAR;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.columnType = type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public String toString(){
        return "{value:"+value+",type:"+type+"}";
    }
}
