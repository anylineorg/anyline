 
package org.anyline.jdbc.config.db.impl.oracle; 
 
import org.anyline.dao.AnylineDao;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
 
@Repository("anyline.jdbc.creater.oracle") 
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{ 
	 
	@Autowired(required = false) 
	@Qualifier("anyline.dao") 
	protected AnylineDao dao; 
	 
	public DB_TYPE type(){ 
		return DB_TYPE.ORACLE; 
	} 
 
	public SQLCreaterImpl(){ 
		disKeyFr = "";
		disKeyTo = "";
	}
	@Override
	public String getDisKeyFr(){
		return disKeyFr;
	}
	@Override
	public String getDisKeyTo(){
		return disKeyTo;
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
				builder.append("SELECT TAB_I.* ,ROWNUM AS ROW_NUMBER \n"); 
				builder.append("FROM( \n"); 
				builder.append(sql); 
				builder.append(")  TAB_I \n");
				builder.append(")  TAB_O WHERE ROW_NUMBER >= "+(first+1)+" AND ROW_NUMBER <= "+(last+1));

			 
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
}
