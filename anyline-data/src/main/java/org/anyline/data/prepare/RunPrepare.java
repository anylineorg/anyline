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

import org.anyline.entity.Compare;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.Join;

import java.util.Collection;
import java.util.List;


public interface RunPrepare extends Cloneable {

	static final String PROCEDURE_INPUT_PARAM_TYPE = "INPUT_PARAM_TYPE";			// 存储过程输入参数类型 
	static final String PROCEDURE_INPUT_PARAM_VALUE = "INPUT_PARAM_VALUE";		// 存储过程输入参数值 

	// 以::标识的执行时直接替换
	// 以:标识的执行时以?占位
	// NAME LIKE :NM + '%' 
	// (NAME = :NM) 
	// NAME = ':NM' 
	// NM IN (:NM)
	// LIMIT :LIMIT OFFSET :OFFSET
    // 占位符前面的标识,中间可能有空格,占位符以字母开头(避免CODE='A:1'但避免不了'A:A1'这时应该换成$#),后面可能是 ' ) % ,
	static final String SQL_PARAM_VARIABLE_REGEX = "(\\S+)\\s*\\(?(\\s*:+[A-Za-z]\\w+)(\\s|'|\\)|%|\\,)?";
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
	static final String SQL_PARAM_VARIABLE_REGEX_EL = "([^\\s\\$#]+)\\s*\\(?(\\s*[\\$|#]*{\\w+})(\\+|\\s|'|\\)|%)?";


	
	// 自定义SQL.id格式 文件名:id
	static final String XML_SQL_ID_STYLE = "(\\.|\\S)*\\S+:\\S+";

	String getId();
	RunPrepare setId(String id);
	/** 
	 * 设置数据源(这里的数据源是指表)
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
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	RunPrepare setDataSource(String ds);
	String getDataSource();
	String getSchema();
	String getTable();

	/**
	 * 用来标记运行环境key(其中关联了数据源与适配器)<br/>
	 * 经常在service方法参数前加数据源前缀缀时会用到
	 * @param runtime runtime.key
	 * @return RunPrepare
	 */
	RunPrepare setRuntime(String runtime);
	String getRuntime();
	/** 
	 * 添加排序条件,在之前的基础上添加新排序条件,有重复条件则覆盖 
	 * @param order  order
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */ 
	RunPrepare order(String order);
	RunPrepare order(String col, Order.TYPE type);
	RunPrepare order(Order order);
 
	/** 
	 * 添加分组条件,在之前的基础上添加新分组条件,有重复条件则覆盖 
	 * @param group  group
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */ 
	RunPrepare group(String group);
 
	void setPageNavi(PageNavi navi); 
	PageNavi getPageNavi(); 
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
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	RunPrepare addCondition(Compare compare, String column, Object value);
	RunPrepare addCondition(String column, Object value);
 
	 
	/* ******************************************************************************************************* 
	 *  
	 * 											XML定义SQL 
	 *  
	 * *******************************************************************************************************/
	/**
	 * 设置SQL文本, 从XML中text标签中取出
	 * @param text  text
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	RunPrepare setText(String text);
	 
	/** 
	 * 设置查询条件变量值 
	 * @param condition	 条件ID 
	 * @param variable 变量 
	 * @param value  值 
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */ 
	RunPrepare setConditionValue(String condition, String variable, Object value);
	OrderStore getOrders();
	GroupStore getGroups();
	void setOrders(OrderStore ordres); 
	int getVersion(); 
	ConditionChain getConditionChain();
	RunPrepare addCondition(Condition condition);
	String getText(); 
	List<Variable> getSQLVariables();
	
	

	RunPrepare addPrimaryKey(String ... primaryKeys);
	RunPrepare addPrimaryKey(Collection<String> primaryKeys);
	RunPrepare setPrimaryKey(String ... primaryKeys);
	RunPrepare setPrimaryKey(Collection<String> primaryKeys);
	List<String> getPrimaryKeys();
	String getPrimaryKey();
	boolean hasPrimaryKeys();

	RunPrepare addFetchKey(String ... fetchKeys);
	RunPrepare addFetchKey(Collection<String> fetchKeys);
	RunPrepare setFetchKey(String ... fetchKeys);
	RunPrepare setFetchKey(Collection<String> fetchKeys);
	List<String> getFetchKeys();
	RunPrepare setQueryColumns(String ... columns);
	RunPrepare setQueryColumns(List<String> columns);
	List<String> getQueryColumns();
	List<String> getExcludeColumns();
	RunPrepare setExcludeColumns(List<String> excludeColumn);
	RunPrepare setExcludeColumns(String... columns);
	/**
	 * 添加列
	 * CD
	 * CD,NM
	 * @param columns  columns
	 */
	RunPrepare addColumn(String columns);
	RunPrepare excludeColumn(String columns);

	RunPrepare join(Join join);
	RunPrepare join(Join.TYPE type, String table, String condition);
	RunPrepare inner(String table, String condition);
	RunPrepare left(String table, String condition);
	RunPrepare right(String table, String condition);
	RunPrepare full(String table, String condition);
	List<Join> getJoins();
	void setAlias(String alias);
	String getAlias();

	/**
	 * 相同ID是否有多个
	 * @return boolean
	 */
	boolean isMultiple();
	RunPrepare setMultiple(boolean multiple);

	/**
	 * 是否严格格式，不能追加未定义的条件
	 * @return boolean
	 */
	boolean isStrict();
	void setStrict(boolean strict);
} 
