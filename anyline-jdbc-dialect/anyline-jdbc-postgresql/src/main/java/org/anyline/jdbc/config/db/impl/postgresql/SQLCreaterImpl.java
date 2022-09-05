 
package org.anyline.jdbc.config.db.impl.postgresql; 
 
import org.anyline.entity.MetaData;
import org.anyline.entity.PageNavi;
import org.anyline.entity.OrderStore;
import org.anyline.jdbc.config.db.RunValue;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.Column;
import org.anyline.util.BasicUtil;
import org.postgresql.ds.common.PGObjectFactory;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

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

} 
