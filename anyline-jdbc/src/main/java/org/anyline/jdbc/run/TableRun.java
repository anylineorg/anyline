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


package org.anyline.jdbc.run;

import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.OrderStoreImpl;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.prepare.Condition;
import org.anyline.jdbc.prepare.ConditionChain;
import org.anyline.jdbc.prepare.RunPrepare;
import org.anyline.jdbc.prepare.auto.TableSQL;
import org.anyline.jdbc.prepare.auto.init.SimpleAutoConditionChain;
import org.anyline.util.BasicUtil;

import java.util.List;

public class TableRun extends BasicRun implements Run {

	public TableRun(JDBCAdapter adapter, String table){
		this.builder = new StringBuilder();
		this.conditionChain = new SimpleAutoConditionChain();
		this.orderStore = new OrderStoreImpl();
		this.table = table;
		this.adapter = adapter;
	}

	private void parseDataSource(){ 
		table = prepare.getTable();
		table = table.replace(delimiterFr, "").replace(delimiterTo, "");
		if(table.contains(".")){ 
			this.schema = table.substring(0,table.indexOf("."));
			this.table = table.substring(table.indexOf(".") + 1); 
		} else{
			if(BasicUtil.isNotEmpty(prepare.getSchema())){
				schema = prepare.getSchema();
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
	/*
	// adapter中实现
	@Override
	public void createRunQueryTxt(){
		TablePrepare sql = (TablePrepare)this.getPrepare();
		builder.append("SELECT ");
		if(null != sql.getDistinct()){
			builder.append(sql.getDistinct());
		}
		builder.append(org.anyline.jdbc.adapter.JDBCAdapter.BR_TAB);
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
						SQLUtil.delimiter(builder, column, delimiterFr, delimiterTo);
					} 
				} 
				if(i<size-1){ 
					builder.append(","); 
				} 
			} 
			builder.append(org.anyline.jdbc.adapter.JDBCAdapter.BR);
		}else{ 
			//全部查询 
			builder.append("*"); 
			builder.append(org.anyline.jdbc.adapter.JDBCAdapter.BR);
		} 
		builder.append("FROM").append(org.anyline.jdbc.adapter.JDBCAdapter.BR_TAB);
		if(null != schema){
			SQLUtil.delimiter(builder, schema, delimiterFr, delimiterTo).append(".");
		}
		SQLUtil.delimiter(builder, table, delimiterFr, delimiterTo);
		builder.append(JDBCAdapter.BR);
		if(BasicUtil.isNotEmpty(sql.getAlias())){
			//builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(sql.getAlias());
		}
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(org.anyline.jdbc.adapter.JDBCAdapter.BR_TAB).append(join.getType().getCode()).append(" ");
				SQLUtil.delimiter(builder, join.getName(), delimiterFr, delimiterTo);
				if(BasicUtil.isNotEmpty(join.getAlias())){
					//builder.append(" AS ").append(join.getAlias());
					builder.append("  ").append(join.getAlias());
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		builder.append("\nWHERE 1=1\n\t"); 
		 
 
 
		*//*添加查询条件*//*
		//appendConfigStore(); 
		appendCondition();
		appendGroup();
		appendOrderStore();
		checkValid();
	}*//*
	// adapter 中实现
	public void createRunDeleteTxt(){
		TablePrepare sql = (TablePrepare)this.getPrepare();
		builder.append("DELETE FROM ");
		if(null != schema){
			SQLUtil.delimiter(builder, schema, delimiterFr, delimiterTo).append(".");
		}

		SQLUtil.delimiter(builder, table, delimiterFr, delimiterTo);
		builder.append(org.anyline.jdbc.adapter.JDBCAdapter.BR);
		if(BasicUtil.isNotEmpty(sql.getAlias())){
			//builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(sql.getAlias());
		}
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(org.anyline.jdbc.adapter.JDBCAdapter.BR_TAB).append(join.getType().getCode()).append(" ");
				SQLUtil.delimiter(builder, join.getName(), getDelimiterFr(), getDelimiterTo());
				if(BasicUtil.isNotEmpty(join.getAlias())){
					builder.append("  ").append(join.getAlias());
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		builder.append("\nWHERE 1=1\n\t");



		*//*添加查询条件*//*
		//appendConfigStore();
		appendCondition();
		appendGroup();
		appendOrderStore();
		checkValid();
	}*/

	public void appendOrderStore(){
		 
	}
	public void appendGroup(){
		if(null != groupStore){
			builder.append(groupStore.getRunText(delimiterFr+delimiterTo));
		}
		if(BasicUtil.isNotEmpty(having)){
			builder.append(" HAVING ").append(having);
		} 
	}

	public void checkValid(){
		if(null != conditionChain && !conditionChain.isValid()){
			this.valid = false;
		}
	}
	/** 
	 * 拼接查询条件
	 */ 
	public void appendCondition(){
		if(null == conditionChain){ 
			return; 
		}
		builder.append(conditionChain.getRunText(adapter));
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
	public Run setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, RunPrepare.COMPARE_TYPE compare) {
		return this;
	}

	@Override
	public Run setConditionValue(boolean required, String condition, String variable, Object value, RunPrepare.COMPARE_TYPE compare) {
		return setConditionValue(required,  false, condition, variable, value, compare);
	}
} 
