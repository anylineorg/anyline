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


package org.anyline.data.param;

import org.anyline.entity.Compare;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;

import java.util.List;
import java.util.Map;
 
public interface Config {
	// 从request 取值方式
	public static int FETCH_REQUEST_VALUE_TYPE_NONE 	= 0;	// 没有参数
	public static int FETCH_REQUEST_VALUE_TYPE_SINGLE 	= 1;	// 单值
	public static int FETCH_REQUEST_VALUE_TYPE_MULTIPLE = 2;	// 数组
	public void setValue(Map<String,Object> values); 
	public List<Object> getValues() ; 
	public List<Object> getOrValues() ; 
	public void addValue(Object value);
	public void setValue(Object value); 
 
	/** 
	 *  
	 * @param chain 容器 
	 * @return Condition
	 */ 
	public Condition createAutoCondition(ConditionChain chain);

	public String getPrefix() ; 	// XML condition.id 或表名/表别名
 
	public void setPrefix(String prefix) ;

	public String getVariable() ;//XML condition中的key 或列名

	public void setVariable(String variable) ;

	public String getKey() ;//参数key

	public void setKey(String key) ;


	public Compare getCompare() ; 
	public void setCompare(Compare compare) ; 
	
	public Compare getOrCompare() ; 
	public void setOrCompare(Compare compare) ; 
 
	public boolean isEmpty() ; 
 
	public void setEmpty(boolean empty) ; 
 
	public boolean isRequire() ; 
	public boolean isStrictRequired(); 
	public void setRequire(boolean require) ;
	public void setStrictRequired(boolean require);

	public String getJoin() ; 
 
	public void setJoin(String join) ; 
 
	public boolean isKeyEncrypt() ; 
 
	public boolean isValueEncrypt();
	
	public Object clone();
	public String toString();
	public String cacheKey();

	public void setText(String text);
	public String getText();

	public void setOverCondition(boolean over);
	public void setOverValue(boolean over);
	public boolean isOverCondition();
	public boolean isOverValue();
}
