/*
 * Copyright 2006-2025 www.anyline.org
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

import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ParseResult;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.init.AbstractRunPrepare;
import org.anyline.data.prepare.text.TextPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.XMLRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.CommandParser;
import org.anyline.entity.Compare;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.ArrayList;
import java.util.List;
public class DefaultTextPrepare extends AbstractRunPrepare implements TextPrepare {
	/*解析XML*/ 
	private String id;
	private boolean strict = true;	// 严格格式, true:不允许添加XML定义之外 的临时查询条件
	private List<Variable> variables = new ArrayList<>();

	public DefaultTextPrepare(String text, boolean parse) {
		super();
		chain = new DefaultTextConditionChain();
		this.text = text;
		this.parse = parse;
	}
	public DefaultTextPrepare() {
		super(); 
		chain = new DefaultTextConditionChain();
	} 
	public RunPrepare init() {
		for(Variable variable:variables) {
			if(null == variable) {
				continue;
			} 
			variable.init(); 
		} 
		if(null != chain) {
			for(Condition condition:chain.getConditions()) {
				if(null == condition) {
					continue;
				} 
				condition.init(); 
			} 
		} 
		return this; 
	}
	/** 
	 * 设置SQL 主体文本 
	 * @param text  text
	 */ 
	public RunPrepare setText(String text) {
		if(null == text) {
			return this; 
		}
		if(!parse){
			return this;
		}
		text = text.replaceAll("--.*","");//过滤注释
		this.text = text; 
		parseText(); 
		return this; 
	} 
 
	/** 
	 * 添加静态文本查询条件 
	 */ 
	public RunPrepare addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)) {
			return this; 
		} 
		if(condition.contains(":")) {
			ParseResult parser = ConfigParser.parse(condition,false);
			 
			String prefix = parser.getPrefix();
			String var = null; 
			Object value = ConfigParser.getValues(parser);//parser.getKey(); 
			if(prefix.contains(".")) {
				String[] keys = prefix.split(".");
				prefix = keys[0];
				if(keys.length > 1) {
					var = keys[1]; 
				} 
			} 
			setConditionValue(prefix,var,value);
		} 
		return this; 
	} 
	/** 
	 * 解析 RunPrepare 主体文本
	 */ 
	private void parseText() {
		if(null == text) {
			return;
		}
		if(!parse){
			return;
		}
		List<Variable> vars = CommandParser.parseTextVariable(ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT, text, Compare.EMPTY_VALUE_SWITCH.NULL);
		this.variables.addAll(vars);
	} 
	/** 
	 * 添加SQL主体变量 
	 * @param var  var
	 */ 
	private void addVariable(Variable var) {
		variables.add(var); 
	} 
 
 
	/* *********************************************************************************************************************************** 
	 *  
	 * 														赋值 
	 *  
	 * ***********************************************************************************************************************************/ 
	/** 
	 * 添加查询条件 
	 * @param condition 
	 * 			列名|查询条件ID 
	 * @param variable 
	 * 			变量key 
	 * @param value 
	 * 			值 
	 */ 
	public RunPrepare setConditionValue(String condition, String variable, Object value) {
		/*不指定变量名时,根据condition为SQL主体变量赋值*/ 
		if(BasicUtil.isEmpty(variable)) {
			for(Variable v:variables) {
				if(v.getKey().equalsIgnoreCase(condition)) {
					v.setValue(value); 
				} 
			} 
		} 
		/*参数赋值*/ 
		if(null == condition) {
			return this; 
		} 
		DefaultTextCondition con = getCondition(condition);
		if(null == con) {
			return this; 
		} 
		variable = BasicUtil.nvl(variable, condition).toString(); 
		con.setValue(variable, value); 
		return this; 
	} 
	private DefaultTextCondition getCondition(String id) {
		if(null == chain) {
			return null; 
		} 
		for(Condition con:chain.getConditions()) {
			if(BasicUtil.equalsIgnoreCase(id, con.getId())) {
				return (DefaultTextCondition)con;
			} 
		} 
		return null; 
	} 
	/* *********************************************************************************************************************************** 
	 *  
	 * 														生成SQL 
	 *  
	 * ***********************************************************************************************************************************/ 
//	public void appendCondition(StringBuilder builder, String delimiter) {
//		if(null == chain) {
//			return; 
//		} 
//		builder.append(chain.getRunText(delimiter));
//		addRunValue(chain.getRunValues()); 
//	} 
//	/** 
//	 * 生成运行时SQL 
//	 */ 
//	public String getRunText(String delimiter) {
//		StringBuilder builder = new StringBuilder(); 
//		builder.append(createRunText()); 
//		appendCondition(builder, delimiter);
//		return builder.toString(); 
//	} 
 
//	/** 
//	 * 添加分组 
//	 * @param builder  builder
//	 */ 
//	public void appendGroup(StringBuilder builder) {
//		if(null != groups && !groups.isEmpty()) {
//			int size = groups.size(); 
//			builder.append(" GROUP BY "); 
//			for(int i=0; i<size; i++) {
//				builder.append(groups.get(i)); 
//				if(i<size-1) {
//					builder.append(", "); 
//				} 
//			} 
//		} 
//	} 
	/*  
	 * 创建运行时主体SQL 
	 * @param text  text
	 * @return String
	 */ 
//	private String createRunText() {
//		initRunValues(); 
//		String result = text; 
//		if(null == variables) {
//			return result;
//		} 
// 
//		for(Variable var:variables) {
//			if(var.getType() == Variable.VAR_TYPE_REPLACE) {
//				// CD = ::CD 
//				Object varValue = var.getValues(); 
//				String value = null; 
//				if(BasicUtil.isNotEmpty(varValue)) {
//					value = varValue.toString(); 
//				} 
//				if(null != value) {
//					result = result.replace("::"+var.getKey(), value); 
//				}else{
//					result = result.replace("::"+var.getKey(), "NULL"); 
//				} 
//			} 
//		} 
//		for(Variable var:variables) {
//			if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE) {
//				// CD = ':CD' 
//				List<Object> varValues = var.getValues(); 
//				String value = null; 
//				if(BasicUtil.isNotEmpty(true,varValues)) {
//					value = (String)varValues.get(0); 
//				} 
//				if(null != value) {
//					result = result.replace(":"+var.getKey(), value); 
//				}else{
//					result = result.replace(":"+var.getKey(), ""); 
//				} 
//			} 
//		} 
//		for(Variable var:variables) {
//			if(var.getType() == Variable.VAR_TYPE_KEY) {
//				// CD = :CD 
//				List<Object> varValues = var.getValues(); 
//				if(BasicUtil.isNotEmpty(true, varValues)) {
//					if(var.getCompare() == Compare_IN) {
//						// 多个值IN 
//						String replaceSrc = ":"+var.getKey(); 
//						String replaceDst = "";  
//						for(Object tmp:varValues) {
//							addRunValue(tmp); 
//							replaceDst += " ?"; 
//						} 
//						replaceDst = replaceDst.trim().replace(" ",",");
//						result = result.replace(replaceSrc, replaceDst); 
//					}else{
//						// 单个值 
//						result = result.replace(":"+var.getKey(), "?"); 
//						addRunValue(varValues.get(0)); 
//					} 
//				} 
//			} 
//		} 
//		// 添加其他变量值 
//		for(Variable var:variables) {
//			// CD = ? 
//			if(var.getType() == Variable.VAR_TYPE_INDEX) {
//				List<Object> varValues = var.getValues(); 
//				String value = null; 
//				if(BasicUtil.isNotEmpty(true, varValues)) {
//					value = (String)varValues.get(0); 
//				} 
//				addRunValue(value); 
//			} 
//		} 
//		return result; 
//	} 
 
	public RunPrepare setDest(String dest) {
		this.id = dest;
		return this; 
	}

	@Override
	public RunPrepare setDest(Table dest) {
		return null;
	}

	@Override
	public RunPrepare setCatalog(String catalog) {
		return null;
	}

	@Override
	public Catalog getCatalog() {
		return null;
	}

	@Override
	public String getCatalogName() {
		return null;
	}

	@Override
	public RunPrepare setSchema(String schema) {
		return null;
	}

	@Override
	public String getSchemaName() {
		return null;
	}

	@Override
	public String getTableName() {
		return null;
	}

	public String getDest() {
		return id ; 
	} 
	public Schema getSchema() {
		return null; 
	} 
	@Override 
	public String getText() {
		return this.text; 
	} 
	@Override 
	public List<Variable> getSQLVariables() {
		return this.variables; 
	}

	@Override
	public RunPrepare addColumn(String columns) {
		return this;
	}
	@Override
	public RunPrepare addColumn(Column column) {
		return this;
	}

	@Override
	public RunPrepare excludeColumn(String columns) {
		return this;
	}

	@Override
	public Table getTable() {
		return null;
	}
	@Override
	public Run build(DataRuntime runtime) {
		XMLRun run = new XMLRun();
		run.setPrepare(this);
		run.setRuntime(runtime);
		return run;
	}


	@Override
	public DataRow map(boolean empty, boolean join) {
		DataRow row = new OriginRow();
		row.put("text", text);
		return row;
	}
	public DefaultTextPrepare clone() {
		DefaultTextPrepare clone = null;
		try{
			clone = (DefaultTextPrepare)super.clone();
		}catch (Exception e) {
			clone =new DefaultTextPrepare();
		}
		clone.chain = chain.clone();
		if(!variables.isEmpty()) {
			List<Variable> cVariables = new ArrayList<>();
			for(Variable var:variables) {
				if(null == var) {
					continue;
				}
				cVariables.add(var.clone());
			}
			clone.variables = cVariables;
		}
		return clone;
	}
} 
