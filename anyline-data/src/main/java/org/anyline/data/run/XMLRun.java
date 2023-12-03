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

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.ParseResult;
import org.anyline.data.prepare.*;
import org.anyline.data.prepare.init.DefaultGroupStore;
import org.anyline.data.prepare.xml.init.DefaultXMLConditionChain;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.Column;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DefaultOgnlMemberAccess;

import java.util.*;

public class XMLRun extends BasicRun implements Run {
	private List<String> conditions; 
	private List<String> staticConditions; 
	public XMLRun(){
		this.builder = new StringBuilder();
		this.conditionChain = new DefaultXMLConditionChain();
		this.orderStore = new DefaultOrderStore();
		this.groupStore = new DefaultGroupStore();
	} 
 
	public Run setPrepare(RunPrepare prepare){
		this.prepare = prepare;
		this.table = prepare.getTable();
		copyParam(); 
		return this; 
	}

	public void init(){
		super.init(); 
		if(null != configStore){
			for(Config conf:configStore.getConfigChain().getConfigs()){
				setConditionValue(conf.getSwitch(), conf.getCompare(), conf.getPrefix(), conf.getVariable(), conf.getValues());
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
		// condition赋值 
		if(null != conditions){
			for(String condition:conditions){
				ParseResult parser = ConfigParser.parse(condition,false);
				Object value = ConfigParser.getValues(parser);// parser.getKey(); 
				if(parser.getParamFetchType() == ParseResult.FETCH_REQUEST_VALUE_TYPE_MULTIPLE){
					 value = BeanUtil.object2list(value);
				} 
				setConditionValue(parser.getSwitch(), parser.getCompare(), parser.getPrefix(), parser.getVar(), value);
			} 
		} 
		// 检查必须条件required strictRequired 
		for(Condition con:conditionChain.getConditions()){
			if(!con.isActive()){//没有根据value激活
				if(con.getSwitch() == EMPTY_VALUE_SWITCH.BREAK){
					log.warn("[valid:false][不具备执行条件][con:{}]",con.getId());
					this.valid = false; 
				}else if(con.getSwitch() == EMPTY_VALUE_SWITCH.IGNORE){
					log.warn("[valid:false][忽略当前条件][con:{}]",con.getId());
				}else{
					con.setActive(true);
					List<Variable> vars = con.getVariables();
					if(null != vars){
						for(Variable var:vars){
							var.setValue(false,null);
						}
					}
				}
			} 
		} 
		GroupStore groupStore = prepare.getGroups();
		if(null != groupStore){
			List<Group> groups = groupStore.getGroups();
			if(null != groups){
				for(Group group:groups){
					this.groupStore.group(group); 
				} 
			} 
		} 
		checkTest(); 
		parseText();
	}

	public boolean checkValid(){
		if(!valid){
			return false;
		} 
		if(null != variables){
			for(Variable var:variables){
				if(var.getSwitch() == EMPTY_VALUE_SWITCH.BREAK){
					if(BasicUtil.isEmpty(true,var.getValues())){
						log.warn("[valid:false][不具备执行条件][var:{}]",var.getKey());
						this.valid = false; 
						return false;
					} 
				} 
			} 
		} 
		if(null != conditionChain && !conditionChain.isValid()){
			this.valid = false; 
			return false;
		}
		return valid;
	} 
	protected void parseText(){
		String result = prepare.getText();
		if(null != variables){
			for(Variable var:variables){
				if(null == var){
					continue; 
				} 
				if(var.getType() == Variable.VAR_TYPE_REPLACE){
					// CD = ::CD 
					// CD = ${CD} 
					List<Object> varValues = var.getValues(); 
					String value = null; 
					if(BasicUtil.isNotEmpty(true,varValues)){
						value = (String)varValues.get(0); 
						if(null != value){
							value = value.replace("'", "").replace("%", ""); 
						} 
					} 
					String replaceKey = var.getFullKey();
					if(null != value){
						result = result.replace(replaceKey, value); 
					}else{
						result = result.replace(replaceKey, "NULL"); 
					} 
				} 
			} 
			for(Variable var:variables){
				if(null == var){
					continue; 
				}
				// 符合占位需要替换如在''内
				if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE){
					// CD = ':CD'
					// CD = '::CD'
					// CD = '{CD}' 
					List<Object> varValues = var.getValues(); 
					String value = null; 
					if(BasicUtil.isNotEmpty(true,varValues)){
						value = (String)varValues.get(0); 
						if(null != value){
							value = value.replace("'", "").replace("%", ""); 
						} 
					}
					if(null == value){
						value = "";
					}
					if(var.getSignType() == Variable.KEY_TYPE_SIGN_V1){
						result = result.replace("::" + var.getKey(), value);
						result = result.replace(":" + var.getKey(), value);
					}else if(var.getSignType() == Variable.KEY_TYPE_SIGN_V2){
						result = result.replace( "${" + var.getKey() + "}", value);
						result = result.replace( "#{" + var.getKey() + "}", value);
					}
				} 
			}
			boolean IS_AUTO_SPLIT_ARRAY = ConfigTable.IS_AUTO_SPLIT_ARRAY;
			for(Variable var:variables){
				if(null == var){
					continue; 
				} 
				if(var.getType() == Variable.VAR_TYPE_KEY){
					// CD = :CD 
					// CD = {CD} (弃用)
					// CD like '%:CD%' 
					// CD like '%${CD}%'
					List<Object> varValues = var.getValues(); 
					if(BasicUtil.isNotEmpty(true, varValues)){
 
						String replaceKey = var.getFullKey();
						if(var.getCompare() == Compare.LIKE){
							// CD LIKE '%{CD}%' > CD LIKE concat('%',?,'%') || CD LIKE '%' + ? + '%' 
							result = result.replace("'%"+replaceKey+"%'", runtime.getAdapter().concat(runtime,"'%'","?","'%'"));
							addValues(Compare.EQUAL, new Column(var.getKey()), varValues.get(0), IS_AUTO_SPLIT_ARRAY);
						}else if(var.getCompare() == Compare.LIKE_SUFFIX){
							result = result.replace("'%"+replaceKey+"'", runtime.getAdapter().concat(runtime,"'%'","?"));
							addValues(Compare.EQUAL, new Column(var.getKey()), varValues.get(0), IS_AUTO_SPLIT_ARRAY);
						}else if(var.getCompare() == Compare.LIKE_PREFIX){
							result = result.replace("'"+replaceKey+"%'", runtime.getAdapter().concat(runtime,"?","'%'"));
							addValues(Compare.EQUAL, new Column(var.getKey()), varValues.get(0), IS_AUTO_SPLIT_ARRAY);
						}else if(var.getCompare() == Compare.IN){
							// 多个值IN 
							String replaceDst = "";  
							for(Object tmp:varValues){
								replaceDst += " ?"; 
							}
							addValues(Compare.IN, new Column(var.getKey()), varValues, IS_AUTO_SPLIT_ARRAY);
							replaceDst = replaceDst.trim().replace(" ", ","); 
							result = result.replace(replaceKey, replaceDst); 
						}else{
							// 单个值 
							result = result.replace(replaceKey, "?"); 
							addValues(Compare.EQUAL, new Column(var.getKey()), varValues.get(0), IS_AUTO_SPLIT_ARRAY);
						} 
					} 
				} 
			} 
			// 添加其他变量值 
			for(Variable var:variables){
				if(null == var){
					continue; 
				} 
				// CD = ? 
				if(var.getType() == Variable.VAR_TYPE_INDEX){
					List<Object> varValues = var.getValues(); 
					String value = null; 
					if(BasicUtil.isNotEmpty(true, varValues)){
						value = (String)varValues.get(0); 
					} 
					addValues(Compare.EQUAL, new Column(var.getKey()), value, IS_AUTO_SPLIT_ARRAY);
				} 
			} 
		}

		builder.append(result);
		appendCondition(); 
		appendStaticCondition(); 
		appendGroup(); 
		// appendOrderStore();
	} 
 
	private void copyParam(){
		// 复制XML RunPrepare 变量 
		List<Variable> xmlVars = prepare.getSQLVariables();
		if(null != xmlVars){
			if(null == this.variables){
				variables = new ArrayList<Variable>();
			} 
			for(Variable var:xmlVars){
				if(null == var){
					continue; 
				} 
				try{
					variables.add((Variable)var.clone());
				}catch(Exception e){
					e.printStackTrace(); 
				} 
			} 
		} 
		// 复制XML RunPrepare 查询条件 
		ConditionChain xmlConditionChain = prepare.getConditionChain();
		if(null != xmlConditionChain){
			if(null == this.conditionChain){
				this.conditionChain = new DefaultXMLConditionChain();
			} 
			List<Condition> conditions = xmlConditionChain.getConditions(); 
			if(null != conditions){
				for(Condition condition:conditions){
					if(null == condition){
						continue; 
					} 
					try{
						this.conditionChain.addCondition((Condition)condition.clone()); 
					}catch(Exception e){
						e.printStackTrace(); 
					} 
				} 
			} 
		} 
		// 复制XML RunPrepare ORDER 
		OrderStore xmlOrderStore = prepare.getOrders();
		if(null != xmlOrderStore){
			List<Order> xmlOrders = xmlOrderStore.getOrders(); 
			if(null != xmlOrders){
				for(Order order:xmlOrders){
					this.orderStore.order(order); 
				} 
			} 
		} 
		// 复制 XML RunPrepare GROUP 
		GroupStore xmlGroupStore = prepare.getGroups();
		if(null != xmlGroupStore){
			List<Group> xmlGroups = xmlGroupStore.getGroups(); 
			if(null != xmlGroups){
				for(Group group:xmlGroups){
					this.groupStore.group(group); 
				} 
			} 
		} 
				 
	} 
	private void appendGroup(){
		if(null != groupStore){
			builder.append(groupStore.getRunText(delimiterFr+delimiterTo));
		} 
	} 
	/** 
	 * 检测test表达式 
	 */ 
	@SuppressWarnings("rawtypes")
	private void checkTest(){
		if(null != conditionChain){
			for(Condition con:conditionChain.getConditions()){
				String test = con.getTest(); 
 
				if(null != test){
					Map<String,Object> map = con.getRunValuesMap(); 
					Map<String,Object> runtimeValues = new HashMap<String,Object>();
					// 如果是数组只取第0个值 ognl不支持数组
					for(Map.Entry<String, Object> entry : map.entrySet()){
					    String mapKey = entry.getKey();
					    Object mapValue = entry.getValue();
					    if(null != mapValue && mapValue instanceof Collection){
					    	Collection cols = (Collection)mapValue;
					    	for(Object obj:cols){
					    		runtimeValues.put(mapKey, obj);
					    		break;
					    	}
					    }
					}
					try {
						OgnlContext context = new OgnlContext(null, null, new DefaultOgnlMemberAccess(true));
						Boolean result = (Boolean) Ognl.getValue(test,context, runtimeValues); 
						if(!result){
							con.setActive(false); 
						}else{
							if(con.getVariableType() == Condition.VARIABLE_PLACEHOLDER_TYPE_NONE){
								con.setActive(true); 
								conditionChain.setActive(true); 
							} 
						} 
					} catch (OgnlException e) {
						e.printStackTrace(); 
					} 
				}else{
					// 无test条件 
					if(con.getVariableType() == Condition.VARIABLE_PLACEHOLDER_TYPE_NONE){
						con.setActive(true); 
						conditionChain.setActive(true); 
					} 
				} 
			} 
		} 
	} 
	/** 
	 * 拼接查询条件
	 */ 
	private void appendCondition(){
		if(null == conditionChain || !conditionChain.isActive()){
			return; 
		} 
		if(!endWithWhere(builder.toString())){
			builder.append(" WHERE 1=1");
		}
		builder.append(conditionChain.getRunText(null, runtime));
		addValues(conditionChain.getRunValues()); 
//		if(null != staticConditions){
//			for(String con:staticConditions){
//				query.append("\nAND ").append(con); 
//			} 
//		} 
	} 
	private void appendStaticCondition(){
		if(!endWithWhere(builder.toString())){
			builder.append(" WHERE 1=1");
		} 
		if(null != staticConditions){
			for(String con:staticConditions){
				builder.append("\nAND ").append(con);
			} 
		} 
	} 
	 
	public void setConfigs(ConfigStore configs) {
		this.configStore = configs; 
		if(null != configs){
			this.pageNavi = configs.getPageNavi(); 
			 
		} 
	} 
 
	public Variable getVariable(String key){
		if(null != variables){
			for(Variable v:variables){
				if(null == v){
					continue; 
				} 
				if(v.getKey().equalsIgnoreCase(key)){
					return v; 
				} 
			} 
		} 
		return null; 
	} 
	private List<Variable> getVariables(String key){
		List<Variable> vars = new ArrayList<Variable>();
		if(null != variables){
			for(Variable v:variables){
				if(null == v){
					continue; 
				} 
				if(v.getKey().equalsIgnoreCase(key)){
					vars.add(v); 
				} 
			} 
		} 
		return vars; 
	}

	/**
	 *
	 * @param swt 遇到空值处理方式
	 * @param prefix  查询条件ID
	 * @param variable  列名|变量key
	 * @param value  值
	 * @param compare 比较方式
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run setConditionValue(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String variable, Object value) {
		/*不指定condition.id或condition.id = variable 时,根据var为SQL主体变量赋值*/
		// 只提供var 不提供condition
		if(null != variables &&  
				(BasicUtil.isEmpty(prefix) || prefix.equals(variable))
		){
			List<Variable> vars = getVariables(variable);
			for(Variable var:vars){
				var.setValue(value); 
			} 
		} 
		/*参数赋值*/ 
		if(null == variable){
			return this; 
		}
		Condition con = null;
		if(null == prefix){
			con = getCondition(variable);
		}else{
			con = getCondition(prefix);;
		}

		Variable var = getVariable(variable);
		if(null == con && null == var){//没有对应的condition也没有对应的text中的变量
			//追加条件  换成 调用addcondition
			/*if(this.isStrict()){
				return this; 
			}else{
				// 生成新条件
				Condition newCon = new DefaultAutoCondition(swt, compare, prefix, variable, value);
				conditionChain.addCondition(newCon); 
				if(newCon.isActive()){
					conditionChain.setActive(true); 
				} 
			} */
			return this; 
		} 
		if(null != con){
			con.setValue(variable, value); 
			if(con.isActive()){
				this.conditionChain.setActive(true); 
			} 
		} 
		return this; 
	}
		 
	public Run addCondition(String ... conditions) {
		/*添加查询条件*/ 
		if(null != conditions){
			for(String condition:conditions){
				if(null == condition){
					continue; 
				}
				condition = condition.trim(); 
				String up = condition.toUpperCase().replaceAll("\\s+", " ").trim(); 
				if(up.startsWith("ORDER BY")){
					String orderStr = condition.substring(up.indexOf("ORDER BY") + "ORDER BY".length()).trim(); 
					String orders[] = orderStr.split(","); 
					for(String item:orders){
						// sql.order(item); 
						if(null != configStore){
							configStore.order(item); 
						} 
						if(null != this.orderStore){
							this.orderStore.order(item); 
						} 
					} 
					continue; 
				}else if(up.startsWith("GROUP BY")){
					String groupStr = condition.substring(up.indexOf("GROUP BY") + "GROUP BY".length()).trim(); 
					String groups[] = groupStr.split(","); 
					for(String item:groups){
						// sql.group(item); 
						if(null != configStore){
							configStore.group(item); 
						} 
					} 
					continue; 
				} 
				addCondition(condition); 
			} 
		} 
		return this; 
	} 
 
	public void addSatticCondition(String condition){
		if(null == staticConditions){
			staticConditions = new ArrayList<>();
		}
			staticConditions.add(condition); 

	} 
	public Run addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this; 
		} 
 
		if(condition.startsWith("${") && condition.endsWith("}")){
			// 原生SQL  不处理 
			addSatticCondition(condition.substring(2, condition.length()-1));
			return this; 
		} 
		if(condition.contains(":")){
			// :符号是否表示时间 
			boolean isTime = false; 
			int idx = condition.indexOf(":"); 
			// ''之内 
			if(condition.indexOf("'")<idx && condition.indexOf("'", idx+1) > 0){
				isTime = true; 
			} 
			if(!isTime){			 
				// 需要解析的SQL 
				if(null == conditions){
					conditions = new ArrayList<>();
				} 
				conditions.add(condition); 
				return this; 
			} 
		} 
		addSatticCondition(condition); 
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
	 * @param prefix  condition.id
	 * @param variable variable
	 * @param value value
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	public Run addCondition(String prefix, String variable, Object value) {
		if(null != variables && BasicUtil.isEmpty(variable)){
			for(Variable v:variables){
				if(null == v){
					continue; 
				} 
				if(v.getKey().equalsIgnoreCase(prefix)){
					v.setValue(value); 
				} 
			} 
		} 
		/*参数赋值*/ 
		if(null == prefix){
			return this; 
		} 
		Condition con = getCondition(prefix);
		if(null == con){
			return this; 
		} 
		variable = BasicUtil.nvl(variable, prefix).toString();
		con.setValue(variable, value); 
		return this; 
	} 
 
	public void setConfigStore(ConfigStore configStore) {
		this.configStore = configStore; 
	} 
	public Run addCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String column, Object value){
		setConditionValue(swt, compare, column, null, value);
		return this; 
	}
	 
} 
