 
package org.anyline.jdbc.config.db.impl.oracle;

import org.anyline.dao.AnylineDao;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
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
		return concatOr(args);
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
					pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
				}
				row.put(pk, primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
			}
			builder.append(head).append("VALUES ");
			insertValue(builder, row, keys);
			builder.append(" \n");
		}
		builder.append("SELECT 1 FROM DUAL");
	}

	@Override
	public void format(StringBuilder builder, Object obj, String key){
		Object value = null;
		if(obj instanceof DataRow){
			value = ((DataRow)obj).get(key);
		}
		if(AdapterProxy.hasAdapter()){
			Field field = AdapterProxy.field(obj.getClass(), key);
			value = BeanUtil.getFieldValue(obj, field);
		}else{
			value = BeanUtil.getFieldValue(obj, key);
		}
		if(null == value || "NULL".equals(value)){
			builder.append("null");
		}else if(value instanceof String){
			String str = value.toString();
			if(str.startsWith("${") && str.endsWith("}") && !BeanUtil.isJson(value)){
				str = str.substring(2, str.length()-1);
			}else{
				str = "'" + str.replace("'", "''") + "'";
			}
			builder.append(str);
		}else if(value instanceof Timestamp
				|| value instanceof java.util.Date
				|| value instanceof java.sql.Date
				|| value instanceof LocalDate
				|| value instanceof LocalTime
				|| value instanceof LocalDateTime
		){
			Date date = DateUtil.parse(value);
			builder.append("TO_DATE('").append(DateUtil.format(date,DateUtil.FORMAT_DATE_TIME)).append("','yyyy-mm-dd hh24:mi:ss')");
		}else if(value instanceof Date){
			builder.append("TO_DATE('").append(DateUtil.format((Date)value,DateUtil.FORMAT_DATE_TIME)).append("','yyyy-mm-dd hh24:mi:ss')");
		}else if(value instanceof Number || value instanceof Boolean){
			builder.append(value.toString());
		}else{
			builder.append(value.toString());
		}
	}
}
