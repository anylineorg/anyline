package org.anyline.jdbc.prepare.simple;

public class ProcedureParam {
    private Object value;
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

    public void setType(Integer type) {
        this.type = type;
    }
    public String toString(){
        return "{value:"+value+",type:"+type+"}";
    }
}
