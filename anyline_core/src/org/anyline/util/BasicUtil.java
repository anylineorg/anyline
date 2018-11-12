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

import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.sf.json.JSONObject;

import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;

public class BasicUtil {
	private static Logger log = Logger.getLogger(BasicUtil.class);

	/**
	 * 是否为空或""或"null"(大写字母"NULL"不算空) 集合对象检查是否为空或集合中是否有对象
	 * 
	 * @param obj
	 * @param recursion
	 *            是否递归查检集合对象
	 * @return
	 */
	public static boolean isEmpty(boolean recursion, Object obj) {
		if (null == obj) {
			return true;
		}
		if (obj instanceof Collection && recursion) {
			Collection collection = (Collection) obj;
			for (Object item : collection) {
				if (!isEmpty(recursion, item)) {
					return false;
				}
			}
		} else if (obj instanceof Map && recursion) {
			Map map = (Map) obj;
			for (Iterator itrKey = map.keySet().iterator(); itrKey.hasNext();) {
				if (!isEmpty(map.get(itrKey.next()))) {
					return false;
				}
			}
		} else {
			String tmp = obj.toString().trim();
			if (!tmp.equals("") && !tmp.equals("null")) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(Object obj) {
		return isEmpty(false, obj);
	}


	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(false, obj);
	}
	public static boolean isNotEmpty(boolean recursion, Object obj) {
		return !isEmpty(recursion, obj);
	}

	public static boolean isEqual(Object obj1, Object obj2) {
		if (null == obj1) {
			if (null == obj2) {
				return true;
			} else {
				return obj2.equals(obj1);
			}
		} else {
			return obj1.equals(obj2);
		}
	}

	/**
	 * nvl 取第一个不为null的值,没有符合条件的 则返回null
	 * 
	 * @param recursion
	 *            对于集合变量,是否递归
	 * @param values
	 * @return
	 */
	public static Object nvl(boolean recursion, Object... values) {
		if (null == values) {
			return null;
		}
		for (Object item : values) {
			if ("".equals(item) || isNotEmpty(recursion, item)) {
				return item;
			}
		}
		return null;
	}
	public static Object nvl(boolean recursion, String... values) {
		if (null == values) {
			return null;
		}
		for (Object item : values) {
			if ("".equals(item) || isNotEmpty(recursion, item)) {
				return item;
			}
		}
		return null;
	}

	public static Object nvl(Object... values) {
		return nvl(false, values);
	}
	public static Object nvl(String... values) {
		return nvl(false, values);
	}

	/**
	 * 反回第一个不为空(""|null|empty)的值 没有符合条件的 则返回NULL
	 * 与nvl区别 : ""不符合evl条件 但符合nvl条件 
	 * @param recursion
	 * @param values
	 * @return
	 */
	public static Object evl(boolean recursion, Object... values) {
		if (null == values) {
			return null;
		}
		for (Object item : values) {
			if (isNotEmpty(recursion, item)) {
				return item;
			}
		}
		return null;
	}

	public static Object evl(Object... values) {
		return evl(false, values);
	}

	/**
	 * 随机数
	 * 
	 * @param fr
	 * @param to
	 * @return
	 */
	public static int getRandomNumber(int fr, int to) {
		return NumberUtil.getRandom(fr, to);
	}
	public static double getRandomNumber(double fr, double to) {
		return NumberUtil.getRandom(fr, to);
	}

	/**
	 * 生成随机字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length, StringBuffer buffer) {
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int range = buffer.length();
		for (int i = 0; i < length; i++) {
			sb.append(buffer.charAt(r.nextInt(range)));
		}
		return sb.toString();
	}

	public static String getRandomString(int length) {
		return getRandomString(length,new StringBuffer("012356789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}

	public static String getRandomLowerString(int length) {
		return getRandomString(length, new StringBuffer("abcdefghijklmnopqrstuvwxyz"));
	}

	public static String getRandomUpperString(int length) {
		return getRandomString(length, new StringBuffer("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}

	public static String getRandomNumberString(int length) {
		return getRandomString(length, new StringBuffer("123567890"));
	}
	/**
	 * 随机中文字符(GBK)
	 * @param length
	 * @return
	 */
	public static String getRandomCnString(int length){
		String result = "";
		for (int i = 0; i < length; i++) {
			String str = null;
			int hPos, lPos; // 定义高低位
			Random random = new Random();
			hPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
			lPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
			byte[] b = new byte[2];
			b[0] = (new Integer(hPos).byteValue());
			b[1] = (new Integer(lPos).byteValue());
			try {
				str = new String(b, "GBk"); // 转成中文
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			result += str;
		}
     return result;
	}
	/**
	 * 在src的第idx位置插入key
	 * @param src
	 * @param idx
	 * @param key
	 * @return
	 */
	public static String insert(String src, int idx, String key) {
		if (null == src || null == key) {
			return src;
		}
		src = src.substring(0, idx) + key + src.substring(idx);
		return src;

	}

	/**
	 * 判断数字
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNumber(Object obj) {
		boolean result = false;
		if (obj == null) {
			return result;
		}
		if (obj instanceof Number)
			return true;
		String str = obj.toString();
		try {
			Double.parseDouble(str);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
	public static boolean isDate(Object obj){
		if(null == obj){
			return false;
		}
		if(obj instanceof Date){
			return true;
		}
		return RegularUtil.isDate(obj.toString());
	}
	public static boolean isDateTime(Object obj){
		if(null == obj){
			return false;
		}
		if(obj instanceof Date){
			return true;
		}
		return RegularUtil.isDateTime(obj.toString());
	}
	public static int parseInt(Object value, int def) {
		if (null == value) {
			return def;
		}
		try {
			return (int) Double.parseDouble(value.toString());
		} catch (Exception e) {
			return def;
		}
	}

	public static Integer parseInteger(Object value, Integer def) {
		if (null == value) {
			return def;
		}
		try {
			return (int) Double.parseDouble(value.toString());
		} catch (Exception e) {
			return def;
		}
	}

	public static Double parseDouble(Object value, Double def) {
		if (null == value) {
			return def;
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (Exception e) {
			return def;
		}
	}

	public static BigDecimal parseDecimal(Object value, double def) {
		return parseDecimal(value,new BigDecimal(def));
	}

	public static BigDecimal parseDecimal(Object value, BigDecimal def) {
		if (null == value) {
			return def;
		}
		try {
			return new BigDecimal(value.toString());
		} catch (Exception e) {
			return def;
		}
	}
	public static Long parseLong(Object value, Long def) {
		if (null == value) {
			return def;
		}
		try {
			return Long.parseLong(value.toString());
		} catch (Exception e) {
			return def;
		}
	}

	/**
	 * 类型转换
	 * 
	 * @param obj
	 * @param def
	 * @return
	 */
	public static Boolean parseBoolean(Object obj, Boolean def) {
		boolean result = def;
		if (isEmpty(obj))
			return result;
		try {
			if ("1".equals(obj.toString())
					|| "true".equalsIgnoreCase(obj.toString())
					|| "on".equalsIgnoreCase(obj.toString())
					|| "t".equalsIgnoreCase(obj.toString())) {
				result = true;
			} else if ("0".equals(obj.toString())
					|| "false".equalsIgnoreCase(obj.toString())
					|| "off".equalsIgnoreCase(obj.toString())
					|| "f".equalsIgnoreCase(obj.toString())) {
				result = false;
			} else {
				result = Boolean.parseBoolean(obj.toString());
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean parseBoolean(Object obj) {
		return parseBoolean(obj, false);
	}

	/**
	 * 拆分权限数 ： 将任意一个数拆分成多个（2的n次方）的和
	 * 
	 * @param num
	 * @return
	 */
	public static List<String> parseLimit(int num) {
		List<String> list = new ArrayList<String>();
		int count = 0;
		while (num >= 1) {
			int temp = num % 2;
			num = (num - temp) / 2;
			if (temp == 1) {
				if (count == 0){
					list.add("1");
				}else{
					list.add((2 << (count - 1)) + "");
				}
			}
			count++;
		}
		return list;
	}

	/**
	 * 字符串替换
	 * 
	 * @param src
	 * @param pattern
	 * @param replace
	 * @return
	 */
	public static String replace(String src, String pattern, String replace) {
		if (src == null)
			return null;
		int s = 0;
		int e = 0;
		StringBuilder result = new StringBuilder();
		while ((e = src.indexOf(pattern, s)) >= 0) {
			result.append(src.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}

		result.append(src.substring(s));
		return result.toString();
	}

	/**
	 * 删除空格
	 * 
	 * @param str
	 * @return
	 */
	public static String trim(Object str) {
		String result = "";
		if (str != null) {
			if (!isNumber(str))
				result = str.toString().trim();
			else
				result = "" + str;
		} else {
			result = "";
		}
		if (result.equals("-1"))
			result = "";
		return result;
	}

	/**
	 * 删除空格
	 * 
	 * @param str
	 * @return
	 */
	public static String trim(String str) {
		String result = "";
		if (str != null) {
			if (!isNumber(str))
				result = str.toString().trim();
			else
				result = "" + str;
		} else {
			result = "";
		}
		if (result.equals("-1"))
			result = "";
		return result;
	}

	/**
	 * 压缩空白 将多个空白压缩成一个空格
	 * 
	 * @param str
	 * @return
	 */
	public static String compressionSpace(String str) {
		if (null != str) {
			str = str.replaceAll("\\s{2,}", " ").trim();
		}
		return str;
	}

	public static String[] compressionSpace(String[] strs) {
		if (null != strs) {
			int size = strs.length;
			for (int i = 0; i < size; i++) {
				strs[i] = compressionSpace(strs[i]);
			}
		}
		return strs;
	}

	/**
	 * 填充字符(从左侧填充)
	 * 
	 * @param src
	 *            原文
	 * @param chr
	 *            填充字符
	 * @param len
	 *            需要达到的长度
	 * @return
	 */
	public static String fillLChar(String src, String chr, int len) {
		if (null != src && null != chr && chr.length() > 0) {
			while (src.length() < len) {
				src = chr + src;
			}
		}
		return src;
	}

	public static String fillRChar(String src, String chr, int len) {
		if (null != src && null != chr && chr.length() > 0) {
			while (src.length() < len) {
				src = src + chr;
			}
		}
		return src;
	}

	public static String fillChar(String src, String chr, int len) {
		return fillLChar(src, chr, len);
	}

	public static String fillChar(String src, int len) {
		return fillChar(src, "0", len);
	}

	/**
	 * 提取HashMap的key
	 * 
	 * @param map
	 * @return
	 */
	public static List<String> getMapKeys(Map<?, ?> map) {
		List<String> keys = new ArrayList<String>();
		Iterator<?> it = map.keySet().iterator();
		while (it.hasNext()) {
			keys.add(it.next().toString());
		}
		return keys;
	}

	/**
	 * 合成笛卡尔组合
	 * 
	 * @param src
	 * @return
	 */
	public static List<List<String>> mergeDescartes(List<List<String>> src) {
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> st = src.get(0);
		for (String t : st) {
			List<String> tmp = new ArrayList<String>();
			tmp.add(t);
			result.add(tmp);
		}
		List<List<String>> store = new ArrayList<List<String>>();
		for (int i = 1; i < src.size(); i++) {
			List<String> r2 = src.get(i);
			for (int j = 0; j < result.size(); j++) {
				List<String> rns = result.get(j);
				for (int k = 0; k < r2.size(); k++) {
					List<String> mid = new ArrayList<String>();
					mid.addAll(rns);
					mid.add(r2.get(k));
					store.add(mid);
				}
			}
			result = new ArrayList<List<String>>();
			result.addAll(store);
			store = new ArrayList<List<String>>();
		}
		return result;
	}

	public static List<String> split(String str, String separator){
		List<String> list = new ArrayList<String>();
		if(null !=str && null != separator){
			String tmps[] = str.split(separator);
			for(String tmp : tmps){
				tmp = tmp.trim();
				if(BasicUtil.isNotEmpty(tmp)){
					list.add(tmp);
				}
			}
		}
		return list;
	}
	/**
	 * 合并数组
	 * 
	 * @param obj0
	 * @param obj1
	 * @return
	 */
	public static Object[] merge(Object[] obj0, Object[] obj1) {
		if (null == obj0) {
			if (null == obj1) {
				return null;
			} else {
				return obj1;
			}
		} else {
			if (null == obj1) {
				return obj0;
			} else {
				Object[] obj = new Object[obj0.length + obj1.length];
				int idx = 0;
				for (int i = 0; i < obj0.length; i++) {
					obj[idx++] = obj0[i];
				}
				for (int i = 0; i < obj1.length; i++) {
					obj[idx++] = obj1[i];
				}
				return obj;
			}
		}
	}

	/**
	 * 数组转换成字符串
	 * 
	 * @param list
	 *            数组
	 * @param split
	 *            分隔符
	 * @return
	 */
	public static String array2string(List<?> list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				builder.append(list.get(i));
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}

	public static String array2string(Object[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}

	/**
	 * 子串出现次数
	 * 
	 * @param src
	 * @param chr
	 * @return
	 */
	public static int catSubCharCount(String src, String chr) {
		int count = 0;
		int idx = -1;
		if (null == src || null == chr || "".equals(chr.trim())) {
			return 0;
		}
		while ((idx = src.indexOf(chr, idx + chr.length())) != -1) {
			count++;
		}
		return count;
	}

	public static Object fetch(Collection<?> items, String key, Object value) {
		if (null == items) {
			return null;
		}
		for (Object item : items) {
			Object tmpValue = BeanUtil.getFieldValue(item, key);
			if (null != tmpValue && tmpValue.equals(value)) {
				return item;
			}
		}
		return null;
	}

	public static String cut(String src, int fr, int to) {
		if (null == src) {
			return null;
		}
		int len = src.length();
		if (to > len) {
			to = len;
		}
		return src.substring(fr, to);
	}
	public static String left(String src, int len){
		if(null == src){
			return null;
		}
		int max = src.length();
		if(len > max){
			len = max;
		}
		return src.substring(0, len);
	}
	public static String right(String src, int len){
		if(null == src){
			return null;
		}
		int max = src.length();
		if(len > max){
			len = max;
		}
		return src.substring(max-len, max);
	}
	/**
	 * 拼接集合
	 * @param objs
	 * @return
	 */
	public static String join(Collection<?> objs){
		if(null == objs){
			return "";
		}
		String result = "";
		int idx = 0;
		for(Object obj:objs){
			result += "[" + idx++ +"]" + obj;
		}
		return result;
	}
	public static String collection2string(Object[] objs){
		if(null == objs){
			return "";
		}
		String result = "";
		int idx = 0;
		for(Object obj:objs){
			result += "[" + idx++ +"]" + obj;
		}
		return result;
	}
	/**
	 * 按key升序拼接
	 * @param params
	 * @return
	 */
	public static String joinBySort(Map<String,?> params){
		String result = "";
		SortedMap<String, Object> sort = new TreeMap<String, Object>(params);
		Set es = sort.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if ("".equals(v)) {
				//params.remove(k);
				continue;
			}
			if (!"".equals(result)) {
				result += "&";
			}
			result += k + "=" + v;
		}
		return result;
	}
	/**
	 * 获取本机IP
	 * @return
	 */
	public static List<InetAddress> getLocalIps(){
		List<InetAddress> ips = new ArrayList<InetAddress>();
		try{
			Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements()){
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				Enumeration addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()){
					ip = (InetAddress) addresses.nextElement();
					if(ip != null && ip instanceof Inet4Address){
						ips.add(ip);
					} 
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return ips;
	}
	/**
	 * 获取本机IP地址
	 * @return
	 */
	public static List<String> getLocalIpsAddress(){
		List<String> ips = new ArrayList<String>();
		List<InetAddress> list = getLocalIps();
		for(InetAddress ip:list){
			ips.add(ip.getHostAddress());
		}
		return ips;
	}
	public static Object toUpperCaseKey(Object obj, String ... keys){
		if(null == obj){
			return null;
		}
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Date) {
			return obj;
		}
		if(obj instanceof Map){
			obj = toUpperCaseKey((Map<String,Object>)obj, keys);
		}else if(obj instanceof Collection){
			obj = toUpperCaseKey((Collection)obj, keys);
		}
		return obj;
	}
	public static Collection toUpperCaseKey(Collection con, String ... keys){
		if(null == con){
			return con;
		}
		for(Object obj :con){
			obj = toUpperCaseKey(obj, keys);
		}
		return con;
	}
	public static Map<String,Object> toUpperCaseKey(Map<String,Object> map, String ... keys){
		if(null == map){
			return map;
		}
		List<String> ks = getMapKeys(map);
		for(String k:ks){
			if(null == keys || keys.length == 0 || containsString(keys, k)){
				Object v = map.get(k);
				String key = k.toUpperCase();
				map.remove(k);
				map.put(key, v);
			}
		}
		return map;
	}
	/**
	 * 数组是否包含
	 * @param objs
	 * @param obj
	 * @return
	 */
	public static boolean containsString(Object[] objs, Object obj){
		if(null == objs && null == obj){
			return true;
		}
		if(null == objs || null == obj){
			return false;
		}
		for(Object o : objs){
			if(obj.toString().equals(o.toString())){
				return true;
			}
		}
		return false;
	}
	public static boolean contains(Object[] objs, Object obj){
		if(null == objs && null == obj){
			return true;
		}
		if(null == objs || null == obj){
			return false;
		}
		for(Object o : objs){
			if(obj.equals(o)){
				return true;
			}
		}
		return false;
	}
	public static boolean contains(Collection<Object> objs, Object obj){
		if(null == objs && null == obj){
			return true;
		}
		if(null == objs || null == obj){
			return false;
		}
		for(Object o : objs){
			if(obj.equals(o)){
				return true;
			}
		}
		return false;
	}
	public static boolean containsString(Collection<Object> objs, Object obj){
		if(null == objs && null == obj){
			return true;
		}
		if(null == objs || null == obj){
			return false;
		}
		for(Object o : objs){
			if(obj.toString().equals(o.toString())){
				return true;
			}
		}
		return false;
	}
	public static boolean containsIgnoreCase(Object[] objs, Object obj){
		if(null == objs || null == obj){
			return false;
		}
		for(Object o : objs){
			if(obj.toString().equalsIgnoreCase(o.toString())){
				return true;
			}
		}
		return false;
	}
	public static boolean containsIgnoreCase(Collection<Object> objs, Object obj){
		if(null == objs || null == obj){
			return false;
		}
		for(Object o : objs){
			if(obj.toString().equalsIgnoreCase(o.toString())){
				return true;
			}
		}
		return false;
	}
   public static String concat(List<String> list, String connector){
	   if(null == list){
		   return "";
	   }
	   StringBuffer result = new StringBuffer();
	   for(String val:list){
		   if(result.length() > 0){
			   result.append(connector);
		   }
		   result.append(val);
	   }
	   return result.toString();
   }
   public static boolean isWrapClass(Object obj) { 
	    try { 
	    	return ((Class) obj.getClass().getField("TYPE").get(null)).isPrimitive();
	    } catch (Exception e) { 
	        return false; 
	    } 
   }
   public static boolean isJson(Object json){
	   if(null == json){
		   return false;
	   }
	   if(json instanceof JSONObject){
		   return true;
	   }
	   try{
		   JSONObject.fromObject(json.toString());
	   }catch(Exception e){
		   return false;
	   }
	   return true;
   }
   public static String omit(String src, int left, int right){
	   return omit(src, left, right, "*");
   }
   public static String omit(String src, int left, int right, String ellipsis){
	   String result = "";
	   if(BasicUtil.isEmpty(src)){
		   return result;
	   }
	   int length = src.length();
		if(left > length){
			left = length;
		}
		if(right > length - left){
			right = length - left;
		}
		String l = src.substring(0,left);
		String r = src.substring(length - right);
		result = l+BasicUtil.fillRChar("", ellipsis, length-left-right)+r;
		return result;
   }
   public static String escape(String src) {  
       return CodeUtil.escape(src);
   }  

   public static String unescape(String src) {  
       return CodeUtil.unescape(src);  
   } 

}