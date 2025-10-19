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

package org.anyline.data.neo4j.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;
import org.neo4j.driver.Driver;

public class Neo4jRuntime extends AbstractRuntime implements DataRuntime {

    protected Driver driver;
    public Neo4jRuntime() {
    }

    public void setProcessor(Object processor) {
        if(processor instanceof Driver) {
            this.driver = (Driver) processor;
        }
    }
    @Override
    public void setAdapterKey(String adapter) {
    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public Neo4jRuntime(String key, Driver driver, DriverAdapter adapter) {
        setKey(key);
        setProcessor(driver);
        setAdapter(adapter);
    }

    public Driver driver() {
        return driver;
    }


    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != driver) {
                feature = driver.getClass().getName();
            }
        }
        return feature;
    }

}
