/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.net;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.util.BeanUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpBuilder {
    private static final Log log = LogProxy.get(HttpBuilder.class);
    private CloseableHttpClient client;
    private Map<String, String> headers = new HashMap<>();
    private HttpEntity entity;
    private Map<String, Object> params = new HashMap<>();
    private List<NameValuePair> pairs = new ArrayList<>();
    private String userAgent;
    private String url;
    private String charset = "UTF-8";
    private DownloadTask task;
    private Map<String, Object> files;    //可以是文件或byte[]
    private String returnType = "text";
    private static int default_connect_timeout = 72000; // 毫秒
    private static int default_socket_timeout = 72000;

    public HttpClient build() {
        HttpClient client = new HttpClient();
        try {
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .register("https",ignoreSSL()).build();
            PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            manager.setMaxTotal(100);
            manager.setDefaultMaxPerRoute(20);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(default_socket_timeout) // 设置读取超时为5000毫秒（5秒）
                    .setConnectTimeout(default_connect_timeout) // 设置连接超时为5000毫秒（5秒）
                    .setConnectionRequestTimeout(5000) // 设置从连接池获取连接的等待超时为5000毫秒（5秒）
                    .build();
            this.client = HttpClients.custom()
                    .setConnectionManager(manager)
                    .setDefaultRequestConfig(requestConfig).build();
        }catch (Exception e) {
            log.error("build http client exception:", e);
        }
        client.setClient(this.client);
        if(null != headers && !headers.isEmpty()) {
            client.setHeaders(headers);
        }
        client.setEntity(entity);
        client.setParams(params);
        client.setPairs(pairs);
        if(null != userAgent) {
            client.setUserAgent(userAgent);
        }
        client.setUrl(url);
        if(null != charset) {
            client.setEncode(charset);
        }
        client.setTask(task);
        client.setFiles(files);
        client.setReturnType(returnType);
        return client;
    }
    /**
     * 绕过验证
     * @return SSLConnectionSocketFactory
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLConnectionSocketFactory ignoreSSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
                }
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sc.init(null, new TrustManager[] { trustManager }, null);
            sslsf = new SSLConnectionSocketFactory(sc, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }
        return sslsf;
    }
    /**
     * 解析url 识别出参数k=v&amp;k=v
     * @param url url
     * @return HttpBuilder
     */
    public static HttpBuilder parse(String url) {
        HttpBuilder builder = new HttpBuilder();
        builder.setUrl(url);
        if(null != url) {
            String base = null;
            String sub = null;
            int idx_session = url.indexOf(";");
            int idx_param = url.indexOf("?");
            if(idx_param > 0) {
                if (idx_session > 0 && idx_session < idx_param) {
                    //jsessionid
                    base = url.substring(0, idx_session);
                    sub = url.substring(url.indexOf("?") + 1);
                } else {
                    base = url.substring(0, url.indexOf("?"));
                    sub = url.substring(url.indexOf("?") + 1);
                }
                builder.setUrl(base);
            }
            if(null != sub) {
                String[] ps = sub.split("&");
                for(String p:ps) {
                    if(p.contains("=")) {
                        String[] kv = p.split("=");
                        if(kv.length == 2) {
                            builder.setParam(kv[0], kv[1]);
                        }
                    }
                }
            }
        }
        return builder;
    }

    public static HttpBuilder init() {
        return new HttpBuilder();
    }
    public static HttpBuilder init(String url) {
        return new HttpBuilder().setUrl(url);
    }
    public static HttpBuilder init(CloseableHttpClient client, String url) {
        return new HttpBuilder().setClient(client).setUrl(url);
    }
    public static HttpBuilder init(CloseableHttpClient client) {
        return new HttpBuilder().setClient(client);
    }
    public HttpBuilder setContentType(String type) {
        headers.put("Content-Type", type);
        return this;
    }
    public HttpBuilder setClient(CloseableHttpClient client) {
        this.client = client;
        return this;
    }
    public HttpBuilder setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }
    public HttpBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }
    public HttpBuilder setUrl(String url) {
        this.url = url;
        return this;
    }
    public HttpBuilder setCharset(String charset) {
        this.charset = charset;
        return this;
    }
    public HttpBuilder setEntity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }
    public HttpBuilder clearHeader() {
        headers.clear();
        return this;
    }
    public HttpBuilder setEntity(String entity) {
        try {
            this.entity = new StringEntity(entity, charset);
        }catch (Exception e) {
            log.error("create string entity exception:", e);
        }
        return this;
    }
    public HttpBuilder setEntity(Map<String, ?> map) {
        try {
            entity = new StringEntity(BeanUtil.map2json(map), charset);
        }catch (Exception e) {
            log.error("create string entity exception:", e);
        }
        return this;
    }
    public HttpBuilder setPairs(List<NameValuePair> pairs) {
        this.pairs = pairs;
        return this;
    }
    public HttpBuilder addPair(List<NameValuePair> pairs) {
        this.pairs = pairs;
        return this;
    }
    public HttpBuilder addDownloadTask(DownloadTask task) {
        this.task = task;
        return this;
    }
    public HttpBuilder setUploadFiles(Map<String, Object> files) {
        this.files = files;
        return this;
    }
    public HttpBuilder addUploadFiles(String key, File file) {
        if(null == files) {
            files = new HashMap<>();
        }
        files.put(key, file);
        return this;
    }
    public HttpBuilder addUploadFiles(String key, byte[] file) {
        if(null == files) {
            files = new HashMap<>();
        }
        files.put(key, file);
        return this;
    }
    public HttpBuilder setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }
    public HttpBuilder addParam(String key, String value) {
        params.put(key, value);
        return this;
    }
    public HttpBuilder setReturnType(String type) {
        this.returnType = type;
        return this;
    }
    public HttpBuilder setUserAgent(String agent) {
        this.userAgent = agent;
        return this;
    }
    public Object getParam(String key) {
        if(null != params) {
            return params.get(key);
        }else{
            return null;
        }
    }
    public HttpBuilder setParam(String key, Object value) {
        if(null == params) {
            params = new HashMap<>();
        }
        Object param = params.get(key);
        if(null != param && null != value) {
            List list = null;
            if(param instanceof List) {
                list = (List)param;
            }else{
                list = new ArrayList();
            }
            if(value instanceof List) {
                list.addAll((List)value);
            }else{
                list.add(value);
            }
            params.put(key, list);
        }else{
            params.put(key, value);
        }
        return this;
    }

}
