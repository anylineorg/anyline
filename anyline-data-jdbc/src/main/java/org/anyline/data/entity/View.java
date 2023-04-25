package org.anyline.data.entity;

public class View extends Table implements org.anyline.entity.data.View{
    protected String keyword = "VIEW"            ;
    protected String sql;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
