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
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ParseResult;
import org.anyline.data.prepare.init.AbstractRunPrepare;
import org.anyline.data.prepare.init.DefaultVariable;
import org.anyline.data.prepare.SyntaxHelper;
import org.anyline.data.prepare.xml.XMLPrepare;
import org.anyline.entity.Compare;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.util.ArrayList;
import java.util.List;
public class DefaultXMLPrepare extends AbstractRunPrepare implements XMLPrepare {
	/*解析XML*/ 
	private String id; 
	private String text;
	private boolean strict = true;	// 严格格式, true:不允许添加XML定义之外 的临时查询条件
	private List<Variable> variables;
	 
	public DefaultXMLPrepare(){
		super(); 
		chain = new DefaultXMLConditionChain();
	} 
	public RunPrepare init() {
		if(null == variables){
			variables = new ArrayList<Variable>();
		} 
		for(Variable variable:variables){
			if(null == variable){
				continue;
			} 
			variable.init(); 
		} 
		if(null != chain){
			for(Condition condition:chain.getConditions()){
				if(null == condition){
					continue;
				} 
				condition.init(); 
			} 
		} 
		return this; 
	} 
	public DefaultXMLPrepare clone() {
		DefaultXMLPrepare clone = null;
		try{
			clone = (DefaultXMLPrepare)super.clone();
		}catch (Exception e){
			clone = new DefaultXMLPrepare();
		}
		clone.chain = chain.clone();
		if(null != variables){
			List<Variable> cVariables = new ArrayList<>();
			for(Variable var:variables){
				if(null == var){
					continue;
				} 
				cVariables.add(var.clone());
			} 
			clone.variables = cVariables; 
		} 
		return clone; 
	} 
	/** 
	 * 设置SQL 主体文本 
	 * @param text  text
	 */ 
	public RunPrepare setText(String text) {
		if(null == text){
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
		if(BasicUtil.isEmpty(condition)){
			return this; 
		} 
		if(condition.contains(":")){
			ParseResult parser = ConfigParser.parse(condition,false);
			 
			String prefix = parser.getPrefix();
			String var = null; 
			Object value = ConfigParser.getValues(parser);//parser.getKey(); 
			if(prefix.contains(".")){
				String[] keys = prefix.split(".");
				prefix = keys[0];
				if(keys.length > 1){
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
	private void parseText(){
		if(null == text) {
			return;
		} 
		try{
			List<List<String>> keys = null;
			int type = 0;

			// AND CD = {CD} || CD LIKE '%{CD}%' || CD IN ({CD}) || CD = ${CD} || CD = #{CD}
			//{CD} 用来兼容旧版本，新版本中不要用，避免与josn格式冲突
			keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX, Regular.MATCH_MODE.CONTAIN);
			type = Variable.KEY_TYPE_SIGN_V2 ;
			if(keys.isEmpty() && ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT){
				// AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD
				keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX_EXT, Regular.MATCH_MODE.CONTAIN);
				type = Variable.KEY_TYPE_SIGN_V1 ;
			} 
			if(BasicUtil.isNotEmpty(true,keys)){
				// AND CD = :CD AND CD = {CD} AND CD = ${CD} AND CD = ${CD}
				for(int i=0; i<keys.size();i++){
					List<String> keyItem = keys.get(i); 
					Variable var = SyntaxHelper.buildVariable(type, keyItem.get(0), keyItem.get(1), keyItem.get(2), keyItem.get(3));
					if(null == var){
						continue;
					}
					var.setSwitch(Compare.EMPTY_VALUE_SWITCH.NULL);
					addVariable(var); 
				}// end for 
			}else{
				// AND CD = ? 
				List<String> idxKeys = RegularUtil.fetch(text, "\\?",Regular.MATCH_MODE.CONTAIN,0); 
				if(BasicUtil.isNotEmpty(true,idxKeys)){
					for(int i=0; i<idxKeys.size(); i++){
						Variable var = new DefaultVariable();
						var.setType(Variable.VAR_TYPE_INDEX);
						var.setSwitch(Compare.EMPTY_VALUE_SWITCH.NULL);
						addVariable(var); 
					} 
				} 
			} 
		}catch(Exception e){
			e.printStackTrace(); 
		} 
	} 
	/** 
	 * 添加SQL主体变量 
	 * @param var  var
	 */ 
	private void addVariable(Variable var){
		if(null == variables){
			variables = new ArrayList<Variable>();
		}
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
	public RunPrepare setConditionValue(String condition, String variable, Object value){
		/*不指定变量名时,根据condition为SQL主体变量赋值*/ 
		if(null != variables && BasicUtil.isEmpty(variable)){
			for(Variable v:variables){
				if(v.getKey().equalsIgnoreCase(condition)){
					v.setValue(value); 
				} 
			} 
		} 
		/*参数赋值*/ 
		if(null == condition){
			return this; 
		} 
		DefaultXMLCondition con = getCondition(condition);
		if(null == con){
			return this; 
		} 
		variable = BasicUtil.nvl(variable, condition).toString(); 
		con.setValue(variable, value); 
		return this; 
	} 
	private DefaultXMLCondition getCondition(String id){
		if(null == chain){
			return null; 
		} 
		for(Condition con:chain.getConditions()){
			if(BasicUtil.equalsIgnoreCase(id, con.getId())){
				return (DefaultXMLCondition)con;
			} 
		} 
		return null; 
	} 
	/* *********************************************************************************************************************************** 
	 *  
	 * 														生成SQL 
	 *  
	 * ***********************************************************************************************************************************/ 
//	public void appendCondition(StringBuilder builder, String delimiter){
//		if(null == chain){
//			return; 
//		} 
//		builder.append(chain.getRunText(delimiter));
//		addRunValue(chain.getRunValues()); 
//	} 
//	/** 
//	 * 生成运行时SQL 
//	 */ 
//	public String getRunText(String delimiter){
//		StringBuilder builder = new StringBuilder(); 
//		builder.append(createRunText()); 
//		appendCondition(builder, delimiter);
//		return builder.toString(); 
//	} 
 
//	/** 
//	 * 添加分组 
//	 * @param builder  builder
//	 */ 
//	public void appendGroup(StringBuilder builder){
//		if(null != groups && !groups.isEmpty()){
//			int size = groups.size(); 
//			builder.append(" GROUP BY "); 
//			for(int i=0; i<size; i++){
//				builder.append(groups.get(i)); 
//				if(i<size-1){
//					builder.append(","); 
//				} 
//			} 
//		} 
//	} 
	/*  
	 * 创建运行时主体SQL 
	 * @param text  text
	 * @return String
	 */ 
//	private String createRunText(){
//		initRunValues(); 
//		String result = text; 
//		if(null == variables) {
//			return result;
//		} 
// 
//		for(Variable var:variables){
//			if(var.getType() == Variable.VAR_TYPE_REPLACE){
//				// CD = ::CD 
//				Object varValue = var.getValues(); 
//				String value = null; 
//				if(BasicUtil.isNotEmpty(varValue)){
//					value = varValue.toString(); 
//				} 
//				if(null != value){
//					result = result.replace("::"+var.getKey(), value); 
//				}else{
//					result = result.replace("::"+var.getKey(), "NULL"); 
//				} 
//			} 
//		} 
//		for(Variable var:variables){
//			if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE){
//				// CD = ':CD' 
//				List<Object> varValues = var.getValues(); 
//				String value = null; 
//				if(BasicUtil.isNotEmpty(true,varValues)){
//					value = (String)varValues.get(0); 
//				} 
//				if(null != value){
//					result = result.replace(":"+var.getKey(), value); 
//				}else{
//					result = result.replace(":"+var.getKey(), ""); 
//				} 
//			} 
//		} 
//		for(Variable var:variables){
//			if(var.getType() == Variable.VAR_TYPE_KEY){
//				// CD = :CD 
//				List<Object> varValues = var.getValues(); 
//				if(BasicUtil.isNotEmpty(true, varValues)){
//					if(var.getCompare() == Compare_IN){
//						// 多个值IN 
//						String replaceSrc = ":"+var.getKey(); 
//						String replaceDst = "";  
//						for(Object tmp:varValues){
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
//		for(Variable var:variables){
//			// CD = ? 
//			if(var.getType() == Variable.VAR_TYPE_INDEX){
//				List<Object> varValues = var.getValues(); 
//				String value = null; 
//				if(BasicUtil.isNotEmpty(true, varValues)){
//					value = (String)varValues.get(0); 
//				} 
//				addRunValue(value); 
//			} 
//		} 
//		return result; 
//	} 
 
	public RunPrepare setDest(String dest){
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

	public String getDest(){
		return id ; 
	} 
	public Schema getSchema(){
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
	
} 
