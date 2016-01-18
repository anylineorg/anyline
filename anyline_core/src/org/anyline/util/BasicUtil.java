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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;

public class BasicUtil {
	private static Logger LOG = Logger.getLogger(BasicUtil.class);

	/**
	 * 是否为空或""或"null"(大写字母"NULL"不算空) 集合对象检查是否为空或集合中是否有对象
	 * 
	 * @param obj
	 * @param recursion
	 *            是否递归查检集合对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
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

	public static boolean isNotEmpty(boolean recursion, Object obj) {
		return !isEmpty(recursion, obj);
	}

	public static boolean isNotEmpty(Object obj) {
		return isNotEmpty(false, obj);
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
	 * nvl 取第一个不为null的值,如果全为空则返回null
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

	public static Object nvl(Object... values) {
		return nvl(false, values);
	}

	/**
	 * 反回第一个不为空(""|null|empty)的值
	 * 
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
		int result = 0;
		Random r = new Random();
		result = fr + r.nextInt(to - fr);
		return result;
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
		return getRandomString(
				length,
				new StringBuffer(
						"_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}

	public static String getRandomLowerString(int length) {
		return getRandomString(length, new StringBuffer(
				"abcdefghijklmnopqrstuvwxyz"));
	}

	public static String getRandomUpperString(int length) {
		return getRandomString(length, new StringBuffer(
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}

	public static String getRandomNumberString(int length) {
		return getRandomString(length, new StringBuffer("1234567890"));
	}

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
		if (isEmpty(obj))
			return def;
		try {
			if ("1".equals(obj.toString())
					|| "true".equalsIgnoreCase(obj.toString())
					|| "on".equalsIgnoreCase(obj.toString())
					|| "t".equalsIgnoreCase(obj.toString())) {
				def = true;
			} else if ("0".equals(obj.toString())
					|| "false".equalsIgnoreCase(obj.toString())
					|| "f".equalsIgnoreCase(obj.toString())) {
				def = false;
			} else {
				def = Boolean.parseBoolean(obj.toString());
			}
		} catch (Exception e) {
		}
		return def;
	}

	public static boolean parseBoolean(Object obj) {
		return parseBoolean(obj, false);
	}

	/**
	 * 选取最大数
	 * 
	 * @param num
	 * @return
	 */
	public static double getMax(double num, double... nums) {
		double max = num;
		if (null != nums) {
			int size = nums.length;
			for (int i = 0; i < size; i++) {
				if (max < nums[i]) {
					max = nums[i];
				}
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 * 
	 * @param num
	 * @return
	 */
	public static double getMin(double num, double... nums) {
		double min = num;
		if (null != nums) {
			int size = nums.length;
			for (int i = 0; i < size; i++) {
				if (min > nums[i]) {
					min = nums[i];
				}
			}
		}
		return min;
	}

	/**
	 * 过滤JSON转义符 " \
	 * 
	 * @param value
	 * @return
	 */
	public static String convertJSONChar(String value) {
		if (null != value) {
			value = value.replace("\\", "\\\\").replace("\"", "\\\"");
		}
		return value;
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
				if (count == 0)
					list.add("1");
				else
					list.add((2 << (count - 1)) + "");
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
			str = str.replace("\\s{2,}", " ");
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

	/**
	 * 检查并转换JSON格式文本
	 * 
	 * @param str
	 * @return
	 */
	public static String validateJSONString(String str) {
		if (null == str) {
			str = "";
		} else {
			str = str.replace("\"", "\\\"");
			str = str.replace("'", "\\'");
		}
		return str;
	}

	/**
	 * 检查并转换XML格式文本
	 * 
	 * @param str
	 * @return
	 */
	public static String validateXMLString(String str) {
		if (null == str) {
			str = "";
		} else {
			str = str.replace(">", "&gt;");
			str = str.replace("<", "&lt;");
		}
		return str;
	}

	/**
	 * 测试函数
	 * 
	 * @param map
	 */
	public static void print(Object obj) {
		System.out.print(obj);
		if (null == obj) {
			return;
		}
		if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			for (Iterator<?> itr = map.keySet().iterator(); itr.hasNext();) {
				Object key = itr.next();
				Object value = map.get(key);
				System.out.println(key + "=" + value);
			}
		}
		if (obj instanceof ServletRequest) {
			ServletRequest request = (ServletRequest) obj;
			Enumeration<?> keys = request.getParameterNames();
			for (; keys.hasMoreElements();) {
				String key = keys.nextElement().toString();
				String value = request.getParameter(key);
				System.out.println(key + "=" + value);
			}
		}
	}

	/**
	 * 数据格式化
	 * 
	 * @param src
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(String src, String pattern) {
		if (null == src) {
			return "";
		}
		double srcDouble = Double.parseDouble(src);
		return formatNumber(srcDouble, pattern);
	}

	/**
	 * 数字格式化
	 * 
	 * @param src
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(Number src, String pattern) {
		if (null == src) {
			return "";
		}
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(src);
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
	public static String array2String(List<?> list, String split) {
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

	public static String array2String(Object[] list, String split) {
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
	/**
	 * 金额大小
	 * @param n
	 * @return
	 */
	public static String moneyUpper(double n) {
		String fraction[] = { "角", "分" };
		String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
		String unit[][] = { { "元", "万", "亿" }, { "", "拾", "佰", "仟" } };

		String head = n < 0 ? "负" : "";
		n = Math.abs(n);

		String s = "";
		for (int i = 0; i < fraction.length; i++) {
			s += (digit[(int) (Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i])
					.replaceAll("(零.)+", "");
		}
		if (s.length() < 1) {
			s = "整";
		}
		int integerPart = (int) Math.floor(n);

		for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
			String p = "";
			for (int j = 0; j < unit[1].length && n > 0; j++) {
				p = digit[integerPart % 10] + unit[1][j] + p;
				integerPart = integerPart / 10;
			}
			s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i]
					+ s;
		}
		return head
				+ s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "")
						.replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
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
			LOG.error(e);
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
			if(null == keys || keys.length == 0 || contains(keys, k)){
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
	public static boolean contains(Object[] objs, Object obj){
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
}