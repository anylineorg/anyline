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


package org.anyline.util; 
 
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.anyline.util.regular.RegularUtil;

import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
 
public class BasicUtil { 

	/** 
	 * 是否为空或""或"null"(大写字母"NULL"不算空) 集合对象检查是否为空或集合中是否有对象 
	 *  
	 * @param obj  obj
	 * @param recursion  recursion
	 *            是否递归查检集合对象 
	 * @return return
	 */ 
	@SuppressWarnings("rawtypes")
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
		} else if (obj instanceof Map) {
			Map map = (Map) obj;
			if(null == map || map.isEmpty()){
				return true;
			} 
			if(recursion){ 
				for (Iterator itrKey = map.keySet().iterator(); itrKey.hasNext();) { 
					if (!isEmpty(map.get(itrKey.next()))) { 
						return false; 
					} 
				}
			}else{
				return false;
			} 
		} else { 
			String tmp = obj.toString().trim(); 
			if (!tmp.equals("") && !tmp.equals("null")) { 
				return false; 
			} 
		} 
		return true; 
	}

	/**
	 * 是否全部为空
	 * @param objs objs
	 * @return boolean
	 */
	public static boolean isEmpty(Object ... objs) {
		if(null == objs){
			return true;
		}
		for(Object obj:objs){
			if(!isEmpty(false, obj)){
				return false;
			}
		}
		return true;
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
	 * @param recursion  对于集合变量,是否递归 
	 * @param values  values
	 * @return return
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
	 * @param recursion  recursion
	 * @param values  values
	 * @return return
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
	 * @param fr  fr
	 * @param to  to
	 * @return return
	 */
	public static int getRandomNumber(int fr, int to) {
		return NumberUtil.random(fr, to);
	}
	public static double getRandomNumber(double fr, double to) {
		return NumberUtil.random(fr, to);
	} 
 
	/** 
	 * 生成随机字符串 
	 *  
	 * @param length  length
	 * @param buffer  buffer
	 * @return return
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
	 * @param length length
	 * @return return
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
	 * @param src src
	 * @param idx idx
	 * @param key key
	 * @return return
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
	 * @param obj  obj
	 * @return return
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

	public static Float parseFloat(Object value, Float def) {
		if (null == value) {
			return def;
		}
		try {
			return Float.parseFloat(value.toString());
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
	 * @param obj  obj
	 * @param def  def
	 * @return return
	 */ 
	public static Boolean parseBoolean(Object obj, Boolean def) {
		try { 
			return parseBoolean(obj);
		} catch (Exception e) {
			return def;
		}
	} 
 
	public static boolean parseBoolean(Object obj) throws Exception{
		if ("1".equals(obj.toString())
				|| "true".equalsIgnoreCase(obj.toString())
				|| "on".equalsIgnoreCase(obj.toString())
				|| "t".equalsIgnoreCase(obj.toString())) {
			return true;
		} else if ("0".equals(obj.toString())
				|| "false".equalsIgnoreCase(obj.toString())
				|| "off".equalsIgnoreCase(obj.toString())
				|| "f".equalsIgnoreCase(obj.toString())) {
			return  false;
		} else {
			return Boolean.parseBoolean(obj.toString());
		}
	} 
 
	/** 
	 * 拆分权限数 ： 将任意一个数拆分成多个（2的n次方）的和 
	 *  
	 * @param num  num
	 * @return return
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
	 * @param src  src
	 * @param pattern  pattern
	 * @param replace  replace
	 * @return return
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
	 * @param str  str
	 * @return return
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
	 * @param str  str
	 * @return return
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
	 * @param str  str
	 * @return return
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
	 * @param src   原文 
	 * @param chr  填充字符 
	 * @param len  需要达到的长度 
	 * @return return
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
	 * @param map  map
	 * @return return
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
	 * @param lists  lists
	 * @param <T> t
	 * @return return
	 */
	public static <T> List<List<T>> descartes(List<List<T>> lists) {
		List<List<T>> result = new ArrayList<List<T>>();
		if(null == lists || lists.size()==0){
			return result;
		}
		List<T> st = lists.get(0);
		for (T t : st) {
			List<T> tmp = new ArrayList<T>();
			tmp.add(t); 
			result.add(tmp); 
		} 
		List<List<T>> store = new ArrayList<List<T>>();
		for (int i = 1; i < lists.size(); i++) {
			List<T> r2 = lists.get(i);
			for (int j = 0; j < result.size(); j++) { 
				List<T> rns = result.get(j);
				for (int k = 0; k < r2.size(); k++) { 
					List<T> mid = new ArrayList<T>();
					mid.addAll(rns); 
					mid.add(r2.get(k)); 
					store.add(mid); 
				} 
			} 
			result = new ArrayList<List<T>>();
			result.addAll(store); 
			store = new ArrayList<List<T>>();
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
	 * @param obj0  obj0
	 * @param obj1  obj1
	 * @return return
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
	 * 子串出现次数 
	 *  
	 * @param src  src
	 * @param chr  chr
	 * @return return
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
//	/**
//	 * 拼接集合
//	 * @param objs objs
//	 * @return return
//	 */
//	public static String join(Collection<?> objs){
//		if(null == objs){
//			return "";
//		}
//		String result = "";
//		int idx = 0;
//		for(Object obj:objs){
//			result += "[" + idx++ +"]" + obj;
//		}
//		return result;
//	}
	/** 
	 * 获取本机IP 
	 * @return return
	 */ 
	@SuppressWarnings("rawtypes")
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
	 * @return return
	 */ 
	public static List<String> getLocalIpsAddress(){ 
		List<String> ips = new ArrayList<String>(); 
		List<InetAddress> list = getLocalIps(); 
		for(InetAddress ip:list){ 
			ips.add(ip.getHostAddress()); 
		} 
		return ips; 
	} 
	/** 
	 * 数组是否包含 
	 * @param objs  objs
	 * @param obj  obj
	 * @return return
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
	/**
	 * 拼接字符
	 * @param list list
	 * @param connector connector
	 * @return return
	 */
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


	/**
	 * v1与v2是否相等
	 * @param v1 v1
	 * @param v2 v2
	 * @param propertys1 属性列表1
	 * @param propertys2 属性列表2
	 * @return boolean
	 */
	public static boolean equals(Object v1, Object v2, List<String> propertys1, List<String> propertys2){
		boolean result = false;
		if(null == v1 && null == v2){
			return true;
		}else if(null == v1 || null == v2){
			return false;
		}
		if(v1 instanceof String || v1 instanceof Number || v1 instanceof Boolean || v1 instanceof Date) {
			//v1基础类型
			if(v2 instanceof String || v2 instanceof Number || v2 instanceof Boolean || v2 instanceof Date){
				//v2基础类型
				result = v2.toString().equals(v1.toString());
			}else{
				//v2非基础类型
				if(null != propertys2 && propertys2.size()>0){
					v2 = BeanUtil.getFieldValue(v2, propertys2.get(0))+"";
				}
				result = v2.toString().equals(v1.toString());
			}
		}else{
			//v1非基础类型
			if(v2 instanceof String || v2 instanceof Number || v2 instanceof Boolean || v2 instanceof Date){
				//v2基础类型
				if(null != propertys1 && propertys1.size()>0){
					v1 = BeanUtil.getFieldValue(v1, propertys1.get(0))+"";
				}
				result = v2.toString().equals(v1.toString());
			}else{
				//v2非基础类型
				boolean eq = true;
				int psize = 0;
				if(null == propertys1 || null == propertys2){
					eq = false;
				}else{
					//取长度较短的一个长度
					psize = NumberUtil.min(propertys1.size(), propertys2.size());
				}
				if(psize > 0){
					for(int i=0; i<psize; i++){
						String p1 = propertys1.get(i);
						String p2 = propertys2.get(i);
						String vv1 = BeanUtil.getFieldValue(v1, p1)+"";
						String vv2 = BeanUtil.getFieldValue(v2, p2)+"";
						if(!vv1.equals(vv2)){
							eq = false;
							break;
						}
					}
				}else{
					//没有设置property
					eq = v1.equals(v2);
				}
				if(eq){
					result = true;
				}
			}
		}
		return result;
	}
	/**
	 * v1与v2是否相等
	 * @param v1 v1
	 * @param v2 v2
	 * @param propertys 属性(ID:CD,NM:NAME)(ID,NM)
	 * @return boolean
	 */
	public static boolean equals(Object v1, Object v2, String propertys){
		boolean result = false;
		List<String> propertys1 = new ArrayList<String>();
		List<String> propertys2 = new ArrayList<String>();
		if(BasicUtil.isNotEmpty(propertys)){
			String[] ps = propertys.split(",");
			for(String p:ps){
				if(BasicUtil.isNotEmpty(p)){
					String p1 = p;
					String p2 = p;
					if(p.contains(":")){
						String[] tmps = p.split(":");
						p1 = tmps[0];
						p2 = tmps[1];
					}
					propertys1.add(p1);
					propertys2.add(p2);
				}
			}
		}
		return equals(v1, v2, propertys1, propertys2);
	}
}
