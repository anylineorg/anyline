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

package org.anyline.data.influxdb.param;

import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.util.BasicUtil;

public class InfluxConfigStore extends DefaultConfigStore {
    private String bucket;
    private String org;
    private String start;
    private String stop;
    private String measurement;

    public InfluxConfigStore(){
        super();
    }
    public InfluxConfigStore start(String start){
        this.start = start;
        return this;
    }
    public String start(){
        return start;
    }
    public InfluxConfigStore stop(String stop){
        this.stop = stop;
        return this;
    }
    public String stop(){
        return stop;
    }
    public InfluxConfigStore range(String start, String stop){
        this.start = start;
        this.stop = stop;
        return this;
    }
    public InfluxConfigStore org(String org){
        this.org = org;
        return this;
    }
    public String org(){
        return org;
    }
    public InfluxConfigStore bucket(String bucket){
        this.bucket = bucket;
        return this;
    }
    public String bucket(){
        return bucket;
    }
    public InfluxConfigStore measurement(String measurement){
        this.measurement = measurement;
        return this;
    }
    public String measurement(){
        return measurement;
    }

    @Override
    public boolean isEmptyCondition() {
        if(null != chain && !chain.isEmpty()) {
            return false;
        }
        if(null != measurement){
            return false;
        }
        if(BasicUtil.isNotEmpty(start)){
            return false;
        }
        if(BasicUtil.isNotEmpty(stop)){
            return false;
        }
        return true;
    }

}
