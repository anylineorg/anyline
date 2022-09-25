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


package org.anyline.jdbc.adapter;

import org.anyline.entity.DataSet;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.prepare.RunPrepare;
import org.anyline.jdbc.run.RunValue;
import org.anyline.jdbc.run.Run;
import org.anyline.jdbc.entity.*;
import org.anyline.jdbc.run.TableRun;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface JDBCAdapter {
	//内置VALUE
	public static enum SQL_BUILD_IN_VALUE{
		CURRENT_TIME  		{public String getCode(){return "CURRENT_TIME";}	public String getName(){return "当前时间";}};

		public abstract String getCode();
		public abstract String getName();
	}


	public static enum DB_TYPE{
		Cassandra			{public String getCode(){return "DB_TYPE_CASSANDRA";}			public String getName(){return "Cassandra";}}			,
		ClickHouse			{public String getCode(){return "DB_TYPE_CLICKHOUSE";}			public String getName(){return "ClickHouse";}}			,
		CockroachDB			{public String getCode(){return "DB_TYPE_COCKROACHDB";}			public String getName(){return "CockroachDB";}}			,
		DB2					{public String getCode(){return "DB_TYPE_DB2";}					public String getName(){return "db2";}}					,
		Derby  				{public String getCode(){return "DB_TYPE_DERBY";}				public String getName(){return "Derby";}}				,
		DM		 			{public String getCode(){return "DB_TYPE_DM";}					public String getName(){return "达梦";}}					,
		GBase  				{public String getCode(){return "DB_TYPE_GBASE";}				public String getName(){return "南大通用";}}				,
		H2  				{public String getCode(){return "DB_TYPE_H2";}					public String getName(){return "H2";}}					,
		HighGo				{public String getCode(){return "DB_TYPE_HIGHGO";}				public String getName(){return "瀚高";}}					,
		HSQLDB  			{public String getCode(){return "DB_TYPE_HSQLDB";}				public String getName(){return "HSQLDB";}}				,
		InfluxDB			{public String getCode(){return "DB_TYPE_INFLUXDB";}			public String getName(){return "InfluxDB";}}			,
		KingBase			{public String getCode(){return "DB_TYPE_KINGBASE";}			public String getName(){return "人大金仓 Oracle";}}		,
		KingBase_PostgreSQL	{public String getCode(){return "DB_TYPE_KINGBASE_POSTGRESQL";}	public String getName(){return "人大金仓 PostgreSQL";}}	,
		MariaDB				{public String getCode(){return "DB_TYPE_MARIADB";}				public String getName(){return "MariaDB";}}				,
		MongoDB				{public String getCode(){return "DB_TYPE_MONGODB";}				public String getName(){return "MongoDB";}}				,
		MSSQL				{public String getCode(){return "DB_TYPE_MSSQL";}				public String getName(){return "mssql";}}				,
		MYSQL				{public String getCode(){return "DB_TYPE_MYSQL";}				public String getName(){return "mysql";}}				,
		Neo4j  				{public String getCode(){return "DB_TYPE_NEO4J";}				public String getName(){return "Neo4j";}}				,
		OceanBase 			{public String getCode(){return "DB_TYPE_OCEANBASE";}			public String getName(){return "OceanBase";}}			,
		ORACLE				{public String getCode(){return "DB_TYPE_ORACLE";}				public String getName(){return "oracle";}}				,
		oscar				{public String getCode(){return "DB_TYPE_OSCAR";}				public String getName(){return "神舟通用";}}				,
		PolarDB  			{public String getCode(){return "DB_TYPE_POLARDB";}				public String getName(){return "PolarDB";}}				,
		PostgreSQL 			{public String getCode(){return "DB_TYPE_POSTGRESQL";}			public String getName(){return "PostgreSQL";}}			,
		QuestDB 			{public String getCode(){return "DB_TYPE_QUESTDB";}				public String getName(){return "QuestDB";}}				,
		RethinkDB  			{public String getCode(){return "DB_TYPE_RETHINKDB";}			public String getName(){return "RethinkDB";}}			,
		SQLite  			{public String getCode(){return "DB_TYPE_SQLITE";}				public String getName(){return "SQLite";}}				,
		TDengine  			{public String getCode(){return "DB_TYPE_TDENGINE";}			public String getName(){return "TDengine";}}			,
		Timescale			{public String getCode(){return "DB_TYPE_TIMESCALE";}			public String getName(){return "Timescale";}};

		public abstract String getCode();
		public abstract String getName();
	} 
	public static final String TAB 		= "\t"		;
	public static final String BR 		= "\n"		;
	public static final String BR_TAB 	= "\n\t"	;
	
	public DB_TYPE type();

	/**
	 * 界定符
	 * @return String
	 */
	public String getDelimiterFr();
	public String getDelimiterTo();


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
	 * @param dest 表
	 * @param obj 实体
	 * @param checkParimary 是否检测主键
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run
	 */
	public Run buildInsertRun(String dest, Object obj, boolean checkParimary, String ... columns);

	/**
	 * 根据Collection创建批量插入SQL
	 * @param builder builder
	 * @param dest 表 如果不指定则根据DataSet解析
	 * @param set 数据集
	 * @param keys keys 南非要插入的列
	 */
	public void createInserts(StringBuilder builder, String dest, Collection list, List<String> keys);

	/**
	 * 根据DataSet创建批量插入SQL
	 * @param builder builder
	 * @param dest 表 如果不指定则根据DataSet解析
	 * @param set 数据集
	 * @param keys keys 南非要插入的列
	 */
	public void createInserts(StringBuilder builder, String dest, DataSet set, List<String> keys);

	/**
	 * 确认需要插入的列
	 * @param obj  Entity或DataRow
	 * @param columns 提供额外的判断依据
	 *                列可以加前缀
	 *                +:表示必须插入
	 *                -:表示必须不插入
	 *                ?:根据是否有值
	 *
	 *        如果没有提供columns,长度为0也算没有提供
	 *        则解析obj(遍历所有的属性工Key)获取insert列
	 *
	 *        如果提供了columns则根据columns获取insert列
	 *
	 *        但是columns中出现了添加前缀列，则解析完columns后，继续解析obj
	 *
	 *        以上执行完后，如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true
	 *        则把执行结果与表结构对比，删除表中没有的列
	 * @return List
	 */
	public List<String> confirmInsertColumns(String dst, Object data, String ... columns);


	/* *****************************************************************************************************************
	 * 													UPDATE
	 ******************************************************************************************************************/

	/**
	 * 创建更新SQL
	 * @param dest dest
	 * @param obj obj
	 * @param checkParimary checkParimary
	 * @param columns columns
	 * @return Run
	 */
	public Run buildUpdateRun(String dest, Object obj, boolean checkParimary, String ... columns);



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
	public Run buildQueryRun(RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * 构造查询主体
	 * @param run run
	 * @return Run
	 */
	public void buildQueryRunContent(Run run);


	/**
	 * 创建最终执行查询SQL
	 * @param run  run
	 * @return String
	 */
	public String parseFinalQuery(Run run);


	/* *****************************************************************************************************************
	 * 													COUNT
	 ******************************************************************************************************************/

	/**
	 * 创建统计总数SQL
	 * @param run  Run
	 * @return String
	 */
	public String parseTotalQueryTxt(Run run);


	/* *****************************************************************************************************************
	 * 													EXISTS
	 ******************************************************************************************************************/

	/**
	 * 创建检测是否存在SQL
	 * @param run run
	 * @return String
	 */
	public String parseExistsTxt(Run run);


	/* *****************************************************************************************************************
	 * 													EXECUTE
	 ******************************************************************************************************************/

	/**
	 * 创建执行SQL
	 * @param sql sql
	 * @param configs configs
	 * @param conditions conditions
	 * @return Run
	 */
	public Run buildExecuteRunSQL(RunPrepare prepare, ConfigStore configs, String ... conditions);


	/* *****************************************************************************************************************
	 * 													DELETE
	 ******************************************************************************************************************/

	/**
	 * 创建删除SQL
	 * @param dest 表
	 * @param obj entity
	 * @param columns 删除条件的我
	 * @return Run
	 */
	public Run buildDeleteRunSQL(String dest, Object obj, String ... columns);
	/**
	 * 根据key values删除
	 * @param table 表
	 * @param key key
	 * @param values values
	 * @return Run
	 */
	public Run buildDeleteRunSQL(String table, String key, Object values);

	/**
	 * 构造删除主体
	 * @param run run
	 * @return Run
	 */
	public Run buildDeleteRunContent(Run run);



	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区有
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/

	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照buildQueryTableRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws exception
	 */
	public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	public LinkedHashMap<String, Table> tables(boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, ResultSet set) throws Exception;



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
	public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	public LinkedHashMap<String, MasterTable> mtables(boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, ResultSet set) throws Exception;


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
	public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception;

	/**
	 * 根据主表查询分区有
	 * @param master 主表
	 * @return sql
	 * @throws Exception
	 */
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master) throws Exception;

	/**
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	public LinkedHashMap<String, PartitionTable> ptables(int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	public LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable master, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception;


	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(SELEC * FROM T WHERE 1=0) | 查询系统表
	 * @return sqls
	 */
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception
	 */
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception;

	/**
	 * 解析查询结果metadata(0=1)
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param columns columns
	 * @param set set
	 * @return columns columns
	 * @throws Exception
	 */
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, SqlRowSet set) throws Exception;

	/**
	 * 解析JDBC getcolumns结果
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param columns columns
	 * @param set set
	 * @return columns columns
	 * @throws Exception
	 */
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, ResultSet set) throws Exception;



	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	public List<String> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags
	 * @throws exception
	 */
	public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception;

	/**
	 * 解析查询结果metadata(0=1)
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set 查询结果
	 * @return tags
	 * @throws Exception
	 */
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception;

	/**
	 *
	 * 解析JDBC getcolumns结果
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set 查询结果
	 * @return tags
	 * @throws Exception
	 */
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, ResultSet set) throws Exception;


	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/

	/**
	 * 查询表上的所引
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	public List<String> buildQueryIndexRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Index
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws exception
	 */
	public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception;

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception
	 */
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception;

	/**
	 * 解析JDBC getIndex结果
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception
	 */
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, ResultSet set) throws Exception;


	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/

	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Constraint
	 * @param constraint 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws exception
	 */
	public LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception;
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception;
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception;




	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区有
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/

	/**
	 * 创建表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildCreateRunSQL(Table table) throws Exception;

	/**
	 * 修改表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildAlterRunSQL(Table table) throws Exception;

	/**
	 * 重命名
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildRenameRunSQL(Table table) throws Exception;

	/**
	 * 修改备注
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildChangeCommentRunSQL(Table table) throws Exception;

	/**
	 * 删除表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildDropRunSQL(Table table) throws Exception;

	/**
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists);


	/**
	 * 创建主键在创建表的DDL结尾部分
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	public StringBuilder primary(StringBuilder builder, Table table);

	/**
	 * 表备注
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	public StringBuilder comment(StringBuilder builder, Table table);

	/**
	 * 构造表名
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	public StringBuilder name(StringBuilder builder, Table table);


	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/

	/**
	 * 创建主有
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildCreateRunSQL(MasterTable table) throws Exception;

	/**
	 * 修改主表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildAlterRunSQL(MasterTable table) throws Exception;

	/**
	 * 主表重命名
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildRenameRunSQL(MasterTable table) throws Exception;

	/**
	 * 修改主表备注
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildChangeCommentRunSQL(MasterTable table) throws Exception;

	/**
	 * 删除主表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildDropRunSQL(MasterTable table) throws Exception;


	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/

	/**
	 * 创建分区表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildCreateRunSQL(PartitionTable table) throws Exception;

	/**
	 * 修改分区表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildAlterRunSQL(PartitionTable table) throws Exception;

	/**
	 * 分区表重命名
	 * @param table
	 * @return sql
	 * @throws Exception
	 */
	public String buildRenameRunSQL(PartitionTable table) throws Exception;

	/**
	 * 修改分区有备注
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildChangeCommentRunSQL(PartitionTable table) throws Exception;

	/**
	 * 删除分区表
	 * @param table 表
	 * @return sql
	 * @throws Exception
	 */
	public String buildDropRunSQL(PartitionTable table) throws Exception;


	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 修改表的关键字
	 * @return String
	 */
	public String alterColumnKeyword();

	/**
	 * 添加列
	 * @param column 列
	 * @return String
	 */
	public String buildAddRunSQL(Column column) throws Exception;

	/**
	 * 修改列
	 * 有可能生成多条SQL
	 * @param column 列
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Column column) throws Exception;

	/**
	 * 删除列
	 * @param column 列
	 * @return String
	 */
	public String buildDropRunSQL(Column column) throws Exception;

	/**
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	public String buildRenameRunSQL(Column column) throws Exception;

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	public List<String> buildChangeTypeRunSQL(Column column) throws Exception;

	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	public String buildChangeDefaultRunSQL(Column column) throws Exception;

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	public String buildChangeNullableRunSQL(Column column) throws Exception;

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Column column) throws Exception;

	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder define(StringBuilder builder, Column column);

	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder type(StringBuilder builder, Column column);

	/**
	 * 非空
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder nullable(StringBuilder builder, Column column);

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder charset(StringBuilder builder, Column column);

	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder defaultValue(StringBuilder builder, Column column);

	/**
	 * 递增列
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder increment(StringBuilder builder, Column column);

	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder onupdate(StringBuilder builder, Column column);

	/**
	 * 位置
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder position(StringBuilder builder, Column column);

	/**
	 * 备注
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	public StringBuilder comment(StringBuilder builder, Column column);

	/**
	 * 创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkColumnExists(StringBuilder builder, boolean exists);


	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * @param tag 标签
	 * @return String
	 */
	public String buildAddRunSQL(Tag tag) throws Exception;

	/**
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param tag 标签
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Tag tag) throws Exception;

	/**
	 * 删除标签
	 * @param tag 标签
	 * @return String
	 */
	public String buildDropRunSQL(Tag tag) throws Exception;

	/**
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	public String buildRenameRunSQL(Tag tag) throws Exception;

	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	public String buildChangeDefaultRunSQL(Tag tag) throws Exception;

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	public String buildChangeNullableRunSQL(Tag tag) throws Exception;

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Tag tag) throws Exception;

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	public List<String> buildChangeTypeRunSQL(Tag tag) throws Exception;

	/**
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkTagExists(StringBuilder builder, boolean exists);


	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/

	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	public String buildAddRunSQL(Index index) throws Exception;

	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Index index) throws Exception;

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	public String buildDropRunSQL(Index index) throws Exception;

	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	public String buildRenameRunSQL(Index index) throws Exception;


	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/

	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	public String buildAddRunSQL(Constraint constraint) throws Exception;

	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Constraint constraint) throws Exception;

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	public String buildDropRunSQL(Constraint constraint) throws Exception;

	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	public String buildRenameRunSQL(Constraint constraint) throws Exception;


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
	public String getPrimaryKey(Object obj);

	/**
	 * 获取单主键值
	 * @param obj obj
	 * @return Object
	 */
	public Object getPrimaryValue(Object obj);

	/**
	 * 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 表
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	public boolean convert(String catalog, String schema, String table, RunValue run);

	/**
	 * 数据类型转换
	 * @param columns 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	public boolean convert(Map<String, Column> columns, RunValue run);

	/**
	 * 数据类型转换
	 * @param column 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	public boolean convert(Column column, RunValue run);

	/**
	 * 在不检测数据库结构时才生效，否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param builder builder
	 * @param row DataRow 或 Entity
	 * @param key 列名
	 */
	public void value(StringBuilder builder, Object row, String key);

	/**
	 * 根据数据类型生成SQL(如是否需要'')
	 * @param builder builder
	 * @param value value
	 */
	public void format(StringBuilder builder, Object value);

	/**
	 * 拼接字符串
	 * @param args args
	 * @return String
	 */
	public String concat(String ... args);

	/**
	 * 是否是数字列
	 * @param column 列
	 * @return boolean
	 */
	public boolean isNumberColumn(Column column);

	/**
	 * 是否是boolean列
	 * @param column 列
	 * @return boolean
	 */
	public boolean isBooleanColumn(Column column);

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param column 列
	 * @return boolean
	 */
	public boolean isCharColumn(Column column);

	/**
	 * 内置函数
	 * 如果需要引号，方法应该一块返回
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	public String buildInValue(SQL_BUILD_IN_VALUE value);

	/**
	 * 转换成相应数据库的数据类型
	 * @param type type
	 * @return String
	 */
	public String type2type(String type);

	/**
	 * 数据库类型转换成java类型
	 * @param type type
	 * @return String
	 */
	public String type2class(String type);

}
