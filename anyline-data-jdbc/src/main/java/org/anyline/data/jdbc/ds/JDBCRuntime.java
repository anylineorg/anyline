package org.anyline.data.jdbc.ds;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.springframework.jdbc.core.JdbcTemplate;

public class JDBCRuntime {
    private String datasource;
    private JdbcTemplate template;
    private JDBCAdapter adapter;

    public JDBCRuntime(){

    }
    public JDBCRuntime(String datasource, JdbcTemplate template, JDBCAdapter adapter){
        this.datasource = datasource;
        this.template = template;
        this.adapter = adapter;
    }
    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    public JDBCAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(JDBCAdapter adapter) {
        this.adapter = adapter;
    }
}
