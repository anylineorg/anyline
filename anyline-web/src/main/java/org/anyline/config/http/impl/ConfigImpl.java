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


package org.anyline.config.http.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.ConfigParser;
import org.anyline.config.ParseResult;
import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.SQL.COMPARE_TYPE;
import org.anyline.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.anyline.util.BasicUtil;
import org.apache.log4j.Logger;

public class ConfigImpl implements Config,Serializable{
	private static final long serialVersionUID = 1L;

	protected static final Logger log = Logger.getLogger(ConfigImpl.class);
	//从request 取值方式

//	private String id;				//id
//	private String variable;		//变量名(id.variable:request参数名)
//	private String key;				//http参数key
	private List<Object> values;	//VALUE
//	private int compare;			//比较方式
//	private String className;		//预处理类
//	private String methodName;		//预处理方法
	private boolean empty;			//是否值为空
//	private boolean require;		//是否必须参数
//	private String join = Condition.CONDITION_JOIN_TYPE_AND;			//拼接方式
	
//	private boolean isKeyEncrypt;	//是否HTTP参数名经过加密
//	private boolean isValueEncrypt;	//是否HTTP参数值经过加密
//	
//	int fetchValueType = FETCH_REQUEST_VALUE_TYPE_SINGLE;	//从request取值方式 单个||数组
	private ParseResult parser;
	@Override
	public Object clone(){
		ConfigImpl config = new ConfigImpl();
		config.parser = this.parser;
//		config.id = this.id;
//		config.variable = this.variable;
//		config.key = this.key;
//		config.compare = this.compare;
//		config.join = this.join;
//		config.className = this.className;
//		config.methodName = this.methodName;
		config.empty = this.empty;
//		config.require = this.require;
//		config.isKeyEncrypt = this.isKeyEncrypt; 
//		config.isValueEncrypt = this.isValueEncrypt;
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
		String str = "";
		str = "id:"+ this.getId() +",key:"+this.getKey() 
				+ ", compare:"+this.getCompare().getCode()
				+ ", values:";
		if(null != values){
			for(Object value:values){
				str += " " + value;
			}
		}
		return str;
	}
	public String cacheKey(){
		String str = "";
		str = "id:"+ this.getId() 
				+ ", compare:"+this.getCompare().getCode()
				+ ", values:";
		if(null != values){
			for(Object value:values){
				str += " " + value;
			}
		}
		return str;
	}

	/**
	 * 解析配置
	 * 		[+]	SQL参数名	[.SQL变量名]	:	[>=]request参数名		:默认值
	 * 										[request参数名]
	 * 										%request参数名%
	 * 						
	 * @param config
	 * @return
	 */
	public ConfigImpl(String config){
		parser = ConfigParser.parse(config, true);
//		join = Condition.CONDITION_JOIN_TYPE_AND;
//		/*确定id variable require*/
//		id = config.substring(0,config.indexOf(":"));
//		if(id.startsWith("+")){
//			//必须参数
//			require = true;
//			id = id.substring(1,id.length());
//		}
//		if(id.contains(".")){
//			//XML中自定义参数时,同时指定param.id及变量名
//			variable = id.substring(id.indexOf(".")+1,id.length());
//			id = id.substring(0,id.indexOf("."));
//		}else{
//			//默认变量名
//			//variable = id;
//		}
//		
//		/*取值配置*/
//		String valueConfig = config.substring(config.indexOf(":")+1,config.length());
//		valueConfig = parseCompareType(valueConfig);
//		//解析预处理类.方法
//		valueConfig = parseClassMethod(valueConfig);
//		
//		key = valueConfig;
//		//加密配置
//		key = parseEncrypt();
	}
	/**
	 * 参数加密配置 默认不加密
	 * @param config 参数名　参数名是否加密　参数值是否加密
	 * 只设置一项时　默认为设置参数名加密状态
	 * @return
	 */
//	public String parseEncrypt1(){
//		if(null == key){
//			return null;
//		}
//		if(key.endsWith("+") || key.endsWith("-")){
//			String paramEncrypt = key.substring(key.length()-2,key.length()-1);
//			String valueEncrypt = key.substring(key.length()-1);
//			if("+".equals(paramEncrypt)){
//				isKeyEncrypt = true;
//			}
//			if("+".equals(valueEncrypt)){
//				isValueEncrypt = true;
//			}
//			key = key.replace("+", "").replace("-", "");
//		}
//		return key;
//	}
	/**
	 * 赋值
	 * @param request
	 */
	public void setValue(HttpServletRequest request){
		try{
			values = ConfigParser.getValues(request, parser);
			
			empty = BasicUtil.isEmpty(true, values);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public List<Object> getValues() {
		return values;
	}
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
	/**
	 * 解析key 比较方式 及从request取值方式
	 */
//	private String parseCompareType1(String config){
//		if(config.startsWith(">=")){
//			compare = SQL.COMPARE_TYPE_GREAT_EQUAL;
//			config = config.replace(">=", "");
//		}else if(config.startsWith(">")){
//			compare = SQL.COMPARE_TYPE_GREAT;
//			config = config.replace(">", "");
//		}else if(config.startsWith("<=")){
//			compare = SQL.COMPARE_TYPE_LITTLE_EQUAL;
//			config = config.replace("<=", "");
//		}else if(config.startsWith("<>") || config.startsWith("!=")){
//			compare = SQL.COMPARE_TYPE_NOT_EQUAL;
//			config = config.replace("<>", "").replace("<>", "");
//		}else if(config.startsWith("<")){
//			compare = SQL.COMPARE_TYPE_LITTLE;
//			config = config.replace("<", "");
//		}else if(config.startsWith("[")){
//			compare = SQL.COMPARE_TYPE_IN;
//			config = config.replace("[", "");
//			config = config.replace("]", "");
//			fetchValueType = FETCH_REQUEST_VALUE_TYPE_MULIT;
//		}else if(config.startsWith("%")){
//			if(config.endsWith("%")){
//				compare = SQL.COMPARE_TYPE_LIKE;
//			}else{
//				compare = SQL.COMPARE_TYPE_LIKE_SUBFIX;
//			}
//			config = config.replace("%", "");
//		}else if(config.endsWith("%")){
//			compare = SQL.COMPARE_TYPE_LIKE_PREFIX;
//			config = config.replace("%", "");
//		}else{
//			compare = SQL.COMPARE_TYPE_EQUAL;
//		}
//		return config;
//	}
//	
//	/**
//	 * 解析预处理类.方法
//	 * V2.0只支持一层处理方法
//	 * @param config
//	 */
//	private String parseClassMethod1(String config){
//		if(config.contains("(")){
//			//有预处理方法
//			
//			//解析class.method
//			String classMethod = config.substring(0,config.indexOf("("));
//			if(classMethod.contains(".")){
//				//有特定类
//				className = classMethod.substring(0,classMethod.lastIndexOf("."));
//				methodName = classMethod.substring(classMethod.lastIndexOf(".")+1,classMethod.length());
//			}else{
//				//默认类
//				methodName = classMethod;
//			}
//			config = config.substring(config.indexOf("(")+1,config.indexOf(")"));
//		}
//		return config;
//	}

	/**
	 * 
	 * @param chain 容器
	 * @return
	 */
	public Condition createAutoCondition(ConditionChain chain){
		Condition condition = null;
		if(isRequire() || !isEmpty()){
			if(this instanceof ConfigChain){
				condition = new AutoConditionChainImpl((ConfigChain)this).setJoin(Condition.CONDITION_JOIN_TYPE_AND);
				condition.setContainer(chain);
			}else{
				condition = new AutoConditionImpl(this).setJoin(parser.getJoin());
				condition.setContainer(chain);
			}
		}
		return condition;
	}
	public String getId() {
		return parser.getId();
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
}