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
import org.anyline.data.runtime.init.AbstractRuntime;

public class NebulaRuntime extends AbstractRuntime implements DataRuntime {

    protected SessionPool session;
    public NebulaRuntime(){
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

}
