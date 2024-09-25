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

package org.anyline.data.elasticsearch.param;

import org.anyline.data.param.init.DefaultConfigStore;

public class ElasticSearchConfigStore extends DefaultConfigStore {
    protected String collapse;
    protected String sql;
    protected ElasticSearchRequestBody requestBody;

    public ElasticSearchRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(ElasticSearchRequestBody requestBody) {
        this.requestBody = requestBody;
    }
    public void setRequestBody(String json) {
        this.requestBody = new ElasticSearchRequestBody(json);
    }
    public ElasticSearchConfigStore collapse(String field){
        this.collapse = field;
        return this;
    }
    public String collapse(){
        return this.collapse;
    }

    public ElasticSearchConfigStore sql(String sql){
        this.sql = sql;
        return this;
    }
    public String sql(){
        return this.sql;
    }

}
