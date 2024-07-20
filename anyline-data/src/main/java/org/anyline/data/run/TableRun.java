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

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;

import java.util.List;

public class TableRun extends AbstractRun implements Run {

	public TableRun(DataRuntime runtime, String table) {
		this.builder = new StringBuilder();
		this.conditionChain = new DefaultAutoConditionChain();
		this.orderStore = new DefaultOrderStore();
		this.table = new Table(table);
		this.runtime = runtime;
	}
	public TableRun(DataRuntime runtime, Table table) {
		this.builder = new StringBuilder();
		this.conditionChain = new DefaultAutoConditionChain();
		this.orderStore = new DefaultOrderStore();
		this.table = table;
		this.runtime = runtime;
	}

	private void parseDataSource() {
		String table = getTableName();
		if(null != prepare) {
			table = prepare.getTableName();
		}
		table = table.replace(delimiterFr, "").replace(delimiterTo, "");
		if(table.contains(".")) {

		} else{
			if(null != prepare && BasicUtil.isNotEmpty(prepare.getSchema())) {
				schema = prepare.getSchema();
			}
		}
	} 
	public void init() {
		super.init(); 
		parseDataSource(); 
		if(null != configs) {
			ConditionChain chain = configs.getConfigChain().createAutoConditionChain();
			if(null != chain) {
				//for(Condition condition:chain.getConditions()) {
				//	addCondition(condition);
				//}
				if(this.conditionChain.getConditions().isEmpty()){
					//去除最外一层多余()
					this.conditionChain = chain;
				}else{
					addCondition(chain);
				}
			}
			OrderStore orderStore = configs.getOrders();
			if(null != orderStore) {
				List<Order> orders = orderStore.getOrders(); 
				if(null != orders) {
					for(Order order:orders) {
						this.orderStore.order(order); 
					} 
				} 
			} 
			PageNavi navi = configs.getPageNavi();
			if(navi != null) {
				this.pageNavi = navi; 
			} 
		}
	}

	public void appendOrderStore() {
		 
	}
	public void appendGroup(StringBuilder builder) {
		if(null != configs) {
			if (null == groupStore) {
				groupStore = configs.getGroups();
			}
			if (BasicUtil.isEmpty(having)) {
				having = configs.getHaving();
			}
		}

		if(null != groupStore) {
			builder.append(groupStore.getRunText(delimiterFr+delimiterTo));
		}
		if(BasicUtil.isNotEmpty(having)) {
			if(having.trim().toUpperCase().startsWith("HAVING")) {
				builder.append(having);
			}else {
				builder.append(" HAVING ").append(having);
			}
		} 
	}
	public void appendGroup() {
		appendGroup(builder);
	}

	public boolean checkValid() {
		if(!valid) {
			return false;
		}
		if(null != conditionChain && !conditionChain.isValid()) {
			this.valid = false;
		}
		if(null != configs && !configs.isValid()) {
			this.valid = false;
		}
		return valid;
	}

	/**
	 * 拼接查询条件
	 * @param first 是否首位条件 如果是需要加where
	 * @param placeholder 是否需要占位符
	 */
	public void appendCondition(StringBuilder builder, DriverAdapter adapter, boolean first, boolean placeholder) {
		if(null == conditionChain) {
			return; 
		}
		String alias = null;
		if(null != prepare) {
			alias = prepare.getAlias();
		}
		String condition = conditionChain.getRunText(alias, runtime, placeholder);
		if(!condition.isEmpty()) {
			emptyCondition = false;
			if(first) {
				builder.append("\n").append(adapter.conditionHead()).append(" ");
				condition = SQLUtil.trim(condition);
			}
			builder.append(condition);
		}
		List<RunValue> values = conditionChain.getRunValues();
		addValues(values);
	}

	public void appendCondition(DriverAdapter adapter, boolean first, boolean placeholder) {
		appendCondition(builder, adapter, first, placeholder);
	}
	public void setConfigs(ConfigStore configs) {
		this.configs = configs;
		if(null != configs) {
			this.pageNavi = configs.getPageNavi();
		} 
	}

	@Override
	public Run setConditionValue(Compare.EMPTY_VALUE_SWITCH swt, Compare compare, String condition, String variable, Object value) {
		return this;
	}

	@Override
	public String getTableName() {
		if(null != table) {
			return table.getName();
		}
		return null;
	}

	@Override
	public String getCatalogName() {
		if(null != catalog) {
			return catalog.getName();
		}
		return null;
	}

	@Override
	public String getSchemaName() {
		if(null != schema) {
			return schema.getName();
		}
		return null;
	}
}
