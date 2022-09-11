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

	/**
	 * 修改(添加)列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column){
		StringBuilder builder = new StringBuilder();
		String catalog = column.getCatalog();
		String schema = column.getSchema();
		builder.append("ALTER TABLE ");
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, column.getTable(), getDelimiterFr(), getDelimiterTo());
		Column update = column.getUpdate();
		if(null == update){
			//添加列
			builder.append(" ADD COLUMN ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			//数据类型
			builder.append(column.getTypeName());

			//精度
			int precision = column.getPrecision();
			Integer scale = column.getScale();
			if(precision > 0){
				builder.append("(").append(precision);
				if(null != scale){
					builder.append(",").append(scale);
				}
				builder.append(")");
			}else if(precision == -1){
				builder.append("(max)");
			}
			// 编码
			// CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
			String charset = column.getCharset();
			if(BasicUtil.isNotEmpty(charset)){
				builder.append(" CHARACTER SET ").append(charset);
				String collate = column.getCollate();
				if(BasicUtil.isNotEmpty(collate)){
					builder.append(" COLLATE ").append(collate);
				}
			}
			//默认值
			Object def = column.getDefaultValue();
			if(BasicUtil.isNotEmpty(def)){
				builder.append(" default ");
				boolean isCharColumn = isCharColumn(column);
				if(isCharColumn){
					builder.append("'");
				}
				builder.append(def);
				if(isCharColumn){
					builder.append("'");
				}
			}else {
				//非空
				if (!column.isNullable()) {
					builder.append(" NOT NULL");
				}
			}
			if(column.isOnUpdate()){
				builder.append(" ON UPDATE CURRENT_TIMESTAMP");
			}
			//备注
			String comment = column.getComment();
			if(BasicUtil.isNotEmpty(comment)){
				builder.append(" COMMENT '").append(comment).append("'");
			}
			//位置
			Integer position = column.getPosition();;
			if(null != position && position == 0){
				builder.append(" FIRST");
			}else{
				String after = column.getAfter();
				if(BasicUtil.isNotEmpty(after)){
					builder.append(" AFTER").append(after);
				}
			}
		}
		return builder.toString();
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
	 * 主键
	 * @param builder builder
	 * @param table table
	 */
	@Override
	public void primary(StringBuilder builder, Table table){
		List<Column> pks = table.getPrimaryKeys();
		if(pks.size()>0){
			builder.append(",PRIMARY KEY (");
			int idx = 0;
			for(Column pk:pks){
				if(idx > 0){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo()).append(" ").append(pk.getOrder());
			}
			builder.append(")");
		}
	}
	/**
	 * 备注
	 * @param builder builder
	 * @param column column
	 */
	@Override
	public void comment(StringBuilder builder, Column column){
		String comment = column.getComment();
		if(BasicUtil.isNotEmpty(comment)){
			builder.append(" COMMENT '").append(comment).append("'");
		}
	}

	/**
	 * 位置
	 * @param builder builder
	 * @param column column
	 */
	public void position(StringBuilder builder, Column column){
		Integer position = column.getPosition();
		if(null != position && position == 0){
			builder.append(" FIRST");
		}else{
			String after = column.getAfter();
			if(BasicUtil.isNotEmpty(after)){
				builder.append(" AFTER").append(after);
			}
		}
	}

	/**
	 * 自增长列
	 * @param builder builder
	 * @param column column
	 */
	public void increment(StringBuilder builder, Column column){
		if(column.isAutoIncrement()){
			builder.append(" AUTO_INCREMENT");
		}
	}
	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column column
	 */
	public void onupdate(StringBuilder builder, Column column){
		if(column.isOnUpdate()){
			builder.append(" ON UPDATE CURRENT_TIMESTAMP");
		}
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
