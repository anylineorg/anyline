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

package org.anyline.environment.spring.data.jdbc.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class SpringJDBCRuntime extends AbstractRuntime implements DataRuntime {
    protected JdbcTemplate processor;

    public SpringJDBCRuntime(String key, JdbcTemplate jdbc, DriverAdapter adapter){
        setKey(key);
        setProcessor(jdbc);
        setAdapter(adapter);
    }
    public SpringJDBCRuntime(){
    }
    public JdbcTemplate jdbc(){
        return processor;
    }
    public DataSource getDataSource(){
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
                    DataSource datasource = null;
                    Connection con = null;
                    try {
                        datasource = jdbc.getDataSource();
                        con = DataSourceUtils.getConnection(datasource);
                        DatabaseMetaData meta = con.getMetaData();
                        String url = meta.getURL();
                        if(null == adapterKey){
                            adapterKey = DataSourceUtil.parseAdapterKey(url);
                        }
                        feature = driver + "_" + meta.getDatabaseProductName().toLowerCase().replace(" ","") + "_" + url;
                        if (null == version) {
                            version = meta.getDatabaseProductVersion();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != con && !DataSourceUtils.isConnectionTransactional(con, datasource)) {
                            DataSourceUtils.releaseConnection(con, datasource);
                        }
                    }
                }
            }else{
                feature = driver + "_" + url;
            }
        }
        if(null == adapterKey){
            adapterKey = DataSourceUtil.parseAdapterKey(feature);
        }
        return feature;
    }

    public String getVersion() {
        if(null == version){
            JdbcTemplate jdbc = jdbc();
            if(null != jdbc){
                DataSource datasource = null;
                Connection con = null;
                try {
                    datasource = jdbc.getDataSource();
                    con = DataSourceUtils.getConnection(datasource);
                    DatabaseMetaData meta = con.getMetaData();
                    version = meta.getDatabaseProductVersion();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(null != con && !DataSourceUtils.isConnectionTransactional(con, datasource)){
                        DataSourceUtils.releaseConnection(con, datasource);
                    }
                }
            }
        }
        return version;
    }
}
