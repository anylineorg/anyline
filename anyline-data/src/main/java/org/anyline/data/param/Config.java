/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.param;

import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.Compare;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.entity.DataRow;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public interface Config extends Cloneable {
	// 从request 取值方式
	static int FETCH_REQUEST_VALUE_TYPE_NONE 	= 0;	// 没有参数
	static int FETCH_REQUEST_VALUE_TYPE_SINGLE 	= 1;	// 单值
	static int FETCH_REQUEST_VALUE_TYPE_MULTIPLE = 2;	// 数组

	void setValue(Map<String,Object> values); 
	List<Object> getValues() ; 
	List<Object> getOrValues() ; 
	void addValue(Object value);
	void setValue(Object value);
	void datatype(String type);
	String datatype();
	Config prepare(RunPrepare prepare);
	RunPrepare prepare();
	/**
	 * 顺序 按升序排列
	 * @return double 转认1.0
	 */
	default double index() {
		return 1.0;
	}
	void index(double index);

	static void sort(List<Config> configs) {
		Collections.sort(configs, new Comparator<Config>() {
			public int compare(Config r1, Config r2) {
				double order1 = r1.index();
				double order2 = r2.index();
				if(order1 > order2) {
					return 1;
				}else if(order1 < order2) {
					return -1;
				}
				return 0;
			}
		});
	}

	void setOrValue(Object value);
	void addOrValue(Object value);
	/** 
	 *  
	 * @param chain 容器 
	 * @return Condition
	 */ 
	Condition createAutoCondition(ConditionChain chain);

	String getPrefix() ; 	// XML condition.id 或表名/表别名
 
	void setPrefix(String prefix) ;

	String getVariable() ;//XML condition中的key 或列名

	void setVariable(String variable) ;

	String getKey() ;//参数key

	void setKey(String key) ;

	Compare getCompare() ; 
	void setCompare(Compare compare) ; 
	
	Compare getOrCompare() ; 
	void setOrCompare(Compare compare) ;

	/**
	 * 是否空条件
	 * @return boolean
	 */
	boolean isEmpty() ; 
 
	void setEmpty(boolean empty) ; 

	Condition.JOIN getJoin() ;

	void setJoin(Condition.JOIN join) ;

	boolean isKeyEncrypt() ; 
 
	boolean isValueEncrypt();
	String cacheKey();

	void setText(String text);
	String getText();

	void setOverCondition(boolean over);
	void setOverValue(boolean over);
	boolean isOverCondition();
	boolean isOverValue();
	void setSwt(EMPTY_VALUE_SWITCH swt);
	EMPTY_VALUE_SWITCH getSwt();

	/**
	 * 是否需要跟前面的条件 隔离，前面所有条件加到()中
	 * @return boolean
	 */
/*	boolean apart();
	void apart(boolean apart);*/
	/**
	 * 是否作为一个整体，不可分割，与其他条件合并时以()包围
	 * @return boolean
	 */
	boolean integrality();
	void integrality(boolean integrality);

	Config clone();
	String toString();

	/**
	 * 属性转map
	 * @param empty 是否保留空值属性
	 * @return DataRow
	 */
	DataRow map(boolean empty);
	default DataRow map() {
		return map(false);
	}
	default String json(boolean empty) {
		return map(empty).json();
	}
	default String json() {
		return json(false);
	}
}
