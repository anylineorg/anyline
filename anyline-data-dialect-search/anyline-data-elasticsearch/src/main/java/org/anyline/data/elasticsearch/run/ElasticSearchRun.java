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
