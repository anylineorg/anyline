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


package org.anyline.data.run;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.util.BasicUtil;

import java.util.List;

public class TableRun extends BasicRun implements Run {

	public TableRun(DataRuntime runtime, String table){
		this.builder = new StringBuilder();
		this.conditionChain = new DefaultAutoConditionChain();
		this.orderStore = new DefaultOrderStore();
		this.table = table;
		this.runtime = runtime;
	}

	private void parseDataSource(){
		if(null != prepare) {
			table = prepare.getTable();
		}
		table = table.replace(delimiterFr, "").replace(delimiterTo, "");
		if(table.contains(".")){
			this.schema = table.substring(0, table.indexOf("."));
			this.table = table.substring(table.indexOf(".") + 1); 
		} else{
			if(null != prepare && BasicUtil.isNotEmpty(prepare.getSchema())){
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
				//for(Condition condition:chain.getConditions()){
				//	addCondition(condition);
				//}
				addCondition(chain);
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

	public boolean checkValid(){
		if(!valid){
			return false;
		}
		if(null != conditionChain && !conditionChain.isValid()){
			this.valid = false;
		}
		if(null != configStore && !configStore.isValid()){
			this.valid = false;
		}
		return valid;
	}
	/** 
	 * 拼接查询条件
	 */ 
	public void appendCondition(){
		if(null == conditionChain){
			return; 
		}
		String alias = null;
		if(null != prepare){
			alias = prepare.getAlias();
		}
		builder.append(conditionChain.getRunText(alias, runtime));
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
	public Run setConditionValue(Compare.EMPTY_VALUE_SWITCH swt, Compare compare, String condition, String variable, Object value) {
		return this;
	}

} 
