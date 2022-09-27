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


package org.anyline.jdbc.prepare.auto.init;

import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.prepare.RunPrepare.COMPARE_TYPE;
import org.anyline.jdbc.prepare.auto.AutoCondition;
import org.anyline.jdbc.prepare.init.SimpleCondition;
import org.anyline.jdbc.param.Config;
import org.anyline.jdbc.prepare.Condition;
import org.anyline.jdbc.run.RunValue; 
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.SQLUtil;

import java.util.*;
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public class SimpleAutoCondition extends SimpleCondition implements AutoCondition {
	private String table;		//表或表别名
	private String column;		//列 
	private Object values;		//参数值 
	private Object orValues;		//参数值 
	private COMPARE_TYPE compare = COMPARE_TYPE.EQUAL;
	private COMPARE_TYPE orCompare = COMPARE_TYPE.EQUAL;
 
 
	public SimpleAutoCondition(Config config){
		setJoin(config.getJoin());
		setTable(config.getPrefix());   	//表名或表别名
		setColumn(config.getVariable());   //列名
		setValues(config.getValues()); 
		setOrValues(config.getOrValues());
		setCompare(config.getCompare());
		setRequired(config.isRequire());
		setStrictRequired(config.isStrictRequired()); 
		setVariableType(Condition.VARIABLE_FLAG_TYPE_INDEX); 
		if(config.isRequire()){ 
			setActive(true); 
		} 
	} 
	/** 
	 * @param required  是否必须 
	 * @param strictRequired 是否严格验证 如果缺少严格验证的条件 整个SQL不执行
	 * @param prefix  表
	 * @param var  列
	 * @param values 值 
	 * @param compare  比较方式 
	 */ 
	public SimpleAutoCondition(boolean required, boolean strictRequired, String prefix, String var, Object values, COMPARE_TYPE compare){
		setRequired(required);
		setStrictRequired(strictRequired);
		setTable(prefix);
		setColumn(var);
		setValues(values);
		setCompare(compare); 
		setVariableType(Condition.VARIABLE_FLAG_TYPE_INDEX); 
		if(BasicUtil.isNotEmpty(true,values) || required){ 
			setActive(true); 
		} 
	} 
	public SimpleAutoCondition(String text){
		this.text = text; 
		this.active = true; 
		setVariableType(Condition.VARIABLE_FLAG_TYPE_NONE); 
	} 


	/** 
	 * 运行时文本
	 * @param prefix 前缀
	 * @param adapter adapter
	 * @return String
	 */
	@Override
	public String getRunText(String prefix, JDBCAdapter adapter){
		runValues = new ArrayList<>();
		String text = "";
		if(this.variableType == Condition.VARIABLE_FLAG_TYPE_NONE){
			text = this.text; 
		}else{ 
			String txt = "";
			if(BasicUtil.isNotEmpty(true, values) || isRequired()){
				txt = getRunText(prefix, adapter, values, compare);
				if(BasicUtil.isNotEmpty(txt)){
					text = txt;
				}
				if(BasicUtil.isNotEmpty(true, orValues)){
					txt = getRunText(prefix, adapter, orValues, orCompare);
					if(BasicUtil.isNotEmpty(txt)){
						if(BasicUtil.isEmpty(text)){
							text = txt;
						}else{
							text = "(" + text +" OR " + txt + ")";
						}
					}
				}
			}
		} 
		return text; 
	} 

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getRunText(String prefix, JDBCAdapter adapter, Object val, COMPARE_TYPE compare){
		StringBuilder builder = new StringBuilder();
		String delimiterFr = adapter.getDelimiterFr();
		String delimiterTo = adapter.getDelimiterTo();
		if(!column.contains(".")){
			if(BasicUtil.isNotEmpty(prefix)){
				SQLUtil.delimiter(builder, prefix, delimiterFr, delimiterTo).append(".");
			}else {
				if (BasicUtil.isNotEmpty(table)) {
					SQLUtil.delimiter(builder, table, delimiterFr, delimiterTo).append(".");
				}
			}
		}
		SQLUtil.delimiter(builder, column, delimiterFr, delimiterTo);
		int compareCode = compare.getCode();
		if(compare == COMPARE_TYPE.EQUAL){
			Object v = getValue(val);
			if(null == v || "NULL".equals(v.toString())){
				builder.append(" IS NULL");
				if("NULL".equals(getValue())){
					this.variableType = Condition.VARIABLE_FLAG_TYPE_NONE;
				}
			}else{
				builder.append(compare.getSql());
			}
		}else if(compare == COMPARE_TYPE.GREAT){
			// "> ?";
			builder.append(compare.getSql());
		}else if(compare == COMPARE_TYPE.GREAT_EQUAL){
			//">= ?";
			builder.append(compare.getSql());
		}else if(compare == COMPARE_TYPE.LESS){
			//"< ?";
			builder.append(compare.getSql());
		}else if(compare == COMPARE_TYPE.NOT_EQUAL){
			//"<> ?";
			builder.append(compare.getSql());
		}else if(compare == COMPARE_TYPE.LESS_EQUAL){
			// "<= ?";
			builder.append(compare.getSql());
		}else if(compare == COMPARE_TYPE.BETWEEN){
			// " BETWEEN ? AND ?";
			builder.append(compare.getSql());
		}else if(compare == COMPARE_TYPE.IN || compare == COMPARE_TYPE.NOT_IN){
			adapter.buildConditionIn(builder, compare, val);
		}else if(compareCode >= 50 && compareCode <= 52){
			adapter.buildConditionLike(builder, compare);
		}
		//runtime value
		if(compare == COMPARE_TYPE.IN || compare == COMPARE_TYPE.NOT_IN || compare == COMPARE_TYPE.BETWEEN){
			List<Object> list = getValues(val);
			if(null != list){
				for(Object obj:list){
					runValues.add(new RunValue(this.column, obj));
				}
			}
		}else{
			Object value = getValue(val);
			if((null == value || "NULL".equals(value)) && compare == COMPARE_TYPE.EQUAL){
			}else{
				runValues.add(new RunValue(this.column,value));
			}
		}
		return builder.toString();
	} 

	@SuppressWarnings({ "rawtypes" }) 
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
	@SuppressWarnings({ "unchecked", "rawtypes" }) 
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
	} 
	public void setOrValues(Object values) { 
		this.orValues = values; 
	} 
	public COMPARE_TYPE getCompare() { 
		return compare; 
	} 
	public AutoCondition setCompare(COMPARE_TYPE compare) { 
		this.compare = compare; 
		return this;
	} 
	
	public COMPARE_TYPE getOrCompare() {
		return orCompare;
	}
	public AutoCondition setOrCompare(COMPARE_TYPE orCompare) {
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

}
