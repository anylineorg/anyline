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


package org.anyline.jdbc.config.db.sql.auto.impl; 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicCondition;
import org.anyline.jdbc.config.db.sql.auto.AutoCondition;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public class AutoConditionImpl extends BasicCondition implements AutoCondition{
	private String table;		//表或表别名
	private String column;		//列 
	private Object values;		//参数值 
	private Object orValues;		//参数值 
	private COMPARE_TYPE compare = SQL.COMPARE_TYPE.EQUAL; 
	private COMPARE_TYPE orCompare = SQL.COMPARE_TYPE.EQUAL; 
 
 
	public AutoConditionImpl(Config config){
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
	 * @param	required  是否必须 
	 * @param strictRequired 是否严格验证 如果缺少严格验证的条件 整个SQL不执行
	 * @param	prefix  表
	 * @param	var  列
	 * @param	values 值 
	 * @param	compare  比较方式 
	 */ 
	public AutoConditionImpl(boolean required, boolean strictRequired, String prefix, String var, Object values, COMPARE_TYPE compare){
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
	public AutoConditionImpl(String text){ 
		this.text = text; 
		this.active = true; 
		setVariableType(Condition.VARIABLE_FLAG_TYPE_NONE); 
	} 

//	@SuppressWarnings("unchecked") 
//	public String getRunText(SQLCreater creater){ 
//		String disKeyFr = creater.getDisKeyFr(); 
//		String disKeyTo = creater.getDisKeyTo(); 
//		runValues = new ArrayList<Object>(); /////////////////////////////////////////////////////////////////////////////
//		String text = ""; 
//		if(this.variableType == Condition.VARIABLE_FLAG_TYPE_NONE){  /////////////////////////////////////////////////////////////////////////////
//			//static txt
//			text = this.text; 
//		}else{ 
//			text = disKeyFr + column.replace(".", disKeyTo+"."+disKeyFr) + disKeyTo; 
//			
//			if(compare == SQL.COMPARE_TYPE.EQUAL){ 
//				if(null == getValue() || "NULL".equals(getValue())){ 
//					text += " IS NULL"; 
//					if("NULL".equals(getValue())){
//						this.variableType = Condition.VARIABLE_FLAG_TYPE_NONE;
//					}
//				}else{
//					text += compare.getSql(); 
//				} 
//			}else if(compare == SQL.COMPARE_TYPE.GREAT){ 
//				//text += "> ?";
//				text += compare.getSql(); 
//			}else if(compare == SQL.COMPARE_TYPE.GREAT_EQUAL){ 
//				//text += ">= ?";
//				text += compare.getSql(); 
//			}else if(compare == SQL.COMPARE_TYPE.LESS){ 
//				//text += "< ?";
//				text += compare.getSql(); 
//			}else if(compare == SQL.COMPARE_TYPE.NOT_EQUAL){ 
//				//text += "<> ?";
//				text += compare.getSql(); 
//			}else if(compare == SQL.COMPARE_TYPE.LESS_EQUAL){
//				//text += "<= ?";
//				text += compare.getSql();
//			}else if(compare == SQL.COMPARE_TYPE.BETWEEN){
//				//text += " BETWEEN ? AND ?";
//				text += compare.getSql();
//			}else if(compare == SQL.COMPARE_TYPE.IN || compare == SQL.COMPARE_TYPE.NOT_IN){
//				if(compare == SQL.COMPARE_TYPE.NOT_IN){
//					text += " NOT";
//				}
//				text += " IN ("; 
//				if(values instanceof Collection){ 
//					Collection<Object> coll = (Collection)values; 
//					int size = coll.size(); 
//					for(int i=0; i<size; i++){ 
//						text += "?"; 
//						if(i < size-1){ 
//							text += ","; 
//						} 
//					} 
//					text += ")"; 
//				}else{ 
//					text += "= ?"; 
//				} 
//			}else if(compare == SQL.COMPARE_TYPE.LIKE){
//				text += " LIKE "+ creater.concat("'%'", "?" , "'%'"); 
//			}else if(compare == SQL.COMPARE_TYPE.LIKE_PREFIX){ 
//				text += " LIKE "+ creater.concat("?" , "'%'"); 
//			}else if(compare == SQL.COMPARE_TYPE.LIKE_SUBFIX){ 
//				text += " LIKE "+ creater.concat("'%'", "?"); 
//			}  
//			text += ""; 
//			//runtime value
//			if(compare == SQL.COMPARE_TYPE.IN || compare == SQL.COMPARE_TYPE.NOT_IN || compare == SQL.COMPARE_TYPE.BETWEEN){ 
//				runValues = getValues(); 
//			}else{ 
//				Object value = getValue(); 
//				runValues = new ArrayList<Object>(); 
//				if((null == value || "NULL".equals(value)) && compare == SQL.COMPARE_TYPE.EQUAL){ 
//				}else{ 
//					runValues.add(value); 
//				} 
//			} 
//		} 
//		return text; 
//	} 

	/** 
	 * 运行时文本
	 * @param creater creater
	 * @return String
	 */ 
	public String getRunText(SQLCreater creater){ 
		runValues = new ArrayList<Object>();
		String text = "";
		if(this.variableType == Condition.VARIABLE_FLAG_TYPE_NONE){
			text = this.text; 
		}else{ 
			String txt = "";
			if(BasicUtil.isNotEmpty(true, values) || isRequired()){
				txt = getRunText(creater, values, compare);
				if(BasicUtil.isNotEmpty(txt)){
					text = txt;
				}
				if(BasicUtil.isNotEmpty(true, orValues)){
					txt = getRunText(creater, orValues, orCompare);
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
	public String getRunText(SQLCreater creater, Object val, COMPARE_TYPE compare){ 
		String disKeyFr = creater.getDisKeyFr(); 
		String disKeyTo = creater.getDisKeyTo(); 
		String text = "";
		if(BasicUtil.isNotEmpty(table)){
			text += disKeyFr + table + disKeyTo + ".";
		}
		text += disKeyFr + column + disKeyTo;

		if(compare == SQL.COMPARE_TYPE.EQUAL){
			Object v = getValue(val);
			if(null == v || "NULL".equals(v.toString())){
				text += " IS NULL";
				if("NULL".equals(getValue())){
					this.variableType = Condition.VARIABLE_FLAG_TYPE_NONE;
				}
			}else{
				text += compare.getSql();
			}
		}else if(compare == SQL.COMPARE_TYPE.GREAT){
			//text += "> ?";
			text += compare.getSql();
		}else if(compare == SQL.COMPARE_TYPE.GREAT_EQUAL){
			//text += ">= ?";
			text += compare.getSql();
		}else if(compare == SQL.COMPARE_TYPE.LESS){
			//text += "< ?";
			text += compare.getSql();
		}else if(compare == SQL.COMPARE_TYPE.NOT_EQUAL){
			//text += "<> ?";
			text += compare.getSql();
		}else if(compare == SQL.COMPARE_TYPE.LESS_EQUAL){
			//text += "<= ?";
			text += compare.getSql();
		}else if(compare == SQL.COMPARE_TYPE.BETWEEN){
			//text += " BETWEEN ? AND ?";
			text += compare.getSql();
		}else if(compare == SQL.COMPARE_TYPE.IN || compare == SQL.COMPARE_TYPE.NOT_IN){
			if(compare == SQL.COMPARE_TYPE.NOT_IN){
				text += " NOT";
			}
			text += " IN (";
			if(val instanceof Collection){
				Collection<Object> coll = (Collection)val;
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
		}else if(compare == SQL.COMPARE_TYPE.LIKE){
			text += " LIKE "+ creater.concat("'%'", "?" , "'%'");
		}else if(compare == SQL.COMPARE_TYPE.LIKE_PREFIX){
			text += " LIKE "+ creater.concat("?" , "'%'");
		}else if(compare == SQL.COMPARE_TYPE.LIKE_SUBFIX){
			text += " LIKE "+ creater.concat("'%'", "?");
		}
		text += "";
		//runtime value
		if(compare == SQL.COMPARE_TYPE.IN || compare == SQL.COMPARE_TYPE.NOT_IN || compare == SQL.COMPARE_TYPE.BETWEEN){
			runValues.addAll(getValues(val));
		}else{
			Object value = getValue(val);
			if((null == value || "NULL".equals(value)) && compare == SQL.COMPARE_TYPE.EQUAL){
			}else{
				runValues.add(value);
			}
		}
		return text; 
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
