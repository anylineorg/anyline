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
 *          
 */


package org.anyline.config.db.run.impl; 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQL.COMPARE_TYPE;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.db.impl.SQLVariableImpl;
import org.anyline.config.db.run.RunSQL;
import org.anyline.config.db.sql.auto.AutoCondition;
import org.anyline.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigStoreImpl;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
 
public class TextRunSQLImpl extends BasicRunSQLImpl implements RunSQL{ 
	public TextRunSQLImpl(){ 
		this.conditionChain = new AutoConditionChainImpl(); 
		this.configStore = new ConfigStoreImpl(); 
		this.orderStore = new OrderStoreImpl();
		setStrict(false); 
	} 
	 
	public RunSQL setSql(SQL sql){ 
		this.sql = sql; 
		parseText(); 
		return this; 
	} 
	public void init(){ 
		super.init(); 
		//复制 SQL 查询条件 
		if(null != conditionChain){ 
 
			List<Condition> conditions = conditionChain.getConditions(); 
			if(null != conditions){ 
				for(Condition condition:conditions){
					if(null == condition){
						continue;
					} 
					AutoCondition con = (AutoCondition)condition; 
					setConditionValue(
							con.isRequired(), con.isStrictRequired(), con.getId(), null, con.getValues(), con.getCompare()); 
					SQLVariable var = this.getVariable(con.getId());
					if(null != var){
						var.setValue(false, con.getValues());
					} 
				}
			} 
		} 
		if(null != configStore){ 
			for(Config conf:configStore.getConfigChain().getConfigs()){ 
				Condition con = getCondition(conf.getId());
				SQLVariable var = this.getVariable(conf.getId());
				//sql体中有对应的变量
				if(null != con){
					setConditionValue( 
						conf.isRequire(), conf.isStrictRequired(), conf.getId(), conf.getVariable(), conf.getValues(), conf.getCompare());
				}
				if(null != var){
					var.setValue(false, conf.getValues());
				}
				
				if(null == var && null == con){
					conditionChain.addCondition(conf.createAutoCondition(conditionChain));
				}
			} 
			 
			OrderStore orderStore = configStore.getOrders(); 
			if(null != orderStore){ 
				List<Order> orders = orderStore.getOrders(); 
				if(null != orders){ 
					for(Order order:orders){ 
						addOrder(order); 
					} 
				} 
			} 
			PageNavi navi = configStore.getPageNavi(); 
			if(navi != null){ 
				this.pageNavi = navi; 
			} 
		} 
		createRunTxt();
		checkValid(); 
	} 
	private void parseText(){ 
		String text = sql.getText(); 
		if(null == text){
			return;
		} 
		try{ 
			int varType = -1; 
			COMPARE_TYPE compare = SQL.COMPARE_TYPE.EQUAL; 
			List<List<String>> keys = RegularUtil.fetchs(text, SQL.SQL_PARAM_VAIRABLE_REGEX, Regular.MATCH_MODE.CONTAIN); 
			if(BasicUtil.isNotEmpty(true,keys)){ 
				//AND CD = :CD 
				for(int i=0; i<keys.size();i++){ 
					List<String> keyItem = keys.get(i); 
					String prefix = keyItem.get(1).trim(); 
					String fullKey = keyItem.get(2).trim();	// :CD ::CD 
					String typeChar = keyItem.get(3);		//  null || "'" || ")" 
					String key = fullKey.replace(":", ""); 
					if(fullKey.startsWith("::")){ 
						// AND CD = ::CD 
						varType = SQLVariable.VAR_TYPE_REPLACE; 
					}else if(BasicUtil.isNotEmpty(typeChar) && ("'".equals(typeChar) || "%".equals(typeChar))){ 
						// AND CD = ':CD' 
						varType = SQLVariable.VAR_TYPE_KEY_REPLACE; 
					}else{ 
						// AND CD = :CD 
						varType = SQLVariable.VAR_TYPE_KEY; 
						// AND CD = :CD 
						varType = SQLVariable.VAR_TYPE_KEY; 
						if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){ 
							//AND CD IN(:CD) 
							compare = SQL.COMPARE_TYPE.IN; 
						} 
					} 
					SQLVariable var = new SQLVariableImpl(); 
					var.setKey(key); 
					var.setType(varType); 
					var.setCompare(compare); 
					addVariable(var); 
				}// end for 
			}else{ 
				// AND CD = ? 
				List<String> idxKeys = RegularUtil.fetch(text, "\\?",Regular.MATCH_MODE.CONTAIN,0); 
				if(BasicUtil.isNotEmpty(true,idxKeys)){ 
					for(int i=0; i<idxKeys.size(); i++){ 
						SQLVariable var = new SQLVariableImpl(); 
						var.setType(SQLVariable.VAR_TYPE_INDEX); 
						addVariable(var); 
					} 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	}
	private void checkValid(){
		if(null != conditionChain && !conditionChain.isValid()){
			this.valid = false;
		}
	} 
	private void createRunTxt(){ 
		String result = sql.getText(); 
		if(null != variables){ 
			for(SQLVariable var:variables){
				if(null == var){
					continue;
				} 
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
				if(null == var){
					continue;
				} 
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
				if(null == var){
					continue;
				} 
				if(var.getType() == SQLVariable.VAR_TYPE_KEY){ 
					// CD = :CD 
					List<Object> varValues = var.getValues(); 
					if(BasicUtil.isNotEmpty(true, varValues)){ 
						if(var.getCompare() == SQL.COMPARE_TYPE.IN){ 
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
				if(null == var){
					continue;
				} 
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
		//appendOrderStore(); 
	} 
	private void appendGroup(){ 
		if(null != groupStore){ 
			builder.append(groupStore.getRunText(disKeyFr+disKeyTo)); 
		} 
	}
	/** 
	 * 拼接查询条件 
	 * @param builder  builder
	 * @param sql  sql
	 */ 
	private void appendCondition(){ 
		if(null == conditionChain){ 
			return; 
		} 
		List<Condition> cons = conditionChain.getConditions(); 
		if(null == cons || cons.size()==0){ 
			return; 
		} 
		String txt = builder.toString(); 
		boolean where = hasWhere(txt); 
		if(!where){ 
			builder.append(" WHERE 1=1"); 
		}
		builder.append(conditionChain.getRunText(creater));
		addValues(conditionChain.getRunValues());
//		 
//		int idx = 0; 
//		for(Condition con:cons){ 
//			SQLVariable var = getVariable(con.getId()); 
//			if(null != var){ 
//				//sql主体变量 
//				continue; 
//			} 
//			if(idx > 0){ 
//				builder.append(" AND "); 
//			} 
//			builder.append(con.getRunText(creater)); 
//			addValues(con.getRunValues()); 
//			idx ++; 
//		} 
	} 
	 
	public void setConfigs(ConfigStore configs) { 
		this.configStore = configs; 
		if(null != configs){ 
			this.pageNavi = configs.getPageNavi(); 
			 
		} 
	} 
 
 
 
	@Override 
	public RunSQL setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, SQL.COMPARE_TYPE compare) { 
		/*不指定变量名时,根据condition为SQL主体变量赋值*/ 
		if(null != variables && BasicUtil.isEmpty(variable)){ 
			for(SQLVariable v:variables){
				if(null == v){
					continue;
				} 
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
 
	@Override
	public RunSQL setConditionValue(boolean required, String condition, String variable, Object value, SQL.COMPARE_TYPE compare) {
		return setConditionValue(required, false, condition, variable, value, compare);
	}

		 
 
 
	/** 
	 * 添加参数值 
	 * @param obj  obj
	 * @return return
	 */ 
	public TextRunSQLImpl addValues(Object obj){ 
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
	 
	 
	 
 
 
	/* ****************************************************************************************** 
	 *  
	 * 										添加条件 
	 *  
	 * *******************************************************************************************/ 
 
	/** 
	 * 添加静态文本查询条件 
	 * @param condition condition
	 * @param variable variable
	 * @param value value
	 * @return return
	 */
	public RunSQL addCondition(String condition, String variable, Object value) { 
		if(null != variables && BasicUtil.isEmpty(variable)){ 
			for(SQLVariable v:variables){
				if(null == v){
					continue;
				} 
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
	public RunSQL addCondition(boolean required, boolean strictRequired, String column, Object value, COMPARE_TYPE compare){ 
		Condition condition = new AutoConditionImpl(required,strictRequired,column, value, compare); 
		if(null == conditionChain){ 
			conditionChain = new AutoConditionChainImpl(); 
		} 
		conditionChain.addCondition(condition); 
		return this; 
	} 
	private SQLVariable getVariable(String key){ 
		if(null == key || null == variables){ 
			return null; 
		} 
		for(SQLVariable var:variables){
			if(null == var){
				continue;
			} 
			if(key.equalsIgnoreCase(var.getKey())){ 
				return var; 
			} 
		} 
		return null; 
	} 
}
