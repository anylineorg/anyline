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



package org.anyline.data.influxdb.runtime;

import com.influxdb.client.InfluxDBClient;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

public class InfluxRuntime extends AbstractRuntime implements DataRuntime {

    protected InfluxDBClient client;
    protected String bucket;
    protected String org;
    protected String token;
    protected String user;
    protected String password;

    public InfluxRuntime() {
    }
    public String bucket(){
        return bucket;
    }
    public String org(){
        return org;
    }
    public String token(){
        return token;
    }
    public String user(){
        return user;
    }
    public String password(){
        return password;
    }
    public InfluxRuntime token(String token){
        this.token = token;
        return this;
    }
    public InfluxRuntime org(String org){
        this.org = org;
        return this;
    }
    public InfluxRuntime bucket(String bucket){
        this.bucket = bucket;
        return this;
    }
    public InfluxRuntime password(String password){
        this.password = password;
        return this;
    }
    public InfluxRuntime user(String user){
        this.user = user;
        return this;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof InfluxDBClient) {
            this.client = (InfluxDBClient) processor;
        }
    }
    public InfluxDBClient client() {
        return client;
    }

    @Override
    public void setAdapterKey(String adapter) {
    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public InfluxRuntime(String key, InfluxDBClient client, DriverAdapter adapter) {
        setKey(key);
        setProcessor(client);
        setAdapter(adapter);
    }

    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != client) {
                feature = client.getClass().getName();
            }
        }
        return feature;
    }

    public void setSession(InfluxDBClient client) {
        this.client = client;
    }

}
