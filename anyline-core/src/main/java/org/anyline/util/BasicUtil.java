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

package org.anyline.util;

import org.anyline.util.regular.RegularUtil;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class BasicUtil {

	public static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789, .?'_-=+!@#$%^&*() ";
	/**
	 * 是否为空或""或"null"(大写字母"NULL"不算空) 集合对象检查是否为空或集合中是否有对象
	 *
	 * @param obj  obj
	 * @param recursion  recursion
	 *            是否递归查检集合对象
	 * @return boolean
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(boolean recursion, Object obj) {
		if (null == obj) {
			return true;
		}
		if (obj instanceof Collection && recursion) {
			Collection collection = (Collection) obj;
			if(collection.isEmpty()) {
				return true;
			}
			for (Object item : collection) {
				if (!isEmpty(recursion, item)) {
					return false;
				}
			}
		}else if(obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			return len == 0;
		} else if (obj instanceof Map) {
			Map map = (Map) obj;
			if(map.isEmpty()) {
				return true;
			}
			if(recursion) {
				for(Object item:map.values()) {
					if (!isEmpty(recursion, item)) {
						return false;
					}
				}
			}else{
				return false;
			}
		} else {
			String tmp = obj.toString();
			if(null == tmp) {
				return true;
			}
			tmp = tmp.trim();
			if (!tmp.equals("") && !tmp.equals("null")) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(Object obj) {
		return isEmpty(false, obj);
	}

	/**
	 * 是否全部为空
	 * @param objs objs
	 * @return boolean
	 */
	public static boolean isEmpty(Object ... objs) {
		if(null == objs) {
			return true;
		}
		for(Object obj:objs) {
			if(!isEmpty(false, obj)) {
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
	public static boolean equals(byte[] bytes1, byte[] bytes2) {
		if(null == bytes1) {
			if(null == bytes2) {
				return true;
			}else{
				return false;
			}
		}
		if(null == bytes2) {
			return false;
		}
		int l1 = bytes1.length;
		int l2 = bytes2.length;
		if(l1 != l2) {
			return false;
		}
		for (int i=0; i<l1; i++) {
			if(bytes1[i] != bytes2[i]) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Object obj1, Object obj2) {
		return equals(obj1, obj2, false);
	}
	public static boolean equalsIgnoreCase(Object obj1, Object obj2) {
		return equals(obj1, obj2, true);
	}

	/**
	 * 对象toString相等
	 * @param obj1 object
	 * @param obj2 object
	 * @param ignoreCase 是否忽略大小写
	 * @return boolean
	 */
	public static boolean equals(Object obj1, Object obj2, boolean ignoreCase) {
		if (null == obj1) {
			if (null == obj2) {
				return true;
			} else {
				return false;
			}
		} else {
			if(null == obj2) {
				return false;
			}else {
				if(ignoreCase) {
					return obj1.toString().equalsIgnoreCase(obj2.toString());
				}else {
					return obj1.toString().equals(obj2.toString());
				}
			}
		}
	}

	/**
	 * nvl 取第一个不为null的值, 没有符合条件的 则返回null
	 * @param values values
	 * @return T
	 * @param <T> T
	 */
	public static <T> T nvl(T... values) {
		if (null == values) {
			return null;
		}
		for (T item : values) {
			if (null != item) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 反回第一个不为空(""|null|empty)的值 没有符合条件的 则返回NULL
	 * 与nvl区别 : ""不符合evl条件 但符合nvl条件
	 * @param recursion  recursion
	 * @param values  values
	 * @param <T> T
	 * @return Object
	 */
	public static <T> T evl(boolean recursion, T... values) {
		if (null == values) {
			return null;
		}
		for (T item : values) {
			if (isNotEmpty(recursion, item)) {
				return item;
			}
		}
		return null;
	}

	public static <T> T evl(T... values) {
		return evl(false, values);
	}

	/**
	 * 随机数
	 *
	 * @param fr  fr
	 * @param to  to
	 * @return int
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
	 * @return String
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
		return getRandomString(length, new StringBuffer("012356789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
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
	 * @return String
	 */
	public static String getRandomCnString(int length) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			String str = null;
			int hPos, lPos; // 定义高低位
			Random random = new Random();
			hPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
			lPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
			byte[] b = new byte[2];
			b[0] = (Integer.valueOf(hPos).byteValue());
			b[1] = (Integer.valueOf(lPos).byteValue());
			try {
				str = new String(b, "GBk"); // 转成中文
			} catch (Exception ignored) {
			}
			result.append(str);
		}
     return result.toString();
	}
	/**
	 * 在src的第idx位置插入key
	 * @param src src
	 * @param idx idx
	 * @param key key
	 * @return String
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
	 * @return boolean
	 */
	public static boolean isNumber(Object obj) {
		boolean result = false;
		if (obj == null) {
			return result;
		}
		if (obj instanceof Number) {
			return true;
		}
		String str = obj.toString();
		if(str.matches("-?\\d+(\\.\\d+)?")){
			return true;
		}
		try {
			Double.parseDouble(str);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
	public static boolean isBoolean(Object obj) {
		boolean result = false;
		if(null == obj) {
			return result;
		}
		if(obj instanceof Boolean) {
			return true;
		}
		if(obj.toString().equalsIgnoreCase("true") || obj.toString().equalsIgnoreCase("false")) {
			return true;
		}
		return result;
	}
	public static boolean isDate(Object obj) {
		if(null == obj) {
			return false;
		}
		if(obj instanceof Date) {
			return true;
		}
		return RegularUtil.isDate(obj.toString());
	}
	public static boolean isDateTime(Object obj) {
		if(null == obj) {
			return false;
		}
		if(obj instanceof Date) {
			return true;
		}
		return RegularUtil.isDateTime(obj.toString());
	}

	public static Byte parseByte(Object value, Byte def) {
		try {
			return Byte.parseByte(value.toString());
		}catch (Exception e) {
			return def;
		}
	}
	public static Byte parseByte(Object value) throws NumberFormatException {
		return Byte.parseByte(value.toString());
	}
	public static Short parseShort(Object value, Short def) {
		if (null == value) {
			return def;
		}
		if(value instanceof Short) {
			return (Short) value;
		}
		try {
			return (short) Double.parseDouble(value.toString());
		} catch (Exception e) {
			return def;
		}
	}
	public static Short parseShort(Object value) throws NumberFormatException {
		if (null == value) {
			//return null;
			//应该抛出异常
		}
		return (short) Double.parseDouble(value.toString());
	}

	public static Integer parseInt(Object value, Integer def) {
		if (null == value) {
			return def;
		}
		if(value instanceof Integer) {
			return (Integer) value;
		}
		try {
			return (int) Double.parseDouble(value.toString().trim());
		} catch (Exception e) {
			return def;
		}
	}
	public static Integer parseInt(Object value) throws NumberFormatException {
		if (null == value) {
			//return null;
			//应该抛出异常
		}
		return (int) Double.parseDouble(value.toString());
	}

	public static Float parseFloat(Object value, Float def) {
		if (null == value) {
			return def;
		}
		if(value instanceof Float) {
			return (Float) value;
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
		if(value instanceof Double) {
			return (Double) value;
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (Exception e) {
			return def;
		}
	}

	public static BigDecimal parseDecimal(Object value, double def) {
		return parseDecimal(value, new BigDecimal(def));
	}

	public static BigDecimal parseDecimal(Object value, BigDecimal def) {
		if (null == value) {
			return def;
		}
		if(value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		BigDecimal result = null;
		if(value instanceof Long) {
			result = new BigDecimal((Long)value);
		}else if(value instanceof Date) {
			Date date = (Date)value;
			result = new BigDecimal(date.getTime());
		}else if(value instanceof java.sql.Timestamp) {
			java.sql.Timestamp timestamp = (java.sql.Timestamp)value;
			result = new BigDecimal(timestamp.getTime());
		}else if(value instanceof java.sql.Date) {
			Date date = (java.sql.Date)value;
			result = new BigDecimal(date.getTime());
		}else if(value instanceof LocalDateTime) {
			result = new BigDecimal(DateUtil.parse((LocalDateTime)value).getTime());
		}else if(value instanceof LocalDate) {
			result = new BigDecimal(DateUtil.parse((LocalDate)value).getTime());
		}else{
			try {
				result = new BigDecimal(value.toString());
			} catch (Exception e) {
				return def;
			}
		}
		if(null == result) {
			result = def;
		}
		return result;
	}
	public static Long parseLong(Object value, Long def) {
		try{
			if(null == value) {
				return def;
			}
			return parseLong(value);
		}catch (Exception e) {
			return def;
		}
	}
	public static Long parseLong(Object value) throws NumberFormatException {
		if (value == null) {
			//return null;
			//应该抛出异常
		}
		if(value instanceof Long) {
			return (Long)value;
		}
		if(value instanceof Date) {
			Date date = (Date)value;
			return date.getTime();
		}
		if(value instanceof java.sql.Timestamp) {
			java.sql.Timestamp timestamp = (java.sql.Timestamp)value;
			return timestamp.getTime();
		}
		if(value instanceof java.sql.Date) {
			Date date = (java.sql.Date)value;
			return date.getTime();
		}
		if(value instanceof LocalDateTime || value instanceof LocalDate) {
			return DateUtil.parse(value).getTime();
		}
		return Long.parseLong(value.toString());
	}

	/**
	 * 类型转换
	 *
	 * @param obj  obj
	 * @param def  def
	 * @return Boolean
	 */
	public static Boolean parseBoolean(Object obj, Boolean def) {
		if(obj instanceof Boolean) {
			return (Boolean) obj;
		}
		try {
			return parseBoolean(obj);
		} catch (Exception e) {
			return def;
		}
	}

	public static Boolean parseBoolean(Object obj) {
		if(obj instanceof Boolean) {
			return (Boolean)obj;
		}
		if ("1".equals(obj.toString())
				|| "true".equalsIgnoreCase(obj.toString())
				|| "t".equalsIgnoreCase(obj.toString())
				|| "on".equalsIgnoreCase(obj.toString())
				|| "yes".equalsIgnoreCase(obj.toString())
				|| "y".equalsIgnoreCase(obj.toString())) {
			return true;
		} else if ("0".equals(obj.toString())
				|| "false".equalsIgnoreCase(obj.toString())
				|| "off".equalsIgnoreCase(obj.toString())
				|| "f".equalsIgnoreCase(obj.toString())
				|| "no".equalsIgnoreCase(obj.toString())
				|| "n".equalsIgnoreCase(obj.toString())) {
			return  false;
		} else {
			return Boolean.parseBoolean(obj.toString());
		}
	}

	/**
	 * 拆分权限数 : 将任意一个数拆分成多个（2的n次方）的和
	 *
	 * @param num  num
	 * @return List
	 */
	public static List<String> parseLimit(int num) {
		List<String> list = new ArrayList<>();
		int count = 0;
		while (num >= 1) {
			int temp = num % 2;
			num = (num - temp) / 2;
			if (temp == 1) {
				if (count == 0) {
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
	 * 根据正则替换
	 *
	 * @param src  src
	 * @param pattern  pattern
	 * @param replacement  替换成replacement
	 * @return String
	 */
	public static String replace(String src, String pattern, String replacement) {
		if (src == null)
			return null;
		int s = 0;
		int e = 0;
		StringBuilder result = new StringBuilder();
		while ((e = src.indexOf(pattern, s)) >= 0) {
			result.append(src.substring(s, e));
			result.append(replacement);
			s = e + pattern.length();
		}

		result.append(src.substring(s));
		return result.toString();
	}
	/**
	 * 根据正则替换(只替换第1个)
	 * 通知是因为相同的原文要替换成不同的结果
	 * 如定义a=1 输出{a} 重定义a=2 输出{a}
	 * @param src  src
	 * @param pattern  pattern
	 * @param replacement  替换成replacement
	 * @return String
	 */
	public static String replaceFirst(String src, String pattern, String replacement) {
		if (src == null)
			return null;
		int s = 0;
		int e = 0;
		StringBuilder result = new StringBuilder();
		if ((e = src.indexOf(pattern, s)) >= 0) {
			result.append(src.substring(s, e));
			result.append(replacement);
			s = e + pattern.length();
		}
		result.append(src.substring(s));
		return result.toString();
	}

	//直接替换文本不解析
	public static String replace(String text, Map<String, String> replaces){
		if(null != text){
			for(String key:replaces.keySet()){
				String value = replaces.get(key);
				//原文没有${}的也不要添加
				text = text.replace(key, value);
			}
		}
		return text;
	}
	/**
	 * 删除空格
	 *
	 * @param str  str
	 * @return String
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
	 * @return String
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
	 * @param str  str
	 * @return String
	 */
	public static String compress(String str) {
		if (null != str) {
			//str = str.replaceAll("\\s{2,}", " ").trim();
			//不压缩引号内空格
			String reg = "\\s+(?=([^']*'[^']*')*[^']*$)";
			str = str.replaceAll(reg, " ").trim();
		}
		return str;
	}

	public static String[] compress(String[] strs) {
		if (null != strs) {
			int size = strs.length;
			for (int i = 0; i < size; i++) {
				strs[i] = compress(strs[i]);
			}
		}
		return strs;
	}

	public static List<String> compress(List<String> strs) {
		List<String> result = new ArrayList<>();
		if (null != strs) {
			for(String str:strs) {
				result.add(compress(str));
			}
		}
		return strs;
	}

	public static String compressXml(String xml) {
		xml = compress(xml);
		xml = xml.replaceAll("<\\!--[\\s\\S]*-->", "");
		xml = xml.replaceAll("\\s{1,}<", "<");
		return xml;
	}

	/**
	 * 填充字符(从左侧填充)
	 *
	 * @param src   原文
	 * @param chr  填充字符
	 * @param len  需要达到的长度
	 * @return String
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

	public static String fillLChar(int src, String chr, int len) {
		return fillLChar(src+"", chr, len);
	}

	public static String fillRChar(int src, String chr, int len) {
		 return fillRChar(src+"", chr, len);
	}

	public static String fillChar(int src, String chr, int len) {
		return fillChar(src+"", chr, len);
	}

	public static String fillChar(int src, int len) {
		return fillChar(src+"", len);
	}


	public static String tab(String origin, int qty) {
		return tab(origin, qty, "\t");
	}
	public static String tab(String origin) {
		return tab(origin, 1, "\t");
	}
	public static String tab(String origin, int qty, String tab) {
		if(null != origin) {
			String[] lines = origin.split("\n");
			StringBuilder tabs = new StringBuilder();
			for(int i=0; i<qty; i++) {
				tabs.append(tab);
			}
			StringBuilder builder = new StringBuilder();
			for (String line:lines) {
				builder.append("\n").append(tabs).append(line);
			}
			return builder.toString();
		}
		return origin;
	}
	/**
	 * 提取HashMap的key
	 *
	 * @param map  map
	 * @return List
	 */
	public static List<String> getMapKeys(Map<?, ?> map) {
		List<String> keys = new ArrayList<>();
		Iterator<?> it = map.keySet().iterator();
		while (it.hasNext()) {
			Object value = it.next();
			if(null != value) {
				keys.add(value.toString());
			}
		}
		return keys;
	}

	public static List<String> split(String str, String separator) {
		List<String> list = new ArrayList<>();
		if(null !=str && null != separator) {
			String tmps[] = str.split(separator);
			for(String tmp : tmps) {
				tmp = tmp.trim();
				if(BasicUtil.isNotEmpty(tmp)) {
					list.add(tmp);
				}
			}
		}
		return list;
	}

	/**
	 * char在full中出现次数
	 *
	 * @param full  全文
	 * @param chr  chr
	 * @return int
	 */
	public static int charCount(String full, String chr) {
		int count = 0;
		int idx = -1;
		if (null == full || null == chr || chr.trim().isEmpty()) {
			return 0;
		}
		boolean first = true;
		while (true) {
			if(first){
				idx = full.indexOf(chr);
			}else {
				idx = full.indexOf(chr, idx + chr.length());
			}
			first = false;
			if(idx == -1){
				break;
			}
			count++;
		}
		return count;
	}

	/**
	 * 检测是否完整String 没有拆破"" ''
	 * 一般是用来判断根据标识符拆分后 前缀有没有不成对的引号用来判断 标记符有没有在引号内
	 * @param text 一般是前缀
	 * @return boolean
	 */
	public static boolean isFullString(String text){
		return BasicUtil.charCount(text, "'")%2==0 && BasicUtil.charCount(text, "\"")%2==0;
	}
	public static Object fetch(Collection<?> items, String key, Object value) {
		if (null == items) {
			return null;
		}
		for (Object item : items) {
			Object tmpValue = BeanUtil.getFieldValue(item, key, true);
			if (null != tmpValue && tmpValue.equals(value)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 截取子串
	 * @param src string
	 * @param fr 开始位置
	 * @param to 结束位置 负数表示倒数, 如-2表示删除最后2位
	 * @return String
	 */
	public static String cut(String src, int fr, int to) {
		if (null == src) {
			return null;
		}
		int len = src.length();
		if (to > len) {
			to = len;
		}
		if(to < 0) {
			to = src.length() + to;
		}
		if(to < 0 || to < fr) {
			return null;
		}
		return src.substring(fr, to);
	}

	/**
	 * 从左侧开始取len位
	 * @param src String
	 * @param len 截取长度
	 * @return String
	 */
	public static String left(String src, int len) {
		if(null == src) {
			return null;
		}
		int max = src.length();
		if(len > max) {
			len = max;
		}
		return src.substring(0, len);
	}

	/**
	 * 从右侧开始取len位
	 * @param src String
	 * @param len 截取长度
	 * @return String
	 */
	public static String right(String src, int len) {
		if(null == src) {
			return null;
		}
		int max = src.length();
		if(len > max) {
			len = max;
		}
		return src.substring(max-len, max);
	}

	/**
	 * 超长部分忽略
	 * @param length 最长显示长度
	 * @param src 原文
	 * @return String
	 */
	public static String ellipsis(int length, String src) {
		String result ="";
		int size = length * 2;
		String chrs[] = src.split("");
		long cnt = 0;
		boolean above = false;
		for(String chr:chrs) {
			if(cnt >= size) {
				above = true;
				break;
			}
			if(SINGLE_CHAR.contains(chr.toLowerCase())) {
				cnt += 1;
			}else{
				cnt += 2;
			}
			result += chr;
		}
		if(above) {
			result += "...";
		}
		return result;
	}

	/**
	 * 获取本机IP
	 * @return List
	 */
	@SuppressWarnings("rawtypes")
	public static List<InetAddress> localInetAddress() {
		List<InetAddress> ips = new ArrayList<InetAddress>();
		try{
			Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				Enumeration addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					if(ip != null && ip instanceof Inet4Address) {
						ips.add(ip);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ips;
	}
	/**
	 * 获取本机IP地址
	 * @return List
	 */
	public static List<String> localIps() {
		List<String> ips = new ArrayList<>();
		List<InetAddress> list = localInetAddress();
		for(InetAddress ip:list) {
			ips.add(ip.getHostAddress());
		}
		return ips;
	}

	/**
	 * 数组是否包含
	 * @param objs  objs
	 * @param obj  obj
	 * @param ignoreCase  是否不区分大小写
	 * @param ignoreNull  是否忽略null, 如果忽略 则无论是否包含null都返回false
	 * @return boolean
	 */
	public static boolean containsString(boolean ignoreNull, boolean ignoreCase, Object[] objs, String obj) {
		if(null == objs) {
			return false;
		}
		return containsString(ignoreNull, ignoreCase, BeanUtil.array2list(objs), obj);
	}
	public static int index(boolean ignoreNull, boolean ignoreCase, Object[] objs, String obj) {
		if(null == objs) {
			return -1;
		}
		return index(ignoreNull, ignoreCase, BeanUtil.array2list(objs), obj);
	}
	public static boolean containsString(Object[] objs, String obj) {
		return containsString(false, false, objs, obj);
	}
	public static int index(Object[] objs, String obj) {
		return index(false, false, objs, obj);
	}
	public static boolean contains(Object[] objs, Object obj) {
		if(null == objs) {
			return false;
		}
		return contains(false, BeanUtil.array2list(objs), obj);
	}
	public static int index(Object[] objs, Object obj) {
		if(null == objs) {
			return -1;
		}
		return index(false, BeanUtil.array2list(objs), obj);
	}

	public static int index(boolean ignoreNull, Collection<Object> objs, Object obj) {
		return index(ignoreNull, objs, obj);
	}
	public static boolean contains(boolean ignoreNull, Collection<Object> objs, Object obj) {
		if(null == objs) {
			return false;
		}
		for(Object o : objs) {
			if(ignoreNull) {
				if(null == obj || null == o) {
					continue;
				}
			}else{
				if(null == obj && null == o) {
					return true;
				}
			}
			if(null == o) {
				continue;
			}
			if(obj.equals(o)) {
				return true;
			}
		}
		return false;
	}
	public static <T> boolean containsString(boolean ignoreNull, boolean ignoreCase, Collection<T> objs, String obj) {
		if(null == objs) {
			return false;
		}
		int idx = 0;
		for(T o : objs) {
			if(ignoreNull) {
				if(null == obj || null == o) {
					continue;
				}
			}else{
				if(null == obj && null == o) {
					return true;
				}
			}
			if (null != obj) {
				if(null == o) {
					continue;
				}
				String val = o.toString();
				if(ignoreCase) {
					obj = obj.toLowerCase();
					val = val.toLowerCase();
				}
				if(obj.equals(val)) {
					return true;
				}
			}
		}
		return false;
	}

	public static <T> int index(boolean ignoreNull, boolean ignoreCase, Collection<T> objs, String obj) {
		int idx = -1;
		if(null == objs) {
			return -1;
		}
		for(T o : objs) {
			idx ++;
			if(ignoreNull) {
				if(null == obj || null == o) {
					continue;
				}
			}else{
				if(null == obj && null == o) {
					return idx;
				}
			}
			if (null != obj) {
				if(null == o) {
					continue;
				}
				String val = o.toString();
				if(ignoreCase) {
					obj = obj.toLowerCase();
					val = val.toLowerCase();
				}
				if(obj.equals(val)) {
					return idx;
				}
			}
		}
		return -1;
	}
	public static boolean containsString(Collection<Object> objs, String obj) {
		return containsString(false, false, objs, obj);
	}
	public static int index(Collection<Object> objs, String obj) {
		return index(false, false, objs, obj);
	}

	/**
	 * 拼接字符
	 * @param list list
	 * @param split split
	 * @return String
	 */
   public static String concat(List<String> list, String split) {
	   if(null == list) {
		   return "";
	   }
	   StringBuffer result = new StringBuffer();
	   for(String val:list) {
		   if(BasicUtil.isEmpty(val)) {
			   continue;
		   }
		   if(result.length() > 0) {
			   result.append(split);
		   }
		   result.append(val);
	   }
	   return result.toString();
   }

	public static String concat(String split, String ... values) {
		StringBuilder builder = new StringBuilder();
		if(null != values) {
			for(String value:values) {
				if(BasicUtil.isEmpty(value)) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(split);
				}
				builder.append(value);
			}
		}
		return builder.toString();
	}
   public static String omit(String src, int left, int right) {
	   return omit(src, left, right, "*");
   }

	/**
	 *
	 * @param src 原文
	 * @param vol 每个段最大长度,超出 vol 的拆成多段(vol大于1时有效)
	 * @param left 每段左侧保留原文长度
	 * @param right 每段右侧保留原文长度
	 * @param ellipsis 省略符号
	 * @return string
	 */
   public static String omit(String src, int vol, int left, int right, String ellipsis) {
	   String result = "";
	   if(BasicUtil.isEmpty(src)) {
		   return result;
	   }
	   int length = src.length();
	   if(length > vol && vol > 1) {
		   List<String> list = new ArrayList<>();
		   while (null != src && !src.isEmpty()) {
			   String cut = BasicUtil.cut(src, 0, vol);
			   list.add(cut);
			   if(src.length() > vol) {
				   list.add(ellipsis);//连接处
			   }
			   src = BasicUtil.cut(src, vol+1, src.length());
		   }
		   StringBuilder builder = new StringBuilder();
		   for(String item:list){
			   builder.append(omit(item, left, right, ellipsis));
		   }
		   result = builder.toString();
	   }else{
		   result = omit(src, left, right, ellipsis);
	   }

	   return result;
   }
	public static String omit(String src, int len, int left, int right) {
		return omit(src, len, left, right, "*");
	}
	public static String omit(String src, int left, int right, String ellipsis) {
		String result = "";
		if(BasicUtil.isEmpty(src)) {
			return result;
		}
		if(src.equals(ellipsis)){
			return src;
		}
		int length = src.length();
		if(left > length) {
			left = length;
		}
		if(right > length - left) {
			right = length - left;
		}
		String l = src.substring(0, left);
		String r = "";
		if (length > right + left) {
			r = src.substring(length - right);
		} else if(right >0){ //避免全部明文
			r = ellipsis + src.substring(length - right + 1);
		}

		result = l + BasicUtil.fillRChar("", ellipsis, length-left-right) + r;
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
	 * @param properties1 属性列表1
	 * @param properties2 属性列表2
	 * @return boolean
	 */
	public static boolean equals(Object v1, Object v2, List<String> properties1, List<String> properties2) {
		boolean result = false;
		if(null == v1 && null == v2) {
			return true;
		}else if(null == v1 || null == v2) {
			return false;
		}
		if(v1 instanceof String || v1 instanceof Number || v1 instanceof Boolean || v1 instanceof Date) {
			// v1基础类型
			if(v2 instanceof String || v2 instanceof Number || v2 instanceof Boolean || v2 instanceof Date) {
				// v2基础类型
				result = v2.toString().equals(v1.toString());
			}else{
				// v2非基础类型
				if(null != properties2 && !properties2.isEmpty()) {
					v2 = BeanUtil.getFieldValue(v2, properties2.get(0), true)+"";
				}
				result = v2.toString().equals(v1.toString());
			}
		}else{
			// v1非基础类型
			if(v2 instanceof String || v2 instanceof Number || v2 instanceof Boolean || v2 instanceof Date) {
				// v2基础类型
				if(null != properties1 && !properties1.isEmpty()) {
					v1 = BeanUtil.getFieldValue(v1, properties1.get(0), true)+"";
				}
				result = v2.toString().equals(v1.toString());
			}else{
				// v2非基础类型
				boolean eq = true;
				int psize = 0;
				if(null == properties1 || null == properties2) {
					eq = false;
				}else{
					// 取长度较短的一个长度
					psize = NumberUtil.min(properties1.size(), properties2.size());
				}
				if(psize > 0) {
					for(int i=0; i<psize; i++) {
						String p1 = properties1.get(i);
						String p2 = properties2.get(i);
						String vv1 = BeanUtil.getFieldValue(v1, p1, true)+"";
						String vv2 = BeanUtil.getFieldValue(v2, p2, true)+"";
						if(!vv1.equals(vv2)) {
							eq = false;
							break;
						}
					}
				}else{
					// 没有设置property
					eq = v1.equals(v2);
				}
				if(eq) {
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
	 * @param properties 属性(ID:CD, NM:NAME)(ID, NM)
	 * @return boolean
	 */
	public static boolean equals(Object v1, Object v2, String properties) {
		boolean result = false;
		List<String> properties1 = new ArrayList<>();
		List<String> properties2 = new ArrayList<>();
		if(BasicUtil.isNotEmpty(properties)) {
			String[] ps = properties.split(",");
			for(String p:ps) {
				if(BasicUtil.isNotEmpty(p)) {
					String p1 = p;
					String p2 = p;
					if(p.contains(":")) {
						String[] tmps = p.split(":");
						p1 = tmps[0];
						p2 = tmps[1];
					}
					properties1.add(p1);
					properties2.add(p2);
				}
			}
		}
		return equals(v1, v2, properties1, properties2);
	}

	/**
	 * 计算下标
	 * @param index 下标 从0开始 -1表示最后一行 -2表示倒数第2行
	 * @param size 总行数
	 * @return 最终下标
	 */
	public static int index(int index, int size) {
		if(size == 0) {
			return 0;
		}
		if(index >= size) {
			index = size -1;
		}else if(index < 0) {
			//倒数
			index = size + index;
			if(index < 0) {
				//超出0按0算
				index = 0;
			}
		}
		return index;
	}

	/**
	 * 确认边界
	 * @param begin 开始
	 * @param end 结束
	 * @param qty 数量
	 * @param total 总数
	 * @return [开始, 结束]
	 */
	public static int[] range(Integer begin, Integer end, Integer qty, Integer total) {
		int[] result = new int[2];
		if(null != begin && begin < 0) {
			begin = 0;
		}
		if(null != end && end < 0) {// end<0, 取最后-end个
			begin = total + end;
			end = total;
		}
		if(null != begin && null != qty) {
			end = begin + qty;
		}
		if(null != total) {
			if(null == end || end > total) {
				end = total;
			}
		}
		if(null == begin) {
			begin = 0;
		}
		if(null == end) {
			end = total;
		}
		if(end < begin) {
			end = begin;
		}
		if(begin <0) {
			begin = 0;
		}
		if(end <0) {
			end = 0;
		}
		result[0] = begin;
		result[1] = end;
		return result;
	}

	/**
	 * 检测是否符合el格式
	 * ${body}<br/>
	 * 主要过滤掉${A}xxx{B}中间不能再再现其他{}或${}格式
	 * @param str string
	 * @return boolean
	 */
	public static boolean checkEl(String str) {
		if(null != str) {
			if(str.startsWith("${") && str.endsWith("}")) {
				String body = RegularUtil.cut(str, "${", "}");
				if(body.length() == str.length()-3) {
					//过滤 ${A}XX{B} 格式
					return true;
				}
			}
		}
		return false;
	}

}
