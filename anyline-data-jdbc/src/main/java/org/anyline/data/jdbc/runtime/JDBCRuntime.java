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
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.runtime.init.DefaultRuntime;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class JDBCRuntime extends DefaultRuntime implements DataRuntime {
    protected JdbcTemplate processor;

    protected String url;
    protected String driver;

    public JDBCRuntime(String key, JdbcTemplate jdbc, DriverAdapter adapter){
        setKey(key);
        setProcessor(jdbc);
        setAdapter(adapter);
    }
    public JDBCRuntime(){
    }
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

    public JdbcTemplate jdbc(){
        return processor;
    }
    public DataSource getDatasource(){
        if(null != processor){
            return processor.getDataSource();
        }
        return null;
    }

    public Object getProcessor() {
        return processor;
    }

    public void setProcessor(Object processor) {
        this.processor = (JdbcTemplate) processor;
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
                        String url = meta.getURL();
                        if(null == adapterKey){
                            adapterKey = RuntimeHolder.parseAdapterKey(url);
                        }
                        feature = driver + "_" + meta.getDatabaseProductName().toLowerCase().replace(" ","") + "_" + url;
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
                feature = driver + "_" + url;
            }
        }
        if(null == adapterKey){
            adapterKey = RuntimeHolder.parseAdapterKey(feature);
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
