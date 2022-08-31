package org.anyline.jdbc.config.db.impl.mysql;
 
import org.anyline.entity.PageNavi;
import org.anyline.entity.OrderStore;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
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
} 
