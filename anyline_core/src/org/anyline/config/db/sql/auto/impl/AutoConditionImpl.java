/* 
 * Copyright 2006-2015 www.anyline.org
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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.config.db.sql.auto.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.BasicCondition;
import org.anyline.config.db.sql.auto.AutoCondition;
import org.anyline.config.http.Config;
import org.anyline.util.BasicUtil;


/**
 * 自动生成的参数
 * @author Administrator
 *
 */
public class AutoConditionImpl extends BasicCondition implements AutoCondition{
	private static final long serialVersionUID = 7232219177277303525L;
	private String column;		//列
	private Object values;		//参数值
	private int compare = SQL.COMPARE_TYPE_EQUAL;


	public AutoConditionImpl(Config config){
		setJoin(config.getJoin());
		setColumn(config.getId());
		setValues(config.getValues());
		setCompare(config.getCompare());
		setVariableType(Condition.VARIABLE_FLAG_TYPE_INDEX);
		if(config.isRequire()){
			setActive(true);
		}
	}
	/**
	 * @param	required
	 * 			是否必须
	 * @param	column
	 * 			列
	 * @param	values
	 * 			值
	 * @param	compare
	 * 			比较方式
	 */
	public AutoConditionImpl(boolean required, String column, Object values, int compare){
		setRequired(required);
		setColumn(column);
		setValues(values);
		setCompare(compare);
		setVariableType(Condition.VARIABLE_FLAG_TYPE_INDEX);
		if(BasicUtil.isNotEmpty(true,values) || required){
			setActive(true);
		}
	}
	public AutoConditionImpl(String text){
		this.text = text;
		this.active = true;
		setVariableType(Condition.VARIABLE_FLAG_TYPE_NONE);
	}
	/**
	 * 运行时文本
	 */
	@SuppressWarnings("unchecked")
	public String getRunText(SQLCreater creater){
		
		String disKeyFr = creater.getDisKeyFr();
		String diskeyTo = creater.getDisKeyTo();
		runValues = new ArrayList<Object>();
		String text = "";
		if(this.variableType == Condition.VARIABLE_FLAG_TYPE_NONE){
			//静态文本
			text = this.text;
		}else{
			text = disKeyFr + column + diskeyTo;
			if(compare == SQL.COMPARE_TYPE_EQUAL){
				if(null == getValue() || "NULL".equals(getValue())){
					text += " IS NULL";
				}else{
					text += "= ?";
				}
			}else if(compare == SQL.COMPARE_TYPE_GREAT){
				text += "> ?";
			}else if(compare == SQL.COMPARE_TYPE_GREAT_EQUAL){
				text += ">= ?";
			}else if(compare == SQL.COMPARE_TYPE_LITTLE){
				text += "< ?";
			}else if(compare == SQL.COMPARE_TYPE_NOT_EQUAL){
				text += "<> ?";
			}else if(compare == SQL.COMPARE_TYPE_LITTLE_EQUAL){
				text += "<= ?";
			}else if(compare == SQL.COMPARE_TYPE_IN){
				if(values instanceof Collection){
					text += "IN (";
					Collection<Object> coll = (Collection)values;
					int size = coll.size();
					for(int i=0; i<size; i++){
						text += "?";
						if(i < size-1){
							text += ",";
						}
					}
					text += ")";
				}else{
					text += "= ?";
				}
			}else if(compare == SQL.COMPARE_TYPE_LIKE){
				text += " LIKE "+ creater.concat("'%'", "?" , "'%'");
			}else if(compare == SQL.COMPARE_TYPE_LIKE_PREFIX){
				text += " LIKE concat(? , '%')";
				text += " LIKE "+ creater.concat("?" , "'%'");
			}else if(compare == SQL.COMPARE_TYPE_LIKE_SUBFIX){
				text += " LIKE "+ creater.concat("'%'", "?");
			} 
			text += "";
			/*运行时参数*/
			if(compare == SQL.COMPARE_TYPE_IN){
				runValues = getValues();
			}else{
				Object value = getValue();
				runValues = new ArrayList<Object>();
				if((null == value || "NULL".equals(value)) && compare == SQL.COMPARE_TYPE_EQUAL){
				}else{
					runValues.add(value);
				}
			}
		}
		return text;
	}
	@SuppressWarnings("unchecked")
	public Object getValue(){
		Object value = null;
		if(values instanceof List){
			if(((List) values).size()>0){
				value = ((List)values).get(0);
			}
		}else{
			value = values;
		}
		return value;
	}
	@SuppressWarnings("unchecked")
	public List<Object> getValues(){
		List<Object> values;
		if(this.values instanceof List){
			values = (List)this.values;
		}else{
			values = new ArrayList<Object>();
			values.add(this.values);
		}
		return values;
	}

	public String getId(){
		return column;
	}
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}

	public void setValues(Object values) {
		this.values = values;
	}
	public int getCompare() {
		return compare;
	}
	public void setCompare(int compare) {
		this.compare = compare;
	}
}
