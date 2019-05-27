/* 
 * Copyright 2006-2015 www.anyline.org
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
 *
 *          
 */
package org.anyline.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
/**
 * 基于hpptclient4.x
 * 第一个参数用来保持session连接 
 * null或default:整个应用使用同一个session 
 * createClient(name):为不同域名创建各自的session
 *HttpClientUtil.post(null, "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *HttpClientUtil.post(HttpClientUtil.defaultClient(), "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *HttpClientUtil.post(HttpClientUtil.createClient("deep"), "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *HttpClientUtil.post(null, "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *
 *
 *HttpEntity entity = new StringEntity(BeanUtil.map2json(map), "UTF-8");
 *String txt = HttpClientUtil.post(url, "UTF-8", entity).getText();
 */
public class HttpClientUtil {
	
	public static String PROTOCOL_TLSV1 = "TLSv1";
	private static final Logger log = Logger.getLogger(HttpClientUtil.class);
	private static Map<String, CloseableHttpClient> clients = new HashMap<String,CloseableHttpClient>();
    private static PoolingHttpClientConnectionManager connMgr;  
    private static RequestConfig requestConfig;  
    private static final int MAX_TIMEOUT = 7200; 
    
    static {  
        // 设置连接池  
        connMgr = new PoolingHttpClientConnectionManager();  
        // 设置连接池大小  
        connMgr.setMaxTotal(100);  
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());  
  
        RequestConfig.Builder configBuilder = RequestConfig.custom();  
        // 设置连接超时  
        configBuilder.setConnectTimeout(MAX_TIMEOUT);  
        // 设置读取超时  
        configBuilder.setSocketTimeout(MAX_TIMEOUT);  
        // 设置从连接池获取连接实例的超时  
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);  
        // 在提交请求之前 测试连接是否可用  
        configBuilder.setStaleConnectionCheckEnabled(true);  
        requestConfig = configBuilder.build();  
    }
	public static Source post(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) {
		return post(client, null, url, encode, entitys);
	}
	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		List<HttpEntity> list = new ArrayList<HttpEntity>();
		if(null != entitys){
			for(HttpEntity entity:entitys){
				list.add(entity);
			}
		}
		return post(client, headers, url, encode, list);
	}

	public static Source post(CloseableHttpClient client, String url, String encode, Map<String, Object> params) {
		return post(client, null, url, encode, params);
	}
	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		List<HttpEntity> entitys = new ArrayList<HttpEntity>();
		if(null != params && !params.isEmpty()){
			List<NameValuePair> pairs = packNameValuePair(params);
			try {
				HttpEntity entity = new UrlEncodedFormEntity(pairs, encode);
				entitys.add(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return post(client, headers, url, encode, entitys);
	}


	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		HttpPost method = new HttpPost(url);
		if(null != entitys){
			for(HttpEntity entity:entitys){
				method.setEntity(entity);
			}
		}
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}


	public static Source post(String url, String encode, HttpEntity... entitys) {
		return post(defaultClient(), url, encode, entitys);
	}
	public static Source post(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		return post(defaultClient(),headers, url, encode, entitys);
	}
	public static Source post(String url, String encode, Map<String, Object> params) {
		return post(defaultClient(), url, encode, params);
	}
	public static Source post(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return post(defaultClient(), headers, url, encode, params);
	}
	public static Source post(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		return post(defaultClient(),headers, url, encode, entitys);
	}

	public static Source put(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) {
		return put(client, null, url, encode, entitys);
	}

	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		List<HttpEntity> list = new ArrayList<HttpEntity>();
		if(null != entitys){
			for(HttpEntity entity:entitys){
				list.add(entity);
			}
		}
		return put(client, headers, url, encode, list);
	}

	public static Source put(CloseableHttpClient client, String url, String encode, Map<String, Object> params) {
		return put(client, null, url, encode, params);
	}
	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		List<HttpEntity> entitys = new ArrayList<HttpEntity>();
		if(null != params && !params.isEmpty()){
			List<NameValuePair> pairs = packNameValuePair(params);
			try {
				HttpEntity entity = new UrlEncodedFormEntity(pairs, encode);
				entitys.add(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return put(client, headers, url, encode, entitys);
	}


	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		HttpPut method = new HttpPut(url);
		if(null != entitys){
			for(HttpEntity entity:entitys){
				method.setEntity(entity);
			}
		}
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}
	

	public static Source put(String url, String encode, HttpEntity... entitys) {
		return put(defaultClient(), url, encode, entitys);
	}
	public static Source put(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		return put(defaultClient(), headers, url, encode, entitys);
	}

	public static Source put(String url, String encode, Map<String, Object> params) {
		return put(defaultClient(), url, encode, params);
	}
	public static Source put(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return put(defaultClient(), headers, url, encode, params);
	}
	public static Source put(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		return put(defaultClient(), headers, url, encode, entitys);
	}
	

	public static Source get(CloseableHttpClient client, String url) {
		return get(client, url, "UTF-8");
	}
	public static Source get(CloseableHttpClient client, String url, String encode) {
		return get(client, url, encode, new HashMap<String,Object>());
	}
	public static Source get(CloseableHttpClient client, String url, String encode, Map<String, Object> params) {
		return get(client, null, url, encode, params);
	}

	public static Source get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		List<NameValuePair> pairs = packNameValuePair(params);
		return get(client, headers, url, encode, pairs);
	}

	public static Source get(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs) {
		return get(client, null, url, encode, pairs);
	}

	public static Source get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		if (null != pairs && !pairs.isEmpty()) {
			String params = URLEncodedUtils.format(pairs,encode);
			if (url.contains("?")) {
				url += "&" + params;
			} else {
				url += "?" + params;
			}
		}
		if(ConfigTable.isDebug()){
			log.warn("[HTTP GET][url:"+url+"]");
		}
		HttpGet method = new HttpGet(url);
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}
	

	public static Source get(String url) {
		return get(url, "UTF-8");
	}
	public static Source get(String url, String encode) {
		return get(url, encode, new HashMap<String,Object>());
	}
	public static Source get(String url, String encode, Map<String, Object> params) {
		return get(defaultClient(), url, encode, params);
	}

	public static Source get(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return get(defaultClient(), headers, url, encode, params);
	}

	public static Source get(Map<String, String> headers, String url, String encode) {
		return get(defaultClient(), headers, url, encode, new HashMap<String,Object>());
	}

	public static Source get(String url, String encode, List<NameValuePair> pairs) {
		return get(defaultClient(), url, encode, pairs);
	}

	public static Source get(Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		return get(defaultClient(), headers, url, encode, pairs);
	}
	

	
	public static Source delete(CloseableHttpClient client, String url, String encode, Map<String, Object> params) {
		return delete(client, null, url, encode, params);
	}

	public static Source delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		List<NameValuePair> pairs = packNameValuePair(params);
		return delete(client, headers, url, encode, pairs);
	}

	public static Source delete(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs) {
		return delete(client, null, url, encode, pairs);
	}
	public static Source delete(CloseableHttpClient client,Map<String, String> headers, String url, String encode, NameValuePair ... pairs) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		if(null != pairs){
			for(NameValuePair pair:pairs){
				list.add(pair);
			}
		}
		return delete(client, headers, url, encode, list);
	}

	public static Source delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		if (null != pairs) {
			String params = URLEncodedUtils.format(pairs,encode);
			if (url.contains("?")) {
				url += "&" + params;
			} else {
				url += "?" + params;
			}
		}
		HttpDelete method = new HttpDelete(url);
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}




	public static Source delete(String url, String encode, Map<String, Object> params) {
		return delete(defaultClient(), url, encode, params);
	}

	public static Source delete(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return delete(defaultClient(), headers, url, encode, params);
	}

	public static Source delete(String url, String encode, List<NameValuePair> pairs) {
		return delete(defaultClient(), url, encode, pairs);
	}

	public static Source delete(Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		return delete(defaultClient(), headers, url, encode, pairs);
	}



	public static Source delete(Map<String, String> headers, String url, String encode, NameValuePair ... pairs) {
		return delete(defaultClient(), headers, url, encode, pairs);
	}

	
	private static Source exe(CloseableHttpClient client, HttpRequestBase method, String encode){
		CloseableHttpResponse response = null;
		Source result = null;
		try {
			long fr = System.currentTimeMillis();
			method.setHeader("Connection", "close");  
			if(ConfigTable.isDebug()){
				log.warn("[Http Request][URL:"+method.getURI()+"]");
			}
			response = client.execute(method);
			result = getResult(result,response, encode);
			if(ConfigTable.isDebug()){
				log.warn("[Http Request][耗时:"+(System.currentTimeMillis() - fr)+"][URL:"+method.getURI()+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
				method.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 设置header
	 * 
	 * @param method
	 * @param headers
	 */
	private static void setHeader(HttpRequestBase method,
			Map<String, String> headers) {
		if (null != headers) {
			Iterator<String> keys = headers.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = headers.get(key);
				method.setHeader(key, value);
			}
		}
	}

	public static Source getResult(Source src, CloseableHttpResponse response,
			String encode) {
		if (null == src) {
			src = new Source();
		}
		try {
			Map<String, String> headers = new HashMap<String, String>();
			Header[] all = response.getAllHeaders();
			for (Header header : all) {
				String key = header.getName();
				String value = header.getValue();
				headers.put(key, value);
				if ("Set-Cookie".equalsIgnoreCase(key)) {
					HttpCookie c = new HttpCookie(value);
					src.setCookie(c);
				}
			}
			src.setHeaders(headers);
			src.setStatus(response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			if (null != entity) {
				String text = EntityUtils.toString(entity, encode);
				src.setText(text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return src;
	}
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
						if(ConfigTable.isDebug()){
							log.warn("[Request Param][" + key + "=" + BasicUtil.cut(val,0,20) + "]");
						}						
					}
				}else if(value instanceof Collection){
					Collection vals = (Collection)value;
					for(Object val:vals){
						if(null == val){
							continue;
						}
						pairs.add(new BasicNameValuePair(key, val.toString()));
						if(ConfigTable.isDebug()){
							log.warn("[Request Param][" + key + "=" + BasicUtil.cut(val.toString(),0,20) + "]");
						}						
					}
				}else if(null != value){
					pairs.add(new BasicNameValuePair(key, value.toString()));
					if(ConfigTable.isDebug()){
						log.warn("[Request Param][" + key + "=" + BasicUtil.cut(value.toString(),0,20) + "]");
					}
				}
			}
		}
		return pairs;
	}

	public static CloseableHttpClient defaultClient(){
		return createClient("default");
	}
	public static CloseableHttpClient createClient(String key){
		CloseableHttpClient client = clients.get(key);
		if(null == client){
			client = HttpClients.createDefault();
			clients.put(key, client);
			if(ConfigTable.isDebug()){
				log.warn("[创建Http Client][KEY:"+key+"]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[Http Client缓存][KEY:"+key+"]");
			}
		}
		return client;
	}
	
	public static CloseableHttpClient defaultSSLClient(){
		return ceateSSLClient("default");
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
	        // Trust own CA and all self-signed certs
	        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, password.toCharArray()).build();
	        // Allow TLSv1 protocol only
	        String[] protocols = new String[] {protocol};
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,protocols,null,SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	        httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		}catch(Exception e){
			e.printStackTrace();
		}
		return httpclient;
	}
	public static CloseableHttpClient ceateSSLClient(File keyFile, String password){
		return ceateSSLClient(keyFile, HttpClientUtil.PROTOCOL_TLSV1, password);
	}
	public static CloseableHttpClient ceateSSLClient(String key){
		key = "SSL:"+key;
		CloseableHttpClient client = clients.get(key);
		if(null == client){
			client = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
			clients.put(key, client);
			if(ConfigTable.isDebug()){
				log.warn("[创建Https Client][KEY:"+key+"]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[Https Client缓存][KEY:"+key+"]");
			}
		}
		 return client;
	}
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {  
        SSLConnectionSocketFactory sslsf = null;  
        try {  
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {  
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
                    return true;  
                }  
            }).build();  
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {  
  
                public boolean verify(String arg0, SSLSession arg1) {  
                    return true;  
                }  
  
                public void verify(String host, SSLSocket ssl) throws IOException {  
                }  
  
                public void verify(String host, X509Certificate cert) throws SSLException {  
                }  
  
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {  
                }  
            });  
        } catch (GeneralSecurityException e) {  
            e.printStackTrace();  
        }  
        return sslsf;  
    }  
}
