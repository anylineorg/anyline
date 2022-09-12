 
package org.anyline.jdbc.config.db.impl.postgresql; 

import org.anyline.entity.PageNavi;
import org.anyline.entity.OrderStore;
import org.anyline.jdbc.config.db.RunValue;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.postgresql.ds.common.PGObjectFactory;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository("anyline.jdbc.creater.postgresql")
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater, InitializingBean {
 
	public DB_TYPE type(){ 
		return DB_TYPE.PostgreSQL; 
	} 
	public SQLCreaterImpl(){ 
		delimiterFr = "\"";
		delimiterTo = "\"";
	}
	@Value("${anyline.jdbc.delimiter.postgresql:}")
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
			sql += " LIMIT " + limit + " OFFSET " + navi.getFirstRow(); 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	}

/*
uuid:String
int8:Long
varchar:String
date:java.sql.Date
timestamp:java.sql.Timestamp
timestamptz:java.sql.Timestamp
time:java.sql.Time
timez:java.sql.Time
text:String
numeric:BigDecimal
json:String
xml:String
bit:Boolean
bool:Boolean
box:String
bytea:byte[]
bpchar:String
cidr:String
circle:String
float4:Float
float8:Double
inet:String
int2:Short
int4:Integer
int8:Long
interval:String
jsonb:String
line:String
lseg:String
macaddr:String
money:Double
path:String
point:String
polygon:String
smallserial:Short
serial:Integer
bigserial:Long
tsquery:String
tsvector:String
txid_snapshot:String
varbit:String
* */

	public boolean convert(Column column, RunValue run){
		boolean result = false;

		if(null == column){
			return false;
		}
		if(null == run){
			return true;
		}
		Object value = run.getValue();
		if(null == value){
			return true;
		}
		try {
			String clazz = column.getClassName();
			String typeName = column.getTypeName().toUpperCase();
			//先解析特定数据库类型，注意不需要重复解析super中解析的类型
			//
			if(typeName.equals("JSON")
					|| typeName.equals("XML")
					|| typeName.equals("BOX")
					|| typeName.equals("BIT")
					|| typeName.equals("CIDR")
					|| typeName.equals("CIRCLE")
			){
				run.setValue(value(typeName.toLowerCase(), value));
				return true;
			}else if(typeName.equals("BOOL")){
				run.setValue(BasicUtil.parseBoolean(value, null));
				return true;
			}else{
				//没有成功,super继续解析通用类型
				result = super.convert(column, run);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
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
	public String concat(String ... args){
		return concatOr(args);
	}

	/**
	 * 修改表名
	 * ALTER TABLE A RENAME TO B;
	 * @param table table
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) {
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" RENAME TO ");
		name(builder, table.getUpdate());
		return builder.toString();
	}

	/**
	 * 修改列名
	 * ALTER TABLE T  RENAME  A  to B ;
	 * @param column column
	 * @return String
	 */
	public String buildRenameRunSQL(Column column) {
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable());
		builder.append(" RENAME ").append(column.getName()).append(" TO ").append(column.getNewName());
		return builder.toString();
	}
	/**
	 * 主键
	 * CONSTRAINT PK_BS_DEV PRIMARY KEY (ID ASC)
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		List<Column> pks = table.getPrimaryKeys();
		if(pks.size()>0){
			builder.append(",CONSTRAINT ").append("PK_").append(table.getName()).append(" PRIMARY KEY (");
			int idx = 0;
			for(Column pk:pks){
				if(idx > 0){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				String order = pk.getOrder();
				if(null != order){
					builder.append(" ").append(order);
				}
			}
			builder.append(")");
		}
		return builder;
	}
	@Override
	public String type(String type){
		if(type.equalsIgnoreCase("int")){
			return "int4";
		}
		return type;
	}
} 
