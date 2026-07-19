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


package org.anyline.data.opensearch.param;

import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;

import java.util.Arrays;
import java.util.List;

public class OpenSearchConfigStore extends DefaultConfigStore {
    protected String collapse;
    protected List<Object> afters;
    protected Boolean trackTotalHits;
    protected String sql;
    protected RunPrepare prepare;
    protected OpenSearchRequestBody requestBody;

    public OpenSearchRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(OpenSearchRequestBody requestBody) {
        this.requestBody = requestBody;
    }
    public void setRequestBody(String json) {
        this.requestBody = new OpenSearchRequestBody(json);
    }
    public OpenSearchConfigStore collapse(String field){
        this.collapse = field;
        return this;
    }
    public String collapse(){
        return this.collapse;
    }
    public OpenSearchConfigStore afters(Object ... afters){
        if(null != afters){
            this.afters.addAll(Arrays.asList(afters));
        }
        return this;
    }
    public OpenSearchConfigStore afters(List<Object> afters){
        this.afters = afters;
        return this;
    }
    public List<Object> afters(){
        return this.afters;
    }

    public OpenSearchConfigStore trackTotalHits(Boolean trackTotalHits){
        this.trackTotalHits = trackTotalHits;
        return this;
    }
    public Boolean trackTotalHits(){
        return this.trackTotalHits;
    }

    public OpenSearchConfigStore sql(String sql){
        this.sql = sql;
        return this;
    }
    public OpenSearchConfigStore sql(RunPrepare prepare){
        this.prepare = prepare;
        return this;
    }
    public String sql(){
        return this.sql;
    }

    public RunPrepare prepare(){
        return this.prepare;
    }

}