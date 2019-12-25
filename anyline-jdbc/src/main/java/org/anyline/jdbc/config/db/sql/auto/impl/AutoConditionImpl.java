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
 *          
 */


package org.anyline.jdbc.config.db.sql.auto.impl; 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.db.impl.BasicCondition;
import org.anyline.jdbc.config.db.sql.auto.AutoCondition;
import org.anyline.jdbc.config.Config;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public class AutoConditionImpl extends BasicCondition implements AutoCondition{ 
	private String column;		//列 
	private Object values;		//参数值 
	private COMPARE_TYPE compare = SQL.COMPARE_TYPE.EQUAL; 
 
 
	public AutoConditionImpl(Config config){
		setJoin(config.getJoin());
		setColumn(config.getId()); 
		setValues(config.getValues()); 
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
	 * @param	column  列 
	 * @param	values 值 
	 * @param	compare  比较方式 
	 */ 
	public AutoConditionImpl(boolean required, boolean strictRequired, String column, Object values, COMPARE_TYPE compare){ 
		setRequired(required);
		setStrictRequired(strictRequired); 
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
		String disKeyTo = creater.getDisKeyTo(); 
		runValues = new ArrayList<Object>(); 
		String text = ""; 
		if(this.variableType == Condition.VARIABLE_FLAG_TYPE_NONE){ 
			//static txt
			text = this.text; 
		}else{ 
			text = disKeyFr + column.replace(".", disKeyTo+"."+disKeyFr) + disKeyTo; 
			if(compare == SQL.COMPARE_TYPE.EQUAL){ 
				if(null == getValue() || "NULL".equals(getValue())){ 
					text += " IS NULL"; 
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
				if(values instanceof Collection){ 
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
				runValues = getValues(); 
			}else{ 
				Object value = getValue(); 
				runValues = new ArrayList<Object>(); 
				if((null == value || "NULL".equals(value)) && compare == SQL.COMPARE_TYPE.EQUAL){ 
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
	public COMPARE_TYPE getCompare() { 
		return compare; 
	} 
	public void setCompare(COMPARE_TYPE compare) { 
		this.compare = compare; 
	} 
	public String toString(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("join", this.getJoin());
		map.put("column", column);
		map.put("compare", compare.getName());
		map.put("values", values);
		return BeanUtil.map2json(map);
	}
} 
