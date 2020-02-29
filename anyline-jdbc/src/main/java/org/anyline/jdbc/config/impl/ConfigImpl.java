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


package org.anyline.jdbc.config.impl; 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.ConfigChain;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class ConfigImpl implements Config{
	protected static final Logger log = LoggerFactory.getLogger(ConfigImpl.class);
	protected List<Object> values;	//VALUE
	protected List<Object> orValues;
	protected boolean empty;			//是否值为空
	protected ParseResult parser;
	@Override
	public Object clone(){
		ConfigImpl config = new ConfigImpl();
		config.parser = this.parser;
		config.empty = this.empty;
		List<Object> values = new ArrayList<Object>();
		for(Object value:this.values){
			values.add(value);
		}
		config.values = values;
		return config;
	} 
	public ConfigImpl(){
		this.parser = new ParseResult();
	} 
	public String toString(){ 
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("join", this.getJoin());
		map.put("id", this.getId());
		map.put("field", this.getField());
		map.put("key", this.getKey());
		map.put("compare", this.getCompare().getCode());
		map.put("values", values);
		return BeanUtil.map2json(map);
	}
	public String cacheKey(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", this.getId());
		map.put("compare", this.getCompare().getCode());
		map.put("values", values);
		return BeanUtil.map2json(map);
	} 
 
	/** 
	 * 解析配置 
	 * 		[+]	SQL参数名	[.SQL变量名]	:	[&gt;=]request参数名		:默认值 
	 * 										[request参数名] 
	 * 										%request参数名% 
	 * 						 
	 * @param config  config
	 */ 
	public ConfigImpl(String config){
		parser = ConfigParser.parse(config, true); 
	}
	public void setValue(Map<String,Object> values){ 
		try{
			this.values = ConfigParser.getValues(values, parser);
			empty = BasicUtil.isEmpty(true, this.values); 
			setOrValue(values);
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	} 
	public void setOrValue(Map<String,Object> values){ 
		try{
			this.orValues = ConfigParser.getValues(values, parser.getOr());
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	} 

	public List<Object> getValues() { 
		return values; 
	} 
	public List<Object> getOrValues() { 
		return orValues; 
	} 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addValue(Object value){
		if(null == values){
			values = new ArrayList<Object>();
		}
		if(null != value && value instanceof Collection){
			values.addAll((Collection)value);
		}else{
			values.add(value);
		}
	}
	public void setValue(Object value){
		values = new ArrayList<Object>();
		addValue(value);
	} 
	public void setOrValue(Object value){
		orValues = new ArrayList<Object>();
		addValue(value);
	} 
	/** 
	 *  createAutoCondition
	 * @param chain 容器 
	 * @return return
	 */ 
	public Condition createAutoCondition(ConditionChain chain){ 
		Condition condition = null; 
		if(isRequire() || !isEmpty()){
			if(this instanceof ConfigChain){
				condition = new AutoConditionChainImpl((ConfigChain)this).setJoin(Condition.CONDITION_JOIN_TYPE_AND);
				condition.setContainer(chain);
			}else{
				condition = new AutoConditionImpl(this).setOrCompare(getOrCompare()).setJoin(parser.getJoin()); 
				condition.setContainer(chain);
			} 
		} 
		return condition; 
	} 
	public String getId() { 
		return parser.getId(); 
	} 
 	public String getField(){
		return parser.getField();
	}
	public void setId(String id) { 
		parser.setId(id); 
	} 
 
	public String getVariable() { 
		return parser.getField(); 
	} 
 
	public void setVariable(String variable) { 
		parser.setField(variable); 
	} 
 
 
 
	public String getKey() { 
		return parser.getKey(); 
	} 
 
	public void setKey(String key) { 
		parser.setKey(key); 
	} 
 
	public COMPARE_TYPE getCompare() { 
		return parser.getCompare(); 
	} 
 
	public void setCompare(COMPARE_TYPE compare) { 
		parser.setCompare(compare); 
	} 
 
	public boolean isEmpty() { 
		return empty; 
	} 
 
	public void setEmpty(boolean empty) { 
		this.empty = empty; 
	} 
 
	public boolean isRequire() { 
		return parser.isRequired(); 
	} 
	 
	public void setRequire(boolean require) { 
		parser.setRequired(require); 
	} 
	public boolean isStrictRequired() {
		return parser.isStrictRequired();
	}
	public void setStrictRequired(boolean strictRequired) {
		parser.setStrictRequired(strictRequired);
	} 
	public String getJoin() { 
		return parser.getJoin(); 
	} 
 
	public void setJoin(String join) {
		parser.setJoin(join);
	} 
 
	public boolean isKeyEncrypt() { 
		return parser.isKeyEncrypt(); 
	} 
 
	public boolean isValueEncrypt() { 
		return parser.isValueEncrypt(); 
	}
	@Override
	public COMPARE_TYPE getOrCompare() {
		ParseResult or = parser.getOr();
		if(null != or){
			return or.getCompare();
		}
		return parser.getCompare();
	}
	@Override
	public void setOrCompare(COMPARE_TYPE compare) {
		ParseResult or = parser.getOr();
		if(null != or){
			or.setCompare(compare);
		}
	}
	public void setTable(String table){

	}
	public String getTable(){
		return parser.getField();
	}
}
