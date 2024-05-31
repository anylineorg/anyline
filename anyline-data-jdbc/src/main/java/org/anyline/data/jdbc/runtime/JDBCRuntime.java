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
import org.anyline.data.runtime.init.AbstractRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class JDBCRuntime extends AbstractRuntime implements DataRuntime {
    private static Logger log = LoggerFactory.getLogger(JDBCRuntime.class);
    protected DataSource processor;

    public JDBCRuntime(String key, DataSource datasource, DriverAdapter adapter){
        setKey(key);
        setProcessor(datasource);
        setAdapter(adapter);
    }
    public JDBCRuntime(){
    }
    public DataSource getDataSource(){
        return processor;
    }

    public Object getProcessor() {
        return processor;
    }

    public void setProcessor(Object processor) {
        this.processor = (DataSource) processor;
    }

    public String getFeature(boolean connection) {
        if(null == feature){
            if(connection || null == driver || null == url) {
                if (null != processor) {
                    Connection con = null;
                    try {
                        con = processor.getConnection();
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
                        log.error("获取数据源特征 异常:", e);
                    } finally {
                        try {
                            con.close();
                        }catch (Exception e){
                            log.error("释放连接 异常:", e);
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
            if(null != processor){
                Connection con = null;
                try {
                    con = processor.getConnection();
                    DatabaseMetaData meta = con.getMetaData();
                    version = meta.getDatabaseProductVersion();
                }catch (Exception e){
                    log.error("获取数据源版本 异常:", e);
                }finally {
                    try {
                        con.close();
                    }catch (Exception e){
                        log.error("释放连接 异常:", e);
                    }
                }
            }
        }
        return version;
    }

    @Override
    public boolean destroy() throws Exception {
        JDBCRuntimeHolder.instance().destroy(this.key);
        return true;
    }

}
