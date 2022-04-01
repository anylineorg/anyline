package org.anyline.net;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpBuilder {
    private static final Logger log = LoggerFactory.getLogger(HttpBuilder.class);
    private CloseableHttpClient client;
    private Map<String, String> headers = new HashMap<>();
    private List<HttpEntity> entitys = new ArrayList<>();
    private Map<String, Object> params = new HashMap<>();
    private List<NameValuePair> pairs = new ArrayList<>();
    private String userAgent;
    private String url;
    private String encode;
    private DownloadTask task;
    private Map<String,File> uploads;
    private String returnType = "text";

    public HttpClient build(){
        HttpClient client = new HttpClient();
        client.setClient(this.client);
        client.setHeaders(headers);
        client.setEntitys(entitys);
        client.setParams(params);
        client.setPairs(pairs);
        client.setUserAgent(userAgent);
        client.setUrl(url);
        client.setEncode(encode);
        client.setTask(task);
        client.setUploads(uploads);
        client.setReturnType(returnType);
        return client;
    }

    public static HttpBuilder init(){
        return new HttpBuilder();
    }
    public HttpBuilder setClient(CloseableHttpClient client){
        this.client = client;
        return this;
    }
    public HttpBuilder setHeaders(Map<String, String> headers){
        this.headers = headers;
        return this;
    }
    public HttpBuilder addHeader(String key, String value){
        headers.put(key, value);
        return this;
    }
    public HttpBuilder setUrl(String url){
        this.url = url;
        return this;
    }
    public HttpBuilder setEncode(String encode){
        this.encode = encode;
        return this;
    }
    public HttpBuilder setEntitys(List<HttpEntity> entitys){
        this.entitys = entitys;
        return this;
    }
    public HttpBuilder addEntity(HttpEntity entity){
        entitys.add(entity);
        return this;
    }
    public HttpBuilder setPairs(List<NameValuePair> pairs){
        this.pairs = pairs;
        return this;
    }
    public HttpBuilder addPair(List<NameValuePair> pairs){
        this.pairs = pairs;
        return this;
    }
    public HttpBuilder addDownloadTask(DownloadTask task){
        this.task = task;
        return this;
    }
    public HttpBuilder setUploadFiles(Map<String, File> files){
        uploads = files;
        return this;
    }
    public HttpBuilder setParams(Map<String, Object> params){
        this.params = params;
        return this;
    }
    public HttpBuilder addParam(String key, String value){
        params.put(key, value);
        return this;
    }
    public HttpBuilder setReturnType(String type){
        this.returnType = type;
        return this;
    }
    public HttpBuilder setUserAgent(String agent){
        this.userAgent = agent;
        return this;
    }


}
