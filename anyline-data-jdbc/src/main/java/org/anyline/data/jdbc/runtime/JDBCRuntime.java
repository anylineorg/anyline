/*
 * Copyright 2006-2025 www.anyline.org
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
import org.anyline.data.runtime.init.AbstractRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class JDBCRuntime extends AbstractRuntime implements DataRuntime {
    private static final Log log = LogProxy.get(JDBCRuntime.class);
    protected DataSource processor;

    public JDBCRuntime(String key, DataSource datasource, DriverAdapter adapter) {
        setKey(key);
        setProcessor(datasource);
        setAdapter(adapter);
    }
    public JDBCRuntime() {
    }
    public DataSource getDataSource() {
        return processor;
    }

    public Object getProcessor() {
        return processor;
    }

    public void setProcessor(Object processor) {
        this.processor = (DataSource) processor;
    }

    public String getFeature(boolean connection) {
        boolean keep = DriverAdapterHolder.keepAdapter(this, getProcessor());
        String feature = DriverAdapterHolder.feature(this, getProcessor());
        String url = null;
        String driver = null;
        if(ConfigTable.KEEP_ADAPTER == 1) {
            feature = this.feature;
            url = this.url;
            driver = this.driver;
            keep = true;
            if(BasicUtil.isEmpty(feature)) {
                feature = BasicUtil.concat("_", url, driver);
            }
        }
        if(!keep) {
            connection = true;
            driver = null;
            url = null;
        }

        if(BasicUtil.isEmpty(feature)) {
            if(connection || null == driver || null == url) {
                if (null != processor) {
                    Connection con = null;
                    try {
                        con = processor.getConnection();
                        DatabaseMetaData meta = con.getMetaData();
                        url = meta.getURL();
                        if(null == adapterKey && ConfigTable.KEEP_ADAPTER == 1) {
                            adapterKey = DataSourceUtil.parseAdapterKey(url);
                        }
                        feature = meta.getDatabaseProductName().toLowerCase().replace(" ","") + "_" + url;
                        if (null == version) {
                            version = meta.getDatabaseProductVersion();
                        }
                    } catch (Exception e) {
                        log.error("获取数据源特征 异常:", e);
                    } finally {
                        try {
                            con.close();
                        }catch (Exception e) {
                            log.error("释放连接 异常:", e);
                        }
                    }
                }
            }else{
                feature = url;
            }
            if(null != driver) {
                feature = driver + "_" + feature;
            }
        }
        if(null == adapterKey && keep) {
            adapterKey = DataSourceUtil.parseAdapterKey(feature);
        }
        if(keep) {
            this.feature = feature;
            this.url = url;
        }
        setLastFeature(feature);
        return feature;
    }

    public String getVersion() {
        if(null == version) {
            if(null != processor) {
                Connection con = null;
                try {
                    con = processor.getConnection();
                    DatabaseMetaData meta = con.getMetaData();
                    version = meta.getDatabaseProductVersion();
                }catch (Exception e) {
                    log.error("获取数据源版本 异常:", e);
                }finally {
                    try {
                        con.close();
                    }catch (Exception e) {
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
