package org.anyline.entity.data;

import org.anyline.entity.metadata.ColumnType;

public class Parameter {
    private boolean in;
    private boolean out;

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

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public String toString(){
        return "{value:"+value+",type:"+type+"}";
    }

}
