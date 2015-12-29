
/**
 * V2.0
 */
package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.db.SQLVariable;

public class SQLVariableImpl implements SQLVariable{
	public static final int VAR_TYPE_INDEX			= 0;		//根据下标区分 CD = ?
	public static final int VAR_TYPE_KEY			= 1;		//根据KEY区分  CD = :CD
	public static final int VAR_TYPE_KEY_REPLACE	= 2;		//字符替换 CD=':CD'
	public static final int VAR_TYPE_REPLACE		= 3;		//字符替换 CD=::CD
	


	private String key;				//变量KEY
	private List<Object> values;	//变量值
	private int type;				//变量识别方式
	private int compare;			//比较方式
	public Object clone() throws CloneNotSupportedException{
		SQLVariableImpl clone = (SQLVariableImpl) super.clone();
		if(null != values){
			List<Object> cValues = new ArrayList<Object>();
			for(Object value:values){
				Object tmp = value;
				cValues.add(tmp);
			}
			clone.values = cValues;
		}
		return clone;
	}
	
	public void init(){
		if(null != values){
			values.clear();
		}
	}
	public int getCompare() {
		return compare;
	}
	public void setCompare(int compare) {
		this.compare = compare;
	}
	public void addValue(Object value){
		if(null == values){
			values = new ArrayList<Object>();
		}
		values.add(value);
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public List<Object> getValues() {
		return values;
	}
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if(null == value){
			return;
		}
		if(null == values){
			values = new ArrayList<Object>();
		}
		if(value instanceof Collection){
			values.addAll((Collection)value);
		}else{
			values.add(value);
		}
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
