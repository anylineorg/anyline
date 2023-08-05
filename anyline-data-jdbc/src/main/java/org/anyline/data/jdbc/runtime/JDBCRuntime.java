package org.anyline.data.jdbc.runtime;

import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.ClientHolder;
import org.anyline.data.util.DriverAdapterHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class JDBCRuntime implements DataRuntime {

    /**
     * 表示数据源名称
     */
    private String key;
    /**
     * 运行环境特征 如jdbc-url
     * 用来匹配 DriverAdapter
     */
    protected String feature;
    /**
     * 运行环境版本 用来匹配 DriverAdapter
     */
    protected String version;
    protected DriverAdapter adapter;
    protected JdbcTemplate client;
    protected AnylineDao dao;

    public AnylineDao getDao() {
        return dao;
    }

    public void setDao(AnylineDao dao) {
        this.dao = dao;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }


    public void setVersion(String version) {
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getClient() {
        return client;
    }

    public void setClient(Object client) {
        this.client = (JdbcTemplate) client;
    }


    public DriverAdapter getAdapter() {
        if(null == adapter){
            String ds = key;
            if("common".equals(ds)){
                ds = ClientHolder.curDataSource();
            }
            adapter = DriverAdapterHolder.getAdapter(ds, this);
        }
        return adapter;
    }
    public String datasource(){
        String ds = key;
        if("common".equals(ds)){
            ds = ClientHolder.curDataSource();
        }
        return ds;
    }
    public void setAdapter(DriverAdapter adapter) {
        this.adapter = adapter;
    }

    public JDBCRuntime(String key, JdbcTemplate jdbc, DriverAdapter adapter){
        setKey(key);
        setClient(jdbc);
        setAdapter(adapter);
    }
    public JDBCRuntime(){
    }

    public JdbcTemplate jdbc(){
        return client;
    }
    public String getFeature() {
        if(null == feature){
            JdbcTemplate jdbc = jdbc();
            if(null != jdbc){
                DataSource ds = null;
                Connection con = null;
                try {
                    ds = jdbc.getDataSource();
                    con = DataSourceUtils.getConnection(ds);
                    DatabaseMetaData meta = con.getMetaData();
                    feature = meta.getDatabaseProductName().toLowerCase().replace(" ", "");
                    if(null == version) {
                        version = meta.getDatabaseProductVersion();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(!DataSourceUtils.isConnectionTransactional(con, ds)){
                        DataSourceUtils.releaseConnection(con, ds);
                    }
                }
            }
        }
        return feature;
    }

    public String getVersion() {
        if(null == version){
            JdbcTemplate jdbc = jdbc();
            if(null != jdbc){
                DataSource ds = null;
                Connection con = null;
                try {
                    ds = jdbc.getDataSource();
                    con = DataSourceUtils.getConnection(ds);
                    DatabaseMetaData meta = con.getMetaData();
                    version = meta.getDatabaseProductVersion();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(!DataSourceUtils.isConnectionTransactional(con, ds)){
                        DataSourceUtils.releaseConnection(con, ds);
                    }
                }
            }
        }
        return version;
    }
}
