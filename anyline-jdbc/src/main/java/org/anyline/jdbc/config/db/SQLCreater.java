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
	public RunSQL createQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions);

	public RunSQL createDeleteRunSQL(String dest, Object obj, String ... columns);
	public RunSQL createDeleteRunSQL(String table, String key, Object values);
	public RunSQL createExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions); 
	 
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
	public RunSQL createInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns);
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set, List<String> keys);
	public void format(StringBuilder builder, Object row, String key);
	public RunSQL createUpdateTxt(String dest, Object obj, boolean checkParimary, String ... columns); 
	public String getDelimiterFr();
	public String getDelimiterTo();
	public String getPrimaryKey(Object obj); 
	public Object getPrimaryValue(Object obj);
	public List<String> confirmInsertColumns(String dst, Object data, String ... columns);


	public boolean convert(String catalog, String schema, String table, RunValue run);
	public boolean convert(Map<String, Column> columns, RunValue value);
	public boolean convert(Column column, RunValue value);
	/**
	 * 拼接字符串
	 * @param args args
	 * @return String
	 */
	public String concat(String ... args);


	public String createAddRunSQL(Column column);
	public String createAlterRunSQL(Column column);
	public String createDropRunSQL(Column column);


	public String createAddRunSQL(Table table);
	public String createAlterRunSQL(Table table);
	public String createDropRunSQL(Table table);

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

}
