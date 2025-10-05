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

package org.anyline.data.mongodb.runtime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

public class MongoRuntime extends AbstractRuntime implements DataRuntime {

    protected MongoClient client;
    protected MongoDatabase database;

    public MongoRuntime() {
    }

    public Object getProcessor() {
        return database;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof MongoClient) {
            this.client = (MongoClient) processor;
        }else if(processor instanceof MongoDatabase) {
            this.database = (MongoDatabase)processor;
        }
    }

    public String datasource() {
        String datasource = key;
        return datasource;
    }

    public MongoRuntime(String key, MongoClient client, MongoDatabase database, DriverAdapter adapter) {
        setKey(key);
        setClient(client);
        setProcessor(database);
        setAdapter(adapter);
    }

    public MongoClient client() {
        return client;
    }

    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != client) {
                feature = client.getClass().getName()+"_mongodb:";
            }
        }
        return feature;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }
    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }


    @Override
    public boolean destroy() throws Exception {
        MongoRuntimeHolder.instance().destroy(this.key);
        return true;
    }
}
