/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.data.jdbc.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class JDBCRuntime implements DataRuntime {

    private String origin;
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
    protected JdbcTemplate processor;
    protected RuntimeHolder holder;

    protected String url;
    protected String driver;

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getDriver() {
        return driver;
    }

    @Override
    public void setDriver(String driver) {
        this.driver = driver;
    }

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public void origin(String origin) {
        this.origin = origin;
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

    public Object getProcessor() {
        return processor;
    }

    public void setProcessor(Object processor) {
        this.processor = (JdbcTemplate) processor;
    }


    public DriverAdapter getAdapter() {
        if(null == adapter){
            String ds = key;
            adapter = DriverAdapterHolder.getAdapter(ds, this);
        }
        return adapter;
    }

    @Override
    public String datasource() {
        return key;
    }

    public void setAdapter(DriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void setHolder(RuntimeHolder holder) {
        this.holder = holder;
    }

    @Override
    public RuntimeHolder getHolder() {
        return holder;
    }

    public JDBCRuntime(String key, JdbcTemplate jdbc, DriverAdapter adapter){
        setKey(key);
        setProcessor(jdbc);
        setAdapter(adapter);
    }
    public JDBCRuntime(){
    }

    public JdbcTemplate jdbc(){
        return processor;
    }
    public String getFeature(boolean connection) {
        if(null == feature){
            if(connection || null == driver || null == url) {
                JdbcTemplate jdbc = jdbc();
                if (null != jdbc) {
                    DataSource ds = null;
                    Connection con = null;
                    try {
                        ds = jdbc.getDataSource();
                        con = DataSourceUtils.getConnection(ds);
                        DatabaseMetaData meta = con.getMetaData();
                        feature = driver + "_" + meta.getDatabaseProductName().toLowerCase().replace(" ", "") + "_"
                        + meta.getURL();
                        if (null == version) {
                            version = meta.getDatabaseProductVersion();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != con && !DataSourceUtils.isConnectionTransactional(con, ds)) {
                            DataSourceUtils.releaseConnection(con, ds);
                        }
                    }
                }
            }else{
                return driver + "_" + url;
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
                    if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
                        DataSourceUtils.releaseConnection(con, ds);
                    }
                }
            }
        }
        return version;
    }
}
