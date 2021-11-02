/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.jdbc.config.db.run; 
 
import java.util.List;

import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.GroupStore;
import org.anyline.jdbc.config.db.Order;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.ConfigStore;

public interface RunSQL { 
	public void setCreater(SQLCreater creater); 
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
	 * @return return
	 */
	public RunSQL setConditionValue(boolean required, boolean strictRequired, String prefix, String variable, Object value, SQL.COMPARE_TYPE compare);
	public RunSQL setConditionValue(boolean required, String prefix, String variable, Object value, SQL.COMPARE_TYPE compare);
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
	 * @param strictRequired 是否严格验证必须
	 * @param	prefix 表名
	 * @param	var 列名
	 * @param	value 值 
	 * @param	compare 比较方式 
	 * @return return
	 */
	public RunSQL addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, COMPARE_TYPE compare);
	public RunSQL addCondition(boolean required,  String prefix, String var, Object value, COMPARE_TYPE compare);
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
	 * @param obj  obj
	 * @return return
	 */ 
	public RunSQL addValues(Object obj);

	public RunSQL addOrders(OrderStore orderStore); 
	public RunSQL addOrder(Order order); 
	public SQL getSql() ; 
	public RunSQL setSql(SQL sql) ;
	public RunSQL setInsertColumns(List<String> keys);
	public List<String> getInsertColumns();
	public RunSQL setUpdateColumns(List<String> keys);
	public List<String> getUpdateColumns();
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
	public String getFetchColumns();
	public void setStrict(boolean strict);
	public boolean isStrict();
	public boolean isValid();
	public StringBuilder getBuilder();
	public void setBuilder(StringBuilder builder);
} 
