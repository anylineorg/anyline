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


package org.anyline.data.prepare;

import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;

import java.util.List;
import java.util.Map;



public interface Condition extends Cloneable{

	String CONDITION_JOIN_TYPE_AND		= " AND "	;	// 拼接方式 AND
	String CONDITION_JOIN_TYPE_OR		= " OR "	;	// 拼接方式 OR
	// 参数变量类型
	int VARIABLE_PLACEHOLDER_TYPE_INDEX	= 0			;	// 按下标区分
	int VARIABLE_PLACEHOLDER_TYPE_KEY		= 1			;	// 按KEY区分
	int VARIABLE_PLACEHOLDER_TYPE_NONE		= 2			;	// 没有变量
	 
	/** 
	 * 运行时文本 
	 * @param prefix 前缀
	 * @param runtime runtime
	 * @return String
	 */
	String getRunText(String prefix, DataRuntime runtime);

	/**
	 * 静态SQL
	 * @param text TEXT
	 * @return Condition
	 */
	Condition setRunText(String text);
	/** 
	 * 运行时参数值 
	 * @return List
	 */ 
	List<RunValue> getRunValues();
	/** 
	 * 拼接方式 
	 * @return String
	 */ 
	String getJoin(); 
	Condition setJoin(String join); 
	/** 
	 * 当前条件所处容器 
	 * @return ConditionChain
	 */ 
	ConditionChain getContainer(); 
	boolean hasContainer(); 
	boolean isContainer(); 
	/** 
	 * 设置当前条件所处容器 
	 * @param chain  chain
	 * @return Condition
	 */ 
	Condition setContainer(ConditionChain chain); 
	 
	/** 
	 * 初始化 
	 */ 
	void init(); 
	void initRunValue(); 
	boolean isActive();
	boolean isValid();
	void setValid(boolean valid);
	void setActive(boolean active); 
	int getVariableType();
	void setVariableType(int variableType);

	EMPTY_VALUE_SWITCH getSwitch() ;

	void setSwitch(EMPTY_VALUE_SWITCH swt) ;
	/* ************************************************************************************************************ 
	 *  
	 * 													 自动生成 
	 * 
	 * ***********************************************************************************************************/ 
	 
	 
	/* ************************************************************************************************************ 
	 *  
	 * 													 XML定义 
	 * 
	 * ***********************************************************************************************************/ 
	String getId();
	Object clone()throws CloneNotSupportedException; 
	void setValue(String key, Object value);
	void setTest(String test);
	String getTest();
	Map<String,Object> getRunValuesMap();
	List<Variable> getVariables();
	Variable getVariable(String name);

	/**
	 * 是否只是用来给变量赋值的
	 * 用来给java/xml定义SQL中变量赋值,本身并不拼接到最终SQL
	 * @return boolean
	 */
	boolean isVariableSlave();
	void setVariableSlave(boolean bol);
	boolean isSetValue();
	boolean isSetValue(String variable);
} 