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

package org.anyline.data.elasticsearch.run;

import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;

public class ElasticSearchRun extends SimpleRun {
    private String method;
    private String endpoint;
    public ElasticSearchRun(DataRuntime runtime, String cmd){
        super(runtime, cmd);
    }
    public ElasticSearchRun(DataRuntime runtime){
        super(runtime);
    }
    public ElasticSearchRun(DataRuntime runtime, StringBuilder builder){
        super(runtime, builder);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
