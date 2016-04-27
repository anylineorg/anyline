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


package org.anyline.config.db;

import java.util.List;



public interface Condition extends Cloneable{
	public static String CONDITION_JOIN_TYPE_AND		= " AND ";	//拼接方式 AND
	public static String CONDITION_JOIN_TYPE_OR			= " OR ";	//拼接方式 OR
	//参数变量类型
	public static final int VARIABLE_FLAG_TYPE_INDEX	= 0;		//按下标区分
	public static final int VARIABLE_FLAG_TYPE_KEY		= 1;		//按KEY区分
	public static final int VARIABLE_FLAG_TYPE_NONE		= 2;		//没有变量
	
	/**
	 * 运行时文本
	 * @return
	 */
	public String getRunText(SQLCreater creater);
	/**
	 * 运行时参数值
	 * @return
	 */
	public List<Object> getRunValues();
	/**
	 * 拼接方式
	 * @return
	 */
	public String getJoin();
	public Condition setJoin(String join);
	/**
	 * 当前条件所处容器
	 * @return
	 */
	public ConditionChain getContainer();
	public boolean hasContainer();
	public boolean isContainer();
	/**
	 * 设置当前条件所处容器
	 * @param chain
	 */
	public Condition setContainer(ConditionChain chain);
	
	/**
	 * 初始化
	 */
	public void init();
	public void initRunValue();
	public boolean isActive();
	public void setActive(boolean active);
	public int getVariableType();
	/*************************************************************************************************************
	 * 
	 * 													 自动生成
	 *
	 ************************************************************************************************************/
	
	
	/*************************************************************************************************************
	 * 
	 * 													 XML定义
	 *
	 ************************************************************************************************************/
	public String getId();
	public Object clone()throws CloneNotSupportedException;
	public void setValue(String key, Object value);
}
