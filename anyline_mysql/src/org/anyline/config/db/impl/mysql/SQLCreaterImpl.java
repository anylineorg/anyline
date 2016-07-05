
package org.anyline.config.db.impl.mysql;

import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.config.db.run.RunSQL;
import org.springframework.stereotype.Repository;
@Repository
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{
	
	public SQLCreaterImpl(){
		disKeyFr = "`";
		disKeyTo = "`";
	}
	/**
	 * 鏌ヨSQL
	 * RunSQL 鍙嶈浆璋冪敤
	 * @param baseTxt
	 * @return
	 */
	@Override
	public String parseFinalQueryTxt(RunSQL run){
		String sql = run.getBaseQueryTxt();
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
			sql += " LIMIT " + navi.getFirstRow() + "," + limit;
		}
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
		return sql;
	}

	public String concat(String ... args){
		String result = "";
		if(null != args && args.length > 0){
			result = "concat(";
			int size = args.length;
			for(int i=0; i<size; i++){
				String arg = args[i];
				if(i>0){
					result += ",";
				}
				result += arg;
			}
			result += ")";
		}
		return result;
	}
}
