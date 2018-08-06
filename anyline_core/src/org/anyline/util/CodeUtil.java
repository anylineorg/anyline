package org.anyline.util;


public class CodeUtil {
	public static void main(String args[]) {
		String str = "中s12_*";
		System.out.println("src\t\t:"+str);
		str = string2unicode(str);
		System.out.println("string2unicode\t:"+str);
		str = unicode2string(str);
		System.out.println("unicode2string\t:"+str);
		str = string2ascii(str);
		System.out.println("string2ascii\t:"+str);
		str = ascii2string(str);
		System.out.println("ascii2string\t:"+str);
		str = BasicUtil.escape(str);
		System.out.println("escape\t\t:"+str);
		str = BasicUtil.unescape(str);
		System.out.println("unescape\t:"+str);
		String url = "http://www.anyline.org?a=1&b=2&name=张";
		url = urlEncode(url);
		System.out.println("urlEncoder\t:"+url);
		url = urlDecode(url);
		System.out.println("urlDecoder\t:"+url);
	}
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

}
