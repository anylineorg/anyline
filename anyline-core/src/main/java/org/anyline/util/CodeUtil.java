package org.anyline.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeUtil {
	public static String escape(String src) {
		if (BasicUtil.isEmpty(src)) {
			return "";
		}
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);
		for (i = 0; i < src.length(); i++) {
			j = src.charAt(i);
			if (Character.isDigit(j) || Character.isLowerCase(j)
					|| Character.isUpperCase(j))
				tmp.append(j);
			else if (j < 256) {
				tmp.append("%");
				if (j < 16)
					tmp.append("0");
				tmp.append(Integer.toString(j, 16));
			} else {
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	public static String unescape(String src) {
		if (BasicUtil.isEmpty(src)) {
			return "";
		}
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(
							src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(
							src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	public static String string2unicode(String string) {
		if (string == null || string.isEmpty()) {
			return "";
		}
		StringBuilder unicode = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			unicode.append("\\u").append(Integer.toHexString(c));
		}
		return unicode.toString();
	}

	public static String unicode2string(String unicode) {
		if (unicode == null || unicode.isEmpty()) {
			return "";
		}
		StringBuilder string = new StringBuilder();
		String[] hex = unicode.split("\\\\u");
		for (int i = 1; i < hex.length; i++) {
			int data = Integer.parseInt(hex[i], 16);
			string.append((char) data);
		}
		return string.toString();
	}

	public static String string2ascii(String str) {
		if (BasicUtil.isEmpty(str)) {
			return "";
		}
		StringBuffer sbu = new StringBuffer();
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i != chars.length - 1) {
				sbu.append((int) chars[i]).append(",");
			} else {
				sbu.append((int) chars[i]);
			}
		}
		return sbu.toString();
	}

	public static String ascii2string(String str) {
		if (BasicUtil.isEmpty(str)) {
			return "";
		}
		StringBuffer sbu = new StringBuffer();
		String[] chars = str.split(",");
		for (int i = 0; i < chars.length; i++) {
			sbu.append((char) Integer.parseInt(chars[i]));
		}
		return sbu.toString();
	}

	public static String urlDecode(String url, String encode) {
		if (BasicUtil.isEmpty(url)) {
			return "";
		}
		String result = "";
		try {
			result = java.net.URLDecoder.decode(url, encode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String urlDecode(String url) {
		return urlDecode(url, "utf-8");
	}

	public static String urlEncode(String url, String encode) {
		if (BasicUtil.isEmpty(url)) {
			return "";
		}
		String result = "";
		try {
			result = java.net.URLEncoder.encode(url, encode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String urlEncode(String url) {
		return urlEncode(url, "utf-8");
	}

	public static String unicode2String(String str) {
		if (BasicUtil.isEmpty(str)) {
			return "";
		}
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			String group = matcher.group(2);
			ch = (char) Integer.parseInt(group, 16);
			String group1 = matcher.group(1);
			str = str.replace(group1, ch + "");
		}
		return str;
	}

	/**
	 * 半角转全角
	 * 
	 * @param input
	 * @return
	 */
	public static String half2full(String input) {
		if (BasicUtil.isEmpty(input)) {
			return "";
		}
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角
	 * 
	 * @param QJstr
	 * @return
	 */
	public static final String full2Half(String src) {
		if (BasicUtil.isEmpty(src)) {
			return "";
		}
		StringBuffer outStrBuf = new StringBuffer("");
		String str = "";
		try {
			byte[] b = null;
			int length = src.length();
			for (int i = 0; i < length; i++) {
				str = src.substring(i, i + 1);
				// 全角空格转换成半角空格
				if (str.equals("　")) {
					outStrBuf.append(" ");
					continue;
				}
				b = str.getBytes("unicode");
				// 得到 unicode 字节数据
				if (b[2] == -1) {
					// 表示全角？
					b[3] = (byte) (b[3] + 32);
					b[2] = 0;
					outStrBuf.append(new String(b, "unicode"));
				} else {
					outStrBuf.append(str);
				}
			} // end for.
		} catch (Exception e) {

		}
		return outStrBuf.toString();
	}
}
