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

import org.anyline.entity.Compare;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.Join;

import java.util.Collection;
import java.util.List;

 
public interface RunPrepare extends Cloneable {

	public static final String PROCEDURE_INPUT_PARAM_TYPE = "INPUT_PARAM_TYPE";			// 存储过程输入参数类型 
	public static final String PROCEDURE_INPUT_PARAM_VALUE = "INPUT_PARAM_VALUE";		// 存储过程输入参数值 

	// 以::标识的执行时直接替换
	// 以:标识的执行时以?占位
	// NAME LIKE :NM + '%' 
	// (NAME = :NM) 
	// NAME = ':NM' 
	// NM IN (:NM)

	public static final String SQL_PARAM_VARIABLE_REGEX = "(\\S+)\\s*\\(?(\\s*:+\\w+)(\\s|'|\\)|%|\\,)?";
	// 与上一种方式　二选一不能同时支持
	//新版本中不要用{key} 避免与json格式冲突
	// 以${}标识的执行时直接替换
	// 以{}或#{}标识的执行时以?占位

	// NM = {NM}
	// NM = ${NM}
	// NM = #{NM}
	// NM LIKE '%${NM}%'
	// NM LIKE '%#{NM}%'
	// NM IN(${NM})
	// NM IN(#{NM})
	public static final String SQL_PARAM_VARIABLE_REGEX_EL = "([^\\s\\$#]+)\\s*\\(?(\\s*[\\$|#]*{\\w+})(\\+|\\s|'|\\)|%)?";


	
	// 自定义SQL.id格式 文件名:id
	public static final String XML_SQL_ID_STYLE = "(\\.|\\S)*\\S+:\\S+";

	public String getId();
	public RunPrepare setId(String id);
	/** 
	 * 设置数据源 
	 * <p> 
	 * 查询全部列 : setDataSource("V_ADMIN")<br> 
	 * 查询指定列 : setDataSource(ADMIN(CD,ACCOUNT,NAME,REG_TIME))<br> 
	 * 查询指定列 : setDataSource(ADMIN(DISTINCT CD,ACCOUNT,NAME,REG_TIME))<br> 
	 * 查询指定列 : setDataSource(ADMIN(DISTINCT {NEWID()},{getDate()},CD,ACCOUNT,NAME,REG_TIME))<br> 
	 * {}中内容按原样拼接到运行时SQL,其他列将添加[]以避免关键重复 
	 * </p> 
	 * <p> 
	 * 	根据XML定义SQL : setDataSource("admin.power:S_POWER")<br> 
	 *  admin.power : XML文件路径,文件目录以.分隔<br> 
	 *  S_POWER : 自定义SQL的id 
	 * </p>
	 * @param ds  数据源 : 表|视图|自定义SQL.id
	 * @return RunPrepare
	 */
	public RunPrepare setDataSource(String ds);
	public String getDataSource();
	public String getSchema();
	public String getTable();
	/** 
	 * 添加排序条件,在之前的基础上添加新排序条件,有重复条件则覆盖 
	 * @param order  order
	 * @return RunPrepare
	 */ 
	public RunPrepare order(String order);
	public RunPrepare order(String col, Order.TYPE type);
	public RunPrepare order(Order order);
 
	/** 
	 * 添加分组条件,在之前的基础上添加新分组条件,有重复条件则覆盖 
	 * @param group  group
	 * @return RunPrepare
	 */ 
	public RunPrepare group(String group);
 
	public void setPageNavi(PageNavi navi); 
	public PageNavi getPageNavi(); 
	/* ******************************************************************************************************* 
	 *  
	 * 											自动生成SQL 
	 *  
	 * *******************************************************************************************************/ 
	/** 
	 * 添加查询条件 
	 * @param column  列名 
	 * @param value   值 
	 * @param compare 比较方式 
	 * @return RunPrepare
	 */
	public RunPrepare addCondition(Compare compare, String column, Object value);
	public RunPrepare addCondition(String column, Object value);
 
	 
	/* ******************************************************************************************************* 
	 *  
	 * 											XML定义SQL 
	 *  
	 * *******************************************************************************************************/
	/**
	 * 设置SQL文本, 从XML中text标签中取出
	 * @param text  text
	 * @return RunPrepare
	 */
	public RunPrepare setText(String text);
	 
	/** 
	 * 设置查询条件变量值 
	 * @param condition	 条件ID 
	 * @param variable 变量 
	 * @param value  值 
	 * @return RunPrepare
	 */ 
	public RunPrepare setConditionValue(String condition, String variable, Object value);
	public OrderStore getOrders();
	public GroupStore getGroups();
	public void setOrders(OrderStore ordres); 
	public int getVersion(); 
	public ConditionChain getConditionChain();
	public RunPrepare addCondition(Condition condition);
	public String getText(); 
	public List<Variable> getSQLVariables();
	
	

	public RunPrepare addPrimaryKey(String ... primaryKeys);
	public RunPrepare addPrimaryKey(Collection<String> primaryKeys);
	public RunPrepare setPrimaryKey(String ... primaryKeys);
	public RunPrepare setPrimaryKey(Collection<String> primaryKeys);
	public List<String> getPrimaryKeys();
	public String getPrimaryKey();
	public boolean hasPrimaryKeys();

	public RunPrepare addFetchKey(String ... fetchKeys);
	public RunPrepare addFetchKey(Collection<String> fetchKeys);
	public RunPrepare setFetchKey(String ... fetchKeys);
	public RunPrepare setFetchKey(Collection<String> fetchKeys);
	public List<String> getFetchKeys();
	public List<String> getColumns();
	public void setStrict(boolean strict);
	public boolean isStrict();
	public boolean isValid();
	public RunPrepare join(Join join);
	public RunPrepare join(Join.TYPE type, String table, String condition);
	public RunPrepare inner(String table, String condition);
	public RunPrepare left(String table, String condition);
	public RunPrepare right(String table, String condition);
	public RunPrepare full(String table, String condition);
	public List<Join> getJoins();
	public void setAlias(String alias);
	public String getAlias();

	/**
	 * 相同ID是否有多个
	 * @return boolean
	 */
	public boolean isMultiple();
	public RunPrepare setMultiple(boolean multiple);
	
} 
