 
package org.anyline.data.jdbc.kingbase;
 
import org.anyline.dao.AnylineDao;
import org.anyline.entity.PageNavi;
import org.anyline.entity.OrderStore;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.SQLAdapter;
import org.anyline.data.run.Run;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.kingbase")
public class KingbaseAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	 
	@Autowired(required = false) 
	@Qualifier("anyline.dao") 
	protected AnylineDao dao; 

	public DB_TYPE type(){
		return DB_TYPE.KingBase;
	}
	@Value("${anyline.jdbc.delimiter.kingbase:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}


	public KingbaseAdapter(){
		delimiterFr = "";
		delimiterTo = "";
	}

	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/
	@Override 
	public String parseFinalQuery(Run run){
		StringBuilder builder = new StringBuilder(); 
		String cols = run.getQueryColumns(); 
		PageNavi navi = run.getPageNavi(); 
		String sql = run.getBaseQuery(); 
		OrderStore orders = run.getOrderStore(); 
		int first = 0; 
		int last = 0; 
		String order = ""; 
		if(null != orders){ 
			order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
		} 
		if(null != navi){ 
			first = navi.getFirstRow(); 
			last = navi.getLastRow(); 
		} 
		if(null == navi){ 
			builder.append(sql).append("\n").append(order); 
		}else{ 
			// 分页 
				builder.append("SELECT "+cols+" FROM( \n");
				builder.append("SELECT TAB_I.* ,ROWNUM AS ROW_NUMBER \n"); 
				builder.append("FROM( \n"); 
				builder.append(sql);
				builder.append("\n").append(order);
				builder.append(")  TAB_I \n");
				builder.append(")  TAB_O WHERE ROW_NUMBER >= "+(first+1)+" AND ROW_NUMBER <= "+(last+1));

		} 
		 
		return builder.toString(); 
		 
	} 
 
	public String concat(String ... args){
		return concatOr(args);
	} 
}
