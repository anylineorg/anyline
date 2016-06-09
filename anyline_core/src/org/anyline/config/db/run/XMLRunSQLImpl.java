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
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.config.db.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.KeyValueEncryptConfig;
import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.Group;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.GroupStoreImpl;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.db.sql.xml.impl.XMLConditionChainImpl;
import org.anyline.config.db.sql.xml.impl.XMLConditionImpl;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigStoreImpl;
import org.anyline.util.BasicUtil;

public class XMLRunSQLImpl extends BasicRunSQLImpl implements RunSQL{
	private List<String> conditions;
	public XMLRunSQLImpl(){
		this.conditionChain = new XMLConditionChainImpl();
		this.configStore = new ConfigStoreImpl();
		this.orderStore = new OrderStoreImpl();
		this.groupStore = new GroupStoreImpl();
	}
	public void init(){
		super.init();
		//复制XML SQL 变量
		List<SQLVariable> vars = sql.getSQLVariables();
		if(null != vars){
			if(null == this.variables){
				variables = new ArrayList<SQLVariable>();
			}
			for(SQLVariable var:vars){
				try{
					variables.add((SQLVariable)var.clone());
				}catch(Exception e){
					
				}
			}
		}
		//复制XML SQL 查询条件
		ConditionChain conditionChain = sql.getConditionChain();
		if(null != conditionChain){
			if(null == this.conditionChain){
				this.conditionChain = new XMLConditionChainImpl();
			}
			List<Condition> conditions = conditionChain.getConditions();
			if(null != conditions){
				for(Condition condition:conditions){
					try{
						this.conditionChain.addCondition((Condition)condition.clone());
					}catch(Exception e){
						
					}
				}
			}
		}
		if(null != configStore){
			for(Config conf:configStore.getConfigChain().getConfigs()){
				setConditionValue(conf.getId(), conf.getVariable(), conf.getValues());
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
		//condition赋值
		if(null != conditions){
			for(String condition:conditions){
				if(BasicUtil.isEmpty(condition)){
					continue;
				}
				if(condition.contains(":")){
					KeyValueEncryptConfig conf = new KeyValueEncryptConfig(condition);
					addCondition(false,conf.getField(),conf.getKey(),SQL.COMPARE_TYPE_EQUAL);
				}else{
					Condition con = new XMLConditionImpl(condition);
					conditionChain.addCondition(con);
				}
			}
		}
		GroupStore groupStore = sql.getGroups();
		if(null != groupStore){
			List<Group> groups = groupStore.getGroups();
			if(null != groups){
				for(Group group:groups){
					this.groupStore.group(group);
				}
			}
		}
		createRunTxt();
	}
	private void createRunTxt(){
//		if(null != configStore){
//			for(Config conf:configStore.getConfigChain().getConfigs()){
//				setConditionValue(conf.getId(), conf.getVariable(), conf.getValues());
//			}
//		}
		
		String result = sql.getText();
		if(null != variables){
			for(SQLVariable var:variables){
				if(var.getType() == SQLVariable.VAR_TYPE_REPLACE){
					//CD = ::CD
					Object varValue = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(varValue)){
						value = varValue.toString();
					}
					if(null != value){
						result = result.replace("::"+var.getKey(), value);
					}else{
						result = result.replace("::"+var.getKey(), "NULL");
					}
				}
			}
			for(SQLVariable var:variables){
				if(var.getType() == SQLVariable.VAR_TYPE_KEY_REPLACE){
					//CD = ':CD'
					List<Object> varValues = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true,varValues)){
						value = (String)varValues.get(0);
					}
					if(null != value){
						result = result.replace(":"+var.getKey(), value);
					}else{
						result = result.replace(":"+var.getKey(), "");
					}
				}
			}
			for(SQLVariable var:variables){
				if(var.getType() == SQLVariable.VAR_TYPE_KEY){
					// CD = :CD
					List<Object> varValues = var.getValues();
					if(BasicUtil.isNotEmpty(true, varValues)){
						if(var.getCompare() == SQL.COMPARE_TYPE_IN){
							//多个值IN
							String replaceSrc = ":"+var.getKey();
							String replaceDst = ""; 
							for(Object tmp:varValues){
								addValues(tmp);
								replaceDst += " ?";
							}
							replaceDst = replaceDst.trim().replace(" ", ",");
							result = result.replace(replaceSrc, replaceDst);
						}else{
							//单个值
							result = result.replace(":"+var.getKey(), "?");
							addValues(varValues.get(0));
						}
					}
				}
			}
			//添加其他变量值
			for(SQLVariable var:variables){
				//CD = ?
				if(var.getType() == SQLVariable.VAR_TYPE_INDEX){
					List<Object> varValues = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true, varValues)){
						value = (String)varValues.get(0);
					}
					addValues(value);
				}
			}
		}
		
		builder.append(result);
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
		if(null == conditionChain || !conditionChain.isActive()){
			return;
		}
		if(!hasWhere(builder.toString())){
			builder.append(" WHERE 1=1");
		}
		builder.append(conditionChain.getRunText(creater));
		addValues(conditionChain.getRunValues());
	}
	
	public void setConfigs(ConfigStore configs) {
		this.configStore = configs;
		if(null != configs){
			this.pageNavi = configs.getPageNavi();
			
		}
	}



	@Override
	public RunSQL setConditionValue(String condition, String variable, Object value) {
		/*不指定变量名时,根据condition为SQL主体变量赋值*/
		if(null != variables && BasicUtil.isEmpty(variable)){
			for(SQLVariable v:variables){
				if(v.getKey().equalsIgnoreCase(condition)){
					v.setValue(value);
				}
			}
		}
		/*参数赋值*/
		if(null == condition){
			return this;
		}
		Condition con = getCondition(condition);
		if(null == con){
			return this;
		}
		variable = BasicUtil.nvl(variable, condition).toString();
		con.setValue(variable, value);
		if(con.isActive()){
			this.conditionChain.setActive(true);
		}
		return this;
	}
	public void setConditions(String[] conditions) {
		/*添加查询条件*/
		if(null != conditions){
			for(String condition:conditions){
				if(condition.toUpperCase().contains("ORDER BY")){
					String orderStr = condition.toUpperCase().replace("ORDER BY", "");
					String orders[] = orderStr.split(",");
					for(String item:orders){
						sql.order(item);
						if(null != configStore){
							configStore.order(item);
						}
						if(null != this.orderStore){
							this.orderStore.order(item);
						}
					}
					continue;
				}else if(condition.toUpperCase().contains("GROUP BY")){
					String groupStr = condition.toUpperCase().replace("GROUP BY", "");
					String groups[] = groupStr.split(",");
					for(String item:groups){
						sql.group(item);
						if(null != configStore){
							configStore.group(item);
						}
					}
					continue;
				}
				addCondition(condition);
			}
		}
	}

	


	/**
	 * 添加参数值
	 * @param obj
	 * @return
	 */
	public XMLRunSQLImpl addValues(Object obj){
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
	
	public RunSQL addOrders(OrderStore orderStore){
		if(null == orderStore){
			return this;
		}
		List<Order> orders = orderStore.getOrders();
		if(null == orders){
			return this;
		}
		for(Order order:orders){
			this.orderStore.order(order);
		}
		return this;
	}
	public RunSQL addOrder(Order order){
		this.orderStore.order(order);
		return this;
	}
	
	
	


	/*******************************************************************************************
	 * 
	 * 										添加条件
	 * 
	 ********************************************************************************************/

	/**
	 * 添加静态文本查询条件
	 */
	public RunSQL addCondition(String condition, String variable, Object value) {
		if(null != variables && BasicUtil.isEmpty(variable)){
			for(SQLVariable v:variables){
				if(v.getKey().equalsIgnoreCase(condition)){
					v.setValue(value);
				}
			}
		}
		/*参数赋值*/
		if(null == condition){
			return this;
		}
		Condition con = getCondition(condition);
		if(null == con){
			return this;
		}
		variable = BasicUtil.nvl(variable, condition).toString();
		con.setValue(variable, value);
		return this;
	}

	public void setConfigStore(ConfigStore configStore) {
		this.configStore = configStore;
	}
	public RunSQL addCondition(boolean requried, String column, Object value, int compare){
		setConditionValue(column, null, value);
		return this;
	}
	
	public RunSQL addCondition(String condition){
		if(null == this.conditions){
			this.conditions = new ArrayList<String>();
		}
		this.conditions.add(condition);
		return this;
	}
	
}
