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
 
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.run.RunValue;

import java.util.List;
import java.util.Map;
 
 
 
public interface Condition extends Cloneable{
	public static enum EMPTY_VALUE_CROSS{
		 DEFAULT //默认由参数格式决定  如 +ID:id  ++ID:id,默认情况下如果值为空则忽略当前条件
		, BREAK	 //中断执行 整个SQL不执行
		, IGNORE //当前条件不参考最终SQL执行
		, NULL	 //生成 IS NULL
		, SRC	 //原样处理
	}
	public static String CONDITION_JOIN_TYPE_AND		= " AND "	;	// 拼接方式 AND
	public static String CONDITION_JOIN_TYPE_OR			= " OR "	;	// 拼接方式 OR
	// 参数变量类型
	public static final int VARIABLE_FLAG_TYPE_INDEX	= 0			;	// 按下标区分
	public static final int VARIABLE_FLAG_TYPE_KEY		= 1			;	// 按KEY区分
	public static final int VARIABLE_FLAG_TYPE_NONE		= 2			;	// 没有变量
	 
	/** 
	 * 运行时文本 
	 * @param prefix 前缀
	 * @param adapter adapter
	 * @return String
	 */
	public String getRunText(String prefix, JDBCAdapter adapter);

	/**
	 * 静态SQL
	 * @param text TEXT
	 * @return Condition
	 */
	public Condition setRunText(String text);
	/** 
	 * 运行时参数值 
	 * @return List
	 */ 
	public List<RunValue> getRunValues();
	/** 
	 * 拼接方式 
	 * @return String
	 */ 
	public String getJoin(); 
	public Condition setJoin(String join); 
	/** 
	 * 当前条件所处容器 
	 * @return ConditionChain
	 */ 
	public ConditionChain getContainer(); 
	public boolean hasContainer(); 
	public boolean isContainer(); 
	/** 
	 * 设置当前条件所处容器 
	 * @param chain  chain
	 * @return Condition
	 */ 
	public Condition setContainer(ConditionChain chain); 
	 
	/** 
	 * 初始化 
	 */ 
	public void init(); 
	public void initRunValue(); 
	public boolean isActive();
	public boolean isRequired();
	public void setRequired(boolean required);
	public boolean isStrictRequired();
	public void setStrictRequired(boolean strictRequired);
	public boolean isValid();
	public void setActive(boolean active); 
	public int getVariableType();
	public void setVariableType(int variableType);
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
	public String getId();
	public Object clone()throws CloneNotSupportedException; 
	public void setValue(String key, Object value);
	public void setTest(String test);
	public String getTest();
	public Map<String,Object> getRunValuesMap();
	public List<Variable> getVariables();
	public Variable getVariable(String name);

	/**
	 * 是否只是用来给变量赋值的
	 * 用来给java/xml定义SQL中变量赋值,本身并不拼接到最终SQL
	 * @return boolean
	 */
	public boolean isVariableSlave();
	public void setVariableSlave(boolean bol);
	public boolean isSetValue();
	public boolean isSetValue(String variable);
} 
