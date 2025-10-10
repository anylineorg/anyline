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

package org.anyline.data.runtime.init;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.entity.authorize.Role;
import org.anyline.util.ConfigTable;

import java.util.LinkedHashMap;
import java.util.List;

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

    protected String lastFeature;
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
    protected LinkedHashMap<String, Role> roles;

    @Override
    public LinkedHashMap<String, Role> roles(){
        if(null == roles && null != adapter){
            try {
                roles = adapter.roles(this, null);
            }catch (Exception ignore){
                roles = new LinkedHashMap<>();
            }
        }
        return roles;
    }
    @Override
    public void roles(LinkedHashMap<String, Role> roles){
        this.roles = roles;
    }
    @Override
    public void roles(List<Role> roles){
        if(null == this.roles){
            this.roles = new LinkedHashMap<>();
        }
        for(Role role : roles){
            this.roles.put(role.getName().toUpperCase(), role);
        }
    }
    @Override
    public void roles(Role ... roles){
        if(null == this.roles){
            this.roles = new LinkedHashMap<>();
        }
        for(Role role : roles){
            this.roles.put(role.getName().toUpperCase(), role);
        }
    }
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

    @Override
    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Override
    public String getLastFeature() {
        return this.lastFeature;
    }

    @Override
    public void setLastFeature(String feature) {
        this.lastFeature = feature;
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
        if(ConfigTable.KEEP_ADAPTER == 1) {
            this.catalog = catalog;
        }
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public void setSchema(String schema) {
        if(ConfigTable.KEEP_ADAPTER == 1) {
            this.schema = schema;
        }
    }

    public DriverAdapter getAdapter() {
        boolean keep = DriverAdapterHolder.keepAdapter(this, getProcessor());
        if(null == adapter || !keep) {
            String lockKey = (AbstractRuntime.class.getName() + "getAdapter" + key).intern();
            synchronized (lockKey) {
                if(null == adapter || !keep) {
                    String datasource = key;
                    adapter = DriverAdapterHolder.getAdapter(datasource, this);
                }
            }
        }
        return adapter;
    }

    @Override
    public String datasource() {
        String result = key;
        boolean keep = DriverAdapterHolder.keepAdapter(this, getProcessor());
        if(keep) {
            return result;
        }
        String lockKey = (AbstractRuntime.class.getName() + "getAdapter" + key).intern();
        synchronized (lockKey) {
            result = DriverAdapterHolder.key(this, getProcessor());
        }
        if(null == result) {
            result = key;
        }
        return key+"-"+result;
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
