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
 *
 *          
 */


/** 
 * V2.0 
 */ 
package org.anyline.data.prepare;

import org.anyline.entity.Compare;

import java.io.Serializable;
import java.util.List;
 
public interface Variable extends Cloneable,Serializable{
	public static final int VAR_TYPE_INDEX			= 0;		// 根据下标区分 CD = ? 
	public static final int VAR_TYPE_KEY			= 1;		// 根据KEY区分  CD = :CD 
	public static final int VAR_TYPE_KEY_REPLACE	= 2;		// 字符替换 CD=':CD' 
	public static final int VAR_TYPE_REPLACE		= 3;		// 字符替换 CD=::CD

	public static final int KEY_TYPE_SIGN_V1				= 1; // 以:或::区分
	public static final int KEY_TYPE_SIGN_V2				= 2; // 以{}或${}或#{}区分 (8.5之后用${})
	
	public int getSignType();
	public void setSignType(int signType); 
	public void init(); 
	public Compare getCompare() ; 
	public void setCompare(Compare compare) ; 
	public void addValue(Object value); 
	public String getKey() ; 
	public void setKey(String key) ; 
	 
	public List<Object> getValues() ;
	public void setValue(Object value) ;
	public void setValue(boolean chkNull, Object value) ; 
	public int getType() ; 
	public void setType(int type) ;
	public void setRequired(boolean required);
	public boolean isRequired();
	public boolean isStrictRequired();
	public void setStrictRequired(boolean strictRequired); 
	public Object clone() throws CloneNotSupportedException; 
	 
} 
