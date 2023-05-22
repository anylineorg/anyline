package org.anyline.data.jdbc.gbase;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.adapter.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.gbase")
public class GbasetAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	
	public DatabaseType type(){
		return DatabaseType.GBase;
	} 
	public GbasetAdapter(){
		delimiterFr = "";
		delimiterTo = "";
	}

	@Value("${anyline.data.jdbc.delimiter.gbase:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
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
} 
