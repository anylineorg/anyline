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



package org.anyline.data.runtime.init;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;

public class AbstractRuntime implements DataRuntime {
    /**
     * 复制来源
     */
    protected String origin;
    /**
     * 表示数据源名称
     */
    protected String key;
    /**
     * 运行环境特征 如jdbc-url
     * 用来匹配 DriverAdapter
     */
    protected String feature;
    /**
     * 运行环境版本 用来匹配 DriverAdapter
     */

    protected String url;
    protected String driver;
    protected String version;
    protected DriverAdapter adapter;
    protected String adapterKey;
    protected String catalog;
    protected String schema;
    protected RuntimeHolder holder;

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public void origin(String origin) {
        this.origin = origin;
    }

    @Override
    public String getFeature(boolean connection) {
        return null;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Override
    public String getVersion() {
        return null;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void setDriver(String driver) {

    }

    @Override
    public String getDriver() {
        return null;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Object getProcessor() {
        return null;
    }

    @Override
    public void setProcessor(Object processor) {

    }

    @Override
    public String getCatalog() {
        return catalog;
    }

    @Override
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    public DriverAdapter getAdapter() {
        if(null == adapter) {
            String lockKey = (AbstractRuntime.class.getName() + "getAdapter" + key).intern();
            synchronized (lockKey) {
                if(null == adapter) {
                    String datasource = key;
                    adapter = DriverAdapterHolder.getAdapter(datasource, this);
                }
            }
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

    public void setAdapterKey(String adapter) {
        this.adapterKey = adapter;
    }
    public String getAdapterKey() {
        return adapterKey;
    }
    @Override
    public void setHolder(RuntimeHolder holder) {
        this.holder = holder;
    }

    @Override
    public RuntimeHolder getHolder() {
        return holder;
    }

    @Override
    public boolean destroy() throws Exception {
        return true;
    }

}
