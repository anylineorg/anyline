
package org.anyline.config.db.sql.xml.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.BasicCondition;
import org.anyline.config.db.impl.SQLVariableImpl;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;


/**
 * 通过XML定义的参数
 * @author Administrator
 *
 */
public class XMLConditionImpl extends BasicCondition implements Condition{
	Logger log = Logger.getLogger(XMLConditionImpl.class);
	
	private String text;
	
	private List<SQLVariable> variables;	//变量
	
	
	public Object clone() throws CloneNotSupportedException{
		XMLConditionImpl clone = (XMLConditionImpl)super.clone();
		if(null != variables){
			List<SQLVariable> cVariables = new ArrayList<SQLVariable>();
			for(SQLVariable var:variables){
				cVariables.add((SQLVariable)var.clone());
			}
			clone.variables = cVariables;
		}
		return clone;
	}
	public XMLConditionImpl(){
		join = "";
	}
	public XMLConditionImpl(String id, String text, boolean isStatic){
		join = "";
		this.id = id;
		this.text = text;
		setVariableType(Condition.VARIABLE_FLAG_TYPE_INDEX);
		if(!isStatic){
			parseText();
		}
	}
	public XMLConditionImpl(String text){
		this.text = " AND " + text;
		this.active = true;
		setVariableType(Condition.VARIABLE_FLAG_TYPE_NONE);
	}
	public void init(){
		setActive(false);
		if(null == variables){
			variables = new ArrayList<SQLVariable>();
		}
		for(SQLVariable variable:variables){
			variable.init();
		}
	}
	/**
	 * 赋值
	 * @param variable
	 * @param values
	 */
	public void setValue(String variable, Object values){
		if(null == variable || null == variables){
			return;
		}
		for(SQLVariable v:variables){
			if(variable.equalsIgnoreCase(v.getKey())){
				v.setValue(values);
				if(BasicUtil.isNotEmpty(true,values)){
					setActive(true);
				}
				break;
			}
		}
	}

	/**
	 * 解析变量
	 * @return
	 */
	private void parseText(){
		try{
			List<List<String>> keys = RegularUtil.fetch(text, SQL.SQL_PARAM_VAIRABLE_REGEX, RegularUtil.MATCH_MODE_CONTAIN);
			if(BasicUtil.isNotEmpty(true,keys)){
				////AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD
				setVariableType(VARIABLE_FLAG_TYPE_KEY);
				int varType = SQLVariable.VAR_TYPE_INDEX;
				int compare = SQL.COMPARE_TYPE_EQUAL;
				for(int i=0; i<keys.size(); i++){
					List<String> keyItem = keys.get(i);
					String prefix = keyItem.get(1).trim();		// 前缀
					String fullKey = keyItem.get(2).trim();		// 完整KEY :CD
					String typeChar = keyItem.get(3);	// null || "'" || ")"
					String key = fullKey.replace(":", "");
					if(fullKey.startsWith("::")){
						// AND CD = ::CD
						varType = SQLVariable.VAR_TYPE_REPLACE;
					}else if(BasicUtil.isNotEmpty(typeChar) && ("'".equals(typeChar) || "%".equals(typeChar))){
						// AND CD = ':CD'
						varType = SQLVariable.VAR_TYPE_KEY_REPLACE;
					}else{
						// AND CD = :CD
						varType = SQLVariable.VAR_TYPE_KEY;
						if(prefix.equalsIgnoreCase("IN") || prefix.equalsIgnoreCase("IN(")){
							compare = SQL.COMPARE_TYPE_IN;
						}
					}
					SQLVariable var = new SQLVariableImpl();
					var.setKey(key);
					var.setType(varType);
					var.setCompare(compare);
					addVariable(var);
				}
			}else{
				List<String> idxKeys = RegularUtil.fetch(text, "\\?",RegularUtil.MATCH_MODE_CONTAIN,0);
				if(BasicUtil.isNotEmpty(true,idxKeys)){
					//按下标区分变量
					this.setVariableType(VARIABLE_FLAG_TYPE_INDEX);
					int varType = SQLVariable.VAR_TYPE_INDEX;
					for(int i=0; i<idxKeys.size(); i++){
						SQLVariable var = new SQLVariableImpl();
						var.setType(varType);
						var.setKey(id);
						addVariable(var);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
		}
	}

	private void addVariable(SQLVariable variable){
		if(null == variables){
			variables = new ArrayList<SQLVariable>();
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

	

	public String getRunText(String disKey) {
		String result = text;
		runValues = new ArrayList<Object>();
		if(null == variables) return result;
		for(SQLVariable var: variables){
			if(var.getType() == SQLVariable.VAR_TYPE_REPLACE){
				//CD = ::CD
				List<Object> values = var.getValues();
				String value = null;
				if(BasicUtil.isNotEmpty(true,values)){
					value = (String)values.get(0);
				}
				if(BasicUtil.isNotEmpty(value)){
					result = result.replace("::"+var.getKey(), value);
				}else{
					result = result.replace("::"+var.getKey(), "NULL");
				}
			}
		}
		for(SQLVariable var: variables){
			if(var.getType() == SQLVariable.VAR_TYPE_KEY_REPLACE){
				//CD = ':CD'
				List<Object> values = var.getValues();
				String value = null;
				if(BasicUtil.isNotEmpty(true,values)){
					value = (String)values.get(0);
				}
				if(null != value){
					result = result.replace(":"+var.getKey(), value);
				}else{
					result = result.replace(":"+var.getKey(), "");
				}
			}
		}
		for(SQLVariable var:variables){
			if(var.getType() == SQLVariable.VAR_TYPE_KEY){
				//CD=:CD
				List<Object> varValues = var.getValues();
				if(SQL.COMPARE_TYPE_IN == var.getCompare()){
					String inParam = "";
					for(int i=0; i<varValues.size(); i++){
						inParam += "?";
						if(i<varValues.size()-1){
							inParam += ",";
						}
					}
					result = result.replace(":"+var.getKey(), inParam);
					runValues.addAll(varValues);	
				}else{
					result = result.replace(":"+var.getKey(), "?");
					String value = null;
					if(BasicUtil.isNotEmpty(true,varValues)){
						value = varValues.get(0).toString();
					}
					runValues.add(value);
				}
				
			}
		}
		
		for(SQLVariable var:variables){
			if(var.getType() == SQLVariable.VAR_TYPE_INDEX){
				List<Object> values = var.getValues();
				String value = null;
				if(BasicUtil.isNotEmpty(true,values)){
					value = (String)values.get(0);
				}
				runValues.add(value);
			}
		}
		return result;
	}
	public SQLVariable getVariable(String key) {
		if(null == variables || null == key){
			return null;
		}
		for(SQLVariable variable:variables){
			if(key.equalsIgnoreCase(variable.getKey())){
				return variable;
			}
		}
		return null;
	}
}