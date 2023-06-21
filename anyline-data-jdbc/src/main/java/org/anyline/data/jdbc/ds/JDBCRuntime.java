package org.anyline.data.jdbc.ds;

import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.jdbc.util.SQLAdapterUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class JDBCRuntime {
    /**
     * 表示数据源名称
     */
    private String key;
    /**
     * JdbcTemplate
     */
    private JdbcTemplate template;
    /**
     * JDBCAdapter 用来生成不同数据库的SQL
     */
    private JDBCAdapter adapter;
    /**
     * dao
     */
    private AnylineDao dao;

    public JDBCRuntime(){

    }
    public JDBCRuntime(String key, JdbcTemplate template, JDBCAdapter adapter){
        this.key = key;
        this.template = template;
        this.adapter = adapter;
    }
    public DataSource getDatasource(){
        return template.getDataSource();
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    public AnylineDao getDao() {
        return dao;
    }

    public void setDao(AnylineDao dao) {
        this.dao = dao;
    }

    public JDBCAdapter getAdapter() {
        if(null == adapter){
            String ds = key;
            if("common".equals(ds)){
                ds = DataSourceHolder.curDataSource();
            }
            adapter = SQLAdapterUtil.getAdapter(ds, template);
            if(null != adapter && null == adapter.getDao()){
                adapter.setDao(dao);
            }
        }
        return adapter;
    }
    public String datasource(){
        String ds = key;
        if("common".equals(ds)){
            ds = DataSourceHolder.curDataSource();
        }
        return ds;
    }
    public void setAdapter(JDBCAdapter adapter) {
        this.adapter = adapter;
    }
}
