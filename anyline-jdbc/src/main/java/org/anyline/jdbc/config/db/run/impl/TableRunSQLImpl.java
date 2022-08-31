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


package org.anyline.jdbc.config.db.run.impl;

import org.anyline.entity.*;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.RunValue;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.sql.auto.TableSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.Join;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TableRunSQLImpl extends BasicRunSQLImpl implements RunSQL{

	public TableRunSQLImpl(SQLCreater creater, String table){
		this.conditionChain = new AutoConditionChainImpl();
		this.orderStore = new OrderStoreImpl();
		this.table = table;
		this.creater = creater;
	}

	private void parseDataSource(){ 
		table = sql.getTable(); 
		table = table.replace(delimiterFr, "").replace(delimiterTo, "");
		if(table.contains(".")){ 
			this.author = table.substring(0,table.indexOf(".")); 
			this.table = table.substring(table.indexOf(".") + 1); 
		} else{
			if(BasicUtil.isNotEmpty(sql.getSchema())){
				author = sql.getSchema();
			}
		}
	} 
	public void init(){
		super.init(); 
		parseDataSource(); 
		if(null != configStore){ 
			ConditionChain chain = configStore.getConfigChain().createAutoConditionChain(); 
			if(null != chain){ 
				for(Condition condition:chain.getConditions()){ 
					addCondition(condition); 
				} 
			} 
			OrderStore orderStore = configStore.getOrders(); 
			if(null != orderStore){ 
				List<Order> orders = orderStore.getOrders(); 
				if(null != orders){ 
					for(Order order:orders){ 
						this.orderStore.order(order); 
					} 
				} 
			} 
			PageNavi navi = configStore.getPageNavi(); 
			if(navi != null){ 
				this.pageNavi = navi; 
			} 
		}
	}
	private void checkValid(){
		if(null != conditionChain && !conditionChain.isValid()){
			this.valid = false;
		}
	}
	public void createRunQueryTxt(){
		TableSQL sql = (TableSQL)this.getSql();
		builder.append("SELECT ");
		if(null != sql.getDistinct()){
			builder.append(sql.getDistinct());
		}
		builder.append(SQLCreater.BR_TAB);
		List<String> columns = sql.getColumns(); 
		if(null != columns && columns.size()>0){ 
			//指定查询列 
			int size = columns.size(); 
			for(int i=0; i<size; i++){ 
				String column = columns.get(i);
				if(BasicUtil.isEmpty(column)){
					continue;
				} 
				if(column.startsWith("${") && column.endsWith("}")){
					column = column.substring(2, column.length()-1);
					builder.append(column); 
				}else{
					if(column.toUpperCase().contains(" AS ") || column.contains("(") || column.contains(",")){
						builder.append(column);
					}else if("*".equals(column)){
						builder.append("*");
					}else{
						BasicUtil.delimiter(builder, column, delimiterFr, delimiterTo);
					} 
				} 
				if(i<size-1){ 
					builder.append(","); 
				} 
			} 
			builder.append(SQLCreater.BR); 
		}else{ 
			//全部查询 
			builder.append("*"); 
			builder.append(SQLCreater.BR); 
		} 
		builder.append("FROM").append(SQLCreater.BR_TAB);
		if(null != author){
			BasicUtil.delimiter(builder, author, delimiterFr, delimiterTo).append(".");
		}
		BasicUtil.delimiter(builder, table, delimiterFr, delimiterTo);
		builder.append(SQLCreater.BR);
		if(BasicUtil.isNotEmpty(sql.getAlias())){
			//builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(sql.getAlias());
		}
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(SQLCreater.BR_TAB).append(join.getType().getCode()).append(" ");
				BasicUtil.delimiter(builder, join.getName(), delimiterFr, delimiterTo);
				if(BasicUtil.isNotEmpty(join.getAlias())){
					//builder.append(" AS ").append(join.getAlias());
					builder.append("  ").append(join.getAlias());
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		builder.append("\nWHERE 1=1\n\t"); 
		 
 
 
		/*添加查询条件*/ 
		//appendConfigStore(); 
		appendCondition();
		appendGroup();
		appendOrderStore();
		checkValid();
	}

	public void createRunDeleteTxt(){
		TableSQL sql = (TableSQL)this.getSql();
		builder.append("DELETE FROM ");
		if(null != author){
			BasicUtil.delimiter(builder, author, delimiterFr, delimiterTo).append(".");
		}

		BasicUtil.delimiter(builder, table, delimiterFr, delimiterTo);
		builder.append(SQLCreater.BR);
		if(BasicUtil.isNotEmpty(sql.getAlias())){
			//builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(sql.getAlias());
		}
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(SQLCreater.BR_TAB).append(join.getType().getCode()).append(" ");
				BasicUtil.delimiter(builder, join.getName(), getDelimiterFr(), getDelimiterTo());
				if(BasicUtil.isNotEmpty(join.getAlias())){
					builder.append("  ").append(join.getAlias());
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		builder.append("\nWHERE 1=1\n\t");



		/*添加查询条件*/
		//appendConfigStore();
		appendCondition();
		appendGroup();
		appendOrderStore();
		checkValid();
	}

	private void appendOrderStore(){
		 
	} 
	private void appendGroup(){
		if(null != groupStore){
			builder.append(groupStore.getRunText(delimiterFr+delimiterTo));
		}
		if(BasicUtil.isNotEmpty(having)){
			builder.append(" HAVING ").append(having);
		} 
	} 
	/** 
	 * 拼接查询条件
	 */ 
	private void appendCondition(){
		if(null == conditionChain){ 
			return; 
		}
		builder.append(conditionChain.getRunText(creater));
		List<RunValue> values = conditionChain.getRunValues();
		addValues(values);
	} 

	public void setConfigs(ConfigStore configs) { 
		this.configStore = configs; 
		if(null != configs){ 
			this.pageNavi = configs.getPageNavi();
		} 
	}

	@Override
	public RunSQL setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, COMPARE_TYPE compare) {
		return this;
	}

	@Override
	public RunSQL setConditionValue(boolean required,  String condition, String variable, Object value, COMPARE_TYPE compare) {
		return setConditionValue(required,  false, condition, variable, value, compare);
	}
} 
