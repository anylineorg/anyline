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


package org.anyline.util;

public class DESKey {
	private String version;
	private String key;
	private String keyParam;
	private String keyParamName;
	private String keyParamValue;
	
	private String prefix;
	private String prefixParam;
	private String prefixParamName;
	private String prefixParamValue;
	
	public String getKey(String type){
		String result = key;
		if("param".equalsIgnoreCase(type)){
			result = keyParam;
		}else if("name".equalsIgnoreCase(type) || "key".equalsIgnoreCase(type)){
			result = keyParamName;
		}else if("value".equalsIgnoreCase(type)){
			result = keyParamValue;
		}
		return result;
	}
	public String getPrefix(String type){
		String result = prefix;
		if("param".equalsIgnoreCase(type)){
			result = prefixParam;
		}else if("name".equalsIgnoreCase(type) || "key".equalsIgnoreCase(type)){
			result = prefixParamName;
		}else if("value".equalsIgnoreCase(type)){
			result = prefixParamValue;
		}
		return result;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getKeyParam() {
		return keyParam;
	}
	public void setKeyParam(String keyParam) {
		this.keyParam = keyParam;
	}
	public String getKeyParamName() {
		return keyParamName;
	}
	public void setKeyParamName(String keyParamName) {
		this.keyParamName = keyParamName;
	}
	public String getKeyParamValue() {
		return keyParamValue;
	}
	public void setKeyParamValue(String keyParamValue) {
		this.keyParamValue = keyParamValue;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPrefixParam() {
		return prefixParam;
	}
	public void setPrefixParam(String prefixParam) {
		this.prefixParam = prefixParam;
	}
	public String getPrefixParamName() {
		return prefixParamName;
	}
	public void setPrefixParamName(String prefixParamName) {
		this.prefixParamName = prefixParamName;
	}
	public String getPrefixParamValue() {
		return prefixParamValue;
	}
	public void setPrefixParamValue(String prefixParamValue) {
		this.prefixParamValue = prefixParamValue;
	}
}
