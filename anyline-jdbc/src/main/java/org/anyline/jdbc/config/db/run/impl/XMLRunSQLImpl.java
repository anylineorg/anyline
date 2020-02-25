/*  
 * Copyright 2006-2020 www.anyline.org
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
 
 
package org.anyline.jdbc.config.db.run.impl; 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.Group;
import org.anyline.jdbc.config.db.GroupStore;
import org.anyline.jdbc.config.db.Order;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLVariable;
import org.anyline.jdbc.config.db.impl.GroupStoreImpl;
import org.anyline.jdbc.config.db.impl.OrderStoreImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.jdbc.config.db.sql.xml.impl.XMLConditionChainImpl;
import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.ognl.DefaultMemberAccess;
import org.anyline.util.BasicUtil;
 
public class XMLRunSQLImpl extends BasicRunSQLImpl implements RunSQL{ 
	private List<String> conditions; 
	private List<String> staticConditions; 
	public XMLRunSQLImpl(){ 
		this.conditionChain = new XMLConditionChainImpl(); 
		this.orderStore = new OrderStoreImpl(); 
		this.groupStore = new GroupStoreImpl(); 
	} 
 
	public RunSQL setSql(SQL sql){ 
		this.sql = sql; 
		copyParam(); 
		return this; 
	} 
	public void init(){ 
		super.init(); 
		if(null != configStore){ 
			for(Config conf:configStore.getConfigChain().getConfigs()){ 
				setConditionValue(conf.isRequire(),  
						conf.isStrictRequired(), conf.getField(), conf.getVariable(), conf.getValues(), conf.getCompare());
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
				ParseResult parser = ConfigParser.parse(condition,false); 
				Object value = ConfigParser.getValues(parser);// parser.getKey(); 
				if(parser.getParamFetchType() == ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT){ 
					String[] tmp = value.toString().split(","); 
					if(null != tmp){ 
						List<String> list = new ArrayList<String>(); 
						for(String item:tmp){ 
							list.add(item); 
						} 
						value = list; 
					} 
					 
				} 
				setConditionValue(parser.isRequired(), parser.isStrictRequired(), parser.getId(), parser.getField(), value, parser.getCompare()); 
			} 
		} 
		//检查必须条件required strictRequired 
		for(Condition con:conditionChain.getConditions()){ 
			if(!con.isActive()){//没有根据value激活 
				if(con.isRequired()){ 
					con.setActive(true); 
					List<SQLVariable> vars = con.getVariables(); 
					if(null != vars){ 
						for(SQLVariable var:vars){ 
							var.setValue(false,null); 
						} 
					} 
				} 
				if(con.isStrictRequired()){ 
					log.warn("[valid:false][con:{}]",con.getId());
					this.valid = false; 
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
		checkTest(); 
		parseText(); 
		checkValid(); 
	} 
	private void checkValid(){ 
		if(!valid){ 
			return; 
		} 
		if(null != variables){ 
			for(SQLVariable var:variables){ 
				if(var.isRequired() || var.isStrictRequired()){ 
					if(BasicUtil.isEmpty(true,var.getValues())){ 
						log.warn("[valid:false][var:{}]",var.getKey());
						this.valid = false; 
						return; 
					} 
				} 
			} 
		} 
		if(null != conditionChain && !conditionChain.isValid()){ 
			this.valid = false; 
			return; 
		} 
	} 
	protected void parseText(){ 
		String result = sql.getText(); 
		if(null != variables){ 
			for(SQLVariable var:variables){ 
				if(null == var){ 
					continue; 
				} 
				if(var.getType() == SQLVariable.VAR_TYPE_REPLACE){ 
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
					String replaceKey = ""; 
					if(var.getSignType() ==1){ 
						replaceKey = "::" + var.getKey(); 
					}else if(var.getSignType() ==2){ 
						replaceKey = "${" + var.getKey() + "}"; 
					} 
					if(null != value){ 
						result = result.replace(replaceKey, value); 
					}else{ 
						result = result.replace(replaceKey, "NULL"); 
					} 
				} 
			} 
			for(SQLVariable var:variables){ 
				if(null == var){ 
					continue; 
				} 
				if(var.getType() == SQLVariable.VAR_TYPE_KEY_REPLACE){ 
					//CD = ':CD' 
					//CD = '{CD}' 
					List<Object> varValues = var.getValues(); 
					String value = null; 
					if(BasicUtil.isNotEmpty(true,varValues)){ 
						value = (String)varValues.get(0); 
						if(null != value){ 
							value = value.replace("'", "").replace("%", ""); 
						} 
					} 
 
					String replaceKey = ""; 
					if(var.getSignType() ==1){ 
						replaceKey = ":" + var.getKey(); 
					}else if(var.getSignType() ==2){ 
						replaceKey = "{" + var.getKey() + "}"; 
					} 
					if(null != value){ 
						result = result.replace(replaceKey, value); 
					}else{ 
						result = result.replace(replaceKey, ""); 
					} 
				} 
			} 
			for(SQLVariable var:variables){ 
				if(null == var){ 
					continue; 
				} 
				if(var.getType() == SQLVariable.VAR_TYPE_KEY){ 
					// CD = :CD 
					// CD = {CD} 
					// CD like '%:CD%' 
					// CD like '%{CD}%' 
					List<Object> varValues = var.getValues(); 
					if(BasicUtil.isNotEmpty(true, varValues)){ 
 
						String replaceKey = ""; 
						if(var.getSignType() ==1){ 
							replaceKey = ":" + var.getKey(); 
						}else if(var.getSignType() ==2){ 
							replaceKey = "{" + var.getKey() + "}"; 
						} 
						if(var.getCompare() == SQL.COMPARE_TYPE.LIKE){ 
							//CD LIKE '%{CD}%' > CD LIKE concat('%',?,'%') || CD LIKE '%' + ? + '%' 
							result = result.replace("'%"+replaceKey+"%'", creater.concat("'%'","?","'%'")); 
							addValues(varValues.get(0)); 
						}else if(var.getCompare() == SQL.COMPARE_TYPE.LIKE_SUBFIX){ 
							result = result.replace("'%"+replaceKey+"'", creater.concat("'%'","?")); 
							addValues(varValues.get(0)); 
						}else if(var.getCompare() == SQL.COMPARE_TYPE.LIKE_PREFIX){ 
							result = result.replace("'"+replaceKey+"%'", creater.concat("?","'%'")); 
							addValues(varValues.get(0)); 
						}else if(var.getCompare() == SQL.COMPARE_TYPE.IN){ 
							//多个值IN 
							String replaceDst = "";  
							for(Object tmp:varValues){ 
								addValues(tmp); 
								replaceDst += " ?"; 
							} 
							replaceDst = replaceDst.trim().replace(" ", ","); 
							result = result.replace(replaceKey, replaceDst); 
						}else{ 
							//单个值 
							result = result.replace(replaceKey, "?"); 
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
		appendStaticCondition(); 
		appendGroup(); 
		//appendOrderStore(); 
	} 
 
	private void copyParam(){ 
		//复制XML SQL 变量 
		List<SQLVariable> xmlVars = sql.getSQLVariables(); 
		if(null != xmlVars){ 
			if(null == this.variables){ 
				variables = new ArrayList<SQLVariable>(); 
			} 
			for(SQLVariable var:xmlVars){ 
				if(null == var){ 
					continue; 
				} 
				try{ 
					variables.add((SQLVariable)var.clone()); 
				}catch(Exception e){ 
					e.printStackTrace(); 
				} 
			} 
		} 
		//复制XML SQL 查询条件 
		ConditionChain xmlConditionChain = sql.getConditionChain(); 
		if(null != xmlConditionChain){ 
			if(null == this.conditionChain){ 
				this.conditionChain = new XMLConditionChainImpl(); 
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
		//复制XML SQL ORDER 
		OrderStore xmlOrderStore = sql.getOrders(); 
		if(null != xmlOrderStore){ 
			List<Order> xmlOrders = xmlOrderStore.getOrders(); 
			if(null != xmlOrders){ 
				for(Order order:xmlOrders){ 
					this.orderStore.order(order); 
				} 
			} 
		} 
		//复制 XML SQL GROUP 
		GroupStore xmlGroupStore = sql.getGroups(); 
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
			builder.append(groupStore.getRunText(disKeyFr+disKeyTo)); 
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
					//如果是数组只取第0个值 ognl不支持数组
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
						OgnlContext context = new OgnlContext(null, null, new DefaultMemberAccess(true));
						Boolean result = (Boolean) Ognl.getValue(test,context, runtimeValues); 
						if(!result){ 
							con.setActive(false); 
						}else{ 
							if(con.getVariableType() == Condition.VARIABLE_FLAG_TYPE_NONE){ 
								con.setActive(true); 
								conditionChain.setActive(true); 
							} 
						} 
					} catch (OgnlException e) { 
						e.printStackTrace(); 
					} 
				}else{ 
					//无test条件 
					if(con.getVariableType() == Condition.VARIABLE_FLAG_TYPE_NONE){ 
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
		if(!endwithWhere(builder.toString())){ 
			builder.append(" WHERE 1=1"); 
		} 
		builder.append(conditionChain.getRunText(creater)); 
		addValues(conditionChain.getRunValues()); 
//		if(null != staticConditions){ 
//			for(String con:staticConditions){ 
//				builder.append("\nAND ").append(con); 
//			} 
//		} 
	} 
	private void appendStaticCondition(){ 
		if(!endwithWhere(builder.toString())){ 
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
 
	private SQLVariable getVariable(String key){ 
		if(null != variables){ 
			for(SQLVariable v:variables){ 
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
	private List<SQLVariable> getVariables(String key){ 
		List<SQLVariable> vars = new ArrayList<SQLVariable>(); 
		if(null != variables){ 
			for(SQLVariable v:variables){ 
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
	@Override
	public RunSQL setConditionValue(boolean required, boolean strictRequired, String condition, String variable, Object value, SQL.COMPARE_TYPE compare) { 
		/*不指定变量名或condition = variable 时,根据condition为SQL主体变量赋值*/ 
		if(null != variables &&  
				(BasicUtil.isEmpty(condition) || condition.equals(variable))
		){ 
			List<SQLVariable> vars = getVariables(condition); 
			for(SQLVariable var:vars){ 
				var.setValue(value); 
			} 
		} 
		/*参数赋值*/ 
		if(null == condition){ 
			return this; 
		} 
		Condition con = getCondition(condition); 
		SQLVariable var = getVariable(condition); 
		if(null == con && null == var){//没有对应的condition也没有对应的text中的变量 
			if(this.isStrict()){ 
				return this; 
			}else{ 
				//生成新条件 
				String column = variable; 
				//String condition, String variable 
				if(BasicUtil.isNotEmpty(condition) && !condition.equals(variable)){ 
					column = condition + "." + variable; 
				} 
				Condition newCon = new AutoConditionImpl(required, strictRequired, column, value, compare); 
				conditionChain.addCondition(newCon); 
				if(newCon.isActive()){ 
					conditionChain.setActive(true); 
				} 
			} 
			return this; 
		} 
		if(null != con){ 
			variable = BasicUtil.nvl(variable, condition).toString(); 
			con.setValue(variable, value); 
			if(con.isActive()){ 
				this.conditionChain.setActive(true); 
			} 
		} 
		return this; 
	} 
	@Override 
	public RunSQL setConditionValue(boolean required, String condition, String variable, Object value, SQL.COMPARE_TYPE compare) { 
		return setConditionValue(required, false, condition, variable, value, compare); 
	} 
	 
		 
	public RunSQL addConditions(String[] conditions) { 
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
						//sql.order(item); 
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
						//sql.group(item); 
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
			staticConditions = new ArrayList<String>(); 
		} 
		if(!isStrict()){ 
			staticConditions.add(condition); 
		} 
	} 
	public RunSQL addCondition(String condition) { 
		if(BasicUtil.isEmpty(condition)){ 
			return this; 
		} 
 
		if(condition.startsWith("{") && condition.endsWith("}")){ 
			//原生SQL  不处理 
			addSatticCondition(condition.substring(1, condition.length()-1)); 
			return this; 
		} 
		if(condition.contains(":")){ 
			//:符号是否表示时间 
			boolean isTime = false; 
			int idx = condition.indexOf(":"); 
			//''之内 
			if(condition.indexOf("'")<idx && condition.indexOf("'", idx+1) > 0){ 
				isTime = true; 
			} 
			if(!isTime){			 
				//需要解析的SQL 
				if(null == conditions){ 
					conditions = new ArrayList<String>(); 
				} 
				conditions.add(condition); 
				return this; 
			} 
		} 
		addSatticCondition(condition); 
		return this; 
	} 
 
 
	/** 
	 * 添加参数值 
	 * @param obj  obj
	 * @return return
	 */ 
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	 * @param condition  condition
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
 
	public void setConfigStore(ConfigStore configStore) { 
		this.configStore = configStore; 
	} 
	public RunSQL addCondition(boolean required, boolean strictRequired, String column, Object value, SQL.COMPARE_TYPE compare){ 
		setConditionValue(required, strictRequired, column, null, value, compare); 
		return this; 
	} 
	public RunSQL addCondition(boolean required, String column, Object value, SQL.COMPARE_TYPE compare){ 
		return addCondition(required, false, column, value,compare); 
	} 
	 
} 
