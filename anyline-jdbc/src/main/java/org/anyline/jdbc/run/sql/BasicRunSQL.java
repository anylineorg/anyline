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


package org.anyline.jdbc.run.sql;

import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.OrderStoreImpl;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.prepare.*;
import org.anyline.jdbc.param.ConfigParser;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.param.ParseResult;
import org.anyline.jdbc.prepare.init.SimpleGroupStore;
import org.anyline.jdbc.run.BasicRun;
import org.anyline.jdbc.run.Run;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleAutoConditionChain;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleAutoCondition;
import org.anyline.jdbc.run.RunValue;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class BasicRunSQL extends BasicRun implements Run {
	/**
	 * 添加条件
	 * @param conditions 查询条件 ORDER GROUP 等
	 * @return Run
	 */
	@Override
	public Run addConditions(String ... conditions) {
		/*添加查询条件*/ 
		if(null != conditions){ 
			for(String condition:conditions){
				if(null == condition){
					continue;
				}
				condition = condition.trim();
				String up = condition.toUpperCase().replaceAll("\\s+", " ").trim();

				if(up.startsWith("ORDER BY")){
					//排序条件
					String orderStr = condition.substring(up.indexOf("ORDER BY") + "ORDER BY".length()).trim();
					String orders[] = orderStr.split(","); 
					for(String item:orders){ 
						order(item); 
						if(null != configStore){ 
							configStore.order(item); 
						} 
						if(null != this.orderStore){ 
							this.orderStore.order(item); 
						} 
					} 
					continue; 
				}else if(up.startsWith("GROUP BY")){
					//分组条件
					String groupStr = condition.substring(up.indexOf("GROUP BY") + "GROUP BY".length()).trim();
					String groups[] = groupStr.split(",");
					for(String item:groups){
						if(null == groupStore){
							groupStore = new SimpleGroupStore();
						}
						groupStore.group(item);
					}
					continue;
				}else if(up.startsWith("HAVING")){
					//分组过滤
					String haveStr = condition.substring(up.indexOf("HAVING") + "HAVING".length()).trim();
					this.having = haveStr;
					continue;
				}
//				if(up.contains(" OR ") && !(condition.startsWith("(") && condition.endsWith(")"))){
//					condition = "(" + condition + ")";
//				}


				if(condition.startsWith("${") && condition.endsWith("}")){
					//原生SQL  不处理
					Condition con = new SimpleAutoCondition(condition.substring(2, condition.length()-1));
					addCondition(con);
					continue;
				}

				if(condition.contains(":")){
					//:符号是否表示时间
					boolean isTime = false;
					int idx = condition.indexOf(":");
					//''之内
					if(condition.indexOf("'")<idx && condition.indexOf("'", idx+1) > 0){
						isTime = true;
					}
					if(!isTime){
						//需要解析的SQL
						ParseResult parser = ConfigParser.parse(condition,false);
						Object value = ConfigParser.getValues(parser);
						addCondition(parser.isRequired(), parser.isStrictRequired(), parser.getPrefix(),parser.getVar(),value,parser.getCompare());
						continue;
					}
				}
				Condition con = new SimpleAutoCondition(condition);
				addCondition(con);
			} 
		} 
		return this; 
	}
	protected static boolean endWithWhere(String txt){ 
		boolean result = false;
		txt = txt.toUpperCase(); 
		int fr = 0;
		while((fr = txt.indexOf("WHERE")) > 0){ 
			txt = txt.substring(fr+5); 
			if(txt.indexOf("UNION") > 0){
				continue;
			}
			try{ 
				int bSize = 0;//左括号数据
				if(txt.contains(")")){
					bSize = RegularUtil.fetch(txt, "\\)").size();
				} 
				int eSize = 0;//右括号数量
				if(txt.contains("(")){
					eSize = RegularUtil.fetch(txt, "\\(").size();
				} 
				if(bSize == eSize){ 
					result = true;
					break;
				} 
			}catch(Exception e){ 
				e.printStackTrace(); 
			} 
		} 
		return result; 
	}

	/**
	 * 需要查询的列
	 * @return String
	 */
	@Override
	public String getQueryColumns(){
		String result = "*";
		if(null != prepare){
			List<String> cols = prepare.getFetchKeys();
			if(null != cols && cols.size()>0){
				result = null;
				for(String col:cols){
					if(null == result){

						result = SQLUtil.delimiter(col, adapter.getDelimiterFr() , adapter.getDelimiterTo());
					}else{
						result += "," + SQLUtil.delimiter(col, adapter.getDelimiterFr() , adapter.getDelimiterTo());
					}
				}
			}
		}
		return result;
	}

}
 
 
