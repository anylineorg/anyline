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

package org.anyline.data.prepare.text.init;

import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.VariableBlock;
import org.anyline.data.prepare.init.AbstractCondition;
import org.anyline.data.prepare.init.DefaultVariableBlock;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.CommandParser;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** 
 * 通过XML定义的参数 
 * @author zh 
 * 
 */ 
public class DefaultTextCondition extends AbstractCondition implements Condition {
	protected List<VariableBlock> blocks = new ArrayList<>();
	 
	 
	public DefaultTextCondition clone() {
		DefaultTextCondition clone = null;
		try{
			clone = (DefaultTextCondition)super.clone();
		}catch (Exception e) {
			clone = new DefaultTextCondition();
		}
		clone.text = text;
		List<Variable> cVariables = new ArrayList<>();
		Map<Variable, Variable> vmap = new HashMap<>();
		for(Variable var:variables) {
			if(null == var) {
				continue;
			}
			Variable cvar = var.clone();
			vmap.put(var, cvar);
		}
		cVariables.addAll(vmap.values());
		clone.variables = cVariables;
		//block要与condition共享var block不成立时根据block.vars 删除 conditions.vars
		List<VariableBlock> cblocks = new ArrayList<>();
		for(VariableBlock block:blocks) {
			if(null == block) {
				continue;
			}
			VariableBlock cblock = block.clone();
			List<Variable> bvars = block.variables();
			for(Variable var:bvars) {
				Variable cvar = vmap.get(var);
				cblock.variables().add(cvar);
			}
			cblocks.add(cblock);
		}
		clone.blocks = cblocks;

		clone.setSwt(swt);
		return clone; 
	}
	public DefaultTextCondition() {
		join = null;
	}
	public DefaultTextCondition(String text) {
		this(null, text, false);
	}
	public DefaultTextCondition(String id, String text, boolean isStatic) {
		this.id = id; 
		this.text = text; 
		if(!isStatic) {
			parseText();
			setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_INDEX);
		}else{
			setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_NONE);
		} 
	} 
	public void init() {
		setActive(false);
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
		if(null == variable) {
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
	private void parseText() {
		parseText(text);
	}
	private void parseText(String text) {
		try{
			List<List<String>> boxes = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_BOX_REGEX, Regular.MATCH_MODE.CONTAIN);
			if(!boxes.isEmpty()) {
				String box = boxes.get(0).get(0);
				String prev = RegularUtil.cut(text, RegularUtil.TAG_BEGIN, box);
				List<Variable> vars = CommandParser.parseTextVariable(ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT, prev, Compare.EMPTY_VALUE_SWITCH.IGNORE);
				variables.addAll(vars);
				VariableBlock block = parseTextVarBox(text, box);
				if(null != block) {
					blocks.add(block);
					variables.addAll(block.variables());
				}
				String next = RegularUtil.cut(text, box, RegularUtil.TAG_END);
				parseText(next);
			}else{
				List<Variable> vars = CommandParser.parseTextVariable(ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT, text, Compare.EMPTY_VALUE_SWITCH.IGNORE);
				if(!vars.isEmpty()) {
					int type = vars.get(0).getType();
					this.setVariableType(type);
					if(type == Variable.VAR_TYPE_INDEX) {
						for(Variable var:vars) {
							var.setKey(id);
						}
					}
				}
				variables.addAll(vars);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private VariableBlock parseTextVarBox(String text, String box) {
		// ${ AND ID = ::ID}
		// ${AND CODE=:CODE }
		if(null != box) {
			box = box.trim();
			String body = box.substring(2, box.length()-1);
			List<Variable> vars = CommandParser.parseTextVariable(ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT, body, Compare.EMPTY_VALUE_SWITCH.IGNORE);
			VariableBlock block = new DefaultVariableBlock(box, body);
			block.variables(vars);
			return block;
		}
		return null;
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
	public String getRunText(int lvl, String prefix, DataRuntime runtime, Boolean placeholder, Boolean unicode) {
		return getRunText(lvl, prefix, runtime, placeholder, unicode, text);
	}
	public String getRunText(int lvl, String prefix, DataRuntime runtime, Boolean placeholder, Boolean unicode, String text) {
		for (VariableBlock block : blocks) {
			String box = block.box();
			String body = block.body();
			boolean active = block.active();
			if (!active) {
				text = text.replace(box, "");
				variables.removeAll(block.variables());
			} else {
				text = text.replace(box, body);
			}
		}
		text = replaceVariable(lvl, prefix, runtime, placeholder, unicode, text);
		return text;
	}
	public String replaceVariable(int lvl, String prefix, DataRuntime runtime, Boolean placeholder, Boolean unicode, String text) {
		String result = text;
		runValues = new ArrayList<>();
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
		for(Variable variable:variables) {
			if(null == variable) {
				continue;
			}
			List<Object> values = variable.getValues();
			if(swt == Compare.EMPTY_VALUE_SWITCH.BREAK && BasicUtil.isEmpty(true, values)) {
				return false;
			}
		}

		return true;
	}
	public String toString() {
		return text;
	}

}
