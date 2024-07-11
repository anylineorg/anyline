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



package org.anyline.data.prepare.xml.init;

import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.init.AbstractCondition;
import org.anyline.data.prepare.init.DefaultVariable;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.util.ArrayList;
import java.util.List;
 
 
/** 
 * 通过XML定义的参数 
 * @author zh 
 * 
 */ 
public class DefaultXMLCondition extends AbstractCondition implements Condition {
	private String text;
	 
	 
	public DefaultXMLCondition clone() {
		DefaultXMLCondition clone = null;
		try{
			clone = (DefaultXMLCondition)super.clone();
		}catch (Exception e) {
			clone = new DefaultXMLCondition();
		}
		if(null != variables) {
			List<Variable> cVariables = new ArrayList<>();
			for(Variable var:variables) {
				if(null == var) {
					continue;
				} 
				cVariables.add(var.clone());
			}
			clone.setSwt(swt);
			clone.variables = cVariables; 
		} 
		return clone; 
	} 
	public DefaultXMLCondition() {
		join = ""; 
	} 
	public DefaultXMLCondition(String id, String text, boolean isStatic) {
		join = ""; 
		this.id = id; 
		this.text = text; 
		setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_INDEX); 
		if(!isStatic) {
			parseText(); 
		}else{
			setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_NONE);
		} 
	} 
	public void init() {
		setActive(false); 
		if(null == variables) {
			variables = new ArrayList<Variable>();
		} 
		for(Variable variable:variables) {
			variable.init(); 
		} 
	} 
	/** 
	 * 赋值 
	 * @param variable  variable
	 * @param values  values
	 */ 
	public void setValue(String variable, Object values) {
		runValuesMap.put(variable, values); 
		if(null == variable || null == variables) {
			return; 
		} 
		for(Variable v:variables) {
			if(null == v) {
				continue;
			} 
			if(variable.equalsIgnoreCase(v.getKey())) {
				v.setValue(values);
				Compare.EMPTY_VALUE_SWITCH swt = v.getSwt();
				if(BasicUtil.isNotEmpty(true,values) || swt == Compare.EMPTY_VALUE_SWITCH.NULL || swt == Compare.EMPTY_VALUE_SWITCH.SRC) {
					setActive(true); 
				} 
			} 
		} 
	} 
 
	/** 
	 * 解析变量
	 */ 
	private void parseText() {
		try{
			List<List<String>> keys = null;
			// AND CD = {CD} || CD LIKE '%{CD}%' || CD IN ({CD}) || CD = ${CD} || CD = #{CD}
			//{CD} 用来兼容旧版本，新版本中不要用，避免与josn格式冲突
			keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX, Regular.MATCH_MODE.CONTAIN);
			if(keys.isEmpty() && ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT) {
				// AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD
				keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX_EXT, Regular.MATCH_MODE.CONTAIN);
			} 
			if(BasicUtil.isNotEmpty(true,keys)) {
				setVariableType(VARIABLE_PLACEHOLDER_TYPE_KEY); 
				int varType = Variable.VAR_TYPE_INDEX;
				Compare compare = Compare.EQUAL;
				for(int i=0; i<keys.size(); i++) {
					List<String> keyItem = keys.get(i); 
					String prefix = keyItem.get(1).trim();		// 前缀 空或#或$
					String fullKey = keyItem.get(2).trim();		// 完整KEY :CD ::CD {CD} ${CD} #{CD} 8.5之后不用{CD}避免与json冲突
					String typeChar = keyItem.get(3);	// null || "'" || ")" 
					// String key = fullKey.replace(":","").replace(" {","").replace("}","").replace("$","");
					if(fullKey.startsWith("::") || fullKey.startsWith("${")) {
						//替换
						// AND CD = ::CD  AND CD = ${CD}
						varType = Variable.VAR_TYPE_REPLACE;
					}else if(BasicUtil.isNotEmpty(typeChar) && ("'".equals(typeChar) || "%".equals(typeChar))) {
						//符合占位  但需要替换 如在''内
						// AND CD = ':CD' 
						varType = Variable.VAR_TYPE_KEY_REPLACE;
					}else{
						// AND CD = :CD 
						varType = Variable.VAR_TYPE_KEY;
					}

					if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")) {
						compare = Compare.IN;
					}
					Variable var = new DefaultVariable();
					var.setFullKey(fullKey);
					var.setType(varType); 
					var.setCompare(compare); 
					addVariable(var); 
				} 
			}else{
				int qty = SQLUtil.countPlaceholder(text);
				if(qty > 0) {
					// 按下标区分变量 
					this.setVariableType(VARIABLE_PLACEHOLDER_TYPE_INDEX); 
					int varType = Variable.VAR_TYPE_INDEX;
					for(int i=0; i<qty; i++) {
						Variable var = new DefaultVariable();
						var.setType(varType); 
						var.setKey(id); 
						addVariable(var); 
					} 
				} 
			} 
		}catch(Exception e) {
			e.printStackTrace(); 
		} 
	} 
 
	private void addVariable(Variable variable) {
		if(null == variables) {
			variables = new ArrayList<Variable>();
		} 
		variables.add(variable); 
	} 
 
	public String getId() {
		return id; 
	} 
 
	public void setId(String id) {
		this.id = id; 
	} 
 
	public String getText() {
		return text; 
	}

	@Override
	public String getRunText(String prefix, DataRuntime runtime, boolean placeholder) {
		String result = text; 
		runValues = new ArrayList<>();
		if(null == variables) {
			return result;
		}
		for(Variable var: variables) {
			if(null == var) {
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_REPLACE) {
				// CD = ::CD 
				List<Object> values = var.getValues(); 
				String value = null; 
				if(BasicUtil.isNotEmpty(true,values)) {
					if(var.getCompare() == Compare.IN) {
						value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
					}else {
						value = values.get(0).toString();
					}
				} 
				if(BasicUtil.isNotEmpty(value)) {
					result = result.replace(var.getFullKey(), value);
				}else{
					result = result.replace(var.getFullKey(), "NULL");
				} 
			} 
		} 
		for(Variable var: variables) {
			if(null == var) {
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE) {
				// CD = ':CD' CD = '::CD'
				List<Object> values = var.getValues(); 
				String value = null; 
				if(BasicUtil.isNotEmpty(true,values)) {
					value = (String)values.get(0); 
				} 
				if(null != value) {
					result = result.replace(var.getFullKey(), value);
				}else{
					result = result.replace(var.getFullKey(), "");
				} 
			} 
		} 
		for(Variable var:variables) {
			if(null == var) {
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_KEY) {
				// CD=:CD    ID IN(#{ID})
				List<Object> varValues = var.getValues(); 
				if(Compare.IN == var.getCompare()) {
					String replaceDst = "";
					for(int i=0; i<varValues.size(); i++) {
						replaceDst += "?";
						if(i<varValues.size()-1) {
							replaceDst += ",";
						} 
					}
					result = result.replace(var.getFullKey(), replaceDst);
					for(Object obj:varValues) {
						runValues.add(new RunValue(var.getKey(), obj));
					}
				}else{
					result = result.replace(var.getFullKey(), "?");
					String value = null; 
					if(BasicUtil.isNotEmpty(true,varValues)) {
						value = varValues.get(0).toString(); 
					} 
					runValues.add(new RunValue(var.getKey(), value));
				} 
				 
			} 
		} 
		 
		for(Variable var:variables) {
			if(null == var) {
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_INDEX) {
				List<Object> values = var.getValues(); 
				String value = null; 
				if(BasicUtil.isNotEmpty(true,values)) {
					value = (String)values.get(0); 
				} 
				runValues.add(new RunValue((String)null, value));
			} 
		} 
		return result; 
	}

	@Override
	public Condition setRunText(String text) {
		this.text = text;
		return this;
	}
	public boolean isValid() {
		if(!super.isValid()) {
			return false;
		}
		if(null != variables) {
			for(Variable variable:variables) {
				if(null == variable) {
					continue;
				}
				List<Object> values = variable.getValues();
				if(swt == Compare.EMPTY_VALUE_SWITCH.BREAK && BasicUtil.isEmpty(true, values)) {
					return false;
				}
			}
		}
		return true;
	}

}
