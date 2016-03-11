/* 
 * Copyright 2006-2015 www.anyline.org
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


package org.anyline.config.db.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.KeyValueEncryptConfig;
import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.GroupStoreImpl;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.db.sql.auto.TableSQL;
import org.anyline.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigStoreImpl;
import org.anyline.util.BasicUtil;

public class TableRunSQLImpl extends BasicRunSQLImpl implements RunSQL{
	private String table;
	private String author;
	public TableRunSQLImpl(){
		this.conditionChain = new AutoConditionChainImpl();
		this.configStore = new ConfigStoreImpl();
		this.orderStore = new OrderStoreImpl();
	}
	private void parseDataSource(){
		table = sql.getTable();
		table = table.replace(disKeyFr, "").replace(disKeyTo, "");
		if(table.contains(".")){
			this.author = table.substring(0,table.indexOf("."));
			this.table = table.substring(table.indexOf(".") + 1);
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
		createRunTxt();
	
	}
	private void createRunTxt(){
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
				if(column.startsWith("{")){
					column = column.replace("{", "").replace("}", "");
					builder.append(column);
				}else{
					if(column.contains("AS") || column.contains("(") || column.contains(",")){
						builder.append(column);
					}else{
						builder.append(disKeyFr).append(column).append(disKeyTo);
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
			builder.append(disKeyFr).append(author).append(disKeyTo).append(".");
		}
		
		builder.append(disKeyFr).append(table).append(disKeyTo);
		builder.append(SQLCreater.BR);
		builder.append("\nWHERE 1=1\n\t");
		


		/*添加查询条件*/
		//appendConfigStore();
		appendCondition();
		appendGroup();
		appendOrderStore();
	}
	private void appendOrderStore(){
		
	}
	private void appendGroup(){
		if(null != groupStore){
			builder.append(groupStore.getRunText(disKeyFr+disKeyTo));
		}
	}
	/**
	 * 拼接查询条件
	 * @param builder
	 * @param sql
	 */
	private void appendCondition(){
		if(null == conditionChain){
			return;
		}
		builder.append(conditionChain.getRunText(disKeyFr+disKeyTo));
		addValues(conditionChain.getRunValues());
	}
	

	public void setConfigs(ConfigStore configs) {
		this.configStore = configs;
		if(null != configs){
			this.pageNavi = configs.getPageNavi();
			
		}
	}





	/*******************************************************************************************
	 * 
	 * 										添加条件
	 * 
	 ********************************************************************************************/
	/**
	 * 添加查询条件
	 * @param	required
	 * 			是否必须
	 * @param	column
	 * 			列名
	 * @param	value
	 * 			值
	 * @param	compare
	 * 			比较方式
	 */
	public RunSQL addCondition(boolean requried, String column, Object value, int compare){
		Condition condition = new AutoConditionImpl(requried,column, value, compare);
		if(null == conditionChain){
			conditionChain = new AutoConditionChainImpl();
		}
		conditionChain.addCondition(condition);
		return this;
	}

	/**
	 * 添加静态文本查询条件
	 */
	public RunSQL addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}
		if(condition.contains(":")){
			KeyValueEncryptConfig conf = new KeyValueEncryptConfig(condition);
			addCondition(conf.isRequired(),conf.getField(),conf.getKey(),SQL.COMPARE_TYPE_EQUAL);
		}else{
			Condition con = new AutoConditionImpl(condition);
			conditionChain.addCondition(con);
		}
		return this;
	}
	public RunSQL addCondition(Condition condition) {
		conditionChain.addCondition(condition);
		return this;
	}


	/**
	 * 添加参数值
	 * @param obj
	 * @return
	 */
	public RunSQL addValues(Object obj){
		if(null == obj){
			return this;
		}
		if(null == values){
			values = new ArrayList<Object>();
		}
		if(obj instanceof Collection){
			values.addAll((Collection)obj);
		}else{
			values.add(obj);
		}
		return this;
	}


	
}
