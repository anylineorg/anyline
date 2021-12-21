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
 
import java.io.UnsupportedEncodingException; 
import java.net.URLDecoder; 
 
public class UrlUtil { 
	@SuppressWarnings("deprecation") 
	public static String decode(String url) { 
		if (isUTF8(url)) { 
			url = UTF8Decode(url); 
		} else { 
			url = URLDecoder.decode(url); 
		} 
		return url; 
	} 
	/** 
	 * 是否是UTF-8格式 
	 *  
	 * @param url  url
	 * @return return
	 */ 
	private static boolean isUTF8(String url) { 
		url = url.toLowerCase(); 
		int p = url.indexOf("%"); 
		if (p != -1 && url.length() - p > 9) { 
			url = url.substring(p, p + 9); 
		} 
		return checkUTF8(url); 
	} 
 
	/** 
	 * UTF-8编码是否有效 
	 *  
	 * @param text  text
	 * @return return
	 */ 
	private static boolean checkUTF8(String text) { 
		String sign = ""; 
		if (text.startsWith("%e")) 
			for (int i = 0, p = 0; p != -1; i++) { 
				p = text.indexOf("%", p); 
				if (p != -1) 
					p++; 
				sign += p; 
			} 
		return sign.equals("147-1"); 
	} 
 
	/** 
	 * utf8URL编码转字符 
	 *  
	 * @param text  text
	 * @return return
	 */ 
	private static String UTF82Word(String text) { 
		String result; 
		if (checkUTF8(text)) { 
			byte[] code = new byte[3]; 
			code[0] = (byte) (Integer.parseInt(text.substring(1, 3), 16) - 256); 
			code[1] = (byte) (Integer.parseInt(text.substring(4, 6), 16) - 256); 
			code[2] = (byte) (Integer.parseInt(text.substring(7, 9), 16) - 256); 
			try { 
				result = new String(code, "UTF-8"); 
			} catch (UnsupportedEncodingException ex) { 
				result = null; 
			} 
		} else { 
			result = text; 
		} 
		return result; 
	} 
 
	/** 
	 * Utf8URL解码 
	 *  
	 * @param url  text
	 * @return return
	 */ 
	private static String UTF8Decode(String url) { 
		String result = ""; 
		int p = 0; 
		if (url != null && url.length() > 0) { 
			url = url.toLowerCase(); 
			p = url.indexOf("%e"); 
			if (p == -1) 
				return url; 
			while (p != -1) { 
				result += url.substring(0, p); 
				url = url.substring(p, url.length()); 
				if ("".equals(url) || url.length() < 9) 
					return result; 
				result += UTF82Word(url.substring(0, 9)); 
				url = url.substring(9, url.length()); 
				p = url.indexOf("%e"); 
			} 
		} 
		return result + url; 
	} 
//	/** 
//	 * Utf8URL编码 
//	 *  
//	 * @param text  text
//	 * @return return
//	 */ 
//	private static String UTF8Encode(String text) { 
//		StringBuffer result = new StringBuffer(); 
//		for (int i = 0; i < text.length(); i++) { 
//			char c = text.charAt(i); 
//			if (c >= 0 && c <= 255) { 
//				result.append(c); 
//			} else { 
//				byte[] b = new byte[0]; 
//				try { 
//					b = Character.toString(c).getBytes("UTF-8"); 
//				} catch (Exception ex) { 
//				} 
//				for (int j = 0; j < b.length; j++) { 
//					int k = b[j]; 
//					if (k < 0) 
//						k += 256; 
//					result.append("%" + Integer.toHexString(k).toUpperCase()); 
//				} 
//			} 
//		} 
//		return result.toString(); 
//	} 
} 
