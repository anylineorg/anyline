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
 *
 *          
 */


package org.anyline.data.run;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.SyntaxHelper;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.data.prepare.init.DefaultVariable;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.DefaultOrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.Condition;
import org.anyline.entity.Compare;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextRun extends BasicRun implements Run {
	public TextRun(){
		this.builder = new StringBuilder();
		this.conditionChain = new DefaultAutoConditionChain();
		this.orderStore = new DefaultOrderStore();
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

					//如果有对应的SQL体变量 设置当前con不作为查询条件拼接
					List<Variable> vars = this.getVariables(con.getId());
					if(vars.size() > 0){
						//用来给java/xml定义SQL中变量赋值,本身并不拼接到最终SQL
						con.setVariableSlave(true);
						for(Variable var:vars){
							var.setValue(false, con.getValues());
						}
					} else{
						//查询条件和SQL体变量赋值
						setConditionValue(con.isRequired(), con.isStrictRequired(), con.getId(), null, con.getValues(), con.getCompare());
					}
				}
			} 
		} 
		if(null != configStore){
			List<Config> confs = configStore.getConfigChain().getConfigs();
			for(Config conf:confs){
				//是否覆盖相同var key的条件
				boolean overCondition = conf.isOverCondition();
				//是否相同var key的条件的value
				boolean overValue = conf.isOverValue();
				List<Object> values = conf.getValues();
				Compare compare = conf.getCompare();
				//变量key 如:ID #{ID} 中的ID
				String varKey = conf.getVariable();
				//SQL主体变量
				List<Variable> vars = this.getVariables(varKey);
				//查询条件
				List<Condition> cons = getConditions(varKey);
				//是否已用来赋值
				boolean isUse = false;
				for (Condition con : cons) {
					if (null != con) {
						//如果有对应的SQL体变量 设置当前con不作为查询条件拼接

						//当前条件相就的变量是否赋值过
						boolean isConVarSetValue = con.isSetValue() || con.isSetValue(varKey);
						if(!isConVarSetValue || overValue) {
							isUse = true;
							con.setVariableSlave(true);
							setConditionValue(conf.isRequire(), conf.isStrictRequired(), varKey, varKey, values, conf.getCompare());
						}
					}
				}

				for (Variable var : vars) {
					if(overValue || !var.isSetValue()) {
						isUse = true;
						var.setValue(false, values);
					}
				}
				if(compare != Compare.NONE) {
					//如果没有对应的查询条件和SQL体变量，新加一个条件
					//不覆盖条件 则添加新条件
					if(!overCondition && !isUse){
						conditionChain.addCondition(conf.createAutoCondition(conditionChain));
					}
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
			Compare compare = Compare.EQUAL;

			List<List<String>> keys = null;
			int type = 0;
			// AND CD = {CD} || CD LIKE '%{CD}%' || CD IN ({CD}) || CD = ${CD} || CD = #{CD}
			//{CD} 用来兼容旧版本，新版本中不要用，避免与josn格式冲突
			keys = RegularUtil.fetchs(text, RunPrepare.SQL_PARAM_VARIABLE_REGEX_EL, Regular.MATCH_MODE.CONTAIN);
			type = Variable.KEY_TYPE_SIGN_V2 ;
			if(keys.size() == 0){
				// AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD
				keys = RegularUtil.fetchs(text, RunPrepare.SQL_PARAM_VARIABLE_REGEX, Regular.MATCH_MODE.CONTAIN);
				type = Variable.KEY_TYPE_SIGN_V1 ;
			}
			if(BasicUtil.isNotEmpty(true,keys)){ 
				// AND CD = :CD 
				for(int i=0; i<keys.size();i++){ 
					List<String> keyItem = keys.get(i);

					Variable var = SyntaxHelper.buildVariable(type, keyItem.get(0), keyItem.get(1), keyItem.get(2), keyItem.get(3));
					if(null == var){
						continue;
					}
					/*String prefix = keyItem.get(1).trim(); 	//
					String fullKey = keyItem.get(2).trim();	// :CD ::CD {CD} ${CD}
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
							compare = Compare.IN;
						} 
					} 
					Variable var = new DefaultVariable();
					var.setKey(key); 
					var.setType(varType); 
					var.setCompare(compare); 
					addVariable(var);*/
					var.setRequired(true);
					addVariable(var);
				}// end for 
			}else{ 
				// AND CD = ? 
				List<String> idxKeys = RegularUtil.fetch(text, "\\?",Regular.MATCH_MODE.CONTAIN,0); 
				if(BasicUtil.isNotEmpty(true,idxKeys)){ 
					for(int i=0; i<idxKeys.size(); i++){ 
						Variable var = new DefaultVariable();
						var.setType(Variable.VAR_TYPE_INDEX);
						var.setRequired(true);
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
	public Run setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, Compare compare) {
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
	public Run setConditionValue(boolean required, String condition, String variable, Object value, Compare compare) {
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
	public Run addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, Compare compare){
		Condition condition = new DefaultAutoCondition(required,strictRequired,prefix, var, value, compare);
		if(null == conditionChain){ 
			conditionChain = new DefaultAutoConditionChain();
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
	public List<Variable> getVariables(String key){
		List<Variable> list = new ArrayList<>();
		if(null == key || null == variables){
			return list;
		}
		for(Variable var:variables){
			if(null == var){
				continue;
			}
			if(key.equalsIgnoreCase(var.getKey())){
				list.add(var);
			}
		}
		return list;
	}

}
