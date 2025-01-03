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

import org.anyline.data.prepare.*;
import org.anyline.data.run.RunValue;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.util.BasicUtil;

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
	protected RunPrepare prepare										;   // 子查询 exists
	protected String datatype											;   // 数据类型 到 adapter中根据TypeMetadata解析成class
	protected Map<String, Object> runValuesMap = new HashMap<String, Object>()		;	// 运行时参数
	protected Map<String, Object> runOrValuesMap = new HashMap<String, Object>()		;	// 运行时参数(or)
	protected Condition.JOIN join = JOIN.AND							;	// 连接方式
	protected ConditionChain container									;	// 当前条件所处容器
	protected String id													; 	// ID
	protected String text												;	// 静态条件
	protected String test												;	// 判断条件
	protected List<Variable> variables	= new ArrayList<>()				;	// 变量
	protected List<VariableBlock> blocks = new ArrayList<>();
	protected boolean setValue = false									;   // 是否赋值过
	//protected boolean apart = false										;   // 是否需要跟前面的条件 隔离，前面所有条件加到()中
	protected boolean integrality = true								;   // 是否作为一个整体，不可分割，与其他条件合并时以()包围
	protected double index = 1.0;

	public void init() {
	}

	@Override
	public RunPrepare prepare() {
		return prepare;
	}
	@Override
	public Condition prepare(RunPrepare prepare) {
		this.prepare = prepare;
		return this;
	}
	@Override
	public double index() {
		return index;
	}

	@Override
	public void index(double index) {
		this.index = index;
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
		if(active){
			return true;
		}
		if(null != variables && !variables.isEmpty()) {
			boolean chk = true;
			for (Variable var : variables) {
				boolean varActive = false;
				if (BasicUtil.isNotEmpty(true, var.getValues()) || this.getSwt() == EMPTY_VALUE_SWITCH.NULL || this.getSwt() == EMPTY_VALUE_SWITCH.SRC) {
					varActive = true;
				}
				chk = chk && varActive;
			}
			active = chk;
		}
		return active;
	}
	@Override
	public List<RunValue> getRunValues() {
		return runValues; 
	}

	@Override
	public String text() {
		return text;
	}

	@Override
	public void text(String text) {
		this.text = text;
	}
	@Override
	public Condition setJoin(Condition.JOIN join) {
		this.join = join; 
		return this; 
	}

	@Override
	public Condition.JOIN getJoin() {
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
		if(null == var) {
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
	public Condition addVariable(Variable var) {
		variables.add(var);
		return this;
	}

	@Override
	public Condition addVariable(List<Variable> vars) {
		variables.addAll(vars);
		return this;
	}


	@Override
	public Condition  addVariableBlock(VariableBlock block){
		blocks.add(block);
		return this;
	}
	public List<VariableBlock> getVariableBlocks(){
		return blocks;
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
	public String datatype() {
		return datatype;
	}

	@Override
	public void datatype(String datatype) {
		this.datatype = datatype;
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
