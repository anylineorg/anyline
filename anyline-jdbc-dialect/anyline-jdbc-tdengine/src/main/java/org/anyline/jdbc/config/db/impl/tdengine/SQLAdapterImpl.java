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
			sql += " AND DB_NAME LIKE '" + pattern + "'";
		}
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " AND TABLE_NAME LIKE '" + pattern + "'";
		}
		sqls.add(sql);
		return sqls;
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
		sql = "SELECT * FROM INFORMATION_SCHEMA.INS_STABLES WHERE TYPE = 'NORMAL_TABLE' ";
		if (BasicUtil.isNotEmpty(catalog)) {
			sql += " AND DB_NAME LIKE '" + pattern + "'";
		}
		if (BasicUtil.isNotEmpty(pattern)) {
			sql += " AND TABLE_NAME LIKE '" + pattern + "'";
		}
		sqls.add(sql);
		return sqls;
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
		/*if(table instanceof STable){
			builder.append("SELECT DISTINCT STABLE_NAME,DB_NAME,TAG_NAME,TAG_TYPE FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
			builder.append(table.getCatalog()).append("' AND STABLE_NAME='").append(table.getName()).append("'");
		}else {
			builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
			builder.append(table.getCatalog()).append("' AND TABLE_NAME='").append(table.getName()).append("'");
		}*/
		builder.append("DESCRIBE ").append(table.getName());
		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param table table
	 * @param tags
	 * @param set set
	 * @return tags
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(int index, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception{
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			Tag item = new Tag();
			/*
			//查询 INFORMATION_SCHEMA.INS_TAGS
			String name = row.getString("TAG_NAME");
			tag.setName(name);
			tag.setTypeName(row.getString("TAG_TYPE"));
			tag.setValue(row.get("TAG_VALUE"));
			tags.put(name.toUpperCase(), tag);*/

			//DESCRIBE
			String note = row.getString("note");
			if(!"TAG".equalsIgnoreCase(note)){
				continue;
			}
			String name = row.getString("field");
			item.setName(name);
			item.setTypeName(row.getString("type"));
			item.setPrecision(row.getInt("length", 0));
			item.setValue(row.get("TAG_VALUE"));
			tags.put(name.toUpperCase(), item);
		}
		return tags;
	}

	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata){
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		/*if(table instanceof STable){
			builder.append("SELECT DISTINCT STABLE_NAME,DB_NAME,TAG_NAME,TAG_TYPE FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
			builder.append(table.getCatalog()).append("' AND STABLE_NAME='").append(table.getName()).append("'");
		}else {
			builder.append("SELECT * FROM INFORMATION_SCHEMA.INS_TAGS WHERE db_name = '");
			builder.append(table.getCatalog()).append("' AND TABLE_NAME='").append(table.getName()).append("'");
		}*/
		builder.append("DESCRIBE ").append(table.getName());
		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param table table
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			Column item = new Column();
			//DESCRIBE
			String note = row.getString("note");
			if("TAG".equalsIgnoreCase(note)){
				continue;
			}
			String name = row.getString("field");
			item.setName(name);
			item.setCatalog(table.getCatalog());
			item.setSchema(table.getSchema());
			item.setTypeName(row.getString("type"));
			item.setPrecision(row.getInt("length", 0));
			columns.put(name.toUpperCase(), item);
		}
		return columns;
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
	 * 修改列名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column) {
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
