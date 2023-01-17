package org.anyline.data.jdbc.tdengine;

import org.anyline.data.entity.*;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.entity.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository("anyline.data.jdbc.adapter.tdengine")
public class TDengineAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.TDengine;
	}

	public TDengineAdapter(){
		delimiterFr = "`";
		delimiterTo = "`";
	}

	@Value("${anyline.jdbc.delimiter.tdengine:}")
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
	 * @param compare compare
	 * @param value value
	 * @return value
	 */
	@Override
	public Object buildConditionLike(StringBuilder builder, Compare compare, Object value){
		if(null != value){
			if(value instanceof Collection){
				value = ((Collection)value).iterator().next();
			}
			if(compare == Compare.LIKE){
				builder.append(" LIKE '%"+value+"%'");
			}else if(compare == Compare.LIKE_PREFIX|| compare == Compare.START_WITH){
				builder.append(" LIKE'"+value+"%'");
			}else if(compare == Compare.LIKE_SUFFIX || compare == Compare.END_WITH){
				builder.append(" LIKE '%"+value+"'");
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
	 * @param random random
	 * @param data entity|DataRow|DataSet
	 * @param sql sql
	 * @param values 占位参数值
	 * @return int 影响行数
	 * @throws Exception 异常
	 */
	@Override
	public int insert(String random, Object data, String sql, List<Object> values, String[] pks) throws Exception{
		int cnt = 0;
		if(null == values || values.size() ==0) {
			cnt = jdbc.update(sql);
		}else{
			cnt = jdbc.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws java.sql.SQLException {
					PreparedStatement ps = null;
					if(null != pks && pks.length>0){
						ps = con.prepareStatement(sql, pks);
					}else {
						ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					}
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
	public String parseFinalQuery(Run run){
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
	 * partition table	: 分区表
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
	 * public LinkedHashMap<String, Table> tables(boolean create, LinkedHashMap<String, Table> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception;
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
			// SHOW TABLES 只返回一列stable_name
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
			// SELECT * FROM INFORMATION_SCHEMA.INS_TABLES
			// table_name   | db_name|create_time            |columns |stable_name    |uid                |vgroup_id        |     ttl     |         table_comment          |         type          |
			// a_test       | simple  2022-09-19 11:08:46.512|3       | NULL          |657579901363175104 |           2     |           0 | NULL                           | NORMAL_TABLE          |

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
	public LinkedHashMap<String, Table> tables(boolean create, LinkedHashMap<String, Table> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(create, tables, dbmd, catalog, schema, pattern, types);
	}



	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, MasterTable> mtables(boolean create, LinkedHashMap<String, MasterTable> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception;
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
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param dbmd DatabaseMetaData
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean create, LinkedHashMap<String, MasterTable> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.mtables(create, tables, dbmd, catalog, schema, pattern, types);
	}



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
	@Override
	public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception{
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
			// SELECT * FROM INFORMATION_SCHEMA.INS_STABLES
			// stable_name  |db_name|create_time            |columns|tags|last_update           |table_comment|watermark  |max_delay|rollup|
			// meters       |simple |yyyy-MM-dd HH:mm:ss.SSS|4      |2   |yyyy-MM-dd HH:mm:ss.SS|NULL         |5000a,5000a|-1a,-1a  |      |
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
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name);
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags);
	 * public LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, PartitionTable> ptables(boolean create, LinkedHashMap<String, PartitionTable> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception;
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
	 *   先根据INS_TAGS查出表名(标签值交集) 再根据INS_TABLES补充其他信息
	 * @param master 主表
	 * @return List
	 */
	@Override
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = null;
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
				builder = new StringBuilder();
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
				sqls.add(builder.toString());
			}
		}
		builder = new StringBuilder();
		builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TABLES WHERE STABLE_NAME = '").append(stable).append("'");
		if(BasicUtil.isNotEmpty(name)){
			builder.append(" AND TABLE_NAME = '").append(name).append("'");
		}
		sqls.add(builder.toString());
		return sqls;
	}

	@Override
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags) throws Exception{
		return buildQueryPartitionTableRunSQL(master, tags, null);
	}

	/**
	 *  根据查询结果集构造分区表
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags) 返回顺序
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
	public LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception{
		if(index == 0){
			tables = new LinkedHashMap<>();
			for(DataRow row:set){
				String name = row.getString("table_name");
				PartitionTable table = new PartitionTable(name);
				tables.put(name, table); //不要转大写 下一步需要交集
			}
		}else if(index < total -1){
			//与此之前的集合求交集
			tables.keySet().retainAll(set.getStrings("table_name"));
		}
		if(index == total -1){
			//最后一步 补充详细信息
			LinkedHashMap<String, PartitionTable> result = new LinkedHashMap<>();
			for (DataRow row:set){
				String name = row.getString("table_name");
				if(!tables.containsKey(name)){
					continue;
				}
				PartitionTable table = new PartitionTable(name);
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
	 * @param dbmd DatabaseMetaData
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean create, LinkedHashMap<String, PartitionTable> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception{
		return super.ptables(create, tables, dbmd, catalog, schema, master);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryColumnRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
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
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
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
	 * 会返回TAG,所以上一步未查到的,不需要新创建
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param dbmd DatabaseMetaData
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		return super.columns(false, columns, dbmd, table, pattern);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTagRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Tag> tags(boolean create, LinkedHashMap<String, Tag> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
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
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception{
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
	 * 没有相应接口,不需要实现
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception{
		return super.tags(create, table, tags, set);
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, LinkedHashMap<String, Tag> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
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
	 * public LinkedHashMap<String, Index> indexs(boolean create, LinkedHashMap<String, Index> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception;
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
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
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
	public LinkedHashMap<String, Index> indexs(boolean create, LinkedHashMap<String, Index> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception{
		return super.indexs(create, indexs, dbmd, table, unique, approximate);
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
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Constraint> constraints(int index , boolean create, Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception{

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
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildCreateRunSQL(Table table);
	 * public String buildCreateCommentRunSQL(Table table);
	 * public List<String> buildAlterRunSQL(Table table);
	 * public String buildRenameRunSQL(Table table);
	 * public String buildChangeCommentRunSQL(Table table);
	 * public String buildDropRunSQL(Table table);
	 * public StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * public StringBuilder primary(StringBuilder builder, Table table)
	 * public StringBuilder comment(StringBuilder builder, Table table)
	 * public StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<String> buildCreateRunSQL(Table table) throws Exception{
		return super.buildCreateRunSQL(table);
	}
	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public String buildCreateCommentRunSQL(Table table) throws Exception {
		return super.buildCreateCommentRunSQL(table);
	}
	@Override
	public List<String> buildAlterRunSQL(Table table) throws Exception{
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
	public StringBuilder name(StringBuilder builder, Table table){
		return super.name(builder, table);
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildCreateRunSQL(MasterTable table)
	 * public String buildCreateCommentRunSQL(MasterTable table)
	 * public List<String> buildAlterRunSQL(MasterTable table)
	 * public String buildDropRunSQL(MasterTable table)
	 * public String buildRenameRunSQL(MasterTable table)
	 * public String buildChangeCommentRunSQL(MasterTable table)
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<String> buildCreateRunSQL(MasterTable table) throws Exception{
		List<String> sqls = new ArrayList<>();
		Table tab = table;
 		List<String> list = buildCreateRunSQL(tab);
		 int size = list.size();
		 for(int i=0; i<size; i++){
			 String sql = sqls.get(i);
			 if(i ==0){
				 StringBuilder builder = new StringBuilder();
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
				 sqls.add(builder.toString());
			 }else{
				 sqls.add(sql);
			 }
		 }
		return sqls;
	}
	@Override
	public List<String> buildAlterRunSQL(MasterTable table) throws Exception{
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
	 * public List<String> buildAlterRunSQL(PartitionTable table);
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
	public List<String> buildCreateRunSQL(PartitionTable table) throws Exception{
		List<String> sqls = new ArrayList<>();

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
		sqls.add(builder.toString());
		return sqls;
	}
	@Override
	public List<String> buildAlterRunSQL(PartitionTable table) throws Exception{
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
	 * public String buildCreateCommentRunSQL(Column column)
	 * public StringBuilder define(StringBuilder builder, Column column)
	 * public StringBuilder type(StringBuilder builder, Column column)
	 * public StringBuilder nullable(StringBuilder builder, Column column)
	 * public StringBuilder charset(StringBuilder builder, Column column)
	 * public StringBuilder defaultValue(StringBuilder builder, Column column)
	 * public StringBuilder increment(StringBuilder builder, Column column)
	 * public StringBuilder onupdate(StringBuilder builder, Column column)
	 * public StringBuilder position(StringBuilder builder, Column column)
	 * public StringBuilder comment(StringBuilder builder, Column column)
	 * public StringBuilder checkColumnExists(StringBuilder builder, boolean exists)
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
	public StringBuilder nullable(StringBuilder builder, Column column){
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
	public StringBuilder checkColumnExists(StringBuilder builder, boolean exists){
		return super.checkColumnExists(builder, exists);
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
	 * public StringBuilder checkTagExists(StringBuilder builder, boolean exists)
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
		return super.buildDropRunSQL(tag);
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

	/**
	 * 创建或删除标签时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkTagExists(StringBuilder builder, boolean exists){
		return super.checkTagExists(builder, exists);
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
