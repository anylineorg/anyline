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


package org.anyline.data.adapter;

import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.dao.AnylineDao;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.metadata.type.DatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface DriverAdapter {

	// 内置VALUE
	 enum SQL_BUILD_IN_VALUE{
		CURRENT_TIME("CURRENT_TIME","当前时间");
		private final String code;
		private final String name;
		SQL_BUILD_IN_VALUE(String code, String name){
			this.code = code;
			this.name = name;
		}
		String getCode(){
			return code;
		}
		String getName(){
			return name;
		}
	}
	DatabaseType type();
	String version();


	String TAB 		= "\t"		;
	String BR 		= "\n"		;
	String BR_TAB 	= "\n\t"	;
		//////////////////////////////////////////////////////////////////////////



	DataSet select(DataRuntime runtime, String random, boolean system, String table, Run run);
	long total(DataRuntime runtime, String random, Run run);
	List<Map<String,Object>> maps(DataRuntime runtime, String random, Run run);
	Map<String,Object> map(DataRuntime runtime, String random, Run run);
	int update(DataRuntime runtime, String random, String dest, Object data, Run run);
	int execute(DataRuntime runtime, String random, Run run);
	boolean execute(DataRuntime runtime, String random, Procedure procedure);
	DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi);




	//根据驱动内置接口补充 再根据metadata解析 SELECT * FROM T WHERE 1=0 SqlRowSet set = runtime.getTemplate().queryForRowSet(run.getFinalQuery());
	<T extends Column> LinkedHashMap<String, T> columns(boolean create, DataRuntime runtime, Table table, LinkedHashMap<String, T> columns);

	///////////////////////////////////////////////////////////////////////

	AnylineDao getDao();
	void setDao(AnylineDao dao);


	/**
	 * 界定符(分隔符)
	 * @return String
	 */
	String getDelimiterFr();
	String getDelimiterTo();

	/**
	 * 对应的兼容模式，有些数据库会兼容oracle或pg,需要分别提供两个JDBCAdapter或者直接依赖oracle/pg的adapter
	 * 参考SQLAdapterUtil定位adapter的方法
	 * @return DatabaseType
	 */
	DatabaseType compatible();

	/**
	 * 转换成相应数据库支持类型
	 * @param type type
	 * @return ColumnType
	 */
	ColumnType type(String type);

	/**
	 * 根据java数据类型定位数据库数据类型
	 * 在不开启自动检测数据类型时会调用
	 * @param support class或ColumnType
	 * @return DataWriter
	 */
	DataWriter writer(Object support);
	//根据Java类型
	DataReader reader(Class clazz);
	//根据数据库数据类型
	DataReader reader(ColumnType type);

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * UPDATE			: 更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 * COMMON			：其他通用
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													INSERT
	 ******************************************************************************************************************/

	/**
	 * 创建insert RunPrepare
	 * @param runtime runtime
	 * @param dest 表
	 * @param obj 实体
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run
	 */
	Run buildInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns);

	/**
	 * 根据Collection创建批量插入SQL
	 * @param runtime runtime
	 * @param run run
	 * @param dest 表 如果不指定则根据DataSet解析
	 * @param list 数据集
	 * @param keys keys 南非要插入的列
	 */
	void createInserts(DataRuntime runtime, Run run, String dest, Collection list, List<String> keys);

	/**
	 * 根据DataSet创建批量插入SQL
	 * @param runtime runtime
	 * @param run run
	 * @param dest 表 如果不指定则根据DataSet解析
	 * @param set 数据集
	 * @param keys keys 南非要插入的列
	 */
	void createInserts(DataRuntime runtime, Run run, String dest, DataSet set, List<String> keys);

	/**
	 * 确认需要插入的列
	 * @param data  Entity或DataRow
	 * @param batch  是否批量
	 * @param columns 提供额外的判断依据<br/>
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列
	 * @return List
	 */
	List<String> confirmInsertColumns(String dest, Object data, List<String> columns, boolean batch);

	/**
	 * 批量插入数据时,多行数据之间分隔符
	 * @return String
	 */
	String batchInsertSeparator ();
	/**
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	boolean supportInsertPlaceholder ();

	/**
	 * 执行 insert
	 * @param runtime runtime
	 * @param random random
	 * @param data data
	 * @param sql sql
	 * @param values value
	 * @param pks pks
	 * @return int
	 * @throws Exception 异常
	 */
	int insert(DataRuntime runtime, String random, Object data, String sql, List<Object> values, String[] pks) throws Exception;

	String generatedKey();
	/* *****************************************************************************************************************
	 * 													UPDATE
	 ******************************************************************************************************************/

	/**
	 * 创建更新SQL
	 * @param dest 表
	 * @param obj Entity或DtaRow
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要更新的列
	 * @param configs 更新条件
	 * @return Run
	 */
	Run buildUpdateRun(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns);



	/* *****************************************************************************************************************
	 * 													QUERY
	 ******************************************************************************************************************/

	/**
	 * 创建查询SQL
	 * @param prepare  prepare
	 * @param configs 查询条件配置
	 * @param conditions 查询条件
	 * @return Run
	 */
	Run buildQueryRun(RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * 创建查询序列SQL
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names names
	 * @return String
	 */
	List<Run> buildQuerySequence(boolean next, String ... names);

	/**
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run run
	 */
	void buildQueryRunContent(Run run);


	/**
	 * 创建最终执行查询SQL 包含分页 ORDER
	 * @param run  run
	 * @return String
	 */
	String parseFinalQuery(Run run);


	/**
	 * 构造 LIKE 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param builder builder
	 * @param compare compare
	 * @param value value
	 * @return value 有占位符时返回 占位值，没有占位符返回null
	 */
	Object buildConditionLike(StringBuilder builder, Compare compare, Object value);

	/**
	 * 构造 FIND_IN_SET 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param builder builder
	 * @param column column
	 * @param compare compare
	 * @param value value
	 * @return value
	 */
	Object buildConditionFindInSet(StringBuilder builder, String column, Compare compare, Object value);
	/**
	 * 构造(NOT) IN 查询条件
	 * @param builder builder
	 * @param compare compare
	 * @param value value
	 * @return builder
	 */
	StringBuilder buildConditionIn(StringBuilder builder, Compare compare, Object value);

	/**
	 * JDBC执行完成后的结果处理
	 * @param list JDBC执行结果
	 * @return  DataSet
	 */
	List<Map<String,Object>> process(List<Map<String,Object>> list);


	/* *****************************************************************************************************************
	 * 													COUNT
	 ******************************************************************************************************************/

	/**
	 * 创建统计总数SQL
	 * @param run  Run
	 * @return String
	 */
	String parseTotalQuery(Run run);


	/* *****************************************************************************************************************
	 * 													EXISTS
	 ******************************************************************************************************************/

	/**
	 * 创建检测是否存在SQL
	 * @param run run
	 * @return String
	 */
	String parseExists(Run run);


	/* *****************************************************************************************************************
	 * 													EXECUTE
	 ******************************************************************************************************************/

	/**
	 * 创建执行SQL
	 * @param prepare prepare
	 * @param configs configs
	 * @param conditions conditions
	 * @return Run
	 */
	Run buildExecuteRun(RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * 构造执行主体
	 * @param run run
	 */
	void buildExecuteRunContent(Run run);

	/* *****************************************************************************************************************
	 * 													DELETE
	 ******************************************************************************************************************/

	/**
	 * 创建删除SQL
	 * @param dest 表
	 * @param obj entity
	 * @param columns 删除条件的列，根据columns取obj值并合成删除条件
	 * @return Run
	 */
	Run buildDeleteRun(String dest, Object obj, String ... columns);
	/**
	 * 根据key values删除
	 * @param table 表
	 * @param key key
	 * @param values values
	 * @return Run
	 */
	Run buildDeleteRun(String table, String key, Object values);

	/**
	 * 构造删除主体
	 * @param run run
	 * @return Run
	 */
	Run buildDeleteRunContent(Run run);

	List<Run> buildTruncateSQL(String table);



	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/

	void checkSchema(DataSource dataSource, Table table);
	void checkSchema(Connection con, Table table);
	void checkSchema(DataRuntime runtime, Table table);
	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/

	/**
	 * 查询所有数据库
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryDatabaseRunSQL() throws Exception;

	/**
	 *  根据查询结果集构造 Database
	 * @param index 第几条SQL 对照 buildQueryDatabaseRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set set
	 * @return databases
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Database> databases(int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception;

	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/

	/**
	 * 查询表,不是查表中的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	List<Run> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 * 查询表备注
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	List<Run> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照buildQueryTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;

	/**
	 * 根据驱动内置方法补充
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param runtime runtime
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception;


	/**
	 * 表备注
	 * @param index 第几条SQL 对照buildQueryTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> comments(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;

	/**
	 * 查询表DDL
	 * @param table 表
	 * @return List
	 */
	List<Run> buildQueryDDLRunSQL(Table table) throws Exception;

	/**
	 * 查询表DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param table 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(int index, Table table, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/

	/**
	 * 查询视图
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	List<Run> buildQueryViewRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 *  根据查询结果集构造View
	 * @param index 第几条SQL 对照buildQueryViewRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param views 上一步查询结果
	 * @param set set
	 * @return views
	 * @throws Exception 异常
	 */
	<T extends View> LinkedHashMap<String, T> views(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set) throws Exception;

	/**
	 * 根据JDBC补充
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param runtime runtime
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return views
	 * @throws Exception 异常
	 */
	<T extends View> LinkedHashMap<String, T> views(boolean create, LinkedHashMap<String, T> views, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception;


	/**
	 * 查询viewDDL
	 * @param view view
	 * @return List
	 */
	List<Run> buildQueryDDLRunSQL(View view) throws Exception;
	/**
	 * 查询 view DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param view view
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(int index, View view, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	/**
	 * 查询主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	List<Run> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param runtime runtime
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception;


	/**
	 * 查询 MasterTable DDL
	 * @param table MasterTable
	 * @return List
	 */
	List<Run> buildQueryDDLRunSQL(MasterTable table) throws Exception;
	/**
	 * 查询 MasterTable DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(int index, MasterTable table, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/

	/**
	 * 查询分区表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	List<Run> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 * 根据主表查询分区表
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @param name 分区表名
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name) throws Exception;
	List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags) throws Exception;

	/**
	 *  根据查询结果集构造Table
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param runtime runtime
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, MasterTable master) throws Exception;



	/**
	 * 查询 PartitionTable DDL
	 * @param table PartitionTable
	 * @return List
	 */
	List<Run> buildQueryDDLRunSQL(PartitionTable table) throws Exception;
	/**
	 * 查询 MasterTable DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(int index, PartitionTable table, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return sqls
	 */
	List<Run> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T>
	columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;

	/**
	 * 解析JDBC get columns结果
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param runtime runtime
	 * @return columns 上一步查询结果
	 * @return attern attern
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, DataRuntime runtime, Table table, String pattern) throws Exception;

	Column column(Column column, ResultSetMetaData rsm, int index);
	Column column(Column column, ResultSet rs);


	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	List<Run> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception;


	/**
	 *
	 * 解析JDBC get columns结果
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param runtime runtime
	 * @param pattern pattern
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DataRuntime runtime, Table table, String pattern) throws Exception;


	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	List<Run> buildQueryPrimaryRunSQL(Table table) throws Exception;

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	PrimaryKey primary(int index, Table table, DataSet set) throws Exception;



	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRunSQL(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的外键
	 * @param table 表
	 * @return sqls
	 */
	List<Run> buildQueryForeignsRunSQL(Table table) throws Exception;

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRunSQL 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;


	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/

	/**
	 * 查询表上的索引
	 * @param table 表
	 * @param name 名称
	 * @return sqls
	 */
	List<Run> buildQueryIndexRunSQL(Table table, String name);

	/**
	 *  根据查询结果集构造Index
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	<T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception;


	/**
	 * 解析JDBC getIndex结果
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param runtime runtime
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	<T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DataRuntime runtime, Table table, boolean unique, boolean approximate) throws Exception;


	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/

	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	List<Run> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Constraint> LinkedHashMap<String, T> constraints(int index, boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set) throws Exception;
	<T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception;




	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	/**
	 * 查询表上的trigger
	 * @param table 表
	 * @param events INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	List<Run> buildQueryTriggerRunSQL(Table table, List<Trigger.EVENT> events) ;

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Trigger> LinkedHashMap<String, T> triggers(int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception;



	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/

	List<Run> buildQueryProcedureRunSQL(String catalog, String schema, String name) ;

	<T extends Procedure> LinkedHashMap<String, T> procedures(int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;


	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	List<Run> buildQueryFunctionRunSQL(String catalog, String schema, String name) ;

	<T extends Function> LinkedHashMap<String, T> functions(int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;

	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/



	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/

	/**
	 * 创建表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRunSQL(Table table) throws Exception;

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAddCommentRunSQL(Table table) throws Exception;

	/**
	 * 修改表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRunSQL(Table table) throws Exception;
	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Table table, Collection<Column> columns) throws Exception;

	/**
	 * 重命名
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRunSQL(Table table) throws Exception;

	/**
	 * 修改备注
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRunSQL(Table table) throws Exception;

	/**
	 * 删除表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRunSQL(Table table) throws Exception;

	/**
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTableExists(StringBuilder builder, boolean exists);


	/**
	 * 创建主键在创建表的DDL结尾部分
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	StringBuilder primary(StringBuilder builder, Table table);

	/**
	 * 单独创建主键
	 * @return String
	 */
	//String primary(Table table);

	/**
	 * 表备注
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	StringBuilder comment(StringBuilder builder, Table table);

	/**
	 * 构造表名
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	StringBuilder name(StringBuilder builder, Table table);



	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/

	/**
	 * 创建视图
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRunSQL(View view) throws Exception;

	/**
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAddCommentRunSQL(View view) throws Exception;

	/**
	 * 修改视图
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRunSQL(View view) throws Exception;

	/**
	 * 重命名
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRunSQL(View view) throws Exception;

	/**
	 * 修改备注
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRunSQL(View view) throws Exception;

	/**
	 * 删除视图
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRunSQL(View view) throws Exception;

	/**
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkViewExists(StringBuilder builder, boolean exists);

	/**
	 * 视图备注
	 * @param builder builder
	 * @param view 视图
	 * @return StringBuilder
	 */
	StringBuilder comment(StringBuilder builder, View view);

	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/

	/**
	 * 创建主有
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRunSQL(MasterTable table) throws Exception;

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAddCommentRunSQL(MasterTable table) throws Exception;

	/**
	 * 修改主表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRunSQL(MasterTable table) throws Exception;

	/**
	 * 主表重命名
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRunSQL(MasterTable table) throws Exception;

	/**
	 * 修改主表备注
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRunSQL(MasterTable table) throws Exception;

	/**
	 * 删除主表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRunSQL(MasterTable table) throws Exception;


	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/

	/**
	 * 创建分区表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRunSQL(PartitionTable table) throws Exception;

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAddCommentRunSQL(PartitionTable table) throws Exception;
	/**
	 * 修改分区表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRunSQL(PartitionTable table) throws Exception;

	/**
	 * 分区表重命名
	 * @param table
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRunSQL(PartitionTable table) throws Exception;

	/**
	 * 修改分区表备注
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRunSQL(PartitionTable table) throws Exception;

	/**
	 * 删除分区表
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRunSQL(PartitionTable table) throws Exception;


	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 修改表的关键字
	 * @return String
	 */
	String alterColumnKeyword();

	/**
	 * 添加列
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildAddRunSQL(Column column, boolean slice) throws Exception;
	List<Run> buildAddRunSQL(Column column) throws Exception;

	/**
	 * 添加列引导
	 * @param builder StringBuilder
	 * @param column column
	 * @return String
	 */
	StringBuilder addColumnGuide(StringBuilder builder, Column column);

	/**
	 * 修改列
	 * 有可能生成多条SQL
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Column column, boolean slice) throws Exception;
	List<Run> buildAlterRunSQL(Column column) throws Exception;

	/**
	 * 删除列
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildDropRunSQL(Column column, boolean slice) throws Exception;
	List<Run> buildDropRunSQL(Column column) throws Exception;
	/**
	 * 删除列引导
	 * @param builder StringBuilder
	 * @param column column
	 * @return String
	 */
	StringBuilder dropColumnGuide(StringBuilder builder, Column column);

	/**
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Column column) throws Exception;

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeTypeRunSQL(Column column) throws Exception;

	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeDefaultRunSQL(Column column) throws Exception;

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeNullableRunSQL(Column column) throws Exception;

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeCommentRunSQL(Column column) throws Exception;

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAddCommentRunSQL(Column column) throws Exception;


	/**
	 * 取消自增
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropAutoIncrement(Column column) throws Exception;

	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder define(StringBuilder builder, Column column);

	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder type(StringBuilder builder, Column column);
	/**
	 * 列数据类型定义
	 * @param builder builder
	 * @param column 列
	 * @param type 数据类型(已经过转换)
	 * @param isIgnorePrecision 是否忽略长度
	 * @param isIgnoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	StringBuilder type(StringBuilder builder, Column column, String type, boolean isIgnorePrecision, boolean isIgnoreScale);



	boolean isIgnorePrecision(Column column);
	boolean isIgnoreScale(Column column);
	Boolean checkIgnorePrecision(String datatype);
	Boolean checkIgnoreScale(String datatype);
	/**
	 * 非空
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder nullable(StringBuilder builder, Column column);

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder charset(StringBuilder builder, Column column);

	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder defaultValue(StringBuilder builder, Column column);

	/**
	 * 主键(注意不要跟表定义中的主键重复)
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder primary(StringBuilder builder, Column column);
	/**
	 * 递增列
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder increment(StringBuilder builder, Column column);

	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder onupdate(StringBuilder builder, Column column);

	/**
	 * 位置
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder position(StringBuilder builder, Column column);

	/**
	 * 备注
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder comment(StringBuilder builder, Column column);

	/**
	 * 创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkColumnExists(StringBuilder builder, boolean exists);


	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildAddRunSQL(Tag tag) throws Exception;

	/**
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param tag 标签
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Tag tag) throws Exception;

	/**
	 * 删除标签
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildDropRunSQL(Tag tag) throws Exception;

	/**
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Tag tag) throws Exception;

	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeDefaultRunSQL(Tag tag) throws Exception;

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeNullableRunSQL(Tag tag) throws Exception;

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeCommentRunSQL(Tag tag) throws Exception;

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeTypeRunSQL(Tag tag) throws Exception;

	/**
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTagExists(StringBuilder builder, boolean exists);


	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/

	/**
	 * 添加主键
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildAddRunSQL(PrimaryKey primary) throws Exception;

	/**
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param primary 主键
	 * @return List
	 */
	List<Run> buildAlterRunSQL(PrimaryKey primary) throws Exception;

	/**
	 * 删除主键
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildDropRunSQL(PrimaryKey primary) throws Exception;

	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildRenameRunSQL(PrimaryKey primary) throws Exception;



	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/

	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return String
	 */
	List<Run> buildAddRunSQL(ForeignKey foreign) throws Exception;

	/**
	 * 修改外键
	 * @param foreign 外键
	 * @return List
	 */
	List<Run> buildAlterRunSQL(ForeignKey foreign) throws Exception;

	/**
	 * 删除外键
	 * @param foreign 外键
	 * @return String
	 */
	List<Run> buildDropRunSQL(ForeignKey foreign) throws Exception;

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param foreign 外键
	 * @return String
	 */
	List<Run> buildRenameRunSQL(ForeignKey foreign) throws Exception;
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/

	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	List<Run> buildAddRunSQL(Index index) throws Exception;

	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Index index) throws Exception;

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	List<Run> buildDropRunSQL(Index index) throws Exception;

	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Index index) throws Exception;

	/**
	 * 索引备注
	 * @param builder
	 * @param index
	 */
	void comment(StringBuilder builder, Index index);
	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/

	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	List<Run> buildAddRunSQL(Constraint constraint) throws Exception;

	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Constraint constraint) throws Exception;

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	List<Run> buildDropRunSQL(Constraint constraint) throws Exception;

	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Constraint constraint) throws Exception;


	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	/**
	 * 添加触发器
	 * @param trigger 触发器
	 * @return String
	 */
	List<Run> buildCreateRunSQL(Trigger trigger) throws Exception;
	void each(StringBuilder builder, Trigger trigger);

	/**
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param trigger 触发器
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Trigger trigger) throws Exception;

	/**
	 * 删除触发器
	 * @param trigger 触发器
	 * @return String
	 */
	List<Run> buildDropRunSQL(Trigger trigger) throws Exception;

	/**
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param trigger 触发器
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Trigger trigger) throws Exception;


	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	/**
	 * 添加存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	List<Run> buildCreateRunSQL(Procedure procedure) throws Exception;

	/**
	 * 生在输入输出参数
	 * @param builder builder
	 * @param parameter parameter
	 */
	void parameter(StringBuilder builder, Parameter parameter);
	/**
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param procedure 存储过程
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Procedure procedure) throws Exception;

	/**
	 * 删除存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	List<Run> buildDropRunSQL(Procedure procedure) throws Exception;

	/**
	 * 修改存储过程名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param procedure 存储过程
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Procedure procedure) throws Exception;

	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	/**
	 * 添加函数
	 * @param function 函数
	 * @return String
	 */
	List<Run> buildCreateRunSQL(Function function) throws Exception;

	/**
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param function 函数
	 * @return List
	 */
	List<Run> buildAlterRunSQL(Function function) throws Exception;

	/**
	 * 删除函数
	 * @param function 函数
	 * @return String
	 */
	List<Run> buildDropRunSQL(Function function) throws Exception;

	/**
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param function 函数
	 * @return String
	 */
	List<Run> buildRenameRunSQL(Function function) throws Exception;
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 ******************************************************************************************************************/

	/**
	 * 获取单主键列名
	 * @param obj obj
	 * @return String
	 */
	String getPrimaryKey(Object obj);

	/**
	 * 获取单主键值
	 * @param obj obj
	 * @return Object
	 */
	Object getPrimaryValue(Object obj);
/*
	*//**
	 * 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 表
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	boolean convert(String catalog, String schema, String table, RunValue run);
	boolean convert(DataRuntime runtime, Table table, Run run);

	/**
	 * 数据类型转换
	 * @param columns 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(Map<String, Column> columns, RunValue run);

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param column 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(Column column, RunValue run);
	Object convert(Column column, Object value);

	/**
	 * 在不检测数据库结构时才生效,否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param builder builder
	 * @param row DataRow 或 Entity
	 * @param key 列名
	 */
	void value(StringBuilder builder, Object row, String key);

	/**
	 * 根据数据类型生成SQL(如是否需要'',是否需要格式转换函数)
	 * @param builder builder
	 * @param value value
	 */
	//void format(StringBuilder builder, Object value);

	/**
	 * 从数据库中读取数据,常用的基本类型可以自动转换,不常用的如json/point/polygon/blob等转换成anyline对应的类型
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
	 * @return Object
	 */
	Object read(Column metadata, Object value, Class clazz);

	/**
	 * 通过占位符写入数据库前转换成数据库可接受的Java数据类型<br/>
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	Object write(Column metadata, Object value, boolean placeholder);
 	/**
	 * 拼接字符串
	 * @param args args
	 * @return String
	 */
	String concat(String ... args);

	/**
	 * 是否是数字列
	 * @param column 列
	 * @return boolean
	 */
	boolean isNumberColumn(Column column);

	/**
	 * 是否是boolean列
	 * @param column 列
	 * @return boolean
	 */
	boolean isBooleanColumn(Column column);

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param column 列
	 * @return boolean
	 */
	boolean isCharColumn(Column column);

	/**
	 * 内置函数
	 * 如果需要引号,方法应该一块返回
	 * @param column 列属性,不同的数据类型解析出来的值可能不一样
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	String value(Column column, SQL_BUILD_IN_VALUE value);

	/**
	 * 转换成相应数据库的数据类型包含精度
	 * @param column column
	 * @return String
	 */
	//String type(Column column);

	/**
	 * 数据库类型转换成java类型
	 * @param type type
	 * @return String
	 */
	//String type2class(String type);

	/**
	 * 对象名称格式化(大小写转换)，在查询系统表时需要
	 * @param name name
	 * @return String
	 */
	String objectName(String name);
}
