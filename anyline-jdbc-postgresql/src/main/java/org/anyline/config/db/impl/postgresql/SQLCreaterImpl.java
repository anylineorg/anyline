
package org.anyline.config.db.impl.postgresql;

import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.config.db.run.RunSQL;
import org.anyline.entity.PageNavi;
import org.springframework.stereotype.Repository;
@Repository("anyline.jdbc.creater.postgresql")
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{

	public DB_TYPE type(){
		return DB_TYPE.PostgreSQL;
	}
	public SQLCreaterImpl(){
		disKeyFr = "\"";
		disKeyTo = "\"";
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
			sql += orders.getRunText(getDisKeyFr()+getDisKeyTo());
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
		String result = "";
		if(null != args && args.length > 0){
			int size = args.length;
			for(int i=0; i<size; i++){
				String arg = args[i];
				if(i>0){
					result += " || ";
				}
				result += arg;
			}
		}
		return result;
	}
}
