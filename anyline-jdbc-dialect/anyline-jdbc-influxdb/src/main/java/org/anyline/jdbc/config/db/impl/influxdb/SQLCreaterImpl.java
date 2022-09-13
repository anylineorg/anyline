 
package org.anyline.jdbc.config.db.impl.influxdb;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.run.impl.TableRunSQLImpl;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("anyline.jdbc.creater.influxdb")
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.InfluxDB;
	} 
	public SQLCreaterImpl(){ 
		delimiterFr = "\"";
		delimiterTo = "\"";
	}
	@Value("${anyline.jdbc.delimiter.influxdb:}")
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
			sql += " LIMIT " + limit + " OFFSET " + navi.getFirstRow(); 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
 
	public String concat(String ... args){
		return concatOr(args);
	}


	public RunSQL buildInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns){
		RunSQL run = null;
		if(null != obj){
			StringBuilder builder = new StringBuilder();
			run = new TableRunSQLImpl(this,dest);
			if(obj instanceof DataRow){
				DataRow row = (DataRow)obj;
				List<String> cols = confirmInsertColumns(dest, obj, columns);
				//insert al, tag1=value1 qty=1,name=5
				builder.append("insert ").append(parseTable(dest)).append(" ");
				Map<String,Object> tags = row.getTags();
				for(String tag:tags.keySet()){
					builder.append(",").append(tag).append("=").append(tags.get(tag));
				}
				int qty = 0;
				for(String col:cols){
					Object value = row.get(col);
					if(null == value){
						continue;
					}
					if(qty>0) {
						builder.append(",");
					}
					builder.append(col).append("=");
					if(BasicUtil.isNumber(value) || BasicUtil.isBoolean(value)){
						builder.append(value);
					}else{
						builder.append("\"").append(value).append("\"");
					}
					qty ++;
				}
				builder.append(" ").append(row.getNanoTime());
				run.setBuilder(builder);
			}
		}
		return run;
	}
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set, List<String> keys){
		return;
	}
} 
