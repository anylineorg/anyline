package org.anyline.jdbc.config.db.impl.mysql;

import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("anyline.jdbc.creater.mysql")
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.MYSQL; 
	} 
	public SQLCreaterImpl(){ 
		delimiterFr = "`";
		delimiterTo = "`";
	}
	@Value("${anyline.jdbc.delimiter.mysql:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet() throws Exception {
		setDelimiter(delimiter);
	}

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

 /*
BIGINT:java.lang.Long
BINARY:byte[]
BIT:Boolean
BLOB:byte[]
CHAR:String
DATE:java.sql.Date
DATETIME:java.sql.Timestamp
DECIMAL:BigDecimal
JSON:String
FLOAT:Float
GEOMETRY:byte[]
INT:Integer
GEOMETRY:byte[]
LONGBLOB:byte[]
LONGTEXT:String
MEDIUMBLOB:byte[]
MEDIUMINT：Integer
MEDIUMTEXT:String
GEOMETRY:byte[]
DOUBLE:Double
SMALLINT:Short
YEAR:java.sql.Date
VARBINARY:byte[]
TINYTEXT:java.lang.String
TINYINT:java.lang.Byte
TINYBLOB:byte[]
TIMESTAMP:java.sql.Timestamp
TIME:java.sql.Time
TEXT:String
SMALLINT:Short
*/
	public String concat(String ... args){
		return concatFun(args);
	}
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
			type2type(builder, column);
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
		List<Column> pks = table.getPrimaryKeys();
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
		if(null != position && position == 0){
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
		if(column.isAutoIncrement()){
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
		if(column.isOnUpdate()){
			builder.append(" ON UPDATE CURRENT_TIMESTAMP");
		}
		return builder;
	}
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
