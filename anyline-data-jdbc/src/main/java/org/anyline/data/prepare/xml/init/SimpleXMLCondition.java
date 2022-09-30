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


package org.anyline.data.prepare.xml.init;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.prepare.Condition;
import org.anyline.data.run.RunValue;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.Compare;
import org.anyline.data.prepare.init.SimpleCondition;
import org.anyline.data.prepare.init.SimpleVariable;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.util.ArrayList;
import java.util.List;
 
 
/** 
 * 通过XML定义的参数 
 * @author zh 
 * 
 */ 
public class SimpleXMLCondition extends SimpleCondition implements Condition {
	private String text; 
	private List<Variable> variables;	// 变量
	 
	 
	public Object clone() throws CloneNotSupportedException{ 
		SimpleXMLCondition clone = (SimpleXMLCondition)super.clone();
		if(null != variables){ 
			List<Variable> cVariables = new ArrayList<Variable>();
			for(Variable var:variables){
				if(null == var){
					continue;
				} 
				cVariables.add((Variable)var.clone());
			}
			clone.setRequired(this.isRequired());
			clone.setStrictRequired(this.isStrictRequired()); 
			clone.variables = cVariables; 
		} 
		return clone; 
	} 
	public SimpleXMLCondition(){
		join = ""; 
	} 
	public SimpleXMLCondition(String id, String text, boolean isStatic){
		join = ""; 
		this.id = id; 
		this.text = text; 
		setVariableType(Condition.VARIABLE_FLAG_TYPE_INDEX); 
		if(!isStatic){ 
			parseText(); 
		}else{
			setVariableType(Condition.VARIABLE_FLAG_TYPE_NONE);
		} 
	} 
	public void init(){ 
		setActive(false); 
		if(null == variables){ 
			variables = new ArrayList<Variable>();
		} 
		for(Variable variable:variables){
			variable.init(); 
		} 
	} 
	/** 
	 * 赋值 
	 * @param variable  variable
	 * @param values  values
	 */ 
	public void setValue(String variable, Object values){
		runValuesMap.put(variable, values); 
		if(null == variable || null == variables){ 
			return; 
		} 
		for(Variable v:variables){
			if(null == v){
				continue;
			} 
			if(variable.equalsIgnoreCase(v.getKey())){ 
				v.setValue(values); 
				if(BasicUtil.isNotEmpty(true,values) || v.isRequired() || v.isStrictRequired()){ 
					setActive(true); 
				} 
			} 
		} 
	} 
 
	/** 
	 * 解析变量
	 */ 
	private void parseText(){ 
		try{
			// AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD 
			List<List<String>> keys = RegularUtil.fetchs(text, RunPrepare.SQL_PARAM_VAIRABLE_REGEX, Regular.MATCH_MODE.CONTAIN);
			if(keys.size() ==0){
				// AND CD = {CD} || CD LIKE '%{CD}%' || CD IN ({CD}) || CD = ${CD}
				keys = RegularUtil.fetchs(text, RunPrepare.SQL_PARAM_VAIRABLE_REGEX_EL, Regular.MATCH_MODE.CONTAIN);
			} 
			if(BasicUtil.isNotEmpty(true,keys)){ 
				setVariableType(VARIABLE_FLAG_TYPE_KEY); 
				int varType = Variable.VAR_TYPE_INDEX;
				Compare compare = Compare.EQUAL;
				for(int i=0; i<keys.size(); i++){ 
					List<String> keyItem = keys.get(i); 
					String prefix = keyItem.get(1).trim();		// 前缀 
					String fullKey = keyItem.get(2).trim();		// 完整KEY :CD ::CD {CD} ${CD} 8.5之后不用{CD}避免与json冲突
					String typeChar = keyItem.get(3);	// null || "'" || ")" 
					// String key = fullKey.replace(":", "").replace(" {", "").replace("}", "").replace("$", "");
					String key = fullKey.replace(":", "").replace("${", "").replace("}", "");
					if(fullKey.startsWith("::")){ 
						// AND CD = ::CD 
						varType = Variable.VAR_TYPE_REPLACE;
					}else if(BasicUtil.isNotEmpty(typeChar) && ("'".equals(typeChar) || "%".equals(typeChar))){ 
						// AND CD = ':CD' 
						varType = Variable.VAR_TYPE_KEY_REPLACE;
					}else{ 
						// AND CD = :CD 
						varType = Variable.VAR_TYPE_KEY;
						if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){ 
							compare = Compare.IN;
						} 
					} 
					Variable var = new SimpleVariable();
					var.setKey(key); 
					var.setType(varType); 
					var.setCompare(compare); 
					addVariable(var); 
				} 
			}else{ 
				List<String> idxKeys = RegularUtil.fetch(text, "\\?",Regular.MATCH_MODE.CONTAIN,0); 
				if(BasicUtil.isNotEmpty(true,idxKeys)){ 
					// 按下标区分变量 
					this.setVariableType(VARIABLE_FLAG_TYPE_INDEX); 
					int varType = Variable.VAR_TYPE_INDEX;
					for(int i=0; i<idxKeys.size(); i++){ 
						Variable var = new SimpleVariable();
						var.setType(varType); 
						var.setKey(id); 
						addVariable(var); 
					} 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	} 
 
	private void addVariable(Variable variable){
		if(null == variables){ 
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
	public String getRunText(String prefix, JDBCAdapter adapter) {
		String result = text; 
		runValues = new ArrayList<>();
		if(null == variables){
			return result;
		}
		for(Variable var: variables){
			if(null == var){
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_REPLACE){
				// CD = ::CD 
				List<Object> values = var.getValues(); 
				String value = null; 
				if(BasicUtil.isNotEmpty(true,values)){ 
					value = (String)values.get(0); 
				} 
				if(BasicUtil.isNotEmpty(value)){
					result = result.replace("::"+var.getKey(), value);
					result = result.replace("${"+var.getKey()+"}", value); 
				}else{
					result = result.replace("::"+var.getKey(), "NULL");
					result = result.replace("${"+var.getKey()+"}", "NULL"); 
				} 
			} 
		} 
		for(Variable var: variables){
			if(null == var){
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE){
				// CD = ':CD' 
				List<Object> values = var.getValues(); 
				String value = null; 
				if(BasicUtil.isNotEmpty(true,values)){ 
					value = (String)values.get(0); 
				} 
				if(null != value){
					result = result.replace(":"+var.getKey(), value);
					result = result.replace("${"+var.getKey()+"}", value);
				}else{ 
					result = result.replace(":"+var.getKey(), "");
					result = result.replace("${"+var.getKey()+"}", "");
				} 
			} 
		} 
		for(Variable var:variables){
			if(null == var){
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_KEY){
				// CD=:CD 
				List<Object> varValues = var.getValues(); 
				if(Compare.IN == var.getCompare()){
					String inParam = ""; 
					for(int i=0; i<varValues.size(); i++){ 
						inParam += "?"; 
						if(i<varValues.size()-1){ 
							inParam += ","; 
						} 
					}
					result = result.replace(":"+var.getKey(), inParam);
					result = result.replace("${"+var.getKey()+"}", inParam);
					for(Object obj:varValues){
						runValues.add(new RunValue(var.getKey(), obj));
					}
				}else{
					result = result.replace(":"+var.getKey(), "?");
					result = result.replace("${"+var.getKey()+"}", "?");
					String value = null; 
					if(BasicUtil.isNotEmpty(true,varValues)){ 
						value = varValues.get(0).toString(); 
					} 
					runValues.add(new RunValue(var.getKey(), value));
				} 
				 
			} 
		} 
		 
		for(Variable var:variables){
			if(null == var){
				continue;
			} 
			if(var.getType() == Variable.VAR_TYPE_INDEX){
				List<Object> values = var.getValues(); 
				String value = null; 
				if(BasicUtil.isNotEmpty(true,values)){ 
					value = (String)values.get(0); 
				} 
				runValues.add(new RunValue(null, value));
			} 
		} 
		return result; 
	}

	@Override
	public Condition setRunText(String text) {
		this.text = text;
		return this;
	}

	public Variable getVariable(String var) {
		if(null == variables || null == var){
			return null; 
		} 
		for(Variable variable:variables){
			if(null == variable){
				continue;
			} 
			if(var.equalsIgnoreCase(variable.getKey())){
				return variable; 
			} 
		} 
		return null; 
	}

	public boolean isValid(){
		if(!super.isValid()){
			return false;
		}
		if(null != variables){
			for(Variable variable:variables){
				if(null == variable){
					continue;
				}
				List<Object> values = variable.getValues();
				if(isStrictRequired() && BasicUtil.isEmpty(true, values)){
					return false;
				}
			}
		}
		return true;
	}

	public List<Variable> getVariables(){
		return variables;
	} 
}
