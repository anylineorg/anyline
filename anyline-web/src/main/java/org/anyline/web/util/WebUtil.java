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


package org.anyline.web.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class WebUtil {

	protected static final Logger log = LoggerFactory.getLogger(WebUtil.class);

	//	public static final String PARAMS_FULL_DECRYPT_MAP = "PARAMS_FULL_DECRYPT_MAP"; // 参数值解密后MAP(整体加密) 加密(k=v&k=v)
//	public static final String PARAMS_PART_DECRYPT_MAP = "PARAMS_PART_DECRYPT_MAP"; // 参数值解密后MAP(逐个加密) 加密(k)=加密(v)&加密(k)=加密(v)
	public static final String DECRYPT_PARAM_MAP = "DECRYPT_PARAM_MAP"; //解析后的值(只存整体加密)
	private static final String PACK_REQUEST_PARAM = "PACK_REQUEST_PARAM";//所有request参数
	/**
	 * 提取clicent真实ip
	 *
	 * @param request  request
	 * @return return
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
	 * @param request  request
	 * @return return
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
	 * @param request  request
	 * @return return
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
	 * @param request  request
	 * @return return
	 */
	public static boolean hasReffer(HttpServletRequest request) {
		if (null == request) {
			return false;
		}
		return (request.getHeader("Referer") != null);
	}


	public static Map<String,Object> values(HttpServletRequest request){
		if(null == request){
			return new HashMap<String,Object>();
		}
		Map<String,Object> map = (Map<String,Object>)request.getAttribute(PACK_REQUEST_PARAM);
		if(null == map){
			map = new HashMap<String,Object>();
			Enumeration<String> keys = request.getParameterNames();
			while(keys.hasMoreElements()){
				String key = keys.nextElement()+"";
				String[] values = request.getParameterValues(key);
				if(null != values){
					if(values.length == 1){
						map.put(key, values[0]);
					}else if(values.length > 1){
						List<Object> list = new ArrayList<Object>();
						for(String value:values){
							list.add(value);
						}
						map.put(key, list);
					}
				}
				map.put(DECRYPT_PARAM_MAP, decryptParam(request));
				request.setAttribute(PACK_REQUEST_PARAM, map);
			}
		}
		return map;
	}
	/**
	 * 从解密后的参数MAP中取值
	 *
	 * @param request  request
	 * @param key  key key
	 * @param valueEncrypt  valueEncrypt value是否加密
	 * @return return
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
//	 * @return return
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
			}catch(Exception e){

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
						//v = DESUtil.filterIllegalChar(v);
					}
					if (!"".equals(k) && !"".equals(v)) {
						List<String> list = result.get(k);
						if (null == list) {
							list = new ArrayList<String>();
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
				list = new ArrayList<String>();
				result.put(k, list);
			}
			vs = request.getParameterValues(k);
			if (null != vs) {
				for (String v : vs) {
					if (null != v && !"".equals(v)) {
						//v = DESUtil.filterIllegalChar(v);
						list.add(v.trim());
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
	public static String decrypt(String value){
		return ConfigParser.decryptParamValue(value);
	}
	public static String decryptValue(String value){
		return ConfigParser.decryptParamValue(value);
	}
	public static String decryptKey(String value){
		return ConfigParser.decryptParamKey(value);
	}

	public static String decrypt(String value, String def){
		value = decrypt(value);
		if(BasicUtil.isEmpty(value)){
			return def;
		}
		return value;
	}
	//	/**
//	 * http request参数
//	 *
//	 * @param request  request
//	 * @param key  key
//	 * @param keyEncrypt  keyEncrypt
//	 *            key是否加密
//	 * @param valueEncrypt  valueEncrypt
//	 *            value是否加密
//	 * @return return
//	 */
	public static List<Object> getHttpRequestParams(HttpServletRequest request,String key, boolean keyEncrypt, boolean valueEncrypt) {
		List<Object> result = new ArrayList<Object>();
		if (null == request || null == key) {
			return null;
		}
		Map<String,Object> values = values(request);

		ParseResult parser = new ParseResult();
		parser.setKey(key);
		parser.setKeyEncrypt(keyEncrypt);
		parser.setValueEncrypt(valueEncrypt);
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

	public static List<Object> getHttpRequestParams(HttpServletRequest request,  String param, boolean keyEncrypt) {
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
//	 * @return return
//	 */
	public static Object getHttpRequestParam(HttpServletRequest request,String key, boolean keyEncrypt, boolean valueEncrypt) {
		String result = "";
		List<Object> list = getHttpRequestParams(request, key, keyEncrypt, valueEncrypt);
		if(null != list && list.size()>0){
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
	 * @return return
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
	 * @return return
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
	 * @return return
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
	 * @return return
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
	 * @param request request
	 * @return return
	 */
	public static boolean isLocal(HttpServletRequest request){
		String ip = getRemoteIp(request);
		return ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip));
	}
	/**
	 * 是否微信调用
	 * @param request request
	 * @return return
	 */
	public static boolean isWechat(HttpServletRequest request){
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
	 * @param request request
	 * @return return
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
	 * @param request request
	 * @return return
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
	 * @param request request
	 * @return return
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
	 * @param request request
	 * @return return
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
		}else if(isWechat(request)){
			type = "wechat";
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
	 * @param map  map
	 * @param mix  mix
	 * @param keys  keys
	 * @return return
	 */
	@SuppressWarnings("unchecked")
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
	 * @param obj  obj
	 * @param mix  mix
	 * @param keys  keys
	 * @return return
	 */
	@SuppressWarnings("unchecked")
	private static Object encryptValue(Object obj, boolean mix, String... keys) {
		if (null == obj) {
			return obj;
		}
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Date) {
			return DESUtil.encryptValue(obj.toString(),mix);
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
	 * @param list  list
	 * @param mix  mix
	 * @param keys  keys
	 * @return return
	 */
	@SuppressWarnings("unchecked")
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
	 * @param obj  obj
	 * @param keys  keys
	 * @return return
	 */
	public static Object encrypt(Object obj, boolean mix, String... keys) {
		return encryptValue(obj, mix, keys);
	}
	public static Object encrypt(Object obj, String... keys) {
		return encrypt(obj,false,keys);
	}

	/**
	 * 解析jsp成html 只能解析当前应用下的jsp文件
	 * @param request request
	 * @param response response
	 * @param file "/WEB-INF/page/index.jsp"
	 * @return return
	 * @throws ServletException ServletException
	 * @throws IOException IOException
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
			log.warn("[LOAD JSP TEMPLATE][FILE:{}][HTML:{}]", file, result);
		}
		return result;
	}
	/**
	 * 下载文件
	 * @param response response
	 * @param file file
	 * @param title title
	 * @return return
	 */
	public static boolean download(HttpServletResponse response, File file, String title){
		try{
			if (null != file && file.exists()) {
				if(BasicUtil.isEmpty(title)){
					title = file.getName();
				}
				return download(response, new FileInputStream(file), title);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public static boolean download(HttpServletResponse response, String txt, String title){
		try{
			return download(response, new ByteArrayInputStream(txt.getBytes()), title);
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 下载文件
	 * @param response response
	 * @param in in
	 * @param title title
	 * @return return
	 */
	public static boolean download(HttpServletResponse response, InputStream in, String title){
		OutputStream out = null;
		try {
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Location", title);
			response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(title,"UTF-8"));
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

	/**
	 * 设置cookie
	 * @param response response
	 * @param key key
	 * @param value value
	 * @param expire 过期时间(秒)
	 */
	public static void setCookie(HttpServletResponse response, String key, String value, int expire){
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(expire);
		response.addCookie(cookie);
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String key){
		if(null == key){
			return;
		}
		Cookie[] cks = request.getCookies();
		if(null != cks){
			for(Cookie ck:cks){
				if(key.equals(ck.getName())){
					ck.setValue(null);
					ck.setMaxAge(0);
					response.addCookie(ck);
				}
			}
		}
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

	/**
	 * 根据配置文件设置对象属性值
	 * @param obj 对象
	 * @param prefix 前缀
	 * @param env 配置文件环境
	 */
	public static void setFieldsValue(Object obj, String prefix, Environment env ){
		List<String> fields = BeanUtil.getFieldsName(obj.getClass());
		for(String field:fields){
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
	public static String getProperty(String prefix, Environment env, String ... keys){
		String value = null;
		if(null == env || null == keys){
			return value;
		}
		if(null == prefix){
			prefix = "";
		}
		for(String key:keys){
			key = prefix + key;
			value = env.getProperty(key);
			if(null != value){
				return value;
			}
			//以中划线分隔的配置文件
			String[] ks = key.split("-");
			String sKey = null;
			for(String k:ks){
				if(null == sKey){
					sKey = k;
				}else{
					sKey = sKey + CharUtil.toUpperCaseHeader(k);
				}
			}
			value = env.getProperty(sKey);
			if(null != value){
				return value;
			}

			//以下划线分隔的配置文件
			ks = key.split("_");
			sKey = null;
			for(String k:ks){
				if(null == sKey){
					sKey = k;
				}else{
					sKey = sKey + CharUtil.toUpperCaseHeader(k);
				}
			}
			value = env.getProperty(sKey);
			if(null != value){
				return value;
			}
		}
		return value;
	}
	public static String read(HttpServletRequest request, String encode){
		try {
			return new String(read(request), encode);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public static byte[] read(HttpServletRequest request) {
		int len = request.getContentLength();
		byte[] buffer = new byte[len];
		ServletInputStream in = null;
		try {
			in = request.getInputStream();
			in.read(buffer, 0, len);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer;
	}
}
