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

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.*;
import org.anyline.entity.*;

import java.util.List;

public interface Run {
	public void setAdapter(JDBCAdapter adapter);
	public void init();
	 
	/** 
	 * 添加查询条件 
	 * @param required 			是否必须
	 * @param strictRequired 	是否严格验证必须
	 * @param prefix  			查询条件ID或表名
	 * @param variable  		列名|变量key
	 * @param value  			值
	 * @param compare 			比较方式
	 * @return Run
	 */
	public Run setConditionValue(boolean required, boolean strictRequired, String prefix, String variable, Object value, Compare compare);
	public Run setConditionValue(boolean required, String prefix, String variable, Object value, Compare compare);
	public void setGroupStore(GroupStore groups) ;
	public GroupStore getGroupStore() ; 
	public Run group(String group);
 
	public void setOrderStore(OrderStore orders) ; 
	public void setOrders(String[] orders); 
	public OrderStore getOrderStore() ; 
	public Run order(String order);
	 
	public void setConfigStore(ConfigStore configs);
	public ConfigStore getConfigStore() ; 
	 
 
	/** 
	 * 添加查询条件 
	 * @param required 是否必须 
	 * @param strictRequired 是否严格验证必须
	 * @param prefix 表名
	 * @param var 列名
	 * @param value 值 
	 * @param compare 比较方式 
	 * @return Run
	 */
	public Run addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, Compare compare);
	public Run addCondition(boolean required, String prefix, String var, Object value, Compare compare);
	public Run setConditionChain(ConditionChain chain);

	/**
	 * 添加条件
	 * @param conditions 查询条件、ORDER、GROUP、HAVING 等
	 * @return Run
	 */
	public Run addCondition(String ... conditions);
	public Run addCondition(Condition condition);
	public Condition getCondition(String name);

	/**
	 * 根据key查询条件,包括sql主体部分,有可能有多个相同key的条件
	 * @param name name
	 * @return List
	 */
	public List<Condition> getConditions(String name);
	public ConditionChain getConditionChain() ; 

	/** 
	 * 添加参数值
	 * @param compare  compare
	 * @param column  column
	 * @param obj  obj
	 * @param split 遇到集合/数组类型是否拆分处理
	 * @return Run
	 */ 
	public Run addValues(Compare compare, String column, Object obj, boolean split);

	public Run addOrders(OrderStore orderStore);
	public Run addOrder(Order order);
	public RunPrepare getPrepare() ;
	public String getTable();
	public String getCatalog();
	public String getSchema();
	public String getDataSource();
	public Run setPrepare(RunPrepare prepare) ;
	public Run setInsertColumns(List<String> keys);
	public List<String> getInsertColumns();
	public Run setUpdateColumns(List<String> keys);
	public List<String> getUpdateColumns();
	public String getBaseQuery() ; 
	public String getFinalQuery() ; 
	public String getTotalQuery() ;
	public String getFinalExists(); 
	public String getFinalInsert();
	public String getFinalDelete(); 
	public String getFinalUpdate();
	public String getFinalExecute();
	public List<RunValue> getRunValues() ;
	public List<Object> getValues() ;
	public PageNavi getPageNavi() ; 
	public void setPageNavi(PageNavi pageNavi) ;
	public String getQueryColumns();
	public void setStrict(boolean strict);
	public boolean isStrict();
	public boolean isValid();
	public StringBuilder getBuilder();
	public void setBuilder(StringBuilder builder);
	//1-DataRow 2-Entity
	public int getFrom();
	public void setFrom(int from);
	public boolean isSetValue(String condition, String variable);
	public boolean isSetValue(String variable);
	public Variable getVariable(String var);
}
