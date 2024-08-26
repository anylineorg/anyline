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

import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.util.SQLUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public interface Condition extends Cloneable{
	enum JOIN{
		AND,OR;
		public String getText(){
			return " " + this.name() + " ";
		}
		public String getCode(){
			return this.name();
		}
	}
	// 参数变量类型
	int VARIABLE_PLACEHOLDER_TYPE_INDEX	= 0			;	// 按下标区分
	int VARIABLE_PLACEHOLDER_TYPE_KEY		= 1			;	// 按KEY区分
	int VARIABLE_PLACEHOLDER_TYPE_NONE		= 2			;	// 没有变量

	/**
	 * 顺序 按升序排列
	 * @return double 转认1.0
	 */
	default double index() {
		return 1.0;
	}
	void index(double index);

	static void sort(List<Condition> configs){
		Collections.sort(configs, new Comparator<Condition>() {
			public int compare(Condition r1, Condition r2) {
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
	/** 
	 * 运行时文本 
	 * @param prefix 前缀
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param connector 是否携带开头的连接符号(and or)默认true
	 * @return String
	 */
	default String getRunText(boolean connector, String prefix, DataRuntime runtime, boolean placeholder) {
		String txt = getRunText(prefix, runtime, placeholder).trim();
		if(!connector) {
			txt = SQLUtil.trim(txt);
		}
		return txt;
	}
	String getRunText(int lvl, String prefix, DataRuntime runtime, boolean placeholder);
	default String getRunText(String prefix, DataRuntime runtime, boolean placeholder) {
		return getRunText(0, prefix, runtime, placeholder);
	}

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
	Condition.JOIN getJoin();
	default String getJoinText(){
		Condition.JOIN join = getJoin();
		if(null != join){
			return join.getText();
		}
		return "";
	}
	Condition setJoin(Condition.JOIN join);
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

	EMPTY_VALUE_SWITCH getSwt() ;

	void setSwt(EMPTY_VALUE_SWITCH swt) ;
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
	void setValue(String key, Object value);
	void setTest(String test);
	String getTest();
	Map<String, Object> getRunValuesMap();
	List<Variable> getVariables();
	Variable getVariable(String name);

	/**
	 * 是否只是用来给变量赋值的
	 * 用来给java/xml定义SQL中变量赋值, 本身并不拼接到最终SQL
	 * @return boolean
	 */
	boolean isVariableSlave();
	void setVariableSlave(boolean bol);
	boolean isSetValue();
	boolean isSetValue(String variable);
	void datatype(String type);
	String datatype();
	/**
	 * 是否需要跟前面的条件 隔离，前面所有条件加到()中
	 * @return boolean
	 */
/*
	boolean apart();
	void apart(boolean apart);
*/

	/**
	 * 是否作为一个整体，不可分割，与其他条件合并时以()包围
	 * @return boolean
	 */
	boolean integrality();
	void integrality(boolean integrality);
	Condition clone();
} 
