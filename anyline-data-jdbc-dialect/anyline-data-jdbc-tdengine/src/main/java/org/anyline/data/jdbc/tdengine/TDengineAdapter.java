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


package org.anyline.data.jdbc.tdengine;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository("anyline.data.jdbc.adapter.tdengine")
public class TDengineAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	
	public DatabaseType type(){
		return DatabaseType.TDengine;
	}

	public TDengineAdapter(){
		delimiterFr = "`";
		delimiterTo = "`";
		for (TDengineColumnTypeAlias alias: TDengineColumnTypeAlias.values()){
			types.put(alias.name(), alias.standard());
		}
	}

	@Value("${anyline.data.jdbc.delimiter.tdengine:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}


	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	/**
	 * 构造 LIKE 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value
	 */
	@Override
	public Object createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value){
		if(null != value){
			if(value instanceof Collection){
				value = ((Collection)value).iterator().next();
			}
			if(compare == Compare.LIKE){
				builder.append(" LIKE '%").append(value).append("%'");
			}else if(compare == Compare.LIKE_PREFIX|| compare == Compare.START_WITH){
				builder.append(" LIKE'").append(value).append("%'");
			}else if(compare == Compare.LIKE_SUFFIX || compare == Compare.END_WITH){
				builder.append(" LIKE '%").append(value).append("'");
			}
		}
		return null;
	}
	/**
	 * 批量插入数据时,多行数据之间分隔符
	 * @return String
	 */
	@Override
	public String batchInsertSeparator (){
		return " ";
	}
	/**
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	@Override
	public boolean supportInsertPlaceholder (){
		return false;
	}

	/**
	 * 执行 insert
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data entity|DataRow|DataSet
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks 主键
	 * @return int 影响行数
	 * @throws Exception 异常
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Object data, Run run, String[] pks) {
		long cnt = 0;
		JdbcTemplate jdbc = jdbc(runtime);
		String sql = run.getFinalInsert();
		List<Object> values = run.getValues();
		if(null == values || values.size() ==0) {
			cnt = jdbc.update(sql);
		}else{
			cnt = jdbc.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws java.sql.SQLException {
					PreparedStatement ps = null;
					/*if(null != pks && pks.length>0){
						ps = con.prepareStatement(sql, pks);
					}else {
						ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					}*/
					ps = con.prepareStatement(sql);
					int idx = 0;
					if (null != values) {
						for (Object obj : values) {
							ps.setObject(++idx, obj);
						}
					}
					return ps;
				}
			});
			return cnt;
		}

		return cnt;
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
			sql += " LIMIT " + navi.getFirstRow() + "," + limit;
		}
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
	public String concat(DataRuntime runtime, String ... args){
		return concatFun(args);
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

		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();

		builder.append("SHOW TABLES");
		if (BasicUtil.isNotEmpty(pattern)) {
			builder.append(" LIKE '" + pattern + "'");
		}
		Run r = new SimpleRun();
		runs.add(r);
		builder = r.getBuilder();
		builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TABLES WHERE TYPE = 'NORMAL_TABLE' ");
		if (BasicUtil.isNotEmpty(catalog)) {
			builder.append(" AND DB_NAME = '" + catalog + "'");
		}
		if (BasicUtil.isNotEmpty(pattern)) {
			builder.append( " AND TABLE_NAME LIKE '" + pattern + "'");
		}
		return runs;
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
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		if(index == 0){
			// SHOW TABLES 只返回一列stable_name
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = (T)new MasterTable(name);
						tables.put(name.toUpperCase(), table);
					}else{
						continue;
					}
				}
			}
		}else if(index == 1){
			// SELECT * FROM INFORMATION_SCHEMA.INS_TABLES
			// table_name   | db_name|create_time            |columns |stable_name    |uid                |vgroup_id        |     ttl     |         table_comment          |         type          |
			// a_test       | simple  2022-09-19 11:08:46.512|3       | NULL          |657579901363175104 |           2     |           0 | NULL                           | NORMAL_TABLE          |

			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = (T)new MasterTable(name);
						tables.put(name.toUpperCase(), table);
					}else{
						continue;
					}
				}
				table.setCatalog(row.getString("db_name"));
				table.setType(row.getString("type"));
				table.setComment(row.getString("table_comment"));
			}
		}
		return tables;
	}

	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, List<T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new ArrayList<>();
		}
		if(index == 0){
			// SHOW TABLES 只返回一列stable_name
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T table = table(tables, catalog, schema, name);
				if(null == table){
					if(create) {
						table = (T)new MasterTable(name);
						tables.add(table);
					}
				}
			}
		}else if(index == 1){
			// SELECT * FROM INFORMATION_SCHEMA.INS_TABLES
			// table_name   | db_name|create_time            |columns |stable_name    |uid                |vgroup_id        |     ttl     |         table_comment          |         type          |
			// a_test       | simple  2022-09-19 11:08:46.512|3       | NULL          |657579901363175104 |           2     |           0 | NULL                           | NORMAL_TABLE          |

			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				boolean contains = true;
				T table = table(tables, catalog, schema, name);
				if(null == table){
					if(create) {
						table = (T)new MasterTable(name);
						contains = false;
					}else{
						continue;
					}
				}
				table.setCatalog(row.getString("db_name"));
				table.setType(row.getString("type"));
				table.setComment(row.getString("table_comment"));
				if(!contains){
					tables.add(table);
				}
			}
		}
		return tables;
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(runtime, create, tables, catalog, schema, pattern, types);
	}

	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
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
		List<Run> runs = new ArrayList<>();
		String sql = "SHOW STABLES";
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " LIKE '" + pattern + "'";
		}
		runs.add(new SimpleRun(sql));

		sql = "SELECT * FROM INFORMATION_SCHEMA.INS_STABLES WHERE 1=1 ";
		if (BasicUtil.isNotEmpty(catalog)) {
			sql += " AND DB_NAME = '" + catalog + "'";
		}
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " AND STABLE_NAME LIKE '" + pattern + "'";
		}
		runs.add(new SimpleRun(sql));
		return runs;
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
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
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
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		if(index == 0){
			// SHOW STABLES 只返回一列stable_name
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = (T)new MasterTable(name);
						tables.put(name.toUpperCase(), table);
					}else{
						continue;
					}
				}
			}
		}else if(index == 1){
			// SELECT * FROM INFORMATION_SCHEMA.INS_STABLES
			// stable_name  |db_name|create_time            |columns|tags|last_update           |table_comment|watermark  |max_delay|rollup|
			// meters       |simple |yyyy-MM-dd HH:mm:ss.SSS|4      |2   |yyyy-MM-dd HH:mm:ss.SS|NULL         |5000a,5000a|-1a,-1a  |      |
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = (T)new MasterTable(name);
						tables.put(name.toUpperCase(), table);
					}else{
						continue;
					}
				}
				table.setCatalog(row.getString("db_name"));
				table.setComment(row.getString("table_comment"));
			}
		}
		return tables;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types);
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name);
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags);
	 * <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;
	 * <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, MasterTable master) throws Exception;
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
	/**
	 * 根据主表查询分区表
	 *   先根据INS_TAGS查出表名(标签值交集) 再根据INS_TABLES补充其他信息
	 * @param master 主表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name) throws Exception{
		List<Run> runs = new ArrayList<>();

		String stable = null;
		if(null != master) {
			stable = master.getName();
		}
		//where 不支持子查询
		/*builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TABLES WHERE STABLE_NAME = '").append(stable).append("' AND TYPE='CHILD_TABLE'");
		if(null != tags && tags.size()>0){
			for(String key:tags.keySet()){
				Object value = tags.get(key);
				builder.append(" AND table_name IN(SELECT table_name FROM INFORMATION_SCHEMA.INS_TAGS WHERE stable_name = '").append(stable).append("'")
						.append(" AND TAG_NAME ='").append(key.toLowerCase()).append("'");
						if(null == value){
							builder.append(" AND TAG_VALUE IS NULL");
						}else{
							if(BasicUtil.isNumber(value)){
								builder.append(" AND TAG_VALUE = ").append(value);
							}else{
								builder.append(" AND TAG_VALUE = '").append(value).append("'");
							}
						}
						builder.append(")");
			}
		}*/
		//根据tag查询出表名

		if(null != tags && tags.size()>0){
			for(String key:tags.keySet()){
				Run run = new SimpleRun();
				runs.add(run);
				StringBuilder builder = run.getBuilder();
				Object value = tags.get(key);
				builder.append("SELECT table_name FROM INFORMATION_SCHEMA.INS_TAGS WHERE STABLE_NAME = '").append(stable).append("'")
						.append(" AND TAG_NAME ='").append(key.toLowerCase()).append("'")
						.append(" AND TAG_VALUE ");

				if(null == value){
					builder.append("IS NULL");
				}else{
					if(BasicUtil.isNumber(value)){
						builder.append("= ").append(value);
					}else{
						builder.append("= '").append(value).append("'");
					}
				}
 			}
		}
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TABLES WHERE STABLE_NAME = '").append(stable).append("'");
		if(BasicUtil.isNotEmpty(name)){
			builder.append(" AND TABLE_NAME = '").append(name).append("'");
		}
		return runs;
	}

	@Override
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags) throws Exception{
		return buildQueryPartitionTableRun(runtime, master, tags, null);
	}

	/**
	 *  根据查询结果集构造分区表
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags) 返回顺序
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
		if(index == 0){
			tables = new LinkedHashMap<>();
			for(DataRow row:set){
				String name = row.getString("table_name");
				T table = (T)new PartitionTable(name);
				tables.put(name, table); //不要转大写 下一步需要交集
			}
		}else if(index < total -1){
			//与此之前的集合求交集
			tables.keySet().retainAll(set.getStrings("table_name"));
		}
		if(index == total -1){
			//最后一步 补充详细信息
			LinkedHashMap<String, T> result = new LinkedHashMap<>();
			for (DataRow row:set){
				String name = row.getString("table_name");
				if(!tables.containsKey(name)){
					continue;
				}
				T table = (T)new PartitionTable(name);
				result.put(name.toUpperCase(), table);
				table.setCatalog(row.getString("db_name"));
				table.setComment(row.getString("table_comment"));
				table.setMaster(master);
				result.put(name, table);
			}
			tables.clear();
			for(String key:result.keySet()){
				tables.put(key.toUpperCase(), result.get(key));
			}
		}
		return tables;
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
		}else {
			builder.append("DESCRIBE ").append(table.getName());
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
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		// DESCRIBE
		for(DataRow row:set){
			String name = row.getString("field");
			String note = row.getString("note");
			if(BasicUtil.isEmpty(name)){
				continue;
			}
			if("TAG".equalsIgnoreCase(note)){
				continue;
			}
			T column = columns.get(name.toUpperCase());
			if(null == column){
				if(create){
					column = (T)new Column();
					columns.put(name.toUpperCase(), column);
				}else{
					continue;
				}
			}
			column.setName(name);
			column.setCatalog(table.getCatalog());
			column.setSchema(table.getSchema());
			column.setTypeName(row.getString("type"));
			column.setPrecision(row.getInt("length", 0));
			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
		}
		return columns;
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		return super.columns(runtime, create, columns, table, set);
	}

	/**
	 * 根据驱动内置接口
	 * 会返回TAG,所以上一步未查到的,不需要新创建
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
		return super.columns(runtime, false, columns, table, pattern);
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
	 * 查询标签
	 *  select * from INFORMATION_SCHEMA.INS_TAGS WHERE db_name = 'simple' AND table_name = '';
	 *  table_name ,db_name,stable_name,tag_name,tag_type,tag_value
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		List<Run> runs = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(runtime, builder, table);
			builder.append(" WHERE 1=0");
			runs.add(new SimpleRun(builder));
		}else {
			if (table instanceof MasterTable) {
				builder.append("SELECT DISTINCT STABLE_NAME,DB_NAME,TAG_NAME,TAG_TYPE FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
				builder.append(table.getCatalog()).append("' AND STABLE_NAME='").append(table.getName()).append("'");
			} else {
				builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
				builder.append(table.getCatalog()).append("' AND TABLE_NAME='").append(table.getName()).append("'");
			}
			runs.add(new SimpleRun(builder));
			builder = new StringBuilder();
			builder.append("DESCRIBE ").append(table.getName());
			runs.add(new SimpleRun(builder));
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception{
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		if(index ==0){
			// 查询 INFORMATION_SCHEMA.INS_TAGS
			for(DataRow row:set) {
				String name = row.getString("TAG_NAME");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T item = tags.get(name.toUpperCase());
				if(null == item){
					if(create){
						item = (T)new Tag();
					}else{
						continue;
					}
				}
				item.setName(name);
				item.setTypeName(row.getString("TAG_TYPE"));
				item.setValue(row.get("TAG_VALUE"));
				tags.put(name.toUpperCase(), item);
			}
		}else if(index ==1){
			// DESCRIBE
			for(DataRow row:set){
				String note = row.getString("note");
				if(!"TAG".equalsIgnoreCase(note)){
					continue;
				}
				String name = row.getString("field");

				if(BasicUtil.isEmpty(name)){
					continue;
				}
				T item = tags.get(name.toUpperCase());
				if(null == item){
					if(create){
						item = (T)new Tag();
					}else{
						continue;
					}
				}
				item.setName(name);
				item.setTypeName(row.getString("type"));
				item.setPrecision(row.getInt("length", 0));
				item.setValue(row.get("TAG_VALUE"));
				tags.put(name.toUpperCase(), item);
			}
		}
		return tags;
	}

	/**
	 * 根据metadata 解析
	 * 没有相应接口,不需要实现
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception{
		//td jdbc 不支持1=0(server:3.0.7.1 driver:3.2.4)
		//return super.tags(runtime, create, table, tags, set);
		return tags;
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception{
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
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
	 * @return indexs indexs
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
	 * @return constraints constraints
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
		return super.buildQueryTriggerRun(runtime, table, events);
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
		return super.triggers(runtime, index, create, table, triggers, set);
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
	 * List<Run> buildCreateRun(DataRuntime runtime, Table table);
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table table);
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns);
	 * List<Run> buildRenameRun(DataRuntime runtime, Table table);
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table table);
	 * List<Run> buildDropRun(DataRuntime runtime, Table table);
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder name(DataRuntime runtime, StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}
	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table table) throws Exception {
		return super.buildAppendCommentRun(runtime, table);
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
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildRenameRun(runtime, table);
	}

	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildChangeCommentRun(runtime, table);
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
	@Override
	public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkTableExists(runtime, builder, exists);
	}


	/**
	 * 主键
	 * 不需要显式创建 第一列默认主键
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table){
		return builder;
	}

	/**
	 * 备注
	 * 不支持
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table){
		//log.warn("主表不支持comment");
		/*String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			builder.append(" comment '").append(comment).append("'");
		}*/
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
	 * List<Run> buildCreateRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAlterRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildDropRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildRenameRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table)
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Table tab = table;
 		List<Run> list = buildCreateRun(runtime, tab);
		 int size = list.size();
		 for(int i=0; i<size; i++){
			 Run sql = list.get(i);
			 if(i ==0){
				 StringBuilder builder = new StringBuilder();
				 builder.append(sql.getFinalUpdate());
				 builder.append(" TAGS (");
				 LinkedHashMap<String,Tag> tags = table.getTags();
				 int idx = 0;
				 for(Tag tag:tags.values()){
					 if(idx > 0){
						 builder.append(",");
					 }
					 SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
					 type(runtime, builder, tag);
					 //不支持comment sever:3.0.7.1 driver:3.2.4
					 //comment(builder, tag);
					 idx ++;
				 }
				 builder.append(")");
				 runs.add(new SimpleRun(builder));
			 }else{
				 runs.add(sql);
			 }
		 }
		return runs;
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable table) throws Exception{
		Table tab = table;
		return super.buildDropRun(runtime, tab);
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		String stable = table.getMasterName();
		if(BasicUtil.isEmpty(stable)){
			throw new SQLException("未指定主表");
		}
		Table tab = table;
		builder.append(super.buildCreateRun(runtime, tab).get(0).getFinalUpdate());
		builder.append(" USING ");
		SQLUtil.delimiter(builder, stable, getDelimiterFr(), getDelimiterTo());
		builder.append("(");
		Collection<Tag> tags = table.getTags().values();
		int idx = 0;
		for(Tag tag:tags){
			if(idx > 0){
				builder.append(",");
			}
			SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo());
			idx ++;
		}
		builder.append(") TAGS (");
		idx = 0;
		for(Tag tag:tags){
			if(idx > 0){
				builder.append(",");
			}
			//format(builder, tag.getValue());
			builder.append(write(runtime,null, tag.getValue(), false));
			idx ++;
		}
		builder.append(")");
		return runs;
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception{
		Table tab = table;
		return super.buildDropRun(runtime, tab);
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
		return "ALTER";
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
	 * @return List
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
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column column) throws Exception{
		return super.buildRenameRun(runtime, column);
	}


	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param column 列
	 * @return sql
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column column) throws Exception{
		return super.buildChangeTypeRun(runtime, column);
	}
	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column) throws Exception{
		return super.buildChangeDefaultRun(runtime, column);
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column column) throws Exception{
		return super.buildChangeNullableRun(runtime, column);
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
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column column) throws Exception{
		return super.buildChangeCommentRun(runtime, column);
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
		if(null == builder){
			builder = new StringBuilder();
		}
		String typeName = column.getTypeName();
		if(BasicUtil.isEmpty(typeName)){
			return builder;
		}
		builder.append(typeName);
		if(typeName.equalsIgnoreCase("NCHAR")){
			// 精度
			Integer precision = column.getPrecision();
			if(null != precision) {
				if (precision > 0) {
					builder.append("(").append(precision).append(")");
				}
			}
		}
		return builder;
	}

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column){
		/*int nullable = column.isNullable();
		if(nullable != -1) {
			if (nullable == 0) {
				builder.append(" NOT");
			}
			builder.append(" NULL");
		}*/
		return builder;
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
	 * 不支持
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
	 * 不支持
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column){
		return builder;
	}

	/**
	 * 位置
	 * 不支持
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column){
		return builder;
	}
	/**
	 * 备注
	 * 子类实现
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column){
		if(column instanceof Tag) {
			String comment = column.getComment();
			if (BasicUtil.isNotEmpty(comment)) {
				builder.append(" COMMENT '").append(comment).append("'");
			}
			return builder;
		}else{
			// 列不支持备注
			return null;
		}
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
	 * @return List
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
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildRenameRun(runtime, tag);
	}

	/**
	 * 修改默认值
	 * 子类实现
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
	 * 子类实现
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
	 * 子类实现
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
	 * 子类实现
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
		return super.buildAddRun(runtime, primary);
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
		return super.buildDropRun(runtime, primary);
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
	public List<Run> buildAddRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildAddRun(runtime, index);
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildAlterRun(runtime, index);
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildDropRun(runtime, index);
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param index 索引
	 * @return String
	 */
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
	public List<Run> buildAddRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildAddRun(runtime, constraint);
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return buildAlterRun(runtime, constraint);
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildDropRun(runtime, constraint);
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
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

	@Override
	public boolean isCharColumn(DataRuntime runtime, Column column) {
		return super.isCharColumn(runtime, column);
	}
	/**
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "NOW";
		}
		return null;
	}


}
