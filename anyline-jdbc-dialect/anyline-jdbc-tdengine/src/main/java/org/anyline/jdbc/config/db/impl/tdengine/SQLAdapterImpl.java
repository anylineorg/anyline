package org.anyline.jdbc.config.db.impl.tdengine;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.SQLAdapter;
import org.anyline.jdbc.config.db.impl.BasicSQLAdapter;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.LogUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Repository("anyline.jdbc.sql.adapter.tdengine")
public class SQLAdapterImpl extends BasicSQLAdapter implements SQLAdapter, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.TDengine;
	}

	public SQLAdapterImpl(){
		delimiterFr = "`";
		delimiterTo = "`";
	}

	@Value("${anyline.jdbc.delimiter.tdengine:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet() throws Exception {
		setDelimiter(delimiter);
	}


	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	@Override 
	public String parseFinalQueryTxt(RunSQL run){ 
		String sql = run.getBaseQueryTxt(); 
		String cols = run.getFetchColumns(); 
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
			int limit = navi.getLastRow() - navi.getFirstRow() + 1;
			if(limit < 0){
				limit = 0;
			}
			sql += " LIMIT " + navi.getFirstRow() + "," + limit;
		}
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
	public String concat(String ... args){
		return concatFun(args);
	}



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
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Table> tables(boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, ResultSet set) throws Exception;
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
	public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		List<String> sqls = new ArrayList<>();
		String sql = "SHOW TABLES";
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " LIKE '" + pattern + "'";
		}
		sqls.add(sql);
		sql = "SELECT * FROM INFORMATION_SCHEMA.INS_TABLES WHERE TYPE = 'NORMAL_TABLE' ";
		if (BasicUtil.isNotEmpty(catalog)) {
			sql += " AND DB_NAME = '" + catalog + "'";
		}
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " AND TABLE_NAME LIKE '" + pattern + "'";
		}
		sqls.add(sql);
		return sqls;
	}

	@Override
	public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		if(index == 0){
			//SHOW TABLES 只返回一列stable_name
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				Table table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = new MasterTable(name);
						tables.put(name.toUpperCase(), table);
					}else{
						continue;
					}
				}
			}
		}else if(index == 1){
			//SELECT * FROM INFORMATION_SCHEMA.INS_TABLES
			//table_name   | db_name|create_time            |columns |stable_name    |uid                |vgroup_id        |     ttl     |         table_comment          |         type          |
			//a_test       | simple  2022-09-19 11:08:46.512|3       | NULL          |657579901363175104 |           2     |           0 | NULL                           | NORMAL_TABLE          |

			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				Table table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = new MasterTable(name);
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
	public LinkedHashMap<String, Table> tables(boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, ResultSet set) throws Exception{
		return super.tables(create, catalog, schema, tables, set);
	}



	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, MasterTable> mtables(boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, ResultSet set) throws Exception;
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
	public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		List<String> sqls = new ArrayList<>();
		String sql = "SHOW STABLES";
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " LIKE '" + pattern + "'";
		}
		sqls.add(sql);

		sql = "SELECT * FROM INFORMATION_SCHEMA.INS_STABLES WHERE 1=1 ";
		if (BasicUtil.isNotEmpty(catalog)) {
			sql += " AND DB_NAME = '" + catalog + "'";
		}
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " AND STABLE_NAME LIKE '" + pattern + "'";
		}
		sqls.add(sql);
		return sqls;
	}
	/**
	 * 从jdbc结果中提取表结构
	 * ResultSet set = con.getMetaData().getTables()
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param set 查询结果
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, ResultSet set) throws Exception{
		return super.mtables(create, catalog, schema, tables, set);
	}



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
	@Override
	public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		if(index == 0){
			//SHOW STABLES 只返回一列stable_name
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				MasterTable table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = new MasterTable(name);
						tables.put(name.toUpperCase(), table);
					}else{
						continue;
					}
				}
			}
		}else if(index == 1){
			//SELECT * FROM INFORMATION_SCHEMA.INS_STABLES
			//stable_name  |db_name|create_time            |columns|tags|last_update           |table_comment|watermark  |max_delay|rollup|
			//meters       |simple |yyyy-MM-dd HH:mm:ss.SSS|4      |2   |yyyy-MM-dd HH:mm:ss.SS|NULL         |5000a,5000a|-1a,-1a  |      |
			for(DataRow row:set){
				String name = row.getString("stable_name");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				MasterTable table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = new MasterTable(name);
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
	 * public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master);
	 * public LinkedHashMap<String, PartitionTable> ptables(int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable master, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception;
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
	public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryPartitionTableRunSQL(catalog, schema, pattern, types);
	}
	/**
	 * 根据主表查询分区表
	 * @param master 主表
	 * @return List
	 */
	@Override
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master) throws Exception{
		List<String> sqls = new ArrayList<>();
		String sql = "SELECT * FROM INFORMATION_SCHEMA.INS_TABLES WHERE STABLE_NAME = '"+master.getName()+"' AND TYPE='CHILD_TABLE'";
		sqls.add(sql);
		return sqls;
	}


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
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception{
		return super.ptables(index, create, master, catalog, schema, tables, set);
	}

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
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable master, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception{
		return ptables(create, catalog, master, schema, tables, set);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryColumnRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, ResultSet set) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(builder, table);
			builder.append(" WHERE 1=0");
			sqls.add(builder.toString());
		}else {
			builder = new StringBuilder();
			builder.append("DESCRIBE ").append(table.getName());
			sqls.add(builder.toString());
		}
		return sqls;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		//DESCRIBE
		for(DataRow row:set){
			String name = row.getString("field");
			String note = row.getString("note");
			if(BasicUtil.isEmpty(name)){
				continue;
			}
			if("TAG".equalsIgnoreCase(note)){
				continue;
			}
			Column column = columns.get(name.toUpperCase());
			if(null == column){
				if(create){
					column = new Column();
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
		}
		return columns;
	}
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, SqlRowSet set) throws Exception{
		return super.columns(create, table, columns, set);
	}

	/**
	 * 根据JDBC接口
	 * 会返回TAG，所以上一步未查到的，不需要新创建
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param columns columns
	 * @param set set
	 * @return columns columns
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, ResultSet set) throws Exception{
		return super.columns(false, table, columns, set);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTagRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, ResultSet set) throws Exception;
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
	public List<String> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(builder, table);
			builder.append(" WHERE 1=0");
			sqls.add(builder.toString());
		}else {
			if (table instanceof MasterTable) {
				builder.append("SELECT DISTINCT STABLE_NAME,DB_NAME,TAG_NAME,TAG_TYPE FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
				builder.append(table.getCatalog()).append("' AND STABLE_NAME='").append(table.getName()).append("'");
			} else {
				builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
				builder.append(table.getCatalog()).append("' AND TABLE_NAME='").append(table.getName()).append("'");
			}
			sqls.add(builder.toString());

			builder = new StringBuilder();
			builder.append("DESCRIBE ").append(table.getName());
			sqls.add(builder.toString());
		}
		return sqls;
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception{
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		if(index ==0){
			//查询 INFORMATION_SCHEMA.INS_TAGS
			for(DataRow row:set) {
				String name = row.getString("TAG_NAME");
				if(BasicUtil.isEmpty(name)){
					continue;
				}
				Tag item = tags.get(name.toUpperCase());
				if(null == item){
					if(create){
						item = new Tag();
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
			//DESCRIBE
			for(DataRow row:set){
				String note = row.getString("note");
				if(!"TAG".equalsIgnoreCase(note)){
					continue;
				}
				String name = row.getString("field");

				if(BasicUtil.isEmpty(name)){
					continue;
				}
				Tag item = tags.get(name.toUpperCase());
				if(null == item){
					if(create){
						item = new Tag();
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
	 * 没有相应接口，不需要实现
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception{
		return super.tags(create, table, tags, set);
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, ResultSet set) throws Exception{
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryIndexRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, ResultSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列 
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryIndexRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryIndexRunSQL(table, metadata);
	}

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
	@Override
	public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception{
		return super.indexs(index, create, table, indexs, set);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception{
		return super.indexs(create, table, indexs, set);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, ResultSet set) throws Exception{
		return super.indexs(create, table, indexs, set);
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryConstraintRunSQL(table, metadata);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Constraint> constraints(int index , boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception{

		return super.constraints(index, create, table, constraints, set);
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception{
		return super.constraints(create, table, constraints, set);
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception{
		return super.constraints(create, table, constraints, set);
	}






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
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(Table table);
	 * public String buildAlterRunSQL(Table table);
	 * public String buildRenameRunSQL(Table table);
	 * public String buildChangeCommentRunSQL(Table table);
	 * public String buildDropRunSQL(Table table);
	 * public StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * public StringBuilder primary(StringBuilder builder, Table table)
	 * public StringBuilder comment(StringBuilder builder, Table table)
	 * public StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public String buildCreateRunSQL(Table table) throws Exception{
		return super.buildCreateRunSQL(table);
	}
	@Override
	public String buildAlterRunSQL(Table table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	/**
	 * 修改表名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) throws Exception{
		return super.buildRenameRunSQL(table);
	}

	@Override
	public String buildChangeCommentRunSQL(Table table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Table table) throws Exception{
		return super.buildDropRunSQL(table);
	}



	/**
	 * 创建之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists){
		return super.checkTableExists(builder, exists);
	}


	/**
	 * 主键
	 * 不需要显式创建 第一列默认主键
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
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
	public StringBuilder comment(StringBuilder builder, Table table){
		log.warn("主表不支持comment");
		return builder;
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(StringBuilder builder, Table table){
		return super.name(builder, table);
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(MasterTable table);
	 * public String buildAlterRunSQL(MasterTable table);
	 * public String buildDropRunSQL(MasterTable table);
	 * public String buildRenameRunSQL(MasterTable table);
	 * public String buildChangeCommentRunSQL(MasterTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildCreateRunSQL(MasterTable table) throws Exception{
		StringBuilder builder = new StringBuilder();
		Table tab = table;
 		String sql = buildCreateRunSQL(tab);
		builder.append(sql);
		builder.append(" TAGS (");
		LinkedHashMap<String,Tag> tags = table.getTags();
		int idx = 0;
		for(Tag tag:tags.values()){
			if(idx > 0){
				builder.append(",");
			}
			SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			type(builder, tag);
			comment(builder, tag);
			idx ++;
		}
		builder.append(")");
		return builder.toString();
	}
	@Override
	public String buildAlterRunSQL(MasterTable table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	@Override
	public String buildDropRunSQL(MasterTable table) throws Exception{
		Table tab = table;
		return super.buildDropRunSQL(tab);
	}
	@Override
	public String buildRenameRunSQL(MasterTable table) throws Exception{
		return super.buildRenameRunSQL(table);
	}
	@Override
	public String buildChangeCommentRunSQL(MasterTable table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(PartitionTable table);
	 * public String buildAlterRunSQL(PartitionTable table);
	 * public String buildDropRunSQL(PartitionTable table);
	 * public String buildRenameRunSQL(PartitionTable table);
	 * public String buildChangeCommentRunSQL(PartitionTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildCreateRunSQL(PartitionTable table) throws Exception{
		StringBuilder builder = new StringBuilder();
		String stable = table.getMasterName();
		if(BasicUtil.isEmpty(stable)){
			throw new SQLException("未指定主表");
		}
		Table tab = table;
		builder.append(super.buildCreateRunSQL(tab));
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
			format(builder, tag.getValue());
			idx ++;
		}
		builder.append(")");

		return builder.toString();
	}
	@Override
	public String buildAlterRunSQL(PartitionTable table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	@Override
	public String buildDropRunSQL(PartitionTable table) throws Exception{
		Table tab = table;
		return super.buildDropRunSQL(tab);
	}
	@Override
	public String buildRenameRunSQL(PartitionTable table) throws Exception{
		return super.buildRenameRunSQL(table);
	}
	@Override
	public String buildChangeCommentRunSQL(PartitionTable table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String alterColumnKeyword()
	 * public String buildAddRunSQL(Column column)
	 * public List<String> buildAlterRunSQL(Column column)
	 * public String buildDropRunSQL(Column column)
	 * public String buildRenameRunSQL(Column column)
	 * public List<String> buildChangeTypeRunSQL(Column column)
	 * public String buildChangeDefaultRunSQL(Column column)
	 * public String buildChangeNullableRunSQL(Column column)
	 * public String buildChangeCommentRunSQL(Column column)
	 * public StringBuilder define(StringBuilder builder, Column column)
	 * public StringBuilder type(StringBuilder builder, Column column)
	 * public StringBuilder nullable(StringBuilder builder, Column column)
	 * public StringBuilder charset(StringBuilder builder, Column column)
	 * public StringBuilder defaultValue(StringBuilder builder, Column column)
	 * public StringBuilder increment(StringBuilder builder, Column column)
	 * public StringBuilder onupdate(StringBuilder builder, Column column)
	 * public StringBuilder position(StringBuilder builder, Column column)
	 * public StringBuilder comment(StringBuilder builder, Column column)
	 ******************************************************************************************************************/
	@Override
	public String alterColumnKeyword(){
		return "ALTER";
	}

	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column) throws Exception{
		return super.buildAddRunSQL(column);
	}


	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Column column) throws Exception{
		return super.buildAlterRunSQL(column);
	}


	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Column column) throws Exception{
		return super.buildDropRunSQL(column);
	}

	/**
	 * 修改列名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column) throws Exception{
		return super.buildRenameRunSQL(column);
	}


	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Column column) throws Exception{
		return super.buildChangeTypeRunSQL(column);
	}
	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Column column) throws Exception{
		return super.buildChangeDefaultRunSQL(column);
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Column column) throws Exception{
		return super.buildChangeNullableRunSQL(column);
	}
	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Column column) throws Exception{
		return super.buildChangeCommentRunSQL(column);
	}




	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(StringBuilder builder, Column column){
		return super.define(builder, column);
	}
	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder type(StringBuilder builder, Column column){
		String typeName = column.getTypeName();
		if(BasicUtil.isEmpty(typeName)){
			return builder;
		}
		builder.append(typeName);
		if(typeName.equalsIgnoreCase("NCHAR")){
			//精度
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
	public StringBuilder nullable(StringBuilder builder, Column column){
		int nullable = column.isNullable();
		if(nullable != -1) {
			if (nullable == 0) {
				builder.append(" NOT");
			}
			builder.append(" NULL");
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
	public StringBuilder charset(StringBuilder builder, Column column){
		return super.charset(builder, column);
	}
	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder defaultValue(StringBuilder builder, Column column){
		return super.defaultValue(builder, column);
	}
	/**
	 * 递增列
	 * 不支持
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder increment(StringBuilder builder, Column column){
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
	public StringBuilder onupdate(StringBuilder builder, Column column){
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
	public StringBuilder position(StringBuilder builder, Column column){
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
	public StringBuilder comment(StringBuilder builder, Column column){
		if(column instanceof Tag) {
			String comment = column.getComment();
			if (BasicUtil.isNotEmpty(comment)) {
				builder.append(" COMMENT '").append(comment).append("'");
			}
			return builder;
		}else{
			//列不支持备注
			return null;
		}
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Tag tag);
	 * public List<String> buildAlterRunSQL(Tag tag);
	 * public String buildDropRunSQL(Tag tag);
	 * public String buildRenameRunSQL(Tag tag);
	 * public String buildChangeDefaultRunSQL(Tag tag);
	 * public String buildChangeNullableRunSQL(Tag tag);
	 * public String buildChangeCommentRunSQL(Tag tag);
	 * public List<String> buildChangeTypeRunSQL(Tag tag);
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Tag tag) throws Exception{
		return super.buildAddRunSQL(tag);
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Tag tag) throws Exception{
		return super.buildAlterRunSQL(tag);
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Tag tag) throws Exception{
		return buildDropRunSQL(tag);
	}


	/**
	 * 修改标签名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Tag tag) throws Exception{
		return super.buildRenameRunSQL(tag);
	}

	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Tag tag) throws Exception{
		return super.buildChangeDefaultRunSQL(tag);
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Tag tag) throws Exception{
		return super.buildChangeNullableRunSQL(tag);
	}
	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Tag tag) throws Exception{
		return super.buildChangeCommentRunSQL(tag);
	}

	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Tag tag) throws Exception{
		return super.buildChangeTypeRunSQL(tag);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Index index) throws Exception
	 * public List<String> buildAlterRunSQL(Index index) throws Exception
	 * public String buildDropRunSQL(Index index) throws Exception
	 * public String buildRenameRunSQL(Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	public String buildAddRunSQL(Index index) throws Exception{
		return super.buildAddRunSQL(index);
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Index index) throws Exception{
		return super.buildAlterRunSQL(index);
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	public String buildDropRunSQL(Index index) throws Exception{
		return super.buildDropRunSQL(index);
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	public String buildRenameRunSQL(Index index) throws Exception{
		return super.buildRenameRunSQL(index);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Constraint constraint) throws Exception
	 * public List<String> buildAlterRunSQL(Constraint constraint) throws Exception
	 * public String buildDropRunSQL(Constraint constraint) throws Exception
	 * public String buildRenameRunSQL(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	public String buildAddRunSQL(Constraint constraint) throws Exception{
		return super.buildAddRunSQL(constraint);
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Constraint constraint) throws Exception{
		return buildAlterRunSQL(constraint);
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	public String buildDropRunSQL(Constraint constraint) throws Exception{
		return super.buildDropRunSQL(constraint);
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	public String buildRenameRunSQL(Constraint constraint) throws Exception{
		return super.buildRenameRunSQL(constraint);
	}


	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * public boolean isBooleanColumn(Column column)
	 * public  boolean isNumberColumn(Column column)
	 * public boolean isCharColumn(Column column)
	 * public String buildInValue(SQL_BUILD_IN_VALUE value)
	 * public String type2type(String type)
	 * public String type2class(String type)
	 ******************************************************************************************************************/

	@Override
	public boolean isBooleanColumn(Column column) {
		return super.isBooleanColumn(column);
	}
	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public  boolean isNumberColumn(Column column){
		return super.isNumberColumn(column);
	}

	@Override
	public boolean isCharColumn(Column column) {
		return super.isCharColumn(column);
	}
	/**
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String buildInValue(SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "NOW";
		}
		return null;
	}

	@Override
	public String type2type(String type){
		return super.type2type(type);
	}
	@Override
	public String type2class(String type){
		return super.type2class(type);
	}











}
