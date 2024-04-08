package org.anyline.environment.boot.elasticsearch;

import org.anyline.environment.boot.datasource.DataSourceProperty;
import org.anyline.environment.boot.http.HttpProperty;

//@Configuration("anyline.environment.boot.elasticsearch")
//@ConfigurationProperties(prefix = "anyline.elasticsearch")
public class ElasticsearchProperty extends DataSourceProperty {
    private HttpProperty http = new HttpProperty();

    public HttpProperty getHttp() {
        return http;
    }

    public void setHttp(HttpProperty http) {
        this.http = http;
    }
}
