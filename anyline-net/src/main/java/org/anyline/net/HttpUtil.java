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
import org.apache.http.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

	private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

	private static CloseableHttpClient default_client;
	private static CloseableHttpClient default_ssl_client;
	private static RequestConfig default_request_config;
	private static int default_connect_timeout = 72000; // 毫秒
	private static int default_socket_timeout = 72000;
	private static String default_user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.74 Safari/537.36 Edg/99.0.1150.55";
	public static String PROTOCOL_TLSV1 = "TLSv1";

	public static HttpResponse post(String url, String charset, Map<String, Object> params) {
		return post(null, url, charset, params);
	}
	public static HttpResponse post(String url, Map<String, Object> params) {
		return post(null, url, "UTF-8", params);
	}
	public static HttpResponse post(String url) {
		return post(null, url, "UTF-8", (HttpEntity)null);
	}
	public static HttpResponse post(Map<String, String> headers, String url) {
		return post(headers, url, "UTF-8", (HttpEntity)null);
	}
	public static HttpResponse post(String url, String charset, HttpEntity entity) {
		return post(null, url, charset, entity);
	}
	public static HttpResponse post(String url, HttpEntity entity) {
		return post(null, url, null, entity);
	}
	public static HttpResponse post(Map<String, String> headers, String url, HttpEntity entity) {
		return post(headers, url, null, entity);
	}
	public static HttpResponse post(Map<String, String> headers, String url, String charset, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setParams(params)
				.build().post();
	} 
 
 
	public static HttpResponse post(Map<String, String> headers, String url, String charset, HttpEntity entity) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setEntity(entity)
				.build().post();
	}

	public static HttpResponse put(String url, String charset, Map<String, Object> params) {
		return put(null, url, charset, params);
	}
	public static HttpResponse put(Map<String, String> headers, String url, String charset, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setParams(params)
				.build().put();
	}

	public static HttpResponse put(Map<String, String> headers, String url, String charset, HttpEntity entity) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setEntity(entity)
				.build().put();
	}

	public static HttpResponse stream(String url) {
		return stream(null, url, "UTF-8", (HttpEntity)null);
	}
	public static HttpResponse stream(String url, String charset, HttpEntity entity) {
		return stream(null, url, charset, entity);
	}
	public static HttpResponse stream(String url, String charset, Map<String, Object> params) {
		return stream(null, url, charset, params);
	}
	public static HttpResponse stream(Map<String, String> headers, String url, String charset, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setParams(params)
				.setReturnType("stream")
				.build().post();
	}

	public static HttpResponse stream(Map<String, String> headers, String url, String charset, HttpEntity entity) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setEntity(entity)
				.setReturnType("stream")
				.build().post();
	}

	public static HttpResponse get(String url) {
		return get(url, "UTF-8");
	} 
	public static HttpResponse get(String url, String charset) {
		return get(url, charset, new HashMap<String, Object>());
	} 
	public static HttpResponse get(String url, String charset, Map<String, Object> params) {
		return get(null, url, charset, params);
	} 
 
	public static HttpResponse get(Map<String, String> headers, String url, String charset, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setParams(params)
				.build().get();
	} 
 
	public static HttpResponse get(String url, String charset, List<NameValuePair> pairs) {
		return get(null, url, charset, pairs);
	}

	public static HttpResponse get(Map<String, String> headers, String url, String charset, List<NameValuePair> pairs) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setPairs(pairs)
				.build().get();
	}
	public static HttpResponse get(Map<String, String> headers, String url, String charset) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.build().get();
	}
	public static HttpResponse get(Map<String, String> headers, String url) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.build().get();
	}

	public static HttpResponse delete(String url, String charset, Map<String, Object> params) {
		return delete(null, url, charset, params);
	} 
 
	public static HttpResponse delete(Map<String, String> headers, String url, String charset, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setParams(params)
				.build().delete();
	} 
 
	public static HttpResponse delete(String url, String charset, List<NameValuePair> pairs) {
		return delete(null, url, charset, pairs);
	} 
	public static HttpResponse delete(Map<String, String> headers, String url, String charset, NameValuePair ... pairs) {
		return delete(headers, url, charset, BeanUtil.array2list(pairs));
	} 
 
	public static HttpResponse delete(Map<String, String> headers, String url, String charset, List<NameValuePair> pairs) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setCharset(charset)
				.setPairs(pairs)
				.build().delete();
	} 
 

	public static int status(String url){
		return HttpBuilder.init().setUrl(url).build().status();
	}

	public static DownloadTask download(String url, String dst){
		File file = new File(dst); 
		return download(url, file, null, null, false);
	} 
	public static DownloadTask download(String url, File dst){
		return download(url, dst, null, null, false);
	} 
	public static DownloadTask download(String url, String dest, Map<String, String> headers, Map<String, Object> params){
		File file = new File(dest);
		return download(url, file, headers, params, false);
	} 
	public static DownloadTask download(String url, File dst, Map<String, String> headers, Map<String, Object> params){
		return download(url, dst, headers, params, false);
	} 
	public static DownloadTask download(String url, String dest, Map<String, String> headers, Map<String, Object> params, boolean override){
		File file = new File(dest);
		return download(url, file, headers, params, override);
	} 
	public static DownloadTask download(String url, File dst, Map<String, String> headers, Map<String, Object> params, boolean override){
		return download(new DefaultProgress(url, dst), url, dst, headers, params, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, String dest, boolean override){
		return download(progress, url, new File(dest), null, null, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, File dst, boolean override){
		return download(progress, url, dst, null, null, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, String dest, Map<String, String> headers, Map<String, Object> params, boolean override){
		return download(progress, url, new File(dest), headers, params, override);
	}

	public static DownloadTask download(DownloadProgress progress, String url, File dst, Map<String, String> headers, Map<String, Object> params, boolean override){
		DownloadTask task = new DownloadTask(); 
		task.setProgress(progress); 
		task.setLocal(dst); 
		task.setUrl(url); 
		task.setHeaders(headers); 
		task.setParams(params); 
		task.setOverride(override);
		HttpBuilder.init().addDownloadTask(task).build().download();
		return task; 
	} 

	public static HttpResponse upload(String url, Map<String, Object> files, Map<String, String> headers, Map<String, Object> params){
		return upload(url, files, "UTF-8", headers, params);
	} 
	public static HttpResponse upload(String url, Map<String, Object> files, Map<String, Object> params) {
		return upload( url, files, null, params);
	} 
	public static HttpResponse upload(String url, Map<String, Object> files) {
		return upload(url, files, null, null);
	}

	/**
	 * 文件上传
	 * @param url url
	 * @param files File或byte[]或InputStream如果是url可以调用URL.openStream()获取输入流
	 * @param charset 编码
	 * @param headers header
	 * @param params 参数
	 * @return HttpResponse
	 */
	public static HttpResponse upload(String url, Map<String, Object> files, String charset, Map<String, String> headers, Map<String, Object> params){
		return HttpBuilder.init()
				.setUrl(url)
				.setCharset(charset)
				.setHeaders(headers)
				.setParams(params)
				.setUploadFiles(files)
				.build().upload();
	}

	public static String mergePath(String ... paths){
		String result = null;
		String separator = "/";
		if(null != paths){
			for(String path:paths){
				if(BasicUtil.isEmpty(path)){
					continue;
				}
				path = path.replace("\\","/");
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
	 * @return String
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
			if (host.endsWith("/")) {// src是一个目录
				fullPath = host + url;
			} else {// src有可能是一个文件 : 需要判断是文件还是目录 文件比例多一些
				fullPath = host + "/" + url;
			}
		}
		return fullPath;
	}

	/**
	 * 提取url根目录
	 *
	 * @param url  url
	 * @return String
	 */
	public static String host(String url) {
		if(null == url){
			return null;
		}
		String str = url.replaceAll("http://","").replaceAll("https://","").replaceAll("//","");
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
	 * @return String
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
	 * @return String
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
	 * @return String
	 */
	public static String parseFileName(String url) {
		String name = null;
		if(null != url){
			url = url.replace("://","");
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
	 * @return boolean
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

	public static String read(InputStream is, String charset) {
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
			return new String(bab.toByteArray(), charset);
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
	 * 合并参数
	 * @param url  url
	 * @param params  params
	 * @return String
	 */
	public static String mergeParam(String url, Map<String, Object> params){
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
	public static MultipartEntityBuilder mergeParam(MultipartEntityBuilder builder, Map<String, Object> params, ContentType contetType){
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
	 * @return String
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
	public static List<NameValuePair> packNameValuePair(Map<String, Object> params){
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
						if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
							log.info("[request param][{}={}]", key, BasicUtil.cut(val, 0, 100));
						}
					}
				}else if(value instanceof Collection){
					Collection vals = (Collection)value;
					for(Object val:vals){
						if(null == val){
							continue;
						}
						pairs.add(new BasicNameValuePair(key, val.toString()));
						if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
							log.info("[request param][{}={}]", key, BasicUtil.cut(val.toString(), 0, 100));
						}
					}
				}else if(null != value){
					pairs.add(new BasicNameValuePair(key, value.toString()));
					if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
						log.info("[request param][{}={}]", key, BasicUtil.cut(value.toString(), 0, 100));
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
	public static CloseableHttpClient client(String url, String userAgent){
		if(url.contains("https://")){
			return defaultSSLClient(userAgent);
		}else{
			return defaultClient(userAgent);
		}
	}
	public static CloseableHttpClient defaultClient(){
		return defaultClient(default_user_agent);
	}
	public static CloseableHttpClient defaultClient(String userAgent){
		HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(default_request_config);
		builder.setUserAgent(userAgent);
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
			// ALLOW_ALL_HOSTNAME_VERIFIER  关闭host验证, 允许和所有的host建立SSL通信
			// BROWSER_COMPATIBLE_HOSTNAME_VERIFIER  和浏览器兼容的验证策略, 即通配符能够匹配所有子域名
			// STRICT_HOSTNAME_VERIFIER  严格匹配模式, hostname必须匹配第一个CN或者任何一个subject-alts
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, protocols, null,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		}catch(Exception e){
			e.printStackTrace();
		}
		return httpclient;
	}
	public static CloseableHttpClient defaultSSLClient(){
		return defaultSSLClient(default_user_agent);
	}
	public static CloseableHttpClient defaultSSLClient(String userAgent){
		try {
			if(null != default_ssl_client){
				return default_ssl_client;
			}
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			httpClientBuilder.setUserAgent(userAgent);
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

	/**
	 * url参数编码
	 * @param src 原文
	 * @param enable 转换成是否可访问格式
	 * @param cn 是否编译中文
	 * @return String
	 */
	public static String encode(String src, boolean enable, boolean cn){
		String result = src;
		if(cn){
			try {
				result = URLEncoder.encode(src, "UTF-8");
			}catch (Exception e){
				return src;
			}
		}else{
			result = result
					.replace(" ","+")
					.replace("\n","")
					.replace("\r","")
					.replace("\t","")
					.replace("~","%7E")
					.replace("!","%21")
					.replace("@","%40")
					.replace("#","%23")
					.replace("$","%24")
					.replace("%","%25")
					.replace("^","%5E")
					.replace("&","%26")
					.replace("(","%28")
					.replace(")","%29")
					.replace("=","%3D")
					.replace("`","%60")
					.replace("+","%2B")
					.replace("{","%7B")
					.replace("}","%7D")
					.replace("[","%5B")
					.replace("]","%5D")
					.replace("<","%3C")
					.replace(">","%3E")
					.replace(",", "%2C")
					.replace("/","%2F")
					.replace("\\","%5C")
					.replace("?","%3F")
					.replace(";","%3B")
					.replace("'","%27")
					.replace(":","%3A")
					.replace("\"","%22");
		}
		if(enable) {
			result = result
				.replace("%3A", ":")
				.replace("%2F", "/");
		}
		result = result.replace("+", "%20"); //原来的 空格 被转成了 + 再把 + 转成 %20
		return result;
	}
}
