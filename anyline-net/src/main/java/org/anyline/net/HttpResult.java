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


package org.anyline.net; 
 
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class HttpResult { 
	private static final Logger log = LoggerFactory.getLogger(HttpResult.class); 
	private String url				;//URL 
	private String backFileCd		; //备份文件CD
	private int status				; 
	private String text				; //文本 
	private String fileType			; //文件类型 
	private String encode			; //编码 
	private String contentType		; // 
	private long lastModified		; //最后修改时间毫秒 
	private String parser			; //解析器CD 
	private String host				;
	private Map<String,String> headers = new HashMap<String,String>();
	private Map<String,HttpCookie> cookies = new HashMap<String,HttpCookie>();
	private InputStream inputStream;
 
	private Map<String,String> seed; 
	 
	public String getContentType() { 
		return contentType; 
	} 
	public void setContentType(String contentType) { 
		this.contentType = contentType; 
		try{ 
			fileType = contentType.split(";")[0]; 
		}catch(Exception e){ 
			fileType = "text/html"; 
			log.error("setContentType$parse content type({})",contentType); 
		} 
		try{ 
			String tmps[] = contentType.split("="); 
			if(tmps.length>1){ 
				encode = tmps[1].trim(); 
			} 
		}catch(Exception e){ 
			encode =null; 
		} 
		 
	} 
	/** 
	 * 根据http文件头信息 解析文件类型 
	 * @param contentType  contentType
	 * @return return
	 */ 
	public static String parseHttpFileExtend(String contentType){ 
		String fileType = null; 
		try{ 
			fileType = contentType.split(";")[0]; 
			fileType = fileType.toLowerCase(); 
			fileType = FileUtil.httpFileExtend.get(FileUtil.httpFileType.indexOf(fileType)); 
		}catch(Exception e){ 
			fileType = ""; 
		} 
		return fileType; 
	} 
	public String getText() { 
		return text; 
	}
	public void setText(String text) { 
		this.text = text; 
	} 
	public String getFileType() { 
		return fileType; 
	} 
	public void setFileType(String fileType) { 
		this.fileType = fileType; 
	} 
	public String getEncode() { 
		return encode; 
	} 
	public void setEncode(String encode) { 
		this.encode = encode; 
	} 
	public String getUrl() { 
		return url; 
	} 
	public void setUrl(String url) { 
		this.url = url; 
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, HttpCookie> getCookies() {
		return cookies;
	}
	public void setCookies(Map<String, HttpCookie> cookies) {
		this.cookies = cookies;
	}
	public HttpCookie getCookie(String key){
		return cookies.get(key);
	}
	public void setCookie(HttpCookie cookie){
		if(null != cookie){
			cookies.put(cookie.getKey(), cookie);
		}
	}
	public String getCookieValue(String key){
		HttpCookie cookie = getCookie(key);
		if(null != cookie){
			return cookie.getValue();
		}
		return null;
	}
	public String getHeader(String key){
		return headers.get(key);
	}

	public static List<String> encodeList = new ArrayList<>();
	 
	public long getLastModified() { 
		return lastModified; 
	} 
	public void setLastModified(long lastModified) { 
		this.lastModified = lastModified; 
	} 
	public String getBackFileCd() { 
		return backFileCd; 
	} 
	public void setBackFileCd(String backFileCd) { 
		this.backFileCd = backFileCd; 
	} 
 
	public Map<String,String> getSeed() { 
		return seed; 
	} 
	public void setSeed(Map<String,String> seed) { 
		this.seed = seed; 
	} 
	public String getParser() { 
		return parser; 
	} 
	public void setParser(String parser) { 
		this.parser = parser; 
	} 
	public String getHost() { 
		return host; 
	} 
	public void setHost(String host) { 
		this.host = host; 
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
}
