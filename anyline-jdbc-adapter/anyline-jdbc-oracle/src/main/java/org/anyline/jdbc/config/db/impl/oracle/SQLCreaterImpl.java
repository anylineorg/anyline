 
package org.anyline.jdbc.config.db.impl.oracle; 
 
import org.anyline.dao.AnylineDao;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("anyline.jdbc.creater.oracle") 
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{ 
	 
	@Autowired(required = false) 
	@Qualifier("anyline.dao") 
	protected AnylineDao dao; 

	public DB_TYPE type(){ 
		return DB_TYPE.ORACLE; 
	} 
 
	public SQLCreaterImpl(){ 
		delimiterFr = "";
		delimiterTo = "";
	}
	@Override
	public String getDelimiterFr(){
		return delimiterFr;
	}
	@Override
	public String getDelimiterTo(){
		return delimiterTo;
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
			order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
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
				builder.append("\n").append(order);
				builder.append(")  TAB_I \n");
				builder.append(")  TAB_O WHERE ROW_NUMBER >= "+(first+1)+" AND ROW_NUMBER <= "+(last+1));

		} 
		 
		return builder.toString(); 
		 
	}

	@Override
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
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set, List<String> keys){
		builder.append("INSERT ALL \n");
		String head = "INTO " + dest + " (";
		int keySize = keys.size();
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			head += key;
			if(i<keySize-1){
				head += ", ";
			}
		}
		head += ") ";

		int dataSize = set.size();
		for(int i=0; i<dataSize; i++){
			DataRow row = set.getRow(i);
			if(null == row){
				continue;
			}
			if(row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())){
				String pk = row.getPrimaryKey();
				if(null == pk){
					pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
				}
				row.put(pk, primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
			}
			builder.append(head).append("VALUES ");
			insertValue(builder, row, keys);
			builder.append(" \n");
		}
		builder.append("SELECT 1 FROM DUAL");
	}
}
