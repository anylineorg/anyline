package org.anyline.boot.elasticsearch;

import org.anyline.boot.datasource.DataSourceProperty;
import org.anyline.boot.http.HttpProperty;


//@Configuration("anyline.boot.elasticsearch")
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
