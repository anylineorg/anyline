/* 
 * Copyright 2006-2022 www.anyline.org
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


package org.anyline.jdbc.run;

import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.prepare.Condition;
import org.anyline.jdbc.prepare.ConditionChain;
import org.anyline.jdbc.prepare.GroupStore;
import org.anyline.jdbc.prepare.RunPrepare;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.prepare.RunPrepare.COMPARE_TYPE;

import java.util.List;

public interface Run {
	public void setCreater(JDBCAdapter adapter);
	public void init();
	public void createRunDeleteTxt();
	public void createRunQueryTxt();
	 
	/** 
	 * 添加查询条件 
	 * @param required 是否必须
	 * @param strictRequired 是否严格验证必须
	 * @param	prefix  查询条件ID或表名
	 * @param	variable  列名|变量key
	 * @param	value  值 
	 * @param compare 比较方式
	 * @return Run
	 */
	public Run setConditionValue(boolean required, boolean strictRequired, String prefix, String variable, Object value, COMPARE_TYPE compare);
	public Run setConditionValue(boolean required, String prefix, String variable, Object value, COMPARE_TYPE compare);
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
	 * @param	required 是否必须 
	 * @param strictRequired 是否严格验证必须
	 * @param	prefix 表名
	 * @param	var 列名
	 * @param	value 值 
	 * @param	compare 比较方式 
	 * @return Run
	 */
	public Run addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, RunPrepare.COMPARE_TYPE compare);
	public Run addCondition(boolean required, String prefix, String var, Object value, RunPrepare.COMPARE_TYPE compare);
	public Run setConditionChain(ConditionChain chain);
	public Run addConditions(String[] conditions) ;
	public Run addCondition(String condition);
	public Run addCondition(Condition condition) ;
	public Condition getCondition(String name); 
	public ConditionChain getConditionChain() ; 
	 
	/** 
	 * 添加静态文本查询条件 
	 */ 
	/** 
	 * 添加参数值 
	 * @param column  column
	 * @param obj  obj
	 * @return Run
	 */ 
	public Run addValues(String column, Object obj);

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
	public String getBaseQueryTxt() ; 
	public String getFinalQueryTxt() ; 
	public String getTotalQueryTxt() ;
	public String getExistsTxt(); 
	public String getInsertTxt();
	public String getDeleteTxt(); 
	public String getUpdateTxt();
	public String getExecuteTxt();
	public List<RunValue> getRunValues() ;
	public List<Object> getValues() ;
	public PageNavi getPageNavi() ; 
	public void setPageNavi(PageNavi pageNavi) ;
	public String getFetchColumns();
	public void setStrict(boolean strict);
	public boolean isStrict();
	public boolean isValid();
	public StringBuilder getBuilder();
	public void setBuilder(StringBuilder builder);
} 
