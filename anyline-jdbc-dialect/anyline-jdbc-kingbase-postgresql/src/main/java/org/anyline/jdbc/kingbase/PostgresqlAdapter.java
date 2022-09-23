 
package org.anyline.jdbc.kingbase;
 
import org.anyline.entity.PageNavi;
import org.anyline.entity.OrderStore;
import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.adapter.SQLAdapter;
import org.anyline.jdbc.run.Run;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.jdbc.sql.adapter.kingbase")
public class PostgresqlAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.KingBase;
	} 
	public PostgresqlAdapter(){
		delimiterFr = "\"";
		delimiterTo = "\"";
	}
	@Value("${anyline.jdbc.delimiter.kingbase:}")
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
	public String parseFinalQueryTxt(Run run){
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
 
 
	public String concat(String ... args){
		return concatOr(args);
	} 
} 
