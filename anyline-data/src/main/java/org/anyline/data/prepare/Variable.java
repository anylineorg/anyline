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



package org.anyline.data.prepare;

import org.anyline.entity.Compare;

import java.io.Serializable;
import java.util.List;

public interface Variable extends Cloneable, Serializable{
	int VAR_TYPE_INDEX			= 0;		// 根据下标区分 CD = ? 
	int VAR_TYPE_KEY			= 1;		// 根据KEY区分  CD = :CD  CD = #{CD}
	int VAR_TYPE_KEY_REPLACE	= 2;		// 字符替换 CD=':CD'  符合1但需要替换 如在''内
	int VAR_TYPE_REPLACE		= 3;		// 字符替换 CD=::CD   CD = ${CD}

	int KEY_TYPE_SIGN_V1				= 1; // 以:或::区分
	int KEY_TYPE_SIGN_V2				= 2; // 以{}或${}或#{}区分 (8.5之后不要用{}避免与json格式冲突)

	/**
	 * key前缀 : :: $ #
	 * @return String
	 */
	String getKeyPrefix();
	void setKeyPrefix(String prefix);
	int getSignType();
	void setSignType(int signType); 
	void init(); 
	Compare getCompare() ; 
	void setCompare(Compare compare) ; 
	void addValue(Object value);
	//ID
	String getKey() ;
	void setKey(String key) ;

	//完整KEY :CD ::CD {CD} ${CD} #{CD} 8.5之后不用{CD}避免与json冲突
	String getFullKey();
	void setFullKey(String gkey);
	List<Object> getValues() ;
	void setValue(Object value) ;
	boolean isSetValue();
	void setValue(boolean chkNull, Object value) ;
	int getType() ; 
	void setType(int type) ;
	Object clone() throws CloneNotSupportedException;
	Compare.EMPTY_VALUE_SWITCH getSwitch() ;

	void setSwitch(Compare.EMPTY_VALUE_SWITCH swt) ;
	 
} 
