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


package org.anyline.data.prepare.xml;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;

import java.util.List;
/** 
 * order 需要区分XML定义还是动态添加 
 * @author zh 
 * 
 */ 
public interface XMLPrepare extends RunPrepare{
	public RunPrepare init() ;
 
	/** 
	 * 设置SQL 主体文本 
	 * @param text  text
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */ 
	public RunPrepare setText(String text) ;
	public String getText(); 
 
	/** 
	 * 添加静态文本查询条件 
	 * @param condition condition
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */ 
	public RunPrepare addCondition(String condition) ;
 
	/* *********************************************************************************************************************************** 
	 *  
	 * 														赋值 
	 *  
	 * ***********************************************************************************************************************************/ 
	/** 
	 * 添加查询条件 
	 * @param condition  列名|查询条件ID
	 * @param variable  变量key
	 * @param value  值
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */ 
	public RunPrepare setConditionValue(String condition, String variable, Object value);
	/* *********************************************************************************************************************************** 
	 *  
	 * 														生成SQL 
	 *  
	 * ***********************************************************************************************************************************/ 
	public List<Variable> getSQLVariables();
} 
