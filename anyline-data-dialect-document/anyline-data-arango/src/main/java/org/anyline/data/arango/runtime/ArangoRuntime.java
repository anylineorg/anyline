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


package org.anyline.data.arango.runtime;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

public class ArangoRuntime extends AbstractRuntime implements DataRuntime {

    protected ArangoDB client;
    protected ArangoDatabase database;

    public ArangoRuntime() {
    }

    public Object getProcessor() {
        return database;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof ArangoDB) {
            this.client = (ArangoDB) processor;
        }else if(processor instanceof ArangoDatabase) {
            this.database = (ArangoDatabase)processor;
        }
    }

    public String datasource() {
        String datasource = key;
        return datasource;
    }

    public ArangoRuntime(String key, ArangoDB client, ArangoDatabase database, DriverAdapter adapter) {
        setKey(key);
        setClient(client);
        setProcessor(database);
        setAdapter(adapter);
    }

    public ArangoDB client() {
        return client;
    }

    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != client) {
                feature = client.getClass().getName()+"_arango:";
            }
        }
        return feature;
    }

    public void setClient(ArangoDB client) {
        this.client = client;
    }
    public ArangoDB getClient() {
        return client;
    }

    public ArangoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(ArangoDatabase database) {
        this.database = database;
    }


    @Override
    public boolean destroy() throws Exception {
        ArangoRuntimeHolder.instance().destroy(this.key);
        return true;
    }
}