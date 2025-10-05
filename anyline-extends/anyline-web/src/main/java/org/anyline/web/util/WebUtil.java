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
package org.anyline.web.util;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ParseResult;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.adapter.KeyAdapter.KEY_CASE;
import org.anyline.util.*;
import org.anyline.util.encrypt.DESUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.springframework.core.env.Environment;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtil {

	protected static final Log log = LogProxy.get(WebUtil.class);

	// 	public static final String PARAMS_FULL_DECRYPT_MAP = "PARAMS_FULL_DECRYPT_MAP"; // 参数值解密后MAP(整体加密) 加密(k=v&k=v)
	// 	public static final String PARAMS_PART_DECRYPT_MAP = "PARAMS_PART_DECRYPT_MAP"; // 参数值解密后MAP(逐个加密) 加密(k)=加密(v)&加密(k)=加密(v)
	public static final String DECRYPT_PARAM_MAP = "DECRYPT_PARAM_MAP"; // 解析后的值(只存整体加密)
	private static final String PACK_REQUEST_PARAM = "PACK_REQUEST_PARAM";//所有request参数
	/**
	 * 提取远程ip
	 *
	 * @param request  request
	 * @return ip
	 */

	public static String getRemoteIp(HttpServletRequest request) {
		String ip = getRemoteIps(request);
		if(null != ip && ip.contains(",")) {
			ip = ip.substring(0, ip.indexOf(","));
		}
		return ip;
	}
	//get post参数
	public static Map<String,Object> params(HttpServletRequest request) {
		Map<String,Object> params = new HashMap<>();
		Enumeration<String> keys = request.getParameterNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String[] values = request.getParameterValues(key);
			params.put(key, values);
			if(null != values) {
				if(values.length ==0) {
					params.put(key, null);
				}else if(values.length == 1) {
					params.put(key, values[0]);
				}else if(values.length >1) {
					params.put(key, BeanUtil.array2list(values));
				}
			}
		}
		return params;
	}
	/**
	 * 这里需要根据代理中配置解析
	 * @param request  HttpServletRequest
	 * @return ips如果多个IP以,分隔(如经过多层代理转发一般取第0个IP)
	 */
	public static String getRemoteIps(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
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
	 * @param request  request
	 * @return boolean
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String header = request.getHeader("x-requested-with");
		return header != null && "XMLHttpRequest".equals(header);
	}

	/**
	 * 是否是蜘蛛
	 *
	 * @param request  request
	 * @return boolean
	 */
	public static boolean isSpider(HttpServletRequest request) {
		return !hasReffer(request);
	}

	/**
	 * 是否有入口页
	 *
	 * @param request  request
	 * @return boolean
	 */
	public static boolean hasReffer(HttpServletRequest request) {
		if (null == request) {
			return false;
		}
		return (request.getHeader("Referer") != null);
	}

	public static DataSet<DataRow> values(HttpServletRequest request) {
		DataSet<DataRow> set = new DataSet();
		if(null == request) {
			return set;
		}
		String body = WebUtil.read(request,"UTF-8", true);
		if(BasicUtil.isNotEmpty(body) && body.startsWith("[") && body.endsWith("]")) {
			try {
				set = DataSet.parseJson(KEY_CASE.SRC, body);
			}catch(Exception e) {
				log.error("[json parse error][{}]", e.toString());
			}
		}
		return set;
	}
	private static String decode(String src, String encode) {
		// HTTP参数是否开启了ENCODE
		if(ConfigTable.getBoolean("HTTP_PARAM_ENCODE", false)) {
			try{
				return URLDecoder.decode(src, encode);
			}catch (Exception e) {
				return src;
			}
		}
		return src;

	}
	public static Map<String,Object> value(HttpServletRequest request) {
		if(null == request) {
			return new HashMap<String,Object>();
		}
		String charset = "UTF-8";
		boolean isEncode = true;
		if(ConfigTable.HTTP_PARAM_ENCODE == -1) {
			isEncode = false;
		}else if(ConfigTable.HTTP_PARAM_ENCODE == 0) {
			//TODO 自动识别
		}
		Map<String,Object> map = (Map<String,Object>)request.getAttribute(PACK_REQUEST_PARAM);
		if(null == map) {
			//通过ur形式提交的参数
			String params = request.getQueryString();
			if(BasicUtil.isNotEmpty(params)) {
				map = BeanUtil.param2map(params, true, isEncode);
			}else {
				map = new HashMap<String, Object>();
			}
			// body体json格式(ajax以raw提交)
			String body = WebUtil.read(request, charset,true);
			if(BasicUtil.isNotEmpty(body)) {
				body = body.trim();
				if(body.startsWith("{") && body.endsWith("}")) {
					map.putAll(DataRow.parseJson(KEY_CASE.SRC, body));
				}else{
					//先拆分 再解码,否则解出来 & = 会混淆
					map.putAll(BeanUtil.param2map(body,true, isEncode));
				}
			}else {
				// utl与form表单格式
				Enumeration<String> keys = request.getParameterNames();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement() + "";
					if(map.containsKey(key)) {
						continue;
					}
					String[] values = request.getParameterValues(key);
					if (null != values) {
						if (values.length == 1) {
							String v = values[0];
							if(null != v) {
								if(ConfigTable.IS_HTTP_PARAM_AUTO_TRIM) {
									v = v.trim();
								}
								if(isEncode) {
									map.put(key, decode(v, charset));
								}else{
									map.put(key, v);
								}
							}else{
								map.put(key, null);
							}
						} else if (values.length > 1) {
							List<Object> list = new ArrayList<Object>();
							for (String value : values) {
								if(null != value) {
									if(ConfigTable.IS_HTTP_PARAM_AUTO_TRIM) {
										value = value.trim();
									}
									if(isEncode) {
										value = decode(value, "UTF-8");
									}
								}
								list.add(value);
							}
							map.put(key, list);
						}
					}
					map.put(DECRYPT_PARAM_MAP, decryptParam(request));
				}
			}

			request.setAttribute(PACK_REQUEST_PARAM, map);
		}
		return map;
	}
	/**
	 * 从解密后的参数MAP中取值
	 *
	 * @param request  request
	 * @param key  key key
	 * @param valueEncrypt  valueEncrypt value是否加密
	 * @return List
	 */
//	@SuppressWarnings("unchecked")
//	private static List<Object> getHttpRequestParamsFormDecryptMap(HttpServletRequest request, String key, boolean valueEncrypt) {
//		List<Object> result = new ArrayList<Object>();
//		Map<String,Map<String,List<String>>> decryptMap = decryptParam(request);
//
//		// 整体加密数据
//		Map<String, List<String>> fullMap = (Map<String, List<String>>) request.getAttribute(PARAMS_FULL_DECRYPT_MAP);
//		List<String> values = fullMap.get(key);
//		if (null != values) {
//			result.addAll(values);
//		} else {
//			// 逐个加密数据
//			Map<String, List<String>> partMap = (Map<String, List<String>>) request.getAttribute(PARAMS_PART_DECRYPT_MAP);
//			values = partMap.get(DESUtil.encryptParamKey(key));
//			if (null != values) {
//				if (valueEncrypt) {
//					for (String value : values) {
//						value = DESUtil.decryptParamValue(value);
//						result.add(value);
//					}
//				} else {
//					result.addAll(values);
//				}
//			}
//		}
//		return result;
//	}

//	/*
//	 * 从解密后的参数MAP中取值
//	 *
//	 * @param request  request
//	 * @param key  key
//	 * @param valueEncrypt  valueEncrypt
//	 * @return String
//	 */
//	@SuppressWarnings("unused")
//	private static String getHttpRequestParamFormDecryptMap(HttpServletRequest request, String key, boolean valueEncrypt) {
//		String result = null;
//		List<Object> list = getHttpRequestParamsFormDecryptMap(request, key,valueEncrypt);
//		if (null != list && list.size() > 0) {
//			Object tmp = list.get(0);
//			if (null != tmp) {
//				result = tmp.toString().trim();
//			}
//		}
//		return result;
//	}

	/**
	 * 解密httprequet参数及参数值
	 *
	 * @param request  request
	 */
	private static Map<String,List<String>> decryptParam(HttpServletRequest request) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		// 整体加密格式
		String value = request.getQueryString();
		if (null != value) {
			if (value.contains("&")) {
				value = value.substring(0, value.indexOf("&"));
			}
			try {
				value = DESUtil.decryptParam(value);
			}catch(Exception e) {

			}
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
						// v = DESUtil.filterIllegalChar(v);
					}
					if(null != v) {
						v = v.trim();
					}
					if (!"".equals(k) && !"".equals(v)) {
						List<String> list = result.get(k);
						if (null == list) {
							list = new ArrayList<>();
							result.put(k, list);
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

			List<String> list = result.get(k);
			if (null == list) {
				list = new ArrayList<>();
				result.put(k, list);
			}
			vs = request.getParameterValues(k);
			if (null != vs) {
				for (String v : vs) {
					if (null != v && !"".equals(v)) {
						// v = DESUtil.filterIllegalChar(v);
						list.add(decode(v.trim(),"UTF-8"));
					}
				}
			}

		}
		return result;
	}

	/**
	 * 解密value
	 * @param value value
	 * @return String
	 */
	public static String decrypt(String value) {
		return ConfigParser.decryptParamValue(value);
	}
	public static String decryptValue(String value) {
		return ConfigParser.decryptParamValue(value);
	}
	public static String decryptKey(String value) {
		return ConfigParser.decryptParamKey(value);
	}

	public static String decrypt(String value, String def) {
		value = decrypt(value);
		if(BasicUtil.isEmpty(value)) {
			return def;
		}
		return value;
	}
	// 	/**
//	 * http request参数
//	 *
//	 * @param request  request
//	 * @param key  key
//	 * @param keyEncrypt  keyEncrypt
//	 *            key是否加密
//	 * @param valueEncrypt  valueEncrypt
//	 *            value是否加密
//	 * @return List
//	 */
	public static List<Object> getHttpRequestParams(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) {
		List<Object> result = new ArrayList<>();
		if (null == request || null == key) {
			return null;
		}
		Map<String,Object> values = value(request);

		ParseResult parser = new ParseResult();
		parser.setKey(key);
		parser.setKeyEncrypt(keyEncrypt);
		parser.setValueEncrypt(valueEncrypt);
		parser.setParamFetchType(Config.FETCH_REQUEST_VALUE_TYPE_MULTIPLE);
		result = ConfigParser.getValues(values, parser);
//		String values[] = null;
//		if (keyEncrypt) {
//			// key已加密
//			ParseResult parser = new ParseResult();
//			parser.setKey(key);
//			parser.setKeyEncrypt(keyEncrypt);
//			parser.setValueEncrypt(valueEncrypt);
//			result = ConfigParser.getValues(values(request), parser);
//		} else {
//			// key未加密
//			values = request.getParameterValues(key);
//			if (null != values) {
//				for (String value : values) {
//					if (null == value) {
//						result.add("");
//					}
//					if (valueEncrypt) {
//						value = DESUtil.decryptParamValue(value);
//						value = DESUtil.filterIllegalChar(value);
//					}
//					if (null != value) {
//						value = value.trim();
//						value = DESUtil.filterIllegalChar(value);
//					}
//					result.add(value);
//				}
//			}
//		}
		return result;
	}

	public static List<Object> getHttpRequestParams(HttpServletRequest request, String param, boolean keyEncrypt) {
		return getHttpRequestParams(request, param, keyEncrypt, false);
	}

	public static List<Object> getHttpRequestParams(HttpServletRequest request, String param) {
		return getHttpRequestParams(request, param, false, false);
	}
	// 
//	/*
//	 * HTTP参数
//	 *
//	 * @param request  request
//	 * @param key  key
//	 *            参数名
//	 * @param keyEncrypt  keyEncrypt
//	 *            参数名是否加密过
//	 * @param valueEncrypt  valueEncrypt
//	 *            参数值是否加密过,是则解密
//	 * @return Object
//	 */
	public static Object getHttpRequestParam(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) {
		String result = null;
		List<Object> list = getHttpRequestParams(request, key, keyEncrypt, valueEncrypt);
		if(null != list && list.size()>0) {
			result = (String)list.get(0);
		}
		return result;
	}

	public static Object getHttpRequestParam(HttpServletRequest request, String param, boolean keyEncrypt) {
		return getHttpRequestParam(request, param, keyEncrypt, false);
	}

	public static Object getHttpRequestParam(HttpServletRequest request, String param) {
		return getHttpRequestParam(request, param, false, false);
	}

	/**
	 * 解析IP
	 *
	 * @param ip  ip
	 * @return long
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
	 * @param ipNum  ipNum
	 * @return String
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
	 * 提取refer的uri
	 *
	 * @param request  request
	 * @return String
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
	 * @param request request
	 * @return boolean
	 */
	public static boolean isWap(HttpServletRequest request) {
		boolean result = false;
		try{
			String agent = request.getHeader("user-agent");
			if(null == agent) {
				return false;
			}
			String agentcheck = agent.trim().toLowerCase();
			String[] keywords = {"mobile", "android", "symbianos", "iphone",
					"wp\\d*", "windows phone", "mqqbrowser", "nokia", "samsung",
					"midp-2", "untrusted/1.0", "windows ce", "blackberry", "ucweb",
					"brew", "j2me", "yulong", "coolpad", "tianyu", "ty-",
					"k-touch", "haier", "dopod", "lenovo", "huaqin", "aigo-",
					"ctc/1.0", "ctc/2.0", "cmcc", "daxian", "mot-", "sonyericsson",
					"gionee", "htc", "zte", "huawei", "webos", "gobrowser",
					"iemobile", "wap2.0", "WAPI" };
			Pattern pf = Pattern.compile("wp\\d*");
			Matcher mf = pf.matcher(agentcheck);
			if (agentcheck != null && (!agentcheck.contains("windows nt") && agentcheck .indexOf("Ubuntu") == -1)
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
		}catch(Exception e) {

		}
		return result;
	}
	/**
	 * 是否本地访问
	 * @param request request
	 * @return boolean
	 */
	public static boolean isLocal(HttpServletRequest request) {
		String ip = getRemoteIp(request);
		return ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip));
	}
	/**
	 * 是否微信调用
	 * @param request request
	 * @return boolean
	 */
	public static boolean isWechat(HttpServletRequest request) {
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		return userAgent.indexOf("micromessenger") > -1;
	}
	public static boolean isWechatApp(HttpServletRequest request) {
		String referer = request.getHeader("referer");
		if(null != referer && referer.startsWith("https://servicewechat.com")) {
			return true;
		}
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		return userAgent.indexOf("micromessenger") > -1 && userAgent.indexOf("miniprogram") > -1;
	}
	public static boolean isApp(HttpServletRequest request) {
		if(null == request) {
			return false;
		}
		if(null == request.getSession()) {
			return false;
		}
		String isApp = request.getSession().getAttribute("_IS_APP")+"";
		return "1".equals(isApp);
	}

	/**
	 * 是否支付宝调用
	 * @param request request
	 * @return boolean
	 */
	public static boolean isAlipay(HttpServletRequest request) {
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		return userAgent.indexOf("alipayclient") > -1;
	}
	/**
	 * 是否QQ调用
	 * @param request request
	 * @return boolean
	 */
	public static boolean isQQ(HttpServletRequest request) {
		String userAgent = (request.getHeader("user-agent")+"").toLowerCase();
		return userAgent.indexOf("qq/") > -1;
	}

	/**
	 * 是否android调用
	 * @param request request
	 * @return boolean
	 */
	public static boolean isAndroid(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent").toLowerCase();
		return userAgent.indexOf("android") > -1;
	}
	/**
	 * 是否android调用
	 * @param request request
	 * @return boolean
	 */
	public static boolean isIphone(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent").toLowerCase();
		return userAgent.indexOf("iphone") > -1;
	}
	public static String clientType(HttpServletRequest request) {
		String type = "";
		if(isApp(request)) {
			type = "app";
		}else if(isWechat(request)) {
			type = "wechat";
		}else if(isQQ(request)) {
			type = "qq";
		}else if(isAlipay(request)) {
			type = "alipay";
		}else if(isWap(request)) {
			type = "wap";
		}else{
			type = "web";
		}
		return type;
	}
	/**
	 * 加密map
	 * @param map  map
	 * @param mix  mix
	 * @param keys  keys
	 * @param fixs  fixs
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> encryptValue(Map<String, Object> map, boolean mix, List<String> fixs, String... keys) {
		if (null == map) {
			return map;
		}
		fixs = BeanUtil.merge(fixs, keys);
		List<String> ks = BeanUtil.getMapKeys(map);
		for(String k:ks) {
			Object v = map.get(k);
			if(null == v) {
				continue;
			}

			if(v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) {
				if(null == fixs || fixs.isEmpty() || fixs.contains(k)) {
					v = encryptValue(v.toString(), mix);
				}
			}else{
				if (v instanceof Map) {
					v = encryptValue((Map<String, Object>) v, mix, fixs, keys);
				} else if (v instanceof Collection) {
					v = encryptValue((Collection<Object>) v, mix, fixs, keys);
				} else {
					v = encryptValue(v, mix, fixs, keys);
				}
			}
			map.put(k, v);
		}

		return map;
	}

	private static Map<String, Object> encryptValue(Map<String, Object> map, boolean mix, String[] fixs, String... keys) {
		return encryptValue(map, mix, BeanUtil.array2list(fixs, keys));
	}

	private static Map<String, Object> encryptValue(Map<String, Object> map, boolean mix, String... keys) {
		return encryptValue(map, mix, BeanUtil.array2list(keys));
	}

	/**
	 * 加密对象
	 *
	 * @param obj  obj
	 * @param mix  mix
	 * @param fixs  fixs
	 * @param keys  keys
	 * @return Object
	 */
	@SuppressWarnings("unchecked")
	private static Object encryptValue(Object obj, boolean mix, List<String> fixs, String... keys) {
		if (null == obj) {
			return obj;
		}
		if(obj instanceof DataSet<DataRow> && ((DataSet)obj).isFromCache()){
			return obj;
		}
		if(obj instanceof DataRow && ((DataRow)obj).isFromCache()){
			return obj;
		}
		List<String> list = BeanUtil.merge(fixs, keys);
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Date) {
			//
			return DESUtil.encryptValue(obj.toString(),mix);
		}
		if(obj instanceof String[]){
			String[] arrs = (String[])obj;
			int len = arrs.length;
			for(int i=0; i<len; i++){
				arrs[i] = (String)encryptValue(arrs[i], mix, list);
			}
		}else if (obj instanceof Map) {
			obj = encryptValue((Map<String, Object>) obj, mix, fixs, keys);
		} else if (obj instanceof Collection) {
			obj = encryptValue((Collection<Object>) obj, mix, fixs, keys);
		} else {
			// Object无法加密
			List<String> ks = ClassUtil.getFieldsName(obj.getClass());
			for (String k : ks) {
				Object v = BeanUtil.getFieldValue(obj, k);
				if (null == v) {
					continue;
				}
				if(v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) {
					if(null == list || list.isEmpty() || list.contains(k)) {
						v = encryptValue(v.toString(), mix, list);
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
	private static Object encryptValue(Object obj, boolean mix, String[] fixs, String... keys) {
		return encryptValue(obj, mix, BeanUtil.array2list(fixs, keys));
	}
	private static Object encryptValue(Object obj, boolean mix, String... keys) {
		return encryptValue(obj, mix, BeanUtil.array2list(keys));
	}

	/**
	 * 加密集合
	 * @param list  list
	 * @param mix  mix
	 * @param keys  keys
	 * @return Collection
	 */
	@SuppressWarnings("unchecked")
	private static Collection<Object> encryptValue(Collection<Object> list, boolean mix, List<String> fixs, String... keys) {
		if (null == list) {
			return list;
		}
		for (Object obj : list) {
			if (obj instanceof Map) {
				obj = encryptValue((Map<String, Object>) obj, mix, fixs, keys);
			} else if (obj instanceof Collection) {
				obj = encryptValue((Collection<Object>) obj, mix, fixs, keys);
			} else {
				obj = encryptValue(obj, mix, fixs, keys);
			}
		}
		return list;
	}
	private static Collection<Object> encryptValue(Collection<Object> list, boolean mix, String[] fixs, String... keys) {
		return encryptValue(list, mix, BeanUtil.array2list(fixs, keys));
	}

	/**
	 * 加密obj的keys属性值(递归Collection, Map)
	 * @param mix 是否混淆url 生成随机URL用来防止QQ等工具报警,扰乱爬虫
	 * @param obj  obj
	 * @param keys  keys
	 * @return Object
	 */
	public static Object encrypt(Object obj, boolean mix, String... keys) {
		return encryptValue(obj, mix, keys);
	}
	public static Object encrypt(Object obj, boolean mix, List<String> fixs, String... keys) {
		return encryptValue(obj, mix, BeanUtil.merge(fixs, keys));
	}
	public static Object encrypt(Object obj, boolean mix, String[] fixs, String... keys) {
		return encryptValue(obj, mix, BeanUtil.array2list(fixs, keys));
	}
	public static Object encrypt(Object obj, String... keys) {
		return encrypt(obj,false,keys);
	}
	public static Object encrypt(Object obj, List<String> fixs, String... keys) {
		return encrypt(obj,false,BeanUtil.merge(fixs, keys));
	}
	public static Object encrypt(Object obj, String[] fixs, String... keys) {
		return encrypt(obj,false, BeanUtil.array2list(fixs, keys));
	}
	/**
	 * 解析jsp成html 只能解析当前应用下的jsp文件
	 * @param request request
	 * @param response response
	 * @param jsp "/WEB-INF/page/index.jsp"
	 * @return String
	 * @throws ServletException ServletException
	 * @throws IOException IOException
	 */
	public static String render(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		render(request, response, jsp, os, false);
		String result = os.toString();
		if(ConfigTable.IS_DEBUG && ConfigTable.getBoolean("PARSE_JSP_LOG")) {
			log.warn("[LOAD JSP TEMPLATE][FILE:{}][HTML:{}]", jsp, result);
		}
		return result;
	}
	public static String parseJsp(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
		return render(request, response, jsp);
	}
	public static void render(HttpServletRequest request, HttpServletResponse response, String jsp, File target) throws ServletException, IOException {
		render(request, response, jsp, Files.newOutputStream(target.toPath()), true);
	}

	/**
	 * JSP解析
	 * @param request request
	 * @param response response
	 * @param jsp jsp文件path以根据目录/开始
	 * @param os 输出到os
	 * @param close 关闭输出流
	 * @throws ServletException ServletException
	 * @throws IOException IOException
	 */
	public static void render(HttpServletRequest request, HttpServletResponse response, String jsp, final  OutputStream os, boolean close) throws ServletException, IOException {
		ServletContext servlet = request.getServletContext();
		RequestDispatcher dispatcher = servlet.getRequestDispatcher(jsp);
		final ServletOutputStream stream = new ServletOutputStream() {

			@Override
			public void write(byte[] data, int offset, int length) {
				try {
					os.write(data, offset, length);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void write(int b) throws IOException {
				// os.write(b);
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
		if(close) {
			try{
				os.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下载文件
	 * @param request request
	 * @param response response
	 * @param file file
	 * @param title title
	 * @return boolean
	 */
	public static boolean download(HttpServletRequest request, HttpServletResponse response, File file, String title) {
		try{
			if (null != file && file.exists()) {
				if(BasicUtil.isEmpty(title)) {
					title = file.getName();
				}
				return download(request, response, Files.newInputStream(file.toPath()), title);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean download(HttpServletRequest request, HttpServletResponse response, File file) {
		try{
			if (null != file && file.exists()) {
				return download(request, response, Files.newInputStream(file.toPath()), file.getName());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public static boolean download(HttpServletResponse response, File file) {
		return download(null, response, file, file.getName());
	}
	public static boolean download(HttpServletResponse response, File file, String title) {
		return download(null, response, file, title);
	}
	public static boolean download(HttpServletRequest request, HttpServletResponse response, String txt, String title) {
		try{
			return download(request, response, new ByteArrayInputStream(txt.getBytes()), title);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean download(HttpServletResponse response, String txt, String title) {
		return download(null, response, txt, title);
	}
	public static boolean download(HttpServletResponse response, InputStream in, String title) {
		return download(null, response, in, title);
	}

	/**
	 * 设置下载header
	 * @param request request
	 * @param response response
	 * @param title title
	 */
	public static void download(HttpServletRequest request, HttpServletResponse response, String title) {
		try{
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Location",   title );
			if(null != request) {
				title = encode(request, title);
			}else {
				title = URLEncoder.encode(title, "utf-8");
			}
			String content = "attachment; filename*=utf-8'zh_cn'" + title;
			response.setHeader("Content-Disposition", content);
			String contentType = "application/x-download";
			if(BasicUtil.isNotEmpty(title)) {
				String subName = FileUtil.parseSubName(title);
				if(null != subName) {
					int idx = FileUtil.httpFileExtend.indexOf(subName);
					if(idx != -1) {
						contentType = FileUtil.httpFileType.get(idx);
					}
				}
			}
			response.setContentType(contentType);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 下载文件
	 * @param request request
	 * @param response response
	 * @param in in
	 * @param title title
	 * @return boolean
	 */
	public static boolean download(HttpServletRequest request, HttpServletResponse response, InputStream in, String title) {
		OutputStream out = null;
		try {
			download(request, response, title);
			out = response.getOutputStream();
			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
				out.write(buf, 0, count);
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
	public static String encode(HttpServletRequest request, String value) throws IOException {
		String agent = request.getHeader("User-Agent").toLowerCase(); // 获取浏览器
		if (agent.contains("firefox")) {
			value = URLEncoder.encode(value, "utf-8");
		} else if(agent.contains("msie")) {
			value = URLEncoder.encode(value, "utf-8");
		} else if(agent.contains ("chrome")) {
			value = URLEncoder.encode(value, "utf-8");
		} else if(agent.contains ("safari")) {
			// value = new String (value.getBytes ("utf-8"),"ISO8859-1");
			value = URLEncoder.encode(value, "utf-8");
		} else {
			value = URLEncoder.encode(value, "utf-8");
		}
		return value;
	}
	public static String getCookie(HttpServletRequest request, String key) {
		if(null == key) {
			return null;
		}
		Cookie[] cks = request.getCookies();
		if(null != cks) {
			for(Cookie ck:cks) {
				if(key.equals(ck.getName())) {
					return ck.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 设置cookie
	 * @param response response
	 * @param key key
	 * @param value value
	 * @param expire 过期时间(秒)
	 */
	public static void setCookie(HttpServletResponse response, String key, String value, int expire) {
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(expire);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String key) {
		if(null == key) {
			return;
		}
		Cookie[] cks = request.getCookies();
		if(null != cks) {
			for(Cookie ck:cks) {
				if(key.equals(ck.getName())) {
					ck.setValue(null);
					ck.setMaxAge(0);
					response.addCookie(ck);
				}
			}
		}
	}
	public static String readRequestContent(HttpServletRequest request, String charset) {
		StringBuilder sb = new StringBuilder();
		try{
			BufferedReader br = null;
			if(null != charset) {
				br = new BufferedReader(new InputStreamReader(request.getInputStream(), charset));
			}else{
				br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			}
			String line = null;
			while((line = br.readLine())!=null) {
				sb.append(line);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	public static String readRequestContent(HttpServletRequest request) {
		return readRequestContent(request, "UTF-8");
	}

	public static Map<String, Object> packParam(HttpServletRequest request, String... keys) {
		Map<String, Object> params = new HashMap<>();
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
	public static Object getParam(HttpServletRequest request, String key) {
		String[] values = request.getParameterValues(key);
		if(null == values || values.length ==0) {
			return null;
		}else{
			int len = values.length;
			for(int i=0; i<len; i++) {
				values[i] = decode(values[i],"UTF-8");
			}
			if(values.length ==1) {
				return values[0];
			}else{
				return values;
			}
		}
	}


	/**
	 * 根据配置文件设置对象属性值
	 * @param obj 对象
	 * @param prefix 前缀
	 * @param env 配置文件环境
	 */
	public static void setFieldsValue(Object obj, String prefix, Environment env ) {
		List<String> fields = ClassUtil.getFieldsName(obj.getClass());
		for(String field:fields) {
			String value = getProperty(prefix, env, field);
			if(BasicUtil.isNotEmpty(value)) {
				BeanUtil.setFieldValue(obj, field, value);
			}
		}
	}

	/**
	 * 根据配置文件提取指定key的值
	 * @param prefix 前缀
	 * @param env 配置文件环境
	 * @param keys key列表 第一个有值的key生效
	 * @return String
	 */
	public static String getProperty(String prefix, Environment env, String ... keys) {
		String value = null;
		if(null == env || null == keys) {
			return value;
		}
		if(null == prefix) {
			prefix = "";
		}
		for(String key:keys) {
			key = prefix + key;
			value = env.getProperty(key);
			if(null != value) {
				return value;
			}
			// 以中划线分隔的配置文件
			String[] ks = key.split("-");
			StringBuilder sKey = null;
			for(String k:ks) {
				if(null == sKey) {
					sKey = new StringBuilder(k);
				}else{
					sKey.append(CharUtil.toUpperCaseHeader(k));
				}
			}
			value = env.getProperty(sKey.toString());
			if(null != value) {
				return value;
			}

			// 以下划线分隔的配置文件
			ks = key.split("_");
			sKey = null;
			for(String k:ks) {
				if(null == sKey) {
					sKey = new StringBuilder(k);
				}else{
					sKey.append(CharUtil.toUpperCaseHeader(k));
				}
			}
			value = env.getProperty(sKey.toString());
			if(null != value) {
				return value;
			}
		}
		return value;
	}

	/**
	 * 读取request body
	 * @param request request
	 * @param charset 编码
	 * @param cache 是否缓存(第二次reqad是否有效)
	 * @return String
	 */
	public static String read(HttpServletRequest request, String charset, boolean cache) {
		try {
			String str = new String(read(request,cache), charset);
			//str = decode(str,encode);
			return str;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String read(HttpServletRequest request, String encode) {
		return read(request, encode, true);
	}
	public static byte[] read(HttpServletRequest request, boolean cache) {
		byte[] buffer = (byte[])request.getAttribute("_anyline_request_read_cache_byte");
		if(null != buffer) {
			return buffer;
		}
		buffer = new byte[1024];
		ServletInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = request.getInputStream();
			if(null != in) {
				out = new ByteArrayOutputStream();
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				buffer = out.toByteArray();
				if (cache) {
					request.setAttribute("_anyline_request_read_cache_byte", buffer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer;
	}

	public static byte[] read(HttpServletRequest request) {
		return read(request, true);
	}
}
