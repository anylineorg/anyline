package org.anyline.data.jdbc.ds;

import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.jdbc.util.SQLAdapterUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class JDBCRuntime {
    private String key;
    private JdbcTemplate template;
    private JDBCAdapter adapter;
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

    public void setAdapter(JDBCAdapter adapter) {
        this.adapter = adapter;
    }
}
