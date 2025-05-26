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

package org.anyline.data.milvus.runtime;

import io.milvus.v2.client.MilvusClientV2;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

public class MilvusRuntime extends AbstractRuntime implements DataRuntime {

    protected MilvusClientV2 client;

    public MilvusRuntime() {
    }

    public Object getProcessor() {
        return client;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof MilvusClientV2) {
            this.client = (MilvusClientV2) processor;
        }
    }

    @Override
    public void setAdapterKey(String adapter) {

    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public MilvusRuntime(String key, MilvusClientV2 client, DriverAdapter adapter) {
        setKey(key);
        setProcessor(client);
        setAdapter(adapter);
    }

    public MilvusClientV2 client() {
        return client;
    }

    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != client) {
                feature = client.getClass().getName();
            }
        }
        return feature;
    }

    public void setClient(MilvusClientV2 client) {
        this.client = client;
    }
}
