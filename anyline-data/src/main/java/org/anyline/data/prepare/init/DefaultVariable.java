

/**
 * V2.0 
 */ 
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
 */

package org.anyline.data.prepare.init;

import org.anyline.data.prepare.Variable;
import org.anyline.entity.Compare;
import org.anyline.util.BeanUtil;

import java.util.*;

public class DefaultVariable implements Variable {
	private static final long serialVersionUID = 6111859581787193807L;
	public static final int VAR_TYPE_INDEX			= 0;		// 根据下标区分 CD = ? 
	public static final int VAR_TYPE_KEY			= 1;		// 根据KEY区分  CD = :CD 
	public static final int VAR_TYPE_KEY_REPLACE	= 2;		// 字符替换 CD=':CD' 
	public static final int VAR_TYPE_REPLACE		= 3;		// 字符替换 CD=::CD 
	 
	public String toString(){
		String str = "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", this.getKey());
		map.put("type", type);
		map.put("sign type", signType);
		map.put("compare", this.getCompare());
		map.put("values", values);
		str = BeanUtil.map2json(map);
		return str;
	} 
 
	private String key;				// 变量KEY
	private String fullKey;			// 完整KEY :CD ::CD {CD} ${CD} #{CD} 8.5之后不用{CD}避免与json冲突
	private List<Object> values;	// 变量值 
	private int type;				// 变量替换方式
	private int signType = 1;		// 变量区分方式
	private String keyPrefix;		// 变量前缀
	private Compare compare;		// 比较方式
	private boolean setValue;		//是否赋值过
	private Compare.EMPTY_VALUE_SWITCH swt;
	 
	public Object clone() throws CloneNotSupportedException{
		DefaultVariable clone = (DefaultVariable) super.clone();
		if(null != values){
			List<Object> cValues = new ArrayList<Object>(); 
			for(Object value:values){
				Object tmp = value; 
				cValues.add(tmp); 
			} 
			clone.values = cValues;
			clone.setValue = this.setValue;
		} 
		return clone; 
	} 
	 
	public void init(){
		if(null != values){
			values.clear(); 
		}
		setValue = false;
	} 
	public Compare getCompare() {
		return compare; 
	} 
	public void setCompare(Compare compare) {
		this.compare = compare; 
	} 
	public void addValue(Object value){
		if(null == values){
			values = new ArrayList<Object>(); 
		} 
		values.add(value);
		setValue = true;
	} 
	public String getKey() {
		return key; 
	} 
	public void setKey(String key) {
		this.key = key; 
	} 
	 
	public List<Object> getValues() {
		return values; 
	} 
	public void setValue(Object value) {
		setValue(true, value);
	}
	@SuppressWarnings({"unchecked","rawtypes" })
	public void setValue(boolean chkNull, Object value) {
		if(null == value && chkNull){
			return;
		}
		if(null == values){
			values = new ArrayList<Object>();
		}else {
			values.clear();
		}
		if(value instanceof Collection){
			values.addAll((Collection)value);
		}else{
			values.add(value);
		}
		setValue = true;
	} 
	public int getType() {
		return type; 
	} 
	public void setType(int type) {
		this.type = type; 
	}

	@Override
	public int getSignType() {
		return this.signType;
	}

	@Override
	public void setSignType(int signType) {
		this.signType = signType;
	}

	public void setValues(List<Object> values) {
		this.values = values;
		setValue = true;
	}

	@Override
	public String getKeyPrefix() {
		return keyPrefix;
	}

	@Override
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	@Override
	public String getFullKey() {
		return fullKey;
	}

	@Override
	public void setFullKey(String fullKey) {
		this.fullKey = fullKey;
		if(null != fullKey) {
			if (null == key) {
				key = fullKey.replace(":","")
						.replace("${","")
						.replace("#{","")
					.replace("{","")
					.replace("}","");
			}
			if (null == keyPrefix) {
				if(fullKey.startsWith("#{")){
					setKeyPrefix("#");
				}else if(fullKey.startsWith("${")){
					setKeyPrefix("$");
				}if(fullKey.startsWith("::")){
					setKeyPrefix("::");
				}if(fullKey.startsWith(":")){
					setKeyPrefix(":");
				}
			}
		}
	}

	@Override
	public Compare.EMPTY_VALUE_SWITCH getSwitch() {
		return swt;
	}

	@Override
	public void setSwitch(Compare.EMPTY_VALUE_SWITCH swt) {
		this.swt = swt;
	}

	@Override
	public boolean isSetValue() {
		return setValue;
	}
}
