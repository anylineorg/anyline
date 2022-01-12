/* 
 * Copyright 2006-2022 www.anyline.org
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
 
public class HttpCookie { 
	private String key; 
	private String value; 
	private String expires; 
	private String path; 
	private String domain;
	public HttpCookie(){
		
	} 
	public HttpCookie(String param){ 
		//endDate=deleted; expires=Thu, 01-Jan-1970 00:00:01 GMT; path=/; domain=.anyline.org 
		if(null == param){ 
			return; 
		} 
		String params[] = param.split(";"); 
		for(String p:params){ 
			String[] kv = p.split("="); 
			if(kv.length < 2){ 
				continue; 
			} 
			String k = kv[0].trim(); 
			String v = kv[1].trim(); 
			if("expires".equalsIgnoreCase(k)){ 
				this.setKey(v); 
			}else if("path".equalsIgnoreCase(k)){ 
				this.setPath(v); 
			}else if("domain".equalsIgnoreCase(k)){ 
				this.setDomain(v); 
			}else{ 
				this.setKey(k); 
				this.setValue(v); 
			} 
		} 
	} 
	public String getKey() { 
		return key; 
	} 
	public void setKey(String key) { 
		this.key = key; 
	} 
	public String getValue() { 
		return value; 
	} 
	public void setValue(String value) { 
		this.value = value; 
	} 
	public String getExpires() { 
		return expires; 
	} 
	public void setExpires(String expires) { 
		this.expires = expires; 
	} 
	public String getPath() { 
		return path; 
	} 
	public void setPath(String path) { 
		this.path = path; 
	} 
	public String getDomain() { 
		return domain; 
	} 
	public void setDomain(String domain) { 
		this.domain = domain; 
	} 
	 
} 
