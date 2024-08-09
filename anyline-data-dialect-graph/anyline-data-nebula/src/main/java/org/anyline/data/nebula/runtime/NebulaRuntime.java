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
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

public class NebulaRuntime extends AbstractRuntime implements DataRuntime {

    protected SessionPool pool;
    public NebulaRuntime() {
    }

    public void setProcessor(Object processor) {
        if(processor instanceof SessionPool) {
            this.pool = (SessionPool) processor;
        }
    }
    public SessionPool getSession() {
        return pool;
    }

    @Override
    public void setAdapterKey(String adapter) {
    }

    @Override
    public String getAdapterKey() {
        return null;
    }

    public NebulaRuntime(String key, SessionPool pool, DriverAdapter adapter) {
        setKey(key);
        setProcessor(pool);
        setAdapter(adapter);
    }

    public SessionPool session() {
        return pool;
    }

    public String getFeature(boolean connection) {
        if(null == feature) {
            if(null != pool) {
                feature = pool.getClass().getName();
            }
        }
        return feature;
    }

    public void setSession(SessionPool pool) {
        this.pool = pool;
    }

}
