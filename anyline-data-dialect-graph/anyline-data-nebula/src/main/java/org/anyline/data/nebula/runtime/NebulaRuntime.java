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


package org.anyline.data.nebula.runtime;

import com.vesoft.nebula.client.graph.SessionPool;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.runtime.init.DefaultRuntime;

public class NebulaRuntime extends DefaultRuntime implements DataRuntime {

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
    protected SessionPool session;
    protected RuntimeHolder holder;

    protected String url;
    protected String driver;
    public NebulaRuntime(){
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
        return session;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof SessionPool) {
            this.session = (SessionPool) processor;
        }
    }
    public SessionPool getSession(){
        return session;
    }
    @Override
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
    public void setAdapterKey(String adapter) {

    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public NebulaRuntime(String key, SessionPool client, DriverAdapter adapter){
        setKey(key);
        setProcessor(client);
        setAdapter(adapter);
    }

    public SessionPool session(){
        return session;
    }

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public void origin(String origin) {
        this.origin = origin;
    }

    public String getFeature(boolean connection) {
        if(null == feature){
            if(null != session){
                feature = session.getClass().getName();
            }
        }
        return feature;
    }

    public void setSession(SessionPool session) {
        this.session = session;
    }

    public String getVersion() {
        return version;
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

    @Override
    public RuntimeHolder getHolder() {
        return holder;
    }

    @Override
    public void setHolder(RuntimeHolder holder) {
        this.holder = holder;
    }
}
