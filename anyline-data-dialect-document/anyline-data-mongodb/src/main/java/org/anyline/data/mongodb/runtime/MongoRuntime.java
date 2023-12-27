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


package org.anyline.data.mongodb.runtime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.mongodb.entity.MongoDataRow;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.util.ConfigTable;

public class MongoRuntime implements DataRuntime {

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
    protected MongoClient client;
    protected MongoDatabase database;
    protected RuntimeHolder holder;

    protected String url;
    protected String driver;
    public MongoRuntime(){
        ConfigTable.DEFAULT_MONGO_ENTITY_CLASS = MongoDataRow.class;
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
        return database;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof MongoClient) {
            this.client = (MongoClient) processor;
        }else if(processor instanceof MongoDatabase){
            this.database = (MongoDatabase)processor;
        }
    }


    public DriverAdapter getAdapter() {
        if(null == adapter){
            String ds = key;
            adapter = DriverAdapterHolder.getAdapter(ds, this);
        }
        return adapter;
    }
    public String datasource(){
        String ds = key;
        return ds;
    }
    public void setAdapter(DriverAdapter adapter) {
        this.adapter = adapter;
    }

    public MongoRuntime(String key, MongoClient client, MongoDatabase database, DriverAdapter adapter){
        setKey(key);
        setProcessor(database);
        setAdapter(adapter);
    }

    public MongoClient client(){
        return client;
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
            if(null != client){
                feature = client.getClass().getName();
            }
        }
        return feature;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
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
