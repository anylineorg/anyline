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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class WebUtil {
	static final Logger log = Logger.getLogger(WebUtil.class);
	/*
	 * public static final String HTTP_REQUEST_PARAM_KEY_PREFIX =
	 * "wwwanylineorgk"; //参数名加密前缀 public static final String
	 * HTTP_REQUEST_PARAM_VALUE_PREFIX = "wwwanylineorgv"; //参数值加密前缀 public
	 * static final String HPPT_REQUEST_PARAM_PREFIX = "wwwanylineorgf";
	 * //参数整体加密前缀 public static final String HTTP_REQUEST_PARAM_FULL_DES_KEY =
	 * "@#$%0(*7#"; //整体加密密钥 public static final String
	 * HTTP_REQUEST_PARAM_KEY_DES_KEY = "@#$%#"; //参数名加密密钥 public static final
	 * String HTTP_REQUEST_PARAM_VALUE_DES_KEY = "@#23$%097#"; //参数值加密密钥
	 */
	public static final String REQUEST_ATTR_PARAMS_FULL_DECRYPT_MAP = "REQUEST_ATTR_PARAMS_FULL_DECRYPT_MAP"; // request参数值解密后MAP(整体加密)
	public static final String REQUEST_ATTR_PARAMS_PART_DECRYPT_MAP = "REQUEST_ATTR_PARAMS_PART_DECRYPT_MAP"; // request参数值解密后MAP(逐个加密)
	public static final String REQUEST_ATTR_IS_PARAMS_DECRYPT = "REQUEST_ATTR_IS_PARAMS_DECRYPT"; // request参数值是否已解密
	private static Map<String, DESKey> deskeys = null;
	private static DESKey defaultDesKey = null;
	private static final int MAX_DES_VERSION_INDEX = 12; // 密文中插入版本号最大位置
	private static final int DES_VERSION_LENGTH = 3;
	private static final String ENCRYPT_TYPE_PARAM = "param";
	private static final String ENCRYPT_TYPE_KEY = "name";
	private static final String ENCRYPT_TYPE_VALUE = "value";
	static {
		deskeys = new HashMap<String, DESKey>();
		try {
			File keyFile = new File(ConfigTable.getWebRoot(), ConfigTable.get("DES_KEY_FILE"));
			if (keyFile.exists()) {
				SAXReader reader = new SAXReader();
				Document document = reader.read(keyFile);
				Element root = document.getRootElement();
				for (Iterator<Element> itrKey = root.elementIterator(); itrKey.hasNext();) {
					Element element = itrKey.next();
					DESKey key = new DESKey();
					String version = element.attributeValue("version");
					key.setVersion(version);
					key.setKey(element.elementTextTrim("des-key"));
					key.setKeyParam(element.elementTextTrim("des-key-param"));
					key.setKeyParamName(element.elementTextTrim("des-key-param-name"));
					key.setKeyParamValue(element.elementTextTrim("des-key-param-value"));
					key.setPrefix(element.elementTextTrim("des-prefix"));
					key.setPrefixParam(element.elementTextTrim("des-prefix-param"));
					key.setPrefixParamName(element.elementTextTrim("des-prefix-param-name"));
					key.setPrefixParamValue(element.elementTextTrim("des-prefix-param-value"));
					if (null == defaultDesKey) {
						defaultDesKey = key;
					} else {
						deskeys.put(version, key);
					}
				}
			}
			if (null == defaultDesKey) {
				defaultDesKey = new DESKey();
				String version = "vic";
				defaultDesKey.setVersion(version);
				defaultDesKey.setKey("5*(YHU*6d9");
				defaultDesKey.setKeyParam("@#$%0(*7#");
				defaultDesKey.setKeyParamName("@#$%#");
				defaultDesKey.setKeyParamValue("@#23$%097#");
				defaultDesKey.setPrefix("");
				defaultDesKey.setPrefixParam("als7n6e9o1r5gv78ac1vice624c623f");
				defaultDesKey.setPrefixParamName("l80j0sa9n2y1l4i7n6e9o1r5gk");
				defaultDesKey.setPrefixParamValue("p298pn6e9o1r5gv");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查非法字符
	 * 
	 * @param src
	 * @return
	 */
	public static String filterIllegalChar(String src) {
		if (null == src) {
			return src;
		}
		src = src.replace("'", "''").trim();
		src = XssUtil.strip(src);
		return src;
	}

	/**
	 * 提取clicent真实ip
	 * 
	 * @param request
	 * @return
	 */

	public static String getRemoteIp(HttpServletRequest request) {
		String ip = getRemoteIps(request);
		if(null != ip && ip.contains(",")){
			ip = ip.substring(0, ip.indexOf(","));
		}
		return ip;
	}
	public static String getRemoteIps(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		
		if (ip.equals("127.0.0.1")) {
			// 根据网卡取本机配置的IP
			InetAddress inet = null;
			try {
				inet = InetAddress.getLocalHost();
				ip = inet.getHostAddress();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ip;
	}

	/**
	 * 判断是否是ajax请求
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String header = request.getHeader("x-requested-with");
		if (header != null && "XMLHttpRequest".equals(header)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是蜘蛛
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isSpider(HttpServletRequest request) {
		if (!hasReffer(request)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否有入口页
	 * 
	 * @param request
	 * @return
	 */
	public static boolean hasReffer(HttpServletRequest request) {
		if (null == request) {
			return false;
		}
		return (request.getHeader("Referer") != null);
	}

	/**
	 * 整体加密http 参数(cd=1&nm=2)
	 * 
	 * @param param
	 * @return
	 */
	public static String encryptRequestParam(String param) {
		if (null == param || "".equals(param.trim())) {
			return "";
		}
		return encryptByType(param, ENCRYPT_TYPE_PARAM);
	}

	/**
	 * 整体解密http 参数(cd=1&nm=2)
	 * 
	 * @param param
	 * @return
	 */
	public static String decryptRequestParam(String param) {
		if (null == param) {
			return null;
		}
		return decrypt(param, ENCRYPT_TYPE_PARAM);
	}

	/**
	 * 加密http请求参数名
	 * 
	 * @return
	 */
	public static String encryptHttpRequestParamKey(String key) {
		if (null == key || "".equals(key.trim())) {
			return "";
		}
		return encryptKey(key);
	}

	/**
	 * 解密http请求参数名
	 * 
	 * @return
	 */
	public static String decryptHttpRequestParamKey(String key) {
		if (null == key) {
			return null;
		}
		return decrypt(key, ENCRYPT_TYPE_KEY);
	}

	/**
	 * 加密http请求参数值
	 * 
	 * @return
	 */
	public static String encryptHttpRequestParamValue(String value) {
		if (null == value || "".equals(value.trim())) {
			return "";
		}
		return encryptValue(value);
	}

	/**
	 * 解密http请求参数值
	 * 
	 * @return
	 */
	public static String decryptHttpRequestParamValue(String value) {
		if (null == value) {
			return null;
		}
		return decrypt(value.trim(), ENCRYPT_TYPE_VALUE);
	}
	/**
	 * 加密
	 * 
	 * @param src
	 *            原文
	 * @param type
	 *            原文类型
	 * @return 加密>插入版本号>添加前缀
	 */
	private static String encryptByType(String src, String type, boolean mix) {
		String result = null;
		if (null == src) {
			return null;
		}
		if(isEncrypt(src, type)){
			return src;
		}
		DESUtil des = DESUtil.getInstance(defaultDesKey.getKey(type));
		try {
			result = des.encrypt(src);
			result = insertDESVersion(result);
			String pre = defaultDesKey.getPrefix(type);
			if(mix && ENCRYPT_TYPE_VALUE.equals(type)){
				//随机URL 避免QQ等工具报警 每次生成不同URL 扰乱爬虫追溯
				String rand = "v"+BasicUtil.getRandomNumberString(5)+"v";
				pre = rand+pre;
			}
			result = pre + result;
		} catch (Exception e) {
			result = null;
		}
		return result;
	}
	public static String encryptByType(String src, String type){
		return encryptByType(src, type, false);
	}

	public static String encryptKey(String src) {
		if (null == src) {
			return src;
		}
		return encryptByType(src, ENCRYPT_TYPE_KEY);
	}

	public static String encryptValue(String src, boolean mix) {
		if (null == src) {
			return src;
		}
		return encryptByType(src, ENCRYPT_TYPE_VALUE, mix);
	}
	public static String encryptValue(String src){
		return encryptValue(src, false);
	}

	/**
	 * 是否已加密 (应该根据规则判断,而不是解一次密)
	 * @param src
	 * @return
	 */
	public static boolean isEncrypt(String src, String type){
		if(null == src){
			return false;
		}
		try{
			String value = decrypt(src, type);
			if(null != value){
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	}
	/**
	 * 解密
	 * 
	 * @param src
	 *            密文
	 * @param type
	 *            密文类型
	 * @return 删除前缀 > 解析版本号 > 解密
	 */
	private static String decrypt(String src, String type) {
		if (null == src || null == type) {
			return null;
		}
		String result = null;
		result = decrypt(src, defaultDesKey, type); // 默认版本解密

		if (null == result) {
			// 没有对应版本号,逐个版本解密
			for (Iterator<String> versions = deskeys.keySet().iterator(); versions
					.hasNext();) {
				DESKey key = deskeys.get(versions.next());
				result = decrypt(src, key, type);
				if (null != result) {
					break;
				}
			}
		}
		return result;
	}
	/**
	 * 解密
	 * 
	 * @param src
	 * @param key
	 * @param type
	 * @return
	 */
	private static String decrypt(String src, DESKey key, String type) {
		if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
			log.warn("[decrypt][start][src:"+src+"][type:"+type+"]");
		}
		String result = src;
		if (null == src) {
			return null;
		}
		//删除随机URL混淆码
		if(ENCRYPT_TYPE_VALUE.equals(type)){
			if(RegularUtil.match(result,"v\\d{5}v", Regular.MATCH_MODE.PREFIX)){
				result = result.substring(7);
				if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
					log.warn("[decrypt][删除混淆码][result:"+result+"]");
				}
			}
		}
		// 删除前缀
		try {
			String prefix = key.getPrefix(type);
			int sub = -1;
			if (null != prefix) {
				sub = prefix.length();
			}
			if (sub > result.length() || sub == -1) {
				return null;
			}
			result = result.substring(sub);
			if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
				log.warn("[decrypt][删除前缀][result:"+result+"]");
			}
			// 解析版本
			String tmp[] = parseDESVersion(result);
			if (null != tmp && tmp.length == 2) {
				if (key.getVersion().equals(tmp[0])) {
					// 有版本号并且版本号对应
					result = tmp[1];
				}
			}
			DESUtil des = DESUtil.getInstance(key.getKey(type));
			// 根据对应版本解密
			if (null != des) {
				try {
					result = des.decrypt(result);
				} catch (Exception e) {
					result = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
		if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
			log.warn("[decrypt][end][result:"+result+"]");
		}
		return result;
	}

	/**
	 * 加密url参数部分
	 * 
	 * @param url
	 * @return
	 */
	public static String encryptUrl(String url) {
		if (null == url || !url.contains("?")) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		try {
			param = encryptRequestParam(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		url = url.substring(0, url.indexOf("?") + 1) + param;
		return url;
	}
	public static String encryptUrl(String url, boolean union, boolean encryptKey, boolean encryptValue) {
		if (null == url || !url.contains("?")) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		try {
			if(union){
				param = encryptRequestParam(param);
			}else{
				String params[] = param.split("&");
				param = "";
				for(String p:params){
					String kv[] = p.split("=");
					if(kv.length == 2){
						String k = kv[0];
						if(encryptKey){
							k = encryptKey(k);
						}
						String v = kv[1];
						if(encryptValue){
							v = encryptValue(v);
						}
						if(!"".equals(params)){
							param += "&";
						}
						param += k + "=" + v;
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		url = url.substring(0, url.indexOf("?") + 1) + param;
		return url;
	}

	/**
	 * 加密htmla标签中的url
	 * 
	 * @param tag
	 * @return
	 */
	public static String encryptHtmlTagA(String tag) {
		try {
			String url = RegularUtil.fetchUrl(tag);
			tag = tag.replace(url, encryptUrl(url));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tag;
	}

	/**
	 * 从解密后的参数MAP中取值
	 * 
	 * @param request
	 * @param key
	 *            //key
	 * @param valueEncrypt
	 *            //value是否加密
	 * @return
	 */
	private static List<Object> getHttpRequestParamsFormDecryptMap(HttpServletRequest request, String key, boolean valueEncrypt) {
		List<Object> result = new ArrayList<Object>();
		if (request.getAttribute(REQUEST_ATTR_IS_PARAMS_DECRYPT) == null) {
			decryptHttpRequestParam(request);
		}
		// 整体加密数据
		Map<String, List<String>> fullMap = (Map<String, List<String>>) request.getAttribute(REQUEST_ATTR_PARAMS_FULL_DECRYPT_MAP);
		List<String> values = fullMap.get(key);
		if (null != values) {
			result.addAll(values);
		} else {
			// 逐个加密数据
			Map<String, List<String>> partMap = (Map<String, List<String>>) request.getAttribute(REQUEST_ATTR_PARAMS_PART_DECRYPT_MAP);
			values = partMap.get(encryptHttpRequestParamKey(key));
			if (null != values) {
				if (valueEncrypt) {
					for (String value : values) {
						value = decryptHttpRequestParamValue(value);
						result.add(value);
					}
				} else {
					result.addAll(values);
				}
			}
		}
		return result;
	}

	/**
	 * 从解密后的参数MAP中取值
	 * 
	 * @param request
	 * @param param
	 * @return
	 */
	private static String getHttpRequestParamFormDecryptMap(HttpServletRequest request, String key, boolean valueEncrypt) {
		String result = null;
		List<Object> list = getHttpRequestParamsFormDecryptMap(request, key,valueEncrypt);
		if (null != list && list.size() > 0) {
			Object tmp = list.get(0);
			if (null != tmp) {
				result = tmp.toString().trim();
			}
		}
		return result;
	}

	/**
	 * 解密httprequet参数及参数值
	 * 
	 * @param request
	 */
	private static void decryptHttpRequestParam(HttpServletRequest request) {
		Map<String, List<String>> fullMap = new HashMap<String, List<String>>();
		Map<String, List<String>> partMap = new HashMap<String, List<String>>();
		// 整体加密格式
		String value = request.getQueryString();
		if (null != value) {
			if (value.contains("&")) {
				value = value.substring(0, value.indexOf("&"));
			}
			value = decryptRequestParam(value);
			if (null != value) {
				// 解密后拆分
				String items[] = value.split("&");
				for (String item : items) {
					String kv[] = item.split("=");
					String k = null;
					String v = null;
					if (kv.length > 0) {
						k = kv[0].trim();
					}
					if (kv.length > 1) {
						v = kv[1].trim();
						v = filterIllegalChar(v);
					}
					if (!"".equals(k) && !"".equals(v)) {
						List<String> list = fullMap.get(k);
						if (null == list) {
							list = new ArrayList<String>();
							fullMap.put(k, list);
						}
						list.add(v);
					}
				}
			}
		}
		// 逐个加密格式
		Map<String, String[]> params = request.getParameterMap();
		for (Iterator<String> keys = params.keySet().iterator(); keys.hasNext();) {
			String k = keys.next();
			String vs[] = null;

			List<String> list = partMap.get(k);
			if (null == list) {
				list = new ArrayList<String>();
				partMap.put(k, list);
			}
			vs = request.getParameterValues(k);
			if (null != vs) {
				for (String v : vs) {
					if (null != v && !"".equals(v)) {
						v = filterIllegalChar(v);
						list.add(v.trim());
					}
				}
			}

		}
		request.setAttribute(REQUEST_ATTR_PARAMS_FULL_DECRYPT_MAP, fullMap);
		request.setAttribute(REQUEST_ATTR_PARAMS_PART_DECRYPT_MAP, partMap);
		request.setAttribute(REQUEST_ATTR_IS_PARAMS_DECRYPT, true);
	}

	/**
	 * http request参数
	 * 
	 * @param request
	 * @param key
	 * @param keyEncrypt
	 *            key是否加密
	 * @param valueEncrypt
	 *            value是否加密
	 * @return
	 */
	public static List<Object> getHttpRequestParams(HttpServletRequest request,String key, boolean keyEncrypt, boolean valueEncrypt) {
		List<Object> result = new ArrayList<Object>();
		if (null == request || null == key) {
			return null;
		}
		String values[] = null;
		if (keyEncrypt) {
			// key已加密
			result = getHttpRequestParamsFormDecryptMap(request, key,
					valueEncrypt);
		} else {
			// key未加密
			values = request.getParameterValues(key);
			if (null != values) {
				for (String value : values) {
					if (null == value) {
						result.add("");
					}
					if (valueEncrypt) {
						value = decryptHttpRequestParamValue(value);
						value = filterIllegalChar(value);
					}
					if (null != value) {
						value = value.trim();
						value = filterIllegalChar(value);
					}
					result.add(value);
				}
			}
		}
		return result;
	}

	public static List<Object> getHttpRequestParams(HttpServletRequest request,
			String param, boolean keyEncrypt) {
		return getHttpRequestParams(request, param, keyEncrypt, false);
	}

	public static List<Object> getHttpRequestParams(HttpServletRequest request,
			String param) {
		return getHttpRequestParams(request, param, false, false);
	}

	/**
	 * HTTP参数
	 * 
	 * @param request
	 * @param key
	 *            参数名
	 * @param keyEncrypt
	 *            参数名是否加密过
	 * @param valueEncrypt
	 *            参数值是否加密过,是则解密
	 * @return
	 */
	public static Object getHttpRequestParam(HttpServletRequest request,String key, boolean keyEncrypt, boolean valueEncrypt) {
		String result = "";
		 List<Object> list = getHttpRequestParams(request, key, keyEncrypt, valueEncrypt);
		 if(null != list && list.size()>0){
			 result = (String)list.get(0);
		 }
		return result;
	}

	public static Object getHttpRequestParam(HttpServletRequest request,
			String param, boolean keyEncrypt) {
		return getHttpRequestParam(request, param, keyEncrypt, false);
	}

	public static Object getHttpRequestParam(HttpServletRequest request,
			String param) {
		return getHttpRequestParam(request, param, false, false);
	}

	/**
	 * 解析IP
	 * 
	 * @param ip
	 * @return
	 */
	public static long parseIp(String ip) {
		long ipNum = 0;
		try {
			if (null != ip) {
				ip = ip.trim();
				String num[] = ip.split("\\.");
				if (num.length > 0)
					ipNum += 255 * 255 * 255 * Long.parseLong(num[0]);
				if (num.length > 1)
					ipNum += 255 * 255 * Long.parseLong(num[1]);
				if (num.length > 2)
					ipNum += 255 * Long.parseLong(num[2]);
				if (num.length > 3)
					ipNum += Long.parseLong(num[3]);
			}
		} catch (Exception e) {
			ipNum = 0;
		}
		return ipNum;
	}

	/**
	 * 还原格式化IP
	 * 
	 * @param ipNum
	 * @return
	 */
	public static String formatIp(long ipNum) {
		String ip = "";
		long ip0 = ipNum / 255 / 255 / 255;
		long ip1 = ipNum % (255 * 255 * 255) / 255 / 255;
		long ip2 = ipNum % (255 * 255) / 255;
		long ip3 = ipNum % 255;
		ip = ip0 + "." + ip1 + "." + ip2 + "." + ip3;
		return ip;
	}

	/**
	 * 密文中插入版本号位置
	 * 
	 * @param src
	 *            未插入版本号的密文
	 * @return
	 */
	private static int getDESVersionIndex(String src) {
		int idx = -1;
		if (null != src && src.length() > MAX_DES_VERSION_INDEX) {
			String tmp = src.substring(MAX_DES_VERSION_INDEX);
			int len = tmp.length();
			String chr = src.substring(len / 2, len / 2 + 1);
			idx = (int) chr.toCharArray()[0];
			idx = Math.abs(idx % MAX_DES_VERSION_INDEX);
		}
		return idx;
	}

	/**
	 * 密文中插入版本号
	 * 
	 * @param src
	 *            未插入版本号的密文
	 * @return
	 */
	private static String insertDESVersion(String src, String version) {
		int idx = getDESVersionIndex(src);
		if (idx >= 0) {
			src = BasicUtil.insert(src, idx, version);
		}
		return src;
	}

	private static String insertDESVersion(String src) {
		return insertDESVersion(src, defaultDesKey.getVersion());
	}

	/**
	 * 解析加密版本号
	 * 
	 * @param src
	 * @return
	 */
	private static String[] parseDESVersion(String src) {
		String result[] = null;
		if (null != src && src.length() > DES_VERSION_LENGTH) {
			try {
				result = new String[2];
				String tmp = src.substring(DES_VERSION_LENGTH);
				int idx = getDESVersionIndex(tmp);
				if (idx >= 0) {
					result[0] = src.substring(idx, idx + DES_VERSION_LENGTH); // 版本号
					result[1] = src.substring(0, idx)
							+ src.substring(idx + DES_VERSION_LENGTH); // 没有版本号的密文
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 提取refer的uri
	 * 
	 * @param request
	 * @return
	 */
	public static String fetchReferUri(HttpServletRequest request) {
		if (null == request) {
			return null;
		}
		String result = request.getHeader("Referer");
		if (null == result) {
			return null;
		}
		String host = request.getScheme() + "://" + request.getServerName();
		result = result.replace(host, "");
		if (result.indexOf("?") != -1) {
			result = result.substring(0, result.indexOf("?"));
		}
		return result;
	}

	/**
	 * 是否是移动终端
	 * 
	 * @return
	 */
	public static boolean isWap(HttpServletRequest request) {
		boolean result = false;
		try{
			String agent = request.getHeader("user-agent");
			if(null == agent){
				return false;
			}
			String agentcheck = agent.trim().toLowerCase();
			String[] keywords = { "mobile", "android", "symbianos", "iphone",
					"wp\\d*", "windows phone", "mqqbrowser", "nokia", "samsung",
					"midp-2", "untrusted/1.0", "windows ce", "blackberry", "ucweb",
					"brew", "j2me", "yulong", "coolpad", "tianyu", "ty-",
					"k-touch", "haier", "dopod", "lenovo", "huaqin", "aigo-",
					"ctc/1.0", "ctc/2.0", "cmcc", "daxian", "mot-", "sonyericsson",
					"gionee", "htc", "zte", "huawei", "webos", "gobrowser",
					"iemobile", "wap2.0", "WAPI" };
			Pattern pf = Pattern.compile("wp\\d*");
			Matcher mf = pf.matcher(agentcheck);
			if (agentcheck != null && (agentcheck.indexOf("windows nt") == -1 && agentcheck .indexOf("Ubuntu") == -1)
					|| (agentcheck.indexOf("windows nt") > -1 && mf.find())) {
				for (int i = 0; i < keywords.length; i++) {
					Pattern p = Pattern.compile(keywords[i]);
					Matcher m = p.matcher(agentcheck);
					// 排除 苹果桌面系统 和ipad 、iPod
					if (m.find() && agentcheck.indexOf("ipad") == -1
							&& agentcheck.indexOf("ipod") == -1
							&& agentcheck.indexOf("macintosh") == -1) {
						result = true;
						break;
					}
				}
			}
		}catch(Exception e){
			
		}
		return result;
	}
	/**
	 * 是否本地访问
	 * @param request
	 * @return
	 */
	public static boolean isLocal(HttpServletRequest request){
		String ip = getRemoteIp(request);
		return ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip));
	}
	/**
	 * 是否微信调用
	 * @param request
	 * @return
	 */
	public static boolean isWeixin(HttpServletRequest request){
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		if(userAgent.indexOf("micromessenger")>-1){
		    return true;
		}else{
		    return false;
		}
	}
	public static boolean isApp(HttpServletRequest request){
		if(null == request){
			return false;
		}
		if(null == request.getSession()){
			return false;
		}
		String isApp = request.getSession().getAttribute("_IS_APP")+"";
		if("1".equals(isApp)){
			return true;
		}
		return false;
	}

	/**
	 * 是否支付宝调用
	 * @param request
	 * @return
	 */
	public static boolean isAlipay(HttpServletRequest request){
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		if(userAgent.indexOf("alipayclient")>-1){
		    return true;
		}else{
		    return false;
		}
	}
	/**
	 * 是否QQ调用
	 * @param request
	 * @return
	 */
	public static boolean isQQ(HttpServletRequest request){
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		if(userAgent.indexOf("qq/")>-1){
		    return true;
		}else{
		    return false;
		}
	}

	/**
	 * 是否android调用
	 * @param request
	 * @return
	 */
	public static boolean isAndroid(HttpServletRequest request){
		String userAgent = request.getHeader("user-agent").toLowerCase();
		if(userAgent.indexOf("android")>-1){
		    return true;
		}else{
		    return false;
		}
	}
	/**
	 * 是否android调用
	 * @param request
	 * @return
	 */
	public static boolean isIphone(HttpServletRequest request){
		String userAgent = request.getHeader("user-agent").toLowerCase();
		if(userAgent.indexOf("iphone")>-1){
		    return true;
		}else{
		    return false;
		}
	}
	public static String clientType(HttpServletRequest request){
		String type = "";
		if(isApp(request)){
			type = "app";
		}else if(isWeixin(request)){
			type = "weixin";
		}else if(isQQ(request)){
			type = "qq";
		}else if(isAlipay(request)){
			type = "alipay";
		}else if(isWap(request)){
			type = "wap";
		}else{
			type = "web";
		}
		return type;
	}
	/**
	 * 加密map
	 * @param map
	 * @param mix
	 * @param keys
	 * @return
	 */
	private static Map<String, Object> encryptValue(Map<String, Object> map, boolean mix, String... keys) {
		if (null == map) {
			return map;
		}
		List<String> ks = BeanUtil.getMapKeys(map);
		for(String k:ks){
			Object v = map.get(k);
			if(null == v){
				continue;
			}
			
			if(v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) {
				if(null == keys || keys.length == 0 || BasicUtil.contains(keys, k)){
					v = encryptValue(v.toString(), mix);
				}
			}else{
				if (v instanceof Map) {
					v = encryptValue((Map<String, Object>) v, mix, keys);
				} else if (v instanceof Collection) {
					v = encryptValue((Collection<Object>) v, mix, keys);
				} else {
					v = encryptValue(v, mix, keys);
				}
			}
			map.put(k, v);
		}
			
		return map;
	}

	/**
	 * 加密对象
	 * 
	 * @param obj
	 * @param mix
	 * @param keys
	 * @return
	 */
	private static Object encryptValue(Object obj, boolean mix, String... keys) {
		if (null == obj) {
			return obj;
		}
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Date) {
			return encryptValue(obj.toString(),mix);
		}
		if (obj instanceof Map) {
			obj = encryptValue((Map<String, Object>) obj, mix, keys);
		} else if (obj instanceof Collection) {
			obj = encryptValue((Collection<Object>) obj, mix, keys);
		} else {
			//Object无法加密
			List<String> ks = BeanUtil.getFieldsName(obj.getClass());
			for (String k : ks) {
				Object v = BeanUtil.getFieldValue(obj, k);
				if (null == v) {
					continue;
				}
				if(v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) {
					if(null == keys || keys.length == 0 || BasicUtil.contains(keys, k)){
						v = encryptValue(v.toString(), mix);
					}
				} else {
					if (v instanceof Map) {
						v = encryptValue((Map<String, Object>) v, mix, k);
					} else if (v instanceof Collection) {
						v = encryptValue((Collection<Object>) v, mix, k);
					}
				}
				BeanUtil.setFieldValue(obj, k, v);
			}
		}
		return obj;
	}

	/**
	 * 加密集合
	 * @param list
	 * @param mix
	 * @param keys
	 * @return
	 */
	private static Collection<Object> encryptValue(Collection<Object> list, boolean mix, String... keys) {
		if (null == list) {
			return list;
		}
		for (Object obj : list) {
			if (obj instanceof Map) {
				obj = encryptValue((Map<String, Object>) obj, mix, keys);
			} else if (obj instanceof Collection) {
				obj = encryptValue((Collection<Object>) obj, mix, keys);
			} else {
				obj = encryptValue(obj, mix, keys);
			}
		}
		return list;
	}

	/**
	 * 加密obj的keys属性值(递归Collection, Map)
	 * @param mix 是否混淆url 生成随机URL用来防止QQ等工具报警,扰乱爬虫
	 * @param obj
	 * @param keys
	 */
	public static Object encrypt(Object obj, boolean mix, String... keys) {
		return encryptValue(obj, mix, keys);
	}
	public static Object encrypt(Object obj, String... keys) {
		return encrypt(obj,false,keys);
	}
	
	/*************************************加密key***********************************************/
	/**
	 * 加密map
	 * @param map
	 * @param mix
	 * @param keys
	 * @return
	 */
	private static Map<String, Object> encryptKey(Map<String, Object> map, boolean mix, String... keys) {
		if (null == map) {
			return map;
		}
		List<String> ks = BeanUtil.getMapKeys(map);
		for (String k : ks) {
			Object v = map.get(k);
			if (null == v || v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) {
				if(null == keys || keys.length == 0 || BasicUtil.contains(keys, k)){
					String key = encryptByType(k, WebUtil.ENCRYPT_TYPE_KEY, mix);
					map.remove(k);
					map.put(key, v);
				}
			} else{
				v = encryptKey(v, mix, keys);
			}
			//map.put(k, v);
		}
		return map;
	}

	
	/**
	 * 加密集合
	 * @param list
	 * @param mix
	 * @param keys
	 * @return
	 */
	private static Collection<Object> encryptKey(Collection<Object> list, boolean mix, String... keys) {
		if (null == list) {
			return list;
		}
		for (Object obj : list) {
			obj = encryptKey(obj, mix, keys);
		}
		return list;
	}

	public static Collection<Object> encryptKey(Collection<Object> list, String... keys) {
		return encryptKey(list, false, keys);
	}
	/**
	 * 加密obj的keys属性值(递归Collection, Map)
	 * @param mix 是否混淆url 生成随机URL用来防止QQ等工具报警
	 * @param obj
	 * @param keys
	 */
	@SuppressWarnings("unchecked")
	public static Object encryptKey(Object obj, boolean mix, String... keys) {
		if (null == obj) {
			return obj;
		}
		if (obj instanceof Map) {
			obj = encryptKey((Map<String, Object>) obj, mix, keys);
		} else if (obj instanceof Collection) {
			obj = encryptKey((Collection<Object>) obj, mix, keys);
		} else {
			//Object无法加密
		}
		return obj;
	}
	public static Object encryptKey(Object obj, String... keys) {
		return encryptKey(obj, false, keys);
	}
	/**
	 * 解析jsp成html 只能解析当前应用下的jsp文件
	 * @param request
	 * @param response
	 * @param file "/WEB-INF/page/index.jsp"
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public static String parseJsp(HttpServletRequest request, HttpServletResponse response, String file) throws ServletException, IOException {
		ServletContext servlet = request.getServletContext();
		RequestDispatcher dispatcher = servlet.getRequestDispatcher(file);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final ServletOutputStream stream = new ServletOutputStream() {
			public void write(byte[] data, int offset, int length) {
				os.write(data, offset, length);
			}
			public void write(int b) throws IOException {
				os.write(b);
			}
			@Override
			public boolean isReady() {
				return false;
			}

			@Override                    
			public void setWriteListener(WriteListener arg0) {}
		};
		final PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));
		HttpServletResponse resp = new HttpServletResponseWrapper(response) {
			public ServletOutputStream getOutputStream() {
				return stream;
			}
			public PrintWriter getWriter() {
				return writer;
			}
		};
		dispatcher.include(request, resp);
		writer.flush();
		String result = os.toString();
		if(ConfigTable.isDebug() && ConfigTable.getBoolean("PARSE_JSP_LOG")){
			log.warn("[LOAD JSP TEMPLATE][FILE:"+file+"][HTML:"+result+"]");
		}
		return result;
	}
	/**
	 * 下载文件
	 * @param response
	 * @param request
	 * @param file
	 * @param title
	 */
	public static boolean writeFile(HttpServletResponse response, File file, String title){
		FileInputStream in = null;
		OutputStream out = null;
		try {
			if (null != file && file.exists()) {
				if(BasicUtil.isEmpty(title)){
					title = file.getName();
				}
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Location", title);
				response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(title,"UTF-8"));
				in = new FileInputStream(file);
				out = response.getOutputStream();
				byte[] buf = new byte[1024];
				int count = 0;
				if(ConfigTable.isDebug()){
					log.info("[文件下载][开始传输][FILE:" + file.getAbsolutePath()+"]");
				}
				long fr = System.currentTimeMillis();
				while ((count = in.read(buf)) >= 0) {
					out.write(buf, 0, count);
				}
				if(ConfigTable.isDebug()){
					log.info("[文件下载][传输完成][FILE:" + file.getAbsolutePath() + "][耗时:"+(System.currentTimeMillis()-fr)+"]");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	public static String getCookie(HttpServletRequest request, String key){
		if(null == key){
			return null;
		}
		Cookie[] cks = request.getCookies();
		if(null != cks){
			for(Cookie ck:cks){
				if(key.equals(ck.getName())){
					return ck.getValue();
				}
			}
		}
		return null;
	}
	public static void setCookie(HttpServletResponse response, String key, String value, int expire){
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(expire);
		response.addCookie(cookie);
	}
	public static String readRequestContent(HttpServletRequest request){
    	StringBuilder sb = new StringBuilder();
    	try{
    		BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream()));
    		String line = null;
			while((line = br.readLine())!=null){
			    sb.append(line);
			}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		return sb.toString();
	}

	public static Map<String, Object> packParam(HttpServletRequest request, String... keys) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (null == keys || keys.length == 0) {
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String key = names.nextElement() + "";
				params.put(key, getParam(request, key));
			}
		} else {
			for (String key : keys) {
				params.put(key, getParam(request, key));
			}
		}
		return params;
	}
	public static Object getParam(HttpServletRequest request, String key){
		String[] values = request.getParameterValues(key);
		if(null == values || values.length ==0){
			return null;
		}else{
			if(values.length ==1){
				return values[0];
			}else{
				return values;
			}
		}
	}
}
