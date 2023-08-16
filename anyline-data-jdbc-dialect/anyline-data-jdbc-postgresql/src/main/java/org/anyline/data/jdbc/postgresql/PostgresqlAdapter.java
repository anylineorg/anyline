 
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


package org.anyline.data.jdbc.postgresql;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.RegularUtil;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("anyline.data.jdbc.adapter.postgresql")
public class PostgresqlAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	
	public DatabaseType type(){
		return DatabaseType.PostgreSQL;
	}


	@Value("${anyline.data.jdbc.delimiter.postgresql:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public PostgresqlAdapter(){
		super();
		delimiterFr = "\"";
		delimiterTo = "\"";
		for (PostresqlColumnTypeAlias alias: PostresqlColumnTypeAlias.values()){
			types.put(alias.name(), alias.standard());
		}
		for(PostgresqlWriter writer: PostgresqlWriter.values()){
			Object[] supports = writer.supports();
			if(null != supports) {
				for(Object support:supports)
				writers.put(support, writer.writer());
			}
		}
		for(PostgresqlReader reader: PostgresqlReader.values()){
			Object[] supports = reader.supports();
			if(null != supports) {
				for(Object support:supports)
					readers.put(support, reader.reader());
			}
		}
	}
	@Override
	public String mergeFinalQuery(DataRuntime runtime, Run run){
		String sql = run.getBaseQuery(); 
		String cols = run.getQueryColumns(); 
		if(!"*".equals(cols)){
			String reg = "(?i)^select[\\s\\S]+from"; 
			sql = sql.replaceAll(reg,"SELECT "+cols+" FROM "); 
		} 
		OrderStore orders = run.getOrderStore(); 
		if(null != orders){
			sql += orders.getRunText(getDelimiterFr()+getDelimiterTo());
		} 
		PageNavi navi = run.getPageNavi(); 
		if(null != navi){
			long limit = navi.getLastRow() - navi.getFirstRow() + 1; 
			if(limit < 0){
				limit = 0; 
			} 
			sql += " LIMIT " + limit + " OFFSET " + navi.getFirstRow(); 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	}



	@Override
	public String concat(DataRuntime runtime, String ... args){
		return concatOr(args);
	}



	/* *****************************************************************************************************************
	 *
	 * 														DML
	 *
	 *  *****************************************************************************************************************/

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names){
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		String key = "CURRVAL";
		if(next){
			key = "NEXTVAL";
		}
		if(null != names && names.length>0) {
			builder.append("SELECT ");
			boolean first = true;
			for (String name : names) {
				if(!first){
					builder.append(",");
				}
				first = false;
				builder.append(key).append("('").append(name).append("') AS ").append(name);
			}
		}
		return runs;
	}



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

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * List<Run> buildQueryTableCommentRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, Table table) throws Exception
	 * public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryTableRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryTableCommentRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryTableCommentRun(runtime, catalog, schema, pattern, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.tables(runtime, index, create, catalog, schema, tables, set);
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryMasterTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types);
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryMasterTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryMasterTableRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * 从jdbc结果中提取表结构
	 * ResultSet set = con.getMetaData().getTables()
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return List
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.mtables(runtime, create, tables, catalog, schema, pattern, types);
	}


	/**
	 * 从上一步生成的SQL查询结果中 提取表结构
	 * @param index 第几条SQL
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.mtables(runtime, index, create, catalog, schema, tables, set);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types);
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable table, Map<String,Object> tags);
	 * LinkedHashMap<String, PartitionTable> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable table, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;
	 * LinkedHashMap<String, PartitionTable> ptables(DataRuntime runtime, boolean create, String catalog, MasterTable table, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询分区表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryPartitionTableRun(runtime, catalog, schema, pattern, types);
	}
	@Override
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable table, Map<String,Object> tags) throws Exception{
		return super.buildQueryPartitionTableRun(runtime, table, tags);
	}

	/**
	 *  根据查询结果集构造Table
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.ptables(runtime, total, index, create, master, catalog, schema, tables, set);
	}

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, MasterTable master) throws Exception{
		return super.ptables(runtime, create, tables, catalog, schema, master);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata);
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(true:1=0,false:查询系统表)
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		List<Run> runs = new ArrayList<>();
		if(BasicUtil.isEmpty(table.getName())){
			return runs;
		}
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(runtime, builder, table);
			builder.append(" WHERE 1=0");
		}else{
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			builder.append("SELECT M.* ,FD.DESCRIPTION AS COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS M\n");
			builder.append("LEFT JOIN PG_CLASS FC ON FC.RELNAME = M.TABLE_NAME\n");
			builder.append("LEFT JOIN PG_DESCRIPTION FD ON FD.OBJOID = FC.OID AND FD.OBJSUBID = M.ORDINAL_POSITION\n");
			builder.append("WHERE 1 = 1\n");
			if(BasicUtil.isNotEmpty(catalog)){
				builder.append(" AND M.TABLE_CATALOG = '").append(catalog).append("'");
			}
			if(BasicUtil.isNotEmpty(schema)){
				builder.append(" AND M.TABLE_SCHEMA = '").append(schema).append("'");
			}
			builder.append(" AND M.TABLE_NAME = '").append(table.getName()).append("'");
		}
		return runs;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		set.changeKey("UDT_NAME","DATA_TYPE");
		return super.columns(runtime, index, create, table, columns, set);
	}


	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		return super.columns(runtime, create, columns, table, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
		return super.columns(runtime, create, columns, table, pattern);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata);
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception;
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception;
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/
	/**
	 *
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		return super.buildQueryTagRun(runtime, table, metadata);
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception{
		return super.tags(runtime, index, create, table, tags, set);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception{
		return super.tags(runtime, create, table, tags, set);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception{
		return super.tags(runtime, create, tags, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception
	 * PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		//test_pk_pkey	| p	| {2,1}	| 	PRIMARY KEY (id, name)
		builder.append("SELECT  m.conname,  pg_get_constraintdef(m.oid, true) AS define\n");
		builder.append("FROM pg_constraint m \n");
		builder.append("LEFT JOIN pg_namespace ns ON m.connamespace = ns.oid \n");
		builder.append("LEFT JOIN pg_class ft ON m.conrelid = ft.oid \n");
		builder.append("WHERE ft.relname = '").append(table.getName()).append("'");
		String schema = table.getSchema();
		if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND ns.nspname = '").append(schema).append("'");
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception{
		PrimaryKey primary = null;
		if(set.size()>0){
			DataRow row = set.getRow(0);
			primary = new PrimaryKey();
			//conname 	    |contype	|conkey |  define
			//test_pk_pkey	| p			| {2,1}	| 	PRIMARY KEY (id, name)
			primary.setName(row.getString("conname"));
			String define = row.getString("define");
			String[] cols = RegularUtil.cut(define, "(",")").split(",");
			for(String col:cols){
				Column column = new Column(col.trim().replace("\"", ""));
				column.setTable(table);
				primary.addColumn(column);
			}
		}
		return primary;
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/


	/**
	 * 查询表上的外键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT TC.CONSTRAINT_NAME,TC.TABLE_NAME AS TABLE_NAME, KCU.COLUMN_NAME AS COLUMN_NAME, KCU.ORDINAL_POSITION,CCU.TABLE_NAME AS REFERENCED_TABLE_NAME, CCU.COLUMN_NAME AS REFERENCED_COLUMN_NAME\n");
		builder.append("FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS TC\n");
		builder.append("JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KCU ON TC.CONSTRAINT_NAME = KCU.CONSTRAINT_NAME\n");
		builder.append("JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE AS CCU ON CCU.CONSTRAINT_NAME = TC.CONSTRAINT_NAME\n");
		builder.append("WHERE TC.CONSTRAINT_TYPE = 'FOREIGN KEY'\n");
		if(null != table){
			String name = table.getName();
			if(BasicUtil.isNotEmpty(name)){
				builder.append(" AND TC.TABLE_NAME = '").append(name).append("'\n");
			}
		}
		builder.append("ORDER BY KCU.ORDINAL_POSITION");
		return runs;
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception{
		if(null == foreigns){
			foreigns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("CONSTRAINT_NAME");
			T foreign = foreigns.get(name.toUpperCase());
			if(null == foreign){
				foreign = (T)new ForeignKey();
				foreign.setName(name);
				foreign.setTable(row.getString("TABLE_NAME"));
				foreign.setReference(row.getString("REFERENCED_TABLE_NAME"));
				foreigns.put(name.toUpperCase(), foreign);
			}
			foreign.addColumn(new Column(row.getString("COLUMN_NAME")).setReference(row.getString("REFERENCED_COLUMN_NAME")).setPosition(row.getInt("ORDINAL_POSITION", 0)));

		}
		return foreigns;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, boolean metadata);
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception;
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception;
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列
	 * @param table 表
	 * @param name name
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name){
		return super.buildQueryIndexRun(runtime, table, name);
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception{
		return super.indexs(runtime, index, create, table, indexs, set);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception{
		return super.indexs(runtime, create, table, indexs, set);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception{
		return super.indexs(runtime, create, indexs, table, unique, approximate);
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, boolean metadata);
	 * LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		return super.buildQueryConstraintRun(runtime, table, metadata);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index , boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set) throws Exception{

		return super.constraints(runtime, index, create, table, constraints, set);
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception{
		return super.constraints(runtime, create, table, constraints, set);
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception{
		return super.constraints(runtime, create, table, constraints, set);
	}



	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * 查询表上的trigger
	 * @param table 表
	 * @param events INSERT|UPATE|DELETE
	 * @return sqls
	 */

	@Override
	public List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT * FROM INFORMATION_SCHEMA.TRIGGERS WHERE 1=1");
		if(null != table){
			String schemae = table.getSchema();
			String name = table.getName();
			if(BasicUtil.isNotEmpty(schemae)){
				builder.append(" AND TRIGGER_SCHEMA = '").append(schemae).append("'");
			}
			if(BasicUtil.isNotEmpty(name)){
				builder.append(" AND EVENT_OBJECT_TABLE = '").append(name).append("'");
			}
		}
		if(null != events && events.size()>0){
			builder.append(" AND(");
			boolean first = true;
			for(Trigger.EVENT event:events){
				if(!first){
					builder.append(" OR ");
				}
				builder.append("EVENT_MANIPULATION ='").append(event);
			}
			builder.append(")");
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */

	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception{
		if(null == triggers){
			triggers = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("TRIGGER_NAME");
			T trigger = triggers.get(name.toUpperCase());
			if(null == trigger){
				trigger = (T)new Trigger();
			}
			trigger.setName(name);
			Table tab = new Table(row.getString("EVENT_OBJECT_TABLE"));
			tab.setSchema(row.getString("TRIGGER_SCHEMA"));
			tab.setCatalog(row.getString("TRIGGER_CATALOG("));
			trigger.setTable(tab);
			boolean each = false;
			if("ROW".equalsIgnoreCase(row.getString("ACTION_ORIENTATION"))){
				each = true;
			}
			trigger.setEach(each);
			try{
				String[] events = row.getStringNvl("EVENT_MANIPULATION").split(",");
				String time = row.getString("ACTION_TIMING");
				trigger.setTime(Trigger.TIME.valueOf(time));
				for(String event:events) {
					trigger.addEvent(Trigger.EVENT.valueOf(event));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			trigger.setDefinition(row.getString("ACTION_STATEMENT"));

			triggers.put(name.toUpperCase(), trigger);

		}
		return triggers;
	}






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
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Table table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table table);
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns)
	 * List<Run> buildRenameRun(DataRuntime runtime, Table table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table table)
	 * List<Run> buildDropRun(DataRuntime runtime, Table table)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder name(DataRuntime runtime, StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}


	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns) throws Exception{
		return super.buildAlterRun(runtime, table, columns);
	}
	/**
	 * 修改表名
	 * ALTER TABLE A RENAME TO B;
	 * @param table table
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Table table)  throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, table);
		builder.append(" RENAME TO ");
		//去掉catalog schema前缀
		Table update = new Table(table.getUpdate().getName());
		name(runtime, builder, update);
		return runs;
	}

	/**
	 * 修改备注
	 * COMMENT ON TABLE T IS 'ABC';
	 * @param table table
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			builder.append("COMMENT ON TABLE ");
			name(runtime, builder, table);
			builder.append(" IS '").append(comment).append("'");
		}
		return runs;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table table) throws Exception {
		return buildChangeCommentRun(runtime, table);
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildDropRun(runtime, table);
	}


	/**
	 * 创建之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkTableExists(runtime, builder, exists);
	}


	/**
	 * 主键
	 * CONSTRAINT PK_BS_DEV PRIMARY KEY (ID ASC)
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table){
		List<Column> pks = table.primarys();
		if(pks.size()>0){
			builder.append(",CONSTRAINT ").append("PK_").append(table.getName()).append(" PRIMARY KEY (");
			boolean first = true;
			for(Column pk:pks){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				String order = pk.getOrder();
				if(null != order){
					builder.append(" ").append(order);
				}
				first = false;
			}
			builder.append(")");
		}
		return builder;
	}


	/**
	 * 备注
	 *
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table){
		//return super.comment(runtime, builder, table);
		//单独添加备注
		return builder;
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, Table table){
		return super.name(runtime, builder, table);
	}


	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, View view);
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, View view);
	 * List<Run> buildAlterRun(DataRuntime runtime, View view);
	 * List<Run> buildRenameRun(DataRuntime runtime, View view);
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, View view);
	 * List<Run> buildDropRun(DataRuntime runtime, View view);
	 * StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, View view)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view)
	 * StringBuilder name(DataRuntime runtime, StringBuilder builder, View view)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception{
		return super.buildCreateRun(runtime, view);
	}

	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception{
		return super.buildAppendCommentRun(runtime, view);
	}


	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception{
		return super.buildAlterRun(runtime, view);
	}
	/**
	 * 修改视图名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param view 视图
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception{
		return super.buildRenameRun(runtime, view);
	}

	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception{
		return super.buildChangeCommentRun(runtime, view);
	}
	/**
	 * 删除视图
	 * @param view 视图
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception{
		return super.buildDropRun(runtime, view);
	}

	/**
	 * 创建或删除视图时检测视图是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkViewExists(runtime, builder, exists);
	}

	/**
	 * 备注 不支持创建视图时带备注的 在子视图中忽略
	 * @param builder builder
	 * @param view 视图
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view){
		return super.comment(runtime, builder, view);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, MasterTable table);
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAlterRun(DataRuntime runtime, MasterTable table);
	 * List<Run> buildDropRun(DataRuntime runtime, MasterTable table);
	 * List<Run> buildRenameRun(DataRuntime runtime, MasterTable table);
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildDropRun(runtime, table);
	}
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildRenameRun(runtime, table);
	}
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildChangeCommentRun(runtime, table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table);
	 * List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table);
	 * List<Run> buildDropRun(DataRuntime runtime, PartitionTable table);
	 * List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table);
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildDropRun(runtime, table);
	}
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildRenameRun(runtime, table);
	}
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildChangeCommentRun(runtime, table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * String alterColumnKeyword(DataRuntime runtime)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column)
	 * List<Run> buildRenameRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Column column)
	 * StringBuilder define(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column)
	 * boolean isIgnorePrecision(DataRuntime runtime, Column column);
	 * boolean isIgnoreScale(DataRuntime runtime, Column column);
	 * Boolean checkIgnorePrecision(DataRuntime runtime, String datatype);
	 * Boolean checkIgnoreScale(DataRuntime runtime, String datatype);
	 * StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/
	@Override
	public String alterColumnKeyword(DataRuntime runtime){
		return super.alterColumnKeyword(runtime);
	}

	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice) throws Exception{
		return super.buildAddRun(runtime, column, slice);
	}
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column column) throws Exception{
		return buildAddRun(runtime, column, false);
	}


	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sqls
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice) throws Exception{
		return super.buildAlterRun(runtime, column, slice);
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column column) throws Exception{
		return buildAlterRun(runtime, column, false);
	}

	

	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice) throws Exception{
		return super.buildDropRun(runtime, column, slice);
	}

	/**
	 * 修改列名
	 * ALTER TABLE T  RENAME  A  to B ;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column column) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, column.getTable(true));
		builder.append(" RENAME ").append(column.getName()).append(" TO ").append(column.getUpdate().getName());
 		column.setName(column.getUpdate().getName());
		return runs;
	}

	/**
	 * alter table T alter column C type varchar(64);
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Column update = column.getUpdate();
		builder.append("ALTER TABLE ");
		name(runtime, builder, column.getTable(true));
		builder.append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" TYPE ");
		type(runtime, builder, update);
		String type = update.getTypeName();
		if(type.contains("(")){
			type = type.substring(0,type.indexOf("("));
		}
		builder.append(" USING ").append(column.getName()).append("::").append(type);
		return runs;
	}


	/**
	 * 修改默认值
	 * ALTER TABLE T ALTER COLUMN C SET DEFAULT 0;
	 * ALTER TABLE T ALTER COLUMN C DROP DEFAULT;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Object def = null;
		if(null != column.getUpdate()){
			def = column.getUpdate().getDefaultValue();
		}else {
			def = column.getDefaultValue();
		}
		if(null != def){
			String str = def.toString();
			if(str.contains("::")){
				str = str.split("::")[0];
			}
			str = str.replace("'","");
			def = str;
		}

		builder.append("ALTER TABLE ");
		name(runtime, builder, column.getTable(true)).append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		if(null != def){
			builder.append(" SET DEFAULT '").append(def).append("'");
		}else{
			builder.append(" DROP DEFAULT");
		}
		return runs;
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME DROP NOT NULL
	 * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME SET NOT NULL
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		int nullable = column.isNullable();
		int uNullable = column.getUpdate().isNullable();
		if(nullable != -1 && uNullable != -1){
			if(nullable != uNullable) {
				builder.append("ALTER TABLE ");
				name(runtime, builder, column.getTable(true)).append(" ALTER ");
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
				if (uNullable == 0) {
					builder.append(" SET ");
				} else {
					builder.append(" DROP ");
				}
				builder.append(" NOT NULL");
				column.setNullable(uNullable);
			}
 		}
		return runs;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column column) throws Exception {
		return buildChangeCommentRun(runtime, column);
	}
	/**
	 * 修改备注
	 * COMMENT ON COLUMN T.ID IS 'ABC'
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		String comment = null;
		Column update = column.getUpdate();
		if(null != update){
			comment = update.getComment();
		}
		if(BasicUtil.isEmpty(comment)){
			comment = column.getComment();
		}
		if(BasicUtil.isNotEmpty(comment)) {
			builder.append("COMMENT ON COLUMN ");
			name(runtime, builder, column.getTable(true)).append(".");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			builder.append(" IS '").append(comment).append("'");
 		}
		return runs;
	}



	/**
	 * 取消自增
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column) throws Exception{
		return super.buildDropAutoIncrement(runtime, column);
	}
	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(DataRuntime runtime, StringBuilder builder, Column column){
		// 如果有递增列 通过数据类型实现
		if(column.isAutoIncrement() == 1){
			String type = column.getTypeName().toLowerCase();
			if ("int4".equals(type) || "int".equals(type) || "integer".equals(type)) {
				column.setType("SERIAL4");
			} else if ("int8".equals(type) || "long".equals(type) || "bigint".equals(type)) {
				column.setType("SERIAL8");
			} else if ("int2".equals(type) || "smallint".equals(type) || "short".equals(type)) {
				//9.2.0
				column.setType("SERIAL2");
			}else{
				column.setType("SERIAL8");
			}
		}
		return super.define(runtime, builder, column);
	}
	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column){
		return super.type(runtime, builder, column);
	}

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column){
		return super.nullable(runtime, builder, column);
	}
	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column){
		return super.charset(runtime, builder, column);
	}
	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column){
		return super.defaultValue(runtime, builder, column);
	}
	/**
	 * 递增列
	 * 通过数据类型实现,这里不需要
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column){
		return builder;
	}




	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column){
		return super.onupdate(runtime, builder, column);
	}

	/**
	 * 位置
	 *
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column){
		return super.position(runtime, builder, column);
	}
	/**
	 * 备注
	 *
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column){
		//return super.comment(runtime, builder, column);
		//单独生成备注
		return builder;
	}

	/**
	 * 创建或删除列时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkColumnExists(runtime, builder, exists);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildDropRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag);
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag);
	 * StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildAddRun(runtime, tag);
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return sqls
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildAlterRun(runtime, tag);
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildDropRun(runtime, tag);
	}


	/**
	 * 修改标签名
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Tag tag)  throws Exception{
		return super.buildRenameRun(runtime, tag);
	}

	/**
	 * 修改默认值
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeDefaultRun(runtime, tag);
	}

	/**
	 * 修改非空限制
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeNullableRun(runtime, tag);
	}
	/**
	 * 修改备注
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeCommentRun(runtime, tag);
	}

	/**
	 * 修改数据类型
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeTypeRun(runtime, tag);
	}

	/**
	 * 创建或删除标签时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkTagExists(runtime, builder, exists);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 * List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 * List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Map<String,Column> columns = primary.getColumns();
		if(columns.size()>0) {
			builder.append("ALTER TABLE ");
			name(runtime, builder, primary.getTable(true));
			builder.append(" ADD PRIMARY KEY (");
			boolean first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
				first = false;
			}
			builder.append(")");

		}
		return runs;
	}
	/**
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param primary 主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		return super.buildAlterRun(runtime, primary);
	}

	/**
	 * 删除主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, primary.getTable(true));
		builder.append(" DROP CONSTRAINT ");
		SQLUtil.delimiter(builder, primary.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}
	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		return super.buildRenameRun(runtime, primary);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/

	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildAddRun(runtime, foreign);
	}
	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildAlterRun(runtime, foreign);
	}

	/**
	 * 删除外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildDropRun(runtime, foreign);
	}

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildRenameRun(runtime, foreign);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, Index index) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Index index) throws Exception
	 * List<Run> buildDropRun(DataRuntime runtime, Index index) throws Exception
	 * List<Run> buildRenameRun(DataRuntime runtime, Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildAddRun(runtime, index);
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildAlterRun(runtime, index);
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Index index) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(index.isPrimary()){
			log.info("[主键索引,忽略删除][index:{}]", index.getName());
		}else {
			builder.append("DROP INDEX ").append(index.getName());
		}
		return runs;
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildRenameRun(runtime, index);
	}
	/**
	 * 索引备注
	 * @param builder
	 * @param index
	 */
	public void comment(DataRuntime runtime, StringBuilder builder, Index index){
		super.comment(runtime, builder, index);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, Constraint constraint) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Constraint constraint) throws Exception
	 * List<Run> buildDropRun(DataRuntime runtime, Constraint constraint) throws Exception
	 * List<Run> buildRenameRun(DataRuntime runtime, Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildAddRun(runtime, constraint);
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildAlterRun(runtime, constraint);
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildDropRun(runtime, constraint);
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildRenameRun(runtime, constraint);
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildCreateRun(runtime, trigger);
	}
	public void each(DataRuntime runtime, StringBuilder builder, Trigger trigger){
		super.each(runtime, builder, trigger);
	}
	/**
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param trigger 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildAlterRun(runtime, trigger);
	}

	/**
	 * 删除触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildDropRun(runtime, trigger);
	}

	/**
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildRenameRun(runtime, trigger);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Procedure procedure) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildCreateRun(runtime, procedure);
	}

	/**
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param procedure 存储过程
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildAlterRun(runtime, procedure);
	}

	/**
	 * 删除存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildDropRun(runtime, procedure);
	}

	/**
	 * 修改存储过程名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildRenameRun(runtime, procedure);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Function function) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Function function) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Function function) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Function function) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 添加函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildCreateRun(runtime, function);
	}

	/**
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param function 函数
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildAlterRun(runtime, function);
	}

	/**
	 * 删除函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildDropRun(runtime, function);
	}

	/**
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildRenameRun(runtime, function);
	}


	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(DataRuntime runtime, Column column)
	 *  boolean isNumberColumn(DataRuntime runtime, Column column)
	 * boolean isCharColumn(DataRuntime runtime, Column column)
	 * String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value)
	 * String type(String type)
	 * String type2class(String type)
	 ******************************************************************************************************************/

	/**
	 * 是否是boolean类型
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isBooleanColumn(DataRuntime runtime, Column column) {
		return super.isBooleanColumn(runtime, column);
	}
	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public  boolean isNumberColumn(DataRuntime runtime, Column column){
		return super.isNumberColumn(runtime, column);
	}

	/**
	 * 是否是字符类型
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isCharColumn(DataRuntime runtime, Column column) {
		return super.isCharColumn(runtime, column);
	}
	/**
	 * 内置函数 多种数据库兼容时需要
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "now()";
		}
		return null;
	}



	/* *****************************************************************************************************************
	 *
	 * 														common
	 *
	 *  *****************************************************************************************************************/

	public PGobject value(String type, Object value) throws SQLException {
		PGobject pg = null;
		if(value instanceof PGobject) {
			pg =  (PGobject)value;
			if(!type.equals(pg.getType())){
				String val = pg.getValue();
				pg = new PGobject();
				pg.setType(type);
				pg.setValue(val);
			}
			return pg;
		}
		pg = new PGobject();
		pg.setType(type);
		if(null != value) {
			pg.setValue(value.toString());
		}
		return pg;
	}

}