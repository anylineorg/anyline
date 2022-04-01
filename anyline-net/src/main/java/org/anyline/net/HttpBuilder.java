package org.anyline.net;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpBuilder {
    private static final Logger log = LoggerFactory.getLogger(HttpBuilder.class);
    private static CloseableHttpClient default_client;
    private static CloseableHttpClient default_ssl_client;
    private static RequestConfig default_request_config;
    private static int default_connect_timeout = 72000; //毫秒
    private static int default_socket_timeout = 72000;
    private static String default_user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36";

    public static String PROTOCOL_TLSV1 = "TLSv1";
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



    /**
     * 合并参数
     * @param url  url
     * @param params  params
     * @return return
     */
    public static String mergeParam(String url, Map<String,Object> params){
        if(BasicUtil.isEmpty(params)){
            return url;
        }
        if(null == url){
            url = "";
        }
        url = url.trim();
        String kv = BeanUtil.map2string(params);
        if(BasicUtil.isNotEmpty(kv)){
            if (url.indexOf("?") > -1) {
                if (url.indexOf("?") < url.length() - 1 && url.indexOf("&") < url.length() - 1) {
                    url += "&";
                }
            } else {
                url += "?";
            }
            url += kv;
        }
        return url;
    }
    public static MultipartEntityBuilder mergeParam(MultipartEntityBuilder builder, Map<String,Object> params, ContentType contetType){
        if(null != params){
            String txt = BeanUtil.map2string(params);
            String[] kvs = txt.split("&");
            for(String kv:kvs){
                String[] tmps = kv.split("=");
                if(tmps.length==2){
                    builder.addTextBody(tmps[0], tmps[1], contetType);
                }
            }
        }
        return builder;
    }
    /**
     * 合并参数
     * @param url  url
     * @param params  params
     * @return return
     */
    public static String mergeParam(String url, String ... params){
        if(BasicUtil.isEmpty(url) || BasicUtil.isEmpty(params)){
            return url;
        }
        url = url.trim();
        if (url.indexOf("?") > -1) {
            if (url.indexOf("?") < url.length() - 1 && url.indexOf("&") < url.length() - 1) {
                url += "&";
            }
        } else {
            url += "?";
        }
        String tmp = null;
        for(String param:params){
            if(BasicUtil.isEmpty(param)){
                continue;
            }
            if(null == tmp){
                tmp = param;
            }else{
                tmp += "&"+param;
            }
        }
        url += tmp;
        return url;
    }

    @SuppressWarnings("rawtypes")
    public static List<NameValuePair> packNameValuePair(Map<String,Object> params){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        if (null != params) {
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = params.get(key);
                if(null == value){
                    continue;
                }
                if(value instanceof String[]){
                    String vals[] = (String[])value;
                    for(String val:vals){
                        if(null == val){
                            continue;
                        }
                        pairs.add(new BasicNameValuePair(key, val));
                        if(ConfigTable.isDebug() && log.isWarnEnabled()){
                            log.warn("[request param][{}={}]", key, BasicUtil.cut(val,0,20));
                        }
                    }
                }else if(value instanceof Collection){
                    Collection vals = (Collection)value;
                    for(Object val:vals){
                        if(null == val){
                            continue;
                        }
                        pairs.add(new BasicNameValuePair(key, val.toString()));
                        if(ConfigTable.isDebug() && log.isWarnEnabled()){
                            log.warn("[request param][{}={}]",key,BasicUtil.cut(val.toString(),0,20));
                        }
                    }
                }else if(null != value){
                    pairs.add(new BasicNameValuePair(key, value.toString()));
                    if(ConfigTable.isDebug() && log.isWarnEnabled()){
                        log.warn("[request param][{}={}]",key,BasicUtil.cut(value.toString(),0,20));
                    }
                }
            }
        }
        return pairs;
    }

    public static CloseableHttpClient client(String url){
        if(url.contains("https://")){
            return defaultSSLClient();
        }else{
            return defaultClient();
        }
    }
    public static CloseableHttpClient defaultClient(){
        HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(default_request_config);
        builder.setUserAgent(default_user_agent);
        default_client = builder.build();
        return default_client;
    }
    public static CloseableHttpClient createClient(String userAgent){
        CloseableHttpClient client = null;
        HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(default_request_config);
        builder.setUserAgent(userAgent);
        client = builder.build();
        return client;
    }


    public static CloseableHttpClient ceateSSLClient(File keyFile, String protocol, String password){
        CloseableHttpClient httpclient = null;
        try{
            KeyStore keyStore  = KeyStore.getInstance("PKCS12");
            FileInputStream instream = new FileInputStream(keyFile);
            try {
                keyStore.load(instream, password.toCharArray());
            } finally {
                instream.close();
            }
            SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, password.toCharArray()).build();
            String[] protocols = new String[] {protocol};
            //ALLOW_ALL_HOSTNAME_VERIFIER  关闭host验证，允许和所有的host建立SSL通信
            //BROWSER_COMPATIBLE_HOSTNAME_VERIFIER  和浏览器兼容的验证策略，即通配符能够匹配所有子域名
            //STRICT_HOSTNAME_VERIFIER  严格匹配模式，hostname必须匹配第一个CN或者任何一个subject-alts
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,protocols, null,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        }catch(Exception e){
            e.printStackTrace();
        }
        return httpclient;
    }
    public static CloseableHttpClient defaultSSLClient(){
        try {
            if(null != default_ssl_client){
                return default_ssl_client;
            }
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            httpClientBuilder.setMaxConnTotal(10000);
            httpClientBuilder.setMaxConnPerRoute(1000);

            httpClientBuilder.evictIdleConnections((long) 15, TimeUnit.SECONDS);
            SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
            socketConfigBuilder.setTcpNoDelay(true);
            httpClientBuilder.setDefaultSocketConfig(socketConfigBuilder.build());
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            requestConfigBuilder.setConnectTimeout(default_connect_timeout);
            requestConfigBuilder.setSocketTimeout(default_socket_timeout);
            httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new SimpleX509TrustManager(null);
            ctx.init(null, new TrustManager[]{tm}, null);
            httpClientBuilder.setSslcontext(ctx);
            httpClientBuilder.setConnectionManagerShared(true);
            default_ssl_client = httpClientBuilder.build();

        } catch (Exception e) {

        }
        return default_ssl_client;
    }
}
