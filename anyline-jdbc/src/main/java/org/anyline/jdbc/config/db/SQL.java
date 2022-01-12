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


package org.anyline.jdbc.config.db; 
 
import java.util.Collection;
import java.util.List;

import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.sql.auto.impl.Join;

/** 
 * V3.0 
 */ 
 
 
 
public interface SQL extends Cloneable { 

	public static enum COMPARE_TYPE{
		EQUAL			{public int getCode(){return 10;} public String getSql(){return " = ?";} 				public String getName(){return "等于";}},
		GREAT			{public int getCode(){return 20;} public String getSql(){return " > ?";} 				public String getName(){return "大于";}},
		GREAT_EQUAL		{public int getCode(){return 21;} public String getSql(){return " >= ?";} 				public String getName(){return "大于等于";}},
		LESS			{public int getCode(){return 30;} public String getSql(){return " < ?";} 				public String getName(){return "小于";}},
		LESS_EQUAL		{public int getCode(){return 31;} public String getSql(){return " <= ?";} 				public String getName(){return "小于等于";}},
		IN				{public int getCode(){return 40;} public String getSql(){return " IN ";} 				public String getName(){return "in";}},
		LIKE			{public int getCode(){return 50;} public String getSql(){return " LIKE ";} 				public String getName(){return "%like%";}},
		LIKE_PREFIX		{public int getCode(){return 51;} public String getSql(){return " LIKE ";} 				public String getName(){return "%like";}},
		LIKE_SUBFIX		{public int getCode(){return 52;} public String getSql(){return " LIKE ";} 				public String getName(){return "like%";}},
		BETWEEN			{public int getCode(){return 80;} public String getSql(){return " BETWEEN ? AND ? ";} 	public String getName(){return "区间";}},
		NOT_EQUAL		{public int getCode(){return 110;} public String getSql(){return " != ?";} 				public String getName(){return "不等于";}},
		NOT_IN			{public int getCode(){return 140;} public String getSql(){return " NOT IN ";} 			public String getName(){return "不包含";}};
		public abstract String getName();
		public abstract String getSql();
		public abstract int getCode();
	}
	public static enum ORDER_TYPE{
		ASC				{public String getCode(){return "ASC";} 	public String getName(){return "正序";}},
		DESC			{public String getCode(){return "DESC";} 	public String getName(){return "倒序";}};
		public abstract String getName();
		public abstract String getCode();
	} 
	//public static int COMPARE_TYPE_EQUAL 			= 10;	// == 
//	public static int COMPARE_TYPE_GREAT 			= 20;	// > 
//	public static int COMPARE_TYPE_GREAT_EQUAL		= 21;	// >= 
//	public static int COMPARE_TYPE_LITTLE 			= 30;	// < 
//	public static int COMPARE_TYPE_LITTLE_EQUAL		= 31;	// <= 
//	public static int COMPARE_TYPE_IN				= 40;	// IN 
//	public static int COMPARE_TYPE_LIKE				= 50;	// LIKE '%张%' 
//	public static int COMPARE_TYPE_LIKE_PREFIX		= 51;	// LIKE '张%' 
//	public static int COMPARE_TYPE_LIKE_SUBFIX		= 52;	// LIKE '%张' 
//	public static int COMPARE_TYPE_NOT_EQUAL		= 61;	// <> 
//	public static int COMPARE_TYPE_NOT_IN			= 62;	// NOT IN 
//	 
	public static final String PROCEDURE_INPUT_PARAM_TYPE = "INPUT_PARAM_TYPE";			//存储过程输入参数类型 
	public static final String PROCEDURE_INPUT_PARAM_VALUE = "INPUT_PARAM_VALUE";		//存储过程输入参数值 

	//以:标识的执行时直接替换
	//以::标识的执行时以?占位 
	//NAME LIKE :NM + '%' 
	//(NAME = :NM) 
	//NAME = ':NM' 
	//NM IN (:NM)
	public static final String SQL_PARAM_VAIRABLE_REGEX = "(\\S+)\\s*\\(?(\\s*:+\\w+)(\\s|'|\\)|%|\\,)?";
	//与上一种方式　二选一不能同时支持
	//以${}标识的执行时直接替换
	//以{}标识的执行时以?占位
	//NM = {NM}
	//NM = ${NM}
	//NM LIKE '%{NM}%'    NM LIKE '%${NM}%'
	//NM IN({NM})
	public static final String SQL_PARAM_VAIRABLE_REGEX_EL = "([^\\s$]+)\\s*\\(?(\\s*\\$*{\\w+})(\\+|\\s|'|\\)|%)?";
	
	//自定义SQL.id格式 文件名:id
	public static final String XML_SQL_ID_STYLE = "(\\.|\\S)*\\S+:\\S+"; 
 
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
	 * @param	ds  数据源 : 表|视图|自定义SQL.id
	 * @return return
	 */
	public SQL setDataSource(String ds);
	public String getDataSource();
	public String getSchema();
	public String getTable();
	/** 
	 * 添加排序条件,在之前的基础上添加新排序条件,有重复条件则覆盖 
	 * @param order  order
	 * @return return
	 */ 
	public SQL order(String order); 
	public SQL order(String col, SQL.ORDER_TYPE type); 
	public SQL order(Order order); 
 
	/** 
	 * 添加分组条件,在之前的基础上添加新分组条件,有重复条件则覆盖 
	 * @param group  group
	 * @return return
	 */ 
	public SQL group(String group); 
 
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
	 * @return return
	 */ 
	public SQL addCondition(String column, Object value, int compare); 
 
	 
	/* ******************************************************************************************************* 
	 *  
	 * 											XML定义SQL 
	 *  
	 * *******************************************************************************************************/
	/**
	 * 设置SQL文本, 从XML中text标签中取出
	 * @param text  text
	 * @return return
	 */
	public SQL setText(String text);
	 
	/** 
	 * 设置查询条件变量值 
	 * @param condition	 条件ID 
	 * @param variable 变量 
	 * @param value  值 
	 * @return return
	 */ 
	public SQL setConditionValue(String condition, String variable, Object value); 
	public OrderStore getOrders();
	public GroupStore getGroups(); 
	public void setOrders(OrderStore ordres); 
	public int getVersion(); 
	public ConditionChain getConditionChain(); 
	public SQL addCondition(Condition condition); 
	public String getText(); 
	public List<SQLVariable> getSQLVariables();
	
	

	public SQL addPrimaryKey(String ... primaryKeys);
	public SQL addPrimaryKey(Collection<String> primaryKeys);
	public SQL setPrimaryKey(String ... primaryKeys);
	public SQL setPrimaryKey(Collection<String> primaryKeys);
	public List<String> getPrimaryKeys();
	public String getPrimaryKey();
	public boolean hasPrimaryKeys();

	public SQL addFetchKey(String ... fetchKeys);
	public SQL addFetchKey(Collection<String> fetchKeys);
	public SQL setFetchKey(String ... fetchKeys);
	public SQL setFetchKey(Collection<String> fetchKeys);
	public List<String> getFetchKeys();
	public List<String> getColumns();
	public void setStrict(boolean strict);
	public boolean isStrict();
	public boolean isValid();
	public SQL join(Join join);
	public SQL join(Join.TYPE type, String table, String condition);
	public SQL inner(String table, String condition);
	public SQL left(String table, String condition);
	public SQL right(String table, String condition);
	public SQL full(String table, String condition);
	public List<Join> getJoins();
	
} 
