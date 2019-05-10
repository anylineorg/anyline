package org.anyline.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CodeUtil {
	public static String escape(String src) {  
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
	       StringBuffer tmp = new StringBuffer();  
	       tmp.ensureCapacity(src.length());  
	       int lastPos = 0, pos = 0;  
	       char ch;  
	       while (lastPos < src.length()) {  
	           pos = src.indexOf("%", lastPos);  
	           if (pos == lastPos) {  
	               if (src.charAt(pos + 1) == 'u') {  
	                   ch = (char) Integer.parseInt(src  
	                           .substring(pos + 2, pos + 6), 16);  
	                   tmp.append(ch);  
	                   lastPos = pos + 6;  
	               } else {  
	                   ch = (char) Integer.parseInt(src  
	                           .substring(pos + 1, pos + 3), 16);  
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
		StringBuffer sbu = new StringBuffer();
		String[] chars = str.split(",");
		for (int i = 0; i < chars.length; i++) {
			sbu.append((char) Integer.parseInt(chars[i]));
		}
		return sbu.toString();
	}

    public static String urlDecode(String url, String encode) {
        String result = "";
        if (null == url) {
            return "";
        }
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
        String result = "";
        if (null == url) {
            return "";
        }
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
}
