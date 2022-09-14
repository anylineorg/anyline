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

import org.anyline.entity.DataSet;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;

import java.util.List;
import java.util.Map;

public interface SQLCreater{
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
		MariaDB				{public String getCode(){return "DB_TYPE_MARIADB";}				public String getName(){return "MariaDB";}}				,
		MongoDB				{public String getCode(){return "DB_TYPE_MONGODB";}				public String getName(){return "MongoDB";}}				,
		MSSQL				{public String getCode(){return "DB_TYPE_MSSQL";}				public String getName(){return "mssql";}}				,
		MYSQL				{public String getCode(){return "DB_TYPE_MYSQL";}				public String getName(){return "mysql";}}				,
		Neo4j  				{public String getCode(){return "DB_TYPE_NEO4J";}				public String getName(){return "Neo4j";}}				,
		KingBase			{public String getCode(){return "DB_TYPE_KINGBASE";}			public String getName(){return "人大金仓 Oracle";}}		,
		KingBase_PostgreSQL	{public String getCode(){return "DB_TYPE_KINGBASE_POSTGRESQL";}	public String getName(){return "人大金仓 PostgreSQL";}}	,
		OceanBase 			{public String getCode(){return "DB_TYPE_OCEANBASE";}			public String getName(){return "OceanBase";}}			,
		ORACLE				{public String getCode(){return "DB_TYPE_ORACLE";}				public String getName(){return "oracle";}}				,
		oscar				{public String getCode(){return "DB_TYPE_OSCAR";}				public String getName(){return "神舟通用";}}				,
		PolarDB  			{public String getCode(){return "DB_TYPE_POLARDB";}				public String getName(){return "PolarDB";}}				,
		PostgreSQL 			{public String getCode(){return "DB_TYPE_POSTGRESQL";}			public String getName(){return "PostgreSQL";}}			,
		QuestDB 			{public String getCode(){return "DB_TYPE_QUESTDB";}				public String getName(){return "QuestDB";}}				,
		RethinkDB  			{public String getCode(){return "DB_TYPE_RETHINKDB";}			public String getName(){return "RethinkDB";}}			,
		SQLite  			{public String getCode(){return "DB_TYPE_SQLITE";}				public String getName(){return "SQLite";}}				,
		TDengine  			{public String getCode(){return "DB_TYPE_TDENGINE";}			public String getName(){return "TDengine";}}			,
		TimescaleDB			{public String getCode(){return "DB_TYPE_TIMESCALEDB";}			public String getName(){return "TimescaleDB";}};

		public abstract String getCode();
		public abstract String getName();
	} 
	public static final String TAB = "\t"; 
	public static final String BR = "\n"; 
	public static final String BR_TAB = "\n\t"; 
	
	public DB_TYPE type();
	/** 
	 * 创建查询SQL 
	 * @param sql  sql
	 * @param configs 查询条件配置
	 * @param conditions 查询条件
	 * @return RunSQL
	 */
	public RunSQL buildQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions);

	public RunSQL buildDeleteRunSQL(String dest, Object obj, String ... columns);
	public RunSQL buildDeleteRunSQL(String table, String key, Object values);
	public RunSQL buildExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions);
	 
	public String parseBaseQueryTxt(RunSQL run); 
	/** 
	 * 求总数SQL 
	 * @param run  RunSQL
	 * @return String
	 */ 
	public String parseTotalQueryTxt(RunSQL run); 
	 
	public String parseExistsTxt(RunSQL run); 
	/** 
	 * 查询SQL 
	 * @param run  run
	 * @return String
	 */ 
	public String parseFinalQueryTxt(RunSQL run);
	public RunSQL buildInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns);
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set, List<String> keys);

	public RunSQL createUpdateTxt(String dest, Object obj, boolean checkParimary, String ... columns);

	/**
	 * 界定符
	 * @return String
	 */
	public String getDelimiterFr();
	public String getDelimiterTo();
	public String getPrimaryKey(Object obj); 
	public Object getPrimaryValue(Object obj);

	/**
	 * 确认需要插入的列
	 * @param dst dst
	 * @param data data
	 * @param columns 明确指定需要插入的列
	 * @return list
	 */
	public List<String> confirmInsertColumns(String dst, Object data, String ... columns);

	/**
	 * 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param catalog catalog
	 * @param schema schema
	 * @param table table
	 * @param run  RunValue
	 * @return boolean 返回false表示转换失败 如果有多个creater 则交给creater继续转换
	 */

	public boolean convert(String catalog, String schema, String table, RunValue run);
	public boolean convert(Map<String, Column> columns, RunValue value);
	public boolean convert(Column column, RunValue value);

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
	 * 拼接字符串
	 * @param args args
	 * @return String
	 */
	public String concat(String ... args);


	public String buildAddRunSQL(Column column);

	/**
	 * 修改列
	 * 有可能生成多条SQL
	 * @param column column
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Column column);

	/**
	 * 删除列
	 * @param column column
	 * @return String
	 */
	public String buildDropRunSQL(Column column);

	/**
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildRenameRunSQL(Column column);

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public List<String> buildChangeTypeRunSQL(Column column);
	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeDefaultRunSQL(Column column);

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeNullableRunSQL(Column column);

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Column column);


	public String buildCreateRunSQL(Table table);
	public String buildAlterRunSQL(Table table);
	public String buildDropRunSQL(Table table);
	public String buildRenameRunSQL(Table table);
	public String buildChangeCommentRunSQL(Table table);


	/**
	 * 构造表名
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	public StringBuilder name(StringBuilder builder, Table table);

	public String alterColumnKeyword();
	/**
	 * 主键
	 * @param builder builder
	 * @param table table
	 */
	public StringBuilder primary(StringBuilder builder, Table table);

	/**
	 * 定义列
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder define(StringBuilder builder, Column column);
	/**
	 * 自增长列
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder increment(StringBuilder builder, Column column);

	/**
	 * 备注
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder comment(StringBuilder builder, Column column);

	/**
	 * 位置
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder position(StringBuilder builder, Column column);
	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder onupdate(StringBuilder builder, Column column);

	/**
	 * 默认值
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder defaultValue(StringBuilder builder, Column column);

	/**
	 * 编码
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder charset(StringBuilder builder, Column column);

	/**
	 * 非空
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder nullable(StringBuilder builder, Column column);

	/**
	 * 数据类型
	 * @param builder builder
	 * @param column column
	 */
	public StringBuilder type(StringBuilder builder, Column column);

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param column 列
	 * @return boolean
	 */
	public boolean isCharColumn(Column column);
	public boolean isNumberColumn(Column column);
	public boolean isBooleanColumn(Column column);

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
