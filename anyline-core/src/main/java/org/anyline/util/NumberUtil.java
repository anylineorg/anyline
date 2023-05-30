/*
 * Copyright 2006-2023 www.anyline.org
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class NumberUtil {
	/*
	* HEX ：十六进制 Hexadecimal
	* DEC ：十进制 Decimal
	* OCT ：八进制 Octal
	* BIN ：二进制 Binary
* */
	/**
	 * 数据格式化
	 *
	 * @param src src
	 * @param pattern pattern
	 * @return String
	 */
	public static String format(String src, String pattern) {
		if (BasicUtil.isEmpty(src) || BasicUtil.isEmpty(src.trim())) {
			return "";
		}
		BigDecimal num = null;
		try{
			num = new BigDecimal(src);
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		return format(num, pattern);
	}

	/**
	 * 数字格式化
	 *
	 * @param src src
	 * @param pattern pattern
	 * @return String
	 */
	public static String format(Number src, String pattern) {
		if (null == src) {
			return "";
		}
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(src);
	}
	/**
	 * 中文大写(简体)
	 * @param num num
	 * @return String
	 */
	public static String upper(long num){
		return NumberTextUtil.getInstance(NumberTextUtil.Lang.ChineseSimplified).getText(num);
	}

	/**
	 * 选取最大数
	 *
	 * @param nums nums
	 * @return BigDecimal
	 */
	public static BigDecimal max(BigDecimal... nums) {
		BigDecimal max = nums[0];
		for (BigDecimal num:nums) {
			if(null == max){
				max = num;
			}else if (null != num && max.compareTo(num) <0) {
				max = num;
			}
		}
		return max;
	}
	/**
	 * 选取最小数
	 *
	 * @param nums nums
	 * @return BigDecimal
	 */
	public static BigDecimal min(BigDecimal... nums) {
		BigDecimal min = nums[0];
		for (BigDecimal num:nums) {
			if(null == min){
				min = num;
			}else if (null != num && min.compareTo(num) >0) {
				min = num;
			}
		}
		return min;
	}

	/**
	 * 选取最大数
	 *
	 * @param nums nums
	 * @return double
	 */
	public static double max(double... nums) {
		double max = nums[0];
		for (double num:nums) {
			if (max < num) {
				max = num;
			}
		}
		return max;
	}
	/**
	 * 选取最小数
	 *
	 * @param nums nums
	 * @return double
	 */
	public static double min(double... nums) {
		double min = nums[0];
		for (double num:nums) {
			if (min > num) {
				min = num;
			}
		}
		return min;
	}
	/**
	 * 选取最大数
	 *
	 * @param nums nums
	 * @return int
	 */
	public static int max(int... nums) {
		int max = nums[0];
		for (int num:nums) {
			if (max < num) {
				max = num;
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 *
	 * @param nums nums
	 * @return int
	 */
	public static int min(int... nums) {
		int min = nums[0];
		for (int num:nums) {
			if (min > num) {
				min = num;
			}
		}
		return min;
	}

	/**
	 * 选取最大数
	 *
	 * @param nums nums
	 * @return long
	 */
	public static long max(long... nums) {
		long max = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (max < nums[i]) {
				max = nums[i];
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 *
	 * @param nums nums
	 * @return long
	 */
	public static long min(long... nums) {
		long min = nums[0];
		for (long num:nums) {
			if (min > num) {
				min = num;
			}
		}
		return min;
	}

	/**
	 * 选取最大数
	 *
	 * @param nums nums
	 * @return float
	 */
	public static float max(float... nums) {
		float max = nums[0];
		for (float num:nums) {
			if (max < num) {
				max = num;
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 *
	 * @param nums nums
	 * @return float
	 */
	public static float min(float... nums) {
		float min = nums[0];
		for (float num:nums) {
			if (min > num) {
				min = num;
			}
		}
		return min;
	}

	/**
	 * 选取最大数
	 *
	 * @param nums nums
	 * @return short
	 */
	public static short max(short... nums) {
		short max = nums[0];
		for (short num:nums) {
			if (max < num) {
				max = num;
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 *
	 * @param nums nums
	 * @return short
	 */
	public static short min(short... nums) {
		short min = nums[0];
		for (short num:nums) {
			if (min > num) {
				min = num;
			}
		}
		return min;
	}
	public static int random(int fr, int to) {
		int result = 0;
		Random r = new Random();
		result = fr + r.nextInt(to - fr);
		return result;
	}
	public static double random(double fr, double to) {
		double result = 0;
		Random r = new Random();
		result = fr + r.nextDouble() * (to - fr);
		return result;
	}
	public static long random(long fr, long to) {
		long result = 0;
		Random r = new Random();
		result = fr + r.nextLong() * (to - fr);
		return result;
	}
	public static float random(float fr, float to) {
		float result = 0;
		Random r = new Random();
		result = fr + r.nextFloat() * (to - fr);
		return result;
	}

	public static List<Integer> random(int fr, int to, int qty) {
		List<Integer> list = new ArrayList<Integer>();
		Random r = new Random();
		while(true){
			int rdm = fr + r.nextInt(to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	public static List<Double> random(double fr, double to, int qty) {
		List<Double> list = new ArrayList<Double>();
		Random r = new Random();
		while(true){
			double rdm = fr + r.nextDouble() * (to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	public static List<Float> random(float fr, float to, int qty) {
		List<Float> list = new ArrayList<Float>();
		Random r = new Random();
		while(true){
			float rdm = fr + r.nextFloat() * (to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	public static List<Long> random(long fr, long to, int qty) {
		List<Long> list = new ArrayList<Long>();
		Random r = new Random();
		while(true){
			long rdm = fr + r.nextLong() * (to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}

	public static boolean isInt(double src){
		return src == (int)src;
	}
	public static boolean isInt(float src){
		return src == (int)src;
	}


	/**
	 * 二进制转十进制
	 * @param number number
	 * @return int
	 */
	public static int bin2dec(String number) {
		return Integer.parseInt(number,2);
	}


	/**
	 * 十进制转二进制
	 * @param number number
	 * @return String
	 */
	public static String dec2bin(int number) {
		return Integer.toBinaryString((number & 0xFF) + 0x100).substring(1);
	}
	/**
	 * 整形转换成网络传输的字节流（字节数组）型数据
	 * @param i 一个整型数据
	 * @return 4个字节的byte数组
	 */
	public byte[] dec2bytes(int i){
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (0xff & (i >> 0));
		bytes[1] = (byte) (0xff & (i >> 8));
		bytes[2] = (byte) (0xff & (i >> 16));
		bytes[3] = (byte) (0xff & (i >> 24));
		return bytes;
	}

	/**
	 * 四个字节的字节数据转换成一个整形数据
	 * @param bytes 4个字节的字节数组
	 * @return 一个整型数据
	 */
	public static int byte2int(byte[] bytes) {
		int num = 0;
		int temp;
		temp = (0x000000ff & (bytes[0])) << 0;
		num = num | temp;
		temp = (0x000000ff & (bytes[1])) << 8;
		num = num | temp;
		temp = (0x000000ff & (bytes[2])) << 16;
		num = num | temp;
		temp = (0x000000ff & (bytes[3])) << 24;
		num = num | temp;
		return num;
	}
	public static int byte2int(byte[] bytes, int offset) {
		return (bytes[offset] & 0xFF) |
				((bytes[offset + 1] & 0xFF) << 8) |
				((bytes[offset + 2] & 0xFF) << 16) |
				((bytes[offset + 3] & 0xFF) << 24);
	}

	/**
	 * 长整形转换成网络传输的字节流（字节数组）型数据
	 *
	 * @param l 一个长整型数据
	 * @return 4个字节的byte数组
	 */
	public static byte[] dec2bytes(long l) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (0xff & (l >> (i * 8)));
		}
		return bytes;
	}

	/**
	 * 大数字转换字节流（字节数组）型数据
	 *
	 * @param n 十进制
	 * @return bytes
	 */
	public static byte[] dec2bytes(BigInteger n) {
		byte tmpd[] = (byte[]) null;
		if (n == null) {
			return null;
		}

		if (n.toByteArray().length == 33) {
			tmpd = new byte[32];
			System.arraycopy(n.toByteArray(), 1, tmpd, 0, 32);
		} else if (n.toByteArray().length == 32) {
			tmpd = n.toByteArray();
		} else {
			tmpd = new byte[32];
			for (int i = 0; i < 32 - n.toByteArray().length; i++) {
				tmpd[i] = 0;
			}
			System.arraycopy(n.toByteArray(), 0, tmpd, 32 - n.toByteArray().length, n.toByteArray().length);
		}
		return tmpd;
	}

	/**
	 * 换字节流（字节数组）型数据转大数字
	 * @param bytes bytes
	 * @return BigInteger
	 */
	public static BigInteger byte2bigint(byte[] bytes) {
		if (bytes[0] < 0) {
			byte[] temp = new byte[bytes.length + 1];
			temp[0] = 0;
			System.arraycopy(bytes, 0, temp, 1, bytes.length);
			return new BigInteger(temp);
		}
		return new BigInteger(bytes);
	}
	/**
	 * 16进制转10进制
	 * @param hex hex
	 * @return int
	 */
	public static int hex2dec(String hex){
		return Integer.parseInt(hex,16);
	}

	/**
	 * 截取hex中fr到to部分转成10进制
	 * @param hex hex数组
	 * @param fr 开始位置
	 * @param to 结束位置
	 * @return int
	 */
	public static int hex2dec(String[] hex, int fr, int to){
		StringBuilder builder = new StringBuilder();
		for(int i=fr; i<=to; i++){
			builder.append(hex[i]);
		}
		return Integer.valueOf(builder.toString(), 16);
	}

	public static String byte2hex(byte[] bytes, String split) {
		return byte2hex(bytes, bytes.length, split);
	}
	public static String byte2hex(byte[] bytes,int len) {
		return byte2hex(bytes, len, null);
	}
	public static String byte2hex(byte[] bytes, int len, String split) {
		StringBuffer builder = new StringBuffer();
		for(int i = 0; i < len; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if(hex.length() < 2){
				builder.append(0);
			}
			builder.append(hex);
			if(i<len-1 && null != split){
				builder.append(split);
			}
		}
		return builder.toString();
	}
	public static String byte2hex(byte[] bytes) {
		return byte2hex(bytes,null);
	}
	public static String[] byte2hexs(byte[] bytes) {
		String[] hexs = new String[bytes.length];
		for(int i=0; i<hexs.length; i++){
			hexs[i] = byte2hex(bytes[i]);
		}
		return hexs;
	}
	public static String byte2hex(byte b) {
		String hex = Integer.toHexString(b & 0xFF);
		if(hex.length() < 2){
			hex = "0" + hex;
		}
		return hex;
	}

	public static String dec2hex(int number){
		String hex = Integer.toHexString(number & 0xFF);
		if(hex.length() < 2){
			hex = "0" + hex;
		}
		return hex;
	}
	public static String[] dec2hex(int[] numbers){
		String[] hex = new String[numbers.length];
		for(int i=0; i<hex.length; i++){
			hex[i] = Integer.toHexString(numbers[i]);
		}
		return hex;
	}
	/**
	 * 从byte数组中截取fr到to转换成String 按charset编码格式
	 * @param bytes bytes
	 * @param fr fr
	 * @param to to
	 * @param charset 编码
	 * @return String
	 */
	public static String byte2string(byte[] bytes, int fr, int to, String charset){
		byte[] bts = new byte[to-fr+1];
		for(int i=fr; i<=to; i++){
			bts[i-fr] = bytes[i];
		}
		return new String(bts, Charset.forName(charset));
	}
	public static String byte2string(byte[] bytes, int fr, int to){
		return byte2string(bytes, fr, to, "UTF-8");
	}
	public static String byte2string(byte[] bytes){
		return byte2string(bytes, 0, bytes.length-1, "UTF-8");
	}
	public static String byte2string(byte[] bytes, String charset){
		return byte2string(bytes, 0, bytes.length-1, charset);
	}

	public static byte[] hex2bytes(String hex){
		int hexlen = hex.length();
		byte[] result;
		if (hexlen % 2 == 1){
			// 奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			hex="0"+hex;
		}else {
			// 偶数
			result = new byte[(hexlen/2)];
		}
		int j=0;
		for (int i = 0; i < hexlen; i+=2){
			result[j]=hex2byte(hex.substring(i,i+2));
			j++;
		}
		return result;
	}

	public static byte hex2byte(String hex){
		return (byte)Integer.parseInt(hex,16);
	}

	public static byte[] dec2bcd(long num) {
		int digits = 0;
		long temp = num;
		while (temp != 0) {
			digits++;
			temp /= 10;
		}
		int len = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
		byte bcd[] = new byte[len];
		for (int i = 0; i < digits; i++) {
			byte tmp = (byte) (num % 10);
			if (i % 2 == 0) {
				bcd[i / 2] = tmp;
			} else {
				bcd[i / 2] |= (byte) (tmp << 4);
			}
			num /= 10;
		}
		for (int i = 0; i < len / 2; i++) {
			byte tmp = bcd[i];
			bcd[i] = bcd[len - i - 1];
			bcd[len - i - 1] = tmp;
		}
		return bcd;
	}
	/**
	 * 将byte转成二进制
	 * @param b byte
	 * @return String
	 */
	public static String byte2bin(byte  b){
		String value = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
		return value;
	}public static int byte2decimal(byte res) {
		return res & 0xff;
	}
	/**
	 * ascii码
	 * @param b byte<br/>
	 * @return String
	 */
	public static String byte2ascii(byte b) {
		StringBuilder sb = new StringBuilder();
		int value = byte2decimal(b);
		sb.append((char) value);
		return sb.toString();
	}


	public static String byte2bcd(byte bit) {
		StringBuffer sb = new StringBuffer();
		byte high = (byte) (bit & 0xf0);
		high >>>= (byte) 4;
		high = (byte) (high & 0x0f);
		byte low = (byte) (bit & 0x0f);
		sb.append(high);
		sb.append(low);
		return sb.toString();
	}

	public static String bytes2bcd(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(byte2bcd(bytes[i]));
		}
		return sb.toString();
	}

	/**
	 * String转16进制<br/>
	 * 中文abc123_# &gt;e4b8ade696876162633132335f23<br/>
	 *
	 * @param origin 原文
	 * @return hex
	 */

	public static String string2hex(String origin, String charset) {
		byte[] bytes = origin.getBytes(Charset.forName(charset));
		String hex = byte2hex(bytes);
		return hex;
	}
	public static String string2hex(String origin) {
		return string2hex(origin, "UTF-8");
	}

	/**
	 * 16进制转String<br/>
	 * e4b8ade696876162633132335f23 &gt; 中文abc123_#
	 * @param hex hex
	 * @return String
	 */
	public static String hex2string(String hex, String charset) {
		byte[] bytes = NumberUtil.hex2bytes(hex);
		return new String(bytes, Charset.forName(charset));
	}
	public static String hex2string(String hex) {
		return hex2string(hex, "UTF-8");
	}

	/**
	 * 16进制string拆分<br/>
	 * 0102 &gt; ["01","02"]
	 * @param hex hex
	 * @return strings
	 */
	public static String[] hex2array(String hex) {
		String[] array = new String[hex.length() / 2];
		int k = 2;
		for (int i = 0; i < array.length; i++) {
			array[i] = hex.substring(i * 2, k);
			k += 2;
		}
		return array;
	}
	public static byte[] string2bytes(String src, String charset){
		return src.getBytes(Charset.forName(charset));
	}
	public static byte[] string2bytes(String src){
		return src.getBytes(Charset.forName("UTF-8"));
	}

	/**
	 * 数字字符串转ASCII码字符串
	 *
	 * @param str 字符串
	 * @return ASCII字符串
	 */
	public static String string2ascii(String str) {
		String result = "";
		int max = str.length();
		for (int i = 0; i < max; i++) {
			char c = str.charAt(i);
			String b = Integer.toHexString(c);
			result = result + b;
		}
		return result;
	}

	/**
	 * ASCII码字符串转数字字符串
	 *
	 * @param content ASCII字符串
	 * @return 字符串
	 */
	public static String ascii2string(String content) {
		String result = "";
		int length = content.length() / 2;
		for (int i = 0; i < length; i++) {
			String c = content.substring(i * 2, i * 2 + 2);
			int a = hex2dec(c);
			char b = (char) a;
			String d = String.valueOf(b);
			result += d;
		}
		return result;
	}


	/**
	 * 压缩最前位的0与小数最后的0
	 * @param src src
	 * @param integer 是否压缩成整数 1|1.0
	 * @return String
	 */
	public static String compress(String src, boolean integer) {
		if (null != src) {
			int idx = src.indexOf(".");
			if(idx != -1){
				src = src.replaceAll("^0+","");
				src = src.replaceAll("0+$","");
			}
			if(".".equals(src)){
				if(integer){
					src = "0";
				}else{
					src = "0.0";
				}
			}else if(src.endsWith(".")){
				if(integer){
					src = src.substring(0, src.length()-1);
				}else{
					src = src + "0";
				}
			}
			if(src.startsWith(".")){
				src = "0" + src;
			}
		}
		return src;
	}

	public static double compress(double src) {
		String str = compress(src+"", false);
		src = BasicUtil.parseDouble(str, src);
		return src;
	}
	public static String compress(String src) {
		return compress(src, false);
	}



	/**
	 * byte转double[],可用于把数据库中的point(JDBC取出byte[])转成double[]
	 * @param bytes bytes
	 * @return double[]
	 */
	public static double[] byte2points(byte[] bytes){
		int len=(bytes.length-13)/8;
		double[] result=new double[len];
		for(int i=0;i<len; i++){
			result[i]=byte2double(bytes,13+i*8);
		}
		return result;
	}
	public static double byte2double(byte[] arr,int start) {
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value |= ((long) (arr[start+i] & 0xff)) << (8 * i);
		}
		return Double.longBitsToDouble(value);

	}
	public static byte[] double2bytes(double d) {
		long value = Double.doubleToRawLongBits(d);
		byte[] byteRet = new byte[8];
		for (int i = 0; i < 8; i++) {
			byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
		}
		return byteRet;
	}
} 
