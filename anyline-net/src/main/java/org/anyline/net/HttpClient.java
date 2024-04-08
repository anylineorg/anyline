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

package org.anyline.net;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.*;

public class HttpClient {
	private static final Logger log = LoggerFactory.getLogger(HttpClient.class);
	private String protocol = "TLSv1";
	private RequestConfig requestConfig;
	private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.74 Safari/537.36 Edg/99.0.1150.55";
	private PoolingHttpClientConnectionManager connManager;
    private int connectTimeout = 6000; // 毫秒
	private int socketTimeout = 6000;
	private Map<String, String> headers;
	private String url;
	private String charset = "UTF-8";
	private Map<String, Object> params = new HashMap<>();
	private HttpEntity entity = null;
	private List<NameValuePair> pairs = new ArrayList<>();
    private boolean autoClose = true;
    private DownloadTask task;
	//上传文件 File或byte[]
	private Map<String, Object> files;

	private String returnType = "text";//stream
	private CloseableHttpClient client;

	public HttpClient(){
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		// 设置连接超时
		configBuilder.setConnectTimeout(connectTimeout);
		// 设置读取超时
		configBuilder.setSocketTimeout(socketTimeout);
		// 设置从连接池获取连接实例的超时
		configBuilder.setConnectionRequestTimeout(connectTimeout);
		// 在提交请求之前 测试连接是否可用
		configBuilder.setStaleConnectionCheckEnabled(true);
		requestConfig = configBuilder.build();
	}

	public HttpResponse post() {
		if(null != params && !params.isEmpty()){
			List<NameValuePair> pairs = HttpUtil.packNameValuePair(params);
			try {
				entity = new UrlEncodedFormEntity(pairs, charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		HttpPost method = new HttpPost(url);
		if(null != entity){
			method.setEntity(entity);
		}
		return exe(method);
	}

	public HttpResponse put() {
		if(null != params && !params.isEmpty()){
			List<NameValuePair> pairs = HttpUtil.packNameValuePair(params);
			try {
				entity = new UrlEncodedFormEntity(pairs, charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		HttpPut method = new HttpPut(url);
		if(null != entity){
			method.setEntity(entity);
		}
		return exe(method);
	}

	public HttpResponse get() {
		url = HttpUtil.mergeParam(url, params);
		if (null != pairs && !pairs.isEmpty()) {
			url = HttpUtil.mergeParam(url, URLEncodedUtils.format(pairs, charset));
		}
		HttpGet method = new HttpGet(url);
		return exe(method);
	}
	public HttpResponse delete() {
		url = HttpUtil.mergeParam(url, params);
		if (null != pairs && !pairs.isEmpty()) {
			url = HttpUtil.mergeParam(url, URLEncodedUtils.format(pairs, charset));
		}
		HttpDelete method = new HttpDelete(url);
		return exe(method);
	}

	public HttpResponse postStream(Map<String, String> headers, String url, String encode, HttpEntity entity) {
		HttpResponse result = new HttpResponse();
		InputStream is = null;
		if(null == client){
			client = HttpUtil.client(url);
		}
		if(url.startsWith("//")){
			url = "http:" + url;
		}
		HttpPost method = new HttpPost(url);
		if(null != entity){
			method.setEntity(entity);
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

	private HttpResponse exe(HttpRequestBase method){
		setHeader(method, headers);
		if(null == client){
			client = HttpUtil.client(url, userAgent);
		}
		if(url.startsWith("//")){
			url = "http:" + url;
		}
		CloseableHttpResponse response = null;
		HttpResponse result = null;
		try {
			long fr = System.currentTimeMillis();
			if(ConfigTable.IS_HTTP_LOG && log.isWarnEnabled()){
				log.warn("[http request][method:{}][url:{}]", method.getMethod(), method.getURI());
			}
			if("stream".equals(returnType)) {
				response = client.execute(method);
				InputStream is = response.getEntity().getContent();
				result = new HttpResponse();
				result.setInputStream(is);
			}else{
				method.setHeader("Connection","close");
				response = client.execute(method);
				result = parseResult(result, response, charset);
			}
			if(ConfigTable.IS_HTTP_LOG && log.isWarnEnabled()){
				log.warn("[http request][method:{}][status:{}][耗时:{}][url:{}]", method.getMethod(), result.getStatus(), System.currentTimeMillis() - fr, method.getURI());
			}
		} catch (Exception e) {
			result = new HttpResponse();
			e.printStackTrace();
		} finally {
			if(!"stream".equals(returnType)) {
				try {
					if (null != response) {
						response.close();
					}
					method.releaseConnection();
					if (autoClose) {
						client.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public boolean download(){
		boolean result = false;
		String url = task.getUrl();
		if(null == client){
			client = HttpUtil.client(url);
		}
		if(url.startsWith("//")){
			url = "http:" + url;
		}
		String finalUrl = url;
		if(null != finalUrl && finalUrl.startsWith("//")){
			finalUrl = "http:"+url;
		}
		finalUrl = HttpUtil.mergeParam(finalUrl, task.getParams());
		// DownloadProgress progress = task.getProgress();
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
		Map<String, String> headers = task.getHeaders();
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
		0, 100, 40  开始, 结束, 已完成
		101, 200
		 表示头500个字节:bytes=0-499
		表示第二个500字节:bytes=500-999
		表示最后500个字节:bytes=-500
		表示500字节以后的范围:bytes=500-
		第一个和最后一个字节:bytes=0-0, -1
		同时指定几个范围:bytes=500-600, 601-999
		 */
		long start=0;
		if(tmpFile.exists()){//继上次进度下载
			start = tmpFile.length();
		}
		String range = "bytes=" + start + "-";
		get.setHeader("Range", range);

		try {
			org.apache.http.HttpResponse response = client.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if(code == 416){
				get.removeHeaders("Range");
				response = client.execute(get);
				code = response.getStatusLine().getStatusCode();
				log.warn("[http download][断点设置异常][url:{}]", url);
			}
			if(code != 200 && code !=206){
				// progress.error(url, "", code, "状态异常");
				task.error(code, "状态异常");
				return false;
			}
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				long total = entity.getContentLength();
				// progress.init(url, "", total, start);
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
						log.warn("[http download][break][url:{}]", url);
						break;
					}
					raf.write(buffer, 0, len);

					// progress.step(url, "", len);
					task.step(len);
				}
			}
			result = true;
		} catch (Exception e) {
			// progress.error(url, "", 0, e.toString());
			task.error(-1, e.toString());
			log.warn("[http download][下载异常][url:{}]", url);
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
			task.finish();
		}
		return result;
	}

	public HttpResponse upload() {
		if(null != url && url.startsWith("//")){
			url = "http:"+url;
		}
		if(BasicUtil.isEmpty(charset)){
			charset = "UTF-8";
		}
		String BOUNDARY = "-----" + BasicUtil.getRandomLowerString(20);  // 设置边界
		MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
		HttpPost post = new HttpPost(url);
		post.setConfig(requestConfig);
		builder.setBoundary(BOUNDARY);
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);  // 浏览器兼容模式
		builder.setCharset(Charset.forName(charset));  // 设置字符编码集
		ContentType contentType = ContentType.create("text/plain", Charset.forName(charset));

		HttpUtil.mergeParam(builder, params, contentType);
		if(null != headers){
			for(String key:headers.keySet()){
				post.setHeader(key, headers.get(key));
			}
		}
		String fileLog = "";
		if(null != files) {
			for (String key : files.keySet()) {
				Object  val  = files.get(key);
				if(null == val){
					continue;
				}
				if(val instanceof File) {
					File file = (File)val;
					builder.addBinaryBody(key, file, ContentType.MULTIPART_FORM_DATA, file.getName());
					fileLog += "[" + key + ":" + file.getAbsolutePath() + "]";
				}else if(val instanceof byte[]){
					byte[] bytes = (byte[]) val;
					builder.addBinaryBody(key, bytes, ContentType.MULTIPART_FORM_DATA, key);
					fileLog += "[" + key + ":bytes]";
				}else if(val instanceof InputStream){
					InputStream is = (InputStream)val;
					builder.addBinaryBody(key, is, ContentType.MULTIPART_FORM_DATA, key);
				}
			}
		}
		if(ConfigTable.IS_HTTP_LOG && log.isWarnEnabled()){
			log.warn("[http upload][url:{}]"+fileLog, url);
		}

		HttpEntity entity = builder.build();// 生成 HTTP POST 实体
		post.setEntity(entity);   // post 实体.
		post.addHeader("Content-Type","multipart/form-data;boundary=" + BOUNDARY);  // 表单形式.
		HttpResponse source = exe(post);
		return source;
	}
	public int status(){
		int code = -1;
		CloseableHttpClient client = HttpUtil.defaultSSLClient();
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
	public static HttpResponse parseResult(HttpResponse result, CloseableHttpResponse response, String encode) {
		if (null == result) {
			result = new HttpResponse();
		}
		try {
			if(null != response){
				Map<String, String> headers = new HashMap<>();
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
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					result.setInputStream(entity.getContent());
					String text = EntityUtils.toString(entity, encode);
					result.setText(text);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 设置header
	 *
	 * @param method  method
	 * @param headers  headers
	 */
	public static void setHeader(HttpRequestBase method, Map<String, String> headers) {
		if (null != headers) {
			Iterator<String> keys = headers.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = headers.get(key);
				method.setHeader(key, value);
			}
		}
	}

	public CloseableHttpClient createClient(){
		CloseableHttpClient client = null;
		HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
		builder.setUserAgent(userAgent);
		client = builder.build();
		return client;
	}
	public CloseableHttpClient ceateSSLClient(File keyFile, String protocol, String password){
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
			// ALLOW_ALL_HOSTNAME_VERIFIER  关闭host验证, 允许和所有的host建立SSL通信                  
			// BROWSER_COMPATIBLE_HOSTNAME_VERIFIER  和浏览器兼容的验证策略, 即通配符能够匹配所有子域名
			// STRICT_HOSTNAME_VERIFIER  严格匹配模式, hostname必须匹配第一个CN或者任何一个subject-alts
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, protocols, null,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		}catch(Exception e){
			e.printStackTrace();
		}
		return httpclient;
	}
	public CloseableHttpClient ceateSSLClient(File keyFile, String password){
		return ceateSSLClient(keyFile, protocol, password);
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public RequestConfig getRequestConfig() {
		return requestConfig;
	}

	public void setRequestConfig(RequestConfig requestConfig) {
		this.requestConfig = requestConfig;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public PoolingHttpClientConnectionManager getConnManager() {
		return connManager;
	}

	public void setConnManager(PoolingHttpClientConnectionManager connManager) {
		this.connManager = connManager;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	public void setEncode(String charset) {
		this.charset = charset;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public List<NameValuePair> getPairs() {
		return pairs;
	}

	public void setPairs(List<NameValuePair> pairs) {
		this.pairs = pairs;
	}

	public boolean isAutoClose() {
		return autoClose;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public DownloadTask getTask() {
		return task;
	}

	public void setTask(DownloadTask task) {
		this.task = task;
	}

	public Map<String, Object> getFiles() {
		return files;
	}

	public void setFiles(Map<String, Object> files) {
		this.files = files;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public CloseableHttpClient getClient() {
		return client;
	}

	public void setClient(CloseableHttpClient client) {
		this.client = client;
	}

	public HttpEntity getEntity() {
		return entity;
	}

	public void setEntity(HttpEntity entity) {
		this.entity = entity;
	}

	public Object getParam(String key){
		if(null != params){
			return params.get(key);
		}else{
			return null;
		}
	}
}
