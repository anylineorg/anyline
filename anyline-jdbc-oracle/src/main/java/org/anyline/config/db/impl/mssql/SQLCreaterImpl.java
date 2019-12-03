
package org.anyline.config.db.impl.mssql;

import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.config.db.run.RunSQL;
import org.anyline.dao.AnylineDao;
import org.anyline.dao.PrimaryCreater;
import org.anyline.entity.PageNavi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("anyline.jdbc.creater.oracle")
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{
	private static final long serialVersionUID = 43588201817410304L;
	
	@Autowired(required = false)
	@Qualifier("anyline.dao")
	protected AnylineDao dao;
	
	private PrimaryCreater pc = new PrimaryCreaterImpl();
	public DB_TYPE type(){
		return DB_TYPE.ORACLE;
	}

	public SQLCreaterImpl(){
		disKeyFr = "\"";
		disKeyTo = "\"";
	}
	@Override
	public String parseFinalQueryTxt(RunSQL run){
		StringBuilder builder = new StringBuilder();
		String cols = run.getFetchColumns();
		PageNavi navi = run.getPageNavi();
		String sql = run.getBaseQueryTxt();
		OrderStore orders = run.getOrderStore();
		int first = 0;
		int last = 0;
		String order = "";
		if(null != orders){
			order = orders.getRunText(getDisKeyFr()+getDisKeyTo());
		}
		if(null != navi){
			first = navi.getFirstRow();
			last = navi.getLastRow();
		}
		if(null == navi){
			builder.append(sql).append("\n").append(order);
		}else{
			//分页
				builder.append("SELECT "+cols+" FROM( \n");
				builder.append("SELECT _TAB_I.* ,ROWNUM AS ROW_NUMBER \n");
				builder.append("FROM( \n");
				builder.append(sql);
				builder.append(") AS _TAB_I \n");
				builder.append(") AS _TAB_O WHERE ROW_NUMBER >= "+(first+1)+" AND ROW_NUMBER <= "+(last+1));
			
		}
		
		return builder.toString();
		
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
	@Override
	public PrimaryCreater getPrimaryCreater() {
		return pc;
	}
}