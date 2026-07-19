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


package org.anyline.data.couchbase.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

public class CouchbaseRuntime extends AbstractRuntime implements DataRuntime {

    protected Object client;

    public CouchbaseRuntime() {
    }

    @Override
    public Object getProcessor() {
        return client;
    }

    @Override
    public void setProcessor(Object processor) {
        this.client = processor;
    }

    @Override
    public void setAdapterKey(String adapter) {
    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public CouchbaseRuntime(String key, Object client, DriverAdapter adapter) {
        setKey(key);
        setProcessor(client);
        setAdapter(adapter);
    }

    public Object client() {
        return client;
    }

    @Override
    public String getFeature(boolean connection) {
        if (null == feature && null != client) {
            feature = client.getClass().getName();
        }
        return feature;
    }

    public void setClient(Object client) {
        this.client = client;
    }
}