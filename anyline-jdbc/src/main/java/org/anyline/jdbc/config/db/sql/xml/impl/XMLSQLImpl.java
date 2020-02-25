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


package org.anyline.jdbc.config.db.sql.xml.impl; 
 
import java.util.ArrayList; 
import java.util.List; 

import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLHelper;
import org.anyline.jdbc.config.db.SQLVariable;
import org.anyline.jdbc.config.db.impl.BasicSQL;
import org.anyline.jdbc.config.db.impl.SQLVariableImpl;
import org.anyline.jdbc.config.db.sql.xml.XMLSQL;
import org.anyline.util.BasicUtil; 
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil; 
public class XMLSQLImpl extends BasicSQL implements XMLSQL{ 
	/*解析XML*/ 
	private String id; 
	private String text;
	private boolean strict = true;	//严格格式, true:不允许添加XML定义之外 的临时查询条件
	private List<SQLVariable> variables; 
	 
	public XMLSQLImpl(){ 
		super(); 
		chain = new XMLConditionChainImpl(); 
	} 
	public SQL init() { 
		if(null == variables){ 
			variables = new ArrayList<SQLVariable>(); 
		} 
		for(SQLVariable variable:variables){
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
	public Object clone() throws CloneNotSupportedException{ 
		XMLSQLImpl clone = (XMLSQLImpl)super.clone(); 
		clone.chain = (ConditionChain)chain.clone(); 
		if(null != variables){ 
			List<SQLVariable> cVariables = new ArrayList<SQLVariable>(); 
			for(SQLVariable var:variables){
				if(null == var){
					continue;
				} 
				cVariables.add((SQLVariable)var.clone()); 
			} 
			clone.variables = cVariables; 
		} 
		return clone; 
	} 
	/** 
	 * 设置SQL 主体文本 
	 * @param text  text
	 */ 
	public SQL setText(String text) { 
		if(null == text){ 
			return this; 
		} 
		text = text.replaceAll("--.*", "");//过滤注释 
		this.text = text; 
		parseText(); 
		return this; 
	} 
 
	/** 
	 * 添加静态文本查询条件 
	 */ 
	public SQL addCondition(String condition) { 
		if(BasicUtil.isEmpty(condition)){ 
			return this; 
		} 
		if(condition.contains(":")){
			ParseResult parser = ConfigParser.parse(condition,false);
			 
			String id = parser.getId(); 
			String var = null; 
			Object value = ConfigParser.getValues(parser);//parser.getKey(); 
			if(id.contains(".")){ 
				String[] keys = id.split("."); 
				id = keys[0]; 
				if(keys.length > 1){ 
					var = keys[1]; 
				} 
			} 
			setConditionValue(id,var,value); 
		} 
		return this; 
	} 
	/** 
	 * 解析 SQL 主体文本 
	 */ 
	private void parseText(){ 
		if(null == text) {
			return;
		} 
		try{
			 
			List<List<String>> keys = RegularUtil.fetchs(text, SQL_PARAM_VAIRABLE_REGEX, Regular.MATCH_MODE.CONTAIN);
			int type = 1 ;
			if(keys.size() ==0){
				keys = RegularUtil.fetchs(text, SQL_PARAM_VAIRABLE_REGEX_EL, Regular.MATCH_MODE.CONTAIN);
				type = 2;
			} 
			if(BasicUtil.isNotEmpty(true,keys)){ 
				//AND CD = :CD 
				for(int i=0; i<keys.size();i++){
					List<String> keyItem = keys.get(i); 
					SQLVariable var = SQLHelper.buildVariable(type, keyItem.get(0), keyItem.get(1), keyItem.get(2), keyItem.get(3));
					var.setRequired(true);
					addVariable(var); 
				}// end for 
			}else{ 
				// AND CD = ? 
				List<String> idxKeys = RegularUtil.fetch(text, "\\?",Regular.MATCH_MODE.CONTAIN,0); 
				if(BasicUtil.isNotEmpty(true,idxKeys)){ 
					for(int i=0; i<idxKeys.size(); i++){ 
						SQLVariable var = new SQLVariableImpl(); 
						var.setType(SQLVariable.VAR_TYPE_INDEX);
						var.setRequired(true); 
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
	private void addVariable(SQLVariable var){ 
		if(null == variables){ 
			variables = new ArrayList<SQLVariable>(); 
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
	 * @param	condition 
	 * 			列名|查询条件ID 
	 * @param	variable 
	 * 			变量key 
	 * @param	value 
	 * 			值 
	 */ 
	public SQL setConditionValue(String condition, String variable, Object value){ 
		/*不指定变量名时,根据condition为SQL主体变量赋值*/ 
		if(null != variables && BasicUtil.isEmpty(variable)){ 
			for(SQLVariable v:variables){ 
				if(v.getKey().equalsIgnoreCase(condition)){ 
					v.setValue(value); 
				} 
			} 
		} 
		/*参数赋值*/ 
		if(null == condition){ 
			return this; 
		} 
		XMLConditionImpl con = getCondition(condition); 
		if(null == con){ 
			return this; 
		} 
		variable = BasicUtil.nvl(variable, condition).toString(); 
		con.setValue(variable, value); 
		return this; 
	} 
	private XMLConditionImpl getCondition(String id){ 
		if(null == chain){ 
			return null; 
		} 
		for(Condition con:chain.getConditions()){ 
			if(BasicUtil.isEqual(id, con.getId())){ 
				return (XMLConditionImpl)con; 
			} 
		} 
		return null; 
	} 
	/* *********************************************************************************************************************************** 
	 *  
	 * 														生成SQL 
	 *  
	 * ***********************************************************************************************************************************/ 
//	public void appendCondition(StringBuilder builder, String disKey){ 
//		if(null == chain){ 
//			return; 
//		} 
//		builder.append(chain.getRunText(disKey)); 
//		addRunValue(chain.getRunValues()); 
//	} 
//	/** 
//	 * 生成运行时SQL 
//	 */ 
//	public String getRunText(String diskey){ 
//		StringBuilder builder = new StringBuilder(); 
//		builder.append(createRunText()); 
//		appendCondition(builder, diskey); 
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
	 * @return return
	 */ 
//	private String createRunText(){ 
//		initRunValues(); 
//		String result = text; 
//		if(null == variables) {
//			return result;
//		} 
// 
//		for(SQLVariable var:variables){ 
//			if(var.getType() == SQLVariable.VAR_TYPE_REPLACE){ 
//				//CD = ::CD 
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
//		for(SQLVariable var:variables){ 
//			if(var.getType() == SQLVariable.VAR_TYPE_KEY_REPLACE){ 
//				//CD = ':CD' 
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
//		for(SQLVariable var:variables){ 
//			if(var.getType() == SQLVariable.VAR_TYPE_KEY){ 
//				// CD = :CD 
//				List<Object> varValues = var.getValues(); 
//				if(BasicUtil.isNotEmpty(true, varValues)){ 
//					if(var.getCompare() == SQL.COMPARE_TYPE_IN){ 
//						//多个值IN 
//						String replaceSrc = ":"+var.getKey(); 
//						String replaceDst = "";  
//						for(Object tmp:varValues){ 
//							addRunValue(tmp); 
//							replaceDst += " ?"; 
//						} 
//						replaceDst = replaceDst.trim().replace(" ", ","); 
//						result = result.replace(replaceSrc, replaceDst); 
//					}else{ 
//						//单个值 
//						result = result.replace(":"+var.getKey(), "?"); 
//						addRunValue(varValues.get(0)); 
//					} 
//				} 
//			} 
//		} 
//		//添加其他变量值 
//		for(SQLVariable var:variables){ 
//			//CD = ? 
//			if(var.getType() == SQLVariable.VAR_TYPE_INDEX){ 
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
 
	public SQL setDataSource(String ds){ 
		this.id = ds; 
		return this; 
	} 
	public String getDataSource(){ 
		return id ; 
	} 
	public String getSchema(){ 
		return null; 
	} 
	@Override 
	public String getText() { 
		return this.text; 
	} 
	@Override 
	public List<SQLVariable> getSQLVariables() { 
		return this.variables; 
	}
	@Override
	public String getTable() {
		return null;
	}
	@Override
	public List<String> getColumns() {
		return null;
	}
	public boolean isStrict() {
		return strict;
	}
	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
} 
