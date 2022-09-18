package org.anyline.jdbc.config.db.impl.tdengine;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.SQLAdapter;
import org.anyline.jdbc.config.db.impl.BasicSQLAdapter;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.STable;
import org.anyline.jdbc.entity.Table;
import org.anyline.jdbc.entity.Tag;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
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
	 ******************************************************************************************************************/

	/**
	 *
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return List
	 */
	@Override
	public List<String> buildQuerySTableRunSQL(String catalog, String schema, String pattern, String types){
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
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照 buildQuerySTableRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, STable> stables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, STable> tables, DataSet set) throws Exception{
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
				STable table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = new STable(name);
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
				STable table = tables.get(name.toUpperCase());
				if(null == table){
					if(create) {
						table = new STable(name);
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

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables tables
	 * @param set set
	 * @return stables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, STable> stables(boolean create, String catalog, String schema, LinkedHashMap<String, STable> tables, ResultSet set) throws Exception{
		return tables;
	}


	/**
	 *
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return List
	 */
	@Override
	public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types){
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


	/**
	 * 根据JDBC
	 *
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables tables
	 * @param set set
	 * @return stables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Table> tables(boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, ResultSet set) throws Exception{
		return  super.tables(create, catalog, schema, null, set);
	}


	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata){
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
	 * @param table table
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index,boolean create,  Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
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
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, ResultSet set) throws Exception{
		//上一步中没有的不要新创建，这一步可能获取到tag
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		return columns;
	}

	/**
	 *
	 * 查询标签
	 *  select * from INFORMATION_SCHEMA.INS_TAGS WHERE db_name = 'simple' AND table_name = '';
	 *  table_name ,db_name,stable_name,tag_name,tag_type,tag_value
	 * @param table table
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryTagRunSQL(Table table, boolean metadata){
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(builder, table);
			builder.append(" WHERE 1=0");
			sqls.add(builder.toString());
		}else {
			if (table instanceof STable) {
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
	 *
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table table
	 * @param tags
	 * @param set set
	 * @return tags
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(int index,boolean create,  Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception{
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

	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 ******************************************************************************************************************/

	@Override
	public String buildCreateRunSQL(Table table){
		LinkedHashMap<String,Tag> tags = table.getTags();
		String sql = super.buildCreateRunSQL(table);
		if(table instanceof STable){
			//超表
			StringBuilder builder = new StringBuilder();
			builder.append(sql);
			builder.append(" TAGS (");
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
		return sql;
	}

	public StringBuilder fromSuperTable(StringBuilder builder, Table table){

		String stable = table.getStableName();
		if(BasicUtil.isNotEmpty(stable)){
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
		}
		return builder;
	}
	/**
	 * 修改表名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param table table
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) {
		return null;
	}
	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeDefaultRunSQL(Column column){
		return null;
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeNullableRunSQL(Column column){
		return null;
	}
	/**
	 * 修改备注
	 * 不支付
	 * @param column column
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Column column){
		return null;
	}


	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param table table
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Table table){
		return null;
	}
	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return sql
	 */
	public List<String> buildChangeTypeRunSQL(Column column){
		return null;
	}
	/**
	 * 更新行事件
	 * 子类实现
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder onupdate(StringBuilder builder, Column column){
		return builder;
	}
	/**
	 * 自增长列
	 * 子类实现
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder increment(StringBuilder builder, Column column){
		return builder;
	}

	/**
	 * 位置
	 * 子类实现
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder position(StringBuilder builder, Column column){
		return builder;
	}

	/**
	 * 主键
	 * 不需要显式创建 第一列默认主键
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		return builder;
	}
	/**
	 * 备注
	 * 子类实现
	 * @param builder builder
	 * @param column column
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

	/**
	 * 数据类型
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
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

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 ******************************************************************************************************************/

	/**
	 * 创建之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists){
		builder.append(" IF ");
		if(!exists){
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
		return builder;
	}
	/**
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	public String buildInValue(SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "NOW";
		}
		return null;
	}
}
