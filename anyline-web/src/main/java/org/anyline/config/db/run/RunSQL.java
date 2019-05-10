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
 *
 *          
 */


package org.anyline.config.db.run;

import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQL.COMPARE_TYPE;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.PageNavi;

public interface RunSQL {
	public void setCreater(SQLCreater creater);
	public void init();
	
	/**
	 * 添加查询条件
	 * @param	condition
	 * 			列名|查询条件ID
	 * @param	variable
	 * 			变量key
	 * @param	value
	 * 			值
	 */
	public RunSQL setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, SQL.COMPARE_TYPE compare);
	public RunSQL setConditionValue(boolean required, String condition, String variable, Object value, SQL.COMPARE_TYPE compare);
	public void setGroupStore(GroupStore groups) ;
	public GroupStore getGroupStore() ;
	public RunSQL group(String group);

	public void setOrderStore(OrderStore orders) ;
	public void setOrders(String[] orders);
	public OrderStore getOrderStore() ;
	public RunSQL order(String order);
	
	public void setConfigStore(ConfigStore configs);
	public ConfigStore getConfigStore() ;
	

	/**
	 * 添加查询条件
	 * @param	required 是否必须
	 * @param	column 列名
	 * @param	value 值
	 * @param	compare 比较方式
	 */
	public RunSQL addCondition(boolean requried, boolean strictRequired, String column, Object value, COMPARE_TYPE compare);
	public RunSQL addCondition(boolean requried, String column, Object value, COMPARE_TYPE compare);
	public RunSQL setConditionChain(ConditionChain chain);
	public RunSQL addConditions(String[] conditions) ;
	public RunSQL addCondition(String condition);
	public RunSQL addCondition(Condition condition) ;
	public Condition getCondition(String name);
	public ConditionChain getConditionChain() ;
	
	/**
	 * 添加静态文本查询条件
	 */
	/**
	 * 添加参数值
	 * @param obj
	 * @return
	 */
	public RunSQL addValues(Object obj);
	public RunSQL addOrders(OrderStore orderStore);
	public RunSQL addOrder(Order order);
	public SQL getSql() ;
	public RunSQL setSql(SQL sql) ;
	public String getBaseQueryTxt() ;
	public String getFinalQueryTxt() ;
	public String getTotalQueryTxt() ;
	public String getExistsTxt();
	public String getInsertTxt();
	public String getDeleteTxt();
	public String getUpdateTxt();
	public String getExecuteTxt();
	public List<Object> getValues() ;
	public PageNavi getPageNavi() ;
	public void setPageNavi(PageNavi pageNavi) ;
	public StringBuilder getBuilder() ;
	public void setBuilder(StringBuilder builder) ;
	public String getFetchColumns();
	public void setStrict(boolean strict);
	public boolean isStrict();
	public boolean isValid();
}
