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


package org.anyline.data.run;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.entity.*;

import java.util.List;

public interface Run {
	void setAdapter(DriverAdapter adapter);
	void init();
	 
	/** 
	 * 添加查询条件 
	 * @param swt 				遇到空值处理方式
	 * @param prefix  			查询条件ID或表名
	 * @param variable  		列名|变量key
	 * @param value  			值
	 * @param compare 			比较方式
	 * @return Run
	 */
	Run setConditionValue(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String variable, Object value);
	void setGroupStore(GroupStore groups) ;
	GroupStore getGroupStore() ; 
	Run group(String group);
 
	void setOrderStore(OrderStore orders) ; 
	void setOrders(String[] orders); 
	OrderStore getOrderStore() ; 
	Run order(String order);
	 
	void setConfigStore(ConfigStore configs);
	ConfigStore getConfigStore() ; 
	 
 
	/** 
	 * 添加查询条件 
	 * @param swt 遇到空值处理方式
	 * @param prefix 表名
	 * @param var 列名
	 * @param value 值 
	 * @param compare 比较方式 
	 * @return Run
	 */
	Run addCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value);
	Run setConditionChain(ConditionChain chain);

	/**
	 * 添加条件
	 * @param conditions 查询条件、ORDER、GROUP、HAVING 等
	 * @return Run
	 */
	Run addCondition(String ... conditions);
	Run addCondition(Condition condition);
	Condition getCondition(String name);

	/**
	 * 根据key查询条件,包括sql主体部分,有可能有多个相同key的条件
	 * @param name name
	 * @return List
	 */
	List<Condition> getConditions(String name);
	ConditionChain getConditionChain() ; 

	/** 
	 * 添加参数值
	 * @param compare  compare
	 * @param column  column
	 * @param obj  obj
	 * @param split 遇到集合/数组类型是否拆分处理
	 * @return Run
	 */ 
	Run addValues(Compare compare, String column, Object obj, boolean split);

	Run addOrders(OrderStore orderStore);
	Run addOrder(Order order);
	RunPrepare getPrepare() ;
	String getTable();
	String getCatalog();
	String getSchema();
	String getDataSource();
	Run setPrepare(RunPrepare prepare) ;
	Run setInsertColumns(List<String> keys);
	List<String> getInsertColumns();
	Run setUpdateColumns(List<String> keys);
	List<String> getUpdateColumns();
	String getBaseQuery() ; 
	String getFinalQuery() ; 
	String getTotalQuery() ;
	String getFinalExists(); 
	String getFinalInsert();
	String getFinalDelete(); 
	String getFinalUpdate();
	String getFinalExecute();
	List<RunValue> getRunValues() ;
	List<Object> getValues() ;
	PageNavi getPageNavi() ; 
	void setPageNavi(PageNavi pageNavi) ;
	String getQueryColumns();

	EMPTY_VALUE_SWITCH getStrict();

	void setSwitch(EMPTY_VALUE_SWITCH swt);
	boolean isValid();
	boolean checkValid();
	void setValid(boolean valid);
	StringBuilder getBuilder();
	void setBuilder(StringBuilder builder);
	//1-DataRow 2-Entity
	int getFrom();
	void setFrom(int from);
	boolean isSetValue(String condition, String variable);
	boolean isSetValue(String variable);
	Variable getVariable(String var);
}