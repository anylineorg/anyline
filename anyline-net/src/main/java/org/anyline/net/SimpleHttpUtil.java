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
 *           
 */ 
package org.anyline.net; 
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SimpleHttpUtil { 
	public static String post(String url, String param) { 
		return request(url, "POST", param); 
	} 
 
	public static String get(String url, String param) { 
		return request(url, "GET", param); 
	} 
 
	public static String request(String url, String method, String param) { 
		HttpURLConnection conn = null; 
		InputStream is = null; 
		InputStreamReader isr = null; 
		BufferedReader br = null; 
		try { 
			conn = (HttpURLConnection) new URL(url).openConnection(); 
			conn.setDoOutput(true); 
			conn.setDoInput(true); 
			conn.setUseCaches(false); 
			conn.setRequestMethod(method); 
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded"); 
			if (null != param) { 
				OutputStream os = conn.getOutputStream(); 
				os.write(param.getBytes(StandardCharsets.UTF_8));

				os.close(); 
			} 
			// 从输入流读取返回内容 
			is = conn.getInputStream(); 
			isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			br = new BufferedReader(isr); 
			String str = null; 
			StringBuilder buffer = new StringBuilder();
			while ((str = br.readLine()) != null) { 
				buffer.append(str); 
			} 
			return buffer.toString(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally { 
			try { 
				// 释放资源
				if(null != br) br.close();
				if(null != isr) isr.close();
				if(null != is) is.close();
				if(null != conn) conn.disconnect();
				is = null;
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
		} 
		return null; 
	} 
} 
