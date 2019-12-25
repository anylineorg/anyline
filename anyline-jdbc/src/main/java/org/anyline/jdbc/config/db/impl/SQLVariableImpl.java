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


/** 
 * V2.0 
 */ 
package org.anyline.jdbc.config.db.impl; 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.jdbc.config.db.SQLVariable;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
 
public class SQLVariableImpl implements SQLVariable{ 
	private static final long serialVersionUID = 6111859581787193807L;
	public static final int VAR_TYPE_INDEX			= 0;		//根据下标区分 CD = ? 
	public static final int VAR_TYPE_KEY			= 1;		//根据KEY区分  CD = :CD 
	public static final int VAR_TYPE_KEY_REPLACE	= 2;		//字符替换 CD=':CD' 
	public static final int VAR_TYPE_REPLACE		= 3;		//字符替换 CD=::CD 
	 
	public String toString(){
		String str = "";
		str = "key:"+this.getKey() 
				+ ", type:"+type 
				+ ", sign type:"+signType
				+ ", compare:"+compare.getCode()
				+ ", required:"+required
				+ ", strict required:"+strictRequired
				+ ", values:";
		if(null != values){
			for(Object value:values){
				str += " " + value;
			}
		}
		return str;
	} 
 
	private String key;				//变量KEY 
	private List<Object> values;	//变量值 
	private int type;				//变量替换方式
	private int signType = 1;		//变量区分方式
	private COMPARE_TYPE compare;	//比较方式
	private boolean required;
	private boolean strictRequired;
	 
	public Object clone() throws CloneNotSupportedException{ 
		SQLVariableImpl clone = (SQLVariableImpl) super.clone(); 
		if(null != values){ 
			List<Object> cValues = new ArrayList<Object>(); 
			for(Object value:values){ 
				Object tmp = value; 
				cValues.add(tmp); 
			} 
			clone.values = cValues; 
		} 
		return clone; 
	} 
	 
	public void init(){ 
		if(null != values){ 
			values.clear(); 
		} 
	} 
	public COMPARE_TYPE getCompare() { 
		return compare; 
	} 
	public void setCompare(COMPARE_TYPE compare) { 
		this.compare = compare; 
	} 
	public void addValue(Object value){ 
		if(null == values){ 
			values = new ArrayList<Object>(); 
		} 
		values.add(value); 
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
	@SuppressWarnings("unchecked")
	public void setValue(boolean chkNull, Object value) {
		if(null == value && chkNull){
			return;
		}
		if(null == values){
			values = new ArrayList<Object>();
		}
		if(value instanceof Collection){
			values.addAll((Collection)value);
		}else{
			values.add(value);
		}
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

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isStrictRequired() {
		return strictRequired;
	}

	public void setStrictRequired(boolean strictRequired) {
		this.strictRequired = strictRequired;
	} 
	 
} 
