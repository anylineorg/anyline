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

package org.anyline.data.hbase.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;
import org.apache.hadoop.hbase.client.Connection;

public class HBaseRuntime extends AbstractRuntime implements DataRuntime {

    protected Connection connection;

    public HBaseRuntime() {
    }

    public Object getProcessor() {
        return connection;
    }

    public void setProcessor(Object processor) {
        if(processor instanceof Connection) {
            this.connection = (Connection) processor;
        }
    }

    @Override
    public void setAdapterKey(String adapter) {

    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public HBaseRuntime(String key, Connection connection, DriverAdapter adapter) {
        setKey(key);
        setProcessor(connection);
        setAdapter(adapter);
    }

    public Connection connection() {
        return connection;
    }

    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != this.connection) {
                feature = this.connection.getClass().getName();
            }
        }
        return feature;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
