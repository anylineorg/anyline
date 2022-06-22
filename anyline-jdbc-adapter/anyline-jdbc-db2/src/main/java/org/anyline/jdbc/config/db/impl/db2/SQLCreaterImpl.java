/*
 * Copyright 2006-2022 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.anyline.jdbc.config.db.impl.db2; 
 
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.springframework.stereotype.Repository;
@Repository("anyline.jdbc.creater.db2") 
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{ 
	public DB_TYPE type(){ 
		return DB_TYPE.DB2; 
	} 
	public SQLCreaterImpl(){ 
		delimiterFr = "\"";
		delimiterTo = "\"";
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
