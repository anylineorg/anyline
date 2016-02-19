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
 */


/**
 * V2.0
 */
package org.anyline.config.db;

import java.util.List;

public interface SQLVariable extends Cloneable{
	public static final int VAR_TYPE_INDEX			= 0;		//根据下标区分 CD = ?
	public static final int VAR_TYPE_KEY			= 1;		//根据KEY区分  CD = :CD
	public static final int VAR_TYPE_KEY_REPLACE	= 2;		//字符替换 CD=':CD'
	public static final int VAR_TYPE_REPLACE		= 3;		//字符替换 CD=::CD
	
	public void init();
	public int getCompare() ;
	public void setCompare(int compare) ;
	public void addValue(Object value);
	public String getKey() ;
	public void setKey(String key) ;
	
	public List<Object> getValues() ;
	public void setValue(Object value) ;
	public int getType() ;
	public void setType(int type) ;
	public Object clone() throws CloneNotSupportedException;
	
}
