/* 
 * Copyright 2006-2015 the original author or authors.
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
import java.net.URL;

import org.apache.log4j.Logger;

import org.anyline.util.regular.RegularUtil;

public class HttpUtil {
	private static Logger log = Logger.getLogger(HttpUtil.class);
	private static int CONNECT_TIMEOUT = 90000; // 连接超时
	private static int READ_TIMEOUT = 50000; // 读取超时

	/**
	 * 获取url源文件
	 * 
	 * @param url
	 * @return
	 */
	public static Source getSourceByUrl(String url, String encode) {
		Source source = new Source();
		String host = getHostUrl(url);
		source.setUrl(url);
		source.setHost(host);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		InputStream is = null;
		try {
			/* 代理 */
			/*
			 * System.getProperties().put( "proxySet", "true");
			 * System.getProperties().put( "proxyHost", "72.36.112.74");
			 * System.getProperties().put( "proxyPort", "3124");
			 */

			connection = imitateBrowser(url);
			source.setContentType(connection.getContentType());

			/* 确认编码 */
			if (null == source.getEncode()) {
				source.setEncode(connection.getContentEncoding());
			}
			if (null == source.getEncode()) {
				source.setEncode(encode);
			}
			/* 读取数据 */
			is = connection.getInputStream();
			if (null == source.getEncode()) {
				reader = new BufferedReader(new InputStreamReader(is));
			} else {
				reader = new BufferedReader(new InputStreamReader(is,
						source.getEncode()));
			}
			StringBuffer buffer = new StringBuffer();
			String line;
			int idx = 0;
			int encodeStatus = 0; // 编码状态(0-未验证 1-验证正确 2-验证错误)
			while ((line = reader.readLine()) != null) {
				buffer.append("\n");
				buffer.append(line);
				idx++;
				if (idx == 30) {
					// 读取30行时验证编码
					String realEncode = RegularUtil.cut(buffer.toString(),
							"charset=", "\"");
					if (BasicUtil.isNotEmpty(realEncode)
							&& !realEncode.equalsIgnoreCase(source.getEncode())) {
						encodeStatus = 2;
						source.setEncode(realEncode);
						break;
					} else {
						encodeStatus = 1;
					}
				}
			}
			if (encodeStatus == 0) {
				// 验证编码(全文不到30行)
				String realEncode = RegularUtil.cut(buffer.toString(),
						"charset=", "\"");
				if (BasicUtil.isNotEmpty(realEncode)
						&& !realEncode.equalsIgnoreCase(source.getEncode())) {
					encodeStatus = 2;
					source.setEncode(realEncode);
				} else {
					encodeStatus = 1;
				}
			}

			if (encodeStatus == 2) {
				// 修正编码重新读取数据
				connection = imitateBrowser(url);
				buffer = new StringBuffer();
				is = connection.getInputStream();
				reader = new BufferedReader(new InputStreamReader(is,
						source.getEncode()));
				while ((line = reader.readLine()) != null) {
					buffer.append("\n");
					buffer.append(line);
				}
			}

			source.setText(buffer.toString());
			source.setLastModified(connection.getLastModified());
			is.close();
		} catch (Exception e) {
			log.error("getSourceByUrl(" + url + "):\n" + e);
			source = null;
		} finally {
			try {
				if (null != reader)
					reader.close();
				if (null != is)
					is.close();
				if (null != connection)
					connection.disconnect();
			} catch (Exception e) {
				log.error(e);
			}
		}
		return source;
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

	/**
	 * 模拟浏览器
	 * 
	 * @param url
	 * @return
	 */
	private static HttpURLConnection imitateBrowser(String url) {
		HttpURLConnection connection = null;
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout",
					CONNECT_TIMEOUT + "");
			System.setProperty("sun.net.client.defaultReadTime", READ_TIMEOUT
					+ "");
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("GET");
			connection
					.setRequestProperty("User-Agent",
							"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)");
		} catch (Exception e) {

		}
		return connection;
	}

	public static void download(String url, String dst) {
		download(url, new File(dst));
	}
		public static void download(String url, File dst) {
		OutputStream os = null;
		InputStream is = null;
		byte[] buffer = new byte[1024];
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
			log.error(e);
		} finally {
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (Exception ex) {
					log.error(ex);
					is = null;
				}
			}
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (Exception ex) {
					log.error(ex);
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
		if(!parent.exists()){
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
			log.error(e);
		} finally {
			try {
				bis.close();
				bos.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}
}