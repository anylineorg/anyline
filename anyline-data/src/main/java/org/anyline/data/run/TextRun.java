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

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class TextRun extends AbstractRun implements Run {

	public TextRun() {
		this.builder = new StringBuilder();
		this.conditionChain = new DefaultAutoConditionChain();
		this.orders = new DefaultOrderStore();
	}

	public Run setPrepare(RunPrepare prepare) {
		this.prepare = prepare;
		this.table = prepare.getTable();
		setText(prepare.getText());
		return this; 
	}
	public void init() {
		super.init(); 
		// 复制 RunPrepare 查询条件 
		if(null != conditionChain) {
			List<Condition> conditions = conditionChain.getConditions(); 
			if(null != conditions) {
				for(Condition condition:conditions) {
					if(null == condition) {
						continue;
					}
					if(condition instanceof AutoCondition){
						AutoCondition auto = (AutoCondition)condition;
						//如果有对应的SQL体变量 设置当前con不作为查询条件拼接
						List<Variable> vars = this.getVariables(auto.getId());
						if(!vars.isEmpty()) {
							//用来给java/xml定义SQL中变量赋值, 本身并不拼接到最终SQL
							auto.setVariableSlave(true);
							for(Variable var:vars) {
								var.setValue(false, auto.getValues());
							}
						} else{
							//查询条件和SQL体变量赋值
							setConditionValue(auto.getSwt(), auto.getCompare(), auto.getId(), null, auto.getValues());
						}
					}
				}
			} 
		}
		//configStore解析与Table不一样, txt需要检测sql体中有没有需要赋值的占位符，所以不能全部用来生成新条件
		if(null != configs) {
			List<Config> confs = configs.getConfigChain().getConfigs();
			for(Config conf:confs) {
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
				boolean isUse = false;
				//相同key的查询条件
				if(overCondition) {
					List<Condition> cons = getConditions(varKey);
					//是否已用来赋值
					for (Condition con : cons) {
						if (null != con) {
							//如果有对应的SQL体变量 设置当前con不作为查询条件拼接
							//当前条件相就的变量是否赋值过
							boolean isConVarSetValue = con.isSetValue() || con.isSetValue(varKey);
							if (!isConVarSetValue || overValue) {
								isUse = true;
								con.setVariableSlave(true);
								setConditionValue(conf.getSwt(), conf.getCompare(), varKey, varKey, values);
							}
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
					//没有用过给其他参数赋值 则添加新条件
					//if(!overCondition && !isUse) {
					if(!isUse) {
						conditionChain.addCondition(conf.createAutoCondition(conditionChain));
					}
				}
			}
			LinkedHashMap<String, Config> params = configs.params();
			for(String key:params.keySet()){
				Config conf = params.get(key);
				//SQL主体变量 + 查询条件变量
				List<Variable> vars = this.getVariables(key);
				boolean overCondition = conf.isOverCondition();
				boolean overValue = conf.isOverValue();
				//相同key的variable
				List<Object> values = conf.getValues();
				for (Variable var : vars) {
					if(overValue || !var.isSetValue()) {
						var.setValue(false, values);
					}
				}
				//相同key的查询条件
				if(overCondition) {
					List<Condition> cons = getConditions(key);
					//是否已用来赋值
					for (Condition con : cons) {
						if (null != con) {
							//如果有对应的SQL体变量 设置当前con不作为查询条件拼接
							//当前条件相就的变量是否赋值过
							boolean isConVarSetValue = con.isSetValue() || con.isSetValue(key);
							if (!isConVarSetValue || overValue) {
								con.setVariableSlave(true);
								setConditionValue(conf.getSwt(), conf.getCompare(), key, key, values);
							}
						}
					}
				}
			}
			//根据下标赋值
			List<Object> values = configs.values();
			if(null != values) {
				int i = 0;
				int len = values.size();
				List<Variable> vars = this.variables;
				if(null == vars) {
					vars = new ArrayList<>();
				}
				if(vars.isEmpty()){
					vars = conditionChain.getVariables();
				}
				for(Variable var:vars) {
					if(i < len) {
						var.setValue(values.get(i));
						i++;
					}else {
						break;
					}
				}
			}
			this.add(configs.getOrders());

			PageNavi navi = configs.getPageNavi();
			if(navi != null) {
				this.pageNavi = navi; 
			} 
		}
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
	public void appendGroup(DataRuntime runtime, Boolean placeholder, Boolean unicode) {
		if(null != configs) {
			if (null == groups) {
				groups = configs.getGroups();
			}
			if (having.isEmptyCondition()) {
				having = configs.having();
			}
		}

		if(null != groups) {
			builder.append("\n").append(groups.getRunText(delimiterFr+delimiterTo));
		}

		if(null != having) {
			String txt = SQLUtil.trim(having.getRunText(runtime, false, unicode));
			if(BasicUtil.isNotEmpty(txt)) {
				builder.append("\nHAVING ").append(txt);
			}
		}
	}

	/** 
	 * 拼接查询条件
	 */
	public void appendCondition(Boolean placeholder, Boolean unicode) {
		if(null == conditionChain) {
			return; 
		} 
		List<Condition> cons = conditionChain.getConditions(); 
		if(null == cons || cons.isEmpty()) {
			return; 
		} 
		String txt = builder.toString();

		String condition = conditionChain.getRunText(null, runtime, placeholder, false);
		boolean where = endWithWhere(txt);
		if(BasicUtil.isNotEmpty(condition)) {
			emptyCondition = false;
			if (!where) {
				builder.append("\nWHERE ");
				condition = SQLUtil.trim(condition);
			}
			builder.append(condition);
		}

		if(where) {
			emptyCondition = false;
		}
		addValues(conditionChain.getRunValues());
	}
	 
	public void setConfigs(ConfigStore configs) {
		this.configs = configs;
		if(null != configs) {
			this.pageNavi = configs.getPageNavi(); 
			 
		} 
	} 

	@Override 
	public Run setConditionValue(EMPTY_VALUE_SWITCH swt, Compare compare, String condition, String variable, Object value) {
		/*不指定变量名时, 根据condition为SQL主体变量赋值*/ 
		if(!variables.isEmpty() && BasicUtil.isEmpty(variable)) {
			for(Variable v:variables) {
				if(null == v) {
					continue;
				} 
				if(v.getKey().equalsIgnoreCase(condition)) {
					v.setValue(value); 
				} 
			} 
		} 
		/*参数赋值*/ 
		if(null == condition) {
			return this; 
		} 
		Condition con = getCondition(condition); 
		if(null == con) {
			return this; 
		} 
		variable = BasicUtil.nvl(variable, condition).toString();
		con.setValue(variable, value);
		return this; 
	} 

		 
 
 
	/**
	 * 添加参数值 
	 * @param key  key
	 * @param obj  obj
	 * @return TextRun
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	public TextRun addValues(String key, Object obj) {
		if(null == obj) {
			return this; 
		} 
		if(null == values) {
			values = new ArrayList<>();
		}
		if(null != obj && obj instanceof RunValue) {
			throw new RuntimeException("run value");
		}
		if(obj instanceof Collection) {
			Collection list = (Collection)obj;
			for(Object item:list) {
				addValues(key, item);
			}
		}else{
			addValues(new RunValue(key, obj));
		} 
		return this; 
	} 

	public Run add(OrderStore orders) {
		this.orders.add(orders);
		return this; 
	} 
	public Run add(Order order) {
		this.orders.add(order);
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
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	public Run addCondition(String condition, String variable, Object value) {
		if(!variables.isEmpty() && BasicUtil.isEmpty(variable)) {
			for(Variable v:variables) {
				if(null == v) {
					continue;
				} 
				if(v.getKey().equalsIgnoreCase(condition)) {
					v.setValue(value); 
				} 
			} 
		} 
		/*参数赋值*/ 
		if(null == condition) {
			return this; 
		} 
		Condition con = getCondition(condition); 
		if(null == con) {
			return this; 
		} 
		variable = BasicUtil.nvl(variable, condition).toString();
		con.setValue(variable, value);
		return this; 
	} 
 
	/** 
	 * 添加查询条件 
	 * @param swt  空值处理方式
	 * @param prefix 表名
	 * @param var  列名
	 * @param value  值
	 * @param compare  比较方式
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	public Run addCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value) {
		Condition condition = new DefaultAutoCondition(swt, compare, prefix, var, value);
		if(null == conditionChain) {
			conditionChain = new DefaultAutoConditionChain();
		} 
		conditionChain.addCondition(condition); 
		return this; 
	}
	public Variable getVariable(String key) {
		if(null == key) {
			return null;
		}
		for(Variable var:variables) {
			if(null == var) {
				continue;
			}
			if(key.equalsIgnoreCase(var.getKey())) {
				return var;
			}
		}
		return null;
	}

	public List<Variable> getVariables(String key) {
		List<Variable> list = new ArrayList<>();
		if(null == key) {
			return list;
		}
		//sql主体变量
		for(Variable var:variables) {
			if(null == var) {
				continue;
			}
			if(key.equalsIgnoreCase(var.getKey())) {
				list.add(var);
			}
		}
		//查询条件变量
		for(Condition condition:conditionChain.getConditions()){
			Variable var = condition.getVariable(key);
			if(null != var){
				list.add(var);
			}
		}
		return list;
	}
	protected boolean supportSqlVarPlaceholderRegexExt = true;
	public boolean supportSqlVarPlaceholderRegexExt() {
		return supportSqlVarPlaceholderRegexExt;
	}
	public void supportSqlVarPlaceholderRegexExt(boolean supportSqlVarPlaceholderRegexExt) {
		this.supportSqlVarPlaceholderRegexExt = supportSqlVarPlaceholderRegexExt;
	}
}
