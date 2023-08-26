 
/*
 * Copyright 2006-2023 www.anyline.org
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
 */


package org.anyline.data.jdbc.influxdb;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository("anyline.data.jdbc.adapter.influxdb")
public class InfluxAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	
	public DatabaseType type(){
		return DatabaseType.InfluxDB;
	} 
	public InfluxAdapter(){
		delimiterFr = "\"";
		delimiterTo = "\"";
	}
	@Value("${anyline.data.jdbc.delimiter.influxdb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/
	@Override 
	public String mergeFinalQuery(DataRuntime runtime, Run run){
		String sql = run.getBaseQuery(); 
		String cols = run.getQueryColumn(); 
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
			long limit = navi.getLastRow() - navi.getFirstRow() + 1; 
			if(limit < 0){
				limit = 0; 
			} 
			sql += " LIMIT " + limit + " OFFSET " + navi.getFirstRow(); 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
 
	public String concat(DataRuntime runtime, String ... args){
		return concatOr(args);
	}


	/**
	 * 创建 insert Run
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表
	 * @param obj 实体
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns){
		Run run = null;
		if(null != obj){
			StringBuilder builder = new StringBuilder();
			run = new TableRun(runtime,dest);
			if(obj instanceof DataRow){
				DataRow row = (DataRow)obj;
				LinkedHashMap<String,Column> cols = confirmInsertColumns(runtime, dest, obj, columns, false);
				// insert al, tag1=value1 qty=1,name=5
				builder.append("insert ").append(parseTable(dest)).append(" ");
				Map<String,Object> tags = row.getTags();
				for(String tag:tags.keySet()){
					builder.append(",").append(tag).append("=").append(tags.get(tag));
				}
				int qty = 0;
				for(Column column:cols.values()){
					String col = column.getName();
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
} 
