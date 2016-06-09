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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.util;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;


public class HttpUtil {
	private static Logger LOG = Logger.getLogger(HttpUtil.class);
	private static HttpUtil instance = new HttpUtil();
	private HttpClient client = null;
	private String encode = "UTF-8";
	private int timeout = 60000;
	private boolean initialized = false;
	private HttpProxy proxy;

	public static Source post(HttpProxy proxy, String url, String encode, Object... params) {
		Map<String, Object> map = paramToMap(params);
		return post(proxy, url, encode, map);
	}
	public static Source post(String url, String encode, Object... params) {
		return post(null, url, encode, params);
	}
	public static Source post(String url, String encode, Map<String, Object> params) {
		return post(null, url, encode, params);
	}
	public static Source post(HttpProxy proxy, String url, String encode, Map<String, Object> params) {
		HttpUtil instance = getInstance();
		instance.setProxy(proxy);
		instance.setEncode(encode);
		HttpMethod method = instance.packPost(url, params);
		return instance.invoke(method);
	}

	public static Source get(HttpProxy proxy, String url, String encode, Object... params) {
		Map<String, Object> map = paramToMap(params);
		return get(proxy, url, encode, map);
	}
	public static Source get(String url, String encode, Object... params) {
		return get(null, url, encode, params);
	}
	public static Source get(String url, String encode, Map<String, Object> params) {
		return get(null, url, encode, params);
	}
	public static Source get(HttpProxy proxy,String url, String encode,  Map<String, Object> params) {
		HttpUtil instance = getInstance();
		instance.setProxy(proxy);
		instance.setEncode(encode);
		HttpMethod method = instance.packGet(url, params);
		return instance.invoke(method);
	}

	public static HttpUtil getInstance() {
		if (!instance.initialized) {
			instance.init();
		}
		return instance;
	}
	private static Map<String,Object> paramToMap(Object ... params){
		Map<String,Object> result = new HashMap<String,Object>();
		if(null != params){
			int size = params.length;
			for(int i=0; i<size-1; i+=2){
				Object key = params[i];
				Object value = params[i+1];
				if(null == value){
					value = "";
				}
				result.put(key.toString(), value);
			}
		}
		return result;
	}

	private synchronized void init() {
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		if (encode != null && !encode.trim().equals("")) {
			client.getParams().setParameter("http.protocol.content-charset", encode);
			client.getParams().setContentCharset(encode);
		}
		if (timeout > 0) {
			client.getParams().setSoTimeout(timeout);
		}
		if (null != proxy) {
			HostConfiguration hc = new HostConfiguration();
			hc.setProxy(proxy.getHost(), proxy.getPort());
			client.setHostConfiguration(hc);
			client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxy.getUser(), proxy.getPassword()));
		}
		initialized = true;
	}

	private Source invoke(HttpMethod method) {
		Source source = new Source();
		String result = "";
		String uri = null;
		try {
			uri = method.getURI().getURI();
			client.executeMethod(method);
			BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), encode));
			String line = null;
			String html = null;
			while ((line = reader.readLine()) != null) {
				if (html == null) {
					html = "";
				} else {
					html += "\r\n";
				}
				html += line;
			}
			if (html != null) {
				//result = new String(html.getBytes("ISO-8859-1"), encode);
				result = html;
			}
		} catch (SocketTimeoutException e) {
			LOG.error("连接超时[" + uri + "]"+e.getMessage());
		} catch (java.net.ConnectException e) {
			LOG.error("连接失败[" + uri + "]"+e.getMessage());
		} catch (Exception e) {
			LOG.error("连接时出现异常[" + uri + "]"+e.getMessage());
			e.printStackTrace();
		} finally {
			if (method != null) {
				try {
					method.releaseConnection();
				} catch (Exception e) {
					LOG.error("释放网络连接失败[" + uri + "]"+e.getMessage());
				}
			}
		}
		source.setText(result);
		return source;
	}

	private PostMethod packPost(String url, Map<String, Object> params) {
		PostMethod post = new PostMethod(url);
		post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,encode);  
		if (null != params && params.size() > 0) {
			Iterator<String> paramKeys = params.keySet().iterator();
			NameValuePair[] form = new NameValuePair[params.size()];
			int idx = 0;
			while (paramKeys.hasNext()) {
				String key = (String) paramKeys.next();
				Object value = params.get(key);
				if (null == value) {
					value = "";
				}
				if (value instanceof String[] && ((String[]) value).length > 0) {
					NameValuePair[] tempForm = new NameValuePair[form.length + ((String[]) value).length - 1];
					for (int i = 0; i < idx; i++) {
						tempForm[i] = form[i];
					}
					form = tempForm;
					for (String v : (String[]) value) {
						if(null == v){
							v = "";
						}
						form[idx] = new NameValuePair(key, v);
						idx++;
					}
				} else if (value instanceof Collection) {
					Collection<Object> con = (Collection<Object>)value;
					if(con.size()>0){
						NameValuePair[] tempForm = new NameValuePair[form.length + con.size() - 1];
						for (int i = 0; i < idx; i++) {
							tempForm[i] = form[i];
						}
						form = tempForm;
						for (Object v : con) {
							if(null == v){
								v = "";
							}
							form[idx] = new NameValuePair(key, v.toString());
							idx++;
						}
					}
				}else{
					form[idx] = new NameValuePair(key, value.toString());
					idx++;
				}
			}
			post.setRequestBody(form);
		}
		return post;
	}

	private GetMethod packGet(String url, Map<String, Object> params) {
		GetMethod get = null;
		if (params != null && params.size() > 0) {
			Iterator paramKeys = params.keySet().iterator();
			StringBuffer getUrl = new StringBuffer(url.trim());
			if (url.trim().indexOf("?") > -1) {
				if (url.trim().indexOf("?") < url.trim().length() - 1 && url.trim().indexOf("&") < url.trim().length() - 1) {
					getUrl.append("&");
				}
			} else {
				getUrl.append("?");
			}
			while (paramKeys.hasNext()) {
				String key = (String) paramKeys.next();
				Object value = params.get(key);
				if (null == value) {
					value = "";
				}
				if (value instanceof String[] && ((String[]) value).length > 0) {
					for (String v : (String[]) value) {
						getUrl.append(key).append("=").append(v).append("&");
					}
				} else if (value instanceof Collection) {
					Collection<Object> con = (Collection)value;
					for (Object v : con) {
						if(null == v || "".equals(v.toString())){
							continue;
						}
						getUrl.append(key).append("=").append(v).append("&");
					}
				}else{
					getUrl.append(key).append("=").append(value).append("&");
				}
			}
			if (getUrl.lastIndexOf("&") == getUrl.length() - 1) {
				url = getUrl.substring(0, getUrl.length() - 1);
			} else {
				url = getUrl.toString();
			}
		}
		get = new GetMethod(url);
		get.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,encode);
		return get;
	}


	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public HttpProxy getProxy() {
		return proxy;
	}
	public void setProxy(HttpProxy proxy) {
		this.proxy = proxy;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public synchronized boolean isInitialized() {
		return initialized;
	}
	
	


	


	/**
	 * 提取url根目录
	 * 
	 * @param url
	 * @return
	 */
	public static String getHostUrl(String url) {
		url = url.replaceAll("http://", "");
		int idx = url.indexOf("/");
		if (idx != -1) {
			url = url.substring(0, idx);
		}
		url = "http://" + url;
		return url;
	}

	/**
	 * 创建完整HTTP路径
	 * 
	 * @param srcUrl
	 * @param dstUrl
	 * @return
	 */
	public static String createFullHttpPath(String host, String url) {
		// 完整的目标URL
		if (url.startsWith("http:"))
			return url;
		String fullPath = null;

		if (url.startsWith("/")) {// 当前站点的绝对路径
			fullPath = getHostUrl(host) + url;
		} else if (url.startsWith("?")) {// 查询参数
			fullPath = fetchPathByUrl(host) + url;
		} else {// 当前站点的相对路径
			host = fetchDirByUrl(host);
			if (host.endsWith("/")) {
				// src是一个目录
				fullPath = host + url;
			} else {
				// src有可能是一个文件 : 需要判断是文件还是目录 文件比例多一些
				fullPath = host + "/" + url;
			}
		}
		return fullPath;
	}

	/**
	 * 从URL中提取文件目录(删除查询参数)
	 * 
	 * @param url
	 * @return
	 */
	public static String fetchPathByUrl(String url) {
		int to = url.indexOf("?");
		if (to != -1)
			url = url.substring(0, to);
		return url;
	}

	/**
	 * 提取一个URL所在的目录
	 * 
	 * @param path
	 * @return
	 */
	public static String fetchDirByUrl(String url) {
		String dir = null;
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
	 * path是否包含文件名
	 * 
	 * @param path
	 * @return
	 */
	private static boolean isHttpFile(String path) {

		if (path.endsWith("/")) {
			return false;
		}
		String head = "http://";
		int fr = head.length();
		int l1 = path.lastIndexOf("/");
		int l2 = path.lastIndexOf(".");
		// int l3 = path.length();
		if (l1 == -1) {
			return false;
		} else if (l2 > l1 && l2 > fr) {
			return true;
		}
		return false;
	}


	public static void download(String url, String dst) {
		download(url, new File(dst));
	}

	public static void download(String url, File dst) {
		
		OutputStream os = null;
		InputStream is = null;
		byte[] buffer = new byte[10];
		int len = 0;
		try {
			File dir = dst.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			os = new FileOutputStream(dst);
			is = new URL(url).openStream();
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (Exception ex) {
					LOG.error(ex);
					is = null;
				}
			}
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (Exception ex) {
					LOG.error(ex);
					os = null;
				}
			}
		}

	}

	public static void downloadFile(String remoteFilePath, String dst) {
		downloadFile(remoteFilePath, new File(dst));
	}

	/**
	 * 下载远程文件并保存到本地
	 * 
	 * @param remoteFilePath
	 *            远程文件路径
	 * @param localFilePath
	 *            本地文件路径
	 */
	public static void downloadFile(String remoteFilePath, File dst) {
		URL urlfile = null;
		HttpURLConnection httpUrl = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		File parent = dst.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		try {
			urlfile = new URL(remoteFilePath);
			httpUrl = (HttpURLConnection) urlfile.openConnection();
			httpUrl.connect();
			bis = new BufferedInputStream(httpUrl.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(dst));
			int len = 2048;
			byte[] b = new byte[len];
			while ((len = bis.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			bos.flush();
			bis.close();
			httpUrl.disconnect();
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			try {
				bis.close();
				bos.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}
}