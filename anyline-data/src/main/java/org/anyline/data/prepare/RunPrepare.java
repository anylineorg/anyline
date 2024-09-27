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

import org.anyline.data.entity.Join;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.util.BeanUtil;
import org.anyline.util.SQLUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public interface RunPrepare extends Cloneable {

	String PROCEDURE_INPUT_PARAM_TYPE = "INPUT_PARAM_TYPE";			// 存储过程输入参数类型 
	String PROCEDURE_INPUT_PARAM_VALUE = "INPUT_PARAM_VALUE";		// 存储过程输入参数值 

	// 以::标识的执行时直接替换
	// 以:标识的执行时以?占位
	// NAME LIKE :NM + '%' 
	// (NAME = :NM) 
	// NAME = ':NM' 
	// NM IN (:NM)
	// LIMIT :LIMIT OFFSET :OFFSET
    // 占位符前面的标识, 中间可能有空格, 占位符以字母开头(避免CODE='A:1'但避免不了'A:A1'这时应该换成$#), 后面可能是 ' ) %,
	// 注意pg中可能出现 ::int 格式的类型转换需要禁用当前格式的表达式 ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT = false;
	String SQL_VAR_PLACEHOLDER_REGEX_EXT = "(\\S+)\\s*\\(?(\\s*:+[A-Za-z]\\w+)(\\s|'|\\)|%|\\, )?";
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
	String SQL_VAR_PLACEHOLDER_REGEX = "([^\\s\\$#]+)\\s*\\(?(\\s*[\\$|#]*{\\w+})(\\+|\\s|'|\\)|%)?";

	// ${ AND ID = ::ID}  ${AND CODE=:CODE }
	String SQL_VAR_BOX_REGEX = "\\$\\{.+?:.+?\\}";

	// 自定义SQL.id格式 目录名.目录名.文件名:id
	String XML_SQL_ID_STYLE = "(\\.|\\S)*\\S+:\\S+";

	/**
	 * 是否一次性的(执行过程中可修改，否则应该clone一份，避免影响第二闪使用)
	 * @return boolean
	 */
	default boolean disposable(){
		return false;
	}
	RunPrepare disposable(boolean disposable);
	String getId();
	RunPrepare setId(String id);

	Join getJoin();
	RunPrepare setJoin(Join join);
	default RunPrepare setJoin(Join.TYPE type, ConfigStore configs) {
		Join join = new Join();
		join.setType(type);
		join.setConditions(configs);
		return setJoin(join);
	}
	default RunPrepare setJoin(Join.TYPE type, String ... conditions) {
		Join join = new Join();
		join.setType(type);
		join.setConditions(conditions);
		return setJoin(join);
	}
	/** 
	 * 设置查询或操作的目标(表、存储过程、SQL等)
	 * <p> 
	 * 查询全部列 : setDataSource("V_ADMIN")<br> 
	 * 查询指定列 : setDataSource(ADMIN(CD, ACCOUNT, NAME, REG_TIME))<br>
	 * 查询指定列 : setDataSource(ADMIN(DISTINCT CD, ACCOUNT, NAME, REG_TIME))<br>
	 * 查询指定列 : setDataSource(ADMIN(DISTINCT {NEWID()}, {getDate()}, CD, ACCOUNT, NAME, REG_TIME))<br>
	 * {}中内容按原样拼接到运行时SQL, 其他列将添加[]以避免关键重复
	 * </p> 
	 * <p> 
	 * 	根据XML定义SQL : setDataSource("admin.power:S_POWER")<br> 
	 *  admin.power : XML文件路径, 文件目录以.分隔<br>
	 *  S_POWER : 自定义SQL的id 
	 * </p>
	 * @param dest  数据源 : 表|视图|自定义SQL.id
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	RunPrepare setDest(String dest);
	RunPrepare setDest(Table dest);
	String getDest();
	RunPrepare setCatalog(String catalog);
	Catalog getCatalog();
	String getCatalogName();
	RunPrepare setSchema(String schema);
	Schema getSchema();
	String getSchemaName();
	Table getTable();
	String getTableName();
	/**
	 * 用来标记运行环境key(其中关联了数据源与适配器)<br/>
	 * 经常在service方法参数前加数据源前缀缀时会用到
	 * @param runtime runtime.key
	 * @return RunPrepare
	 */
	RunPrepare setRuntime(String runtime);
	String getRuntime();
	/** 
	 * 添加排序条件, 在之前的基础上添加新排序条件, 有重复条件则覆盖
	 * @param order  order
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	RunPrepare order(String order);
	RunPrepare order(String col, Order.TYPE type);
	RunPrepare order(Order order);
 
	/** 
	 * 添加分组条件, 在之前的基础上添加新分组条件, 有重复条件则覆盖
	 * @param groups  groups
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	RunPrepare group(String ... groups);
	RunPrepare having(String having);
 
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
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
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
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	RunPrepare setText(String text);
	 
	/** 
	 * 设置查询条件变量值 
	 * @param condition	 条件ID 
	 * @param variable 变量 
	 * @param value  值 
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	RunPrepare setConditionValue(String condition, String variable, Object value);
	OrderStore getOrders();
	GroupStore getGroups();
	String getHaving();
	void setOrders(OrderStore orders);
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
	LinkedHashMap<String,Column> getColumns();
	List<String> getExcludes();
	RunPrepare setExcludeColumns(List<String> excludeColumn);
	RunPrepare setExcludeColumns(String... columns);
	/**
	 * 添加列
	 * CD
	 * CD, NM
	 * @param columns  columns
	 */
	RunPrepare addColumn(String columns);
	RunPrepare addColumn(Column column);
	RunPrepare excludeColumn(String columns);

	RunPrepare join(Join.TYPE type, String table, String condition);
	RunPrepare inner(String table, String condition);
	RunPrepare left(String table, String condition);
	RunPrepare right(String table, String condition);
	RunPrepare full(String table, String condition);
	RunPrepare setAlias(String alias);
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
	void setBatch(int batch);
	int getBatch();

	ConfigStore condition();
	RunPrepare condition(ConfigStore configs);
	RunPrepare setUnionAll(boolean all);
	boolean isUnionAll();
	RunPrepare union(RunPrepare prepare, boolean all);
	RunPrepare union(RunPrepare prepare);
	default RunPrepare unionAll(RunPrepare prepare) {
		return union(prepare, true);
	}
	RunPrepare union(List<RunPrepare> prepares, boolean all);
	RunPrepare union(List<RunPrepare> prepare);
	default RunPrepare unionAll(List<RunPrepare> prepare) {
		return union(prepare, true);
	}
	List<RunPrepare> getUnions();
	List<RunPrepare> getJoins();
	RunPrepare join(RunPrepare prepare);
	default RunPrepare join(RunPrepare prepare, Join join) {
		prepare.setJoin(join);
		return join(prepare);
	}
	default RunPrepare join(RunPrepare prepare, Join.TYPE type, ConfigStore configs) {
		Join join = new Join();
		join.setType(type);
		join.setConditions(configs);
		return join(prepare, join);
	}
	default RunPrepare join(RunPrepare prepare, Join.TYPE type, String ... conditions) {
		Join join = new Join();
		join.setType(type);
		join.setConditions(conditions);
		return join(prepare, join);
	}
	/**
	 * 过滤不存在的列
	 * @param metadatas 可用范围
	 */
	default void filter(LinkedHashMap<String, Column> metadatas) {
		LinkedHashMap<String, Column> columns = getColumns();
		if(null != columns) {
			List<String> keys = BeanUtil.getMapKeys(columns);
			for(String key:keys) {
				if(SQLUtil.isSingleColumn(key) && !metadatas.containsKey(key.toUpperCase())) {
					columns.remove(key);
				}
			}
		}
	}
	String getDistinct();
	RunPrepare setDistinct(boolean distinct);
	Run build(DataRuntime runtime);
	DataRow map(boolean empty, boolean join);
	default DataRow map(boolean empty) {
		return map(empty, false);
	}
	default DataRow map() {
		return map(false);
	}
	default String json(boolean empty) {
		return map(empty).json();
	}
	default String json() {
		return json(false);
	}
	RunPrepare clone();
}
