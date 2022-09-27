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
import org.anyline.jdbc.prepare.RunPrepare;
import org.anyline.jdbc.prepare.Variable;
import org.anyline.jdbc.param.Config;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.prepare.Condition;
import org.anyline.jdbc.prepare.RunPrepare.COMPARE_TYPE;
import org.anyline.jdbc.prepare.init.SimpleVariable;
import org.anyline.jdbc.prepare.auto.AutoCondition;
import org.anyline.jdbc.prepare.auto.init.SimpleAutoConditionChain;
import org.anyline.jdbc.prepare.auto.init.SimpleAutoCondition;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextRun extends BasicRun implements Run {
	public TextRun(){
		this.builder = new StringBuilder();
		this.conditionChain = new SimpleAutoConditionChain();
		this.orderStore = new OrderStoreImpl();
		setStrict(false); 
	}

	public Run setPrepare(RunPrepare prepare){
		this.prepare = prepare;
		this.table = prepare.getTable();
		parseText(); 
		return this; 
	} 
	public void init(){ 
		super.init(); 
		// 复制 RunPrepare 查询条件 
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
					Variable var = this.getVariable(con.getId());
					if(null != var){
						var.setValue(false, con.getValues());
					} 
				}
			} 
		} 
		if(null != configStore){ 
			for(Config conf:configStore.getConfigChain().getConfigs()){ 
				Condition con = getCondition(conf.getVariable());
				Variable var = this.getVariable(conf.getVariable());
				// sql体中有对应的变量
				if(null != con){
					setConditionValue( 
						conf.isRequire(), conf.isStrictRequired(), conf.getVariable(), conf.getVariable(), conf.getValues(), conf.getCompare());
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
	} 
	private void parseText(){ 
		String text = prepare.getText();
		if(null == text){
			return;
		} 
		try{ 
			int varType = -1; 
			COMPARE_TYPE compare = COMPARE_TYPE.EQUAL;
			List<List<String>> keys = RegularUtil.fetchs(text, RunPrepare.SQL_PARAM_VAIRABLE_REGEX, Regular.MATCH_MODE.CONTAIN);
			if(BasicUtil.isNotEmpty(true,keys)){ 
				// AND CD = :CD 
				for(int i=0; i<keys.size();i++){ 
					List<String> keyItem = keys.get(i); 
					String prefix = keyItem.get(1).trim(); 
					String fullKey = keyItem.get(2).trim();	// :CD ::CD 
					String typeChar = keyItem.get(3);		// null || "'" || ")" 
					String key = fullKey.replace(":", ""); 
					if(fullKey.startsWith("::")){ 
						// AND CD = ::CD 
						varType = Variable.VAR_TYPE_REPLACE;
					}else if(BasicUtil.isNotEmpty(typeChar) && ("'".equals(typeChar) || "%".equals(typeChar))){ 
						// AND CD = ':CD' 
						varType = Variable.VAR_TYPE_KEY_REPLACE;
					}else{ 
						// AND CD = :CD 
						varType = Variable.VAR_TYPE_KEY;
						// AND CD = :CD 
						varType = Variable.VAR_TYPE_KEY;
						if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){ 
							// AND CD IN(:CD) 
							compare = COMPARE_TYPE.IN;
						} 
					} 
					Variable var = new SimpleVariable();
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
						Variable var = new SimpleVariable();
						var.setType(Variable.VAR_TYPE_INDEX);
						addVariable(var); 
					} 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	}
	public void checkValid(){
		if(null != conditionChain && !conditionChain.isValid()){
			this.valid = false;
		}
	}
	public void appendGroup(){
		if(null != groupStore){ 
			builder.append(groupStore.getRunText(delimiterFr+delimiterTo));
		} 
	}
	/** 
	 * 拼接查询条件
	 */
	public void appendCondition(){
		if(null == conditionChain){ 
			return; 
		} 
		List<Condition> cons = conditionChain.getConditions(); 
		if(null == cons || cons.size()==0){ 
			return; 
		} 
		String txt = builder.toString();
		boolean where = endWithWhere(txt); 
		if(!where){ 
			builder.append(" WHERE 1=1"); 
		}
		builder.append(conditionChain.getRunText(null, adapter));
		addValues(conditionChain.getRunValues());
	}
	 
	public void setConfigs(ConfigStore configs) { 
		this.configStore = configs; 
		if(null != configs){ 
			this.pageNavi = configs.getPageNavi(); 
			 
		} 
	} 
 
 
 
	@Override 
	public Run setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, COMPARE_TYPE compare) {
		/*不指定变量名时,根据condition为SQL主体变量赋值*/ 
		if(null != variables && BasicUtil.isEmpty(variable)){ 
			for(Variable v:variables){
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
	public Run setConditionValue(boolean required, String condition, String variable, Object value, COMPARE_TYPE compare) {
		return setConditionValue(required, false, condition, variable, value, compare);
	}

		 
 
 
	/**
	 * 添加参数值 
	 * @param key  key
	 * @param obj  obj
	 * @return TextRun
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TextRun addValues(String key, Object obj){
		if(null == obj){ 
			return this; 
		} 
		if(null == values){ 
			values = new ArrayList<>();
		}
		if(null != obj && obj instanceof RunValue){
			throw new RuntimeException("run value");
		}
		if(obj instanceof Collection){
			Collection list = (Collection)obj;
			for(Object item:list){
				addValues(key,item);
			}
		}else{ 
			addValues(new RunValue(key, obj));
		} 
		return this; 
	} 

	public Run addOrders(OrderStore orderStore){
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
	public Run addOrder(Order order){
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
	 * @return Run
	 */
	public Run addCondition(String condition, String variable, Object value) {
		if(null != variables && BasicUtil.isEmpty(variable)){ 
			for(Variable v:variables){
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
	 * @param required  是否必须
	 * @param strictRequired  是否必须
	 * @param prefix 表名
	 * @param var  列名
	 * @param value  值
	 * @param compare  比较方式
	 * @return Run
	 */ 
	public Run addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, COMPARE_TYPE compare){
		Condition condition = new SimpleAutoCondition(required,strictRequired,prefix, var, value, compare);
		if(null == conditionChain){ 
			conditionChain = new SimpleAutoConditionChain();
		} 
		conditionChain.addCondition(condition); 
		return this; 
	} 
	public Variable getVariable(String key){
		if(null == key || null == variables){ 
			return null; 
		} 
		for(Variable var:variables){
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
