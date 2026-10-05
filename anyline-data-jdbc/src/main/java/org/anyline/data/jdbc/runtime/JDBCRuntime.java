/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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
    /** feature是否只是根据数据源属性识别的(没有连接数据库,不含产品名),连接数据库后需要重新识别 */
    protected boolean urlOnlyFeature;

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
                if(BasicUtil.isNotEmpty(feature)) {
                    // 根据url driver识别出的特征不含产品名
                    urlOnlyFeature = true;
                }
            }
        }
        if(!keep) {
            connection = true;
            driver = null;
            url = null;
        }

        if(BasicUtil.isEmpty(feature) || (connection && urlOnlyFeature)) {
            // 先尝试在不建立连接的情况下识别数据库特征
            // 通过DataSource对象注册的数据源没有配置url driver,但可以从数据源属性中读取(如HikariDataSource#getJdbcUrl DruidDataSource#getUrl)
            if(null == url) {
                url = DataSourceUtil.parseUrl(processor);
            }
            if(null == driver) {
                driver = DataSourceUtil.parseDriver(processor);
            }
            if(BasicUtil.isNotEmpty(url)) {
                feature = url;
                urlOnlyFeature = true;
                if(null == adapterKey && ConfigTable.KEEP_ADAPTER == 1) {
                    adapterKey = DataSourceUtil.parseAdapterKey(url);
                }
            }
            if(BasicUtil.isNotEmpty(driver)) {
                if(BasicUtil.isEmpty(feature)) {
                    feature = driver;
                    urlOnlyFeature = true;
                }else {
                    feature = driver + "_" + feature;
                }
            }
            // 只有在不建立连接就无法识别的情况下才建立连接
            // 根据url driver识别出的特征不含产品名,不够准确,要求建立连接时重新识别
            if(connection && (BasicUtil.isEmpty(feature) || urlOnlyFeature)) {
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
                        urlOnlyFeature = false;
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

    @Override
    public String getVersion() throws Exception{
        if(null == version) {
            if(null != processor) {
                Connection con = null;
                try {
                    con = processor.getConnection();
                    DatabaseMetaData meta = con.getMetaData();
                    version = meta.getDatabaseProductVersion();
                } finally {
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