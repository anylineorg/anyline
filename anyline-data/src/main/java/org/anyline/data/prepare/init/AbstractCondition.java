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



package org.anyline.data.prepare.init;

import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.Variable;
import org.anyline.data.run.RunValue;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public abstract class AbstractCondition implements Condition {
	protected boolean isVariableSlave = false							;	// 是否用来给java/xml定义SQL中变量赋值, 本身并不拼接到最终SQL
	protected EMPTY_VALUE_SWITCH swt = EMPTY_VALUE_SWITCH.IGNORE		;   // 遇到空值处理方式
	protected boolean active = false									;	// 是否活动(是否拼接到SQL中)
	protected Boolean vaild = null;
	protected int variableType = VARIABLE_PLACEHOLDER_TYPE_NONE				;	// 变量标记方式
	protected List<RunValue> runValues = new ArrayList<>()				;	// 运行时参数
	protected List<RunValue> runOrValues = new ArrayList<>()			;	// 运行时参数(or)
	protected String valueClass											;   // 数据类型 到 adapter中根据TypeMetadata解析成class
	protected Map<String, Object> runValuesMap = new HashMap<String, Object>()		;	// 运行时参数
	protected Map<String, Object> runOrValuesMap = new HashMap<String, Object>()		;	// 运行时参数(or)
	protected String join = Condition.CONDITION_JOIN_TYPE_AND			;	// 连接方式
	protected ConditionChain container									;	// 当前条件所处容器
	protected String id													; 	// ID
	protected String text												;	// 静态条件
	protected String test												;	// 判断条件
	protected List<Variable> variables									;	// 变量
	protected boolean setValue = false									;   // 是否赋值过
	//protected boolean apart = false										;   // 是否需要跟前面的条件 隔离，前面所有条件加到()中
	protected boolean integrality = true								;   // 是否作为一个整体，不可分割，与其他条件合并时以()包围

	public void init() {
	} 
	public void initRunValue() {
		if(null == runValues) {
			runValues = new ArrayList<>();
		}else{
			runValues.clear(); 
		}
		setValue = false;
	}
	@Override 
	public void setActive(boolean active) {
		this.active = active; 
	}
	@Override
	public boolean isActive() {
		return active; 
	}
	@Override
	public List<RunValue> getRunValues() {
		return runValues; 
	}

	@Override
	public Condition setJoin(String join) {
		this.join = join; 
		return this; 
	}

	@Override
	public String getJoin() {
		return join; 
	}
	@Override
	public ConditionChain getContainer() {
		return container; 
	}
	@Override
	public Condition setContainer(ConditionChain container) {
		this.container = container; 
		return this; 
	}
	@Override
	public boolean hasContainer() {
		return (null != container); 
	}
	@Override
	public boolean isContainer() {
		return (this instanceof ConditionChain); 
	}
	@Override
	public String getId() {
		return id; 
	}
	@Override
	public int getVariableType() {
		return variableType;
	}
	@Override
	public void setVariableType(int variableType) {
		this.variableType = variableType; 
	}

	/** 
	 * 赋值 
	 * @param variable  variable
	 * @param values  values
	 */
	@Override
	public void setValue(String variable, Object values) {
		Variable var = getVariable(variable);
		if(null != var) {
			var.setValue(values);
			setValue = true;
		}
	}

	@Override
	public void setTest(String test) {
		this.test = test;
	}
	@Override
	public String getTest() {
		return test;
	}
	@Override
	public Map<String, Object> getRunValuesMap() {
		return runValuesMap;
	}
	@Override
	public boolean isValid() {
		if(null != vaild && !vaild) {
			return false;
		}
		if(swt == EMPTY_VALUE_SWITCH.BREAK && ! isActive()) {
			return false;
		}
		return true;
	}

	public void setValid(boolean valid) {
		this.vaild = valid;
	}

	@Override
	public Variable getVariable(String var) {
		if(null == variables || null == var) {
			return null;
		}
		for(Variable variable:variables) {
			if(null == variable) {
				continue;
			}
			if(var.equalsIgnoreCase(variable.getKey())) {
				return variable;
			}
		}
		return null;
	}

	@Override
	public List<Variable> getVariables() {
		return variables;
	}
	@Override
	public boolean isVariableSlave() {
		return isVariableSlave;
	}

	@Override
	public void setVariableSlave(boolean bol) {
		isVariableSlave = bol;
	}

	@Override
	public boolean isSetValue() {
		return setValue;
	}
	@Override
	public boolean isSetValue(String variable) {
		Variable var = getVariable(variable);
		if(null != var) {
			return var.isSetValue();
		}
		return false;
	}

	@Override
	public String getValueClass() {
		return valueClass;
	}

	@Override
	public void setValueClass(String valueClass) {
		this.valueClass = valueClass;
	}

	@Override
	public EMPTY_VALUE_SWITCH getSwt() {
		return swt;
	}

	@Override
	public void setSwt(EMPTY_VALUE_SWITCH swt) {
		this.swt = swt;
	}

/*	@Override
	public boolean apart() {
		return apart;
	}

	@Override
	public void apart(boolean apart) {
		this.apart = apart;
	}*/
	@Override
	public boolean integrality() {
		return integrality;
	}

	@Override
	public void integrality(boolean integrality) {
		this.integrality = integrality;
	}
	public AbstractCondition clone() {
		AbstractCondition clone = null;
		try {
			clone = (AbstractCondition) super.clone();
		}catch (Exception e) {
		}
		if(null != runValues) {
			List<RunValue> cRunValues = new ArrayList<>();
			for(RunValue obj:runValues) {
				RunValue tmp = obj;
				cRunValues.add(tmp);
			}
			clone.runValues = cRunValues;
		}
		if(null != container) {
			clone.container = container.clone();
		}
		return clone;
	}

}
