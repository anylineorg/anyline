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
 *
 *          
 */


package org.anyline.data.prepare.auto.init;

import org.anyline.data.param.Config;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.init.DefaultCondition;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.SQLUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public class DefaultAutoCondition extends DefaultCondition implements AutoCondition {
	private String table;			// 表或表别名
	private String column;			// 列
	private Object values;			// 参数值
	private Object orValues;		// 参数值 
	private Compare compare = Compare.EQUAL;
	private Compare orCompare = Compare.EQUAL;
 
 
	public DefaultAutoCondition(Config config){
		setJoin(config.getJoin());
		setTable(config.getPrefix());   	// 表名或表别名
		setColumn(config.getVariable());   // 列名
		setValues(config.getValues()); 
		setOrValues(config.getOrValues());
		setCompare(config.getCompare());
		setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_INDEX);
		setSwitch(config.getSwitch());
		if(BasicUtil.isNotEmpty(true, values) || config.getSwitch() == EMPTY_VALUE_SWITCH.NULL || config.getSwitch() == EMPTY_VALUE_SWITCH.SRC){
			setActive(true); 
		} 
	} 
	/** 
	 * @param swt  遇到空值处理方式
	 * @param prefix  表
	 * @param var  列
	 * @param values 值 
	 * @param compare  比较方式 
	 */ 
	public DefaultAutoCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object values){
		setSwitch(swt);
		setTable(prefix);
		setColumn(var);
		setValues(values);
		setCompare(compare);
		setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_INDEX);
		if(BasicUtil.isNotEmpty(true, values) || swt == EMPTY_VALUE_SWITCH.NULL || swt == EMPTY_VALUE_SWITCH.SRC){
			setActive(true); 
		} 
	} 
	public DefaultAutoCondition(String text){
		this.text = text; 
		this.active = true; 
		setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_NONE); 
	} 


	/** 
	 * 运行时文本
	 * @param prefix 前缀
	 * @param runtime 运行环境主要包含适配器数据源或客户端
	 * @return String
	 */
	@Override
	public String getRunText(String prefix, DataRuntime runtime){
		runValues = new ArrayList<>();
		String text = "";
		if(this.variableType == Condition.VARIABLE_PLACEHOLDER_TYPE_NONE){//没有变量
			text = this.text; 
		}else{
			String txt = "";
			//if(BasicUtil.isNotEmpty(true, values) || isRequired()){
				txt = getRunText(prefix, runtime, values, compare);
				if(BasicUtil.isNotEmpty(txt)){
					text = txt;
				}
				if(BasicUtil.isNotEmpty(true, orValues)){
					txt = getRunText(prefix, runtime, orValues, orCompare);
					if(BasicUtil.isNotEmpty(txt)){
						if(BasicUtil.isEmpty(text)){
							text = txt;
						}else{
							text = "(" + text +" OR " + txt + ")";
						}
					}
				}
			}
		return text; 
	} 

	@SuppressWarnings({"rawtypes", "unchecked" })
	public String getRunText(String prefix, DataRuntime runtime, Object val, Compare compare){
		StringBuilder builder = new StringBuilder();
		String delimiterFr = runtime.getAdapter().getDelimiterFr();
		String delimiterTo = runtime.getAdapter().getDelimiterTo();
		boolean empty = BasicUtil.isEmpty(true, val);
		int compareCode = compare.getCode();
		if(compareCode == -1){
			//只作参数赋值
			return "";
		}
		if(empty){
			if(swt == EMPTY_VALUE_SWITCH.BREAK || swt == EMPTY_VALUE_SWITCH.IGNORE){
				return  "";
			}
		}
		if(BasicUtil.isNotEmpty(table)){
			prefix = table;
		}

		StringBuilder col_builder = new StringBuilder();
		if(!column.contains(".")){
			if(BasicUtil.isNotEmpty(prefix)){
				SQLUtil.delimiter(col_builder, prefix, delimiterFr, delimiterTo).append(".");
			}else {
				if (BasicUtil.isNotEmpty(table)) {
					SQLUtil.delimiter(col_builder, table, delimiterFr, delimiterTo).append(".");
				}
			}
		}
		SQLUtil.delimiter(col_builder, column, delimiterFr, delimiterTo);
		if(compareCode >=60 && compareCode <= 62){				// FIND_IN_SET(?, CODES)
			val = runtime.getAdapter().createConditionFindInSet(runtime, builder, col_builder.toString(), compare, val);
		}else{
			builder.append(col_builder);
			if(compareCode == 10){
				Object v = getValue(val);
				if("NULL".equals(v)){
					builder.append(" IS NULL");
					this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
				}else if (empty){//空值根据swt处理
					if(swt == EMPTY_VALUE_SWITCH.NULL){
						builder.append(" IS NULL");
						this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
					}else if(swt == EMPTY_VALUE_SWITCH.SRC && null == v){
						builder.append(" IS NULL");
						this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
					}else{
						builder.append(compare.getSQL());
					}
				}else{
					builder.append(compare.getSQL());
				}
				/*if(null == v || "NULL".equals(v.toString())){
					builder.append(" IS NULL");
					if("NULL".equals(getValue())){
						this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
					}
				}else{
					builder.append(compare.getSQL());
				}*/
			}else if(compareCode == 20){ 							// > ?
				builder.append(compare.getSQL());
			}else if(compareCode == 21){ 							// >= ?
				builder.append(compare.getSQL());
			}else if(compareCode == 30){ 							// < ?
				builder.append(compare.getSQL());
			}else if(compareCode == 110){ 							// <> ?
				Object v = getValue(val);
				if("NULL".equals(v.toString())){
					builder.append(" IS NOT NULL");
					this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
				}else if (empty){//空值根据swt处理
					if(swt == EMPTY_VALUE_SWITCH.NULL){
						builder.append(" IS NOT NULL");
						this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
					}else if(swt == EMPTY_VALUE_SWITCH.SRC && null == v){
						builder.append(" IS NOT NULL");
						this.variableType = Condition.VARIABLE_PLACEHOLDER_TYPE_NONE;
					}else{
						builder.append(compare.getSQL());
					}
				}else{
					builder.append(compare.getSQL());
				}
			}else if(compareCode == 31){ 							// <= ?
				builder.append(compare.getSQL());
			}else if(compareCode == 80){ 							// BETWEEN ? AND ?
				builder.append(compare.getSQL());
			}else if(compareCode == 40 || compareCode == 140){		// IN(?,?,?)
				runtime.getAdapter().createConditionIn(runtime, builder, compare, val);
			}else if((compareCode >= 50 && compareCode <= 52) 		// LIKE ?
					|| (compareCode >= 150 && compareCode <= 152)){ // NOT LIKE ?
				val = runtime.getAdapter().createConditionLike(runtime, builder, compare, val) ;
			}
		}

		// runtime value
		if(variableType != Condition.VARIABLE_PLACEHOLDER_TYPE_NONE){
			if(null == val){
				runValues.add(new RunValue(this.column, val));
			}else { //多个值 IN 、 BETWEEN 、 FIND_IN_SET
				if (compareCode == 40 || compareCode == 140 || compareCode == 80 || (compareCode >=60 && compareCode <= 62)) {
					List<Object> list = getValues(val);
					if (null != list) {
						for (Object obj : list) {
							runValues.add(new RunValue(this.column, obj));
						}
					}
				} else {
					Object value = getValue(val);
					runValues.add(new RunValue(this.column, value));
				}
			}
		}
		return builder.toString();
	} 

	@SuppressWarnings({"rawtypes" }) 
	public Object getValue(Object src){
		Object value = null; 
		if(null != src){
			if(src instanceof List){
				if(((List) src).size()>0){
					value = ((List)src).get(0); 
				} 
			}else{
				value = src; 
			} 
		}
		return value; 
	} 
	@SuppressWarnings({"unchecked", "rawtypes" }) 
	public List<Object> getValues(Object src){
		List<Object> values = new ArrayList<Object>(); 
		if(null != src){
			if(src instanceof List){
				values = (List)src; 
			}else{
				values.add(src); 
			} 
		}
		return values; 
	} 
	public Object getValue(){
		return getValue(values);
	} 
	public List<Object> getValues(){
		return getValues(values);
	} 
	public Object getOrValue(){
		return getValue(orValues);
	} 
	public List<Object> getOrValues(){
		return getValues(orValues);
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
		setValue = true;
	} 
	public void setOrValues(Object values) {
		this.orValues = values;
		setValue = true;
	} 
	public Compare getCompare() {
		return compare; 
	} 
	public AutoCondition setCompare(Compare compare) {
		this.compare = compare; 
		return this;
	} 
	
	public Compare getOrCompare() {
		return orCompare;
	}
	public AutoCondition setOrCompare(Compare orCompare) {
		this.orCompare = orCompare;
		return this;
	}
	public String toString(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("join", this.getJoin());
		map.put("column", column);
		map.put("compare", compare.getName());
		map.put("values", values);
		return BeanUtil.map2json(map);
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	@Override
	public Condition setRunText(String text) {
		this.text = text;
		return this;
	}
}
