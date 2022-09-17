package org.anyline.jdbc.config.db.impl.mysql;

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
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.CheckedOutputStream;

@Repository("anyline.jdbc.sql.adapter.mysql")
public class SQLAdapterImpl extends BasicSQLAdapter implements SQLAdapter, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.MYSQL; 
	} 
	public SQLAdapterImpl(){
		delimiterFr = "`";
		delimiterTo = "`";
	}
	@Value("${anyline.jdbc.delimiter.mysql:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet() throws Exception {
		setDelimiter(delimiter);
	}

	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/
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

	/* *****************************************************************************************************
	 *
	 * 											metadata
	 *
	 * ****************************************************************************************************/

	/**
	 * 查询超表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types){
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();

		builder.append("SELECT * FROM information_schema.TABLES WHERE 1=1 ");
		//8.0版本中 这个表中 TABLE_CATALOG = def  TABLE_SCHEMA = 数据库名
		if(BasicUtil.isNotEmpty(catalog)){
			builder.append(" AND TABLE_SCHEMA = '").append(catalog).append("'");
		}
		/*if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND TABLE_SCHEMA = '").append(schema).append("'");
		}*/
		if(BasicUtil.isNotEmpty(pattern)){
			builder.append(" AND TABLE_NAME LIKE '").append(pattern).append("'");
		}
		if(BasicUtil.isNotEmpty(types)){
			String[] tmps = types.split(",");
			builder.append(" AND TABLE_TYPE IN(");
			int idx = 0;
			for(String tmp:tmps){
				if(idx > 0){
					builder.append(",");
				}
				builder.append("'").append(tmp).append("'");
				idx ++;
			}
			builder.append(")");
		}else {
			builder.append(" AND TABLE_TYPE IN ('BASE TABLE','TABLE')");
		}
 		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 *
	 * @param index 第几条SQL 对照buildQueryTableRunSQL返回顺序
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Table> tables(int index, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			Table table = new Table();
			table.setCatalog(row.getString("TABLE_CATALOG"));
			table.setSchema(row.getString("TABLE_SCHEMA"));
			table.setName(row.getString("TABLE_NAME"));
			table.setEngine(row.getString("ENGINE"));
			table.setComment(row.getString("TABLE_COMMENT"));
			tables.put(table.getName().toUpperCase(), table);
		}
		return tables;
	}

	/**
	 * 查询瑗表上的列
	 * @param table table
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata){
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(builder, table);
			builder.append(" WHERE 1=0");
		}else{
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			builder.append("SELECT * FROM information_schema.TABLES WHERE 1=1 ");
			if(BasicUtil.isNotEmpty(catalog)){
				builder.append(" AND TABLE_CATALOG = '").append(catalog).append("'");
			}
			if(BasicUtil.isNotEmpty(schema)){
				builder.append(" AND TABLE_SCHEMA = '").append(schema).append("'");
			}
			builder.append(" AND TABLE_NAME = '").append(table.getName()).append("'");
		}
		sqls.add(builder.toString());
		return sqls;
	}


	/**
	 * 根据查询结果集构造Tag
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
			Column column = new Column();
			column.setCatalog(row.getString("TABLE_CATALOG"));
			column.setSchema(row.getString("TABLE_SCHEMA"));
			column.setTable(row.getString("TABLE_NAME"));
			column.setName(row.getString("COLUMN_NAME"));
			column.setPosition(row.getInt("ORDINAL_POSITION"));
			column.setComment(row.getString("COLUMN_COMMENT"));
			column.setTypeName(row.getString("DATA_TYPE"));
			column.setDefaultValue(row.get("COLUMN_DEFAULT"));
			column.setNullable(row.getBoolean("IS_NULLABLE"));
			int len = row.getInt("CHARACTER_MAXIMUM_LENGTH",-1);
			if(len == -1){
				len = row.getInt("NUMERIC_PRECISION",0);
			}
			column.setPrecision(len);
			column.setScale(row.getInt("NUMERIC_SCALE",0));
			column.setCharset(row.getString("CHARACTER_SET_NAME"));
			column.setCollate(row.getString("COLLATION_NAME"));
			columns.put(column.getName().toUpperCase(), column);
		}
		return columns;
	}


	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 ******************************************************************************************************************/

	/**
	 * 修改列 ALTER TABLE   HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column
	 * @return
	 */
	@Override
	public List<String> buildAlterRunSQL(Column column){
		List<String> sqls = new ArrayList<>();
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER TABLE ");
		name(builder, table);
		Column update = column.getUpdate();
		if(null != update){
			builder.append(" CHANGE ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			if(!BasicUtil.equalsIgnoreCase(column.getName(), update.getTableName())) {
				SQLUtil.delimiter(builder, update.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			}
			define(builder, update);
		}
		sqls.add(builder.toString());
		return sqls;
	}
	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column){
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER TABLE ");
		name(builder, table);
		Column update = column.getUpdate();
		if(null == update){
			//添加列
			builder.append(" ADD COLUMN ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			//数据类型
			type(builder, column);
			// 编码
			charset(builder, column);
			//默认值
			defaultValue(builder, column);
			//非空
			nullable(builder, column);
			//更新事件
			onupdate(builder, column);
			//备注
			comment(builder, column);
			//位置
			position(builder, column);
		}
		return builder.toString();
	}

	@Override
	public String buildRenameRunSQL(Table table) {
		StringBuilder builder = new StringBuilder();
		builder.append("RENAME TABLE ");
		name(builder, table);
		builder.append(" TO ");
		name(builder, table.getUpdate());
		return builder.toString();
	}

	/**
	 * 修改表备注
	 *  ALTER TABLE T COMMENT 'ABC';
	 * @param table
	 * @return
	 */
	@Override
	public String buildChangeCommentRunSQL(Table table) {
		String comment = table.getComment();
		if(BasicUtil.isEmpty(comment)){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" COMMENT '").append(comment).append("'");
		return builder.toString();
	}


	@Override
	public String alterColumnKeyword(){
		return "ALTER COLUMN";
	}
	/**
	 * 主键
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		List<Column> pks = table.primarys();
		if(pks.size()>0){
			builder.append(",PRIMARY KEY (");
			int idx = 0;
			for(Column pk:pks){
				if(idx > 0){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				String order = pk.getOrder();
				if(BasicUtil.isNotEmpty(order)){
					builder.append(" ").append(order);
				}
			}
			builder.append(")");
		}
		return builder;
	}
	/**
	 * 备注
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Column column){
		String comment = column.getComment();
		if(BasicUtil.isNotEmpty(comment)){
			builder.append(" COMMENT '").append(comment).append("'");
		}
		return builder;
	}

	/**
	 * 位置
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder position(StringBuilder builder, Column column){
		Integer position = column.getPosition();
		if(null != position && 0 == position){
			builder.append(" FIRST");
		}else{
			String after = column.getAfter();
			if(BasicUtil.isNotEmpty(after)){
				builder.append(" AFTER").append(after);
			}
		}
		return builder;
	}

	/**
	 * 自增长列
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder increment(StringBuilder builder, Column column){
		if(column.isAutoIncrement() == 1){
			builder.append(" AUTO_INCREMENT");
		}
		return builder;
	}
	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder onupdate(StringBuilder builder, Column column){
		if(column.isOnUpdate() == 1){
			builder.append(" ON UPDATE CURRENT_TIMESTAMP");
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 ******************************************************************************************************************/

	/**
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	public String buildInValue(SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "now()";
		}
		return null;
	}
} 
