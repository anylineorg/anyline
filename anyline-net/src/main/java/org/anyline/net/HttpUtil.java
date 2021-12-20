/*  
 * Copyright 2006-2020 www.anyline.org
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
package org.anyline.net; 
 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.RandomAccessFile; 
import java.io.UnsupportedEncodingException; 
import java.nio.charset.Charset; 
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
import java.util.concurrent.TimeUnit;

import javax.net.ssl.*;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil; 
import org.apache.http.Header; 
import org.apache.http.HttpEntity; 
import org.apache.http.HttpResponse; 
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
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder; 
import org.apache.http.conn.ssl.SSLContexts; 
import org.apache.http.conn.ssl.TrustStrategy; 
import org.apache.http.conn.ssl.X509HostnameVerifier; 
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder; 
import org.apache.http.impl.client.CloseableHttpClient; 
import org.apache.http.impl.client.HttpClientBuilder; 
import org.apache.http.impl.client.HttpClients; 
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager; 
import org.apache.http.message.BasicNameValuePair; 
import org.apache.http.util.ByteArrayBuffer; 
import org.apache.http.util.EntityUtils; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
/** 
 * 基于hpptclient4.x 
 * 第一个参数用来保持session连接  
 * null或default:整个应用使用同一个session  
 * createClient(name):为不同域名创建各自的session 
 *HttpUtil.post(null, "http://www.anyboot.org", "UTF-8", "name", "zhang", "age", "20"); 
 *HttpUtil.post(HttpUtil.defaultClient(), "http://www.anyboot.org", "UTF-8", "name", "zhang", "age", "20"); 
 *HttpUtil.post(HttpUtil.createClient("deep"), "http://www.anyboot.org", "UTF-8", "name", "zhang", "age", "20"); 
 *HttpUtil.post(null, "http://www.anyboot.org", "UTF-8", "name", "zhang", "age", "20"); 
 * 
 * 
 *HttpEntity entity = new StringEntity(BeanUtil.map2json(map), "UTF-8"); 
 *String txt = HttpUtil.post(url, "UTF-8", entity).getText(); 
 */ 
@SuppressWarnings("deprecation") 
public class HttpUtil { 
	 
	public static String PROTOCOL_TLSV1 = "TLSv1"; 
	private static final Logger log = LoggerFactory.getLogger(HttpUtil.class); 
    private static PoolingHttpClientConnectionManager connManager;   
    private static RequestConfig requestConfig;   
    private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"; 
    private static int MAX_TIMEOUT = 72000; //毫秒 
     
    private static CloseableHttpClient client;
	private static CloseableHttpClient sslClient;
    private Map<String,HttpUtil> instances = new HashMap<String,HttpUtil>();  
     
    static {   
    	MAX_TIMEOUT = ConfigTable.getInt("HTTP_MAX_TIMEOUT", 72000); 
        // 设置连接池   
    	connManager = new PoolingHttpClientConnectionManager();   
        // 设置连接池大小   
    	connManager.setMaxTotal(100);   
    	connManager.setDefaultMaxPerRoute(connManager.getMaxTotal());   
   
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
    public HttpUtil getInstance(){ 
    	return getInstance("default"); 
    } 
    public HttpUtil getInstance(String key){ 
    	if(BasicUtil.isEmpty(key)){ 
    		key = "default"; 
    	} 
    	HttpUtil instance = instances.get(key); 
    	if(null == instance){ 
    		instance = new HttpUtil(); 
    		instances.put(key, instance); 
    	} 
    	 
    	return instance; 
    } 
     
	/*staic*/ 
	public static HttpResult post(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) { 
		return post(client, null, url, encode, entitys); 
	} 
	public static HttpResult post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) { 
		List<HttpEntity> list = new ArrayList<HttpEntity>(); 
		if(null != entitys){ 
			for(HttpEntity entity:entitys){ 
				list.add(entity); 
			} 
		} 
		return post(client, headers, url, encode, list); 
	} 
 
	public static HttpResult post(CloseableHttpClient client, String url, String encode, Map<String, Object> params) { 
		return post(client, null, url, encode, params); 
	} 
	public static HttpResult post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
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
 
 
	public static HttpResult post(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) { 
		if(null == client){ 
			client(url);
		} 
		if(url.startsWith("//")){ 
			url = "http:" + url; 
		} 
		HttpResult result = new HttpResult(); 
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


	/**
	 * post
	 * @param url     url
	 * @param encode endoce
	 * @param entitys new StringEntity(BeanUtil.map2json(params), "UTF-8");
	 * @return HttpResult
	 */
	public static HttpResult post(String url, String encode, HttpEntity... entitys) { 
		return post(client(url), url, encode, entitys);
	} 
	public static HttpResult post(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) { 
		return post(client(url),headers, url, encode, entitys);
	} 
	public static HttpResult post(String url, String encode, Map<String, Object> params) { 
		return post(client(url), url, encode, params);
	} 
	public static HttpResult post(Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
		return post(client(url), headers, url, encode, params);
	} 
	public static HttpResult post(String url,   Map<String, Object> params) { 
		return post(client(url), null, url, "UTF-8", params);
	} 
	public static HttpResult post(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) { 
		return post(client(url),headers, url, encode, entitys);
	} 
 
	public static HttpResult put(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) { 
		return put(client, null, url, encode, entitys); 
	} 
 
	public static HttpResult put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) { 
		List<HttpEntity> list = new ArrayList<HttpEntity>(); 
		if(null != entitys){ 
			for(HttpEntity entity:entitys){ 
				list.add(entity); 
			} 
		} 
		return put(client, headers, url, encode, list); 
	} 
 
	public static HttpResult put(CloseableHttpClient client, String url, String encode, Map<String, Object> params) { 
		return put(client, null, url, encode, params); 
	} 
	public static HttpResult put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
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
 
 
	public static HttpResult put(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) { 
		if(null == client){ 
			client = client(url);
		} 
		if(url.startsWith("//")){ 
			url = "http:" + url; 
		} 
		HttpResult result = new HttpResult(); 
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
	 
 
	public static HttpResult put(String url, String encode, HttpEntity... entitys) { 
		return put(client(url), url, encode, entitys);
	} 
	public static HttpResult put(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) { 
		return put(client(url), headers, url, encode, entitys);
	} 
 
	public static HttpResult put(String url, String encode, Map<String, Object> params) { 
		return put(client(url), url, encode, params);
	} 
	public static HttpResult put(String url, Map<String, Object> params) { 
		return put(client(url), url, "UTF-8", params);
	} 
	public static HttpResult put(Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
		return put(client(url), headers, url, encode, params);
	} 
	public static HttpResult put(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) { 
		return put(client(url), headers, url, encode, entitys);
	} 
	 
 
	public static HttpResult get(CloseableHttpClient client, String url) { 
		return get(client, url, "UTF-8"); 
	} 
	public static HttpResult get(CloseableHttpClient client, String url, String encode) { 
		return get(client, url, encode, new HashMap<String,Object>()); 
	} 
	public static HttpResult get(CloseableHttpClient client, String url, String encode, Map<String, Object> params) { 
		return get(client, null, url, encode, params); 
	} 
 
	public static HttpResult get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
		List<NameValuePair> pairs = packNameValuePair(params); 
		return get(client, headers, url, encode, pairs); 
	} 
 
	public static HttpResult get(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs) { 
		return get(client, null, url, encode, pairs); 
	} 
 
	public static HttpResult get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) { 
		if(null == client){ 
			client = client(url);
		} 
		if(url.startsWith("//")){ 
			url = "http:" + url; 
		} 
		HttpResult result = new HttpResult(); 
		if (null != pairs && !pairs.isEmpty()) { 
			String params = URLEncodedUtils.format(pairs,encode); 
			if (url.contains("?")) { 
				url += "&" + params; 
			} else { 
				url += "?" + params; 
			} 
		} 
		HttpGet method = new HttpGet(url); 
		setHeader(method, headers); 
		result = exe(client, method, encode); 
		return result; 
	} 
	 
 
	public static HttpResult get(String url) { 
		return get(url, "UTF-8"); 
	} 
	public static HttpResult get(String url, String encode) { 
		return get(url, encode, new HashMap<String,Object>()); 
	} 
	public static HttpResult get(String url, String encode, Map<String, Object> params) { 
		return get(client(url), url, encode, params);
	} 
 
	public static HttpResult get(String url,  Map<String, Object> params) { 
		return get(client(url), url, "UTF-8", params);
	} 
 
	public static HttpResult get(Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
		return get(client(url), headers, url, encode, params);
	} 
 
	public static HttpResult get(Map<String, String> headers, String url, String encode) { 
		return get(client(url), headers, url, encode, new HashMap<String,Object>());
	} 
 
	public static HttpResult get(String url, String encode, List<NameValuePair> pairs) { 
		return get(client(url), url, encode, pairs);
	} 
 
	public static HttpResult get(Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) { 
		return get(client(url), headers, url, encode, pairs);
	} 
	 
 
	 
	public static HttpResult delete(CloseableHttpClient client, String url, String encode, Map<String, Object> params) { 
		return delete(client, null, url, encode, params); 
	} 
 
	public static HttpResult delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
		List<NameValuePair> pairs = packNameValuePair(params); 
		return delete(client, headers, url, encode, pairs); 
	} 
 
	public static HttpResult delete(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs) { 
		return delete(client, null, url, encode, pairs); 
	} 
	public static HttpResult delete(CloseableHttpClient client,Map<String, String> headers, String url, String encode, NameValuePair ... pairs) { 
		List<NameValuePair> list = new ArrayList<NameValuePair>(); 
		if(null != pairs){ 
			for(NameValuePair pair:pairs){ 
				list.add(pair); 
			} 
		} 
		return delete(client, headers, url, encode, list); 
	} 
 
	public static HttpResult delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) { 
		if(null == client){ 
			client = client(url);
		} 
		if(url.startsWith("//")){ 
			url = "http:" + url; 
		} 
		HttpResult result = new HttpResult(); 
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
 
 
 
 
	public static HttpResult delete(String url, String encode, Map<String, Object> params) { 
		return delete(client(url), url, encode, params);
	} 
 
	public static HttpResult delete(Map<String, String> headers, String url, String encode, Map<String, Object> params) { 
		return delete(client(url), headers, url, encode, params);
	} 
 
	public static HttpResult delete(String url, String encode, List<NameValuePair> pairs) { 
		return delete(client(url), url, encode, pairs);
	} 
 
	public static HttpResult delete(Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) { 
		return delete(client(url), headers, url, encode, pairs);
	} 
 
 
 
	public static HttpResult delete(Map<String, String> headers, String url, String encode, NameValuePair ... pairs) { 
		return delete(client(url), headers, url, encode, pairs);
	}

	private static HttpResult exe(CloseableHttpClient client, HttpRequestBase method, String encode){
		CloseableHttpResponse response = null; 
		HttpResult result = null; 
		try { 
			long fr = System.currentTimeMillis(); 
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[http request][method:{}][url:{}]",method.getMethod(),method.getURI()); 
			} 
			method.setHeader("Connection", "close");   
			response = client.execute(method); 
			result = parseResult(result,response, encode); 
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[http request][method:{}][status:{}][耗时:{}][url:{}]",method.getMethod(), result.getStatus(), System.currentTimeMillis() - fr, method.getURI()); 
			} 
		} catch (Exception e) { 
			result = new HttpResult(); 
			e.printStackTrace(); 
		} finally {
			try {
				if(null != response){
					response.close();
				}
				method.releaseConnection();
				if(client == HttpUtil.client){
					client.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		return result; 
	} 
 
	/** 
	 * 设置header 
	 *  
	 * @param method  method
	 * @param headers  headers
	 */ 
	private static void setHeader(HttpRequestBase method,  Map<String, String> headers) { 
		if (null != headers) { 
			Iterator<String> keys = headers.keySet().iterator(); 
			while (keys.hasNext()) { 
				String key = keys.next(); 
				String value = headers.get(key); 
				method.setHeader(key, value); 
			} 
		} 
	} 
	public static int status(String url){ 
		int code = -1; 
		CloseableHttpClient client = defaultSSLClient(); 
		CloseableHttpResponse response = null; 
		if(url.startsWith("//")){ 
			url = "http:" + url; 
		} 
		HttpGet method = new HttpGet(url); 
		try { 
			response = client.execute(method); 
			code = response.getStatusLine().getStatusCode(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		}finally { 
			try { 
				response.close(); 
				method.releaseConnection(); 
				client.close(); 
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
		} 
		return code; 
	} 
	private static HttpResult parseResult(HttpResult result, CloseableHttpResponse response, String encode) {
		if (null == result) {
			result = new HttpResult();
		} 
		try {
			if(null != response){ 
				Map<String, String> headers = new HashMap<String, String>(); 
				Header[] all = response.getAllHeaders(); 
				for (Header header : all) { 
					String key = header.getName(); 
					String value = header.getValue(); 
					headers.put(key, value); 
					if ("Set-Cookie".equalsIgnoreCase(key)) { 
						HttpCookie c = new HttpCookie(value);
						result.setCookie(c);
					} 
				} 
				int code = response.getStatusLine().getStatusCode();
				result.setHeaders(headers);
				result.setStatus(code);
				if(code ==200){ 
					HttpEntity entity = response.getEntity(); 
					if (null != entity) {
						result.setInputStream(entity.getContent());
						String text = EntityUtils.toString(entity, encode);
						result.setText(text);
					} 
				} 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return result;
	} 
 
	public static DownloadTask download(String url, String dst){ 
		File file = new File(dst); 
		return download(client(url), new DefaultProgress(url, file), url, file, null, null, false);
	} 
	public static DownloadTask download(String url, File dst){ 
		return download(client(url), new DefaultProgress(url, dst), url, dst, null, null, false);
	} 
	public static DownloadTask download(String url, String dst, Map<String,String> headers,Map<String,Object> params){ 
		File file = new File(dst); 
		return download(client(url), new DefaultProgress(url, file), url, file, headers, params, false);
	} 
	public static DownloadTask download(String url, File dst, Map<String,String> headers,Map<String,Object> params){ 
		return download(client(url), new DefaultProgress(url, dst), url, dst, headers, params, false);
	} 
	public static DownloadTask download(String url, String dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		File file = new File(dst); 
		return download(client(url), new DefaultProgress(url, file), url, file, headers, params, override);
	} 
	public static DownloadTask download(String url, File dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		return download(client(url), new DefaultProgress(url, dst), url, dst, headers, params, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, String dst, boolean override){ 
		return download(client(url), progress, url, new File(dst), null, null, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, File dst, boolean override){ 
		return download(client(url), progress, url, dst, null, null, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, String dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		return download(client(url), progress, url, new File(dst), headers, params, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, File dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		return download(client(url), progress, url, dst, headers, params, override);
	} 
	public static DownloadTask download(CloseableHttpClient client, DownloadProgress progress, String url, File dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		DownloadTask task = new DownloadTask(); 
		task.setProgress(progress); 
		task.setLocal(dst); 
		task.setUrl(url); 
		task.setHeaders(headers); 
		task.setParams(params); 
		task.setOverride(override); 
		download(client, task); 
		return task; 
	} 
 
	public static boolean download(DownloadTask task){ 
		return download(client(task.getUrl()), task);
	} 
	public static boolean download(CloseableHttpClient client, DownloadTask task){ 
		boolean result = false; 
		String url = task.getUrl(); 
		String finalUrl = url; 
		if(null != finalUrl && finalUrl.startsWith("//")){ 
			finalUrl = "http:"+url; 
		} 
 
		finalUrl = mergeParam(finalUrl, task.getParams()); 
		//DownloadProgress progress = task.getProgress(); 
		File dst = task.getLocal(); 
		if(BasicUtil.isEmpty(url) || BasicUtil.isEmpty(dst)){ 
			return result; 
		} 
		long past = 0; 
		long length = 0; 
		boolean override = task.isOverride(); 
		if(dst.exists() && !override){ 
			past = dst.length(); 
			task.init(length, past); 
			task.finish(); 
//			progress.init(url, "", length, past); 
//			progress.finish(url, ""); 
			return true; 
		} 
		File parent = dst.getParentFile(); 
		if(null != parent && !parent.exists()){ 
			parent.mkdirs(); 
		} 
		HttpGet get = new HttpGet(finalUrl); 
		get.setConfig(requestConfig); 
		Map<String,String> headers = task.getHeaders(); 
		if(null != headers){ 
			for(String key:headers.keySet()){ 
				get.setHeader(key, headers.get(key)); 
			} 
		} 
		RandomAccessFile raf = null; 
		InputStream is = null; 
		File tmpFile = new File(dst.getParent(), dst.getName()+".downloading"); 
		File configFile = new File(dst.getParent(), dst.getName()+".downloading.config"); 
		String config = ""; 
		if(configFile.exists()){ 
			config = FileUtil.read(configFile).toString(); 
		}else{ 
		} 
/* 
0,100,40  开始，结束，已完成 
101,200 
 表示头500个字节：bytes=0-499   
表示第二个500字节：bytes=500-999   
表示最后500个字节：bytes=-500   
表示500字节以后的范围：bytes=500-   
第一个和最后一个字节：bytes=0-0,-1   
同时指定几个范围：bytes=500-600,601-999 
 */ 
		long start=0; 
		if(tmpFile.exists()){//继上次进度下载 
			start = tmpFile.length(); 
		} 
		String range = "bytes=" + start + "-"; 
		get.setHeader("Range", range); 
		 
		try { 
		    HttpResponse response = client.execute(get); 
		    int code = response.getStatusLine().getStatusCode(); 
		    if(code == 416){ 
		    	get.removeHeaders("Range"); 
		    	response = client.execute(get); 
		    	code = response.getStatusLine().getStatusCode(); 
		    	log.warn("[http download][断点设置异常][url:{}]",url); 
		    } 
		    if(code != 200 && code !=206){ 
				//progress.error(url, "", code, "状态异常"); 
				task.error(code, "状态异常"); 
		        return false; 
		    } 
		    HttpEntity entity = response.getEntity(); 
		    if(entity != null) { 
			    long total = entity.getContentLength(); 
				//progress.init(url, "", total, start); 
				task.init(total, past); 
			    int buf = 1024*1024*10; 
			    if(buf > total){ 
			    	buf = (int)total; 
			    } 
		        is = entity.getContent(); 
		        raf = new RandomAccessFile(tmpFile, "rwd"); 
		        raf.seek(start); 
		         
		        byte[] buffer = new byte[buf]; 
		        int len = -1; 
		        while((len = is.read(buffer) )!= -1){ 
		        	if(task.getAction() !=1){ 
		    	    	log.warn("[http download][break][url:{}]",url); 
		        		break; 
		        	} 
		        	raf.write(buffer, 0, len); 
		        	 
		        	//progress.step(url, "", len); 
		        	task.step(len); 
		        } 
		    } 
		    result = true; 
		} catch (Exception e) { 
			//progress.error(url, "", 0, e.getMessage()); 
			task.error(-1, e.getMessage()); 
	    	log.warn("[http download][下载异常][url:{}]",url); 
		    e.printStackTrace(); 
		}finally{ 
			if(null != raf){ 
				try{ 
			        raf.close(); 
				}catch(Exception e){ 
					e.printStackTrace(); 
				} 
			} 
			if(null != is){ 
				try{ 
			        is.close(); 
				}catch(Exception e){ 
					e.printStackTrace(); 
				} 
			} 
		    try { 
		        client.close(); 
		    } catch (IOException e) { 
		        e.printStackTrace(); 
		    } 
		} 
		if(result){ 
			tmpFile.renameTo(dst); 
	        //progress.finish(url, ""); 
			task.finish(); 
		} 
		return result; 
	} 
	public static HttpResult upload(String url, Map<String,File> files, String encode, Map<String,String> headers, Map<String,Object> params){ 
		return upload(client(url), url, files, encode, headers, params);
	} 
	public static HttpResult upload(String url, Map<String,File> files, Map<String,String> headers, Map<String,Object> params){ 
		return upload(url, files, "UTF-8", headers, params); 
	} 
	public static HttpResult upload(String url, Map<String, File> files,  Map<String, Object> params) { 
		return upload( url, files, null, params); 
	} 
	public static HttpResult upload(String url, Map<String, File> files) { 
		return upload(url, files, null, null); 
	} 
	public static HttpResult upload(CloseableHttpClient client, String url, Map<String,File> files, String encode, Map<String,String> headers, Map<String,Object> params) { 
		if(null != url && url.startsWith("//")){ 
			url = "http:"+url; 
		} 
        if(BasicUtil.isEmpty(encode)){ 
        	encode = "UTF-8"; 
        } 
        String BOUNDARY="-----"+BasicUtil.getRandomLowerString(20);  //设置边界 
		MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532); 
		HttpPost post = new HttpPost(url); 
		post.setConfig(requestConfig); 
        builder.setBoundary(BOUNDARY); 
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);  //浏览器兼容模式 
        builder.setCharset(Charset.forName(encode));  //设置字符编码集 
        ContentType contentType = ContentType.create("text/plain",Charset.forName(encode)); 
 
		mergeParam(builder, params, contentType); 
		if(null != headers){ 
			for(String key:headers.keySet()){ 
				post.setHeader(key, headers.get(key)); 
			} 
		} 
		String fileLog = ""; 
        for(String key:files.keySet()){ 
			File file = files.get(key); 
				builder.addBinaryBody(key, file, ContentType.MULTIPART_FORM_DATA, file.getName()); 
			fileLog += "["+key+":"+file.getAbsolutePath()+"]"; 
		} 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[http upload][url:{}]"+fileLog,url); 
		} 
		 
        HttpEntity entity = builder.build();// 生成 HTTP POST 实体 
        post.setEntity(entity);   //post 实体。 
        post.addHeader("Content-Type", "multipart/form-data;boundary="+ BOUNDARY);  //表单形式。 
        HttpResult source = exe(client, post, encode); 
		return source; 
     }


	/*staic*/
	public static HttpResult postStream(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) {
		return postStream(client, null, url, encode, entitys);
	}
	public static HttpResult postStream(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		List<HttpEntity> list = new ArrayList<HttpEntity>();
		if(null != entitys){
			for(HttpEntity entity:entitys){
				list.add(entity);
			}
		}
		return postStream(client, headers, url, encode, list);
	}

	public static HttpResult postStream(CloseableHttpClient client, String url, String encode, Map<String, Object> params) {
		return postStream(client, null, url, encode, params);
	}
	public static HttpResult postStream(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, Object> params) {
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

		return postStream(client, headers, url, encode, entitys);
	}


	public static HttpResult postStream(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		HttpResult result = new HttpResult();
		InputStream is = null;
		if(null == client){
			client = client(url);
		}
		if(url.startsWith("//")){
			url = "http:" + url;
		}
		HttpPost method = new HttpPost(url);
		if(null != entitys){
			for(HttpEntity entity:entitys){
				method.setEntity(entity);
			}
		}
		setHeader(method, headers);
		try {
			CloseableHttpResponse response = client.execute(method);
			is = response.getEntity().getContent();
			result.setInputStream(is);
		}catch(Exception e){

		}
		return result;
	}


	/**
	 * postStream
	 * @param url     url
	 * @param encode endoce
	 * @param entitys new StringEntity(BeanUtil.map2json(params), "UTF-8");
	 * @return InputStream
	 */
	public static HttpResult postStream(String url, String encode, HttpEntity... entitys) {
		return postStream(client(url), url, encode, entitys);
	}
	public static HttpResult postStream(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		return postStream(client(url),headers, url, encode, entitys);
	}
	public static HttpResult postStream(String url, String encode, Map<String, Object> params) {
		return postStream(client(url), url, encode, params);
	}
	public static HttpResult postStream(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return postStream(client(url), headers, url, encode, params);
	}
	public static HttpResult postStream(String url,   Map<String, Object> params) {
		return postStream(client(url), null, url, "UTF-8", params);
	}
	public static HttpResult postStream(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		return postStream(client(url),headers, url, encode, entitys);
	}

	public static String read(InputStream is, String encode) {
		if (is == null) {
			return null;
		}
		ByteArrayBuffer bab = new ByteArrayBuffer(0);
		byte[] b = new byte[1024];
		int len = 0;
		try {
			while ((len = is.read(b)) != -1) {
				bab.append(b, 0, len);
			}
			return new String(bab.toByteArray(), encode);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	/** 
	 * 提取url根目录 
	 *  
	 * @param url  url
	 * @return return
	 */
	public static String host(String url) {
		if(null == url){
			return null;
		}
		String str = url.replaceAll("http://", "").replaceAll("https://", "").replaceAll("//", "");
		int idx = str.indexOf("/");
		if (idx != -1) {
			str = str.substring(0, idx);
		}
		if(url.startsWith("https")){
			return "https://" + str;
		}else if(url.startsWith("//")){
			return "//" + str;
		}else {
			return "http://" + str;
		}
	}
	public static String domain(String url) {
		if(null == url){
			return null;
		}
		url = url.replace("http://","").replace("https://","");
		if (url.contains(":")){
			url = url.substring(0, url.indexOf(":"));
		}
		if(url.contains("/")){
			url = url.substring(0, url.indexOf("/"));
		}
		return url;
	}
	/** 
	 * 从URL中提取文件目录(删除查询参数) 
	 *  
	 * @param url  url
	 * @return return
	 */ 
	public static String parsePath(String url) { 
		int to = url.indexOf("?"); 
		if (to != -1) 
			url = url.substring(0, to); 
		return url; 
	} 
 
	/** 
	 * 提取一个URL所在的目录 
	 *  
	 * @param url  url
	 * @return return
	 */ 
	public static String parseDir(String url) { 
		String dir = null; 
		if(null == url){ 
			return dir; 
		} 
		if (url.endsWith("/")) { 
			dir = url; 
		} else if (isHttpFile(url)) { 
			int to = url.lastIndexOf("/"); 
			dir = url.substring(0, to); 
		} else { 
			dir = url; 
		} 
		return dir; 
	} 
	/** 
	 * 提取一个URL指向的文件名 
	 *  
	 * @param url  url
	 * @return return
	 */ 
	public static String parseFileName(String url) { 
		String name = null; 
		if(null != url){ 
			url = url.replace("://", ""); 
			if(!url.endsWith("/")){ 
				name = url.substring(url.lastIndexOf("/")+1); 
				if(name.contains("?")){ 
					name = name.substring(0, name.indexOf("?")); 
				} 
			} 
		} 
		return name; 
	} 
 
	/** 
	 * path是否包含文件名 
	 * @param path  path
	 * @return return
	 */ 
	private static boolean isHttpFile(String path) { 
 
		if (path.endsWith("/")) { 
			return false; 
		} 
		String head = "http://"; 
		int fr = head.length(); 
		int l1 = path.lastIndexOf("/"); 
		int l2 = path.lastIndexOf("."); 
		if (l1 == -1) { 
			return false; 
		} else if (l2 > l1 && l2 > fr) { 
			return true; 
		} 
		return false; 
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
							log.warn("[request param][{}={}]", key,BasicUtil.cut(val,0,20)); 
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
 	private static CloseableHttpClient client(String url){
		if(url.contains("https://")){
			return defaultSSLClient();
		}else{
			return defaultClient();
		}
	}
	private static CloseableHttpClient defaultClient(){
		HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig); 
		builder.setUserAgent(userAgent); 
		client = builder.build(); 
		return client; 
	} 
	public static CloseableHttpClient createClient(String userAgent){ 
		CloseableHttpClient client = null; 
		HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig); 
		builder.setUserAgent(userAgent); 
		client = builder.build(); 
		return client; 
	} 
	public static CloseableHttpClient createClient(){ 
		CloseableHttpClient client = null; 
		HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig); 
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
			//ALLOW_ALL_HOSTNAME_VERIFIER  关闭host验证，允许和所有的host建立SSL通信                  
			//BROWSER_COMPATIBLE_HOSTNAME_VERIFIER  和浏览器兼容的验证策略，即通配符能够匹配所有子域名
			//STRICT_HOSTNAME_VERIFIER  严格匹配模式，hostname必须匹配第一个CN或者任何一个subject-alts
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,protocols,null,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build(); 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
		return httpclient; 
	} 
	public static CloseableHttpClient ceateSSLClient(File keyFile, String password){ 
		return ceateSSLClient(keyFile, HttpUtil.PROTOCOL_TLSV1, password); 
	}
    public static String mergePath(String ... paths){ 
		String result = null; 
		String separator = "/"; 
		if(null != paths){ 
			for(String path:paths){ 
				if(BasicUtil.isEmpty(path)){ 
					continue; 
				} 
				path = path.replace("\\", "/"); 
				if(null == result){ 
					result = path; 
				}else{ 
					if(result.endsWith("/")){ 
						if(path.startsWith("/")){ 
							// "root/" + "/sub"  
							result += path.substring(1); 
						}else{ 
							// "root/" + "sub" 
							result += path; 
						} 
					}else{ 
						if(path.startsWith("/")){ 
							// "root" + "/sub"  
							result += path; 
						}else{ 
							// "root" + "sub" 
							result += separator + path; 
						} 
					} 
				} 
			} 
		} 
		return result; 
	} 
	/** 
	 * 创建完整HTTP路径 
	 * @param host  host
	 * @param url  url
	 * @return return
	 */ 
	public static String createFullPath(String host, String url) { 
		if (url.startsWith("http") || url.startsWith("//") || BasicUtil.isEmpty(host)){ 
			return url; 
		} 
		String fullPath = null; 
		if (url.startsWith("/")) {// 当前站点的绝对路径 
			fullPath = host(host) + url;
		} else if (url.startsWith("?")) {// 查询参数 
			fullPath = parsePath(host) + url; 
		} else {// 当前站点的相对路径 
			host = parseDir(host); 
			if (host.endsWith("/")) { // src是一个目录 
				fullPath = host + url; 
			} else { // src有可能是一个文件 : 需要判断是文件还是目录 文件比例多一些 
				fullPath = host + "/" + url; 
			} 
		} 
		return fullPath; 
	} 
	public static void setUserAgent(String agent){ 
		HttpUtil.userAgent = agent; 
	}

	public static CloseableHttpClient defaultSSLClient(){
		try {
			if (sslClient!=null){
				return sslClient;
			}

			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			httpClientBuilder.setMaxConnTotal(10000);
			httpClientBuilder.setMaxConnPerRoute(1000);

			httpClientBuilder.evictIdleConnections((long) 15, TimeUnit.SECONDS);
			SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
			socketConfigBuilder.setTcpNoDelay(true);
			httpClientBuilder.setDefaultSocketConfig(socketConfigBuilder.build());
			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
			requestConfigBuilder.setConnectTimeout(30000);
			requestConfigBuilder.setSocketTimeout(30000);
			httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new SimpleX509TrustManager(null);
			ctx.init(null, new TrustManager[]{tm}, null);
			httpClientBuilder.setSslcontext(ctx);
			httpClientBuilder.setConnectionManagerShared(true);
			sslClient = httpClientBuilder.build();

		} catch (Exception e) {

		}
		return sslClient;
	}
}
