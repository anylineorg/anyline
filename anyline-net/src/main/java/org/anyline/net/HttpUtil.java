/*  
 * Copyright 2006-2022 www.anyline.org
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

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

	private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

	public static HttpResult post(String url, String encode, HttpEntity... entitys) {
		return post(null, url, encode, entitys);
	} 
	public static HttpResult post(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		return post(headers, url, encode, BeanUtil.array2list(entitys));
	} 
 
	public static HttpResult post(String url, String encode, Map<String, Object> params) {
		return post(null, url, encode, params);
	} 
	public static HttpResult post(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setParams(params)
				.build().post();
	} 
 
 
	public static HttpResult post(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setEntitys(entitys)
				.build().post();
	}



	public static HttpResult put(String url, String encode, HttpEntity... entitys) {
		return put(null, url, encode, entitys);
	}

	public static HttpResult put( Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		return put(headers, url, encode, BeanUtil.array2list(entitys));
	}

	public static HttpResult put(String url, String encode, Map<String, Object> params) {
		return put(null, url, encode, params);
	}
	public static HttpResult put(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setParams(params)
				.build().put();
	}


	public static HttpResult put(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setEntitys(entitys)
				.build().put();
	}



	public static HttpResult stream(String url, String encode, HttpEntity... entitys) {
		return stream(null, url, encode, entitys);
	}
	public static HttpResult stream(Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		return stream(headers, url, encode, BeanUtil.array2list(entitys));
	}

	public static HttpResult stream(String url, String encode, Map<String, Object> params) {
		return stream(null, url, encode, params);
	}
	public static HttpResult stream(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setParams(params)
				.setReturnType("stream")
				.build().post();
	}


	public static HttpResult stream(Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setEntitys(entitys)
				.setReturnType("stream")
				.build().post();
	}

	public static HttpResult get(String url) {
		return get(url, "UTF-8");
	} 
	public static HttpResult get(String url, String encode) {
		return get(url, encode, new HashMap<String,Object>());
	} 
	public static HttpResult get(String url, String encode, Map<String, Object> params) {
		return get(null, url, encode, params);
	} 
 
	public static HttpResult get(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setParams(params)
				.build().get();
	} 
 
	public static HttpResult get(String url, String encode, List<NameValuePair> pairs) {
		return get(null, url, encode, pairs);
	} 
 
	public static HttpResult get(Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setPairs(pairs)
				.build().get();
	} 


	 
	public static HttpResult delete(String url, String encode, Map<String, Object> params) {
		return delete(null, url, encode, params);
	} 
 
	public static HttpResult delete(Map<String, String> headers, String url, String encode, Map<String, Object> params) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
				.setParams(params)
				.build().delete();
	} 
 
	public static HttpResult delete(String url, String encode, List<NameValuePair> pairs) {
		return delete(null, url, encode, pairs);
	} 
	public static HttpResult delete(Map<String, String> headers, String url, String encode, NameValuePair ... pairs) {
		return delete(headers, url, encode, BeanUtil.array2list(pairs));
	} 
 
	public static HttpResult delete(Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		return HttpBuilder.init()
				.setHeaders(headers)
				.setUrl(url)
				.setEncode(encode)
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
	public static DownloadTask download(String url, String dst, Map<String,String> headers,Map<String,Object> params){ 
		File file = new File(dst); 
		return download(url, file, headers, params, false);
	} 
	public static DownloadTask download(String url, File dst, Map<String,String> headers,Map<String,Object> params){ 
		return download(url, dst, headers, params, false);
	} 
	public static DownloadTask download(String url, String dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		File file = new File(dst); 
		return download(url, file, headers, params, override);
	} 
	public static DownloadTask download(String url, File dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		return download(new DefaultProgress(url, dst), url, dst, headers, params, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, String dst, boolean override){ 
		return download(progress, url, new File(dst), null, null, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, File dst, boolean override){ 
		return download(progress, url, dst, null, null, override);
	} 
	public static DownloadTask download(DownloadProgress progress, String url, String dst, Map<String,String> headers,Map<String,Object> params, boolean override){ 
		return download(progress, url, new File(dst), headers, params, override);
	}

	public static DownloadTask download(DownloadProgress progress, String url, File dst, Map<String,String> headers,Map<String,Object> params, boolean override){
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

	public static HttpResult upload(String url, Map<String,File> files, Map<String,String> headers, Map<String,Object> params){ 
		return upload(url, files, "UTF-8", headers, params); 
	} 
	public static HttpResult upload(String url, Map<String, File> files,  Map<String, Object> params) { 
		return upload( url, files, null, params); 
	} 
	public static HttpResult upload(String url, Map<String, File> files) { 
		return upload(url, files, null, null); 
	}

	public static HttpResult upload(String url, Map<String,File> files, String encode, Map<String,String> headers, Map<String,Object> params){
		return HttpBuilder.init()
				.setUrl(url)
				.setEncode(encode)
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
	public static boolean isUrl(String src){
		if(null == src){
			return false;
		}
		if(src.startsWith("http://") || src.startsWith("https://")){
			return true;
		}
		if(src.startsWith("//")){
			src = src.substring(2);
			int index1 = src.indexOf("."); 	// 域名中的.
			if(index1 == -1){
				return false;
			}
			int index2 = src.indexOf("/");	// url中的path分隔
			if(index1 < index2){			// 没有在/之前出现的 有可能是文件名中的.
				return true;
			}
			if(index2 == -1){				//没有域名
				return true;
			}
		}
		return false;
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


}
