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

package org.anyline.data.run;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public interface Run extends org.anyline.data.Run{
	Run setRuntime(DataRuntime runtime);
	boolean isEmpty();
	void init();
	DriverAdapter adapter();
	/** 
	 * 添加查询条件 
	 * @param swt 				遇到空值处理方式
	 * @param prefix  			查询条件ID或表名
	 * @param variable  		列名|变量key
	 * @param value  			值
	 * @param compare 			比较方式
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	Run setConditionValue(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String variable, Object value);
	void setGroups(GroupStore groups) ;
	void having(String having);
	ConfigStore having();
	GroupStore getGroups() ;
	Run group(String group);
 
	void setOrders(OrderStore orders) ;
	void setOrders(String ... orders);
	OrderStore getOrders() ;
	Run order(String order);

	void setConfigStore(ConfigStore configs);
	void addConfigStore(ConfigStore configs);
	ConfigStore getConfigs() ;

	/**
	 * 过滤条件是否为空
	 * @return boolean
	 */
	boolean isEmptyCondition();
	 
 
	/** 
	 * 添加查询条件 
	 * @param swt 遇到空值处理方式
	 * @param prefix 表名
	 * @param var 列名
	 * @param value 值 
	 * @param compare 比较方式
	 * @param datatype 数据类型 以StandardTypeMetadata枚举为准不区分大小写
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	Run addCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, String datatype);
	Run setConditionChain(ConditionChain chain);

	/**
	 * 添加条件
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	Run addCondition(String ... conditions);
	Run addCondition(Condition condition);
	Condition getCondition(String name);

	/**
	 * 根据key查询条件, 包括sql主体部分, 有可能有多个相同key的条件
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
	 * @return List 最终执行命令 如JDBC环境中的 SQL 与 参数值 ,如果是集合有可能 会返回多个
	 */
	List<RunValue> addValues(Compare compare, Column column, Object obj, boolean split);

	/**
	 * 设置行数
	 * @param rows 行
	 * @return this
	 */
	Run setRows(long rows);

	/**
	 * 获取行数
	 * @return 未设置行数的返回-1
	 */
	long getRows();
	Run addValue(RunValue value);
	Run add(OrderStore orderStore);
	Run add(Order order);
	RunPrepare getPrepare() ;
	Table getTable();
	Catalog getCatalog();
	Schema getSchema();
	String getTableName();
	String getCatalogName();
	String getSchemaName();
	String getDest();
	Run setPrepare(RunPrepare prepare) ;
	Run setInsertColumns(List<String> keys);
	Run setInsertColumns(LinkedHashMap<String, Column> columns);
	List<String> getInsertColumns();
	LinkedHashMap<String, Column> getInsertColumns(boolean metadata);
	Run setUpdateColumns(List<String> keys);
	Run setUpdateColumns(LinkedHashMap<String, Column> columns);
	List<String> getUpdateColumns();
	LinkedHashMap<String, Column> getUpdateColumns(boolean metadata);
	String getBaseQuery(Boolean placeholder) ;
	default String getBaseQuery() {
		return getBaseQuery(true);
	}
	String getFinalQuery(Boolean placeholder);
	default String getFinalQuery() {
		return getFinalQuery(true);
	}

	String getTotalQuery(Boolean placeholder) ;
	default String getTotalQuery() {
		return getTotalQuery(true);
	}
	String getFinalExists(Boolean placeholder);
	default String getFinalExists() {
		return getFinalExists(true);
	}
	String getFinalInsert(Boolean placeholder);
	default String getFinalInsert() {
		return getFinalInsert(true);
	}
	String getFinalDelete(Boolean placeholder);
	default String getFinalDelete() {
		return getFinalDelete(true);
	}
	String getFinalUpdate(Boolean placeholder);
	default String getFinalUpdate() {
		return getFinalUpdate(true);
	}

    Run addVariable(Variable var);

	Run addVariable(List<Variable> vars);

    Run addVariableBlock(VariableBlock block);

    String getFinalExecute(Boolean placeholder);
	default String getFinalExecute() {
		return getFinalExecute(true);
	}

	/**
	 * SQL是否支持换行
	 * @return boolean
	 */
	default boolean supportBr() {
		return true;
	}
	void supportBr(boolean support);

	List<RunValue> getRunValues() ;
	List<Object> getValues() ;
	PageNavi getPageNavi() ; 
	void setPageNavi(PageNavi pageNavi) ;
	String getQueryColumn();

	EMPTY_VALUE_SWITCH getStrict();

	void setSwt(EMPTY_VALUE_SWITCH swt);
	boolean isValid();
	boolean checkValid();
	void setValid(boolean valid);
	StringBuilder getBuilder();
	void setBuilder(StringBuilder builder);
	//1-DataRow 2-Entity
	int getOriginType();
	void setOriginType(int from);
	boolean isSetValue(String condition, String variable);
	boolean isSetValue(String variable);
	Variable getVariable(String var);
	List<Variable> getVariables();

	Run setQueryColumns(String ... columns);
	Run setQueryColumns(List<String> columns);
	List<String> getQueryColumns();

	List<String> getExcludeColumns();
	Run setExcludeColumns(List<String> excludeColumn);
	Run setExcludeColumns(String... columns);
	Object setFrom(Object from);
	Object getFrom();

	void setValue(Object value);
	void setValues(String key, Collection<Object> values);
	void setValues(List<RunValue> values);
	void addValues(String key, Collection<Object> values);
	Object getValue();
	void setBatch(int batch);
	int getBatch();
	void setVol(int vol);
	int getVol();
	ACTION action();
	void action(ACTION action);

	String format(String cmd);

	String log(ACTION.DML action, Boolean placeholder);

	Run setUnionAll(boolean all);
	boolean isUnionAll();
	Run union(Run run, boolean all);
	Run union(Run run);
	default Run unionAll(Run run) {
		return union(run, true);
	}
	Run union(List<Run> runs, boolean all);
	Run union(List<Run> run);
	default Run unionAll(List<Run> run) {
		return union(run, true);
	}
	List<Run> getUnions();

}
